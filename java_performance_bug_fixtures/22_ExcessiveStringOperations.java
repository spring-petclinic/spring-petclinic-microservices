import java.util.*;
public class ExcessiveStringOperations {
    // PERF-22: Repeated string split/substring in loop; cache results.
    public Map<String, String> parseKeyValuePairs(List<String> lines) {
        Map<String, String> map = new HashMap<>();
        for (String line : lines) {
            String key = line.substring(0, line.indexOf("="));
            String value = line.substring(line.indexOf("=") + 1);
            map.put(key.trim(), value.trim());
        }
        return map;
    }
}
