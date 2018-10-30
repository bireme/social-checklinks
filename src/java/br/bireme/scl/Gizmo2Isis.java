/*=========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.scl;

import bruma.BrumaException;
import bruma.master.Field;
import bruma.master.Master;
import bruma.master.MasterFactory;
import bruma.master.Record;
import bruma.master.Subfield;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
                             final String inputDir,
                             final String outputDir,
                             final String gizEncoding,
                             final String confFile) throws IOException,
                                                            BrumaException {
        if (gizmoFile == null) {
            throw new NullPointerException("gizmoFile");
        }
        if (inputDir == null) {
            throw new NullPointerException("inputDir");
        }
        if (outputDir == null) {
            throw new NullPointerException("outputDir");
        }
        if (gizEncoding == null) {
            throw new NullPointerException("gizEncoding");
        }
        if (inputDir.equals(outputDir)) {
            throw new IOException(
                             "Input and output directories should be diffent");
        }

        final List<GizmoElem> giz = readGizmo(gizmoFile, gizEncoding);
        final Map<String, String> encodingMap = readConfFile(confFile);
        final Map<String, Master> inmap = new HashMap<String, Master>();
        final Map<String, Master> outmap = new HashMap<String, Master>();

        for (GizmoElem elem : giz) {
            changeElem(elem, inputDir, outputDir, encodingMap, inmap, outmap);
        }
        for (Master mst : inmap.values()) {
            mst.close();
        }
        for (Master mst : outmap.values()) {
            mst.close();
        }
    }

    private static void changeElem(final GizmoElem elem,
                                   final String inputDir,
                                   final String outputDir,
                                   final Map<String, String> encodingMap,
                                   final Map<String, Master> inmap,
                                   final Map<String, Master> outmap)
                                                          throws BrumaException,
                                                                   IOException {
        assert elem != null;
        assert inputDir != null;
        assert outputDir != null;
        assert encodingMap != null;
        assert inmap != null;
        assert outmap != null;

        final String from = elem.from;
        String encoding = encodingMap.get(elem.mst);
        encoding = (encoding == null) ? BrokenLinks.DEFAULT_MST_ENCODING
                                      : encoding;

        System.out.println("applying gizmo at " + elem.mst + ": " + elem.mfn);

        Master inmst = inmap.get(elem.mst);
        if (inmst == null) {
            final File in = new File(inputDir, elem.mst);
            inmst = MasterFactory.getInstance(in.getPath())
                                             .setInMemoryXrf(false)
                                             .setEncoding(encoding)
                                             .open();
            inmap.put(elem.mst, inmst);
        }
        Master outmst = outmap.get(elem.mst);
        if (outmst == null) {
            final File out = new File(outputDir, elem.mst);
            outmst = (Master) MasterFactory.getInstance(out.getPath())
                                           .setInMemoryXrf(false)
                                           .asAnotherMaster(inmst)
                                           .forceCreate();
            outmap.put(elem.mst, outmst);
            copyMaster(inmst, outmst);
        }

        final Record rec = inmst.getRecord(elem.mfn);
        final List<Field> flds = new ArrayList<Field>();
        final boolean lilacs = elem.mst.equalsIgnoreCase("lilacs");
        boolean changed = false;

        if (rec.getStatus() == Record.Status.ACTIVE) {
            for (Field fld : rec.getFields()) {
                final int id = fld.getId();

                if ((!lilacs) || (id==8)) { // troca campo
                    final Field other = new Field(id, new ArrayList<Subfield>());

                    for (Subfield sub: fld) {
                        final String content = sub.getContent();
                        final String content_e = EncDecUrl.encodeUrl(content,
                                                               encoding, false);
                        if (from.equals(content_e)) {
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
                System.err.println("ERROR: mfn [" + elem.mfn + "] url [" +
                                      elem.from + "] not found in this record");
            }
            outmst.writeRecord(new Record().setMfn(elem.mfn).addFields(flds));
        } else {
            System.err.println("ERROR: skipping mfn [" + elem.mfn + "] mst [" +
                    elem.mst + "]. Record is not active");
            outmst.writeRecord(rec);
        }
    }

    private static List<GizmoElem> readGizmo(final String gizmoFile,
                                             final String encoding)
                                                            throws IOException {
        assert gizmoFile != null;
        assert encoding != null;

        final List<GizmoElem> lst = new ArrayList<GizmoElem>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(
                                     new FileInputStream(gizmoFile), encoding));
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

    /**
     * Reads the configuration file to get the master file enconding
     * @param confFile configuration file path
     * @return map with (mstName -> mst encoding)
     * @throws IOException
     */
    private static Map<String,String> readConfFile(final String confFile)
                                                            throws IOException {
        final Map<String,String> map = new HashMap<String,String>();

        if (confFile != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(confFile));
                while (true) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    final String[] split = line.trim().split("\\|", 7);
                    if (split.length != 7) {
                        throw new IOException("Wrong line format: " + line);
                    }
                    map.put(split[0], split[6]);
                }
            } finally {
                if (reader != null) {
                     reader.close();
                }
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
                System.out.println("gizmo/copying mfn=" + rec.getMfn());
            }
        }
    }

    private static void usage() {
        System.err.println("usage: Gizmo2Isis <gizmoFile> <inputDir> " +
           "<outputDir>" + "[-gizEncoding=<charset>] [-confFile=<path>]");
        System.exit(1);
    }

    public static void main(final String[] args) throws IOException,
                                                        BrumaException {

        if (args.length < 3) {
            usage();
        }
        String gizEncoding = "UTF-8";
        String confFile = null;

        for (int idx = 3; idx < args.length; idx++) {
            final String arg = args[idx];

            if (arg.startsWith("-gizEncoding=")) {
                gizEncoding = arg.substring(13);
            } else if (arg.startsWith("-confFile=")) {
                confFile = arg.substring(10);
            } else {
                usage();
            }
        }
        apply(args[0], args[1], args[2], gizEncoding, confFile);
    }
}
