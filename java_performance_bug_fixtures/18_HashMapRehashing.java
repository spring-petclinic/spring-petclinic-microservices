import java.util.*;
public class HashMapRehashing {
    // PERF-18: HashMap rehash on growth; pre-size with capacity to avoid multiple rehashes.
    public Map<String, Integer> countWords(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : words) {
            counts.put(word, counts.getOrDefault(word, 0) + 1);
        }
        return counts;
    }
}
