package ru.ifmo.rain.oreshnikov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * @author doreshnikov
 * @date 24-Mar-19
 */

public class ParallelMapperImpl implements ParallelMapper {

    private List<Thread> workers;
    private final Queue<Runnable> tasks;

    private static final Runnable EMPTY_TASK = () -> {
        // do nothing
    };

    public ParallelMapperImpl(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Threads number must be positive");
        }
        tasks = new ArrayDeque<>();
        workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        synchronizedRunTask();
                    }
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        workers.forEach(Thread::start);
    }

    private void synchronizedRunTask() {
        Runnable task = EMPTY_TASK;
        synchronized (tasks) {
            if (!tasks.isEmpty()) {
                task = tasks.poll();
            }
            tasks.notifyAll();
        }
        task.run();
    }

    private void synchronizedAddTask(final Runnable task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    class SynchronizedCollector<R> {
        private List<R> results;
        private int size;
        private int set;

        SynchronizedCollector(int size) {
            this.size = size;
            this.set = 0;
            results = Collections.nCopies(size, null);
        }

        void synchronizedSet(final int position, R element) {
            results.set(position, element);
            synchronized (this) {
                set++;
                if (set == size) {
                    notify();
                }
            }
        }

        List<R> asList() throws InterruptedException {
            while (set < size) {
                wait();
            }
            return results;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        SynchronizedCollector<R> collector = new SynchronizedCollector<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            synchronizedAddTask(() -> {
                collector.synchronizedSet(index, f.apply(args.get(index)));
            });
        }
        return collector.asList();
    }

    @Override
    public void close() {
        workers.forEach(Thread::interrupt);
        try {
            IterativeParallelism.joinAll(workers,true);
        } catch (InterruptedException ignored) {
        }
    }

}
