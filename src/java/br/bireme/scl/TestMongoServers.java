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
