package blink;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GemPathRepository {
    private ArrayList<GemEntry> _gems = new ArrayList<GemEntry>();
    private ArrayList<GemPathEntry> _list = new ArrayList<GemPathEntry>();

    public GemPathRepository() throws ClassNotFoundException, IOException, SQLException {
        _gems.addAll(App.getRepositorio().getGems());
        _list.addAll(App.getRepositorio().getGemPaths());
    }

    private GemEntry _lastGemEntryAdded;
    private GemPathEntry _lastGemPathEntryAdded;
    public GemEntry getLastGemEntryAdded() {
        return _lastGemEntryAdded;
    }
    public GemPathEntry getLastGemPathEntryAdded() {
        return _lastGemPathEntryAdded;
    }

    /**
     * Add path if source gem exist on database
     * and they are not connected yet. (target
     * gem may not exist, in that case it is added)
     */
    public boolean addPathIfItDoesNotExist(Gem source, Gem target,
                                           int targetTSClassSize,
                                           boolean tsRepresentant,
                                           Path path) throws IOException,
            SQLException, ClassNotFoundException {

        // null
        _lastGemEntryAdded = null;
        _lastGemPathEntryAdded = null;

        //
        GemEntry sourceEntry = null;
        GemEntry targetEntry = null;
        for (GemEntry ge: _gems) {
            Gem g = ge.getGem();
            if (source.equals(g)) {
                sourceEntry = ge;
            }
            if (target.equals(g)) {
                targetEntry = ge;
            }
        }
        if (sourceEntry == null) {
            throw new RuntimeException("Source must be an existing gem");
        }
        if (targetEntry == null) {
            targetEntry = new GemEntry(target.getCurrentLabelling(),targetTSClassSize,tsRepresentant);
            App.getRepositorio().insertGems(targetEntry);
            _gems.add(targetEntry);
        }

        // target entry was this one.
        _lastGemEntryAdded = targetEntry;

        // search
        boolean insert = true;
        for (GemPathEntry gpe : _list) {
            if (sourceEntry.getId() == gpe.getSource() &&
                targetEntry.getId() == gpe.getTarget()) {
                insert = false;
                break;
            }
        }

        if (insert) {
            GemPathEntry newGPE = new GemPathEntry(
                    GemPathEntry.NOT_PERSISTENT,
                    sourceEntry.getId(),
                    targetEntry.getId(),
                    path);
            PrintWriter pw = new PrintWriter(new FileOutputStream("c:/workspace/blink/log/gempath.log",true));
            pw.println(String.format("Add path from gem %6d to gem %6d with length %3d",sourceEntry.getId(), targetEntry.getId(), path.size()));
            pw.close();
            App.getRepositorio().insertGemPathEntries(newGPE);

            // target entry was this one.
            _lastGemPathEntryAdded = newGPE;

            _list.add(newGPE);
        }

        return insert;

    }

    /**
     * Add path if both gems exists on database
     * and are not connected yet.
     */
    public boolean addPathIfBothGemsExists(Gem source, Gem target, Path path) throws IOException,
            SQLException, ClassNotFoundException {

        // null
        _lastGemPathEntryAdded = null;

        //
        GemEntry sourceEntry = null;
        GemEntry targetEntry = null;
        for (GemEntry ge: _gems) {
            Gem g = ge.getGem();
            if (source.equals(g)) {
                sourceEntry = ge;
            }
            if (target.equals(g)) {
                targetEntry = ge;
            }
            if (source != null && target != null)
                break;
        }
        if (sourceEntry == null) {
            return false;
        }
        if (targetEntry == null) {
            return false;
        }

        // search
        boolean insert = true;
        for (GemPathEntry gpe : _list) {
            if (sourceEntry.getId() == gpe.getSource() &&
                targetEntry.getId() == gpe.getTarget()) {
                insert = false;
                break;
            }
        }

        if (insert) {
            GemPathEntry newGPE = new GemPathEntry(
                    GemPathEntry.NOT_PERSISTENT,
                    sourceEntry.getId(),
                    targetEntry.getId(),
                    path);
            PrintWriter pw = new PrintWriter(new FileOutputStream("c:/workspace/blink/log/gempath.log",true));
            pw.println(String.format("Add path from gem %6d to gem %6d with length %3d",sourceEntry.getId(), targetEntry.getId(), path.size()));
            pw.close();
            App.getRepositorio().insertGemPathEntries(newGPE);

            // target entry was this one.
            _lastGemPathEntryAdded = newGPE;

            _list.add(newGPE);
        }

        return insert;

    }








}
