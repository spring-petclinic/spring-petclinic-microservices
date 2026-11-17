import java.text.*;
import java.util.*;
public class DateFormattingInLoop {
    // PERF-16: SimpleDateFormat is not thread-safe and expensive; cache or use faster format.
    public List<String> formatDates(List<Date> dates) {
        List<String> result = new ArrayList<>();
        for (Date date : dates) {
            result.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        }
        return result;
    }
}
