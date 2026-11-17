import java.util.*;
public class RepeatedStreamScan {
    // PERF-07: Re-scans the collection for every item; pre-index/group once.
    public long countMatchingPairs(List<String> values) {
        long total = 0;
        for (String value : values)
            total += values.stream().filter(value::equals).count();
        return total;
    }
}
