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

import static br.bireme.scl.BrokenLinks.ASSOCIATED_DOC;
import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.DATE_FIELD;
import static br.bireme.scl.BrokenLinks.DO_NOT_FORCE;
import static br.bireme.scl.BrokenLinks.ELEM_LST_FIELD;
import static br.bireme.scl.BrokenLinks.FUTURE_CHECKS;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.LAST_UPDATE_FIELD;
import static br.bireme.scl.BrokenLinks.LINK_ASSOCIATED_DOC;
import static br.bireme.scl.BrokenLinks.MSG_FIELD;
import static br.bireme.scl.BrokenLinks.PRETTY_BROKEN_URL_FIELD;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static class SearchResult {
        public final int size;
        public final List<IdUrl> documents;

        public SearchResult(final int size, 
                            final List<IdUrl> documents) {
            this.size = size;
            this.documents = documents;
        }                
    }
    public static class SearchResult2 {
        public final int size;
        public final int size2;
        public final List<Element> documents;

        public SearchResult2(final int size, 
                             final int size2,
                             final List<Element> documents) {
            this.size = size;
            this.size2 = size;
            this.documents = documents;
        }                
    }
    
    public static final String FIXED_URL_FIELD = "furl";
    public static final String USER_FIELD = "user";
    public static final String AUTO_FIX_FIELD = "autofix";
    public static final String EXPORTED_FIELD = "exported";
    public static final String MST_FIELD = "mst";
    public static final String CODEC = "UTF-8";

    /*public static Set<String> getCenters0(final DBCollection coll) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        final Set<String> set = new TreeSet<String>();
        final DBCursor cursor = coll.find();

        while (cursor.hasNext()) {
            final BasicDBList lst = (BasicDBList)cursor.next().get(CENTER_FIELD);
            set.add((String) lst.get(0));
        }
        cursor.close();

        return set;
    }*/
    
    public static Set<String> getCenters(final DBCollection coll) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        return new TreeSet<String>(coll.distinct(CENTER_FIELD));
    }
    
    public static Set<String> getDatabases(final DBCollection coll) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }        

        return new TreeSet<String>(coll.distinct("mst"));
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
    private static List<IdUrl> getCenterUrls(final DBCollection coll,
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
            final BasicDBObject in = new BasicDBObject("$in", cclst);
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
                                       (String)doc.get(PRETTY_BROKEN_URL_FIELD),
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
                                        (String)doc.get(PRETTY_BROKEN_URL_FIELD),
                                           ccs,
                                      format.format((Date)doc.get(DATE_FIELD)),
                                           (String)doc.get(MST_FIELD));
                lst.add(iu);                
            }
            cursor.close();
        }
        return lst;
    }
    
    public static SearchResult getDocuments(final DBCollection coll,
                                            final String docMast,
                                            final String docId,
                                            final String docUrl,
                                            final Set<String> centerIds,
                                            final boolean decreasingOrder,
                                            final int from,
                                            final int count) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (from < 1) {
            throw new IllegalArgumentException("from[" + from + "] < 1");
        }
        if (count < 1) {
            throw new IllegalArgumentException("count[" + count + "] < 1");
        }        
        final List<IdUrl> lst = new ArrayList<IdUrl>();
        final BasicDBObject query = new BasicDBObject();
        
        if (docMast != null) {
            query.append(MST_FIELD, docMast);
        }
        if (docId != null) {
            final Pattern pat = Pattern.compile("^" + docId.trim() + "_\\d+");
            query.append(ID_FIELD, pat);
        }
        if (docUrl != null) {
            query.append(BROKEN_URL_FIELD, docUrl.trim());
        }
        if (centerIds != null) {
            final BasicDBList cclst = new BasicDBList();
            for (String centerId : centerIds) {
                cclst.add(centerId);
            }
            final BasicDBObject in = new BasicDBObject("$in", cclst);
            query.append(CENTER_FIELD, in);
        }
        
        // Verify if it is the second check.
        query.append(LAST_UPDATE_FIELD, new BasicDBObject("$exists", true));
        
        final BasicDBObject sort = new BasicDBObject(DATE_FIELD, 
                                                      decreasingOrder ? -1 : 1);
        final DBCursor cursor = coll.find(query).sort(sort).skip(from - 1)
                                                                  .limit(count);
        final int size = cursor.count();
        final SimpleDateFormat format =  new SimpleDateFormat("dd-MM-yyyy");
        while (cursor.hasNext()) {
            final DBObject doc = cursor.next();
            final BasicDBList ccsLst = (BasicDBList)doc.get(CENTER_FIELD);
            final Set<String> ccs = new TreeSet<String>();

            for (Object cc : ccsLst) {
                ccs.add((String)cc);
            }
            final IdUrl iu = new IdUrl((String)doc.get(ID_FIELD),
                                       (String)doc.get(PRETTY_BROKEN_URL_FIELD),
                                       ccs,
                                     format.format((Date)(doc.get(DATE_FIELD))),
                                       (String)doc.get(MST_FIELD));
            lst.add(iu);                 
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                }
        cursor.close();
        
        return new SearchResult(size, lst);
    }        
    
    public static SearchResult2 getHistoryDocuments(final DBCollection coll,
                                                    final Element elem,                                             
                                                    final int from,
                                                    final int count) throws 
                                                   IOException, ParseException {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (elem == null) {
            throw new NullPointerException("elem");
        }
        if (from < 1) {
            throw new IllegalArgumentException("from[" + from + "] < 1");
        }
        if (count < 1) {
            throw new IllegalArgumentException("count[" + count + "] < 1");
        }        
        final List<Element> lst = new ArrayList<Element>();
        final BasicDBObject query = new BasicDBObject();
        final String root = ELEM_LST_FIELD + ".0.";
        final String updated = root + LAST_UPDATE_FIELD; 
        
        if (elem.getDbase() != null) {
            query.append(MST_FIELD, elem.getDbase().trim());
        }        
        if (elem.getId() != null) {
            final Pattern pat = Pattern.compile("^" + elem.getId().trim() 
                                                                     + "_\\d+");
            query.append(ID_FIELD, pat);
        }        
        if (elem.getFurl() != null) {
            query.append(root + FIXED_URL_FIELD, elem.getFurl().trim());
        }        
        if (!elem.getCcs().isEmpty()) {
            final BasicDBList cclst = new BasicDBList();
            for (String centerId : elem.getCcs()) {
                cclst.add(centerId.trim());
            }
            final String cc = root + CENTER_FIELD;
            final BasicDBObject in = new BasicDBObject("$in", cclst);        
            query.append(cc, in);
        }        
        if (elem.getDate() != null) {            
            final SimpleDateFormat simple = new SimpleDateFormat("dd-MM-yyyy");
            final Date date = simple.parse(elem.getDate().trim());
            final BasicDBObject qdate = new BasicDBObject("$gte", date);                        
            query.append(updated, qdate);
        }        
        if (elem.getUser() != null) {
            final String user = root + USER_FIELD;
            query.append(user, elem.getUser().trim());
        }
        
        final BasicDBObject sort = new BasicDBObject(updated, -1);
        final DBCursor cursor = coll.find(query).sort(sort).skip(from - 1)
                                                                  .limit(count); 
        final int size = cursor.count();
        final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        
        while (cursor.hasNext()) {
            final BasicDBObject hdoc = (BasicDBObject)cursor.next();                        
            final BasicDBList elst = (BasicDBList)hdoc.get(ELEM_LST_FIELD);
            final BasicDBObject hcurdoc = (BasicDBObject)elst.get(0);
            if (hcurdoc == null) {
                throw new IOException("document last element found.");
            }
            final BasicDBList ccLst = (BasicDBList) hcurdoc.get(CENTER_FIELD);
            final List<String> ccs = Arrays.asList(ccLst.toArray(new String[0]));
            final Element elem2 = new Element(hdoc.getString(ID_FIELD),
                       hcurdoc.getString(BROKEN_URL_FIELD),
                       hcurdoc.getString(PRETTY_BROKEN_URL_FIELD),
                       hcurdoc.getString(FIXED_URL_FIELD),
                       hdoc.getString(MST_FIELD),
                       format.format((Date)(hcurdoc.get(LAST_UPDATE_FIELD))),                                          
                       hcurdoc.getString(USER_FIELD),
                       ccs,
                       hcurdoc.getBoolean(EXPORTED_FIELD));
            lst.add(elem2);
        }
        cursor.close();
        
        return new SearchResult2(size, lst.size(), lst);
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
                                         final String option,
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
        if (option == null) {
            throw new NullPointerException("option");
        }
        if (fixedUrl.length() >= 900) {
            throw new IOException("fixedUrl is too long >= 900. [" + fixedUrl 
                                                                         + "]");
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
        
        final String brokenUrl = doc.getString(BROKEN_URL_FIELD);
        final String brokenUrl_D = EncDecUrl.decodeUrl(brokenUrl);
        final String fixedUrl_E = EncDecUrl.encodeUrl(fixedUrl, CODEC, false);
        //final String fixedUrl_D = EncDecUrl.decodeUrl(fixedUrl);
        final Date date = new Date();
        final BasicDBObject hcurdoc = new BasicDBObject();
        hcurdoc.append(BROKEN_URL_FIELD, brokenUrl)               
               .append(PRETTY_BROKEN_URL_FIELD, brokenUrl_D)
               .append(FIXED_URL_FIELD, fixedUrl_E)
               .append(MSG_FIELD, (String)doc.get(MSG_FIELD))
               .append(CENTER_FIELD, (BasicDBList)doc.get(CENTER_FIELD))               
               .append(AUTO_FIX_FIELD, automatic)
               .append(EXPORTED_FIELD, false)
               .append(LAST_UPDATE_FIELD, date)
               .append(USER_FIELD, user);
        if (option.equals(FUTURE_CHECKS)) {
            hcurdoc.append(FUTURE_CHECKS, date);
        } else if (option.equals(LINK_ASSOCIATED_DOC)) {
            hcurdoc.append(LINK_ASSOCIATED_DOC, date);
        } else if (option.equals(ASSOCIATED_DOC)) {
            hcurdoc.append(ASSOCIATED_DOC, date);
        }
                              
        lsthdoc.add(0, hcurdoc);
        
        final boolean ret1 = coll.remove(doc, WriteConcern.ACKNOWLEDGED)
                                                           .getLastError().ok();
        final boolean ret2 = hcoll.save(hdoc).getLastError().ok();

        return ret1 && ret2;
    }
    
    public static boolean undoUpdateDocument(final DBCollection coll,
                                             final DBCollection hcoll,
                                             final String docId,
                                             final boolean updateBrokenColl)
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
        doc.put(DATE_FIELD, hdoc.get(DATE_FIELD));
        doc.put(LAST_UPDATE_FIELD, hcurdoc.get(LAST_UPDATE_FIELD));
        doc.put(MST_FIELD, hdoc.get(MST_FIELD));
        doc.put(ID_FIELD, docId);
        doc.put(BROKEN_URL_FIELD, hcurdoc.get(BROKEN_URL_FIELD));
        doc.put(PRETTY_BROKEN_URL_FIELD, hcurdoc.get(PRETTY_BROKEN_URL_FIELD));
        doc.put(MSG_FIELD, hcurdoc.get(MSG_FIELD));
        doc.put(CENTER_FIELD, hcurdoc.get(CENTER_FIELD));    
        
        final boolean ret1 = updateBrokenColl 
                                    ? coll.save(doc).getLastError().ok() : true;
        final boolean ret2;
        
        if (lst.isEmpty()) {
            ret2 = hcoll.remove(query, WriteConcern.ACKNOWLEDGED).getLastError()
                                                                          .ok();
        } else {
            ret2 = hcoll.save(hdoc, WriteConcern.ACKNOWLEDGED).getLastError()
                                                                          .ok();
        }       

        return ret1 && ret2;
    }
    
    public static boolean undoUpdateDocument2(final DBCollection coll,
                                              final DBCollection hcoll,
                                              final String fromDate)
                                                           throws IOException, 
                                                                ParseException {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (hcoll == null) {
            throw new NullPointerException("hcoll");
        }
        final SimpleDateFormat simple = new SimpleDateFormat("yyyyMMdd");
        final Date date = (fromDate == null) ? new Date(0) 
                                             : simple.parse(fromDate);
        final String updated = ELEM_LST_FIELD + ".0." + LAST_UPDATE_FIELD;
        final BasicDBObject qdate = new BasicDBObject("$gte", date);
        final BasicDBObject query = new BasicDBObject(updated, qdate);
        final BasicDBObject sort = new BasicDBObject(updated, -1);  
        final DBCursor cursor = coll.find(query).sort(sort);
        
        boolean ret = true;
                
        while (cursor.hasNext()) {
            final BasicDBObject hdoc = (BasicDBObject)cursor.next();                        
            final BasicDBList lst = (BasicDBList)hdoc.get(ELEM_LST_FIELD);
            final BasicDBObject hcurdoc = (BasicDBObject)lst.remove(0);
            if (hcurdoc == null) {
                throw new IOException("document last element found.");
            }
            final BasicDBObject doc = new BasicDBObject();        
            doc.put(DATE_FIELD, hdoc.get(DATE_FIELD));
            doc.put(LAST_UPDATE_FIELD, hcurdoc.get(LAST_UPDATE_FIELD));
            doc.put(MST_FIELD, hdoc.get(MST_FIELD));
            doc.put(ID_FIELD, hdoc.get(ID_FIELD));
            doc.put(BROKEN_URL_FIELD, hcurdoc.get(BROKEN_URL_FIELD));
            doc.put(PRETTY_BROKEN_URL_FIELD, hcurdoc.get(PRETTY_BROKEN_URL_FIELD));
            doc.put(MSG_FIELD, hcurdoc.get(MSG_FIELD));
            doc.put(CENTER_FIELD, hcurdoc.get(CENTER_FIELD));    

            final boolean ret1 = coll.save(doc).getLastError().ok();
            final boolean ret2;

            if (lst.isEmpty()) {
                ret2 = hcoll.remove(query, WriteConcern.ACKNOWLEDGED)
                                                          .getLastError().ok();
            } else {
                ret2 = hcoll.save(hdoc, WriteConcern.ACKNOWLEDGED)
                                                          .getLastError().ok();
            } 
            final boolean auxret = (ret1 && ret2);
            if (!auxret) {
                System.err.println("doc[" + hdoc.get(ID_FIELD) + "] write error");
            }
            ret &= auxret;
        }

        return ret;
    }

    public static Set<IdUrl> fixRelatedUrls(final DBCollection coll,
                                            final DBCollection hcoll,
                                            final String user,
                                            final Set<String> centerIds,
                                            final String filter,
                                            final String brokenUrl,
                                            final String fixedUrl,
                                            final String id,
                                            final String option)
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
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (option == null) {
            throw new NullPointerException("option");
        }
        final Set<IdUrl> ret = new HashSet<IdUrl>();        
        final String brokenUrl_D = EncDecUrl.decodeUrl(brokenUrl);
        final String fixedUrl_D = EncDecUrl.decodeUrl(fixedUrl);
        final String fixedUrl_E = EncDecUrl.encodeUrl(fixedUrl, CODEC, false);
        final String[] patterns = Tools.getPatterns(brokenUrl_D, fixedUrl_D);
        final boolean force = !option.equals(DO_NOT_FORCE);
        
        if ((brokenUrl_D.equals(fixedUrl_D)) || (patterns[0].equals("^"))) {
            if (force || !CheckUrl.isBroken(CheckUrl.check(fixedUrl_E))) {
                final Set<IdUrl> docs = 
                        getDocsWith(coll, centerIds, filter, 
                                                Tools.escapeChars(brokenUrl_D));
                for (IdUrl iu : docs) {
                    if (iu.url.equals(brokenUrl_D)) {
                        IdUrl iu2 = new IdUrl(iu.id, fixedUrl, iu.ccs, iu.since, 
                                                                        iu.mst); 
                        ret.add(iu2);
                        if (!updateDocument(coll, hcoll, iu2.id, iu2.url, 
                                                         user, option, false)) {
                            throw new IOException("could not update " + 
                                                   "document id=" + iu2.id);
                        }
                        break;
                    }
                }
            }
        } else {
            final Set<IdUrl> docs = getDocsWith(coll, centerIds, null, 
                                                                   patterns[0]);
            final Set<IdUrl> docs2 = Tools.filterDomains(docs, id);
            final Set<IdUrl> converted = Tools.getConvertedUrls(docs2,
                                                      patterns[0], patterns[1]);
            final Map<String,List<IdUrl>> map = new HashMap<String,List<IdUrl>>();
            for (IdUrl iu : converted) {
                List<IdUrl> liu = map.get(iu.url);
                if (liu == null) {
                    liu = new ArrayList<IdUrl>();
                    map.put(iu.url, liu);
                }
                liu.add(iu);
            }

            final String[] inurls = map.keySet().toArray(new String[0]);
            final String[] inurls_E = new String[inurls.length];
            
            for (int idx = 0; idx < inurls.length; idx++) {
                inurls_E[idx] = EncDecUrl.encodeUrl(inurls[idx], CODEC, false);
            }
            final int[] results = new CheckUrlArray().check(inurls_E);
            final int len = results.length;

            for (int idx = 0; idx < len; idx++) {
                if (!CheckUrl.isBroken(results[idx])) {
                    for (IdUrl iu : map.get(inurls[idx])) {                    
                        ret.add(iu);
                        if (!updateDocument(coll, hcoll, iu.id, iu.url, user,
                                          option, !fixedUrl_D.equals(iu.url))) {
                            throw new IOException(
                                       "could not update document id=" + iu.id);
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    public static void fixMissingHttp(final DBCollection coll,
                                      final DBCollection hcoll,
                                      final boolean showTell)
                                                           throws IOException {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (hcoll == null) {
            throw new NullPointerException("hcoll");
        }
        final String HTTP = "http://";        
        final DBCursor cursor = coll.find();
        int tell = 0;
        
        while (cursor.hasNext()) {
            final DBObject dbo = cursor.next();
            final String url = ((String) dbo.get(BROKEN_URL_FIELD)).trim();

            if (!url.startsWith(HTTP)) {
                final String fixedUrl = HTTP + url;
                            
                if (!Tools.isDomain(fixedUrl)) {
                    if (!CheckUrl.isBroken(CheckUrl.check(fixedUrl))) {                    
                        final String id = (String) dbo.get(ID_FIELD);
                        if (!updateDocument(coll, hcoll, id, fixedUrl, "system",
                                                          DO_NOT_FORCE, true)) {
                            throw new IOException("could not update document id=" 
                                                                          + id);
                        }
                    }
                }
            }
            if (showTell) {
                if (++tell % 500 == 0) {
                    System.out.println("++" + tell);
                }
            }
        }
        cursor.close();
    }

    public static Set<String> filterCenterFields(final DBCollection coll,
                                                 final Set<String> centerIds) {
        if (coll == null) {
            throw new NullPointerException("coll");
        }
        if (centerIds == null) {
            throw new NullPointerException("centerIds");
        }
        final Set<String> ret = new TreeSet<String>();
        
        for (String id : centerIds) {
            final BasicDBObject query = new BasicDBObject(CENTER_FIELD, id);
            final DBObject dbo = coll.findOne(query);
            if (dbo != null) {
                ret.add(id);
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
