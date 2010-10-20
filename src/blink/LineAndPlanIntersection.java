package blink;

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
public class LineAndPlanIntersection {
    private RTriangle _t;
    private RSegment _s;

    private boolean _onThePlan;
    private boolean _noIntersection;
    private BigRational _parameter;

    public LineAndPlanIntersection(RPoint pointOnThePlan, RPoint planNormal, RSegment s) {
        _s = s;

        BigRational x0 = pointOnThePlan.getX();
        BigRational y0 = pointOnThePlan.getY();
        BigRational z0 = pointOnThePlan.getZ();

        BigRational xn = planNormal.getX();
        BigRational yn = planNormal.getY();
        BigRational zn = planNormal.getZ();

        // point on the plan
        BigRational x1 = s.getA().getX();
        BigRational y1 = s.getA().getY();
        BigRational z1 = s.getA().getZ();
        BigRational x2 = s.getB().getX();
        BigRational y2 = s.getB().getY();
        BigRational z2 = s.getB().getZ();

        // t = (xv*x1-xv*x0+yv*y1-yv*y0+zv*z1-zv*z0) /
        //     (-xv*x2+xv*x1-yv*y2+yv*y1-zv*z2+zv*z1)
        BigRational alphaNum = xn.mul(x1).
                               sub(xn.mul(x0)).
                               add(yn.mul(y1)).
                               sub(yn.mul(y0)).
                               add(zn.mul(z1)).
                               sub(zn.mul(z0));

        BigRational alphaDenom = xn.neg().mul(x2).
                                 add(xn.mul(x1)).
                                 sub(yn.mul(y2)).
                                 add(yn.mul(y1)).
                                 sub(zn.mul(z2)).
                                 add(zn.mul(z1));

        if (alphaDenom.compareTo(BigRational.ZERO) == 0) {
            if (alphaNum.compareTo(BigRational.ZERO) == 0) {
                _onThePlan = true;
                return;
            }
            else {
                _noIntersection = true;
                return;
            }
        }
        else {
            _parameter = alphaNum.div(alphaDenom);
        }

    }


    public LineAndPlanIntersection(RTriangle t, RSegment s) {
        this(t.getA(),t.getNormal(),s);
        _t = t;
    }

    public BigRational getParameter() {
        return _parameter;
    }

    public RPoint getIntersectionPoint() {
        if (this.noIntersection())
            return null;
        return _s.getPoint(_parameter);
    }

    public boolean onThePlan() {
        return _onThePlan;
    }

    public boolean noIntersection() {
        return _noIntersection;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(String.format("Segment: ( %s , %s , %s ) ( %s , %s , %s )\n",
                                _s.getA().getX().toString(),
                                _s.getA().getY().toString(),
                                _s.getA().getZ().toString(),
                                _s.getB().getX().toString(),
                                _s.getB().getY().toString(),
                                _s.getB().getZ().toString()));

        sb.append(String.format("Triangle: ( %s , %s , %s ) ( %s , %s , %s ) ( %s , %s , %s )\n",
                                _t.getS1().getA().getX().toString(),
                                _t.getS1().getA().getY().toString(),
                                _t.getS1().getA().getZ().toString(),
                                _t.getS2().getA().getX().toString(),
                                _t.getS2().getA().getY().toString(),
                                _t.getS2().getA().getZ().toString(),
                                _t.getS3().getA().getX().toString(),
                                _t.getS3().getA().getY().toString(),
                                _t.getS3().getA().getZ().toString()));

        sb.append("On The Plan:        "+this.onThePlan()+"\n");
        sb.append("No Intersection:    "+this.noIntersection()+"\n");
        sb.append("Parameter:          "+_parameter+"\n");
        sb.append("Intersection Point: "+this.getIntersectionPoint()+"\n");

        return sb.toString();

    }

}
