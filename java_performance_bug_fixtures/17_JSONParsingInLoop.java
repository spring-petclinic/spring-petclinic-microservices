import java.util.*;
public class JSONParsingInLoop {
    interface JSONParser { Map<String, Object> parse(String json); }
    // PERF-17: Creating parser instance per iteration is wasteful; reuse singleton.
    public List<Map<String, Object>> parseJsonStrings(List<String> jsonStrings, JSONParser parser) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String json : jsonStrings) {
            result.add(parser.parse(json));
        }
        return result;
    }
}
