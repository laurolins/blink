package blink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * RArc
 */
public class RArc {

    private ArrayList<RSegment> _segments = new ArrayList<RSegment>();

    public RArc() {
    }

    public RArc(RArc a1, RArc a2) {
        ArrayList la1 = new ArrayList();
        ArrayList la2 = new ArrayList();
        la1.addAll(a1.getSegments());
        la2.addAll(a2.getSegments());
        if (a1.getLastPoint().equals(a2.getFirstPoint())) {
            _segments.addAll(la1);
            _segments.addAll(la2);
        }
        else if (a1.getFirstPoint().equals(a2.getFirstPoint())) {
            Collections.reverse(la1);
            _segments.addAll(la1);
            _segments.addAll(la2);
        }
        else if (a1.getFirstPoint().equals(a2.getLastPoint())) {
            Collections.reverse(la1);
            Collections.reverse(la2);
            _segments.addAll(la1);
            _segments.addAll(la2);
        }
        else if (a1.getLastPoint().equals(a2.getLastPoint())) {
            Collections.reverse(la2);
            _segments.addAll(la1);
            _segments.addAll(la2);
        }
        else throw new RuntimeException();
    }

    public RArc(ArrayList<RSegment> segments) {
        for (RSegment s: segments)
            this.addSegment(s);
    }

    public boolean sameEndPoints(RArc a) {
        return (a.getFirstPoint().equals(this.getFirstPoint()) &&
                a.getLastPoint().equals(this.getLastPoint())) ||
                (a.getFirstPoint().equals(this.getLastPoint()) &&
                 a.getLastPoint().equals(this.getFirstPoint()));
    }

    public ArrayList<RPoint> getPointsInOrder(RPoint startPoint) {
        ArrayList<RPoint> points = new ArrayList<RPoint>();
        for (int i=0;i<this.getNumberOfSegments();i++) {
            if (i == 0)
                points.add(this.getSegment(i).getA());
            points.add(this.getSegment(i).getB());
        }
        if (!points.get(0).equals(startPoint))
            Collections.reverse(points);
        return points;
    }



    public RArc(RSegment s) {
        this.addSegment(s);
    }

    public void addSegment(RSegment s) {
        if (_segments.size() > 0 && !this.getLastPoint().equals(s.getA())) {
            throw new RuntimeException("No!");
        }
        _segments.add(s);
    }

    public RSegment getSegmentIncidentToNotEqual(RPoint p,RSegment ss) {
        for (RSegment s: _segments) {
            if ((s.getA().equals(p) || s.getB().equals(p)) && (ss==null || !s.equals(ss))) {
                return s;
            }
        }
        throw new RuntimeException();
    }


    public ArrayList<RSegment> getSegments() {
        return (ArrayList<RSegment>) _segments.clone();
    }

    public HashSet<RPoint> getPoints() {
        HashSet<RPoint> points = new HashSet<RPoint>();
        for (int i=0;i<this.getNumberOfSegments();i++) {
            points.add(this.getSegment(i).getA());
            points.add(this.getSegment(i).getB());
        }
        return points;
    }

    public RSegment getSegment(int index) {
        return _segments.get(index);
    }

    public int getNumberOfSegments() {
        return _segments.size();
    }

    public RPoint getFirstPoint() {
        return _segments.get(0).getA();
    }

    public RPoint getLastPoint() {
        return _segments.get(_segments.size()-1).getB();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Arc\n");
        for (RSegment s: _segments) {
            sb.append("segment "+s.getStringWithPointNamesIfExists()+"\n");
        }
        return sb.toString();
    }

    public boolean contains(RPoint p) {
        for (RSegment s: _segments) {
            if (p.equals(s.getA()) || p.equals(s.getB())) {
                return true;
            }
        }
        return false;
    }

}
