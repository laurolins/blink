package blink;

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
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * ClassHGNormQI
 */
public class ClassHGNormQI {
    private ArrayList<Long> _qis;
    private String _hg;
    private int _numElements;
    public ClassHGNormQI(long qi, String hg, int numElements) {
        _qis = new ArrayList<Long>();
        _qis.add(qi);
        _hg = hg;
        _numElements = numElements;
    }

    public void addQI(long qi, int numElements) {
        if (!_qis.contains(qi)){
            _qis.add(qi);
            _numElements += numElements;
        }
    }

    public String get_hg() {
        return _hg;
    }

    public int get_numElements() {
        return _numElements;
    }

    public long get_qi(int index) {
        return _qis.get(index);
    }

    public long getNumberOfQIs() {
        return _qis.size();
    }

    public HashSet<Long> getMinIdsWithFewestGems() throws SQLException {
        HashMap<Long,Integer> map = new HashMap<Long,Integer>();
        this.load();
        for (BlinkEntry be: this.getBlinks()) {
            if (be.getMinGem() != -1 && be.getMinGem() != 0) {
                Integer i = map.get(be.getMinGem());
                if (i == null) {
                    i = 0;
                }
                map.put(be.getMinGem(),i+1);
            }
        }

        HashSet<Long> result  = new HashSet<Long>();
        int min=0;
        int i=0;
        for (Long key: map.keySet()) {
            if (i == 0) {
                result.add(key);
                min = map.get(key);
            }
            else if (map.get(key) < min) {
                result.clear();
                result.add(key);
                min = map.get(key);
            }
            else if (map.get(key) == min) {
                result.clear();
                result.add(key);
                min = map.get(key);
            }
            i++;
        }
        return result;
    }


    public String getStringOfQIs() {
        String result ="";
        for (int i=0;i<_qis.size();i++) {
            if (i > 0)
                result+=" ";
            result+=_qis.get(i);
        }
        return result;
    }

    public HashSet<Long> getMinGemIDs() throws SQLException {
        this.load();
        HashSet<Long> R = new HashSet<Long>();
        for (BlinkEntry be: this.getBlinks()) {
            R.add(be.getMinGem());
        }
        return R;
    }

    // gambiarra local
    private static HashMap<Long,GemEntry> _gemEntries;
    static {
        try {
            ArrayList<GemEntry> list = App.getRepositorio().getGems();
            _gemEntries = new HashMap<Long,GemEntry>();
            for (GemEntry e: list) {
                _gemEntries.put(e.getId(),e);
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getMinGemCodes() {
        String result = "";
        ArrayList<Long> S = new ArrayList<Long>();
        for (BlinkEntry be: _blinks)
            if (S.indexOf(be.getMinGem()) == -1)
                S.add(be.getMinGem());
        Collections.sort(S);
        boolean first = true;
        for (long l: S) {
            if (!first)
                result+=", ";
            result+=l+(l != -1 ? "m"+_gemEntries.get(l).getTSClassSize() : "");
            result+=(l != -1 ? "v"+_gemEntries.get(l).getNumVertices() : "");
            first = false;
        }
        return result;
    }

    public boolean is_loaded() {
        return _loaded;
    }

    boolean _loaded = false;
    private ArrayList<BlinkEntry> _blinks;
    public void load() throws SQLException {
        if (!_loaded) {
            _blinks = App.getRepositorio().getBlinksByHGQIs(App.MAX_EDGES, _hg, _qis);
            _loaded = true;
        }
    }

    public ArrayList<BlinkEntry> getBlinks() {
        return _blinks;
    }

    public BlinkEntry getMaxCodeMinEdgesBlinkEntry() throws SQLException {
        BlinkEntry maxCodeMinEdges = null;
        this.load();
        for (BlinkEntry be: _blinks) {
            if (maxCodeMinEdges == null) maxCodeMinEdges = be;
            else if (maxCodeMinEdges.get_numEdges() > be.get_numEdges() ||
                     (
                             maxCodeMinEdges.get_numEdges() == be.get_numEdges() &&
                             maxCodeMinEdges.getBlink().compareTo(be.getBlink()) < 0
                     )) {
                maxCodeMinEdges = be;
            }
        }
        return maxCodeMinEdges;
    }

    private Boolean _monochromatic = null;
    public int isMonochromatic() {
        if (is_loaded() && _monochromatic == null) {
            _monochromatic = new Boolean(false);
            for (BlinkEntry be: _blinks) {
                if (new GBlink(be).isMonochromatic()) {
                    _monochromatic = new Boolean(true);
                    break;
                }
            }
        }
        if (_monochromatic != null) {
            if (_monochromatic)
                return 1;
            else
                return 0;
        }
        else return -1;
    }
}
