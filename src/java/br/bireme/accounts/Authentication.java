/*=========================================================================

    Copyright © 2013 BIREME/PAHO/WHO

    This file is part of SocialCheckLinks.

    SocialCheckLinks is free software: you can redistribute it and/or 
    modify it under the terms of the GNU Lesser General Public License as 
    published by the Free Software Foundation, either version 2.1 of 
    the License, or (at your option) any later version.

    SocialCheckLinks is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public 
    License along with SocialCheckLinks. If not, see 
    <http://www.gnu.org/licenses/>.

=========================================================================*/

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
    final static String DEFAULT_HOST = "accounts.teste.bireme.org";
    final static int DEFAULT_PORT = 80;
    final static String DEFAULT_PATH = "/api/auth/login/?format=json";
    final static String SERVICE_NAME = "DIREVE"; //"SocialChecklinks";
    
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
        
        return (Boolean)response.get("success");
    }
    
    public String getCenterId(final JSONObject response) throws IOException, 
                                                                ParseException {
        if (response == null) {
            throw new NullPointerException("response");
        }
        String id = null;
                
        if (isAuthenticated(response)) {
            final JSONObject jobj = (JSONObject)response.get("data");
            
            if (jobj != null) {
                id = (String)jobj.get("cc");
            }
        } 
        
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
        parameters.put("service", SERVICE_NAME);  
        parameters.put("format", "json");
        
        //final URL url = new URL("http", host, port, DEFAULT_PATH);
        final URL url = new URL("http://" + host + DEFAULT_PATH);
        final HttpURLConnection connection = 
                                        (HttpURLConnection)url.openConnection(); 
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();
        
        final StringBuilder builder = new StringBuilder();
        final BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        
 //final String message = parameters.toJSONString();
 //System.out.println(message);
 
        writer.write(parameters.toJSONString());
        writer.newLine(); 
        writer.close();

        final boolean respCodeOk = (connection.getResponseCode() == 200);
        final BufferedReader reader;
        
        if (respCodeOk) {
            reader = new BufferedReader(new InputStreamReader(
                                                  connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(
                                                  connection.getErrorStream()));
        }

        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            builder.append(line);
            builder.append("\n");
        }
        
        reader.close();
        
        if (!respCodeOk) {
            throw new IOException(builder.toString());
        }
        
        return (JSONObject) new JSONParser().parse(builder.toString());
    }    
    
    public static void main(String[] args) throws IOException, ParseException {
        final Authentication aut = new Authentication();
        
        final JSONObject jobj = aut.getUser("barbieri@paho.org", "heitor");
        final boolean isAuthenticated = aut.isAuthenticated(jobj);
        
        System.out.println("isAthenticated=" + isAuthenticated);
        if (isAuthenticated) {
            System.out.println("centerId=" + aut.getCenterId(jobj));
        }        
    }
}
