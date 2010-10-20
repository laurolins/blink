package linsoft.gui.bean;

import java.util.Date;

public class TestObject {
	private boolean _boolean;
	private int _int;
	private short _short;
	private long _long;
	private float _float;
	private double _double;
	private String _string;
	private Date _date;
    public TestObject() {
		_date = new Date();
		_string = "";
    }
    public boolean is_boolean() {
        return _boolean;
    }
    public double get_double() {
        return _double;
    }
    public float get_float() {
        return _float;
    }
    public int get_int() {
        return _int;
    }
    public long get_long() {
        return _long;
    }
    public short get_short() {
        return _short;
    }
    public String get_string() {
        return _string;
    }
    public void set_boolean(boolean _boolean) {
        this._boolean = _boolean;
    }
    public void set_double(double _double) {
        this._double = _double;
    }
    public void set_float(float _float) {
        this._float = _float;
    }
    public void set_int(int _int) {
        this._int = _int;
        this._long = _int;
    }
    public void set_long(long _long) {
        this._long = _long;
    }
    public void set_short(short _short) {
        this._short = _short;
    }
    public void set_string(String _string) {
        this._string = _string;
    }
    public Date get_date() {
        return _date;
    }
    public void set_date(Date _date) {
        this._date = _date;
    }
}