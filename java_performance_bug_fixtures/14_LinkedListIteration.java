import java.util.*;
public class LinkedListIteration {
    // PERF-14: LinkedList.get(i) is O(n); use iterator or convert to ArrayList.
    public int sumWithGet(LinkedList<Integer> list) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++) sum += list.get(i);
        return sum;
    }
}
