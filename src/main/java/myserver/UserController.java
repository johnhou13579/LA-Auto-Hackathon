package myserver;

import org.springframework.web.bind.annotation.*; 
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.servlet.http.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.*;

@RestController
public class UserController {

	@RequestMapping(value = "/chat", method = RequestMethod.POST) 
	public ResponseEntity<String> chat(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String chat = payloadObj.getString("chat"); 
		String username = payloadObj.getString("username");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");

    	String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

    	String hostName = "127.0.0.1";
    	int portNumber = Integer.parseInt("4040");
        try (
            Socket echoSocket = new Socket(hostName, portNumber); //Connect socket to given host name and port number
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true); //Get output stream of socket pass it into a PrintWriter so we can write to the server
        ) {
        	JSONObject obj = new JSONObject();

            String userInput = chat;
            ServerListener serverListener = new ServerListener(echoSocket); //Initialize a thread to listen for input from server
            serverListener.start();
            out.println(userInput);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 


		return new ResponseEntity("{\"message\":\"User Chat Successfully\"}", responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST) 
	public ResponseEntity<String> register(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 
		String password = payloadObj.getString("password");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;

		MessageDigest digest = null;
		String hashedKey = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256"); 
			hashedKey = bytesToHex(digest.digest(password.getBytes("UTF-8"))); 
		}catch(Exception e) {
		}
		 try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "INSERT INTO charity.users(users.username,users.hashedPassword) VALUES(?,?)";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        stmt.setString(2, hashedKey);
	        stmt.executeUpdate();
	    } catch (Exception e) {
	    	return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(Exception e) {
	    		return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.OK);
	    	}
	    }

	    if (!MyServer.users.containsKey(username)) {
			//MyServer.users.put(username, hashedKey.substring(0,10));
			
		}else {
			return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		JSONObject responseObj = new JSONObject();
		responseObj.put("message", MyServer.users.get(username));
		return new ResponseEntity(responseObj.toString(), responseHeaders, HttpStatus.OK);
	}


	@RequestMapping(value = "/login", method = RequestMethod.POST) 
	public ResponseEntity<String> login(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 
		String password = payloadObj.getString("password");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;

		MessageDigest digest = null;
		String hashedKey = null;
		String hashed = null;

		try {
			digest = MessageDigest.getInstance("SHA-256"); 
			hashedKey = bytesToHex(digest.digest(password.getBytes("UTF-8"))); 
		}catch(Exception e) {
		}
		try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "SELECT users.username, users.hashedPassword FROM charity.users WHERE username = ?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            String user = rs.getString("username");
	            hashed = rs.getString("hashedPassword");
	        }
	    } catch (SQLException e ) {
	    	JSONObject responseObj = new JSONObject();
			responseObj.put("message", "cannot register. try again.");
	    	return new ResponseEntity(e, responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {
	    	}
	    }
	    if (hashed.equals(hashedKey)) {
	    		MyServer.users.put(username, hashedKey.substring(0,10));
	    		JSONObject responseObj = new JSONObject();

				responseObj.put("Message", MyServer.users.get(username));
				return new ResponseEntity(responseObj.toString(), responseHeaders, HttpStatus.OK);

			}else {
				return new ResponseEntity("{\"Message\":\"username/password combination is incorrect\"}", responseHeaders, HttpStatus.BAD_REQUEST);
			}
	}

	@RequestMapping(value = "/updateBalance", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> updateBalance(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "SELECT users.balance FROM charity.users WHERE username = ?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
	            Double balance = rs.getDouble("users.balance");

	            JSONObject obj = new JSONObject();
	            obj.put("Balance", balance);
	            usersArray.put(obj);
			   }
	        
	    } catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/searchSort", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> searchSort(@RequestBody String payload, HttpServletRequest request) {
		
		JSONObject payloadObj = new JSONObject(payload);
		String sortBy = payloadObj.getString("search"); //Grabbing name and age parameters from URL
		String username = payloadObj.getString("username");
		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			PreparedStatement stmt = null;
			ResultSet rs = null;
			if(sortBy.equals("Name"))
			{
				String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.charity";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("Most Donated"))
	    	{
	    		String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.donatesTotal DESC";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("Most Subscribers"))
	    	{
	    		String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.subscribers DESC";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("My List"))
	    	{
	    		String query = "SELECT distinct subscribers.charity, charities.subscribers, charities.donatesTotal FROM charity.charities, charity.subscribers WHERE subscribers.username = ? AND subscribers.charity = charities.charity";
		        stmt = conn.prepareStatement(query);
		        stmt.setString(1, username);
		        rs = stmt.executeQuery();
	    	}
	        
	        while (rs.next()) {
	            String charityName = rs.getString("charity");
	            int subs = rs.getInt("charities.subscribers");
	            Double totalDonates = rs.getDouble("charities.donatesTotal");

	            JSONObject obj = new JSONObject();
	            obj.put("Total Donates", totalDonates);
	            obj.put("Subscribers", subs);
	            obj.put("Charity Name", charityName);
	            usersArray.put(obj);
	        
	    } 
	}catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}
	public static String bytesToHex(byte[] in) {
		StringBuilder builder = new StringBuilder();
		for(byte b: in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}
}


class ServerListener extends Thread {
    Socket socket;
    ServerListener(Socket socket) {
        this.socket = socket;
    }
    public void run() {
        try {	
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(socket.getInputStream())); //Input stream from server
            String serverInput;
            while((serverInput = in.readLine()) != null) {
                System.out.println(serverInput); //Print input from server into console

            }
        }catch(IOException ie) {

        }
    }
}


