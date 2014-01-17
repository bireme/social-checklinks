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

import static br.bireme.scl.MongoOperations.EXPORTED_FIELD;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Heitor Barbieri
 * date 20140117
 */
public class ResetExportFlag {
    private static void reset(final String host,
                              final int port,
                              final String database,
                              final String collection,
                              final String sdate) throws UnknownHostException, 
                                                         ParseException {
        assert host != null;
        assert port > 0;
        assert database != null;
        assert collection != null;
        
        final MongoClient client = new MongoClient(host, port); 
        final DB db = client.getDB(database);
        final DBCollection coll = db.getCollection(collection);
        
        final DBCursor cursor;
        
        if (sdate == null) {
            cursor = coll.find();
        } else {
            final SimpleDateFormat simple = new SimpleDateFormat("yyyyMMdd");
            final Date date = simple.parse(sdate);
            final BasicDBObject query = new BasicDBObject("date", date);            
            cursor = coll.find(query);
        }
        
        while (cursor.hasNext()) {
            final BasicDBObject doc = (BasicDBObject)cursor.next();
            final boolean exported = (Boolean)doc.get(EXPORTED_FIELD);
            
            if (exported) {
                doc.put(EXPORTED_FIELD, false);
                coll.save(doc);
            }
        }
        cursor.close();
    }
    
    private static void usage() {
        System.err.println("usage: ResetExportFlag <mongo_host> <database>"  +
        " <collection> [-init_date=<yyyymmdd>] [-mongo_port=<port>]");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws UnknownHostException, 
                                                        ParseException {
        if (args.length < 3) {
            usage();
        }
        String date = null;
        int port = BrokenLinks.DEFAULT_PORT;
        
        for (int idx = 1; idx < args.length; idx++) {
            if (args[idx].startsWith("-init_date=")) {
                date = args[idx].substring(11);
            } else if (args[idx].startsWith("-mongo_port=")) {
                port = Integer.parseInt(args[idx].substring(12));
            } else {
                usage();
            }
        }
        
        reset(args[0], port, args[1], args[2], date);
    }
}
