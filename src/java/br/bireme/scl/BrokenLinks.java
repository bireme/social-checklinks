/*=========================================================================

    Copyright © 2013 BIREME/PAHO/WHO

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
import java.util.Arrays;
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
    public static final String VERSION = "1.0";
    public static final String VERSION_DATE = "2016";

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
    public static final String PRETTY_BROKEN_URL_FIELD = "pburl";
    public static final String FIXED_URL_FIELD = "furl";
    public static final String HISTORY_URL_FIELD = "hurl";
    public static final String MSG_FIELD = "msg";
    public static final String DATE_FIELD = "date";
    public static final String LAST_UPDATE_FIELD = "updated";
    
    /* CC_FIELDS_COL colection fields */
    public static final String MST_FIELD = "mst";
    public static final String ID_TAG_FIELD = "idTag";
    public static final String URL_TAG_FIELD = "urlTag";
    public static final String CC_TAGS_FIELD = "ccTags";
    
    /* HistoryBrokenLinks collection fields */
    public static final String ELEM_LST_FIELD = "elems";
    
    // Do not include the link into future checks
    public static final String FUTURE_CHECKS = "future_checks";
    // Exclude the links from associated database record
    public static final String LINK_ASSOCIATED_DOC = "link_associated_doc";
    // Exclude the associated database record
    public static final String ASSOCIATED_DOC = "associated_doc";

    public static final String DEF_FIELD = ID_FIELD;
    public static final String DO_NOT_FORCE = "do_not_force";
    
    /* CheckLinks - HTTP error messages */
    public static final String[] ALL_MESS = {"Continue", "Switching Protocols", 
        "Processing", "OK", "Created", "Accepted", "Non-Authoritative Information", 
        "No Content", "Reset Content", "Partial Content", "Multi-Status", 
        "Already Reported", "IM Used", "Multiple Choices", "Moved Permanently", 
        "Found", "See Other", "Not Modified", "Use Proxy", "Switch Proxy", 
        "Temporary Redirect", "Permanent Redirect", "Resume Incomplete", 
        "German Wikipedia", "Bad Request", "Unauthorized", "Payment Required", 
        "Forbidden", "Not Found", "Method Not Allowed", "Not Acceptable", 
        "Proxy Authentication Required", "Request Timeout", "Conflict", "Gone", 
        "Length Required", "Precondition Failed", "Payload Too Large", 
        "URI Too Long", "Unsupported Media Type", "Range Not Satisfiable", 
        "Expectation Failed", "I'm a teapot", "Authentication Timeout", 
        "Method Failure", "Enhance Your Calm", "Misdirected Request", 
        "Unprocessable Entity", "Locked", "Failed Dependency", 
        "Upgrade Required", "Precondition Required", "Too Many Requests", 
        "Request Header Fields Too Large", "Login Timeout", "No Response", 
        "Retry With", "Blocked by Windows Parental Controls", 
        "Unavailable For Legal Reasons", "Redirect", "Request Header Too Large", 
        "Cert Error", "No Cert", "HTTP to HTTPS", "Token expired/invalid", 
        "Client Closed Request", "Token required", "Internal Server Error", 
        "Not Implemented", "Bad Gateway", "Service Unavailable", 
        "Gateway Timeout", "HTTP Version Not Supported", 
        "Variant Also Negotiates", "Insufficient Storage", "Loop Detected", 
        "Bandwidth Limit Exceeded", "Not Extended", 
        "Network Authentication Required", "Unknown Error", 
        "Origin Connection Time-out", "Network read timeout error", 
        "Network connect timeout error", };

    /* CheckLinks - HTTP error codes */
    public static final Integer[] ALL_CODES = { 100, 101, 102, 200, 201, 202, 203,
        204, 205, 206, 207, 208, 226, 300, 301, 302, 303, 304, 305, 306, 307, 308, 
        400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 
        414, 415, 416, 417, 418, 419, 420, 421, 422, 423, 424, 425, 426, 428,
        429, 431, 440, 444, 449, 450, 451, 494,495, 496, 497, 498, 499, 
        500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 520, 521,
        522, 598, 599, 1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 
        1009, 1010, 1011, 1012, 1013, 1100};
    
    public static final String[] DEFAULT_ALLOWED_MESS = ALL_MESS;           
    /*(public static final String[] DEFAULT_ALLOWED_MESS = {"MALFORMED_URL", 
                      "Not found", "UNKNOWN_HOST_EXCEPTION", "Not Acceptable" };*/
    
    public static final Integer[] DEFAULT_ALLOWED_CODES = ALL_CODES;
    
    private static final long MILISECONDS_IN_A_DAY = 60 * 60 * 24 * 1000;
    
    public static void createLinks(final String outCheckFile,
                                   final String mstName) throws BrumaException,
                                                                IOException {
        createLinks(outCheckFile, DEFAULT_FILE_ENCODING, mstName,
            DEFAULT_MST_ENCODING, DEFAULT_HOST, DEFAULT_PORT, null, null,
            DEFAULT_ALLOWED_CODES);
    }

    public static void createLinks(final String outCheckFile,
                                   final String outEncoding,
                                   final String mstName,
                                   final String mstEncoding,
                                   final String host) throws BrumaException,
                                                             IOException {
        createLinks(outCheckFile, outEncoding, mstName, mstEncoding, host,
                                  DEFAULT_PORT, null, null, 
                                  DEFAULT_ALLOWED_CODES);
    }

    public static int createLinks(final String outCheckFile,
                                  final String outEncoding,
                                  final String mstName,
                                  final String mstEncoding,
                                  final String host,
                                  final int port,
                                  final String user,
                                  final String password,
                                  final Integer[] allowedCodes)
                                                          throws BrumaException,
                                                                 IOException {
        if (outCheckFile == null) {
            throw new NullPointerException("outCheckFile");
        }
        if (outEncoding == null) {
            throw new NullPointerException("outEncoding");
        }
        if (mstName == null) {
            throw new NullPointerException("mstName");
        }
        if (mstEncoding == null) {
            throw new NullPointerException("mstEncoding");
        }
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port <= 0");
        }
        if (allowedCodes == null) {
            throw new NullPointerException("allowedCodes");
        }
        
        final Master mst = MasterFactory.getInstance(mstName)
                                        .setEncoding(mstEncoding).open();
        final String mName = new File(mst.getMasterName()).getName();
        final BufferedReader in = new BufferedReader(new InputStreamReader(
                              new FileInputStream(outCheckFile), outEncoding));
        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(SOCIAL_CHECK_DB);
        // map -> mfn ->  url,occ
        final Map<Integer,Map<String,Integer>> occMap = 
                                    new HashMap<Integer,Map<String,Integer>>();
        final boolean checkPassword = false;
        if (checkPassword) {
            final boolean auth = db.authenticate(user, password.toCharArray());
            if (!auth) {
                throw new IllegalArgumentException("invalid user/password");
            }
        }

        final DBCollection coll = db.getCollection(BROKEN_LINKS_COL);
        final DBCollection ccColl = db.getCollection(CC_FIELDS_COL);
        final DBCollection hColl = db.getCollection(HISTORY_COL);
        
        if (ccColl.findOne() == null) {
            if (!createCcFieldsCollection(ccColl)) {
                throw new IOException("CC fields collection creation failed");
            }
        }
        final int idTag = getIsisIdField(mName, ccColl);
        final int urlTag = getIsisUrlFields(mName, ccColl);
        if (urlTag <= 0) {
            throw new IOException("Missing Isis url fields");
        }
        final List<Integer> tags = getIsisCcFields(mName, ccColl);
        final Set<Integer> allowedCodeSet = new HashSet<Integer>(
                                                Arrays.asList(allowedCodes));
        final Map<String,Integer> idMap = getIdMfn(mst, idTag);
        int tell = 0;
        int tot = 0;
        
        coll.dropIndexes();

        System.out.println("Saving documents ...");
        while (true) {
            final String line = in.readLine();
            if (line == null) {
                break;
            }
            final String lineT = line.trim();
            if (!lineT.isEmpty()) {                
                final String[] split = lineT.split(" *\\| *"); //master|id|url|err_code
                if (split.length != 4) {
                    throw new IOException("Wrong line format: " + line);
                }
                if (allowedCodeSet.contains(Integer.parseInt(split[3].trim()))) {
                    final Integer id = idMap.get(split[1]); 
                    if (id == null) {
                        throw new IOException("id[" + split[1] + "] not found");
                    }
                    final String url_e = 
                              EncDecUrl.encodeUrl(split[2], outEncoding, false);
                    
                    saveRecord(mName, id, url_e, split[3], urlTag, tags, mst, 
                               coll, hColl, occMap);
                    tot++;
                }
                if (++tell % 5000 == 0) {
                    System.out.println("++" + tell);
                }
            }
        }

        System.out.println("\nFixing urls that do not start with http:// ... ");
        MongoOperations.fixMissingHttp(coll, hColl, true);
                
        System.out.print("\nCreating an index if it does not exist. " +
                                                "Indexing all documents ... ");
        createIndex(coll, hColl);       
        System.out.println(" OK");
        
        in.close();
        mst.close();
        
        return tot;
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
        doc.put(ID_TAG_FIELD, 2);
        doc.put(URL_TAG_FIELD, 8);
        doc.put(CC_TAGS_FIELD, lst);

        final WriteResult ret = coll.save(doc, WriteConcern.ACKNOWLEDGED);

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
    
    private static int getIsisIdField(final String mstName,
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

        return doc.getInt(ID_TAG_FIELD);
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

    private static Map<String,Integer> getIdMfn(final Master mst, 
                                                final int idTag) 
                                                         throws BrumaException,
                                                                IOException {
        assert mst != null;
        assert idTag > 0;
        
        System.out.println("Parsing ids ...");
        
        int cur = 0;
        
        final Map<String,Integer> map = new HashMap<String,Integer>();
        for (Record rec : mst) {
            final int mfn = rec.getMfn();
            
            if (rec.isActive()) {
                final Field idFld = rec.getField(idTag, 1);
                if (idFld == null) {
                    throw new IOException("idTag[" + idTag + "] not found in " +
                                                        " record[" + mfn + "]");
                }
                map.put(idFld.getContent(), mfn);
            }
            if (++cur % 100000 == 0) {
                System.out.println("*" + cur);
            }
        }
        System.out.println();
        
        return map;
    }
    
    private static void createIndex(final DBCollection coll,
                                    final DBCollection hcoll) {
        assert coll != null;
        assert hcoll != null;

        final BasicDBObject flds = new BasicDBObject();
        flds.append(CENTER_FIELD, 1);
        flds.append(BROKEN_URL_FIELD, 1); //Btree::insert: key too large to index
        flds.append(MST_FIELD, 1);
        coll.ensureIndex(flds);
        
        final BasicDBObject hflds = new BasicDBObject();
        hflds.append(FUTURE_CHECKS, 1);
        hflds.append(LINK_ASSOCIATED_DOC, 1);
        hflds.append(ASSOCIATED_DOC, 1);
        hcoll.ensureIndex(hflds);
    }
    
    private static boolean saveRecord(final String mstName,
                                      final int id,
                                      final String url,
                                      final String err,
                                      final int urlTag,
                                      final List<Integer> ccsFlds,                                      
                                      final Master mst,
                                      final DBCollection coll,
                                      final DBCollection hcoll,
                                  final Map<Integer,Map<String,Integer>> occMap)
                                                         throws BrumaException,
                                                                IOException {
        assert mstName != null;
        assert id > 0;
        assert url != null;
        assert urlTag > 0;
        assert err != null;
        assert ccsFlds != null;
        assert mst != null;
        assert coll != null;
        assert hcoll != null;
        assert occMap != null;

        final Record rec = mst.getRecord(id);
        if (!rec.isActive()) {
            //throw new BrumaException("not active record mfn=" + id);
            System.err.println("WARNING: record[" + id + "] is not active. "
                                                              + "Ignoring it!");
            return false;
        }
 
        final List<Field> urls = rec.getFieldList(urlTag);        
        final Date now = new Date();
        final Date date;
        
        Map<String,Integer> fldMap = occMap.get(id);
        if (fldMap == null) {
            fldMap = new HashMap<String,Integer>();
            occMap.put(id, fldMap);
        }
        
        final int occ = nextOcc(url, urls, fldMap);
        if (occ == -1) {                
            System.err.println("url[" + url + "] not found. mfn=" + id);
            //throw new IOException("url[" + url + "] not found. mfn=" + id);                
            return false;
        }

        final BasicDBObject query = new BasicDBObject(ID_FIELD, id + "_" + occ);
        final boolean ret;
        if (fixedRecently(hcoll, query, now, 60) || 
                                                 shouldIgnore(hcoll, id, occ)) {            
            ret = false;
        } else {        
            final BasicDBObject obj = (BasicDBObject) coll.findOne(query);
            final boolean newDoc;            
            if (obj == null) {
                newDoc = true;
                date = now;
            } else {
                newDoc = false;
                date = obj.getDate(DATE_FIELD);
                
                final WriteResult wr = coll.remove(obj, 
                                                   WriteConcern.ACKNOWLEDGED);
                if (!wr.getCachedLastError().ok()) {
                    //TODO
                }                
            }
            final String url_d = EncDecUrl.decodeUrl(url);
            final String url_d_l = (url_d.length() >= 900) 
                                      ? url_d.substring(0, 900) + "..." : url_d;
            final String url_l = (url.length() > 900)
                                        ? url.substring(0, 900) + "..." : url;
            final BasicDBObject doc = new BasicDBObject();        
            doc.put(DATE_FIELD, date);
            if (!newDoc) {
                doc.put(LAST_UPDATE_FIELD, now);
            }
            doc.put(MST_FIELD, mstName);
            doc.put(ID_FIELD, id + "_" + occ);
            doc.put(BROKEN_URL_FIELD, url_l);
            doc.put(PRETTY_BROKEN_URL_FIELD, url_d_l);
            doc.put(MSG_FIELD, err);
            doc.put(CENTER_FIELD, getCCS(rec, ccsFlds));        

            final WriteResult wres = coll.save(doc, WriteConcern.ACKNOWLEDGED);
            ret = wres.getCachedLastError().ok();
        }
        return ret;
    }

    private static boolean fixedRecently(final DBCollection hcoll,
                                         final BasicDBObject query,
                                         final Date now,
                                         final int days) {
        assert hcoll != null;
        assert query != null;
        assert now != null;
        assert days >= 0;
        
        final boolean ret;
        final BasicDBObject obj = (BasicDBObject) hcoll.findOne(query);            
        if (obj == null) {
            ret = false;
        } else {
            final Date date = obj.getDate(LAST_UPDATE_FIELD);
            if (date == null) {
                ret = false;
            } else {
                ret = (now.getTime() - date.getTime() <= 
                                                 (days * MILISECONDS_IN_A_DAY));
            }
        }
        return ret;
    }
    
    private static boolean shouldIgnore(final DBCollection hcoll,
                                        final int id,
                                        final int occ) {
        assert hcoll != null;
        assert id > 0;
        assert occ >= 0;
        
        final BasicDBList or = new BasicDBList();
        or.add(new BasicDBObject(FUTURE_CHECKS, 
                                           new BasicDBObject("$exists", true)));
        or.add(new BasicDBObject(LINK_ASSOCIATED_DOC, 
                                           new BasicDBObject("$exists", true)));
        or.add(new BasicDBObject(ASSOCIATED_DOC, 
                                           new BasicDBObject("$exists", true)));
        
        final BasicDBObject query = new BasicDBObject(ID_FIELD, id + "_" + occ)
                                                             .append("$or", or);       
        return hcoll.findOne(query) != null;
    }
    
    private static int nextOcc(final String url,
                               final List<Field> urls,
                               final Map<String,Integer> fldMap) {
        assert url != null;
        assert urls != null;
        assert fldMap != null;
        
        int ret;      // possible not used occurrence
        
        try {
            final String url_D = EncDecUrl.decodeUrl(url.trim());

            Integer curOcc = fldMap.get(url_D); // bits indicating used occs
            if (curOcc == null) {
                curOcc = 0;
                fldMap.put(url_D, curOcc);
            }

            int val = 1;      // current check bit position        
            boolean found = false;

            ret = 1;      // possible not used occurrence
            outter : for (Field fld : urls) {
                for (Subfield sub : fld.getSubfields()) {
                    final String sfldUrl_D = 
                                   EncDecUrl.decodeUrl(sub.getContent().trim());
                    if (url_D.equals(sfldUrl_D)) {
                        if ((curOcc & val) == 0) { // found not used occurrence
                            curOcc |= val;
                            fldMap.put(url_D, curOcc);
                            found = true;
                            break outter;
                        }
                    }    
                }
                if (val > Integer.MAX_VALUE / 2) { // all positions already used
                    ret = -1;
                    break;
                }
                val *= 2; // go to the next bit position
                ret++;
            }      
            if (!found) {
                ret = -1;
            }
        } catch(Exception ex) {
            ret = -1;
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
                final WriteResult wr = coll.remove(obj, 
                                                   WriteConcern.ACKNOWLEDGED);
                ret = ret && wr.getCachedLastError().ok();
            }
        }
        return ret;
    }
    
    private static void usage() {
        System.err.println("usage: BrokenLinks <outFile> <mstName> <host>"
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
            } else if (args[idx].startsWith("-outFileEncoding=")) {
                fileEncod = args[idx].substring(17);
            } else if (args[idx].startsWith("-outMstEncoding=")) {
                mstEncod = args[idx].substring(16);
            } else if (args[idx].startsWith("-user=")) {
                user = args[idx].substring(6);
            } else if (args[idx].startsWith("-password=")) {
                pswd = args[idx].substring(10);
            } else {
                usage();
            }
        }

        System.out.println("outFileEncoding=" + fileEncod);
        System.out.println("outMstEncoding=" + mstEncod);        
        System.out.println();
        
        final int tot = createLinks(args[0], fileEncod, args[1], mstEncod, 
                                    args[2], port, user, pswd, 
                                    DEFAULT_ALLOWED_CODES);
        
        System.out.println();
        System.out.println("importedDocuments=" + tot);
        System.out.println();
    }
}
