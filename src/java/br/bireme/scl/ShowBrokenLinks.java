/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.scl;

import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Heitor Barbieri
 * date: 20150416
 */
public class ShowBrokenLinks {
    final DBCollection coll;

    public ShowBrokenLinks(final String host,
                           final String database,
                           final String collection) throws UnknownHostException {
        this(host, DEFAULT_PORT, null, null, database, collection);

    }

    public ShowBrokenLinks(final String host,
                           final int port,
                           final String user,
                           final String password,
                           final String database,
                           final String collection) throws UnknownHostException {
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port=" + port);
        }
        if (database == null) {
            throw new NullPointerException("database");
        }
        if (collection == null) {
            throw new NullPointerException("collection");
        }

        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(database);
        if (user != null) {
            final boolean auth = db.authenticate(user, password.toCharArray());
            if (!auth) {
                throw new IllegalArgumentException("invalid user/password");
            }
        }
        coll = db.getCollection(collection);
    }

    public Map<String,String> statistics() throws IOException {
        final Map<String,String> ret = new TreeMap<String,String>();
        final Map<String,Integer> map = new HashMap<String,Integer>();
        final DBCursor cursor = coll.find();

        for (DBObject obj : cursor) {
            final BasicDBObject dobj = (BasicDBObject)obj;
            final BasicDBList ccList = (BasicDBList)dobj.get(CENTER_FIELD);
            final String cc = (String)ccList.get(0);

            if (cc == null) {
                throw new IOException("null center field. Id="
                                                    + dobj.getString(ID_FIELD));
            }
            Integer val = map.get(cc);
            if (val == null) {
                val = 0;
            }
            map.put(cc, val+1);
        }
        cursor.close();

        for (Map.Entry<String,Integer> entry : map.entrySet()) {
            final String key = entry.getKey();
            final int value = entry.getValue();
            final int dif = 999999 - value;
            ret.put(dif + "_" + value + "_" + key , key);
        }

        return ret;
    }

    private static void usage() {
        System.err.println("usage: ShowBrokenLinks <host> [-port=<port>]" +
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
        String collection = BROKEN_LINKS_COL;

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
            } else {
                usage();
            }
        }

        final ShowBrokenLinks blinks = new ShowBrokenLinks(host, port, user,
                                                password, database, collection);
        final Map<String,String> map = blinks.statistics();
        int idx = 0;
        int total = 0;

        for (Map.Entry<String,String> entry : map.entrySet()) {
            final String key = entry.getKey();
            final int idx1 = key.indexOf('_');
            final int idx2 = key.lastIndexOf('_');
            final String num = key.substring(idx1 + 1, idx2);

            System.out.println((++idx) + ") CC=" + entry.getValue() + " #=" +
                                                                           num);
            total += Integer.parseInt(num);
        }
        System.out.println("\nTotal=" + total);
    }
}
