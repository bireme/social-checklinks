/*=========================================================================

    Copyright © 2015 BIREME/PAHO/WHO

    This file is part of Social Check Links.

    Social Check Links is free software: you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 2.1 of
    the License, or (at your option) any later version.

    Social Check Links is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Social Check Links. If not, see
    <http://www.gnu.org/licenses/>.

=========================================================================*/

package br.bireme.scl;

import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Heitor Barbieri
 * date: 20150502
 */
public class TabulateField {
    
    public static void tabulate(final String host, 
                                final int port, 
                                final String user, 
                                final String password, 
                                final String database, 
                                final String collection, 
                                final String path) throws UnknownHostException {
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port <= 0");
        }
        if (collection == null) {
            throw new NullPointerException("collection");
        }
        if (path == null) {
            throw new NullPointerException("path");
        }
        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(database);
        if (user != null) {
            final boolean auth = db.authenticate(user, password.toCharArray());
            if (!auth) {
                throw new IllegalArgumentException("invalid user/password");
            }
        }
        final DBCollection coll = db.getCollection(collection);
        final DBCursor cursor = coll.find();
        final TreeMap<String,Integer> map1 = new TreeMap<String,Integer>();
        final TreeMap<Integer,String> map2 = new TreeMap<Integer,String>();
        final int ADD_VALUE = 99999999;
        final int size = cursor.size();
        
        while (cursor.hasNext()) {
            final BasicDBObject doc = (BasicDBObject)cursor.next();            
            final String key = getValue(doc, path);
            
            if (key != null) {
                Integer val = map1.get(key);
                if (val == null) {
                    val = 0;
                }
                val += 1;
                map1.put(key, val);            
            }
        }
        cursor.close();
        for (Map.Entry<String,Integer> entry : map1.entrySet()) {
            map2.put(ADD_VALUE - entry.getValue(), entry.getKey());
        }
        
        System.out.println("Total # of docs: " + size + "\n");
        
        int idx = 0;
        for (Map.Entry<Integer,String> entry : map2.entrySet()) {
            final int val = ADD_VALUE - entry.getKey();
            final String percent = String.format("%.2f", ((float)val/size)*100);
            System.out.println((++idx) + ") " + entry.getValue() + ": " + val + 
                                                         " (" + percent + "%)");
        }
    }
    
    // x/y[0]/z
    private static String getValue(final DBObject obj,
                                   final String path) {
        assert obj != null;
        assert path != null;
        
        final String[] split = path.split(" */ *");
        final Matcher mat = Pattern.compile("([^\\[]+)\\[(\\d+)\\]").matcher("");
        final int len = split.length;
        final String str;
        DBObject dbo = obj;
        
        for (int idx=0; idx < len-1; idx++) {
            final String elem = split[idx];
                        
            mat.reset(elem);
            if (mat.matches()) {
//System.out.println("0:" + mat.group(0) + " 1:" + mat.group(1) + " 2:" + mat.group(2));
                final BasicDBList lst = (BasicDBList)dbo.get(mat.group(1));
                dbo = (DBObject)lst.get(mat.group(2));
                if (dbo == null) {
                    break;
                }
            } else {
                dbo = (DBObject)dbo.get(elem);
            }
        }
        if (dbo == null) {
            str = null;
        } else {
            final String elem = split[len-1];
            
            mat.reset(elem);
            if (mat.matches()) {
                final BasicDBList lst = (BasicDBList)dbo.get(mat.group(1));
                str = lst.get(mat.group(2)).toString();
            } else {
                str = dbo.get(elem).toString();
            }
        }
        
        return str;
    }
    private static void usage() {
        System.err.println("usage: TabulateField <host> <-path=<x/y[0]/z>" +
                "\n\t\t     [-port=<port>]" +
                "\n\t\t     [-user=<user> -password=<pswd>]" +
                "\n\t\t     [-database=<dbase>] [-collection=<col>]");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws UnknownHostException, 
                                                        IOException,
                                                        ParseException {
        if (args.length < 1) {
            usage();
        }

        String host = args[0];
        int port = DEFAULT_PORT;
        String user = null;
        String password = null;
        String database = SOCIAL_CHECK_DB;
        String collection = HISTORY_COL;
        String path = null;

        for (int idx = 1; idx < args.length; idx++) {
            if (args[idx].startsWith("-port=")) {                
                port = Integer.parseInt(args[idx].substring(6));
            } else if (args[idx].startsWith("-user=")) {
                user = args[idx].substring(6);
            } else if (args[idx].startsWith("-password=")) {
                password = args[idx].substring(10);
            } else if (args[idx].startsWith("-database=")) {
                database = args[idx].substring(10);
            } else if (args[idx].startsWith("-collection=")) {
                collection = args[idx].substring(12);
            } else if (args[idx].startsWith("-path=")) {
                path = args[idx].substring(6);
            } else {
                usage();
            }
        }
        
        tabulate(host, port, user, password, database, collection, path);
    }
}
