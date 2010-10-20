package blink;

import java.io.PrintStream;
import java.io.Serializable;

class QIEntry implements Serializable {
    int _r;
    long _real;
    long _imaginary;
    int _states;
    long _time;

    public long double2long(double d) {
        return (long) Math.round((d + 1.0e-12) * 1.0e11)/10;
    }

    public double long2double(long l) {
        return (l / 1.0e10);
    }

    public QIEntry(int r, double real, double imaginary, int states, long time) {
        _r = r;
        _real = double2long(real);
        _imaginary = double2long(imaginary);
        _states = states;
        _time = time;
    }

    public double get_imaginary() {
        return long2double(_imaginary);
    }

    public int get_r() {
        return _r;
    }

    public double get_real() {
        return long2double( _real);
    }

    public long get_realAsLong() {
        return _real;
    }

    public long get_imaginaryAsLong() {
        return _imaginary;
    }

    public int get_states() {
        return _states;
    }

    public long get_time() {
        return _time;
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream ps) {
        ps.println(String.format("%3d %16.10f %16.10fi %6d %7.1f",
                           get_r(),get_real(),get_imaginary(),get_states(),get_time()/1000.0));
    }

    public String toString() {
        return String.format("%3d %16.10f %16.10fi %6d %7.1f",
                           get_r(),get_real(),get_imaginary(),get_states(),get_time()/1000.0);
    }

    public boolean isEqual(QIEntry qie) {
        if (qie.get_realAsLong() != _real || qie.get_imaginaryAsLong() != _imaginary || qie.get_r() != _r)
            return false;
        return true;
    }


}

