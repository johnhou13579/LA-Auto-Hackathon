package myserver;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

import java.util.*;

@SpringBootApplication
@CrossOrigin(origins = "http://localhost:8080")
public class MyServer {
	public static HashMap<String, String> users = new HashMap<>();
	public static ArrayList<User> tokensArrayList = new ArrayList<>();

	public static void main(String[] args) {
		SpringApplication.run(MyServer.class, args);
	}
}


class User{
	String username;
	String token;
	User(String username, String token)
	{
		this.username = username;
		this.token=token;
	}
}