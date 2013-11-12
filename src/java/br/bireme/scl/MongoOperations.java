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

import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.DATE_FIELD;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.LAST_UPDATE_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
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
    public static final String USER_FIELD = "user";
    public static final String AUTO_FIX_FIELD = "autofix";
    public static final String EXPORTED_FIELD = "exported";

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
                                            final Set<String> centerIds,
                                            final String filter) {
        return getCenterUrls(coll, centerIds, filter, 1, Integer.MAX_VALUE);
    }
    
    /**
     *
     * @param coll
     * @param centerIds
     * @param from indice inicial a ser recuperado. Começa de 1.
     * @return
     */
    public static List<IdUrl> getCenterUrls(final DBCollection coll,
                                            final Set<String> centerIds,
                                            final String filter,
                                            final int from,
                                            final int count) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (centerIds == null) {
            throw new NullPointerException("centerIds");
        }
        if (from < 1) {
            throw new IllegalArgumentException("from[" + from + "] < 1");
        }
        if (count < 1) {
            throw new IllegalArgumentException("count[" + count + "] < 1");
        }
        //final Set<IdUrl> lst = new TreeSet<IdUrl>();
        final List<IdUrl> lst = new ArrayList<IdUrl>();
        final SimpleDateFormat format = 
                                    new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        if (filter == null) {
            final BasicDBList cclst = new BasicDBList();

            for (String centerId : centerIds) {
                cclst.add(centerId);
            }
            BasicDBObject in = new BasicDBObject("$in", cclst);
            final BasicDBObject query = new BasicDBObject(CENTER_FIELD, in);
            final BasicDBObject sort = new BasicDBObject(DATE_FIELD, 1);
            //final DBCursor cursor = coll.find(query).skip(from - 1).limit(count);
            final DBCursor cursor = coll.find(query).sort(sort).skip(from - 1)
                                                                  .limit(count);

            while (cursor.hasNext()) {
                final DBObject doc = cursor.next();
                final BasicDBList ccsLst = (BasicDBList)doc.get(CENTER_FIELD);
                final Set<String> ccs = new TreeSet<String>();

                for (Object cc : ccsLst) {
                    ccs.add((String)cc);
                }
if (doc.get(DATE_FIELD) == null) {
    int x = 0;
}                
                final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD),
                                           (String)doc.get(BROKEN_URL_FIELD),
                                           ccs,
                                    format.format((Date)(doc.get(DATE_FIELD))));
                lst.add(iu);                 
            }
            cursor.close();
        } else {
            final BasicDBObject query = new BasicDBObject(CENTER_FIELD, filter);
            final DBCursor cursor = coll.find(query).skip(from - 1).limit(count);
            
            while (cursor.hasNext()) {
                final DBObject doc = cursor.next();
                final Set<String> ccs = new TreeSet<String>();
                ccs.add(filter);
                final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD),
                                           (String)doc.get(BROKEN_URL_FIELD),
                                           ccs,
                                      format.format((Date)doc.get(DATE_FIELD)));
                lst.add(iu);
                
            }
            cursor.close();
        }
        return lst;
    }
    
    public static int getCentersUrlsNum(final DBCollection coll,
                                        final Set<String> centerIds,
                                        final String filter) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (centerIds == null) {
            throw new NullPointerException("centerIds");
        }
        int num = 0;
        final BasicDBObject query;
        
        if (filter == null) {
            final BasicDBList or = new BasicDBList();

            for (String centerId : centerIds) {
                or.add(new BasicDBObject(CENTER_FIELD, centerId));
            }
            query = new BasicDBObject("$or", or);
        } else {
            query = new BasicDBObject(CENTER_FIELD, filter);
        }
        final DBCursor cursor = coll.find(query);
        num = cursor.size();
        cursor.close();
        
        return num;
    }

    static Set<IdUrl> getDocsWith(final DBCollection coll,
                                  final Set<String> centerIds,
                                  final String filter,
                                  final String pattern) {
        assert coll != null;
        assert centerIds == null;
        assert pattern == null;

        final Matcher mat = Pattern.compile(pattern).matcher("");
        final Set<IdUrl> set = new HashSet<IdUrl>();

        for (IdUrl iu : getCenterUrls(coll, centerIds, filter)) {
            mat.reset(iu.url);
            if (mat.find()) {
                set.add(new IdUrl(iu.id, iu.url, iu.ccs, iu.since));
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
           .append(AUTO_FIX_FIELD, automatic).append(EXPORTED_FIELD, false)
           .append(LAST_UPDATE_FIELD, new Date());

        final boolean ret2 = hcoll.save(doc).getLastError().ok();

        return ret1 && ret2;
    }

    public static boolean undoUpdateDocument(final DBCollection coll,
                                             final DBCollection hcoll,
                                             final String docId)
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

        final BasicDBObject query = new BasicDBObject(ID_FIELD, docId);
        final DBCursor cursor = hcoll.find(query);

        if (!cursor.hasNext()) {
            throw new IOException("document not found id[" + docId + "]");
        }
        final BasicDBObject doc = (BasicDBObject)cursor.next();
        cursor.close();

        final boolean ret1 = hcoll.remove(doc, WriteConcern.SAFE)
                                                           .getLastError().ok();
        doc.removeField(FIXED_URL_FIELD);
        doc.removeField(USER_FIELD);
        doc.removeField(AUTO_FIX_FIELD);

        final boolean ret2 = coll.save(doc).getLastError().ok();

        return ret1 && ret2;
    }

    public static Set<IdUrl> fixRelatedUrls(final DBCollection coll,
                                            final DBCollection hcoll,
                                            final String user,
                                            final Set<String> centerIds,
                                            final String filter,
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
        if (centerIds == null) {
            throw new NullPointerException("centerIds");
        }
        if (brokenUrl == null) {
            throw new NullPointerException("brokenUrl");
        }
        if (fixedUrl == null) {
            throw new NullPointerException("fixedUrl");
        }
        final Set<IdUrl> ret = new HashSet<IdUrl>();

        if (brokenUrl.equals(fixedUrl)) {
            if (!CheckUrl.isBroken(CheckUrl.check(fixedUrl))) {
                final Set<IdUrl> docs = 
                                 getDocsWith(coll, centerIds, filter, fixedUrl);
                for (IdUrl iu : docs) {
                    if (iu.url.equals(fixedUrl)) {
                        ret.add(iu);
                        if (!updateDocument(coll, hcoll, iu.id, iu.url, user,
                                                !fixedUrl.equals(iu.url))) {
                          throw new IOException("could not update document id="
                                                                   + iu.id);
                        }
                        break;
                    }
                }
            }
        } else {
            final String[] patterns = Tools.getPatterns(brokenUrl, fixedUrl);
            final Set<IdUrl> docs = getDocsWith(coll, centerIds, null, 
                                                                   patterns[0]);
            final Set<IdUrl> converted = Tools.getConvertedUrls(docs,
                                                      patterns[0], patterns[1]);
            final Map<String,IdUrl> map = new HashMap<String,IdUrl>();
            for (IdUrl iu : converted) {
                map.put(iu.url, iu);
            }

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
        }
        return ret;
    }

    public static void main(final String[] args) throws UnknownHostException,
                                                                   IOException {
        final MongoClient mongoClient = new MongoClient("ts01vm.bireme.br");
        final DB db = mongoClient.getDB(SOCIAL_CHECK_DB);
        final DBCollection coll = db.getCollection(BROKEN_LINKS_COL);
        final DBCollection hcoll = db.getCollection(HISTORY_COL);

        final Set<String> centers = getCenters(coll);
        for (String center : centers) {
            System.out.println(center);
        }
        System.out.println();

        /*final List<IdUrl> ius = getCenterUrls(coll, "PE1.1");
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
        }*/

    }
}
