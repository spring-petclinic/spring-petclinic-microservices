import java.lang.reflect.*;
import java.util.*;
public class ReflectionInLoop {
    // PERF-12: Reflection lookup is slow; cache Method objects outside the loop.
    public void invokeMethod(List<Object> objects, String methodName) throws Exception {
        for (Object obj : objects) {
            Method method = obj.getClass().getMethod(methodName);
            method.invoke(obj);
        }
    }
}
