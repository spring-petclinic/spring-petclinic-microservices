import java.util.*;
public class ListContainsInLoop {
    // PERF-04: List.contains is linear; repeated checks can be O(n*m).
    public List<String> filterAllowed(List<String> all, List<String> allowed) {
        List<String> out = new ArrayList<>();
        for (String id : all) if (allowed.contains(id)) out.add(id);
        return out;
    }
}
