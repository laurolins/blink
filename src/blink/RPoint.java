package blink;


enum RPointType {
    vertice, arc, internal, undefined;
}

class RPoint {
    private int _id;
    private BigRational _x;
    private BigRational _y;
    private BigRational _z;
    private String _name;
    RPointType _type;

    private static int NEXT_ID = 1;

    public String getName() { return _name; }
    public void setName(String name) { _name = name; }
    public RPoint(BigRational x, BigRational y, BigRational z) {
        _id = NEXT_ID++;
        _x = x;
        _y = y;
        _z = z;
        _type = RPointType.undefined;
    }

    public RPoint scale(BigRational s) {
        return new RPoint(_x.mul(s),_y.mul(s),_z.mul(s));
    }

    public void invert() {
        _x = _x.neg();
        _y = _y.neg();
        _z = _z.neg();
    }

    public void set(BigRational x, BigRational y, BigRational z) {
        _x = x;
        _y = y;
        _z = z;
    }

    public void set(RPoint p) {
        // System.out.println("Set "+this);
        _x = p.getX();
        _y = p.getY();
        _z = p.getZ();
        // System.out.println("To  "+this);
    }

    public void setX(BigRational x) { _x = x; }
    public void setY(BigRational y) { _y = y; }
    public void setZ(BigRational z) { _z = z; }

    public RPoint(int x, int y, int z) {
        _x = new BigRational(x);
        _y = new BigRational(y);
        _z = new BigRational(z);
    }

    public String getNameIfExistsOtherwiseId() {
        if (_name != null) return _name;
        else return ""+this.getId();
    }

    public RPointType getType() {
        return _type;
    }

    public void setType(RPointType t) {
        _type = t;
    }

    public void reducePrecision() {
        BigRational k = new BigRational(1000000L);
        _x = _x.mul(k).round().div(k);
        _y = _y.mul(k).round().div(k);
        _z = _z.mul(k).round().div(k);
    }

    public RPoint approxNormalize() {
        BigRational squareModulus = _x.mul(_x).add(_y.mul(_y)).add(_z.mul(_z));
        int n = 0;
        while (squareModulus.compareTo(BigRational.ONE) < 0) {
            n++;
            squareModulus = squareModulus.mul(100);
        }
        while (squareModulus.compareTo(BigRational.ONE) > 0) {
            n--;
            squareModulus = squareModulus.div(100);
        }

        long v = squareModulus.mul(100000000L).longValue();
        double am = Math.sqrt((double)v)/10000.0;
        BigRational approxModulus = new BigRational((long)(am*100000000L),100000000L);
        BigRational scale = (new BigRational(10)).pow(-n);
        approxModulus = approxModulus.mul(scale);

        // System.out.println("Square Modulus "+(_x.mul(_x).add(_y.mul(_y)).add(_z.mul(_z))));
        // System.out.println("Square Root "+approxModulus);

        return new RPoint(
            _x.div(approxModulus),
            _y.div(approxModulus),
            _z.div(approxModulus));
    }

    public BigRational getX() { return _x; }
    public BigRational getY() { return _y; }
    public BigRational getZ() { return _z; }

    public RPoint copy() {
        RPoint result = new RPoint(_x, _y, _z);
        result.setType(_type);
        return result;
    }

    public int hashCode() {
        return getX().hashCode() + getY().hashCode() + getZ().hashCode();
    }

    public boolean equals(Object o) {
        return isEqual((RPoint) o);
    }

    public boolean isEqual(RPoint p) {
        return this.getX().equals(p.getX()) &&
                this.getY().equals(p.getY()) &&
                this.getZ().equals(p.getZ());
    }

    public RPoint sub(RPoint p) {
        return new RPoint(_x.sub(p.getX()), _y.sub(p.getY()), _z.sub(p.getZ()));
    }

    public RPoint add(RPoint p) {
        return new RPoint(_x.add(p.getX()), _y.add(p.getY()), _z.add(p.getZ()));
    }

    public String toString() {
        return "( "+_x+", "+_y+", "+_z+" )";
    }

    public static BigRational dotProduct(RPoint u, RPoint v) {
        return u.getX().mul(v.getX()).
                add(u.getY().mul(v.getY())).
                add(u.getZ().mul(v.getZ()));
    }

    public static double approxDotProduct(RPoint u, RPoint v) {
        BigRational exact = dotProduct(u,v);
        return (double)exact.mul(100000000L).longValue() / 100000000.0;
    }

    public int getId() {
        return _id;
    }

    public static RPoint crossProduct(RPoint u, RPoint v) {
        BigRational xu = u.getX();
        BigRational yu = u.getY();
        BigRational zu = u.getZ();

        BigRational xv = v.getX();
        BigRational yv = v.getY();
        BigRational zv = v.getZ();

        return new RPoint(
                yu.mul(zv).sub(zu.mul(yv)),
                zu.mul(xv).sub(xu.mul(zv)),
                xu.mul(yv).sub(yu.mul(xv)));
    }

    public boolean samePosition(RPoint p) {
        return _x.equals(p.getX()) && _y.equals(p.getY()) && _z.equals(p.getZ());
    }

    public double approxModulus() {
        double x = (double) _x.mul(100000000L).longValue() / 100000000.0;
        double y = (double) _y.mul(100000000L).longValue() / 100000000.0;
        double z = (double) _z.mul(100000000L).longValue() / 100000000.0;
        return Math.sqrt(x*x+y*y+z*z);
    }
}
