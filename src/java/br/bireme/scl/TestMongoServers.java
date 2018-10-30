/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.scl;

import static br.bireme.scl.BrokenLinks.BROKEN_LINKS_COL;
import static br.bireme.scl.BrokenLinks.HISTORY_COL;
import static br.bireme.scl.BrokenLinks.SOCIAL_CHECK_DB;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

/**
 *
 * @author Heitor Barbieri
 * date: 20150326
 */
public class TestMongoServers {
    public static void testServer(final String server) {
        try {
            final MongoClient mongoClient = new MongoClient(server);
            final DB db = mongoClient.getDB(SOCIAL_CHECK_DB);
            final DBCollection coll = db.getCollection(BROKEN_LINKS_COL);
            final DBCollection hcoll = db.getCollection(HISTORY_COL);
            final DBCursor cursor = coll.find();
            final DBCursor hcursor = hcoll.find();

            System.out.println("Server: " + server);
            System.out.println("(" + SOCIAL_CHECK_DB +
                    "," + BROKEN_LINKS_COL + ") #docs:" + cursor.count());
            System.out.println("(" + SOCIAL_CHECK_DB +
                    "," + HISTORY_COL + ") #docs:" + hcursor.count() + "\n");

            cursor.close();
            hcursor.close();
        } catch (Exception ex) {
            System.out.println("Status: FAILED!\n");
        }
    }

    public static void main(final String[] args) {
        final String server1 = "ts01vm.bireme.br";
        final String server2 = "hm01vm.bireme.br";
        final String server3 = "mongodb.bireme.br";

        System.out.println("MongoDb Server - testing ...\n");
        testServer(server1);
        testServer(server2);
        testServer(server3);
        System.out.println();
    }
}
