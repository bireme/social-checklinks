/*=========================================================================

    Copyright © 2014 BIREME/PAHO/WHO

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

import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.ELEM_LST_FIELD;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.LAST_UPDATE_FIELD;
import static br.bireme.scl.BrokenLinks.MSG_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Moves documents from HISTORY_COL to BROKEN_LINKS_COL collection since a
 * given date.
 * @author Heitor Barbieri
 * 20140721
 */
public class UndoUpdate {
        private static void undo(final String host,
                                 final int port,
                                 final String database,
                                 final String fromDate) 
                                                    throws UnknownHostException, 
                                                           ParseException {
        assert host != null;
        assert port > 0;
        assert database != null;
        assert fromDate != null;
        
        final MongoClient client = new MongoClient(host, port); 
        final DB db = client.getDB(database);
        final DBCollection from_coll = db.getCollection(HISTORY_COL);
        final DBCollection to_coll = db.getCollection(BROKEN_LINKS_COL);
        final String prefix = ELEM_LST_FIELD + ".0.";
        final SimpleDateFormat simple = new SimpleDateFormat("yyyyMMdd");
        final Date fDate = simple.parse(fromDate);            
        final BasicDBObject query = new BasicDBObject(prefix +LAST_UPDATE_FIELD, 
                                              new BasicDBObject("$gte", fDate));
        final DBCursor cursor = from_coll.find(query);
        int total = 0;
        int reverted = 0;
        
        while (cursor.hasNext()) {
            final BasicDBObject doc = (BasicDBObject)cursor.next();
            final String id = doc.getString(ID_FIELD);
            final BasicDBList list = (BasicDBList)doc.get(ELEM_LST_FIELD);
            final BasicDBObject elem = (BasicDBObject)list.get(0);
            
            doc.put(LAST_UPDATE_FIELD, elem.getDate(LAST_UPDATE_FIELD));
            doc.put(BROKEN_URL_FIELD, elem.getString(BROKEN_URL_FIELD));
            doc.put(MSG_FIELD, elem.getString(MSG_FIELD));
            doc.put(CENTER_FIELD, elem.get(CENTER_FIELD));
            doc.removeField(ELEM_LST_FIELD);
            
            final WriteResult wr = to_coll.save(doc, WriteConcern.SAFE);
            if (!wr.getCachedLastError().ok()) {
                System.err.println("Document[" + id + "] update error.");
            } else {
                reverted++;
            }
            total++;
        }
        cursor.close();
        
        System.out.println("total/undo: " + total + "/" + reverted);
    }
    
    private static void usage() {
        System.err.println("usage: UndoUpdate <mongo_host> " + 
        "[-mongo_db=<mongo_db>] [-from_date=<yyyymmdd>] [-mongo_port=<port>]");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws UnknownHostException, 
                                                        ParseException {
        if (args.length < 1) {
            usage();
        }
        String mongo_db = SOCIAL_CHECK_DB;
        String fromDate = "20140101";
        int port = BrokenLinks.DEFAULT_PORT;
        
        for (int idx = 2; idx < args.length; idx++) {
            if (args[idx].startsWith("-mongo_db=")) {
                mongo_db = args[idx].substring(10);
            } else if (args[idx].startsWith("-from_date=")) {
                fromDate = args[idx].substring(11);
            } else if (args[idx].startsWith("-mongo_port=")) {
                port = Integer.parseInt(args[idx].substring(12));
            } else {
                usage();
            }
        }        
        undo(args[0], port, mongo_db, fromDate);
    }
}
