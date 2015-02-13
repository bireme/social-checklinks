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

import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import static br.bireme.scl.BrokenLinks.ELEM_LST_FIELD;
import static br.bireme.scl.BrokenLinks.FIXED_URL_FIELD;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.LAST_UPDATE_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import static br.bireme.scl.MongoOperations.EXPORTED_FIELD;
import static br.bireme.scl.MongoOperations.MST_FIELD;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Heitor Barbieri
 * date: 20130813
 */
public class Gizmo {
    Collection<Element>loadRecords(final String host,
                                   final int port,
                                   final String user,
                                   final String password) throws IOException {
        assert host != null;
        assert port > 0;

        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(SOCIAL_CHECK_DB);

        if ((user != null) && (!user.trim().isEmpty())) {
            final boolean auth = db.authenticate(user, password.toCharArray());
            if (!auth) {
                throw new IllegalArgumentException("invalid user/password");
            }
        }

        final DBCollection coll = db.getCollection(HISTORY_COL);
        final String fldName = ELEM_LST_FIELD + ".0." + EXPORTED_FIELD;
        final BasicDBObject query = new BasicDBObject(fldName, false);
        final DBCursor cursor = coll.find(query);
        final Collection<Element> col = getNotExportedElements(coll, cursor);

        cursor.close();

        return col;
    }

    Collection<Element> getNotExportedElements(final DBCollection coll,
                                               final DBCursor cursor)
                                                           throws IOException {
        assert coll != null;
        assert cursor != null;

        final Collection<Element> col = new ArrayList<Element>();

        while (cursor.hasNext()) {
            final BasicDBObject obj = (BasicDBObject)cursor.next();
            final String id = obj.getString(ID_FIELD);
            final BasicDBList lst = (BasicDBList)obj.get(ELEM_LST_FIELD);            
            if (lst == null) {
                throw new NullPointerException("Elem list espected");
            }
            final BasicDBObject lelem = (BasicDBObject)lst.get(0);
            if (lelem == null) {
                throw new NullPointerException("Elem element espected");
            }
            if (!lelem.getBoolean(EXPORTED_FIELD)) {
                final Element elem = new Element(id,
                                     lelem.getString(BROKEN_URL_FIELD),
                                     lelem.getString(FIXED_URL_FIELD),
                                     obj.getString(MST_FIELD),
                                     lelem.getDate(LAST_UPDATE_FIELD).toString(),
                                     null,
                                     false);
                col.add(elem);
                
                lelem.put(EXPORTED_FIELD, true);
                final WriteResult res = coll.save(obj, 
                                                     WriteConcern.ACKNOWLEDGED);

                if (!res.getCachedLastError().ok()) {
                    throw new IOException("write doc[" + obj.getString(ID_FIELD)
                                                                  + "] failed");
                }
            }
        }

        return col;
    }

    public void createGizmo(final String host,
                            final int port,
                            final String user,
                            final String password,
                            final String gizFile,
                            final String encoding) throws IOException {
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port[" + port + "] <= 0");
        }
        if (gizFile == null) {
            throw new NullPointerException("gizFile");
        }
        if (encoding == null) {
            throw new NullPointerException("encoding");
        }

        final Collection<Element> elems = loadRecords(host, port, user, password);
        final BufferedWriter out = new BufferedWriter(new OutputStreamWriter
                                    (new FileOutputStream(gizFile), encoding));

        for (Element elem : elems) {
            final String[] split = elem.getId().split("_", 2);
            final String burl = elem.getBurl().trim();
            final String furl = elem.getFurl().trim();
            out.append(burl + "|" + furl + "|" + elem.getDbase() + "|" + split[0] 
                                + "|" + split[1] + "|" + elem.getDate() + "\n");
        }

        out.close();
    }

    private static void usage() {
        System.err.println("usage: Gizmo <mongoHost> <gizFile> "
                 + "\n\t[-port=<mongoPort>] [-user=<userName> -password=<pswd>]"
                 + "\n\t[-encoding=<outEncoding>]");
        System.exit(1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            usage();
        }
        int port = DEFAULT_PORT;
        String encoding = "UTF-8";
        String user = null;
        String password = null;
        int len = args.length;

        if (len > 2) {
            for (int idx = 2; idx <= len-1; idx++) {
                if (args[idx].startsWith("-port=")) {
                    port = Integer.parseInt(args[idx].substring(6));
                } else if (args[idx].startsWith("-user=")) {
                    user = args[idx].substring(6);
                } else if (args[idx].startsWith("-pswd=")) {
                    password = args[idx].substring(6);
                } else if (args[idx].startsWith("-encoding=")) {
                    encoding = args[idx].substring(10);
                }
            }
        }
        final Gizmo giz = new Gizmo();
        giz.createGizmo(args[0], port, user, password, args[1], encoding);

        //giz.createGizmo("ts01vm.bireme.br", DEFAULT_PORT, null, null, "out.giz", "UTF-8");
    }
}