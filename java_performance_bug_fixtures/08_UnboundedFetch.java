import java.util.*;
public class UnboundedFetch {
    interface EventRepository { List<String> findAllEvents(); }
    // PERF-08: Fetches all rows; use pagination or a bounded query.
    public int countEvents(EventRepository repo) {
        return repo.findAllEvents().size();
    }
}
