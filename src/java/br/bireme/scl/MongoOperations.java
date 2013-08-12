package br.bireme.scl;

import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.HISTORY_MONGO_COL;
import static br.bireme.scl.BrokenLinks.MONGO_COL;
import static br.bireme.scl.BrokenLinks.MONGO_DB;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Heitor Barbieri
 * date: 20130729
 */
public class MongoOperations {
    public static final String FIXED_URL_FIELD = "furl";
    public static final String DATE_FIELD = "date";    
    public static final String USER_FIELD = "user";
    public static final String AUTO_FIX = "autofix";
            
    public static Set<String> getCenters(final DBCollection coll) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        final Set<String> set = new TreeSet<String>();        
        final DBCursor cursor = coll.find();
        
        while (cursor.hasNext()) {
            set.add((String) cursor.next().get(CENTER_FIELD));
        }
        cursor.close();
        
        return set;
    }
    
    public static List<IdUrl> getCenterUrls(final DBCollection coll,
                                            final String centerId) {
        return getCenterUrls(coll, centerId, 1, Integer.MAX_VALUE);
    }
    
    /**
     * 
     * @param coll
     * @param centerId
     * @param from indice inicial a ser recuperado. Começa de 1.
     * @return 
     */
    public static List<IdUrl> getCenterUrls(final DBCollection coll,
                                            final String centerId,
                                            final int from,
                                            final int count) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (centerId == null) {
            throw new NullPointerException("centerId");
        }
        if (from < 1) {
            throw new IllegalArgumentException("from[" + from + "] < 1");
        }
        if (count < 1) {
            throw new IllegalArgumentException("count[" + count + "] < 1");
        }
        final List<IdUrl> lst = new ArrayList<IdUrl>();
        final BasicDBObject query = new BasicDBObject(CENTER_FIELD, centerId);        
        final DBCursor cursor = coll.find(query).skip(from - 1).limit(count);
        
        while (cursor.hasNext()) {
            final DBObject doc = cursor.next();
            final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD), 
                                       (String)doc.get(BROKEN_URL_FIELD));
            lst.add(iu);
System.out.println("#=" + lst.size() + " hash=" + iu.hashCode() + " id=" + iu.id + " url=" + iu.url);
        }
        cursor.close();
        
        return lst;
    }
           
    public static int getCenterUrlsNum(final DBCollection coll,
                                       final String centerId) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (centerId == null) {
            throw new NullPointerException("centerId");
        }
        final BasicDBObject query = new BasicDBObject(CENTER_FIELD, centerId);        
        final DBCursor cursor = coll.find(query);
        final int num = cursor.count();
        
        cursor.close();
        
        return num;
    }
    
    static Set<IdUrl> getDocsWith(final DBCollection coll,
                                  final String centerId,
                                  final String pattern) {
        assert coll != null;
        assert centerId == null;
        assert pattern == null;
        
        final Matcher mat = Pattern.compile(pattern).matcher("");
        final Set<IdUrl> set = new HashSet<IdUrl>();
        
        for (IdUrl iu : getCenterUrls(coll, centerId)) {
            mat.reset(iu.url);
            if (mat.find()) {
                set.add(new IdUrl(iu.id, iu.url));
            }
        }
        return set;
    }
    
    public static boolean updateDocument(final DBCollection coll,
                                         final DBCollection hcoll,
                                         final String docId,
                                         final String fixedUrl,
                                         final String user,
                                         final boolean automatic) 
                                                            throws IOException {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (hcoll == null) {
            throw new NullPointerException("hcoll");
        }
        if (docId == null) {
            throw new NullPointerException("docId");
        }
        if (fixedUrl == null) {
            throw new NullPointerException("fixedUrl");
        }
        if (user == null) {
            throw new NullPointerException("user");
        }
        
        final BasicDBObject query = new BasicDBObject(ID_FIELD, docId);        
        final DBCursor cursor = coll.find(query);
        
        if (!cursor.hasNext()) {
            throw new IOException("document not found id[" + docId + "]");
        }
        final BasicDBObject doc = (BasicDBObject)cursor.next();
        cursor.close();
        
        final boolean ret1 = coll.remove(doc, WriteConcern.SAFE)
                                                           .getLastError().ok();
        
        doc.append(FIXED_URL_FIELD, fixedUrl).append(USER_FIELD, user)
           .append(AUTO_FIX, automatic).append(DATE_FIELD, new Date());
        
        final boolean ret2 = hcoll.save(doc).getLastError().ok();
        
        return ret1 && ret2;
    }
    
    public static Set<IdUrl> fixRelatedUrls(final DBCollection coll,
                                            final DBCollection hcoll,
                                            final String user,
                                            final String centerId,
                                            final String brokenUrl,
                                            final String fixedUrl) 
                                                            throws IOException { 
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (hcoll == null) {
            throw new NullPointerException("hcoll");
        }
        if (user == null) {
            throw new NullPointerException("user");
        }
        if (centerId == null) {
            throw new NullPointerException("centerId");
        }
        if (brokenUrl == null) {
            throw new NullPointerException("brokenUrl");
        }
        if (fixedUrl == null) {
            throw new NullPointerException("fixedUrl");
        }
        final String[] patterns = Tools.getPatterns(brokenUrl, fixedUrl);        
        final Set<IdUrl> docs = getDocsWith(coll, centerId, patterns[0]);                                                                        
        final Set<IdUrl> converted = Tools.getConvertedUrls(docs, 
                                                      patterns[0], patterns[1]);
        final Map<String,IdUrl> map = new HashMap<String,IdUrl>();        
        for (IdUrl iu : converted) {
            map.put(iu.url, iu);
        }
        final Set<IdUrl> ret = new HashSet<IdUrl>();
        final String[] inurls = map.keySet().toArray(new String[0]);
        final int[] results = new CheckUrlArray().check(inurls);
        final int len = results.length;
        
        for (int idx = 0; idx < len; idx++) {
            if (!CheckUrl.isBroken(results[idx])) {
                final IdUrl iu = map.get(inurls[idx]);
                ret.add(iu);
                if (!updateDocument(coll, hcoll, iu.id, iu.url, user, 
                                                    !fixedUrl.equals(iu.url))) {
                    throw new IOException("could not update document id=" 
                                                                       + iu.id);
                }
            }
        }
        
        return ret;
    }
    
    public static void main(final String[] args) throws UnknownHostException, 
                                                                   IOException {
        final MongoClient mongoClient = new MongoClient("ts01vm.bireme.br");
        final DB db = mongoClient.getDB(MONGO_DB);
        final DBCollection coll = db.getCollection(MONGO_COL);
        final DBCollection hcoll = db.getCollection(HISTORY_MONGO_COL);
        
        final Set<String> centers = getCenters(coll);
        for (String center : centers) {
            System.out.println(center);
        }
        System.out.println();
        
        final List<IdUrl> ius = getCenterUrls(coll, "PE1.1");
        for (IdUrl iu : ius) {
            System.out.println("1) " + iu.id + "  " + iu.url);
        }
        
        final Set<IdUrl> rel = fixRelatedUrls(coll, hcoll, "Heitor", "PE1.1", 
                //"xhttp://new.paho.org/bireme/index.php?Itemid=43&lang=pt",
                //"http://new.paho.org/bireme/index.php?Itemid=43&lang=pt");
        
                //"http://new.paho.org/bireme/indexx.php?Itemid=43&lang=pt",
                //"http://new.paho.org/bireme/index.php?Itemid=43&lang=pt");
        
                "http://new.paho.org/bireme/indexx.php?Itemid=43&lang=xpt",
                "http://new.paho.org/bireme/index.php?Itemid=43&lang=pt");
        
                //"http://new.paho.org/bireme/index.php?Itemid=43&lang=xpt",
                //"http://new.paho.org/bireme/index.php?Itemid=43&lang=pt");
            for (IdUrl iu : rel) {
            System.out.println("2) " + iu.id + "  " + iu.url);
        }

    }
}
