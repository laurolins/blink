package blink;

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
public class ClassQI {
    private long _qi;
    private String _hg;
    private int _numElements;
    public ClassQI(long qi, int numElements) {
        _qi = qi;
        _numElements = numElements;
    }

    public int get_numElements() {
        return _numElements;
    }

    public long get_qi() {
        return _qi;
    }

    public boolean is_loaded() {
        return _loaded;
    }

    boolean _loaded = false;
    private ArrayList<BlinkEntry> _blinks;
    public void load() throws SQLException {
        if (!_loaded) {
            _blinks = App.getRepositorio().getBlinksByQI(App.MAX_EDGES,_qi);
            _loaded = true;
        }
    }

    public ArrayList<BlinkEntry> getBlinks() {
        return _blinks;
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
