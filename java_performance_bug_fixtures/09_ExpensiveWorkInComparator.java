import java.util.*;
public class ExpensiveWorkInComparator {
    interface ProfileService { String loadDisplayName(String userId); }
    // PERF-09: Comparator repeatedly performs expensive lookups while sorting.
    public List<String> sortUsers(List<String> ids, ProfileService profiles) {
        List<String> out = new ArrayList<>(ids);
        out.sort(Comparator.comparing(profiles::loadDisplayName));
        return out;
    }
}
