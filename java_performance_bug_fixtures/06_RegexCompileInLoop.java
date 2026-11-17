import java.util.*;
import java.util.regex.Pattern;
public class RegexCompileInLoop {
    // PERF-06: Pattern is recompiled on every iteration; compile it once.
    public long countValid(List<String> values) {
        long count = 0;
        for (String value : values)
            if (Pattern.compile("[A-Z]{3}-[0-9]{5}").matcher(value).matches()) count++;
        return count;
    }
}
