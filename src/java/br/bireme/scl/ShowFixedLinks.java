/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.scl;

import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import static br.bireme.scl.BrokenLinks.ELEM_LST_FIELD;
import static br.bireme.scl.BrokenLinks.FIXED_URL_FIELD;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.LAST_UPDATE_FIELD;
import static br.bireme.scl.BrokenLinks.PRETTY_BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import static br.bireme.scl.MongoOperations.EXPORTED_FIELD;
import static br.bireme.scl.MongoOperations.MST_FIELD;
import static br.bireme.scl.MongoOperations.USER_FIELD;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Heitor Barbieri
 * 20140716
 */
public class ShowFixedLinks {
    final DBCollection coll;

    public ShowFixedLinks(final String host,
                          final String database,
                          final String collection) throws UnknownHostException {
        this(host, DEFAULT_PORT, null, null, database, collection);

    }

    public ShowFixedLinks(final String host,
                          final int port,
                          final String user,
                          final String password,
                          final String database,
                          final String collection) throws UnknownHostException {
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port=" + port);
        }
        if (database == null) {
            throw new NullPointerException("database");
        }
        if (collection == null) {
            throw new NullPointerException("collection");
        }

        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(database);
        if (user != null) {
            final boolean auth = db.authenticate(user, password.toCharArray());
            if (!auth) {
                throw new IllegalArgumentException("invalid user/password");
            }
        }
        coll = db.getCollection(collection);
    }

    public List<Element> showExportedLinks(final List<String> ccs,
                                           final String fromDate)
                                                         throws ParseException {
        /*if (ccs == null) {
            throw new NullPointerException("ccs");
        }*/
        final List<Element> lst = new ArrayList<Element>();
        final SimpleDateFormat simple = new SimpleDateFormat("yyyyMMdd");
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        final Date date = (fromDate == null) ? new Date(0)
                                             : simple.parse(fromDate);
        final String updated = ELEM_LST_FIELD + ".0." + LAST_UPDATE_FIELD;
        final BasicDBObject qdate = new BasicDBObject("$gte", date);
        final BasicDBObject query = new BasicDBObject(updated, qdate);
        final BasicDBObject sort = new BasicDBObject(updated, -1);
        final DBCursor cursor = coll.find(query).sort(sort);

        while (cursor.hasNext()) {
            final BasicDBObject doc = (BasicDBObject)cursor.next();
            final BasicDBList elems = (BasicDBList)doc.get(ELEM_LST_FIELD);
            final BasicDBObject upd = (BasicDBObject)elems.get(0);
            final BasicDBList ccLst = (BasicDBList)upd.get(CENTER_FIELD);
            final List<String> ccs2 = new ArrayList<String>();

            for (Object cc : ccLst) {
                ccs2.add((String)cc);
            }

            if (ccs == null) {
                final String id = doc.getString(ID_FIELD);
                final Element elem = new Element(
                                     id.substring(0, id.indexOf('_')),
                                     upd.getString(BROKEN_URL_FIELD),
                                     upd.getString(PRETTY_BROKEN_URL_FIELD),
                                     upd.getString(FIXED_URL_FIELD),
                                     doc.getString(MST_FIELD),
                                     sdf.format(upd.getDate(LAST_UPDATE_FIELD)),
                                     upd.getString(USER_FIELD),
                                     ccs2,
                                     upd.getBoolean(EXPORTED_FIELD));
                lst.add(elem);
            } else {
                for (String cc : ccs) {
                    if (ccLst.contains(cc)) {
//System.out.println("cc=" + cc + " id=" + doc.getString(ID_FIELD));
                        final String id = doc.getString(ID_FIELD);
                        final Element elem = new Element(
                                     id.substring(0, id.indexOf('_')),
                                     upd.getString(BROKEN_URL_FIELD),
                                     upd.getString(PRETTY_BROKEN_URL_FIELD),
                                     upd.getString(FIXED_URL_FIELD),
                                     doc.getString(MST_FIELD),
                                     sdf.format(upd.getDate(LAST_UPDATE_FIELD)),
                                     upd.getString(USER_FIELD),
                                     ccs2,
                                     upd.getBoolean(EXPORTED_FIELD));
                        lst.add(elem);
                        break;
                    }
                }
            }
        }
        cursor.close();

        System.out.println("size=" + lst.size() + "\n");

        return lst;
    }

    static String element2CSV(final Element elem) {
        assert elem != null;

        return elem.getDate() + "\t" + elem.getUser() + "\t" + elem.getDbase() +
           "\t" + elem.getId() + "\t" + elem.getBurl() + "\t" + elem.getFurl() +
           "\t" + elem.getCcs().get(0) + "\t" + elem.isExported() + "\n";
    }

    private static void usage() {
        System.err.println("usage: ShowFixedLinks <host> [-port=<port>]" +
                "\n\t\t     [-user=<user> -password=<pswd>]" +
                "\n\t\t     [-database=<dbase>] [-collection=<col>]" +
                "\n\t\t     [-fromDate=<YYYYMMDD>]" +
                "\n\t\t     [-ccs=<cc>,<cc>,...,<cc>]" +
                "\n\t\t     [-outFile=<outputFile>]" +
                "\n\t\t     [--csv_format]");
        System.exit(1);
    }

    public static void main(final String[] args) throws UnknownHostException,
                                                        IOException,
                                                        ParseException {
        if (args.length < 1) {
            usage();
        }

        String host = args[0];
        int port = DEFAULT_PORT;
        String user = null;
        String password = null;
        String database = SOCIAL_CHECK_DB;
        String collection = HISTORY_COL;
        String fromDate = "20140101";
        List<String> ccs = null;
        String outFile = null;
        boolean csvFormat = false;

        final String csvHeader = "Date\tUser\tDbase\tId\tBrokenUrl\tFixedUrl\t"+
                                                                "CC\tExported";

        for (int idx = 1; idx < args.length; idx++) {
            if (args[idx].startsWith("-port=")) {
                port = Integer.parseInt(args[idx].substring(6));
            } else if (args[idx].startsWith("-user=")) {
                user = args[idx].substring(6);
            } else if (args[idx].startsWith("-password=")) {
                password = args[idx].substring(10);
            } else if (args[idx].startsWith("-database=")) {
                database = args[idx].substring(10);
            } else if (args[idx].startsWith("-collection=")) {
                collection = args[idx].substring(12);
            } else if (args[idx].startsWith("-fromDate=")) {
                fromDate = args[idx].substring(10);
            } else if (args[idx].startsWith("-ccs=")) {
                final String[] cc = args[idx].substring(5).trim().split(",");
                ccs = Arrays.asList(cc);
            } else if (args[idx].startsWith("-outFile=")) {
                outFile = args[idx].substring(9);
            } else if (args[idx].equals("--csv_format")) {
                csvFormat = true;
            } else {
                usage();
            }
        }

        final ShowFixedLinks elinks = new ShowFixedLinks(host, port, user,
                                                password, database, collection);
        final List<Element> lst = elinks.showExportedLinks(ccs, fromDate);

        if (!lst.isEmpty()) {
            final OutputStream outs = (outFile == null) ? System.out
                                                : new FileOutputStream(outFile);
            final BufferedWriter out = new BufferedWriter(
                                                  new OutputStreamWriter(outs));
            if (csvFormat) {
                out.append(csvHeader);
                out.newLine();
            }

            for (Element elem : lst) {
                if (csvFormat) {
                    out.append(element2CSV(elem));
                } else {
                    out.append(elem.toString());
                    out.newLine();
                    out.newLine();
                }
            }
            out.close();
        }
    }
}
