package ru.ifmo.rain.oreshnikov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

/**
 * @author doreshnikov
 * @date 24-Mar-19
 */

public class ParallelMapperImpl implements ParallelMapper {

    private List<Thread> workers;
    private Queue<Thread> tasks;
    private int threads;

    ParallelMapperImpl(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Threads number must be positive");
        }
        this.threads = threads;
        tasks = new ArrayDeque<>();
        workers = new ArrayList<>();
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        return null;
    }

    @Override
    public void close() {

    }

}
