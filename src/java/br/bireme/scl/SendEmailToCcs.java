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

import br.bireme.SendEmail;
import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.BROKEN_URL_FIELD;
import static br.bireme.scl.BrokenLinks.CENTER_FIELD;
import static br.bireme.scl.BrokenLinks.DEFAULT_PORT;
import static br.bireme.scl.BrokenLinks.ID_FIELD;
import static br.bireme.scl.BrokenLinks.MST_FIELD;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 *
 * @author Heitor Barbieri
 * date: 20150507
 */
public class SendEmailToCcs {
        
    private class CcProfile {
        final String cc;
        final String name;
        final String email;
        final boolean male;

        public CcProfile(final String cc, 
                         final String name, 
                         final String email, 
                         final boolean male) {
            this.cc = cc;
            this.name = name;
            this.email = email;
            this.male = male;
        }                        
    }
    
    private class EmailFrame {
        final String id;
        final String mst;
        final String url;
        
        EmailFrame(final String id, 
                   final String mst, 
                   final String url) {
            this.id = id;
            this.mst = mst;
            this.url = url;
        }        
    }
    
    public void sendEmails(final String toFile, 
                           final String host, 
                           final int port, 
                           final String user, 
                           final String password, 
                           final String database, 
                           final String collection) throws UnknownHostException {
        if (toFile == null) {
            throw new NullPointerException("toFile");
        }
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port <= 0");
        }
        if (database == null) {
            throw new NullPointerException("database");
        }
        if (collection == null) {
            throw new NullPointerException("collection");
        }
        
        final Map<String,CcProfile> profiles = getProfiles(toFile);
        final MongoClient mongoClient = new MongoClient(host, port);
        final DB db = mongoClient.getDB(database);
        if (user != null) {
            final boolean auth = db.authenticate(user, password.toCharArray());
            if (!auth) {
                throw new IllegalArgumentException("invalid user/password");
            }
        }
        final DBCollection coll = db.getCollection(collection);     
                
        prepareEmails(coll, profiles);                      
    }
    
    private Map<String,CcProfile> getProfiles(String toFile) {
        assert toFile != null;
        
        final Map<String,CcProfile> profiles = new HashMap<String,CcProfile>();
        try {
            final BufferedReader reader = new BufferedReader(
                                                        new FileReader(toFile));
            
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                final String linet = line.trim();
                if (!line.isEmpty()) {
                    final String[] split = linet.split(",");
                    if (split.length != 4) {
                        throw new IOException(
                               "<cc>, <name>,<email>,<gender> format required");
                    }
                    final CcProfile ccp = new CcProfile(
                                                  split[0].trim().toUpperCase(),
                                                  split[1].trim(),
                                                  split[2].trim(),
                                split[3].trim().toUpperCase().charAt(0) == 'M');
                    profiles.put(ccp.cc, ccp);
                }
            }
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(SendEmailToCcs.class.getName()).log(
                                                        Level.SEVERE, null, ex);
        }
        
        return profiles;
    }

    private void prepareEmails(final DBCollection coll, 
                               final Map<String, CcProfile> profiles) {
        assert coll != null;
        assert profiles != null;
        
        final Set<String> ccs = MongoOperations.getCenters(coll);
        final String qry = CENTER_FIELD + ".0";

        for (String cc : ccs) {
            final Set<EmailFrame> eset = new HashSet<EmailFrame>();
            final CcProfile ccp = profiles.get(cc);
            
            if (ccp != null) {
                final BasicDBObject query = new BasicDBObject(qry, cc);
                final DBCursor cursor = coll.find(query);

                while (cursor.hasNext()) {
                    final BasicDBObject doc = (BasicDBObject)cursor.next();            
                    final String id = doc.getString(ID_FIELD);
                    final EmailFrame eframe = new EmailFrame(
                                               id.substring(0, id.indexOf('_')),
                                               doc.getString(MST_FIELD),
                                               doc.getString(BROKEN_URL_FIELD));
                    eset.add(eframe);
                }
                cursor.close();
                sendEmailToCc(ccp, eset);
            }
        }
    }
    
    private void sendEmailToCc(final CcProfile profile,
                               final Set<EmailFrame> frames) {
        assert profile != null;
        assert frames  != null;
        
        final StringBuilder msg = new StringBuilder();
        
        msg.append(profile.male ? "Mr. " : "Ms. ");
        msg.append(profile.name);
        msg.append(",\n\n");
        msg.append("The following ");
        msg.append(frames.size());
        msg.append(" links of your institution [");
        msg.append(profile.cc);
        msg.append("] are probably broken.\n\n");
        msg.append("Please, fix then using the Social Check Links software:\n");
        msg.append("http://socialchecklinks.bireme.org\n\n");
        msg.append("Broken Links:\n\n");
        
        for (EmailFrame frame : frames) {
            msg.append("db: ");
            msg.append(frame.mst);
            msg.append("\nid: ");
            msg.append(frame.id);
            msg.append("\nurl: ");
            msg.append(frame.url);
            msg.append("\n\n");
        }
        
        System.out.println("Sending email about broken links to: " + profile.cc);
        
        try {
            SendEmail.send(profile.email,
                    "no-reply-socialchecklinks@paho.org",
                    "Social Check Links - Broken Links",
                    msg.toString());
        } catch (MessagingException ex) {
            Logger.getLogger(
                    SendEmailToCcs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void usage() {
        System.err.println("usage: SendEmailToCC <to_file> <host>" +
                "\n\t\t     [-port=<port>]" +
                "\n\t\t     [-user=<user> -password=<pswd>]" +
                "\n\t\t     [-database=<dbase>] [-collection=<col>]");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws UnknownHostException {
        if (args.length < 2) {
            usage();
        }
        
        final String toFile = args[0];
        final String host = args[1];
        
        int port = DEFAULT_PORT;
        String user = null;
        String password = null;
        String database = SOCIAL_CHECK_DB;
        String collection = BROKEN_LINKS_COL;
                
        for (int idx = 2; idx < args.length; idx++) {
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
            } else {
                usage();
            }
        }
        
        final SendEmailToCcs sec = new SendEmailToCcs();
        sec.sendEmails(toFile, host, port, user, password, database, collection);
    }        
}
