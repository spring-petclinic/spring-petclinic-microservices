import java.util.concurrent.*;
public class BusyWaiting {
    private volatile boolean flag = false;
    // PERF-23: Busy waiting wastes CPU; use wait/notify or CountDownLatch.
    public void waitForFlag() {
        while (!flag) {
            // Continuously spinning, consuming CPU
            Thread.yield();
        }
    }
    public void setFlag() { flag = true; }
}
