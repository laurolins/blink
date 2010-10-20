package blink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * BlinkEntry
 */
public class BlinkEntry implements Comparable {
    public static final long NOT_PERSISTENT = -1L;
    private long _id;
    private String _mapCode;
    private long _colors;
    private int _numEdges;
    private String _hg;
    private long _qi;
    private long _gem;
    private long _mingem;
    private Path _path;
    private String _comment;
    private int _catalogNumber;
    public BlinkEntry(long id, String mapCode, long colors, int numEdges, String hg, long qi, long gem, long mingem, String comment, int catalogNumber) {
        _id = id;
        _mapCode = mapCode;
        _colors = colors;
        _numEdges = numEdges;
        _hg = hg;
        _qi = qi;
        _gem = gem;
        _mingem = mingem;
        _comment = comment;
        _catalogNumber = catalogNumber;
    }

    public int getCatalogNumber() {
        return _catalogNumber;
    }

    public void setCatalogNumber(int catNumb) {
        _catalogNumber = catNumb;
    }

    public long get_colors() {
        return _colors;
    }

    public long getMinGem() {
        return _mingem;
    }

    public void setMinGem(long mingem) {
        _mingem = mingem;
    }

    public String get_hg() {
        return _hg;
    }

    public long get_gem() {
        return _gem;
    }

    public long get_id() {
        return _id;
    }

    public String get_mapCode() {
        return _mapCode;
    }

    public int get_numEdges() {
        return _numEdges;
    }

    public long get_qi() {
        return _qi;
    }

    public String get_comment() {
        return _comment;
    }

    public void set_qi(long _qi) {
        this._qi = _qi;
    }

    public void set_gem(long gem) {
        this._gem = gem;
    }

    public void set_hg(String hg) {
        this._hg = hg;
    }

    public void set_comment(String _comment) {
        this._comment = _comment;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public Path getPath() {
        return _path;
    }

    public void loadPath() throws ClassNotFoundException, IOException, SQLException {
        if (_path == null)
            App.getRepositorio().loadPaths(this);
    }

    public void set_path(InputStream is) throws ClassNotFoundException, IOException {
        if (is != null) {
            _path = new Path((String)decode(is));
        }
        else _path = null;
    }

    public void set_path(Path p) throws ClassNotFoundException, IOException {
        _path = p;
    }

    public GBlink getBlink() {
        GBlink result = new GBlink(new MapWord(_mapCode));
        result.setColor((int)_colors);
        return result;
    }

    public InputStream get_pathBinaryStream() throws ClassNotFoundException, IOException {
        if (_path == null)
            return null;
        else {
            return encode(_path.getSignature());
        }
    }

    private static InputStream encode(Serializable s) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(bos);
        ObjectOutputStream oos = new ObjectOutputStream(gzos);
        oos.writeObject(s);
        oos.flush();
        oos.close();
        byte[] data = bos.toByteArray();
        return new ByteArrayInputStream(data);
    }

    private static Object decode(InputStream is) throws IOException, ClassNotFoundException {
        GZIPInputStream gzis = new GZIPInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(gzis);
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    public int compareTo(Object o) {
        return this.getBlink().compareTo(((BlinkEntry) o).getBlink());
    }
}
