import java.util.*;
public class CacheStampede {
    interface DataLoader { String load(String key); }
    private final Map<String, String> cache = new HashMap<>();
    // PERF-24: No synchronization on cache miss; multiple threads load simultaneously.
    public String get(String key, DataLoader loader) {
        if (!cache.containsKey(key)) {
            cache.put(key, loader.load(key));
        }
        return cache.get(key);
    }
}
