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
        elements = new ArrayList<>(collection);
        comparator = null;
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        elements = new ArrayList<>(collection);
        this.comparator = comparator;
    }

    /*
    Reverse constructor
     */
    private ArraySet(ArraySet<T> origin) {
        elements = new FastReverseList<>(origin.elements);
        comparator = origin.comparator.reversed();
    }

    private T checkedGet(int index, boolean nothrow) {
        if (index > -1 && index < elements.size()) {
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
        return Collections.binarySearch(elements, t, comparator);
    }

    private int lowerIndex(T t) {
        int index = insertionIndex(t);
        return index >= 0 ? index - 1 : -index - 2;
    }

    private int floorIndex(T t) {
        int index = lowerIndex(t);
        return t == checkedGet(index + 1) ? index + 1 : index;
    }

    private int higherIndex(T t) {
        int index = insertionIndex(t);
        return index >= 0 ? index + 1 : -index - 1;
    }

    private int ceilingIndex(T t) {
        int index = higherIndex(t);
        return t == checkedGet(index - 1) ? index - 1 : index;
    }

    @Override
    public T lower(T t) {
        return checkedGet(lowerIndex(t));
    }

    @Override
    public T floor(T t) {
        return checkedGet(floorIndex(t));
    }

    @Override
    public T higher(T t) {
        return checkedGet(higherIndex(t));
    }

    @Override
    public T ceiling(T t) {
        return checkedGet(ceilingIndex(t));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(this);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int fromIndex = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        int toIndex = toInclusive ? floorIndex(toElement) : lowerIndex(toElement);
        return toIndex > fromIndex || toIndex == -1 || fromIndex == elements.size() ?
                Collections.emptyNavigableSet() :
                new ArraySet<>(elements.subList(fromIndex, toIndex), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, true);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
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
