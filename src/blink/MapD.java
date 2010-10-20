package blink;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import javax.swing.JPanel;

public class MapD {

    private ArrayList<VertexD> _vertices = new ArrayList<VertexD>();
    private ArrayList<IncidentPointD> _borderIPs = new ArrayList<IncidentPointD>();
    private HashMap<Integer, IncidentPointD> _mapMapNodeLabel2IP = new HashMap<Integer, IncidentPointD>();

    private NodeD _root;

    private GBlink _map;
    private MapWord _mapWord;
    private String _homology;

    // bounding ball
    private double _centerX;
    private double _centerY;
    private double _radius;

    private double _minX = Double.POSITIVE_INFINITY;
    private double _minY = Double.POSITIVE_INFINITY;

    private double _maxX = Double.NEGATIVE_INFINITY;
    private double _maxY = Double.NEGATIVE_INFINITY;


    private boolean _color = true;
    public boolean getColor() {
        return _color;
    }
    public void setColor(boolean c) {
        _color = c;
    }

    public GBlink getBlink() {
        return _map;
    }

    private void initPosition(
            NodeD n,
            int level,
            double x,
            double y,
            double theta,
            double thetaMin,
            double thetaMax) {
        n.initPosition(level, x, y , theta, thetaMin, thetaMax);
        if (x < _minX) _minX = x;
        if (y < _minY) _minY = y;
        if (x > _maxX) _maxX = x;
        if (y > _maxY) _maxY = y;
    }

    public boolean isSkeleton(int x, int y) {
        IncidentPointD ip =_mapMapNodeLabel2IP.get(x);
        IncidentPointD ipTarget =_mapMapNodeLabel2IP.get(y);
        if (ip.isDescendent(ipTarget) || ipTarget.isDescendent(ip))
            return true;
        else
            return false;
    }

    public double angle(IncidentPointD ip) {
        return angle(ip.getX(),ip.getY());
    }

    public double angle(double x, double y) {
        double ux = x - _centerX;
        double uy = y - _centerY;
        double vx = 1;
        double vy = 0;
        double costheta = (ux * vx + uy * vy)/(Math.sqrt(ux*ux+uy*uy) * Math.sqrt(vx*vx+vy*vy));
        double theta = Math.acos(costheta);
        double thetaInDegrees = (theta * 180)/Math.PI;
        if (uy < 0)
            theta = 2*Math.PI - theta;
        return theta;
    }


    public MapD(GBlink map) {
        _map = map;
        _mapWord = _map.getMapWord();
        HomologyGroup hg = map.homologyGroupFromGem();
        _homology = (hg != null ? hg.toString().trim() : "");


        // criar objetos VertexD e IncidentPointD
        // preparar um mapa "Label -> IncidentPoint"
        ArrayList<ArrayList<Integer>> vertices = _mapWord.getVertices();
        for (ArrayList<Integer> list: vertices) {
            VertexD v = addVertex();
            for (int i=0;i<list.size();i+=2) {
                int mapNodeLabel1 = list.get(i);
                int mapNodeLabel2 = list.get(i+1);
                IncidentPointD ip = v.addIncidentPoint(mapNodeLabel1,mapNodeLabel2);
                _mapMapNodeLabel2IP.put(mapNodeLabel1,ip);
                _mapMapNodeLabel2IP.put(mapNodeLabel2,ip);
            }
        }

        // setar os vizinhos dos IncidentPoints
        for (int mapNodeLabel: _mapMapNodeLabel2IP.keySet()) {
            IncidentPointD ipSource = _mapMapNodeLabel2IP.get(mapNodeLabel);
            IncidentPointD ipTarget = _mapMapNodeLabel2IP.get(_mapWord.getNeighbour(mapNodeLabel,GBlinkEdgeType.vertex));
            ipSource.setNeighbour(ipTarget);
        }

        if (_vertices.size() == 0)
            return;

        //
        LinkedList<NodeD> queue = new LinkedList<NodeD>();

        // Criar uma spanning tree
        // através de uma busca em largura a partir
        // do vértice 0 e setar os parâmetros
        // para a representação gráfica
        _root = _vertices.get(0);
        this.initPosition(_root,0,0,0,0,0,2*Math.PI);
        _root.setMark(true);
        queue.add(_root);

        while (!queue.isEmpty()) {
           NodeD node = queue.poll();
           int level = node.getLevel();

           // VertexD
           if (node instanceof VertexD) {
               VertexD v = (VertexD) node;

               ArrayList<IncidentPointD> ips = v.getIncidentPoints();

               // adjust ips to make the cyclic order of the incident
               // points correct!
               if (level > 0) {
                   IncidentPointD ip = (IncidentPointD) v.getParent();
                   v.getParent();
                   int index = ips.indexOf(ip);
                   ArrayList<IncidentPointD> aux = new ArrayList<IncidentPointD>();
                   aux.addAll(ips.subList(index,ips.size()));
                   aux.addAll(ips.subList(0,index));
                   ips = aux;
               }

               int n = (level == 0 ? ips.size() : ips.size()-1);
               int index = 0;
               if ( n > 0 ) {
                   for (IncidentPointD ip : ips) {
                       // node is marked
                       if (ip.isMarked())
                           continue;

                       double intervalSize = (v.getMaxTheta() - v.getMinTheta()) / (double) n;
                       double minTheta = v.getMinTheta() + index * intervalSize;
                       double maxTheta = minTheta + intervalSize;
                       double theta = (maxTheta + minTheta) / 2.0;
                       index++;
                       v.addChild(ip);
                       this.initPosition(ip, level, v.getX(), v.getY(), theta, minTheta, maxTheta);
                       ip.setMark(true);
                       queue.add(ip);
                   }
               }
           }

           // IncidentPointD
           else if (node instanceof IncidentPointD) {
               IncidentPointD ip =(IncidentPointD) node;
               IncidentPointD ipTarget = ip.getNeighbour();
               VertexD v = ipTarget.getVertex();
               if (!v.isMarked()) {
                   // add incident point
                   double minTheta = ip.getMinTheta();
                   double maxTheta = ip.getMaxTheta();
                   double theta = (maxTheta + minTheta) / 2.0;

                   if (maxTheta - minTheta - Math.PI * 2 < 1.0e-2 && maxTheta - minTheta - Math.PI * 2 > -1.0e-2) {
                       minTheta = theta-Math.PI/2;
                       maxTheta = theta+Math.PI/2;
                   }

                   ip.addChild(ipTarget);

                   double x = ip.getX() + 1 * Math.cos(ip.getTheta());
                   double y = ip.getY() + 1 * Math.sin(ip.getTheta());

                   this.initPosition(ipTarget, level+1, x, y, theta + Math.PI, 0, 0); // skeleton doesn't have an angle range neither a theta
                   ipTarget.setMark(true);

                   // add vertex
                   ipTarget.addChild(v);
                   this.initPosition(v, level+1, x, y, theta, minTheta, maxTheta);
                   v.setMark(true);
                   queue.add(v);
               }
           }
        }

        { // calculate bounding ball
            _centerX = 0;
            _centerY = 0;
            for (VertexD v : _vertices) {
                _centerX += v.getX();
                _centerY += v.getY();
            }
            _centerX = _centerX / _vertices.size();
            _centerY = _centerY / _vertices.size();
            _radius = 0;
            for (VertexD v : _vertices) {
                double dx = v.getX() - _centerX;
                double dy = v.getY() - _centerY;
                double r = Math.sqrt(dx * dx + dy * dy);
                _radius = Math.max(r, _radius);
            }
            _radius = _radius+1;
        } // calculate bounding ball

        { // project the non skeleton incident points to the border and initialize _borderIPs
            HashSet<NodeD> set = new HashSet<NodeD>();
            for (VertexD v : _vertices) {
                for (IncidentPointD ipSource : v.getIncidentPoints()) {
                    if (set.contains(ipSource))
                        continue;
                    IncidentPointD ipTarget = ipSource.getNeighbour();
                    set.add(ipTarget);
                    set.add(ipSource);
                    if (!(ipTarget.isDescendent(ipSource) || ipSource.isDescendent(ipTarget))) {
                        _borderIPs.add(ipSource);
                        _borderIPs.add(ipTarget);
                        { //
                            IncidentPointD ip = ipSource;
                            double ix = ip.getX();
                            double iy = ip.getY();
                            double theta = ip.getTheta();
                            intersect(ix,iy,theta);
                            this.initPosition(ip, ip.getLevel(), _x, _y, ip.getTheta(), ip.getMinTheta(),ip.getMaxTheta());

                            // set ball angle
                            ip.setBallAngle(this.angle(ip));
                        }

                        { //
                            IncidentPointD ip = ipTarget;
                            double ix = ip.getX();
                            double iy = ip.getY();
                            double theta = ip.getTheta();
                            intersect(ix,iy,theta);
                            this.initPosition(ip, ip.getLevel(), _x, _y, ip.getTheta(), ip.getMinTheta(),ip.getMaxTheta());

                            // set ball angle
                            ip.setBallAngle(this.angle(ip));
                        }
                    }
                }
            }
        } // project the non skeleton incident points to the border


        { // sort border IPs by the angle
            Collections.sort(_borderIPs,new Comparator() {
                public int compare(Object o1, Object o2) {
                    IncidentPointD ip1 = (IncidentPointD) o1;
                    IncidentPointD ip2 = (IncidentPointD) o2;
                    if (ip1.getBallAngle() < ip2.getBallAngle()) return -1;
                    else if (ip2.getBallAngle() < ip1.getBallAngle()) return  1;
                    else return 0;
                }
            });
        } // sort border IPs by the angle

        { // set
            int index = 0;
            for (IncidentPointD ip: _borderIPs)
                ip.setBallIndex(index++);
        }
    }

    public void intersect(double ix, double iy, double theta) {
        intersect(ix, iy, theta, _radius);
    }

    public void intersect(double ix, double iy, double theta, double radius) {

        double cx = _centerX;
        double cy = _centerY;
        double r = radius;

        // is it a vetical
        if (Math.abs(theta - Math.PI/2.0) < 1.0e-4) {
            _x = ix;
            _y = cy + Math.sqrt(r*r- (_x-cx)*(_x-cx));
        }
        else if (Math.abs(theta - 3*Math.PI/2.0) < 1.0e-4) {
            _x = ix;
            _y = cy - Math.sqrt(r*r- (_x-cx)*(_x-cx));
        }
        else {

            double ttheta = Math.tan(theta);
            double ttheta2 = ttheta * ttheta;
            double r2 = r * r;

            double miolo = Math.sqrt( -ttheta2 * cx * cx
                                     + ttheta2 * r2 + 2 * cy * iy + r2 -
                                     iy * iy -
                                     cy * cy + 2 * iy * ttheta * ix -
                                     2 * cy * ttheta * ix -
                                     ttheta2 * ix * ix - 2 * cx * ttheta * iy +
                                     2 * cx * cy * ttheta +
                                     2 * cx * ttheta2 * ix);

            double x = 0;
            if (theta % (Math.PI * 2) > Math.PI / 2 &&
                theta % (Math.PI * 2) < 3 * Math.PI / 2)
                x = (cx - iy * ttheta + cy * ttheta + ttheta2 * ix - miolo) /
                    (ttheta2 + 1);
            else
                x = (cx - iy * ttheta + cy * ttheta + ttheta2 * ix + miolo) /
                    (ttheta2 + 1);
            double y = iy + ttheta * x - ttheta * ix;

            _x = x;
            _y = y;
        }
    }




    public VertexD addVertex() {
        VertexD v = new VertexD(_vertices.size());
        _vertices.add(v);
        return v;
    }

    private double _points[] = {0,0,0,0};
    private AffineTransform _transform = new AffineTransform();
    private double _x=0, _y=0;
    public void setupTransform(double w, double h) {
        _transform = new AffineTransform();
        double marginX = 0.15 * w;
        double marginY = 0.15 * h;
        // double scale = Math.min((w - 2*marginX)/(_maxX-_minX),(h - 2*marginY)/(_maxY-_minY));
        double scale = Math.min((w - 2*marginX)/(2*_radius),(h - 2*marginY)/(2*_radius));
        _transform.translate(marginX,marginY);
        _transform.scale(scale,scale);
        //_transform.translate(-_minX,-_minY);
        _transform.translate(-_centerX+_radius,-_centerY+_radius);
    }

    public Point2D.Double transform(double x, double y) {
        _points[0] = x; _points[1] = y;
        _transform.transform(_points,0,_points,0,1);
        _x = _points[0]; _y = _points[1];
        return new Point2D.Double(_x,_y);
    }

    public void drawHardword(Graphics2D g, double w, double h) {
        g.drawString(_homology,5,14);

        // drawing base case...
        if (_map.getNumberOfGEdges() == 0) {
            double r = 3.2;
            java.awt.geom.Ellipse2D circle = new java.awt.geom.Ellipse2D.Double(w/2.0 - r , h/2.0 - r, 2 * r, 2 * r);
            g.setColor(Color.black);
            g.fill(circle);
            return;
        }


        // setup transform
        this.setupTransform(w, h);

        int k = 1;

        ArrayList<ConnectionD> connections = new ArrayList<ConnectionD>();

        // edges
        HashSet<NodeD> set = new HashSet<NodeD>();
        for (VertexD v : _vertices) {
            for (IncidentPointD ip : v.getIncidentPoints()) {
                if (set.contains(ip))
                    continue;
                IncidentPointD ipTarget = ip.getNeighbour();
                set.add(ipTarget);
                set.add(ip);
                if (!(ipTarget.isDescendent(ip) || ip.isDescendent(ipTarget))) {
                    connections.add(new ConnectionD(ip, ipTarget));
                }
            }
        }
        /*
                     Collections.sort(connections,new Comparator() {
            public int compare(Object o1, Object o2) {
                ConnectionD c1 = (ConnectionD) o1;
                ConnectionD c2 = (ConnectionD) o2;
                if (c1.getPathSize() < c2.getPathSize())
                    return -1;
                else if (c2.getPathSize() < c1.getPathSize())
                    return 1;
                else return c1.getMinLabel() - c2.getMinLabel();
            }
                     });*/

        // draw boundin ball
        {
            transform(_centerX, _centerY);
            double tcx = _x;
            double tcy = _y;
            transform(_centerX + _radius, _centerY);
            double tRadius = 0.99 * Math.abs(_x - tcx);
            java.awt.geom.Ellipse2D circle = new java.awt.geom.Ellipse2D.Double(tcx - tRadius, tcy - tRadius,
                                                                                2 * tRadius, 2 * tRadius);
            g.setColor(Color.white);
            g.fill(circle);
            g.setColor(new java.awt.Color(220, 220, 220));
            g.draw(circle);
        }

        // draw connections
        for (int j = 0; j < connections.size(); j++) {
            ConnectionD c = connections.get(j);

            IncidentPointD p1 = c.getMinBallAngleIP();
            IncidentPointD p2 = c.getMaxBallAngleIP();

            /*
                             { // draw labels
                this.transform(p1.getX(),p1.getY());
                g.setColor(Color.MAGENTA);
             g.drawString( String.format("%d, %.0f",p1.getBallIndex(),p1.getBallAngleInDegrees()), (int) (_x + 5), (int) (_y - 5));

                this.transform(p2.getX(),p2.getY());
                g.setColor(Color.MAGENTA);
             g.drawString( String.format("%d, %.0f",p2.getBallIndex(),p2.getBallAngleInDegrees()), (int) (_x + 5), (int) (_y - 5));
                             } // draw labels
             */

            int n = _borderIPs.size();
            int ballDistanceCCW = (int) Math.abs(p1.getBallIndex() - p2.getBallIndex());
            int ballDistanceCW = (int) Math.abs(n - ballDistanceCCW);

            // ball distance
            int ballDistance = Math.min(ballDistanceCW, ballDistanceCCW);

            // radius
            double r = _radius + ballDistance * _radius * 0.035;

            // intersect
            intersect(p1.getX(), p1.getY(), p1.getTheta(), r);
            double p1x = _x;
            double p1y = _y;
            intersect(p2.getX(), p2.getY(), p2.getTheta(), r);
            double p2x = _x;
            double p2y = _y;

            {
                Line2D.Double line = new Line2D.Double(transform(p1.getX(), p1.getY()), transform(p1x, p1y));
                g.setColor(getColor(p1));
                g.draw(line);
                line = new Line2D.Double(transform(p2.getX(), p2.getY()), transform(p2x, p2y));
                g.setColor(getColor(p1));
                g.draw(line);
            }

            // angles
            double a1 = this.angle(p1x, p1y);
            double a2 = this.angle(p2x, p2y);

            // System.out.println(String.format("%d angle: %.0f", p1.getBallIndex(), Math.toDegrees(a1)));
            // System.out.println(String.format("%d angle: %.0f", p2.getBallIndex(), Math.toDegrees(a2)));

            // System.out.println(
            //        String.format("%d - normal: %6.2f %6.2f angle: %6.2f %6.2f", p1.getBallIndex(), p1x, p1y,
            //                      _centerX + r * Math.cos(a1), _centerY + r * Math.sin(a1)));
            // System.out.println(
            //        String.format("%d - normal: %6.2f %6.2f angle: %6.2f %6.2f", p2.getBallIndex(), p2x, p2y,
            //                      _centerX + r * Math.cos(a2), _centerY + r * Math.sin(a2)));

            double start = a1;
            double extent = a2 - a1;
            if (ballDistanceCW < ballDistanceCCW) {
                start = a2;
                extent = 2 * Math.PI - (a2 - a1);
            }

            transform(_centerX, _centerY);
            double tcx = _x;
            double tcy = _y;

            transform(_centerX + r, _centerY);
            double tr = _x - tcx;

            g.setColor(getColor(p1));
            java.awt.geom.Arc2D.Double circle = new java.awt.geom.Arc2D.Double(tcx - tr,
                                                                               tcy - tr, 2 * tr, 2 * tr,
                                                                               -Math.toDegrees(start),
                                                                               -Math.toDegrees(extent),
                                                                               java.awt.geom.Arc2D.OPEN);
            g.draw(circle);
        }

        // draw spanning tree edges
        if (_root != null) {
            Stack<NodeD> S = new Stack<NodeD>();
            S.push(_root);
            while (!S.isEmpty()) {
                NodeD n = S.pop();

                // push all childs
                for (NodeD child : n.getChilds())
                    S.push(child);

                if (n.getParent() != null && n instanceof IncidentPointD) {
                    IncidentPointD ip = (IncidentPointD) n;
                    NodeD p = (NodeD) n.getParent();

                    g.setColor(getColor(ip));
                    Line2D.Double line = new Line2D.Double(transform(p.getX(), p.getY()), transform(n.getX(), n.getY()));
                    g.draw(line);
                    g.setColor(Color.black);

                    Point2D.Double pos1 = transform(p.getX(), p.getY());
                    Point2D.Double pos2 = transform(n.getX(), n.getY());
                    if (!(Math.abs(pos1.getX() - pos2.getX()) <=1e-1 && Math.abs(pos1.getY() - pos2.getY()) <=1e-1)) {
                        int x = (int) ((pos1.getX() + pos2.getX()) / 2.0);
                        int y = (int) ((pos1.getY() + pos2.getY()) / 2.0);
                        int edgeLabel = GBlink.getGEdgeLabelFromMapVertexLabel(ip.getMapNodeLabel1());
                        g.drawString("" + edgeLabel, x, y);
                    }


                }
            }
        }

        // draw vertices
        for (VertexD v : _vertices) {
            this.transform(v.getX(), v.getY());
            for (IncidentPointD ip : v.getIncidentPoints()) {
                //if (ip.isSkeleton())
                //    continue;

                double r = 10;
                transform(v.getX(), v.getY());
                Line2D.Double line = new Line2D.Double(
                        new java.awt.geom.Point2D.Double(v.getX(), v.getY()),
                        new java.awt.geom.Point2D.Double(ip.getX(), ip.getY()));

                // g.setColor(Color.cyan);
                // g.draw(line);
                /*
                                g.setColor(Color.magenta);
                                g.drawString("" + ip.getMapNodeLabel1()+","+
                                             ip.getMapNodeLabel2(),
                                             (int) (_x + 5*r * Math.cos(ip.getTheta())),
                                             (int) (_y + 5*r * Math.sin(ip.getTheta())));
                 */

            }
            {
                double r = 3.2;
                java.awt.geom.Ellipse2D circle = new java.awt.geom.Ellipse2D.Double(_x - r, _y - r, 2 * r, 2 * r);
                g.setColor(Color.black);
                g.fill(circle);
                //g.setColor(Color.RED);
                //g.draw(circle);

                //g.setColor(Color.black);
                //g.drawString("" + ((VertexD) v).getLabel(), (int) (_x + r), (int) (_y - r));
            }
        }
    }

    public void draw(Graphics2D g, double w, double h, double x0, double y0) {
        g.translate(x0,y0);
        this.drawHardword(g,w,h);
        g.translate(-x0,-y0);
    }

    /**
     * getColor
     */
    private Color getColor(IncidentPointD ip) {
        int blinkEdgeLabel;
        if (ip.getMapNodeLabel1() % 4 == 0)
            blinkEdgeLabel = ip.getMapNodeLabel1() / 4;
        else
            blinkEdgeLabel = ip.getMapNodeLabel1() / 4 + 1;

        if (this.getColor()) {
            BlinkColor c = _map.getColor(blinkEdgeLabel);
            if (c == BlinkColor.green)
                return Color.green;
            else
                return Color.red;
        } else {
            return Color.black;
        }
    }

    // inner class
    class ConnectionD {
        IncidentPointD _p1;
        IncidentPointD _p2;
        public IncidentPointD getP1() {
            return _p1;
        }
        public IncidentPointD getP2() {
            return _p2;
        }
        public IncidentPointD getMinBallAngleIP() {
            if (_p1.getBallAngle() < _p2.getBallAngle())
                return _p1;
            else
                return _p2;
        }
        public IncidentPointD getMaxBallAngleIP() {
            if (_p1.getBallAngle() < _p2.getBallAngle())
                return _p2;
            else
                return _p1;
        }
        public ConnectionD(IncidentPointD p1, IncidentPointD p2) {
            _p1 = p1;
            _p2 = p2;
        }
        public int getMinLabel() {
            int labels[] = {_p1.getMapNodeLabel1(),_p1.getMapNodeLabel2(),_p2.getMapNodeLabel1(),_p2.getMapNodeLabel1()};
            Arrays.sort(labels);
            return labels[0];
        }
    }
}



class NodeD {
    private ArrayList<NodeD> _childs = new ArrayList<NodeD>();
    private boolean _marked = false;
    private int _level;

    private double _x;
    private double _y;

    private boolean _skeleton;

    private double _theta;
    private double _thetaMin;
    private double _thetaMax;
    private NodeD _parent;
    public void initPosition(
            int level,
            double x,
            double y,
            double theta,
            double thetaMin,
            double thetaMax) {
        _level = level;
        _x = x;
        _y = y;
        _theta = theta;
        _thetaMin = thetaMin;
        _thetaMax = thetaMax;
    }

    public double getTheta() {
        return _theta;
    }

    public double getX() {
        return _x;
    }

    public double getY() {
        return _y;
    }

    public double getMinTheta() {
        return _thetaMin;
    }

    public double getMaxTheta() {
        return _thetaMax;
    }

    public NodeD getParent() {
        return _parent;
    }

    public void setParent(NodeD parent) {
        _parent = parent;
    }

    public ArrayList<NodeD> getChilds() {
        return (ArrayList<NodeD>) _childs.clone();
    }

    public void addChild(NodeD n) {
        n.setParent(this);
        _childs.add(n);
    }

    public boolean isSkeleton() {
        return (_parent instanceof IncidentPointD);
    }

    public boolean isMarked() {
        return _marked;
    }

    public void setMark(boolean m) {
        _marked = m;
    }

    public int getLevel() {
        return _level;
    }

    public boolean isDescendent(NodeD n) {
        NodeD v = this;
        while(v != null) {
            if (v == n) {
                return true;
            }
            else v = v.getParent();
        }
        return false;
    }
}

class VertexD extends NodeD {
    private int _label;
    private ArrayList<IncidentPointD> _incidentPoints = new ArrayList<IncidentPointD>();
    public VertexD(int label) {
        _label = label;
    }
    public IncidentPointD addIncidentPoint(int mapNodeLabel1, int mapNodeLabel2) {
        IncidentPointD ip = new IncidentPointD(this,_incidentPoints.size(),mapNodeLabel1,mapNodeLabel2);
        _incidentPoints.add(ip);
        return ip;
    }

    public int getLabel() {
        return _label;
    }

    public ArrayList<IncidentPointD> getIncidentPoints() {
        return (ArrayList<IncidentPointD>) _incidentPoints.clone();
    }

    public int getNumberOfIncidentPoints() {
        return _incidentPoints.size();
    }

}

class IncidentPointD extends NodeD {

    private VertexD _vertex;
    private int _index;
    private int _mapNodeLabel1;
    private int _mapNodeLabel2;

    private double _ballAngle;
    public void setBallAngle(double ba) {
        _ballAngle = ba;
    }
    public double getBallAngle() {
        return _ballAngle;
    }

    public double getBallAngleInDegrees() {
        return _ballAngle*180/Math.PI;
    }

    private int _ballIndex = -1; // not on the ball border
    public void setBallIndex(int i) {
        _ballIndex = i;
    }
    public int getBallIndex() {
        return _ballIndex;
    }


    public IncidentPointD(VertexD vertex, int index, int mapNodeLabel1, int mapNodeLabel2) {
        _vertex = vertex;
        _index = index;
        _mapNodeLabel1 = mapNodeLabel1;
        _mapNodeLabel2 = mapNodeLabel2;
    }

    public VertexD getVertex() {
        return _vertex;
    }

    public int getIndex() {
        return _index;
    }

    private IncidentPointD _neighbour;

    public void setNeighbour(IncidentPointD neighbour) {
        _neighbour = neighbour;
    }

    public IncidentPointD getNeighbour() {
        return _neighbour;
    }

    public int getMapNodeLabel1() {
        return _mapNodeLabel1;
    }

    public int getMapNodeLabel2() {
        return _mapNodeLabel2;
    }
}



class DrawPanelMultipleMaps extends JPanel {
    ArrayList<MapD> _mapsD;
    int _rows;
    int _columns;
    public DrawPanelMultipleMaps(ArrayList<MapD> mapsD, int rows, int columns) {
        super();
        _mapsD = (ArrayList<MapD> )mapsD.clone();
        _rows = rows;
        _columns = columns;
    }

    public MapD getMapFromPosition(int x, int y) {
        double side = Math.min((double)this.getHeight()/_rows,(double)this.getWidth()/_columns);
        int row = (int) (y/side);
        int col = (int) (x/side);
        int index = row * _columns + col;
        System.out.println(String.format("%3d x %3d -> %3d",x,y,index));
        if (index < _mapsD.size())
            return _mapsD.get(index);
        else return null;
    }

    public void paint(Graphics g) {
        super.paint(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double side = Math.min((double)this.getHeight()/_rows,(double)this.getWidth()/_columns);
        int k = 0;
        for (int i=0;i<_rows;i++) {
            for (int j=0;j<_columns;j++) {
                if (k < _mapsD.size()) {
                    _mapsD.get(k++).draw((Graphics2D) g,side,side,j*side,i*side);
                }
            }
        }
        // _mapD.draw((Graphics2D) g, this.getWidth(),this.getHeight());
    }
}
