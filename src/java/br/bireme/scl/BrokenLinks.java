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

import bruma.BrumaException;
import bruma.master.Field;
import bruma.master.Master;
import bruma.master.MasterFactory;
import bruma.master.Record;
import bruma.master.Subfield;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Heitor Barbieri
 * date 20130625
 */
public class BrokenLinks {
    public static final String VERSION = "0.3";

    /* MongoDb settings */
    public static final String DEFAULT_FILE_ENCODING = "IBM850";
    public static final String DEFAULT_MST_ENCODING = "IBM850";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 27017;
    
    /* Database */
    public static final String SOCIAL_CHECK_DB = "SocialCheckLinks";
    
    /* Collections */
    public static final String BROKEN_LINKS_COL = "BrokenLinks";
    public static final String HISTORY_COL = "HistoryBrokenLinks";
    public static final String CC_FIELDS_COL = "IsisCcFields";
        
    /* SOCIAL_CHECK_COL colection fields */
    public static final String ID_FIELD = "_id";
    public static final String CENTER_FIELD = "center";
    public static final String BROKEN_URL_FIELD = "burl";
    public static final String FIXED_URL_FIELD = "furl";
    public static final String HISTORY_URL_FIELD = "hurl";
    public static final String MSG_FIELD = "msg";
    public static final String DATE_FIELD = "date";
    public static final String LAST_UPDATE_FIELD = "updated";
    
    /* CC_FIELDS_COL colection fields */
    public static final String MST_FIELD = "mst";
    public static final String URL_TAG_FIELD = "urlTag";
    public static final String CC_TAGS_FIELD = "ccTags";
    
    /* HistoryBrokenLinks collection fields */
    public static final String ELEM_LST_FIELD = "elems";

    public static final String DEF_FIELD = ID_FIELD;

    
    public static void createLinks(final String outCheckFile,
                                   final String mstName) throws BrumaException,
                                                                IOException {
        createLinks(outCheckFile, DEFAULT_FILE_ENCODING, mstName,
            DEFAULT_MST_ENCODING, DEFAULT_HOST, DEFAULT_PORT, null, null, true);
    }

    public static void createLinks(final String outCheckFile,
                                   final String outEncoding,
                                   final String mstName,
                                   final String mstEncoding,
                                   final String host) throws BrumaException,
                                                             IOException {
        createLinks(outCheckFile, outEncoding, mstName, mstEncoding, host,
                                        DEFAULT_PORT, null, null, true);
    }

    public static void createLinks(final String outCheckFile,
                                   final String outEncoding,
                                   final String mstName,
                                   final String mstEncoding,
                                   final String host,
                                   final int port,
                                   final String user,
                                   final String password,
                                   final boolean clearCol)
                                                          throws BrumaException,
                                                                 IOException {
        if (outCheckFile == null) {
            throw new NullPointerException("outCheckFile");
        }
        if (mstName == null){
            throw new NullPointerException("mstName");
        }
        if (mstEncoding == null){
            throw new NullPointerException("mstEncoding");
        }
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port <= 0");
        }

        final Master mst = MasterFactory.getInstance(mstName)
                                        .setEncoding(mstEncoding).open();
        final String mName = new File(mst.getMasterName()).getName();
        final BufferedReader in = new BufferedReader(new InputStreamReader(
                              new FileInputStream(outCheckFile), outEncoding));
        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(SOCIAL_CHECK_DB);
        final Map<String,Integer> occMap = new HashMap<String,Integer>();

        final boolean checkPassword = false;
        if (checkPassword) {
            final boolean auth = db.authenticate(user, password.toCharArray());
            if (!auth) {
                throw new IllegalArgumentException("invalid user/password");
            }
        }

        final DBCollection coll = db.getCollection(BROKEN_LINKS_COL);
        final DBCollection ccColl = db.getCollection(CC_FIELDS_COL);
        
        if (ccColl.findOne() == null) {
            if (!createCcFieldsCollection(ccColl)) {
                throw new IOException("CC fields collection creation failed");
            }
        }
        final int urlTag = getIsisUrlFields(mName, ccColl);
        if (urlTag <= 0) {
            throw new IOException("Missing Isis url fields");
        }
        final List<Integer> tags = getIsisCcFields(mName, ccColl);
        
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
//System.out.println("line[" + lineT + "]");                
                final String[] split = lineT.split(" *\\| *", 3);

                saveRecord(mName, split[0], split[1], 
                                     split[2], urlTag, tags, mst, coll, occMap);
                if (++tell % 5000 == 0) {
                    System.out.println("++" + tell);
                }
            }
        }

        removeOldDocs(coll);
        
        if (clearCol) {
            createIndex(coll);
        }

        in.close();
        mst.close();
    }
    
    /**
     * Creates the CC Fields Collection and insert a lilacs metadoc document.
     * @param coll CC Fields Collection
     * @return true if ok, false if error
     */
    private static boolean createCcFieldsCollection(final DBCollection coll) {
        assert coll != null;
        
        final BasicDBObject doc = new BasicDBObject();
        final BasicDBList lst = new BasicDBList();
        
        lst.add(1);
        lst.add(920);
        lst.add(930);
        doc.put(MST_FIELD, "LILACS");
        doc.put(URL_TAG_FIELD, 8);
        doc.put(CC_TAGS_FIELD, lst);

        final WriteResult ret = coll.save(doc, WriteConcern.SAFE);

        return ret.getCachedLastError().ok();
    }
    
    private static List<Integer> getIsisCcFields(final String mstName,
                                                 final DBCollection coll) 
                                                            throws IOException {
        assert mstName != null;
        assert coll != null;
        
        final List<Integer> lst = new ArrayList<Integer>();
        final BasicDBObject query = new BasicDBObject(MST_FIELD, mstName);
        final DBCursor cursor = coll.find(query);

        if (cursor.hasNext()) {
            final BasicDBObject obj = (BasicDBObject)cursor.next();
            final BasicDBList flds = (BasicDBList)obj.get(CC_TAGS_FIELD);
            if (flds.isEmpty()) {
                throw new IOException("Missing CCs field");
            }
            for (Object tag : flds) {
                lst.add((Integer)tag);
            }
        } else {
            throw new IOException("Missing collection: " + coll.getName());
        }               
        cursor.close();

        return lst;        
    }
    
    private static int getIsisUrlFields(final String mstName,
                                        final DBCollection coll) 
                                                            throws IOException {
        assert mstName != null;
        assert coll != null;
        
        final BasicDBObject query = new BasicDBObject(MST_FIELD, mstName);
        final BasicDBObject doc = (BasicDBObject)coll.findOne(query);

        if (doc == null) {
            throw new IOException("Missing fields: collection[" + coll.getName() 
                    + "] or master name[" + mstName + "]");
        }

        return doc.getInt(URL_TAG_FIELD);
    }

    private static void createIndex(final DBCollection coll) {
        assert coll != null;

        final BasicDBObject flds = new BasicDBObject();
        flds.append(CENTER_FIELD, 1);
        flds.append(BROKEN_URL_FIELD, 1);
        coll.ensureIndex(flds);
    }

    private static boolean saveRecord(final String mstName,
                                      final String id,
                                      final String url,
                                      final String err,
                                      final int urlTag,
                                      final List<Integer> ccsFlds,                                      
                                      final Master mst,
                                      final DBCollection coll,
                                      final Map<String,Integer> occMap)
                                                         throws BrumaException,
                                                                IOException {
        assert mstName != null;
        assert id != null;
        assert url != null;
        assert urlTag > 0;
        assert err != null;
        assert ccsFlds != null;
        assert mst != null;
        assert coll != null;
        assert occMap != null;

        final Record rec = mst.getRecord(Integer.parseInt(id));
        if (!rec.isActive()) {
            throw new BrumaException("not active record mfn=" + id);
        }

        final List<Field> urls = rec.getFieldList(urlTag);        
        final Date now = new Date();
        Date date;
        
        Integer curOcc = occMap.get(id);
        if (curOcc == null) {
            curOcc = 0;
        }
        final int occ = nextOcc(url, urls, curOcc, occMap);
        if (occ == -1) {                
            //System.err.println("url[" + url + "] not found. mfn=" + id);
            //throw new IOException("url[" + url + "] not found. mfn=" + id);                
            return false;
        }

        final BasicDBObject query = new BasicDBObject(ID_FIELD,
                                                            id + "_" + occ);
        final BasicDBObject obj = (BasicDBObject) coll.findOne(query);            
        if (obj == null) {
            date = now;
        } else {
            date = obj.getDate(LAST_UPDATE_FIELD);
            if (date == null) {
                date = obj.getDate(DATE_FIELD);
            } else {
                final WriteResult wr = coll.remove(obj, WriteConcern.SAFE);
                if (!wr.getCachedLastError().ok()) {
                    //TODO
                }
                date = obj.getDate(DATE_FIELD);
            }
        }
        final BasicDBObject doc = new BasicDBObject();        
        doc.put(DATE_FIELD, date);
        doc.put(LAST_UPDATE_FIELD, now);
        doc.put(MST_FIELD, mstName);
        doc.put(ID_FIELD, id + "_" + occ);
        doc.put(BROKEN_URL_FIELD, url);
        doc.put(MSG_FIELD, err);
        doc.put(CENTER_FIELD, getCCS(rec, ccsFlds));        

        final WriteResult ret = coll.save(doc, WriteConcern.SAFE);

        return ret.getCachedLastError().ok();
    }

    private static int nextOcc(final String url,
                               final List<Field> urls,
                               final int code,
                               final Map<String,Integer> occMap) 
                                                         throws BrumaException {
        assert url != null;
        assert urls != null;
        assert code >= 0;
        assert occMap != null;

        int code2 = code;
        int val = 1;
        int ret = 1;
        
        outter : for (Field fld : urls) {
            for (Subfield sub : fld.getSubfields()) {
                if (url.trim().equals(sub.getContent().trim())) {
                    if ((code2 & val) == 0) { // occurrence not used
                        code2 |= val;
                        occMap.put(url, code2);
                        break outter;
                    }
                }    
            }
            if (val > Integer.MAX_VALUE/2) {
                ret = -1;
                break;
            }
            val *= 2;
            ret++;
        }        
        return ret;
    }
    
    private static BasicDBList getCCS(final Record rec, 
                                      final List<Integer> ccsFlds) 
                                            throws BrumaException, IOException {
        assert rec != null;
        assert ccsFlds != null;
        
        final BasicDBList lst = new BasicDBList();
        final Set<String> set = new HashSet<String>();
        
        for (int ccsFld : ccsFlds) {
            final List<Field> fldList = rec.getFieldList(ccsFld);
            
            for (Field fld : fldList) {
                final Subfield sub = fld.getSubfield('_', 1);
                if (sub == null) {
                    throw new IOException("Missing subfield. Mfn=" 
                                       + rec.getMfn() + " tag=" + fld.getId());
                }
                set.add(sub.getContent().trim());
            }
        }
        for (String elem : set) {
            lst.add(elem);
        }

        return lst;
    }

    private static boolean removeOldDocs(final DBCollection coll) {
        assert coll != null;
        
        final Date now = new Date();
        final DBCursor cursor = coll.find();
        boolean ret = true;

        while (cursor.hasNext()) {
            final BasicDBObject obj = (BasicDBObject)cursor.next();
            final Date auxDate = obj.getDate(LAST_UPDATE_FIELD);
            if ((auxDate == null) || 
                             (now.getTime() - auxDate.getTime()) > 60*60*1000) {
                final WriteResult wr = coll.remove(obj, WriteConcern.SAFE);
                ret = ret && wr.getCachedLastError().ok();
            }
        }
        return ret;
    }
    
    private static void usage() {
        System.err.println(
                         "usage: CreateBrokenLinks <outFile> <mstName> <host>"
     + "\n\t\t[-outFileEncoding=<outFileEncod>] [-outMstEncoding=<outMstEncod>]"
     + "\n\t\t[-port=<port>] [-user=<user> -password=<pswd>] [--clearColl]");
        System.exit(1);
    }

    public static void main(final String[] args) throws BrumaException,
                                                        IOException {
        final int len = args.length;
        int port = DEFAULT_PORT;
        String fileEncod = DEFAULT_FILE_ENCODING;
        String mstEncod = DEFAULT_MST_ENCODING;
        String user = null;
        String pswd = null;
        boolean clearColl = false;

        if (len < 3) {
            usage();
        }
        for (int idx = 3; idx < len; idx++) {
            if (args[idx].startsWith("-port=")) {
                port = Integer.parseInt(args[idx].substring(6));
            } else if (args[idx].startsWith("-outFileEncoding=")) {
                fileEncod = args[idx].substring(17);
            } else if (args[idx].startsWith("-outMstEncoding=")) {
                mstEncod = args[idx].substring(16);
            } else if (args[idx].startsWith("-user=")) {
                user = args[idx].substring(6);
            } else if (args[idx].startsWith("-password=")) {
                pswd = args[idx].substring(10);
            } else if (args[idx].equals("--clearColl")) {
                clearColl = true;
            } else {
                usage();
            }
        }

        System.out.println("outFileEncoding=" + fileEncod);
        System.out.println("outMstEncoding=" + mstEncod);
        System.out.println();
        
        createLinks(args[0], fileEncod, args[1], mstEncod, args[2],
                                                   port, user, pswd, clearColl);


        /*createLinks("/home/heitor/Downloads/LILACS_v8broken.txt", DEFAULT_FILE_ENCODING,
                    "/home/heitor/Downloads/lilacs", DEFAULT_MST_ENCODING,
                    DEFAULT_HOST); */
        
        /*createLinks("/home/heitor/Downloads/LILACS_v8broken.txt", DEFAULT_FILE_ENCODING,
                    "/home/heitor/Downloads/lilacs", DEFAULT_MST_ENCODING,
                    "ts01vm.bireme.br");*/

        /*createLinks("./LILACS_v8broken.txt", DEFAULT_FILE_ENCODING,
                    "/home/heitor/temp/lilacs", DEFAULT_MST_ENCODING,
                    "ts01vm.bireme.br");        */
        //createLinks("./teste.txt", DEFAULT_FILE_ENCODING,
        //    "/home/heitor/temp/lilacs", DEFAULT_MST_ENCODING, "ts01vm.bireme.br");
        //createLinks("./um.txt", DEFAULT_FILE_ENCODING,
        //            "/home/heitor/temp/lilacs", DEFAULT_MST_ENCODING,
        //                                                  "ts01vm.bireme.br");
    }
}
