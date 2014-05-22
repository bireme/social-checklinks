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

import bruma.BrumaException;
import bruma.master.Field;
import bruma.master.Master;
import bruma.master.MasterFactory;
import bruma.master.Record;
import bruma.master.Subfield;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Heitor Barbieri
 * date 20140408
 */
class GizmoElem {        
    final String from;
    final String to;
    final String mst;
    final int mfn;

    GizmoElem(final String from, 
              final String to, 
              final String mst, 
              final String mfn) {
        assert from != null;
        assert to != null;
        assert mst != null;
        assert mfn != null;

        this.from = from;
        this.to = to;
        this.mst = mst;
        this.mfn = Integer.parseInt(mfn);
    }                
}

public class Gizmo2Isis {    
    public static void apply(final String gizmoFile,
                             final String ciparFile,
                             final String outputDir) throws IOException, 
                                                            BrumaException {
        if (gizmoFile == null) {
            throw new NullPointerException("gizmoFile");
        }
        if (ciparFile == null) {
            throw new NullPointerException("ciparFile");
        }
        if (outputDir == null) {
            throw new NullPointerException("outputDir");
        }
        final List<GizmoElem> giz = readGizmo(gizmoFile);
        final Map<String, String> cipar = readCipar(ciparFile);
        final Map<String, Master> inmap = new HashMap<String, Master>();
        final Map<String, Master> outmap = new HashMap<String, Master>();
        
        for (GizmoElem elem : giz) {
            changeElem(elem, cipar, inmap, outmap, outputDir);
        }
        for (Master mst : inmap.values()) {
            mst.close();
        }
        for (Master mst : outmap.values()) {
            mst.close();
        }
    }
    
    private static void changeElem(final GizmoElem elem,
                                   final Map<String, String> cipar,
                                   final Map<String, Master> inmap,
                                   final Map<String, Master> outmap,
                                   final String outDir) throws BrumaException, 
                                                                   IOException {
        assert elem != null;
        assert cipar != null;
        assert inmap != null;
        assert outmap != null;
        assert outDir != null;
        
        final String from = elem.from;
        final String path = cipar.get(elem.mst);
        if ((path == null) || (path.isEmpty())) {
            throw new IllegalArgumentException("master path");
        }
        
        System.out.println(elem.mst + ": " + elem.mfn);
        
        Master inmst = inmap.get(elem.mst);
        if (inmst == null) {
            inmst = MasterFactory.getInstance(path).open();
            inmap.put(elem.mst, inmst);
        }
        Master outmst = outmap.get(elem.mst);
        if (outmst == null) {
            final File file = new File(path);
            final String name = file.getName();
            final File out = new File(outDir, name);
            outmst = (Master) MasterFactory.getInstance(out.getPath())
                                           .setInMemoryXrf(false)
                                           .asAnotherMaster(inmst)
                                           .forceCreate();
//System.out.println("in=" + path + " out=" + out.getPath());
            outmap.put(elem.mst, outmst);
            copyMaster(inmst, outmst);
        }
        
        final Record rec = inmst.getRecord(elem.mfn);
        final List<Field> flds = new ArrayList<Field>();
        final boolean lilacs = elem.mst.equalsIgnoreCase("lilacs");
        boolean changed = false;
        
        if (rec.getStatus() != Record.Status.ACTIVE) {
            throw new BrumaException("mfn[" + elem.mfn + "] is not active");
        }
        for (Field fld : rec.getFields()) {
            final int id = fld.getId();
            
            if ((!lilacs) || (id==8)) { // troca campo
                final Field other = new Field(id, new ArrayList<Subfield>());

                for (Subfield sub: fld) {
                    if (from.equals(sub.getContent())) {
                        other.addSubfield(new Subfield(sub.getId(), elem.to));
                        changed = true; // can not break, repeated fields.
                    } else {
                        other.addSubfield(sub);
                    }
                }
                flds.add(other);
            } else {
                flds.add(fld);
            }
        }
        if (!changed) {
            //throw new IOException("mfn [" + elem.mfn + "] url [" + elem.from 
            //                            + "] not found in this record");
            System.err.println("ERROR: mfn [" + elem.mfn + "] url [" + elem.from 
                                          + "] not found in this record");
        }
        outmst.writeRecord(new Record().setMfn(elem.mfn).addFields(flds));
    }
    
    private static List<GizmoElem> readGizmo(final String gizmoFile)
                                                            throws IOException {
        assert gizmoFile != null;
        
        final List<GizmoElem> lst = new ArrayList<GizmoElem>();
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(gizmoFile));
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                final String[] split = line.trim().split("\\|");
                if (split.length < 4) {
                    throw new IOException("Wrong line format: " + line);
                }
                final GizmoElem elem = new GizmoElem(split[0], split[1],
                                                     split[2], split[3]);                
                lst.add(elem);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }    
        }
        
        return lst;
    }
    
    private static Map<String,String> readCipar(final String ciparFile) 
                                                            throws IOException {
        assert ciparFile != null;
        
        final Map<String,String> map = new HashMap<String,String>();
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(ciparFile));
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                final String[] split = line.trim().split("[\\s+\\,\\;=]", 2);
                if (split.length != 2) {
                    throw new IOException("Wrong line format: " + line);
                }
                map.put(split[0], split[1]);
            }
        } finally {
            if (reader != null) {
                 reader.close();
            }
        }
        
        return map;
    }
    
    private static void copyMaster(final Master inmst, 
                                   final Master outmst) throws BrumaException {
        assert inmst != null;
        assert outmst != null;
        
        int tell = 0;
        
        for (Record rec : inmst) {
            outmst.writeRecord(rec);
            if (++tell % 50000 == 0) {
                System.out.println("copying mfn=" + rec.getMfn());
            }
        }
    }
    
    private static void usage() {
        System.err.println(
                         "usage: Gizmo2Isis <gizmoFile> <ciparFile> <outDir>");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws IOException, 
                                                        BrumaException {

        if (args.length != 3) {
            usage();
        }
        
        apply(args[0], args[1], args[2]);
        
        //apply("Gv8broken.giz", "cipar.par", "other");
    }
}
