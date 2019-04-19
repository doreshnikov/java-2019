package ru.ifmo.rain.oreshnikov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

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
public class IterativeParallelism implements ListIP {

    private final ParallelMapper mapper;

    /**
     * Default constructor.
     * Creates an IterativeParallelism instance operating without {@link ParallelMapper}.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Mapper constructor.
     * Creates an IterativeParallelism instance with {@link ParallelMapper} as a core mapper.
     *
     * @param mapper {@link ParallelMapper} instance
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    private <T> List<T> collect(Stream<? extends Stream<? extends T>> streams) {
        return streams.flatMap(Function.identity()).collect(Collectors.toList());
    }

    private <T> List<Stream<? extends T>> split(int threads, List<? extends T> values) {
        List<Stream<? extends T>> parts = new ArrayList<>();
        int block = values.size() / threads;
        int remainder = values.size() % threads;

        int position = 0;
        for (int i = 0; i < threads; i++) {
            int currentBlock = block + (i < remainder ? 1 : 0);
            if (currentBlock > 0) {
                parts.add(values.subList(position, position + currentBlock).stream());
            }
            position += currentBlock;
        }
        return parts;
    }

    private <T, M, R> R parallelRun(int threads, List<? extends T> values,
                                    Function<Stream<? extends T>, M> process,
                                    Function<Stream<? extends M>, R> reduce) throws InterruptedException {
        List<Stream<? extends T>> parts = split(threads, values);
        List<M> counted;

        if (mapper == null) {
            counted = new ArrayList<>(Collections.nCopies(parts.size(), null));
            List<Thread> workers = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) {
                final int index = i;
                Thread thread = new Thread(() -> counted.set(index, process.apply(parts.get(index))));
                workers.add(thread);
                thread.start();
            }
            joinAll(workers);
        } else {
            counted = mapper.map(process, parts);
        }

        return reduce.apply(counted.stream());
    }

    /**
     * Joins values to string.
     *
     * @param threads number of concurrent threads
     * @param values  values to join
     * @return list of joined result of {@link #toString()} call on each value
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
     * @param threads   number of concurrent threads
     * @param values    values to filter
     * @param predicate filter predicate
     * @return list of values satisfying given predicated. Order of values is preserved
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
     * @param values  values to map
     * @param f       mapper function
     * @return list of values mapped by given function
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f)
            throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.map(f),
                this::collect);
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number or concurrent threads
     * @param values     values to get maximum of
     * @param comparator value comparator
     * @param <T>        value type
     * @return maximum of given values
     * @throws InterruptedException             if executing thread was interrupted
     * @throws java.util.NoSuchElementException if no values are given
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.max(comparator).get(),
                stream -> stream.max(comparator).get());
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number or concurrent threads
     * @param values     values to get minimum of
     * @param comparator value comparator
     * @param <T>        value type
     * @return minimum of given values
     * @throws InterruptedException             if executing thread was interrupted
     * @throws java.util.NoSuchElementException if no values are given
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.min(comparator).get(),
                stream -> stream.min(comparator).get());
    }

    /**
     * Checks if all values satisfy given predicate.
     *
     * @param threads   number or concurrent threads
     * @param values    values to check predicate on
     * @param predicate values predicate
     * @param <T>       value type
     * @return {@code boolean} value indicating if all values satisfy given predicate
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * Checks if any value satisfies given predicate.
     *
     * @param threads   number or concurrent threads
     * @param values    values to check predicate on
     * @param predicate values predicate
     * @param <T>       value type
     * @return {@code boolean} value indicating if any value satisfies given predicate
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return parallelRun(threads, values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }

    static void joinAll(List<Thread> workers) throws InterruptedException {
        List<InterruptedException> exceptions = new ArrayList<>();
        workers.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                exceptions.add(e);
            }
        });
        if (!exceptions.isEmpty()) {
            InterruptedException joinFail = new InterruptedException("Some threads were interrupted");
            exceptions.forEach(joinFail::addSuppressed);
            throw joinFail;
        }
    }

    static void joinAllNothrow(List<Thread> workers) {
        workers.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        });
    }

}
