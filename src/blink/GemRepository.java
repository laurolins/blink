package blink;

import java.util.ArrayList;
import java.util.HashMap;

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
public class GemRepository {
    private HashMap<Long,ArrayList<GemEntry>> _map;
    private ArrayList<GemEntry> _newEntries;

    public GemRepository(ArrayList<GemEntry> list) {
        _map = new HashMap<Long,ArrayList<GemEntry>>();
        _newEntries = new ArrayList<GemEntry>();
        for (GemEntry ge: list) {
            this.add(ge);
        }
    }

    private void add(GemEntry ge) {
        ArrayList<GemEntry> list = this.getListOfHashCode(ge.getGemHashCode());
        for (GemEntry x: list) {
            if (x.equals(ge))
                return;
        }
        list.add(ge);
    }

    public GemEntry getExistingGemEntryOrCreateNew(GemPackedLabelling l, int tsClassSize, boolean representant) {
        ArrayList<GemEntry> list = this.getListOfHashCode(l.getGemHashCode());
        for (GemEntry x: list) {
            if (x.equals(l))
                return x;
        }

        // did not find any gem
        GemEntry result = new GemEntry(l,tsClassSize,representant);
        list.add(result);
        _newEntries.add(result);

        return result;
    }

    public GemEntry getExistingGemEntryOrCreateNew(GemPackedLabelling l) {
        ArrayList<GemEntry> list = this.getListOfHashCode(l.getGemHashCode());
        for (GemEntry x: list) {
            if (x.equals(l))
                return x;
        }
        return null;
    }

    public boolean contains(GemEntry ge) {
        ArrayList<GemEntry> list = this.getListOfHashCode(ge.getGemHashCode());
        for (GemEntry x: list) {
            if (x.equals(ge))
                return true;
        }
        return false;
    }

    public ArrayList<GemEntry> getListOfHashCode(long hc) {
        ArrayList<GemEntry> result = _map.get(hc);
        if (result == null) {
            result = new ArrayList<GemEntry>();
            _map.put(hc,result);
        }
        return result;
    }

    public ArrayList<GemEntry> getNewEntriesLists() {
        return _newEntries;
    }

    public void clearNewEntriesList() {
        _newEntries.clear();
    }
}
