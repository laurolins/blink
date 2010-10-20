package blink;

import java.util.ArrayList;
import java.util.Collection;

class RSegment {
    private RPoint _a;
    private RPoint _b;

    public RSegment(RPoint a, RPoint b) {
        _a = a;
        _b = b;
    }

    public void replacePointInstance(RPoint ... points) {
        for(RPoint p: points) {
            if (_a.equals(p)) _a = p;
            if (_b.equals(p)) _b = p;
        }
    }

    public void replacePointInstance(Collection<RPoint> points) {
        for(RPoint p: points) {
            if (_a.equals(p)) _a = p;
            if (_b.equals(p)) _b = p;
        }
    }

    public RPoint getDifferentPoint(RPoint ... points)  {
        for (RPoint difCandidate: new RPoint[] {_a,_b}) {
            boolean isDifferent = true;
            for (RPoint p: points) {
                if (difCandidate.equals(p)) {
                    isDifferent = false;
                    break;
                }
            }
            if (isDifferent)
                return difCandidate;
        }
        throw new RuntimeException("Ooopppss");
    }


    public RPoint getMiddlePoint() {
        return _a.add(_b).scale(new BigRational(1,2));
    }

    public int hashCode() {
        return getA().hashCode() + getB().hashCode();
    }

    public boolean contains(RPoint p) {
        return _a.equals(p) || _b.equals(p);
    }

    public boolean equals(Object o) {
        RSegment s = (RSegment) o;
        return
                (s.getA().isEqual(this.getA()) && s.getB().isEqual(this.getB())) ||
                (s.getA().isEqual(this.getB()) && s.getB().isEqual(this.getA()));
    }

    public RPoint getA() { return _a; }

    public RPoint getB() { return _b; }

    public RPoint getOpposite(RPoint p) {
        if (p.equals(_a)) return _b;
        else if (p.equals(_b)) return _a;
        else throw new RuntimeException();
    }

    public RPoint getPoint(BigRational s) {
        return new RPoint(
                _a.getX().add(_b.getX().sub(_a.getX()).mul(s)),
                _a.getY().add(_b.getY().sub(_a.getY()).mul(s)),
                _a.getZ().add(_b.getZ().sub(_a.getZ()).mul(s)));
    }

    public ArrayList<RSegment> breakSegment(RPoint p) {
        ArrayList<RSegment> result = new ArrayList<RSegment>(2);
        if (p == _a || p == _b) {
            result.add(new RSegment(_a,_b));
        } else {
            result.add(new RSegment(_a,p));
            result.add(new RSegment(p,_b));
        }
        return result;
    }

    public static RPoint getCommonPoint(RSegment s1, RSegment s2) {
        RPoint a1 = s1.getA();
        RPoint b1 = s1.getB();

        RPoint a2 = s2.getA();
        RPoint b2 = s2.getB();

        if (a1.equals(a2)) return a1;
        else if (a1.equals(b2)) return a1;
        else if (b1.equals(a2)) return b1;
        else if (b1.equals(b2)) return b1;
        else return null;
    }

    public RPoint getVector() {
        return this.getB().sub(this.getA());
    }

    public double getApproxLength() {
        return this.getB().sub(this.getA()).approxModulus();
    }

    public static BigRational dotProduct(RSegment u, RSegment v) {
        return RPoint.dotProduct(u.getVector(),v.getVector());
    }

    public static double approxDotProduct(RSegment u, RSegment v) {
        return RPoint.approxDotProduct(u.getVector(),v.getVector());
    }

    public String getStringWithPointNamesIfExists() {
        return getA().getNameIfExistsOtherwiseId()+" "+getB().getNameIfExistsOtherwiseId();
    }

    public String toString() {
        return getA()+" "+getB();
    }

}

