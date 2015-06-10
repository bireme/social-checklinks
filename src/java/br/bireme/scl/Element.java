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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Heitor Barbieri
 * date 20140716
 */
public class Element {   
    private final String id;
    private final String burl;
    private final String pburl;
    private final String furl;
    private final String dbase;
    private final String date;
    private final String user;
    private final List<String> ccs;
    private final boolean exported;
    private final StringBuilder  builder;

    public Element(final String id, 
                   final String burl, 
                   final String pburl, 
                   final String furl, 
                   final String dbase, 
                   final String date,
                   final String user,
                   final List<String> ccs,
                   final boolean exported) {
        this.id = id;
        this.burl = burl;
        this.pburl = pburl;
        this.furl = furl;
        this.dbase = dbase;
        this.user = user;
        this.date = date;
        this.ccs = (ccs == null) ? new ArrayList<String>() : ccs;
        this.exported = exported;
        this.builder = new StringBuilder();        
    }

    public String getId() {
        return id;
    }

    public String getBurl() {
        return burl;
    }

    public String getPBurl() {
        return pburl;
    }
    
    public String getFurl() {
        return furl;
    }

    public String getDbase() {
        return dbase;
    }

    public String getDate() {
        return date;
    }
    
    public String getUser() {
        return user;
    }
    
    public List<String> getCcs() {
        return ccs;
    }
    
    public boolean isExported() {
        return exported;
    }
    
    @Override
    public String toString() {
        boolean first = true;
        
        builder.setLength(0);
        builder.append("date=");
        builder.append(date);
        builder.append("\nuser=");
        builder.append(user);
        builder.append("\ndbase=");
        builder.append(dbase);
        builder.append("\nid=");
        builder.append(id);
        builder.append("\nburl=");
        builder.append(burl);
        builder.append("\npburl=");
        builder.append(pburl);
        builder.append("\nfurl=");
        builder.append(furl);
        builder.append("\nccs=");
        for (String cc : ccs) {
            if (first) {
                first = false;
            } else {
                builder.append(",");
            }
            builder.append(cc);
        }
        builder.append("\nexported=");
        builder.append(exported);
        return builder.toString();
    }
}
