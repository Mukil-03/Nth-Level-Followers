import java.util.*;

public class Main {

    public static void main(String[] args) {
        List<User> users = List.of(
            new User(1, "Alice", List.of(2, 3)),
            new User(2, "Bob", List.of(4)),
            new User(3, "Charlie", List.of(4, 5)),
            new User(4, "David", List.of(6)),
            new User(5, "Eva", List.of(6)),
            new User(6, "Frank", List.of())
        );

        int findId = 1;
        int level = 2;

        List<Integer> outcome = findNthLevelFollowers(users, findId, level);

        // Print output in required JSON format
        System.out.println("{");
        System.out.println("\" regNo \": \" REG12347 \" ,");
        System.out.print("\" outcome \": [");
        for (int i = 0; i < outcome.size(); i++) {
            System.out.print(outcome.get(i));
            if (i < outcome.size() - 1) {
                System.out.print(" ,");
            }
        }
        System.out.println("]");
        System.out.println("}");
    }

    public static List<Integer> findNthLevelFollowers(List<User> users, int findId, int n) {
        Map<Integer, List<Integer>> graph = new HashMap<>();

        for (User user : users) {
            graph.put(user.id, user.follows);
        }

        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        queue.add(findId);
        visited.add(findId);

        int currentLevel = 0;

        while (!queue.isEmpty() && currentLevel < n) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int userId = queue.poll();
                List<Integer> followees = graph.getOrDefault(userId, List.of());
                for (int followee : followees) {
                    if (!visited.contains(followee)) {
                        queue.add(followee);
                        visited.add(followee);
                    }
                }
            }
            currentLevel++;
        }

        return new ArrayList<>(queue); 
    }

    static class User {
        int id;
        String name;
        List<Integer> follows;

        public User(int id, String name, List<Integer> follows) {
            this.id = id;
            this.name = name;
            this.follows = follows;
        }
    }
}
