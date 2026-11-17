import java.util.*;
public class StringConcatInLoop {
    // PERF-05: Immutable String concatenation creates temporary objects per iteration.
    public String buildReport(List<String> rows) {
        String report = "";
        for (String row : rows) report = report + row + "\n";
        return report;
    }
}
