package ru.ifmo.rain.oreshnikov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = (perHost / 3 + 1);

        downloadersPool = Executors.newFixedThreadPool(downloaders);
        extractorsPool = Executors.newFixedThreadPool(extractors);
        hostMapper = new ConcurrentHashMap<>();
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

        private final String host;

        HostDownloader(String s) {
            waitingTasks = new ArrayDeque<>();
            currentlyRunning = 0;
            host = s;
        }

        synchronized private void callNext() {
            Runnable task = waitingTasks.poll();
            if (task != null) {
                currentlyRunning++;
                downloadersPool.submit(() -> {
                    try {
                        task.run();
                    } finally {
                        currentlyRunning--;
                    }
                });
            }
        }

        synchronized void addTask(Runnable task) {
            waitingTasks.add(task);
            if (currentlyRunning < perHost) {
                callNext();
            }
        }

    }

    private class Worker {

        private final Set<String> success;
        private final ConcurrentHashMap<String, IOException> fail;
        private final Set<String> completed;
        private final Phaser lock;

        Worker(String url) {
            success = ConcurrentHashMap.newKeySet();
            fail = new ConcurrentHashMap<>();
            completed = ConcurrentHashMap.newKeySet();
            completed.add(url);
            lock = new Phaser(1);
        }

        private void queueExtraction(final Document document, int depth) {
            try {
                document.extractLinks().stream().filter(completed::add).forEach(link -> {
                    queueDownload(link, depth);
                });
            } catch (IOException ignored) {
            } finally {
                lock.arrive();
            }
        }

        void queueDownload(final String link, final int depth) {
            String host;
            try {
                host = URLUtils.getHost(link);
            } catch (MalformedURLException e) {
                fail.put(link, e);
                return;
            }

            HostDownloader hostDownloader = hostMapper.computeIfAbsent(host, s -> new HostDownloader(s));
            lock.register();
            hostDownloader.addTask(() -> {
                try {
                    Document document = downloader.download(link);
                    success.add(link);
                    if (depth > 1) {
                        lock.register();
                        extractorsPool.submit(() -> queueExtraction(document, depth - 1));
                    }
                } catch (IOException e) {
                    fail.put(link, e);
                } finally {
                    lock.arrive();
                }
            });
        }

        void awaitCompletion() {
            lock.arriveAndAwaitAdvance();
        }

    }

    @Override
    public Result download(String url, int depth) {
        Worker worker = new Worker(url);
        worker.queueDownload(url, depth);
        worker.awaitCompletion();
        return new Result(new ArrayList<>(worker.success), worker.fail);
    }

    @Override
    public void close() {
        extractorsPool.shutdownNow();
        downloadersPool.shutdownNow();
    }

}
