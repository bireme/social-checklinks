/*=========================================================================

    Copyright © 2016 BIREME/PAHO/WHO

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
import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.ELEM_LST_FIELD;
import static br.bireme.scl.BrokenLinks.FIXED_URL_FIELD;
import static br.bireme.scl.BrokenLinks.FUTURE_CHECKS;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.LAST_UPDATE_FIELD;
import static br.bireme.scl.BrokenLinks.LINK_ASSOCIATED_DOC;
import static br.bireme.scl.BrokenLinks.MST_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import static br.bireme.scl.MongoOperations.EXPORTED_FIELD;
import static br.bireme.scl.MongoOperations.USER_FIELD;
import bruma.BrumaException;
import bruma.master.Field;
import bruma.master.Master;
import bruma.master.MasterFactory;
import bruma.master.Record;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Date;

/**
 * Update an Isis master file according to the mongo doc fields 
 * FUTURE_CHECKS, LINK_ASSOCIATED_DOC, ASSOCIATED_DOC and output
 * a document report.
 * 
 * @author Heitor Barbieri
 * date: 20160713
 */
public class UpdateIsis {
    static void update(final String mongoHost,
                       final int mongoPort,
                       final String mstPath,
                       final int lnkFldTag,
                       final String reportFile) throws UnknownHostException, 
                                                       BrumaException,
                                                       IOException {
        assert mongoHost != null;
        assert mongoPort > 0;
        assert mstPath != null;
        assert lnkFldTag > 0;
        assert reportFile != null;
                
        final MongoClient mongoClient = new MongoClient(mongoHost, mongoPort);
        final DB db = mongoClient.getDB(SOCIAL_CHECK_DB); 
        final DBCollection coll = db.getCollection(HISTORY_COL);
        final Master mst = MasterFactory.getInstance(mstPath).open(); 
        final String mstName = getMstName(mstPath);
        final StringBuilder futBuilder = processFuture(coll, mstName);
        final StringBuilder lnkBuilder = processField(coll, mst, mstName, lnkFldTag);
        final StringBuilder assBuilder = processRecord(coll, mst, mstName, lnkFldTag);
        
        mst.close();
        
        writeReport(futBuilder, lnkBuilder, assBuilder, reportFile);
    }

    private static String getMstName(final String mstPath) {
        assert mstPath != null;
        
        final String mpath1 = mstPath.trim();
        final String mpath2 = (mpath1.charAt(mpath1.length() - 1) == '/') 
                            ? mpath1.substring(0, mpath1.length() - 1) : mpath1;
        final int pos1 = mpath2.lastIndexOf("/");
        
        return (pos1 == -1) ? mpath2 : mpath2.substring(pos1 + 1);
    }
    
    private static StringBuilder processFuture(final DBCollection coll,
                                               final String mstName) {
        assert coll != null;
        assert mstName != null;
        
        final StringBuilder builder = new StringBuilder();
        final String exported = ELEM_LST_FIELD + ".0." + EXPORTED_FIELD;
        final String future = ELEM_LST_FIELD + ".0." + FUTURE_CHECKS;
        final BasicDBObject exists = new BasicDBObject("$exists", true);
        final BasicDBObject query = new BasicDBObject(MST_FIELD, mstName)
                    .append(exported, false).append(future, exists);
        final DBCursor cursor = coll.find(query);
        
        while (cursor.hasNext()) {
            final BasicDBObject doc = (BasicDBObject)cursor.next();
            builder.append(getInfo(doc));
            builder.append("\n");
        }
        cursor.close();
        
        return builder;
    }
    
    private static StringBuilder processField(final DBCollection coll,
                                              final Master mst,
                                              final String mstName,
                                              final int lnkFldTag) 
                                                         throws BrumaException {
        assert coll != null;
        assert mst != null;
        assert mstName != null;
        assert lnkFldTag > 0;
        
        final StringBuilder builder = new StringBuilder();
        final String exported = ELEM_LST_FIELD + ".0." + EXPORTED_FIELD;
        final String assField = ELEM_LST_FIELD + ".0." + LINK_ASSOCIATED_DOC;
        final BasicDBObject exists = new BasicDBObject("$exists", true);
        final BasicDBObject query = new BasicDBObject(MST_FIELD, mstName)
                           .append(exported, false).append(assField, exists);
        final DBCursor cursor = coll.find(query);
        
        while (cursor.hasNext()) {
            final BasicDBObject doc = (BasicDBObject)cursor.next();
            final String sid = doc.getString(ID_FIELD);
            final String tid = sid.substring(0, sid.indexOf('_'));
            final int id = Integer.parseInt(tid);
            final String socc = sid.substring(sid.indexOf('_') + 1);
            final int occ = Integer.parseInt(socc);
            final BasicDBList list = (BasicDBList)doc.get(ELEM_LST_FIELD);
            final BasicDBObject obj = (BasicDBObject)list.get(0);
            final String burl = obj.getString(BROKEN_URL_FIELD);            
            final Object linkObj = obj.get(LINK_ASSOCIATED_DOC);

            if (linkObj != null) {
                final Record rec = mst.getRecord(id);
                if (rec != null) {
                    final Field fld = rec.getField(lnkFldTag, occ);
                    if ((fld != null) && (fld.getContent().contains(burl))) {
                        rec.deleteField(lnkFldTag, occ);
                        mst.writeRecord(rec);
                        builder.append(getInfo(doc));
                        builder.append("\n");                    
                    }
                }                
            }
        }
        cursor.close();
        
        return builder;
    }
    
    private static StringBuilder processRecord(final DBCollection coll,
                                               final Master mst,
                                               final String mstName,
                                               final int lnkFldTag) 
                                                         throws BrumaException {
        assert coll != null;
        assert mst != null;
        assert mstName != null;
        assert lnkFldTag > 0;

        final StringBuilder builder = new StringBuilder();
        final String exported = ELEM_LST_FIELD + ".0." + EXPORTED_FIELD;
        final String assDoc = ELEM_LST_FIELD + ".0." + ASSOCIATED_DOC;
        final BasicDBObject exists = new BasicDBObject("$exists", true);
        final BasicDBObject query = new BasicDBObject(MST_FIELD, mstName)
                           .append(exported, false).append(assDoc, exists);
        final DBCursor cursor = coll.find(query);
        
        while (cursor.hasNext()) {
            final BasicDBObject doc = (BasicDBObject)cursor.next();
            final String sid = doc.getString(ID_FIELD);
            final String tid = sid.substring(0, sid.indexOf('_'));
            final int id = Integer.parseInt(tid);
            final String socc = sid.substring(sid.indexOf('_') + 1);
            final int occ = Integer.parseInt(socc);
            final BasicDBList list = (BasicDBList)doc.get(ELEM_LST_FIELD);
            final BasicDBObject obj = (BasicDBObject)list.get(0);
            final String burl = obj.getString(BROKEN_URL_FIELD); 
            final Object assObj = obj.get(ASSOCIATED_DOC);

            if (assObj != null) {
                final Record rec = mst.getRecord(id);
                if (rec != null) {
                    final Field fld = rec.getField(lnkFldTag, occ);
                    if ((fld != null) && (fld.getContent().contains(burl))) {
                        mst.deleteRecord(id);
                        builder.append(getInfo(doc));
                        builder.append("\n");
                    }                                                
                }
            }
        }
        cursor.close();
        
        return builder;
    }
    
    private static String getInfo(final BasicDBObject doc) {
        assert doc != null;
                
        final BasicDBList list = (BasicDBList)doc.get(ELEM_LST_FIELD);
        final BasicDBObject obj = (BasicDBObject)list.get(0);
        final String id = doc.getString(ID_FIELD);
        final String url = obj.getString(FIXED_URL_FIELD);
        final String user = obj.getString(USER_FIELD);
        final Date date = obj.getDate(LAST_UPDATE_FIELD);
        final BasicDBList list2 = (BasicDBList)obj.get(CENTER_FIELD);        
        final String center = (String)list2.get(0);
        
        return "id:" + id + " url:" + url + " user:" + user + " center:" 
                + center + " date:" + date;                
    }
        
    private static void writeReport(final StringBuilder futBuilder,
                                    final StringBuilder lnkBuilder,
                                    final StringBuilder assBuilder,
                                    final String reportFile) 
                                                            throws IOException {
        assert futBuilder != null;
        assert lnkBuilder != null;
        assert assBuilder != null;
        assert reportFile != null;
        
        final BufferedWriter writer =  Files.newBufferedWriter(
                                         new File(reportFile).toPath(),
                                         Charset.forName("UTF-8"));                        
        if (futBuilder.length() > 0) { 
            writer.append("--------------------------------------------\n");
            writer.append("Links que deixarão de ser verificados\n");
            writer.append("--------------------------------------------\n");
            writer.append(futBuilder.toString());
            writer.newLine();
            writer.newLine();
        }
        if (lnkBuilder.length() > 0) {
            writer.append("--------------------------------------------\n");
            writer.append("Campos de links que serão apagados\n");
            writer.append("--------------------------------------------\n");        
            writer.append(lnkBuilder.toString());
            writer.newLine();
            writer.newLine();
        }                
        if (assBuilder.length() > 0) {
            writer.append("--------------------------------------------\n");
            writer.append("Registros que serão apagados\n");
            writer.append("--------------------------------------------\n");        
            writer.append(assBuilder.toString());
            writer.newLine();
        }                
        writer.close();
    }
    
    private static void usage() {
        System.err.println("usage: UpdateIsis <mongoHost> <mstPath> <lnkFldTag>" 
                           + " <reportFile> [<mongoPort>]");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws BrumaException, 
                                                        IOException {
        if (args.length < 4) {
            usage();
        }
        final int port = (args.length > 4) ? Integer.parseInt(args[4]) : 27017;
        final int lnkFldTag = Integer.parseInt(args[2]);
        
        update(args[0], port, args[1], lnkFldTag, args[3]);
    }
}
