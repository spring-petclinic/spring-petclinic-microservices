import java.util.*;
public class ExceptionInLoop {
    // PERF-11: Creating exceptions in hot loop is expensive; exceptions capture stack traces.
    public List<Integer> parseNumbers(List<String> values) {
        List<Integer> result = new ArrayList<>();
        for (String val : values) {
            try {
                result.add(Integer.parseInt(val));
            } catch (NumberFormatException e) {
                result.add(0);
            }
        }
        return result;
    }
}
