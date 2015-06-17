/*=========================================================================

    Copyright © 2015 BIREME/PAHO/WHO

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

import static br.bireme.scl.BrokenLinks.DEFAULT_HOST;
import static br.bireme.scl.BrokenLinks.DEFAULT_MST_ENCODING;
import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import bruma.BrumaException;
import bruma.master.Field;
import bruma.master.Master;
import bruma.master.MasterFactory;
import bruma.master.Record;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.List;

/**
 *
 * @author Heitor Barbieri
 * date: 20150615
 */
public class JoinTitle {
    public static final String DB_NAME = "PROCESSING";
    public static final String COL_NAME= "TITLE";
    public static final String INDEX_TAG = "indexTag";
    public static final String RETURN_TAG = "retTag";
    
    private final DB db;
        
    public JoinTitle(final String host,
                     final int port) throws UnknownHostException {
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port <= 0");
        }
        
        final MongoClient mongoClient = new MongoClient(host, port);
        
        mongoClient.dropDatabase(DB_NAME);
        db = mongoClient.getDB(DB_NAME);
    }
    
    public void join(final String titlePath,
                     final String titleEncoding,
                     final int indexTag,
                     final int returnTag,
                     final String inMstName,
                     final String inMstEncoding,
                     final int joinTag,
                     final int newTag,
                     final String outMstName) throws UnknownHostException, 
                                                                BrumaException {
        final DBCollection title = getTitle(titlePath, titleEncoding, indexTag, 
                                                                     returnTag);        
        joinTitle(title, inMstName, inMstEncoding, joinTag, newTag, outMstName);
        db.dropDatabase();
    }
    
    private DBCollection getTitle(final String mstName,
                                  final String mstEncoding,
                                  final int indexTag,
                                  final int returnTag) throws 
                                          UnknownHostException, BrumaException {
        assert mstName != null;
        assert indexTag > 0;
        assert returnTag > 0;
        
        final DBCollection coll = db.getCollection(COL_NAME);
        coll.ensureIndex(new BasicDBObject(INDEX_TAG, 1));

        final Master mst = MasterFactory.getInstance(mstName)
                                        .setEncoding(mstEncoding).open();        
        for (Record rec : mst) {
            if (rec.isActive()) {
                final int mfn = rec.getMfn();
                final List<Field> lst1 = rec.getFieldList(indexTag);
                if (!lst1.isEmpty()) {
                    final List<Field> lst2 = rec.getFieldList(returnTag);
                    if (!lst2.isEmpty()) {
                        final BasicDBList dblist1 = new BasicDBList();
                        for (Field fld1 : lst1) {
                            final String str = removeAccents(fld1.getContent())
                                                                 .toUpperCase();
                            dblist1.add(str);
                        }
                        final BasicDBList dblist2 = new BasicDBList();
                        for (Field fld2 : lst2) {
                            dblist2.add(fld2.getContent());
                        }
                        coll.insert(new BasicDBObject(INDEX_TAG, dblist1)
                                                  .append(RETURN_TAG, dblist2)
                                                  .append("mfn", mfn) );
                    }
                }
            }
        }         
        mst.close();
        
        return coll;
    }
    
    private void joinTitle(final DBCollection title,
                           final String inMstName,
                           final String inMstEncoding,
                           final int joinTag,
                           final int newTag,
                           final String outMstName) throws UnknownHostException,
                                                                BrumaException {
        assert title != null;
        assert inMstName != null;
        assert inMstEncoding != null;
        assert joinTag > 0;
        assert newTag > 0;
        assert outMstName != null;
        
        final Master inMst = MasterFactory.getInstance(inMstName)
                                        .setEncoding(inMstEncoding).open();
        final Master outMst = (Master) MasterFactory.getInstance(outMstName)
                                        .asAnotherMaster(inMst).forceCreate();
        
        int cur = 0;
        for (Record rec : inMst) {
            if (++cur % 10000 == 0) {
                System.out.println("++" + cur);
            }
            if (rec.isActive()) {
//System.out.println("out encoding=" + outMstEncoding);                
                for (Field fld : rec.getFieldList(joinTag)) {
                    final String str = removeAccents(fld.getContent())
                                                                 .toUpperCase();
                    final BasicDBObject doc = (BasicDBObject)title.findOne(
                                             new BasicDBObject(INDEX_TAG, str));
                    if (doc == null) {
                        //System.out.println("rec mfn=" + rec.getMfn() + " tag=" 
                        //        + INDEX_TAG + " content=" + fld.getContent());
                    } else {
                        final BasicDBList lst = 
                                              (BasicDBList) doc.get(RETURN_TAG);
                        for (Object obj : lst) {
                            rec.addField(newTag, (String)obj);
                        }
                    }
                }
            }
            outMst.writeRecord(rec);

        }
        inMst.close();
        outMst.close();
    }
    
    private String removeAccents(final String text) {
        return text == null ? null :
            Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
    
    private static void usage() {
        System.err.println("usage: JoinTitle \n\t\t<title mst> " + 
          "\n\t\t<indexTag> \n\t\t<returnTag> \n\t\t<in mst> " +
          "\n\t\t<joinTag> " + "\n\t\t<newTag> \n\t\t<out mst> " +
          "\n\t\t[--titleEncoding=<title encoding>] " + 
          "\n\t\t[--inEncoding=<in encoding>] " +
          "\n\t\t[--mongoHost=<host>] \n\t\t[--mongoPort=<port>]");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws BrumaException, 
                                                          UnknownHostException {
        if (args.length < 7) {
            usage();
        }
        String titleEncoding = DEFAULT_MST_ENCODING;
        String outEncoding = DEFAULT_MST_ENCODING;
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;        
        
        for (int idx = 7; idx < args.length; idx++) {
            if (args[idx].startsWith("--titleEncoding=")) {
                titleEncoding = args[idx].substring(16);
            } else if (args[idx].startsWith("--inEncoding=")) {
                outEncoding = args[idx].substring(13);
            } else if (args[idx].startsWith("--mongoHost=")) {
                host = args[idx].substring(12);
            } else if (args[idx].startsWith("--mongoPort=")) {
                port = Integer.parseInt(args[idx].substring(12));
            } else {
                usage();
            }
        }
        
        final JoinTitle jt = new JoinTitle(host, port);        
        jt.join(args[0], titleEncoding, Integer.parseInt(args[1]), 
                Integer.parseInt(args[2]), args[3], outEncoding,
                Integer.parseInt(args[4]), Integer.parseInt(args[5]),
                args[6]);                
    }    
}
