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


package br.bireme.scl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.net.UnknownHostException;
import java.util.Set;

/**
 *
 * @author Heitor Barbieri
 * date 20140114
 */
public class CopyMongoDb {
    private static void copyDB(final String from_host,
                               final String to_host,
                               final String from_db,
                               final String to_db,
                               final String from_port,
                               final String to_port,
                               final boolean appendCollections) 
                                                   throws UnknownHostException {
        assert from_host != null;
        assert to_host != null;
        assert from_db != null;
        assert to_db != null;
        assert from_port != null;
        assert to_port != null;
        
        final int MAX_LOOP_SIZE = 15000; // MongoException$CursorNotFound
        final MongoClient fromClient = new MongoClient(from_host, 
                                                   Integer.parseInt(from_port));
        final MongoClient toClient = new MongoClient(to_host, 
                                                     Integer.parseInt(to_port));
        final DB fromDb = fromClient.getDB(from_db);
        final DB toDb = toClient.getDB(to_db);
        
        if (!appendCollections) {
            toDb.dropDatabase();
        }

        final Set<String> colls = fromDb.getCollectionNames();

        for (String cname : colls) {
            if (cname.equals("system.indexes")) {
                continue;
            }
            
            final DBCollection fromColl = fromDb.getCollection(cname);
            final DBCollection toColl = toDb.getCollection(cname);
            
            DBCursor cursor = fromColl.find();
            int curr = 0;
            
            System.err.println("Copying collection: " + cname);
            
            while (cursor.hasNext()) {
                if (curr % MAX_LOOP_SIZE == 0) {
                    if (curr > 0) {
                        cursor.close();
                        cursor = fromColl.find().skip(curr);
                    }
                }
                final DBObject doc = cursor.next();
                final WriteResult ret = toColl.save(doc, WriteConcern.SAFE);

                if (!ret.getCachedLastError().ok()) {
                    System.err.println("write error doc id=" + doc.get("_id"));
                }
                if (++curr % 1000 == 0) {
                    System.err.println("+++" + curr);
                }
            }
            cursor.close();
            
            System.err.println();
        }
    }
    
    private static void usage() {
        System.err.println("usage: CopyMongoDb <from_host> <to_host> <from_db>"
        + " [-to_db=<name>] [-from_port=<port>] [-to_port=<port>]"
        + " [--appendCollections]");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws UnknownHostException {
        if (args.length < 3) {
            usage();
        }
        String toDb = args[2];
        String fromPort = Integer.toString(BrokenLinks.DEFAULT_PORT);
        String toPort =  fromPort;
        boolean appendCollections = false;
        
        for (int idx = 3; idx < args.length; idx++) {
            if (args[idx].startsWith("-to_db=")) {
                toDb = args[idx].substring(7);
            } else if (args[idx].startsWith("-from_port=")) {
                fromPort = args[idx].substring(11);
            } else if (args[idx].startsWith("-to_port=")) {
                toPort = args[idx].substring(9);
            } else if (args[idx].equals("--appendCollections")) {
                appendCollections = true;
            } else {
                usage();
            }
        }
        
        copyDB(args[0], args[1], args[2], toDb, fromPort, toPort, 
                                                             appendCollections);
    }
}
