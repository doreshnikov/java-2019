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
 * @version 1.0.0
 */
public class BasicIterativeParallelism implements ListIP {

    /**
     * Default constructor.
     * Creates a BasicIterativeParallelism instance.
     */
    public BasicIterativeParallelism() {
    }

    private <T> List<T> collect(Stream<? extends Stream<? extends T>> streams) {
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

    /**
     * Joins values to string.
     *
     * @param threads number of concurrent threads
     * @param values values to join
     *
     * @return list of joined result of {@link #toString()} call on each value
     *
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threads number of concurrent threads
     * @param values values to filter
     * @param predicate filter predicate
     *
     * @return list of values satisfying given predicated. Order of values is preserved
     *
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.filter(predicate),
                this::collect);
    }

    /**
     * Maps values.
     *
     * @param threads number of concurrent threads
     * @param values values to map
     * @param f mapper function
     *
     * @return list of values mapped by given function
     *
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.map(f),
                this::collect);
    }

    /**
     * Returns maximum value.
     *
     * @param threads number or concurrent threads
     * @param values values to get maximum of
     * @param comparator value comparator
     * @param <T> value type
     *
     * @return maximum of given values
     *
     * @throws InterruptedException if executing thread was interrupted
     * @throws java.util.NoSuchElementException if no values are given
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.max(comparator).orElse(null),
                stream -> stream.max(comparator).orElse(null));
    }

    /**
     * Returns minimum value.
     *
     * @param threads number or concurrent threads
     * @param values values to get minimum of
     * @param comparator value comparator
     * @param <T> value type
     *
     * @return minimum of given values
     *
     * @throws InterruptedException if executing thread was interrupted
     * @throws java.util.NoSuchElementException if no values are given
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.min(comparator).orElse(null),
                stream -> stream.min(comparator).orElse(null));
    }

    /**
     * Checks if all values satisfy given predicate.
     *
     * @param threads number or concurrent threads
     * @param values values to check predicate on
     * @param predicate values predicate
     * @param <T> value type
     *
     * @return {@code boolean} value indicating if all values satisfy given predicate
     *
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * Checks if any value satisfies given predicate.
     *
     * @param threads number or concurrent threads
     * @param values values to check predicate on
     * @param predicate values predicate
     * @param <T> value type
     *
     * @return {@code boolean} value indicating if any value satisfies given predicate
     *
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }
}
