package ru.ifmo.ctddev.zhuchkova.concurrent;

import java.util.*;
import java.util.function.*;

public class Runner<T, R> implements Runnable {
    private T list;
    private Function<T, R> function;
    private R res;

    Runner(T list, Function<T, R> function) {
        this.list = list;
        this.function = function;
    }

    @Override
    public void run() {
        res = function.apply(list);
    }

    R getResult() {
        return res;
    }
}