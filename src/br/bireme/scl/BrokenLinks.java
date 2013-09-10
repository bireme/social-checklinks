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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Heitor Barbieri
 * date 20130625
 */
public class BrokenLinks {
    public static final String VERSION = "V0.1";

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
    
    /* CC_FIELDS_COL colection fields */
    public static final String MST_FIELD = "mst";
    public static final String URL_TAG_FIELD = "urlTag";
    public static final String CC_TAGS_FIELD = "ccTags";

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

        final boolean checkPassword = false;
        if (checkPassword) {
            final boolean auth = db.authenticate(user, password.toCharArray());
            if (!auth) {
                throw new IllegalArgumentException("invalid user/password");
            }
        }

        final DBCollection coll = db.getCollection(BROKEN_LINKS_COL);
        final DBCollection ccColl = db.getCollection(CC_FIELDS_COL);
        final int urlTag = getIsisUrlFields(mName, ccColl);
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
                final String[] split = lineT.split(" *\\| *", 3);

                saveRecord(mName, split[0], split[1], 
                                             split[2], urlTag, tags, mst, coll);
                if (++tell % 5000 == 0) {
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
        final DBCursor cursor = coll.find(query);
        int urlTag = -1;

        if (cursor.hasNext()) {
            final BasicDBObject obj = (BasicDBObject)cursor.next();
            
            urlTag = obj.getInt(URL_TAG_FIELD);
        } else {
            throw new IOException("Missing collection: " + coll.getName());
        }        
        cursor.close();

        return urlTag;        
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
                                      final DBCollection coll)
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

        final Record rec = mst.getRecord(Integer.parseInt(id));
        if (!rec.isActive()) {
            throw new BrumaException("not active record mfn=" + id);
        }

        final List<Field> urls = rec.getFieldList(urlTag);        
        int occ = 0;

        while (true) {
            occ = nextOcc(url, urls, occ);
            if (occ == -1) {
                System.out.println("url[" + url + "] not found."); break;
                //throw new BrumaException("url[" + url + " not found.");
            }
            final BasicDBObject query = new BasicDBObject(ID_FIELD,
                                                                id + "_" + occ);
            final DBCursor cursor = coll.find(query);
            final int size = cursor.size();

            cursor.close();
            if (size == 0) {
                break;
            }
        }

        final BasicDBObject doc = new BasicDBObject();
        
        doc.put(DATE_FIELD, new Date());
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
                               final int prevOcc) throws BrumaException {
        assert url != null;
        assert urls != null;
        assert prevOcc > 0;

        int cocc = 1;
        boolean found = false;

        outter : for (Field fld : urls) {
            if (cocc > prevOcc) {
                for (Subfield sub : fld.getSubfields()) {
                    if (url.trim().equals(sub.getContent().trim())) {
                        found = true;
                        break outter;
                    }
                }
            }
            cocc++;
        }
        return found ? cocc : -1;
    }
    
    private static BasicDBList getCCS(final Record rec, 
                                      final List<Integer> ccsFlds) 
                                                         throws BrumaException {
        assert rec != null;
        assert ccsFlds != null;
        
        final BasicDBList lst = new BasicDBList();
        final Set<String> set = new HashSet<String>();
        
        for (int ccsFld : ccsFlds) {
            final List<Field> fldList = rec.getFieldList(ccsFld);
            
            for (Field fld : fldList) {
                final Subfield sub = fld.getSubfield('_', 1);
                set.add(sub.getContent().trim());
            }
        }
        for (String elem : set) {
            lst.add(elem);
        }

        return lst;
    }

    private static void usage() {
        System.err.println(
                         "usage: CreateBrokenLinks <outFile> <mstName> <host>"
     + "\n\t\t[-outFileEncoding=<outFileEncod>] [-outMstEncoding=<outMstEncod>]"
     + "\n\t\t[-port=<port>] [-user=<user> -password=<pswd>]");
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

        if (len < 3) {
            usage();
        }
        for (int idx = 3; idx < len; idx++) {
            if (args[idx].startsWith("-port=")) {
                port = Integer.parseInt(args[idx].substring(6));
            } else if (args[idx].startsWith("-outFileEncod=")) {
                fileEncod = args[idx].substring(14);
            } else if (args[idx].startsWith("-outMstEncoding=")) {
                mstEncod = args[idx].substring(16);
            } else if (args[idx].startsWith("-user=")) {
                user = args[idx].substring(6);
            } else if (args[idx].startsWith("-password=")) {
                pswd = args[idx].substring(10);
            }
        }

        createLinks(args[0], fileEncod, args[1], mstEncod, args[2],
                                                        port, user, pswd, true);

/*
        createLinks("./LILACS_v8broken.txt", DEFAULT_FILE_ENCODING,
                    "/home/heitor/Downloads/lilacs", DEFAULT_MST_ENCODING,
                    DEFAULT_HOST);
*/
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
