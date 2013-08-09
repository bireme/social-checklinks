package br.bireme.accounts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Heitor Barbieri
 * date: 20130731
 */
public class Authentication {
    final static String DEFAULT_HOST = "";
    final static int DEFAULT_PORT = 8000;
    final static String DEFAULT_PATH = "/api/auth/login/";
    final static String SERVICE_NAME = "SocialChecklinks";
    
    final String host;
    final int port;
    
    public Authentication() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }
    
    public Authentication(final String host,
                          final int port) {
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port[" + port + "] <= 0");
        }
        this.host = host;
        this.port = port;
    }
    
    public boolean isAuthenticated(final JSONObject response) throws IOException, 
                                                                ParseException {
        if (response == null) {
            throw new NullPointerException("response");
        }
        final boolean ret = true;
        
        // TODO
        
        return ret;
    }
    
    public String getCenterId(final JSONObject response) throws IOException, 
                                                                ParseException {
        if (response == null) {
            throw new NullPointerException("response");
        }
        final String id = null;
        
        //TODO
        
        return id;
    }
    
    public JSONObject getUser(final String user,
                              final String password) throws IOException, 
                                                                ParseException {
        if (user == null) {
            throw new NullPointerException("user");
        }
        if (password == null) {
            throw new NullPointerException("password");
        }
        
        final JSONObject parameters = new JSONObject();
        parameters.put("username", user);
        parameters.put("password", password);
        parameters.put("format", "json");
        parameters.put("service", SERVICE_NAME);  
        
        final URL url = new URL("http", host, port, DEFAULT_PATH);
        final HttpURLConnection connection = 
                                        (HttpURLConnection)url.openConnection();                
        connection.setRequestMethod("POST");
        connection.setRequestProperty("content-type", 
                                      "application/json; charset=utf-8");                          
        connection.connect();
        
        final StringBuilder builder = new StringBuilder();
        final BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        writer.write(parameters.toJSONString());
        writer.newLine(); 
            
        final BufferedReader reader = new BufferedReader(
                   new InputStreamReader(connection.getInputStream(), "UTF-8"));
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            builder.append(line);
            builder.append("\n");
        }
        
        reader.close();
        writer.close();
        
        return (JSONObject) new JSONParser().parse(builder.toString());
    }    
}
