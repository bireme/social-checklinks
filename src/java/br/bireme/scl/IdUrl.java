/*=========================================================================

    Copyright © 2013 BIREME/PAHO/WHO

    This file is part of SocialCheckLinks.

    SocialCheckLinks is free software: you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 2.1 of
    the License, or (at your option) any later version.

    SocialCheckLinks is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with SocialCheckLinks. If not, see
    <http://www.gnu.org/licenses/>.

=========================================================================*/

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
    public final String url;        // main url
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
