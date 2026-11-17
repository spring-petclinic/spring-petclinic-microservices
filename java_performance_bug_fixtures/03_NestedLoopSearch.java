import java.util.*;
public class NestedLoopSearch {
    // PERF-03: Nested scan can become O(n*m); build a HashSet/Map for lookups.
    public List<String> findMatches(List<String> requested, List<String> available) {
        List<String> out = new ArrayList<>();
        for (String wanted : requested)
            for (String candidate : available)
                if (wanted.equals(candidate)) { out.add(wanted); break; }
        return out;
    }
}
