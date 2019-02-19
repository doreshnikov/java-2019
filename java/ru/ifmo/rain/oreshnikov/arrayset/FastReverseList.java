package ru.ifmo.rain.oreshnikov.arrayset;

import java.util.*;

/**
 * @author doreshnikov
 * @date 19-Feb-19
 */

class FastReverseList<T> extends AbstractList<T> {
    private final List<T> data;
    private boolean reversed;

    /*
    Initializer + reverse constructor
     */
    FastReverseList(List<T> data) {
        if (data instanceof FastReverseList) {
            FastReverseList<T> list = (FastReverseList<T>) data;
            this.data = list.data;
            this.reversed = !list.reversed;
        } else {
            this.data = data;
            reversed = false;
        }
    }

    @Override
    public T get(int index) {
        return data.get(reversed ? data.size() - index - 1 : index);
    }

    @Override
    public int size() {
        return data.size();
    }
}
