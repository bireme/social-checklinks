package br.bireme.scl;

import bruma.BrumaException;
import bruma.master.Master;
import bruma.master.MasterFactory;
import bruma.master.Record;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Heitor Barbieri
 * date 20130625
 */
public class BrokenLinks {
    public static final String DEFAULT_HOST = "localhost";   
    public static final String MONGO_DB = "CheckLinks";
    public static final String MONGO_COL = "BrokenLinks";
    public static final String HISTORY_MONGO_COL = "HistoryBrokenLinks";
    
    public static final int DEFAULT_PORT = 27017;    
    public static final int LIL_CENTER_FLD = 1;
    
    public static final String ID_FIELD = "_id";
    public static final String CENTER_FIELD = "center";
    public static final String BROKEN_URL_FIELD = "burl";
    public static final String FIXED_URL_FIELD = "furl";
    public static final String HISTORY_URL_FIELD = "hurl";
    public static final String MSG_FIELD = "msg";    
    
    public static final String DEF_FIELD = ID_FIELD;
    
    public static void createLinks(final String outCheckFile,
                                   final String mstName) throws BrumaException, 
                                                                IOException {
        createLinks(outCheckFile, mstName, DEFAULT_HOST, DEFAULT_PORT, false);
    }
    
    public static void createLinks(final String outCheckFile,
                                   final String mstName,
                                   final String host) throws BrumaException, 
                                                             IOException {
        createLinks(outCheckFile, mstName, host, DEFAULT_PORT, false);
    }
                                   
    public static void createLinks(final String outCheckFile,
                                   final String mstName,
                                   final String host,
                                   final int port,
                                   final boolean clearCol) 
                                                          throws BrumaException, 
                                                                 IOException {
        if (outCheckFile == null) {
            throw new NullPointerException("outCheckFile");
        }
        if (mstName == null){
            throw new NullPointerException("mstName");
        }
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port <= 0");
        }
        
        final Master mst = MasterFactory.getInstance(mstName).open();
        final BufferedReader in = new BufferedReader(
                                                  new FileReader(outCheckFile));
        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(MONGO_DB);
        final DBCollection coll = db.getCollection(MONGO_COL);
        int tell = 0;
        
        if (clearCol) {
            coll.remove(new BasicDBObject());
        }
                
        while (true) {
            final String line = in.readLine();
            if (line == null) {
                break;
            }
            final String lineT = line.trim();
            if (!lineT.isEmpty()) {
                final String[] split = lineT.split("\\|", 3);
                
                saveRecord(split[0], split[1], split[2], mst, coll);
                if (++tell % 1000 == 0) {
                    System.out.println("++" + tell);
                }
            }            
        }
        
        if (clearCol) {
            createIndex(coll);
        }
        
        in.close();
        mst.close();
    }
    
    private static void createIndex(final DBCollection coll) {
        assert coll != null;
        
        final BasicDBObject flds = new BasicDBObject();
        flds.append(CENTER_FIELD, 1);
        flds.append(BROKEN_URL_FIELD, 1);
        coll.ensureIndex(flds);        
    }
        
    private static boolean saveRecord(final String id,
                                      final String url,
                                      final String err,
                                      final Master mst,
                                      final DBCollection coll) 
                                                         throws BrumaException {
        assert id != null;
        assert url != null;
        assert err != null;
        assert mst != null;
        assert coll != null;
        
        final Record rec = mst.getRecord(Integer.parseInt(id));                
        if (!rec.isActive()) {
            throw new BrumaException("not active record mfn=" + id);
        }
        final String centerId = rec.getField(LIL_CENTER_FLD, 1).getContent();                                
        final BasicDBObject regExp = new BasicDBObject("$regex", id + "_\\d+");
        final BasicDBObject query = new BasicDBObject(ID_FIELD, regExp);
        final DBCursor cursor = coll.find(query);
        BasicDBObject doc = null;
        boolean found = false;
        int occ = 0;
        
        while (cursor.hasNext()) {
            final BasicDBObject cdoc = (BasicDBObject) cursor.next();
            
            if (url.equals(cdoc.getString(BROKEN_URL_FIELD))) {
                doc = cdoc;
                doc.put(BROKEN_URL_FIELD, url);
                doc.put(MSG_FIELD, err);
                found = true;
                break;
            }
            occ++;
        }
        cursor.close();
        
        if (!found) {
            doc = new BasicDBObject();            
            doc.put(ID_FIELD, id + "_" + occ);
            doc.put(CENTER_FIELD, centerId);
            doc.put(BROKEN_URL_FIELD, url);
            doc.put(MSG_FIELD, err);
        }
                        
        final WriteResult ret = coll.save(doc, WriteConcern.SAFE);
        
        return ret.getCachedLastError().ok();
    }    
        
    private static void usage() {
        System.err.println(
                         "usage: CreateBrokenLinks <outFile> <mstName> <host>");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws BrumaException, 
                                                        IOException {
        /*if (args.length != 3) {
            usage();
        }
        
        createLinks(args[0], args[1], args[2]);*/
        createLinks("./LILACS_v8broken.txt", "/home/heitor/temp/lilacs", "ts01vm.bireme.br");
        //createLinks("./um.txt", "/home/heitor/temp/lilacs", "ts01vm.bireme.br");
    }
}
