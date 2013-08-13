package br.bireme.scl;

import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import static br.bireme.scl.BrokenLinks.FIXED_URL_FIELD;
import static br.bireme.scl.BrokenLinks.HISTORY_MONGO_COL;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.MONGO_DB;
import static br.bireme.scl.MongoOperations.DATE_FIELD;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Heitor Barbieri
 * date: 20130813
 */
public class Gizmo {
    class Elem {        
        String id;
        String burl;
        String furl;
        long date;
        
        Elem(String id) {
            assert id != null;
            this.id = id;
        }
    }
    
    Map<String,Elem>loadRecords(final String host,
                                final int port) throws UnknownHostException {
        assert host != null;
        assert port > 0;
        
        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(MONGO_DB);
        final DBCollection coll = db.getCollection(HISTORY_MONGO_COL);
        final Calendar lastWeek = Calendar.getInstance();
                                          
        lastWeek.add(Calendar.WEEK_OF_YEAR, -1);
        final Date start = lastWeek.getTime();        
        final BasicDBObject query = new BasicDBObject(DATE_FIELD,
                                              new BasicDBObject("$gt", start));
        final DBCursor cursor = coll.find(query);
        final Map<String, Elem> map = getLastElements(cursor);
        
        cursor.close();
        
        return map;
    }
    
    Map<String,Elem> getLastElements(final DBCursor cursor) {
        assert cursor != null;
        
        final Map<String,Elem> map = new HashMap<String,Elem>();
        
        while (cursor.hasNext()) {
            final BasicDBObject obj = (BasicDBObject)cursor.next();
            final String field = obj.getString(ID_FIELD);
            final long time = obj.getDate(DATE_FIELD).getTime();
            
            Elem elem = map.get(field);
            if (elem == null) {
                elem = new Elem(field);
                elem.burl = obj.getString(BROKEN_URL_FIELD);
                elem.furl = obj.getString(FIXED_URL_FIELD);
                elem.date = time; 
                map.put(field, elem);
            } else {                
                if (time > elem.date) {
                    elem.burl = obj.getString(BROKEN_URL_FIELD);
                    elem.furl = obj.getString(FIXED_URL_FIELD);
                    elem.date = time;                                    
                }
            }
        }
        
        return map;
    }
    
    public void createGizmo(final String host,
                            final int port,
                            final String gizFile,
                            final String encoding) throws UnknownHostException, 
                                                          IOException {
        assert host != null;
        assert port > 0;
        assert gizFile != null;
        assert encoding != null;
        
        final Map<String,Elem> map = loadRecords(host, port);
        final BufferedWriter out = new BufferedWriter(new OutputStreamWriter
                                     (new FileOutputStream(gizFile), encoding));
        
        final Collection<Elem> elems = map.values();
        
        for (Elem elem : elems) {
            String id = elem.id.split("_", 2)[0];
            out.append(id + "|" + elem.burl + "|" + elem.furl + "\n");
        }
        
        out.close();
    }
    
    public static void main(String[] args) throws UnknownHostException, 
                                                  IOException {
        final Gizmo giz = new Gizmo();
        
        giz.createGizmo("ts01vm.bireme.br", DEFAULT_PORT, "out.giz", "UTF-8");
    }
}