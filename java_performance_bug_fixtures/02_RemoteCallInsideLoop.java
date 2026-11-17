import java.util.*;
public class RemoteCallInsideLoop {
    interface PricingClient { double getPrice(String productId); }
    // PERF-02: One network round trip per item; prefer a batch endpoint.
    public List<Double> loadPrices(List<String> ids, PricingClient client) {
        List<Double> out = new ArrayList<>();
        for (String id : ids) out.add(client.getPrice(id));
        return out;
    }
}
