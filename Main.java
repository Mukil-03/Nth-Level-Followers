package com.example.followersapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootApplication
public class FollowersAppApplication {
    public static void main(String[] args) {
    SpringApplication.run(FollowersAppApplication.class, args);
}
}

@Configuration
class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@Component
class AppStartupRunner implements ApplicationRunner {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(ApplicationArguments args) {
    callWebhookOnStartup();
}

private void callWebhookOnStartup() {
    String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

    Map<String, String> requestBody = Map.of(
        "name", "John Doe",
        "regNo", "REG12347",
        "email", "john@example.com"
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

    ResponseEntity<GenerateWebhookResponse> response = restTemplate
        .postForEntity(url, request, GenerateWebhookResponse.class);

    if (response.getStatusCode().is2xxSuccessful()) {
        GenerateWebhookResponse resp = response.getBody();
        sendProcessedOutput(resp);
    }
}

private List<Integer> getNthLevelFollowers(List<User> users, int findId, int n) {
    Map<Integer, List<Integer>> graph = new HashMap<>();
    for (User user : users) {
        graph.put(user.getId(), user.getFollows());
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
            for (int followee : graph.getOrDefault(userId, List.of())) {
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

private void sendProcessedOutput(GenerateWebhookResponse resp) {
    List<Integer> outcome = getNthLevelFollowers(
        resp.getData().getUsers(),
        resp.getData().getFindId(),
        resp.getData().getN()
    );

    Map<String, Object> output = new HashMap<>();
    output.put("regNo", "REG12347");
    output.put("outcome", outcome);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", resp.getAccessToken());

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(output, headers);

    int attempts = 0;
    while (attempts < 4) {
        try {
            restTemplate.postForEntity(resp.getWebhook(), request, String.class);
            System.out.println("✅ Webhook call succeeded");
            break;
        } catch (Exception e) {
            attempts++;
            System.out.println("❌ Webhook failed, retrying... (" + attempts + ")");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
        }
    }
}
}

class GenerateWebhookResponse {
    private String webhook;
    private String accessToken;
    private DataWrapper data;

    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }

public String getAccessToken() { return accessToken; }
public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

public DataWrapper getData() { return data; }
public void setData(DataWrapper data) { this.data = data; }
}

class DataWrapper {
    private int n;
    private int findId;
    private List<User> users;

    public int getN() { return n; }
    public void setN(int n) { this.n = n; }

public int getFindId() { return findId; }
public void setFindId(int findId) { this.findId = findId; }

public List<User> getUsers() { return users; }
public void setUsers(List<User> users) { this.users = users; }
}

class User {
    private int id;
    private String name;
    private List<Integer> follows;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

public String getName() { return name; }
public void setName(String name) { this.name = name; }

public List<Integer> getFollows() { return follows; }
public void setFollows(List<Integer> follows) { this.follows = follows; }
}


