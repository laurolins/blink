package blink;

/**
 * Class Entry on database
 */
public class ClassEntry {
    private int _id;
    private int _numEdges;
    private int _order;
    private int _size;
    private String _hg;
    private String _qi;
    private long _gem;
    private String _status;
    private String _qiStatus;
    private int _maxqi;
    public ClassEntry(int id, int numEdges, int order, int size, String hg, String qi, long gem, String status, String qiStatus, int maxqi) {
        _id = id;
        _numEdges = numEdges;
        _order = order;
        _hg = hg;
        _qi = qi;
        _size = size;
        _gem = gem;
        _status = status;
        _qiStatus = qiStatus;
        _maxqi = maxqi;
    }
    public int getId() { return _id; }
    public int getNumEdges() { return _numEdges; }
    public int getOrder() { return _order; }

    public int get_size() {
        return _size;
    }

    public String get_qi() {
        return _qi;
    }

    public String get_hg() {
        return _hg;
    }

    public long get_gem() {
        return _gem;
    }

    public String get_status() {
        return _status;
    }

    public String get_qiStatus() {
        return _qiStatus;
    }

    public int get_maxqi() {
        return _maxqi;
    }

    public String getDescription() {
        return String.format("Class: %7s  status: %-12s  qiStatus: %-10s  #GBlinks: %6d  gem: %6d  HG: %-9s  QI(%3d): %-9s  IDSet: %3d ",
                ""+this.getNumEdges()+"."+this.getOrder(),
                this.get_status(),
                this.get_qiStatus(),
                this.get_size(),
                this.get_gem(),
                this.get_hg(),
                this.get_maxqi(),
                this.get_qi(),
                this.getId());
    }

    public String toString() {
        return getDescription();
    }
}
