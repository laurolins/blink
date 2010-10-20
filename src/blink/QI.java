package blink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public class QI implements Serializable {
    public static final long NOT_PERSISTENT = -1L;
    private long _id;
    private ArrayList<QIEntry> _entries = new ArrayList<QIEntry>();
    public QI(long id) {
        _id = id;
    }

    public QI(long id, InputStream is) throws ClassNotFoundException, IOException {
        _id = id;
        _entries = (ArrayList<QIEntry>)decode(is);
    }

    public void addEntry(QIEntry qi) {
        int i=0;
        for (;i<_entries.size();i++) {
            QIEntry aux = _entries.get(i);
            if (aux.get_r() < qi.get_r())
                continue;
        }
        _entries.add(i,qi);
    }

    public void addEntry(int r, double real, double imaginary) {
        QIEntry qiEntry = new QIEntry(r,real,imaginary,0,0);
        this.addEntry(qiEntry);
    }

    public int get_rmax() {
        if (_entries.size() > 0)
            return _entries.get(_entries.size() - 1).get_r();
        else return -1;
    }

    public long get_id() {
        return _id;
    }

    public long getHashCode() {
        long sum = 0;
        for (QIEntry qi: _entries) {
            sum += qi.get_realAsLong() + qi.get_imaginaryAsLong();
        }
        return sum;
    }

    public InputStream getEntries() throws IOException {
        return encode(_entries);
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

    public double getReal(int r) {
        return this.getEntryByR(r).get_real();
    }

    public double getImaginary(int r) {
        return this.getEntryByR(r).get_imaginary();
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public void print() {
        this.print(System.out);
    }

    public void print(PrintStream ps) {
        for (QIEntry qi: _entries) {
            qi.print(ps);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (QIEntry qi: _entries) {
            sb.append(qi.toString());
            sb.append("\n");
        }
        sb.append("polar\n");
        for (QIEntry qi: _entries) {
            sb.append(toStringPolar(qi));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toStringPolar(QIEntry qie) {
        double[] d = polarModulusAndAngleInRadians(qie.get_real(),qie.get_imaginary());
        return String.format("%3d %16.6fr%16.6fdeg. %6d %7.1f",qie.get_r(),d[0],d[1]*180/Math.PI,qie.get_states(),qie.get_time()/1000.0);
    }

    public double[] polar(double r, double i) {
        double modulo = Math.sqrt(r*r + i*i);
        double theta = 180*Math.atan(i/r)/Math.PI;
        if (r == 0 && i > 0) theta = 90;
        else if (r == 0 && i < 0) theta = -90;
        modulo =modulo *1e6;
        theta=theta*1e6;
        modulo = Math.round(modulo);
        theta = Math.round(theta);
        modulo = modulo*1e-6;
        theta = theta*1e-6;
        return new double[] { modulo, theta };
    }

    public double[] polarModulusAndAngleInRadians(double r, double i) {
        double modulo = Math.sqrt(r*r + i*i);
        double theta;

        if (Math.abs(modulo) < 1.0e-11) {
            modulo = 0;
            theta = 0;
        }
        else if (Math.abs(i) < 1.0e-11) {
            modulo = Math.abs(r);
            theta = (r < 0 ? Math.PI : 0);
        }
        else if (Math.abs(r) < 1.0e-11) {
            modulo = Math.abs(i);
            theta = (i < 0 ? -Math.PI/2.0 : Math.PI/2.0);
        }
        else {
            theta = Math.atan(Math.abs(i) / Math.abs(r));
            if (i < 0 && r > 0)
                theta = -theta;
            else if (i > 0 && r < 0)
                theta = Math.PI - theta;
            else if (i < 0 && r < 0)
                theta = -Math.PI + theta;
        }
        return new double[] { modulo, theta};
    }


    public boolean isEqual(QI qi) {
        int n = _entries.size();
        if (qi._entries.size() != n)
            return false;
        for (int i=0;i<n;i++) {
            QIEntry qie = _entries.get(i);
            QIEntry auxqie = qi._entries.get(i);
            if (!qie.isEqual(auxqie))
                return false;
        }
        return true;
    }

    public boolean isEqualUpToMaxR(QI qi) {
        int n = Math.min(_entries.size(),qi._entries.size());
        for (int i=0;i<n;i++) {
            QIEntry qie = _entries.get(i);
            QIEntry auxqie = qi._entries.get(i);
            if (!qie.isEqual(auxqie))
                return false;
        }
        return true;
    }


    public boolean isInNeighborhood(int r, double real, double imaginary) {
        if (Math.abs(_entries.get(r-3).get_real() - real)<=1e-7 && Math.abs(_entries.get(r-3).get_imaginary() - imaginary)<=1e-7)
            return true;
        else
            return false;
    }

    public QIEntry getEntryByR(int r) {
        return _entries.get(r-3);
    }

    public boolean allEntriesAreReal() {
        for (QIEntry qie: _entries) {
            if (qie.get_imaginaryAsLong() != 0L)
                return false;
        }
        return true;
    }

    public boolean allEntriesAreInteger() {
        for (QIEntry qie: _entries) {
            if (qie.get_imaginaryAsLong() != 0L || (qie.get_real() - (int)qie.get_real())!=0)
                return false;
        }
        return true;
    }

    /**
     * Normalize QI: make its first imaginary entry positive.
     */
    public QI normalize() {
        QI result = new QI(QI.NOT_PERSISTENT);
        Boolean neg1 = null;
        for (int i=3;i<=this.get_rmax();i++) {
            QIEntry e1 = this.getEntryByR(i);
            double r1 = e1.get_real();
            double i1 = e1.get_imaginary();
            if (neg1 == null && i1 != 0) {
                neg1 = (i1 < 0 ? true : false);
            }
            double i1norm = (neg1 == null ? 0.0 : i1 * (neg1 == true ? -1.0 : 1.0));
            QIEntry qie = new QIEntry(i,r1,i1norm,e1.get_states(),e1.get_time());
            result.addEntry(qie);
        }
        return result;
    }

    /**
     * Normalize QI: make its first imaginary entry positive.
     */
    public String getValuesInString(boolean polar, char separator) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (int i=3;i<=this.get_rmax();i++) {
            if (!first)
                sb.append(separator);
            QIEntry e1 = this.getEntryByR(i);
            double r1 = e1.get_real();
            double i1 = e1.get_imaginary();
            if (polar) {
                double polarValues[] = this.polar(r1,i1);
                r1 = polarValues[0];
                i1 = polarValues[1];
            }
            first = false;
            sb.append(String.format("%.6f%c%.6f",r1,separator,i1));
        }
        return sb.toString();
    }

    /**
     * Normalize the quantum invariant is to make it's first
     * proper complex entry (first with imaginary part <> 0)
     * on the sequence be positive. So if this entry is
     * negative then we take all the conjugates as the
     * normalized entries of the quantum invariant.
     *
     * This method returns true if the normalized entries
     * are equal and r_max are also equal. Otherwise returns
     * false.
     *
     * @param qi2 QI
     * @return boolean
     */
    public boolean compareNormalizedEntries(QI qi2) {

        QI qi1 = this;
        if (qi1.get_rmax() != qi2.get_rmax())
            return false;

        Boolean neg1 = null;
        Boolean neg2 = null;
        for (int i=3;i<=qi1.get_rmax();i++) {
            QIEntry e1 = qi1.getEntryByR(i);
            double r1 = e1.get_real();
            double i1 = e1.get_imaginary();
            QIEntry e2 = qi2.getEntryByR(i);
            double r2 = e2.get_real();
            double i2 = e2.get_imaginary();

            if (neg1 == null && i1 != 0) {
                neg1 = (i1 < 0 ? true : false);
            }
            if (neg2 == null && i2 != 0) {
                neg2 = (i2 < 0 ? true : false);
            }

            i1 = ((neg1 != null && neg1 == true) ? -i1 : i1);
            i2 = ((neg2 != null && neg2 == true) ? -i2 : i2);

            if (Math.abs(r1-r2) > 1e-8 || Math.abs(i1-i2) > 1e-8) {
                return false;
            }
        }
        return true;
    }

    /**
     * Normalize the quantum invariant is to make it's first
     * proper complex entry (first with imaginary part <> 0)
     * on the sequence be positive. So if this entry is
     * negative then we take all the conjugates as the
     * normalized entries of the quantum invariant.
     *
     * This method returns true if the normalized entries
     * are equal and r_max are also equal. Otherwise returns
     * false.
     *
     * @param qi2 QI
     * @return boolean
     */
    public boolean compareNormalizedEntriesUntilMaxR(QI qi2) {
        QI qi1 = this;
        int rmax = Math.min(qi1.get_rmax(),qi2.get_rmax());

        Boolean neg1 = null;
        Boolean neg2 = null;
        for (int i=3;i<=rmax;i++) {
            QIEntry e1 = qi1.getEntryByR(i);
            double r1 = e1.get_real();
            double i1 = e1.get_imaginary();
            QIEntry e2 = qi2.getEntryByR(i);
            double r2 = e2.get_real();
            double i2 = e2.get_imaginary();

            if (neg1 == null && i1 != 0) {
                neg1 = (i1 < 0 ? true : false);
            }
            if (neg2 == null && i2 != 0) {
                neg2 = (i2 < 0 ? true : false);
            }

            i1 = ((neg1 != null && neg1 == true) ? -i1 : i1);
            i2 = ((neg2 != null && neg2 == true) ? -i2 : i2);

            if (Math.abs(r1-r2) > 1e-8 || Math.abs(i1-i2) > 1e-8) {
                return false;
            }
        }
        return true;
    }

    /**
     * All entries are equal?
     */
    public boolean equals(Object o) {
        return this.isEqual((QI) o);
    }

}
