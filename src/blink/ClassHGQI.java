package blink;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
public class ClassHGQI {
    private long _qi;
    private String _hg;
    private int _numElements;
    public ClassHGQI(long qi, String hg, int numElements) {
        _qi = qi;
        _hg = hg;
        _numElements = numElements;
    }

    public String get_hg() {
        return _hg;
    }

    public int get_numElements() {
        return _numElements;
    }

    public long get_qi() {
        return _qi;
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
            _blinks = App.getRepositorio().getBlinksByClass(_hg, _qi);
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
