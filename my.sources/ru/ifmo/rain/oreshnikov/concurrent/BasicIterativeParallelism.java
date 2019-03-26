package ru.ifmo.rain.oreshnikov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author doreshnikov
 * @date 19-Mar-19
 */

public class BasicIterativeParallelism implements ListIP {

    public BasicIterativeParallelism() {
    }

    static void joinAll(List<Thread> workers, boolean nothrow) throws InterruptedException {
        InterruptedException joinFail = new InterruptedException("Some threads were interrupted");
        workers.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                joinFail.addSuppressed(e);
            }
        });
        if (!nothrow && joinFail.getSuppressed().length != 0) {
            throw joinFail;
        }
    }

    protected <T> List<T> collect(Stream<? extends Stream<? extends T>> streams) {
        return streams.flatMap(Function.identity()).collect(Collectors.toList());
    }

    protected <T> List<Stream<? extends T>> split(int threads, List<? extends T> values) {
        List<Stream<? extends T>> parts = new ArrayList<>();
        int block = (values.size() + threads - 1) / threads;
        for (int i = 0; i * block < values.size(); i++) {
            parts.add(values.subList(i * block, Math.min((i + 1) * block, values.size())).stream());
        }
        return parts;
    }

    protected <T, M, R> R parallelRun(int threads, List<? extends T> values,
                                      Function<Stream<? extends T>, M> process,
                                      Function<Stream<? extends M>, R> reduce) throws InterruptedException {
        List<Stream<? extends T>> parts = split(threads, values);
        List<M> counted = new ArrayList<>(Collections.nCopies(parts.size(), null));
        List<Thread> workers = new ArrayList<>();

        for (int i = 0; i < parts.size(); i++) {
            final int index = i;
            Thread thread = new Thread(() -> counted.set(index, process.apply(parts.get(index))));
            workers.add(thread);
            thread.start();
        }
        for (Thread t : workers) {
            t.join();
        }
        return reduce.apply(counted.stream());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.filter(predicate),
                this::collect);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.map(f),
                this::collect);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.max(comparator).orElseGet(() -> null),
                stream -> stream.max(comparator).orElseGet(() -> null));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.min(comparator).orElseGet(() -> null),
                stream -> stream.min(comparator).orElseGet(() -> null));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }
}
