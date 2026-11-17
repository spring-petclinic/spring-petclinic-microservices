import java.util.*;
public class ThreadCreationInLoop {
    // PERF-19: Creating new Thread per item is expensive; use thread pool or ExecutorService.
    public void processItemsWithThreads(List<String> items) {
        for (String item : items) {
            new Thread(() -> System.out.println(item)).start();
        }
    }
}
