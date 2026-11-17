import java.util.*;
public class DeepObjectCopy {
    static class User {
        String name;
        User(String name) { this.name = name; }
        User copy() { return new User(this.name); }
    }
    // PERF-20: Deep copying every object in loop; avoid if possible or use batch copy.
    public List<User> duplicateUsers(List<User> users) {
        List<User> copies = new ArrayList<>();
        for (User user : users) copies.add(user.copy());
        return copies;
    }
}
