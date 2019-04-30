package ru.ifmo.rain.oreshnikov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author doreshnikov
 * @date 19-Apr-19
 */

public class WebCrawler implements Crawler {

    private static final String USAGE = "Usage: WebCrawler url [depth [downloaders [extractors [perHost]]]]\n" +
            "\t (all optional values being equal to 1 by default)";

    private final Downloader downloader;

    private final int perHost;

    private final ExecutorService extractorsPool;
    private final ExecutorService downloadersPool;
    private final ConcurrentMap<String, HostDownloader> hostMapper;

    private final List<Phaser> locks;
    private static final int MAX_WORKERS = 100000;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;

        downloadersPool = Executors.newFixedThreadPool(downloaders);
        extractorsPool = Executors.newFixedThreadPool(extractors);
        hostMapper = new ConcurrentHashMap<>();

        locks = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Expected not null arguments and at least one argument specified\n" + USAGE);
        } else {
            try {
                int depth = getArgument(args, 1);
                int downloaders = getArgument(args, 2);
                int extractors = getArgument(args, 3);
                int perHost = getArgument(args, 4);

                new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)
                        .download(args[0], depth);
            } catch (NumberFormatException e) {
                System.err.println("Expected numeric arguments only\n" + USAGE);
            } catch (IOException e) {
                System.err.println("Unable to initialize downloader: " + e.getMessage());
            }
        }
    }

    private static int getArgument(String[] args, int index) {
        return index >= args.length ? 1 : Integer.parseInt(args[index]);
    }

    private class HostDownloader {
        private final Queue<Runnable> waitingTasks;
        private int currentlyRunning;

        HostDownloader() {
            waitingTasks = new ArrayDeque<>();
            currentlyRunning = 0;
        }

        synchronized private void checkedCall(boolean finished) {
            if (finished) {
                currentlyRunning--;
            }
            if (currentlyRunning < perHost) {
                callNext();
            }
        }

        synchronized private void callNext() {
            Runnable task = waitingTasks.poll();
            if (task != null) {
                currentlyRunning++;
                downloadersPool.submit(() -> {
                    try {
                        task.run();
                    } finally {
                        checkedCall(true);
                    }
                });
            }
        }

        synchronized void addTask(Runnable task) {
            waitingTasks.add(task);
            checkedCall(false);
        }
    }

    private class Worker {

        private final Set<String> success;
        private final ConcurrentMap<String, IOException> fail;
        private final Set<String> completed;

        private final Phaser lock;
        private final List<List<String>> awaits;

        Worker(String url, int depth) {
            success = ConcurrentHashMap.newKeySet();
            completed = ConcurrentHashMap.newKeySet();
            fail = new ConcurrentHashMap<>();

            awaits = new ArrayList<>();
            for (int i = 0; i <= depth; i++) {
                awaits.add(new ArrayList<>());
            }
            awaits.get(depth).add(url);
            this.lock = new Phaser(1);

            run(depth);
        }

        private void run(final int depth) {
            lock.register();
            final Phaser level = new Phaser(1);
            synchronized (awaits) {
                awaits.get(depth).stream()
                        .filter(completed::add)
                        .forEach(link -> queueDownload(link, depth, level));
            }
            level.arriveAndAwaitAdvance();
            if (depth > 0) {
                run(depth - 1);
            }
            lock.arrive();
        }

        private void queueExtraction(final Document document, final int depth, final Phaser level) {
            level.register();
            extractorsPool.submit(() -> {
                try {
                    List<String> links = document.extractLinks();
                    synchronized (awaits) {
                        awaits.get(depth).addAll(links);
                    }
                } catch (IOException ignored) {
                } finally {
                    level.arrive();
                }
            });
        }

        void queueDownload(final String link, final int depth, final Phaser level) {
            String host;
            try {
                host = URLUtils.getHost(link);
            } catch (MalformedURLException e) {
                fail.put(link, e);
                return;
            }

            HostDownloader hostDownloader = hostMapper.computeIfAbsent(host, s -> new HostDownloader());
            level.register();
            hostDownloader.addTask(() -> {
                try {
                    Document document = downloader.download(link);
                    success.add(link);
                    if (depth > 1) {
                        queueExtraction(document, depth - 1, level);
                    }
                } catch (IOException e) {
                    fail.put(link, e);
                } finally {
                    level.arrive();
                }
            });
        }

        Result getResult() {
            lock.arriveAndAwaitAdvance();
            return new Result(new ArrayList<>(success), fail);
        }
    }

    @Override
    public Result download(String url, int depth) {
        return new Worker(url, depth).getResult();
    }

    @Override
    public void close() {
        extractorsPool.shutdown();
        downloadersPool.shutdown();
        try {
            extractorsPool.awaitTermination(0, TimeUnit.MILLISECONDS);
            downloadersPool.awaitTermination(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
