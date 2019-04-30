package ru.ifmo.rain.oreshnikov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * @author doreshnikov
 * @version 1.0.0
 */
public class ParallelMapperImpl implements ParallelMapper {

    private List<Thread> workers;
    private final Queue<Runnable> tasks;

    private final int threads;

    private static final int MAX_TASKS = 1000000;

    /**
     * Thread-count constructor.
     * Creates a ParallelMapperImpl instance operating with maximum of {@code threads}
     * threads of type {@link Thread}.
     *
     * @param threads maximum count of operable threads
     */
    public ParallelMapperImpl(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Threads number must be positive");
        }
        this.threads = threads;
        tasks = new ArrayDeque<>();
        workers = new ArrayList<>();
        Runnable BASE_TASK = () -> {
            try {
                while (!Thread.interrupted()) {
                    synchronizedRunTask();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(BASE_TASK));
        }
        workers.forEach(Thread::start);
    }

    private void synchronizedRunTask() throws InterruptedException {
        Runnable task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
            tasks.notifyAll();
        }
        task.run();
    }

    private void synchronizedAddTask(final Runnable task) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.size() >= MAX_TASKS) {
                tasks.wait();
            }
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    /**
     * Getter for thread count.
     *
     * @return maximum thread count {@code threads}
     */
    public int getThreads() {
        return threads;
    }

    private class SynchronizedCollector<R> {
        private List<R> results;
        private int set;

        SynchronizedCollector(int size) {
            this.set = 0;
            results = new ArrayList<>(Collections.nCopies(size, null));
        }

        synchronized void synchronizedSet(final int position, R element) {
            results.set(position, element);
            set++;
            if (set == results.size()) {
                notify();
            }
        }

        synchronized List<R> asList() throws InterruptedException {
            while (set < results.size()) {
                wait();
            }
            return results;
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @param f    mapping function
     * @param args arguments for mapping function
     * @param <T>  type of arguments
     * @param <R>  type of resulting values
     * @return {@link List} of mapping results
     * @throws InterruptedException if some of the threads were interrupted during execution
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        SynchronizedCollector<R> collector = new SynchronizedCollector<>(args.size());
        List<RuntimeException> runtimeExceptions = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            synchronizedAddTask(() -> {
                R value = null;
                try {
                    value = f.apply(args.get(index));
                } catch (RuntimeException e) {
                    synchronized (runtimeExceptions) {
                        runtimeExceptions.add(e);
                    }
                }
                collector.synchronizedSet(index, value);
            });
        }
        if (!runtimeExceptions.isEmpty()) {
            RuntimeException mapFail = new RuntimeException("Errors occured while mapping some of the values");
            runtimeExceptions.forEach(mapFail::addSuppressed);
            throw mapFail;
        }
        return collector.asList();
    }

    /**
     * Stops all threads. All unfinished mappings leave in undefined state.
     */
    @Override
    public void close() {
        workers.forEach(Thread::interrupt);
        IterativeParallelism.joinAllNothrow(workers);
    }

}
