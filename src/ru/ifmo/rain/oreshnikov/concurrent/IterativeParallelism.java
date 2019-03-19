package ru.ifmo.rain.oreshnikov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author doreshnikov
 * @date 19-Mar-19
 */

public class IterativeParallelism implements ListIP {
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return String.join("", map(threads, values, Object::toString));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return values.stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return values.stream().map(f).collect(Collectors.toList());
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return values.stream().max(comparator).orElse(null);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return values.stream().min(comparator).orElse(null);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return values.stream().allMatch(predicate);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return values.stream().anyMatch(predicate);
    }
}
