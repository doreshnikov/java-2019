package ru.ifmo.rain.oreshnikov.arrayset;

import java.util.*;

/**
 * @author doreshnikov
 * @date 18-Feb-19
 */

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> elements;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        elements = Collections.emptyList();
        comparator = null;
    }

    public ArraySet(Collection<? extends T> collection) {
        elements = List.copyOf(new TreeSet<>(collection));
        comparator = null;
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        TreeSet<T> temporary = new TreeSet<>(comparator);
        temporary.addAll(collection);
        elements = List.copyOf(temporary);
        this.comparator = comparator;
    }

    /*
    Non-sorting constructor
    */
    private ArraySet(List<T> elements, Comparator<? super T> comparator) {
        this.elements = elements;
        this.comparator = comparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        try {
            return insertionIndex((T) Objects.requireNonNull(o)) >= 0;
        } catch (ClassCastException e) {
            return false;
        }
    }

    private T checkedGet(int index, boolean nothrow) {
        if (-1 < index && index < elements.size()) {
            return elements.get(index);
        } else if (nothrow) {
            return null;
        } else {
            throw new NoSuchElementException("Attempted to access a non-existent element");
        }
    }

    private T checkedGet(int index) {
        return checkedGet(index, true);
    }

    private int insertionIndex(T t) {
        return Collections.binarySearch(elements, Objects.requireNonNull(t), comparator);
    }

    private int lowerIndex(T t, boolean inclusive) {
        int index = insertionIndex(t);
        if (index < 0) {
            return -index - 2;
        }
        return inclusive ? index : index - 1;
    }

    private int higherIndex(T t, boolean inclusive) {
        int index = insertionIndex(t);
        if (index < 0) {
            return -index - 1;
        }
        return inclusive ? index : index + 1;
    }

    @Override
    public T lower(T t) {
        return checkedGet(lowerIndex(t, false));
    }

    @Override
    public T floor(T t) {
        return checkedGet(lowerIndex(t, true));
    }

    @Override
    public T higher(T t) {
        return checkedGet(higherIndex(t, false));
    }

    @Override
    public T ceiling(T t) {
        return checkedGet(higherIndex(t, true));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("Poll first is not supported");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("Poll last is not supported");
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }

    @Override
    public ArraySet<T> descendingSet() {
        return new ArraySet<>(new FastReverseList<>(elements), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private ArraySet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive,
                               boolean nothrow) {
        if (fromElement == null || toElement == null) {
            throw new IllegalArgumentException("Not null elements expected");
        }
        int fromIndex = higherIndex(fromElement, fromInclusive);
        int toIndex = lowerIndex(toElement, toInclusive);
        return toIndex < fromIndex ?
                new ArraySet<>(Collections.emptyList(), comparator) :
                new ArraySet<>(elements.subList(fromIndex, toIndex + 1), comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArraySet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        if (comparator != null && comparator.compare(fromElement, toElement) > 0 ||
                comparator == null && fromElement instanceof Comparable &&
                        ((Comparable) fromElement).compareTo(toElement) > 0) {
            throw new IllegalArgumentException("Left bound should be not greater than the right one");
        }
        return subSet(fromElement, fromInclusive, toElement, toInclusive, false);
    }

    @Override
    public ArraySet<T> headSet(T toElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return subSet(first(), true, toElement, inclusive, true);
    }

    @Override
    public ArraySet<T> tailSet(T fromElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return subSet(fromElement, inclusive, last(), true, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public ArraySet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public ArraySet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public ArraySet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        return checkedGet(0, false);
    }

    @Override
    public T last() {
        return checkedGet(elements.size() - 1, false);
    }

    @Override
    public int size() {
        return elements.size();
    }
}
