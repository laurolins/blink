package blink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public class GemEntry implements Serializable {
    public static final long NOT_PERSISTENT = -1L;
    private long _id;
    private int _handleNumber;
    private int _tsClassSize;
    private int[] _code;
    private int _catalogNumber;
    private int _status;
    private long _minGem;
    private boolean _tsRepresentant;

    public GemEntry(long id, InputStream is, int handleNumber, int tsClassSize, int catalogNumber, int status, long minGem, boolean tsRepresentant) throws ClassNotFoundException, IOException {
        _id = id;
        _code = (int[]) decode(is);
        _handleNumber = handleNumber;
        _tsClassSize = tsClassSize;
        _catalogNumber = catalogNumber;
        _minGem = minGem;
        _status = status;
        _tsRepresentant = tsRepresentant;
    }

    public int getStatus() {
        return _status;
    }

    public boolean isTSRepresentant() {
        return _tsRepresentant;
    }

    public void setStatus(int status) {
        _status = status;
    }

    public void setStatus(GemPrimeStatus status) {
        _status = status.getNumber();
    }

    public void setMinGem(long minGem) {
        _minGem = minGem;
    }

    public GemPrimeStatus getGemPrimeStatus() {
        return GemPrimeStatus.fromNumber(_status);
    }

    public long getMinGem() {
        return _minGem;
    }

    public int getTSClassSize() {
        return _tsClassSize;
    }

    public GemEntry(GemPackedLabelling lbl, int tsClassSize, boolean representant) {
        _id = NOT_PERSISTENT;
        _code = lbl.getCode();
        _handleNumber = lbl.getHandleNumber();
        _tsClassSize = tsClassSize;
        _tsRepresentant = representant;
        _status = 0;
        _minGem = 0;
    }

    public GemEntry(int catalogNumber, GemPackedLabelling lbl) {
        _id = NOT_PERSISTENT;
        _code = lbl.getCode();
        _handleNumber = lbl.getHandleNumber();
        _tsClassSize = 0;
        _tsRepresentant = false;
        _catalogNumber = catalogNumber;
        _status = 0;
        _minGem = 0;
    }

    public int getCatalogNumber() {
        return _catalogNumber;
    }

    public void setCatalogNumber(int cn) {
        _catalogNumber = cn;
    }

    public int getNumVertices() {
        return 2*_code.length/3;
    }

    public long getId() {
        return _id;
    }

    public long getGemHashCode() {
        return GemPackedLabelling.calculateGemHashCode(_code);
    }

    public int getHandleNumber() {
        return _handleNumber;
    }

    public byte[] getCodeAsByteArray() throws IOException {
        return encode(_code);
    }

    private static byte[] encode(Serializable s) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(bos);
        ObjectOutputStream oos = new ObjectOutputStream(gzos);
        oos.writeObject(s);
        oos.flush();
        oos.close();
        return bos.toByteArray();
    }

    private static Object decode(InputStream is) throws IOException, ClassNotFoundException {
        GZIPInputStream gzis = new GZIPInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(gzis);
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public GemPackedLabelling getLabelling() {
        return new GemPackedLabelling(_code,_handleNumber);
    }

    public Gem getGem() {
        return new Gem(this.getLabelling());
    }

    public boolean equals(Object o) {
        if (o instanceof GemEntry) {
            return this.getLabelling().compareTo(((GemEntry) o).getLabelling()) == 0;
        }
        else if (o instanceof GemPackedLabelling) {
            return this.getLabelling().compareTo(((GemPackedLabelling) o)) == 0;
        }
        return false;
    }

}
