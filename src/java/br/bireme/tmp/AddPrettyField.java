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
package br.bireme.tmp;

import br.bireme.scl.BrokenLinks;
import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.ELEM_LST_FIELD;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.PRETTY_BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import br.bireme.scl.EncDecUrl;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;

/**
 * Adiciona, se não existir, o campo 'pretty bug url' na coleção 
 * HistoryBrokenLinks.  
 * @author Heitor Barbieri
 * date: 20150609
 */
public class AddPrettyField {
    private static void add(final String mongo_host,
                            final int mongo_port,
                            final String mongo_db,
                            final String mongo_col) throws UnknownHostException {
        assert mongo_host != null;
        assert mongo_port > 0;
        assert mongo_db != null;
        assert mongo_col != null;
        
        final MongoClient client = new MongoClient(mongo_host, mongo_port); 
        final DB db = client.getDB(mongo_db);
        final DBCollection coll = db.getCollection(HISTORY_COL);        
        final DBCursor cursor = coll.find();
        int total = 0;
        
        System.out.println("host=" + mongo_host);
        System.out.println("port=" + mongo_port);
        System.out.println("database=" + mongo_db);
        System.out.println("collection=" + mongo_col);
        System.out.println("num of documents=" + cursor.size());
        
        while (cursor.hasNext()) {
            final BasicDBObject doc1 = (BasicDBObject)cursor.next();
            final BasicDBList list = (BasicDBList)doc1.get(ELEM_LST_FIELD);
            
            for (Object obj : list) {
                final BasicDBObject doc2 = (BasicDBObject)obj;
                if (!doc2.containsField(PRETTY_BROKEN_URL_FIELD)) {
                    final String burl = doc2.getString(BROKEN_URL_FIELD);                                        
                    try {                                          
                        final String pburl = EncDecUrl.decodeUrl(burl);
                        doc2.append(PRETTY_BROKEN_URL_FIELD, pburl);
                    
                        final WriteResult wr = coll.save(doc1, 
                                                     WriteConcern.ACKNOWLEDGED);
                        if (wr.getCachedLastError().ok()) {
                            total++;
                        } else {
                            System.err.println("Document[" 
                                   + doc1.getString("_id") + "] update error.");
                        }
                    } catch (IOException ioe) {
                        System.err.println("Document[" 
                             + doc1.getString("_id") + "] bad encode conversion"
                             + " url=[" + burl + "]");
                    }
                }
            }                        
        }
        cursor.close();        
        System.out.println("num of added fields: " + total);
    }
    
    private static void usage() {
        System.err.println("usage: AddPrettyField <mongo_host> " + 
                                                        "[-mongo_port=<port>]");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws UnknownHostException, 
                                                        ParseException {
        if (args.length < 1) {
            usage();
        }
        final String mongo_db = SOCIAL_CHECK_DB;
        final String mongo_col = HISTORY_COL;
        int port = BrokenLinks.DEFAULT_PORT;
        
        for (int idx = 1; idx < args.length; idx++) {
            if (args[idx].startsWith("-mongo_port=")) {
                port = Integer.parseInt(args[idx].substring(12));
            } else {
                System.err.println(args[idx] + "\n");
                usage();
            }
        }        
        add(args[0], port, mongo_db, mongo_col);
    }
}
