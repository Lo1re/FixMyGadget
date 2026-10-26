package com.fixmygadget.sort;

import java.util.Comparator;
import java.util.List;
/**
 * Implements the QuickSort algorithm for sorting application data.
 *
 * <p>Uses a comparator to determine the ordering of elements and a
 * median-of-three pivot selection strategy to improve partitioning.</p>
 *
 * <p>Provides reusable sorting functionality without relying on
 * Java's built-in sorting algorithms.</p>
 */
public final class QuickSort {

    private QuickSort() {
    }

    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        if (list.size() < 2) {
            return;
        }
        quickSort(list, 0, list.size() - 1, comparator);
    }

    private static <T> void quickSort(List<T> list, int low, int high,
                                      Comparator<? super T> comparator) {
        if (low < high) {
            int pi = partition(list, low, high, comparator);
            quickSort(list, low, pi - 1, comparator);
            quickSort(list, pi + 1, high, comparator);
        }
    }

    private static <T> int partition(List<T> list, int low, int high,
                                     Comparator<? super T> comparator) {

        medianOfThree(list, low, high, comparator);
        T pivot = list.get(high);

        int i = low - 1;
        for (int j = low; j <= high - 1; j++) {
            if (comparator.compare(list.get(j), pivot) < 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static <T> void medianOfThree(List<T> list, int low, int high,
                                          Comparator<? super T> comparator) {
        int mid = low + (high - low) / 2;

        if (comparator.compare(list.get(mid), list.get(low)) < 0) {
            swap(list, mid, low);
        }
        if (comparator.compare(list.get(high), list.get(low)) < 0) {
            swap(list, high, low);
        }
        if (comparator.compare(list.get(high), list.get(mid)) < 0) {
            swap(list, high, mid);
        }
        swap(list, mid, high);
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}