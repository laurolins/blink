package blink;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point3d;

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
public class Embedding {
    EVertex _vertices[];
    HashMap<Integer,EAxis> _mapAxis;
    public Embedding() {
        double l = 1;
        double alpha = 0.2;

        // initialize vertices
        EVertex v0 = new EVertex(new Point3d(0,0,0),GemColor.yellow);
        EVertex v1 = new EVertex(new Point3d(0,l,l),GemColor.blue);
        EVertex v2 = new EVertex(new Point3d(l,0,l),GemColor.red);
        EVertex v3 = new EVertex(new Point3d(l,l,0),GemColor.green);
        _vertices = new EVertex[] {v0,v1,v2,v3};

        // get center
        Point3d center = new Point3d(0,0,0);
        for (EVertex v: _vertices)
            center.add(v.getPosition());
        center.scale(0.25);

        // create axis
        _mapAxis = new HashMap<Integer,EAxis>();
        for (int i = 0; i < _vertices.length; i++) {
            EVertex vi = _vertices[i];
            for (int j = i+1; j < _vertices.length; j++) {
                EVertex vj = _vertices[j];

                Point3d origin = new Point3d(vi.getPosition());
                origin.add(vj.getPosition());
                origin.scale(0.5);

                Point3d direction = new Point3d(origin);
                direction.sub(center);
                double length = direction.distance(new Point3d(0,0,0));
                direction.scale(alpha/length);

                EAxis axis = new EAxis(this,vi,vj,origin,direction);
                _mapAxis.put(axis.getColorSet(),axis);
            }
        }

        // create arcs
        for (EAxis a: _mapAxis.values()) {
            EVertex u = a.getV1();
            EVertex v = a.getV2();
            EArc arc = new EArc(u,v);
            a.addArc(arc);
        }

        //
        {
            int colorSet = GemColor.getColorSet(GemColor.red, GemColor.yellow);
            EAxis axis = _mapAxis.get(colorSet);
            EArc a1 = new EArc(v0, v2);
            EArc a2 = new EArc(v1, v3);
            axis.addArc(a1);
            axis.addArc(a2);
        }

        //
        {
            int colorSet = GemColor.getColorSet(GemColor.yellow, GemColor.green);
            EAxis axis = _mapAxis.get(colorSet);
            EArc a1 = new EArc(v0, v3);
            EArc a2 = new EArc(v1, v2);
            axis.addArc(a1);
            axis.addArc(a2);
        }
        //
        /*
        colorSet = GemColor.getColorSet(GemColor.yellow,GemColor.green);
        axis = _mapAxis.get(colorSet);
        a1 = new EArc(v0,v3);
        a2 = new EArc(v1,v2);
        axis.addArc(a1);
        axis.addArc(a2);*/

        //
        /*
        colorSet = GemColor.getColorSet(GemColor.blue,GemColor.green);
        axis = _mapAxis.get(colorSet);
        a1 = new EArc(v1,v3);
        a2 = new EArc(v0,v2);
        axis.addArc(a1);
        axis.addArc(a2);*/




    }

    public int getNumArcs() {
        int result = 0;
        for (EAxis a: _mapAxis.values()) {
            result+=a.getArcs().size();
        }
        return result;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Embedding E = new Embedding();
        PrintWriter pw = new PrintWriter("c:/embedding.wrl");
        E.printVRML(pw);
        pw.close();
    }

    public void printVRML(PrintWriter pw) {
        pw.println("#VRML V2.0 utf8");

        pw.println(
        "PROTO ConnectingCylinder [\n"+
        "   field SFNode  appearance NULL\n"+
        "   field SFBool  bottom     TRUE\n"+
        "   field SFVec3f vertex0    0 -1 0\n"+
        "   field SFVec3f vertex1    0 1 0\n"+
        "   field SFFloat radius     1\n"+
        "   field SFBool  side       TRUE\n"+
        "   field SFBool  top        TRUE\n"+
        "]\n"+
        "{\n"+
        "   DEF TRANSFORM Transform {\n"+
        "      children [\n"+
        "         Shape {\n"+
        "            appearance Appearance { material Material { diffuseColor 0.5 0.6 0 } }\n"+
        "            geometry Cylinder {\n"+
        "            bottom IS bottom\n"+
        "            height 1\n"+
        "            radius IS radius\n"+
        "            side IS  side\n"+
        "            top  IS top\n"+
        "            }\n"+
        "         }\n"+
        "      ]\n"+
        "   }\n"+
        "   Script {\n"+
        "      field SFVec3f vertex0   IS vertex0\n"+
        "      field SFVec3f vertex1   IS vertex1\n"+
        "      field SFNode  transform USE TRANSFORM\n"+
        "      directOutput TRUE\n"+
        "      url  \"javascript:\n"+
        "         function initialize() {\n"+
        "            // Calculate vector for cylinder\n"+
        "            var vecCylinder;\n"+
        "            vecCylinder = vertex0.subtract(vertex1);\n"+
        "            // Calculate length and store into scale factor\n"+
        "            transform.scale = new SFVec3f (1,vecCylinder.length(),1);\n"+
        "            // Calculate translation (average of vertices) and store\n"+
        "            var vecTranslation;\n"+
        "            vecTranslation = vertex0.add(vertex1).divide(2);\n"+
        "            transform.translation = vecTranslation;\n"+
        "            // Calculate rotation (rotation that takes vector 0 1 0 to vecCylinder).\n"+
        "            var rotTransform;\n"+
        "            rotTransform = new SFRotation(new SFVec3f(0,1,0),vecCylinder);\n"+
        "            transform.rotation = rotTransform;\n"+
        "            // Done\n"+
        "            return;\n"+
        "         }\n"+
        "      \"\n"+
        "   }\n"+
        "}");


        pw.println("PROTO BlueVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 0 0 1 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO GreenVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 0 1 0 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO CyanVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 0 1 1 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO YellowVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 1 1 0 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO WhiteVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 1 1 1 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO RedVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 1 0 0 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO SmallWhiteVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 1 1 1 } }");
        pw.println("geometry Sphere{radius 0.03}");
        pw.println("} }");

        // axis
        /*
        for (EAxis a: _mapAxis.values()) {
            Point3d o = new Point3d(a.getOrigin());
            EArc last = a.getLastArc();

            Point3d f;
            f = (last != null ? new Point3d(last.getCenter()) : new Point3d(o));
            f.add(a.getDirection());

            pw.println(String.format(
               "ConnectingCylinder { radius 0.01 vertex0 %.8f %.8f %.8f vertex1 %.8f %.8f %.8f }",
               o.x,o.y,o.z,f.x,f.y,f.z));

            pw.println("Transform {");
            pw.println(String.format("translation  %.6f %.6f %.6f", o.x, o.y, o.z));
            pw.println("children CyanVertice {}");
            pw.println("}");

        }*/

        // arcs
        for (EAxis a: _mapAxis.values()) {

            ArrayList<EArc> arcs = a.getArcs();
            for (int i = 0;i<arcs.size();i++) {
                EArc arc = arcs.get(i);

                ArrayList<EVertex> S = new ArrayList<EVertex>();
                for (EVertex v: _vertices)
                    if (v != arc.getU() && v != arc.getU())
                        S.add(v);

                Point3d p[] = {
                              new Point3d(arc.getU().getPosition()),
                              new Point3d(arc.getCenter()),
                              new Point3d(arc.getV().getPosition()),
                              new Point3d(S.get(0).getPosition()),
                              new Point3d(S.get(1).getPosition())
                };

                pw.println("Transform {");
                pw.println(String.format("translation  %.6f %.6f %.6f",p[1].x,p[1].y,p[1].z));
                pw.println("children SmallWhiteVertice {}");
                pw.println("}");

                { // draw arcs
                    pw.println(String.format(
                            "ConnectingCylinder { radius 0.01 vertex0 %.8f %.8f %.8f vertex1 %.8f %.8f %.8f }",
                            p[0].x, p[0].y, p[0].z, p[1].x, p[1].y, p[1].z));
                    pw.println(String.format(
                            "ConnectingCylinder { radius 0.01 vertex0 %.8f %.8f %.8f vertex1 %.8f %.8f %.8f }",
                            p[1].x, p[1].y, p[1].z, p[2].x, p[2].y, p[2].z));
                }


                // define the tetrahedras that are not the first
                /*
                if (arc == a.getArcs().get(0) &&
                    arc.getAxis().getColorSet() != GemColor.getColorSet(GemColor.yellow,GemColor.green) &&
                    arc.getAxis().getColorSet() != GemColor.getColorSet(GemColor.blue,GemColor.red))
                    continue;*/

                /*

                // get other arc
                if (arc == a.getArcs().get(0))
                    continue;

                EArc arc0 = arcs.get(i-1);
                S = new ArrayList<EVertex>();
                for (EVertex v: _vertices)
                    if (v != arc0.getU() && v != arc0.getU())
                        S.add(v);
                Point3d p0[] = {
                              new Point3d(arc0.getU().getPosition()),
                              new Point3d(arc0.getCenter()),
                              new Point3d(arc0.getV().getPosition()),
                              new Point3d(S.get(0).getPosition()),
                              new Point3d(S.get(1).getPosition())
                };

                double r = Math.random();
                double g = Math.random();
                double b = Math.random();
                pw.println("Shape {");
                pw.println(String.format("appearance Appearance { material Material { diffuseColor %.3f %.3f %.3f  transparency 0 } } ",r,g,b));
                pw.println("geometry IndexedFaceSet { ");
                // pw.println(" solid FALSE ");
                pw.println("coord Coordinate { ");
                pw.println("point [ ");
                for (int j=0;j<p.length;j++) {
                    pw.print(String.format("%.8f %.8f %.8f", p[j].x, p[j].y, p[j].z));
                    pw.println(",");
                }
                for (int j=0;j<p0.length;j++) {
                    pw.print(String.format("%.8f %.8f %.8f", p0[j].x, p0[j].y, p0[j].z));
                    if (j == p0.length-1)
                        pw.println("]");
                    else
                        pw.println(",");
                }
                pw.println("}");
                pw.println("coordIndex [");
                pw.println("0 1 3,");
                pw.println("1 2 3,");
                pw.println("4 1 0,");
                pw.println("1 2 4,");
                pw.println("5 6 8,");
                pw.println("6 7 8,");
                pw.println("9 6 5,");
                pw.println("6 7 9");
                pw.println("] } }");
                */
            }
        }

        for (EVertex v: _vertices) {
            Point3d p = v.getPosition();

            String st = "";
            if (v.getColor() == GemColor.yellow) {
                st = "YellowVertice";
            }
            else if (v.getColor() == GemColor.blue) {
                st = "BlueVertice";
            }
            else if (v.getColor() == GemColor.red) {
                st = "RedVertice";
            }
            else if (v.getColor() == GemColor.green) {
                st = "GreenVertice";
            }


            pw.println("Transform {");
            pw.println(String.format("translation  %.6f %.6f %.6f",p.x,p.y,p.z));
            pw.println("children "+st+" {}");
            pw.println("}");
        }
        pw.flush();

    }

}
class EAxis {
    private Embedding _embedding;
    private Point3d _origin;
    private Point3d _direction;
    private EVertex _v1;
    private EVertex _v2;
    private ArrayList<EArc> _arcs;
    public EAxis(Embedding embedding, EVertex v1, EVertex v2, Point3d origin, Point3d direction) {
        _embedding = embedding;
        _v1 = v1;
        _v2 = v2;
        _origin = origin;
        _direction = direction;
        _arcs = new ArrayList<EArc>();
    }
    /**
     * Return the "color set" of the tetrahedron
     * faces whose common tetrahedron edge is
     * perpendicular to this axis.
     */
    public int getColorSet() {
        return GemColor.getComplementColorSet(_v1.getColor(),_v2.getColor());
    }
    public Point3d getOrigin() { return (Point3d)_origin.clone(); }
    public Point3d getDirection() { return (Point3d)_direction.clone(); }
    public EVertex getV1() { return _v1; }
    public EVertex getV2() { return _v2; }
    public EArc getLastArc() {
        if (_arcs.size() == 0) return null;
        return _arcs.get(_arcs.size()-1);
    }

    public void addArc(EArc a) {
        /*
        EArc last = this.getLastArc();
        Point3d p = (last == null ? getOrigin() : last.getCenter());
        if (last != null)
            p.add(this.getDirection());
        a.setCenter(p);*/
        if (_arcs.size() == 0) {
            a.setCenter(this.getOrigin());
        }
        else {
            int n = _embedding.getNumArcs() - 4;
            if (n < 0) throw new RuntimeException();
            Point3d p = this.getDirection();
            p.scale(n+1);
            p.add(this.getOrigin());
            a.setCenter(p);
        }

        // add
        a.setAxis(this);
        _arcs.add(a);
    }

    public ArrayList<EArc> getArcs() {
        return _arcs;
    }


    public void addArc(int index, EArc a) {
        if (index == _arcs.size())
            addArc(a);

        EArc a1 = _arcs.get(index);
        EArc a2 = _arcs.get(index+1);

        Point3d p = new Point3d(a1.getCenter());

        Point3d increment = new Point3d(a2.getCenter());
        increment.sub(a1.getCenter());
        increment.scale(0.5);

        p.add(increment);

        a.setCenter(p);

        // add
        a.setAxis(this);
        _arcs.add(a);
    }
}

class EArc {
    private Point3d _center;
    private EVertex _u;
    private EVertex _v;
    private EAxis _axis;
    public EArc(EVertex u, EVertex v) {
        _u = u;
        _v = v;
    }
    public void setAxis(EAxis axis) {
        _axis = axis;
    }
    public EAxis getAxis() {
        return _axis;
    }
    public void setCenter(Point3d p) {
        _center = p;
    }
    public Point3d getCenter() {
        return (Point3d) _center.clone();
    }
    public EVertex getU() {
        return _u;
    }
    public EVertex getV() {
        return _v;
    }
}

class EVertex {
    private Point3d _position;
    private GemColor _color;
    public EVertex(Point3d position, GemColor color) {
        _position = position;
        _color = color;
    }
    public Point3d getPosition() {
        return (Point3d) _position.clone();
    }
    public GemColor getColor() {
        return _color;
    }
}
