package blink;

class SegmentIntersection {

    private RSegment _s1;
    private RSegment _s2;

    private BigRational _s1Parameter;
    private BigRational _s2Parameter;
    private RPoint _intersectionPoint;

    private boolean _linesAreColinear;

    public RSegment getSegment1() { return _s1; }
    public RSegment getSegment2() { return _s2; }

    /**
     * Intersectar a projeção das retas definidas por
     * s1 e s2 no plano xy. Depois verificar se o
     * a coordenada z é igual.
     */
    public SegmentIntersection(RSegment s1, RSegment s2) {
        _s1 = s1;
        _s2 = s2;

        RPoint P1 = _s1.getA();
        RPoint P2 = _s1.getB();

        RPoint Q1 = _s2.getA();
        RPoint Q2 = _s2.getB();

        BigRational xp1 = P1.getX();
        BigRational xp2 = P2.getX();
        BigRational yp1 = P1.getY();
        BigRational yp2 = P2.getY();
        BigRational zp1 = P1.getZ();
        BigRational zp2 = P2.getZ();

        BigRational xq1 = Q1.getX();
        BigRational xq2 = Q2.getX();
        BigRational yq1 = Q1.getY();
        BigRational yq2 = Q2.getY();
        BigRational zq1 = Q1.getZ();
        BigRational zq2 = Q2.getZ();

        BigRational a = xp2.sub(xp1);
        BigRational b = xq1.sub(xq2);
        BigRational c = yp2.sub(yp1);
        BigRational d = yq1.sub(yq2);
        BigRational e = zp2.sub(zp1);
        BigRational f = zq1.sub(zq2);

        BigRational g = xq1.sub(xp1);
        BigRational h = yq1.sub(yp1);
        BigRational i = zq1.sub(zp1);

        BigRational detXY = a.mul(d).sub(b.mul(c));
        BigRational detXZ = a.mul(f).sub(b.mul(e));
        BigRational detYZ = c.mul(f).sub(d.mul(e));

        BigRational detAlfaXY = g.mul(d).sub(b.mul(h));
        BigRational detBetaXY = a.mul(h).sub(g.mul(c));

        BigRational detAlfaXZ = g.mul(f).sub(b.mul(i));
        BigRational detBetaXZ = a.mul(i).sub(g.mul(e));

        BigRational detAlfaYZ = h.mul(f).sub(d.mul(i));
        BigRational detBetaYZ = c.mul(i).sub(h.mul(e));

        // impossible intersecion on plane XY projection
        boolean impossibleXY = (detXY.compareTo(BigRational.ZERO) == 0 &&
                                (detAlfaXY.compareTo(BigRational.ZERO) != 0 ||
                                 detAlfaXY.compareTo(BigRational.ZERO) != 0));
        boolean indeterminateXY = (detXY.compareTo(BigRational.ZERO) == 0 &&
                                   detAlfaXY.compareTo(BigRational.ZERO) == 0 &&
                                   detAlfaXY.compareTo(BigRational.ZERO) == 0);

        // impossible intersecion on plane XZ projection
        boolean impossibleXZ = (detXZ.compareTo(BigRational.ZERO) == 0 &&
                                (detAlfaXZ.compareTo(BigRational.ZERO) != 0 ||
                                 detAlfaXZ.compareTo(BigRational.ZERO) != 0));
        boolean indeterminateXZ = (detXZ.compareTo(BigRational.ZERO) == 0 &&
                                   detAlfaXZ.compareTo(BigRational.ZERO) == 0 &&
                                   detAlfaXZ.compareTo(BigRational.ZERO) == 0);

        // impossible intersecion on plane YZ projection
        boolean impossibleYZ = (detYZ.compareTo(BigRational.ZERO) == 0 &&
                                (detAlfaYZ.compareTo(BigRational.ZERO) != 0 ||
                                 detAlfaYZ.compareTo(BigRational.ZERO) != 0));
        boolean indeterminateYZ = (detYZ.compareTo(BigRational.ZERO) == 0 &&
                                   detAlfaYZ.compareTo(BigRational.ZERO) == 0 &&
                                   detAlfaYZ.compareTo(BigRational.ZERO) == 0);

        // impossible no intersection
        if (impossibleXY || impossibleXZ || impossibleYZ) {
            return;
        }

        // indeterminate solution
        if (indeterminateXY && indeterminateXZ && indeterminateYZ) {
            _linesAreColinear = true;
            return;
        }
        else {

            // XY
            if (!indeterminateXY) {
                BigRational s1pCandidate = detAlfaXY.div(detXY);
                BigRational s2pCandidate = detBetaXY.div(detXY);
                if (_s1Parameter == null) {
                    _s1Parameter = s1pCandidate;
                    _s2Parameter = s2pCandidate;
                }
                // incompatible solutions?
                else if (_s1Parameter.compareTo(s1pCandidate) != 0 ||
                         _s2Parameter.compareTo(s2pCandidate) != 0) {
                    _s1Parameter = null;
                    _s2Parameter = null;
                    return;
                }
            }

            // XZ
            if (!indeterminateXZ) {
                BigRational s1pCandidate = detAlfaXZ.div(detXZ);
                BigRational s2pCandidate = detBetaXZ.div(detXZ);
                if (_s1Parameter == null) {
                    _s1Parameter = s1pCandidate;
                    _s2Parameter = s2pCandidate;
                }
                // incompatible solutions?
                else if (_s1Parameter.compareTo(s1pCandidate) != 0 ||
                         _s2Parameter.compareTo(s2pCandidate) != 0) {
                    _s1Parameter = null;
                    _s2Parameter = null;
                    return;
                }
            }

            // YZ
            if (!indeterminateYZ) {
                BigRational s1pCandidate = detAlfaYZ.div(detYZ);
                BigRational s2pCandidate = detBetaYZ.div(detYZ);
                if (_s1Parameter == null) {
                    _s1Parameter = s1pCandidate;
                    _s2Parameter = s2pCandidate;
                }
                // incompatible solutions?
                else if (_s1Parameter.compareTo(s1pCandidate) != 0 ||
                         _s2Parameter.compareTo(s2pCandidate) != 0) {
                    _s1Parameter = null;
                    _s2Parameter = null;
                    return;
                }
            }
        }

        BigRational xp = xp1.add(xp2.sub(xp1).mul(_s1Parameter));
        BigRational yp = yp1.add(yp2.sub(yp1).mul(_s1Parameter));
        BigRational zp = zp1.add(zp2.sub(zp1).mul(_s1Parameter));

        // does the z position coincide?
        _intersectionPoint = new RPoint(xp, yp, zp);
    }

    /**
     * Returns true if the lines defined by
     * s1 and s2 intersects in exactely one point.
     */
    public boolean onePointLineIntersection() {
        return _intersectionPoint != null;
    }

    /**
     * Returns true if the segments
     * s1 and s2 intersects in exactely one point.
     */
    public boolean onePointSegmentIntersection() {
        return onePointLineIntersection() &&
                _s1Parameter.compareTo(BigRational.ZERO) >= 0 &&
                _s1Parameter.compareTo(BigRational.ZERO) <= 1 &&
                _s2Parameter.compareTo(BigRational.ZERO) >= 0 &&
                _s2Parameter.compareTo(BigRational.ONE) <= 1;
    }

    /**
     * Returns true if the lines defined
     * by s1 and s2 are the same.
     */
    public boolean linesAreColinear() {
        return _linesAreColinear;
    }

    /**
     * Returns the one point intersection of
     * the two lines. Returns null there is
     * no such point.
     */
    public RPoint getIntersectionPoint() {
        return _intersectionPoint;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(String.format("Segment 1: %s  %s \n",
                                _s1.getA().getNameIfExistsOtherwiseId(),
                                _s1.getB().getNameIfExistsOtherwiseId()));

        sb.append(String.format("Segment 2: %s  %s \n",
                                _s2.getA().getNameIfExistsOtherwiseId(),
                                _s2.getB().getNameIfExistsOtherwiseId()));

        sb.append("Colinear: "+this.linesAreColinear()+"\n");
        sb.append("Intersection Point: "+this.getIntersectionPoint()+"\n");
        sb.append("s1 Parameter: "+_s1Parameter+"\n");
        sb.append("s2 Parameter: "+_s2Parameter+"\n");
        return sb.toString();
    }

}
