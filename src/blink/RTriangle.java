package blink;

class RTriangle {

    private RSegment _s1;
    private RSegment _s2;
    private RSegment _s3;

    private RPoint _a;
    private RPoint _b;
    private RPoint _c;

    public RTriangle(RSegment s1, RSegment s2, RSegment s3) {
        _s1 = s1;
        _s2 = s2;
        _s3 = s3;
        _a = RSegment.getCommonPoint(s1,s2);
        _b = RSegment.getCommonPoint(s2,s3);
        _c = RSegment.getCommonPoint(s3,s1);
    }

    public RSegment getSegment(RPoint a, RPoint b) {
        if (_s1.contains(a) && _s1.contains(b)) return _s1;
        else if (_s2.contains(a) && _s2.contains(b)) return _s2;
        else if (_s3.contains(a) && _s3.contains(b)) return _s3;
        else throw new RuntimeException();
    }

    public RPoint getDifferentPoint(RPoint ... points)  {
        for (RPoint difCandidate: new RPoint[] {_a,_b,_c}) {
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

    public void invertOrientation() {
        RSegment aux = _s2;
        _s2 = _s3;
        _s3 = aux;
        RPoint auxp = _b;
        _b = _c;
        _c = auxp;
    }

    public RTriangle(RPoint a, RPoint b, RPoint c) {
        _a = a;
        _b = b;
        _c = c;
        _s1 = new RSegment(a,b);
        _s2 = new RSegment(b,c);
        _s3 = new RSegment(c,a);
    }

    public void replaceSegment(RSegment s) {
        if (Log.MAX_LEVEL >= 4) {
            Log.log(4,"replace segment "+s.getStringWithPointNamesIfExists()+" on triangle "+this.toString());
        }
        if (_s1.equals(s)) _s1 = s;
        if (_s2.equals(s)) _s2 = s;
        if (_s3.equals(s)) _s3 = s;
        if (_a.equals(s.getA())) _a = s.getA();
        if (_a.equals(s.getB())) _a = s.getB();
        if (_b.equals(s.getA())) _b = s.getA();
        if (_b.equals(s.getB())) _b = s.getB();
        if (_c.equals(s.getA())) _c = s.getA();
        if (_c.equals(s.getB())) _c = s.getB();
        _s1.replacePointInstance(_a,_b,_c);
        _s2.replacePointInstance(_a,_b,_c);
        _s3.replacePointInstance(_a,_b,_c);
    }

    public RPoint getMiddle() {
        return getA().add(getB()).add(getC()).scale(new BigRational(1,3));
    }
    public RSegment[] getSegments() { return new RSegment[] {_s1,_s2,_s3}; }
    public RSegment getS1() { return _s1; }
    public RSegment getS2() { return _s2; }
    public RSegment getS3() { return _s3; }
    public RPoint getA() { return _a; }
    public RPoint getB() { return _b; }
    public RPoint getC() { return _c; }
    public RPoint getNormal() {
        return RPoint.crossProduct(getB().sub(getA()),getC().sub(getA()));
    }
    public boolean containsSegment(RSegment s) {
        // instance equals
        return s.equals(_s1) || s.equals(_s2) || s.equals(_s3);
    }
    public String toString() {
        return this.getA().getNameIfExistsOtherwiseId()+" , "+
               this.getB().getNameIfExistsOtherwiseId()+" , "+
               this.getC().getNameIfExistsOtherwiseId();
    }

    /**
     * Check whether p1 is on the same side
     * of the line defined by AB as p2.
     */
    public static boolean sameSide(RPoint p1, RPoint p2, RPoint a, RPoint b) {
        RPoint cp1 = RPoint.crossProduct(b.sub(a), p1.sub(a));
        RPoint cp2 = RPoint.crossProduct(b.sub(a), p2.sub(a));
        // System.out.println("cp1 "+cp1);
        // System.out.println("cp2 "+cp2);
        // System.out.println("dotProd "+RPoint.dotProduct(cp1, cp2));
        if (RPoint.dotProduct(cp1, cp2).compareTo(BigRational.ZERO) >= 0)
            return true;
        else
            return false;
    }

    public boolean onTheSamePlane(RPoint p) {
        return RPoint.dotProduct(
            p.sub(this.getA()),
            this.getNormal()).compareTo(BigRational.ZERO) == 0;
    }

    /**
     * Test if point is inside or on
     * the border line of the triangle.
     */
    public boolean hitTest(RPoint p) {
        if (!onTheSamePlane(p))
            return false;

        RPoint a = this.getA();
        RPoint b = this.getB();
        RPoint c = this.getC();

        boolean result = sameSide(p, a, b, c) &&
                         sameSide(p, b, a, c) &&
                         sameSide(p, c, a, b);

        // System.out.println("Test " + p.getNameIfExistsOtherwiseLocation() + " with " + toString() + " -> " + result);

        return result;
    }

    public RPoint projectPoint(RPoint p) {
        RPoint n = this.getNormal();
        LineAndPlanIntersection i = new LineAndPlanIntersection(this, new RSegment(p, p.add(n)));
        return i.getIntersectionPoint();
    }

    public RSegment projectSegment(RSegment s) {
        return new RSegment(this.projectPoint(s.getA()), this.projectPoint(s.getB()));
    }

    public RSegment getSegmentIncidentToButNot(RPoint p, RSegment s) {
        if (this.getS1().contains(p) && !s.equals(getS1()))
            return this.getS1();
        else if (this.getS2().contains(p) && !s.equals(getS2()))
            return this.getS2();
        else if (this.getS3().contains(p) && !s.equals(getS3()))
            return this.getS3();
        else throw new RuntimeException();
    }

}
