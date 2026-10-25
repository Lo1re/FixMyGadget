package sort;

import com.fixmygadget.sort.QuickSort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickSortTest {

    private static final Comparator<Integer> ASC = Comparator.naturalOrder();

    private static List<Integer> list(Integer... values) {
        return new ArrayList<>(Arrays.asList(values));
    }

    @Test

    void emptyList() {
        List<Integer> data = new ArrayList<>();
        QuickSort.sort(data, ASC);
        assertTrue(data.isEmpty());
    }

    @Test

    void singleElement() {
        List<Integer> data = list(42);
        QuickSort.sort(data, ASC);
        assertEquals(List.of(42), data);
    }

    @Test

    void twoElements() {
        List<Integer> data = list(2, 1);
        QuickSort.sort(data, ASC);
        assertEquals(List.of(1, 2), data);
    }

    @Test

    void duplicates() {
        List<Integer> data = list(3, 1, 3, 2, 1, 3, 2);
        QuickSort.sort(data, ASC);
        assertEquals(List.of(1, 1, 2, 2, 3, 3, 3), data);
    }

    @Test

    void allEqual() {
        List<Integer> data = list(5, 5, 5, 5, 5);
        QuickSort.sort(data, ASC);
        assertEquals(List.of(5, 5, 5, 5, 5), data);
    }

    @Test

    void alreadySorted() {
        List<Integer> data = list(1, 2, 3, 4, 5, 6, 7, 8);
        QuickSort.sort(data, ASC);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), data);
    }

    @Test

    void reverseOrder() {
        List<Integer> data = list(8, 7, 6, 5, 4, 3, 2, 1);
        QuickSort.sort(data, ASC);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), data);
    }

    @Test

    void negativeNumbers() {
        List<Integer> data = list(0, -5, 10, -1, 3, -20);
        QuickSort.sort(data, ASC);
        assertEquals(List.of(-20, -5, -1, 0, 3, 10), data);
    }

    @Test

    void descendingComparator() {
        List<Integer> data = list(4, 9, 1, 7, 3);
        QuickSort.sort(data, Comparator.reverseOrder());
        assertEquals(List.of(9, 7, 4, 3, 1), data);
    }

    @Test

    void stringsByLength() {
        List<String> data = new ArrayList<>(List.of("ccc", "a", "bb", "dddd"));
        QuickSort.sort(data, Comparator.comparingInt(String::length));
        assertEquals(List.of("a", "bb", "ccc", "dddd"), data);
    }

    @Test

    void linkedList() {
        List<Integer> data = new LinkedList<>(List.of(5, 2, 8, 1, 9, 3));
        QuickSort.sort(data, ASC);
        assertEquals(List.of(1, 2, 3, 5, 8, 9), data);
    }

    @Test

    void randomDataMatchesJdkSort() {
        Random random = new Random(12345);
        for (int round = 0; round < 50; round++) {
            int size = random.nextInt(200);
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                data.add(random.nextInt(50) - 25);
            }
            List<Integer> expected = new ArrayList<>(data);
            Collections.sort(expected);

            QuickSort.sort(data, ASC);

            assertEquals(expected, data, "round " + round);
        }
    }

    @Test

    void largeSortedInput() {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            data.add(i);
        }
        List<Integer> expected = new ArrayList<>(data);
        QuickSort.sort(data, ASC);
        assertEquals(expected, data);
    }
}
