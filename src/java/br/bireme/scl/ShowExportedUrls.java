/*=========================================================================

    Copyright © 2014 BIREME/PAHO/WHO

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

import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import static br.bireme.scl.BrokenLinks.ELEM_LST_FIELD;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.LAST_UPDATE_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import static br.bireme.scl.MongoOperations.EXPORTED_FIELD;
import static br.bireme.scl.MongoOperations.USER_FIELD;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Heitor Barbieri
 * date 20140508
 */
public class ShowExportedUrls {
    public static void show(final String mongodb_host,
                            final Date from,
                            final Date to,
                            final Writer out) throws UnknownHostException, 
                                                     ParseException,
                                                     IOException {
        if (mongodb_host == null) {
            throw new NullPointerException("mongodb_host");
        }        
        if (out == null) {
            throw new NullPointerException("out");
        }
  
        final BufferedWriter writer = new BufferedWriter(out);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("w");
        final Date from2 = (from == null) ? sdf.parse("19700101") : from;
        final Date to2 = (to == null) ? new Date() : to;
        final MongoClient client = new MongoClient(mongodb_host, DEFAULT_PORT);
        final DB fromDb = client.getDB(SOCIAL_CHECK_DB);
        final DBCollection coll = fromDb.getCollection(HISTORY_COL);                
//db.posts.find({"created_on": {"$gte": start, "$lt": end}})        
        final DBCursor cursor = coll.find();
        
        writer.append("ISO DATE;WEEK IN YEAR;DOC ID;EMAIL;CCS\n\n");

        while (cursor.hasNext()) {
            final BasicDBObject doc = (BasicDBObject)cursor.next();
            final BasicDBList elems = (BasicDBList)doc.get(ELEM_LST_FIELD);            
            final BasicDBObject elem = (BasicDBObject) elems.get(0);
            final BasicDBList ccs = (BasicDBList)elem.get(CENTER_FIELD);
            
            if (elem.getBoolean(EXPORTED_FIELD)) {
                final Date updated = elem.getDate(LAST_UPDATE_FIELD);
                final StringBuilder builder = new StringBuilder();
                boolean first = true;
                
                for (Object obj : ccs) {
                    final String cc = (String)obj;
                    
                    if (first) {
                        first = false;
                    } else {
                        builder.append(", ");
                    }
                    builder.append(cc);
                }
                
                if ((updated.compareTo(from2) >= 0) && 
                                                (updated.compareTo(to2) <= 0)) {
                    final String id = doc.getString(ID_FIELD);
                    writer.append(sdf.format(updated));
                    writer.append(";");
                    writer.append(sdf2.format(updated));
                    writer.append(";");
                    writer.append(id.substring(0, id.indexOf('_')));
                    writer.append(";");
                    writer.append(elem.getString(USER_FIELD));
                    writer.append(";");
                    
                    writer.append(builder.toString());
                    writer.append("\n");
                }
            }
        }
        
        writer.flush();
        cursor.close();        
    }
    
    private static void usage() {
        System.err.println("usage: ShowExportedUrls <mongodbhost> <outfile> " + 
                                         "[-from=<yyyymmdd>] [-to=<yyyymmdd>]");
        System.exit(1);
    }
    
    public static void main0(final String[] args) throws IOException, 
                                                        UnknownHostException, 
                                                        ParseException {
        if ((args.length < 2) || (args.length > 4)) {
            usage();
        }
        
        String from = null;
        String to = null;
        
        for (int idx = 2; idx <= args.length; idx++) {
            if (args[idx].startsWith("-from=")) {
                from = args[idx].substring(6);
            } else if (args[idx].startsWith("-to=")) {
                to = args[idx].substring(4);
            } else {
                usage();
            }
        }
        
        final Date fDate = (from == null) ? null : new Date(from);
        final Date tDate = (to == null) ? null : new Date(to);
        Writer out = null;
        
        try {
            out = new FileWriter(args[1]);       
            show(args[0], fDate, tDate, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }           
    
    public static void main(final String[] args) throws IOException, 
                                                        UnknownHostException, 
                                                        ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        final String from = "20140101";
        final String to = null;               
        final Date fDate = sdf.parse(from);;
        final Date tDate = (to == null) ? null : sdf.parse(to);
        final Writer out = new FileWriter("report.csv");       
        
        show("mongodb.bireme.br", fDate, tDate, out);
        out.close();        
    }
}
