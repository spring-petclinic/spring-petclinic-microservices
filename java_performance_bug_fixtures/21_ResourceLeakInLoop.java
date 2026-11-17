import java.io.*;
import java.util.*;
public class ResourceLeakInLoop {
    // PERF-21: File streams not closed on exception; use try-with-resources.
    public List<String> readMultipleFiles(List<String> filePaths) throws IOException {
        List<String> contents = new ArrayList<>();
        for (String path : filePaths) {
            FileInputStream fis = new FileInputStream(path);
            contents.add(new String(fis.readAllBytes()));
            fis.close();
        }
        return contents;
    }
}
