import java.util.*;
public class NPlusOneDatabaseCalls {
    interface OrderRepository { List<String> findOrdersByCustomerId(long id); }
    // PERF-01: N+1 database calls inside a loop.
    public List<String> loadOrders(List<Long> ids, OrderRepository repo) {
        List<String> out = new ArrayList<>();
        for (Long id : ids) out.addAll(repo.findOrdersByCustomerId(id));
        return out;
    }
}
