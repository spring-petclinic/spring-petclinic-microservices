import java.io.*;
import java.util.*;
public class FileIOInLoop {
    // PERF-15: Opening/closing file per iteration is slow; batch or use buffered access.
    public List<String> readLines(List<String> filePaths) throws IOException {
        List<String> result = new ArrayList<>();
        for (String path : filePaths) {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            result.add(reader.readLine());
            reader.close();
        }
        return result;
    }
}
