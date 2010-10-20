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
 * GemPathEntry
 */
public class GemPathEntry {
    public static long NOT_PERSISTENT = -1L;
    private long _id;
    private long _source;
    private long _target;
    private Path _path;

    public long getSource() {
        return _source;
    }

    public long getTarget() {
        return _target;
    }

    public GemPathEntry(long id, long source, long target) {
        _id = id;
        _source = source;
        _target = target;
    }

    public GemPathEntry(long id, long source, long target, InputStream pathStream) throws ClassNotFoundException,
            IOException {
        _id = id;
        _source = source;
        _target = target;
        if (pathStream != null)
            _path = new Path((String)decode(pathStream));
    }

    public long getOpposite(long id) {
        if (id == _target) return _source;
        else if (id == _source) return _target;
        else throw new RuntimeException();
    }

    public GemPathEntry(long id, long source, long target, Path path) {
        _id = id;
        _source = source;
        _target = target;
        _path = path;
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

    public Path getPath() {
        return _path;
    }

    public int getPathLength() {
        if (_path == null) return 0;
        else return _path.size();
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
}
