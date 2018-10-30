/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.scl;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Heitor Barbieri
 * date: 20130730
 */
public class IdUrl implements Comparable<IdUrl>, Serializable {
    public final String id;         // LILACS & Mongo
    public final String url;        // main url (decoded)
    public final Set<String> ccs;   // Collaboration Centers
    public final String since;      // document creation date
    public final String mst;        // database from where it comes

    public IdUrl(final String id,
                 final String url,
                 final Set<String> ccs,
                 final String since,
                 final String mst) {
        this.id = id;
        this.url = url;
        this.ccs = ccs;
        this.since = since;
        this.mst = mst;
    }

    @Override
    public int compareTo(IdUrl other) {
        return url.compareTo(other.url);
    }
}
