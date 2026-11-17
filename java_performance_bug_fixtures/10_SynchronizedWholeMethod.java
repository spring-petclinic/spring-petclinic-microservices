import java.util.*;
public class SynchronizedWholeMethod {
    private final Map<String, String> cache = new HashMap<>();
    interface Loader { String load(String key); }
    // PERF-10: Broad lock serializes callers, including expensive cache-miss work.
    public synchronized String getOrLoad(String key, Loader loader) {
        String value = cache.get(key);
        if (value == null) { value = loader.load(key); cache.put(key, value); }
        return value;
    }
}
