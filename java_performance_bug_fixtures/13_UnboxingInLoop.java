import java.util.*;
public class UnboxingInLoop {
    // PERF-13: Repeated unboxing from wrapper objects creates unnecessary allocations.
    public long sumValues(List<Long> values) {
        long sum = 0;
        for (Long val : values) sum += val;
        return sum;
    }
}
