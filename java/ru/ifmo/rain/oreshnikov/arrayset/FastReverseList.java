package ru.ifmo.rain.oreshnikov.arrayset;

import java.util.*;

/**
 * @author doreshnikov
 * @date 19-Feb-19
 */

class FastReverseList<T> extends AbstractList<T> {
    private final List<T> elements;
    private boolean reversed;

    /*
    Initializer constructor
     */
    FastReverseList(List<T> data) {
        elements = data;
        reversed = false;
    }

    /*
    Reverse constructor
     */
    FastReverseList(FastReverseList<T> origin) {
        elements = origin.elements;
        reversed = !origin.reversed;
    }

    @Override
    public T get(int index) {
        return elements.get(reversed ? elements.size() - index - 1 : index);
    }

    @Override
    public int size() {
        return elements.size();
    }
}
