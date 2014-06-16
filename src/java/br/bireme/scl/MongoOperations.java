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

import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.DATE_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import static br.bireme.scl.BrokenLinks.ELEM_LST_FIELD;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.LAST_UPDATE_FIELD;
import static br.bireme.scl.BrokenLinks.MSG_FIELD;

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
    public static final String MST_FIELD = "mst";

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
        return getCenterUrls(coll, centerIds, filter, 1, Integer.MAX_VALUE, 
                                                                         true);
    }
    
    /**
     * Obtem uma lista com objetos IdUrl obtidos da base de dados MongoDb
     * @param coll coleção onde estão as urls
     * @param centerIds filtro dos centros colaboradores desejados. Nunca é nulo
     * @param filter se não nulo filtra as urls com um cc específico
     * @param from indice inicial da lista a ser recuperado. Começa de 1.
     * @param count numero de elementos a serem devolvidos
     * @param ascendingOrder se retorna por ordem de data ascendente ou descendente
     * @return lista de objetos IdUrl
     */
    public static List<IdUrl> getCenterUrls(final DBCollection coll,
                                            final Set<String> centerIds,
                                            final String filter,
                                            final int from,
                                            final int count,
                                            final boolean ascendingOrder) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (centerIds == null) {
            throw new NullPointerException("centerIds");
        }
        if (centerIds.isEmpty()) {
            throw new IllegalArgumentException("empty centerIds");
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
        final BasicDBObject sort = new BasicDBObject(DATE_FIELD, 
                                                       ascendingOrder ? 1 : -1);

        if (filter == null) {
            final BasicDBList cclst = new BasicDBList();

            for (String centerId : centerIds) {
                cclst.add(centerId);
            }
            BasicDBObject in = new BasicDBObject("$in", cclst);
            final BasicDBObject query = new BasicDBObject(CENTER_FIELD, in);
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
                final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD),
                                           (String)doc.get(BROKEN_URL_FIELD),
                                           ccs,
                                    format.format((Date)(doc.get(DATE_FIELD))),
                                           (String)doc.get(MST_FIELD));
                lst.add(iu);                 
            }
            cursor.close();
        } else {
            final BasicDBObject query = new BasicDBObject(CENTER_FIELD, filter);
            final DBCursor cursor = coll.find(query).sort(sort).skip(from - 1)
                                                                  .limit(count);
            
            while (cursor.hasNext()) {
                final DBObject doc = cursor.next();
                final Set<String> ccs = new TreeSet<String>();
                ccs.add(filter);
                final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD),
                                           (String)doc.get(BROKEN_URL_FIELD),
                                           ccs,
                                      format.format((Date)doc.get(DATE_FIELD)),
                                           (String)doc.get(MST_FIELD));
                lst.add(iu);
                
            }
            cursor.close();
        }
        return lst;
    }
    
    public static List<IdUrl> getDocMaster(final DBCollection coll,
                                           final String docMast,
                                           final int from,
                                           final int count) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (docMast == null) {
            throw new NullPointerException("docMast");
        }
        if (from < 1) {
            throw new IllegalArgumentException("from[" + from + "] < 1");
        }
        if (count < 1) {
            throw new IllegalArgumentException("count[" + count + "] < 1");
        }
        
        final List<IdUrl> lst = new ArrayList<IdUrl>();
        final SimpleDateFormat format = 
                                    new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        
        final BasicDBObject query = new BasicDBObject("mst", docMast);
        final DBCursor cursor = coll.find(query).skip(from - 1).limit(count);
        
        while (cursor.hasNext()) {
            final DBObject doc = cursor.next();
            final BasicDBList ccsLst = (BasicDBList)doc.get(CENTER_FIELD);
            final Set<String> ccs = new TreeSet<String>();

            for (Object cc : ccsLst) {
                ccs.add((String)cc);
            }
            final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD),
                                       (String)doc.get(BROKEN_URL_FIELD),
                                       ccs,
                                  format.format((Date)doc.get(DATE_FIELD)),
                                       (String)doc.get(MST_FIELD));
            lst.add(iu);

        }
        cursor.close();
        return lst;
    }
    
    public static List<IdUrl> getDocId(final DBCollection coll,
                                       final String docId) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (docId == null) {
            throw new NullPointerException("docId");
        }
        final List<IdUrl> lst = new ArrayList<IdUrl>();
        final SimpleDateFormat format = 
                                    new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        final Pattern pat = Pattern.compile(docId.trim() + "_\\d+");
        final BasicDBObject query = new BasicDBObject("_id", pat);
        final DBCursor cursor = coll.find(query);
        
        while (cursor.hasNext()) {
            final DBObject doc = cursor.next();
            final BasicDBList ccsLst = (BasicDBList)doc.get(CENTER_FIELD);
            final Set<String> ccs = new TreeSet<String>();

            for (Object cc : ccsLst) {
                ccs.add((String)cc);
            }
            final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD),
                                       (String)doc.get(BROKEN_URL_FIELD),
                                       ccs,
                                  format.format((Date)doc.get(DATE_FIELD)),
                                       (String)doc.get(MST_FIELD));
            lst.add(iu);
        }
        cursor.close();
        return lst;
    }
    
    public static List<IdUrl> getDocUrl(final DBCollection coll,
                                        final String docUrl) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (docUrl == null) {
            throw new NullPointerException("docUrl");
        }
        final List<IdUrl> lst = new ArrayList<IdUrl>();
        final SimpleDateFormat format = 
                                    new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        
        final BasicDBObject query = new BasicDBObject("burl", docUrl);
        final DBCursor cursor = coll.find(query);
        
        while (cursor.hasNext()) {
            final DBObject doc = cursor.next();
            final BasicDBList ccsLst = (BasicDBList)doc.get(CENTER_FIELD);
            final Set<String> ccs = new TreeSet<String>();

            for (Object cc : ccsLst) {
                ccs.add((String)cc);
            }
            final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD),
                                       (String)doc.get(BROKEN_URL_FIELD),
                                       ccs,
                                  format.format((Date)doc.get(DATE_FIELD)),
                                       (String)doc.get(MST_FIELD));
            lst.add(iu);

        }
        cursor.close();
        return lst;
    }
                                            
    public static int getDocMasterNum(final DBCollection coll,
                                      final String docMast) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (docMast == null) {
            throw new NullPointerException("docMast");
        }
        int num;
        final BasicDBObject query = new BasicDBObject("mst", docMast);
        final DBCursor cursor = coll.find(query);
        
        num = cursor.size();
        cursor.close();
        
        return num;
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
                set.add(new IdUrl(iu.id, iu.url, iu.ccs, iu.since, iu.mst));
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
        final BasicDBObject doc = (BasicDBObject)coll.findOne(query);

        if (doc == null) {
            throw new IOException("document not found id[" + docId + "]");
        }
        
        final BasicDBList lsthdoc;
        BasicDBObject hdoc = (BasicDBObject)hcoll.findOne(query);
        if (hdoc == null) {
            hdoc = new BasicDBObject();
            hdoc.append(ID_FIELD, docId);
            hdoc.append(MST_FIELD, (String)doc.get(MST_FIELD));
            hdoc.append(DATE_FIELD, (Date)doc.get(DATE_FIELD));
            lsthdoc = new BasicDBList();
            hdoc.append(ELEM_LST_FIELD, lsthdoc);
        } else {
            lsthdoc = (BasicDBList)hdoc.get(ELEM_LST_FIELD);
        }
        
        final BasicDBObject hcurdoc = new BasicDBObject();
        hcurdoc.append(BROKEN_URL_FIELD, (String)doc.get(BROKEN_URL_FIELD))
               .append(FIXED_URL_FIELD, fixedUrl)
               .append(MSG_FIELD, (String)doc.get(MSG_FIELD))
               .append(CENTER_FIELD, (BasicDBList)doc.get(CENTER_FIELD))               
               .append(AUTO_FIX_FIELD, automatic)
               .append(EXPORTED_FIELD, false)
               .append(LAST_UPDATE_FIELD, new Date())
               .append(USER_FIELD, user);
                              
        lsthdoc.add(0, hcurdoc);
        
        final boolean ret1 = coll.remove(doc, WriteConcern.SAFE)
                                                           .getLastError().ok();
        final boolean ret2 = hcoll.save(hdoc).getLastError().ok();

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
        final BasicDBObject hdoc = (BasicDBObject)hcoll.findOne(query);

        if (hdoc == null) {
            throw new IOException("document not found id[" + docId + "]");
        }
        final BasicDBList lst = (BasicDBList)hdoc.get(ELEM_LST_FIELD);
        final BasicDBObject hcurdoc = (BasicDBObject)lst.remove(0);
        if (hcurdoc == null) {
            throw new IOException("document last element found. Id[" + docId 
                                                                         + "]");
        }
        final BasicDBObject doc = new BasicDBObject();        
        doc.put(DATE_FIELD, (Date)hdoc.get(DATE_FIELD));
        doc.put(LAST_UPDATE_FIELD, (Date)hcurdoc.get(LAST_UPDATE_FIELD));
        doc.put(MST_FIELD, (String)hdoc.get(MST_FIELD));
        doc.put(ID_FIELD, docId);
        doc.put(BROKEN_URL_FIELD, (String)hcurdoc.get(BROKEN_URL_FIELD));
        doc.put(MSG_FIELD, (String)hcurdoc.get(MSG_FIELD));
        doc.put(CENTER_FIELD, (BasicDBList)hcurdoc.get(CENTER_FIELD));    
        
        final boolean ret1 = coll.save(doc).getLastError().ok();
        final boolean ret2;
        
        if (lst.isEmpty()) {
            ret2 = hcoll.remove(query, WriteConcern.SAFE).getLastError().ok();
        } else {
            ret2 = hcoll.save(hdoc, WriteConcern.SAFE).getLastError().ok();
        }       

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
        final String[] patterns = Tools.getPatterns(brokenUrl, fixedUrl);
        
        if ((brokenUrl.equals(fixedUrl)) || (patterns[0].equals("^"))) {
            if (!CheckUrl.isBroken(CheckUrl.check(fixedUrl))) {
                final Set<IdUrl> docs = 
                        getDocsWith(coll, centerIds, filter, 
                                                  Tools.escapeChars(brokenUrl));
                for (IdUrl iu : docs) {
                    if (iu.url.equals(brokenUrl)) {
                        IdUrl iu2 = new IdUrl(iu.id, fixedUrl, iu.ccs, iu.since, 
                                                                        iu.mst); 
                        ret.add(iu2);
                        if (!updateDocument(coll, hcoll, iu2.id, iu2.url, user,
                                                                       false)) {
                          throw new IOException("could not update document id="
                                                                   + iu2.id);
                        }
                        break;
                    }
                }
            }
        } else {
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
                        throw new IOException(
                                       "could not update document id=" + iu.id);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Filtra um conjunto com id de centros colaboradores deixando apenas os que
     * aparecem nos documentos da colacao
     * @param coll colecao onde serao procurados os cc
     * @param centerIds conjunto inicial com os ccs
     * @return conjunto final com ccs que aparecem nos documentos da colecao
     */
    public static Set<String> filterCenterIds(final DBCollection coll,
                                              final Set<String> centerIds) {
        if (coll == null) {
            throw new NullPointerException("coll");
        } 
        if (centerIds == null) {
            throw new NullPointerException("centerIds");
        }
        final Set<String> set = new HashSet<String>();
        
        for (String cid : centerIds) {
            final BasicDBObject query = new BasicDBObject(CENTER_FIELD, cid);
            final BasicDBObject doc = (BasicDBObject)coll.findOne(query);

            if (doc != null) {
                set.add(cid);
            }
        }
        return set;
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
