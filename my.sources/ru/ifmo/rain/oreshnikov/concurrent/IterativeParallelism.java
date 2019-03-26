package ru.ifmo.rain.oreshnikov.concurrent;

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
 * @date 19-Mar-19
 */

public class IterativeParallelism extends BasicIterativeParallelism {

    private final ParallelMapper mapper;

    public IterativeParallelism() {
        mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected <T, M, R> R parallelRun(int threads, List<? extends T> values,
                                      Function<Stream<? extends T>, M> process,
                                      Function<Stream<? extends M>, R> reduce) throws InterruptedException {
        if (mapper == null) {
            return super.parallelRun(threads, values, process, reduce);
        } else {
            List<Stream<? extends T>> parts = split(threads, values);
            return reduce.apply(mapper.map(process, parts).stream());
        }
    }
}
