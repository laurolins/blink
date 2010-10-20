package blink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class GemFromBlink {

    public HashMap<GBlinkVertex,GemVertex> _inside = new HashMap<GBlinkVertex,GemVertex>();
    public HashMap<GBlinkVertex,GemVertex> _outside = new HashMap<GBlinkVertex,GemVertex>();
    public HashMap<GemVertex,GBlinkVertex> _blinkMate = new HashMap<GemVertex,GBlinkVertex>();
    public void setNeighbours(GemVertex u, GemVertex v, GemColor color) {
        v.setNeighbour(u,color);
        u.setNeighbour(v,color);
    }

    Gem _gem = new Gem();

    public GemFromBlink(GBlink blink) {

        int label = 1;
        ArrayList<GBlinkVertex> vs = blink.getVertices();
        Collections.sort(vs);
        for (GBlinkVertex v: vs) {
            GemVertex vi = _gem.newVertex(label);
            GemVertex vo = _gem.newVertex(label+1);
            _inside.put(v,vi);
            _outside.put(v,vo);
            _blinkMate.put(vi,v);
            _blinkMate.put(vo,v);
            label+=2;
        }

        //
        int n = blink.getNumberOfGEdges();
        for (int i=1;i<=n;i++) {

            int k = 1+(4*(i-1));

            GBlinkVertex a = blink.findVertex(k);
            GBlinkVertex b = a.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex d = c.getNeighbour(GBlinkEdgeType.face);

            // color
            BlinkColor color = blink.getColor(i);

            GemVertex ain = _inside.get(a);
            GemVertex aout = _outside.get(a);
            GemVertex bin = _inside.get(b);
            GemVertex bout = _outside.get(b);
            GemVertex cin = _inside.get(c);
            GemVertex cout = _outside.get(c);
            GemVertex din = _inside.get(d);
            GemVertex dout = _outside.get(d);

            if (color == BlinkColor.green) {

                // green edges (green bridge is over the even vertices)
                setNeighbours(ain,aout,GemColor.green);
                setNeighbours(bin,dout,GemColor.green);
                setNeighbours(bout,din,GemColor.green);
                setNeighbours(cin,cout,GemColor.green);

                // 2 yellow edges (yellow edge is over de odd vertices)
                setNeighbours(ain,cout,GemColor.yellow);
                setNeighbours(aout,cin,GemColor.yellow);

            }
            else { // color == BlinkColor.red

                // green edges (green bridge is over the odd vertices)
                setNeighbours(ain,cout,GemColor.green);
                setNeighbours(bin,bout,GemColor.green);
                setNeighbours(aout,cin,GemColor.green);
                setNeighbours(din,dout,GemColor.green);

                // 2 yellow edges (yellow edge is over de even vertices)
                setNeighbours(bin,dout,GemColor.yellow);
                setNeighbours(bout,din,GemColor.yellow);

            }

            // red edges
            setNeighbours(ain,bin,GemColor.red);
            setNeighbours(bout,cout,GemColor.red);
            setNeighbours(cin,din,GemColor.red);
            setNeighbours(aout,dout,GemColor.red);

            // blue edges
            setNeighbours(ain,_inside.get(a.getNeighbour(GBlinkEdgeType.edge)),GemColor.blue);
            setNeighbours(aout,_outside.get(a.getNeighbour(GBlinkEdgeType.edge)),GemColor.blue);
            setNeighbours(bin,_inside.get(b.getNeighbour(GBlinkEdgeType.edge)),GemColor.blue);
            setNeighbours(bout,_outside.get(b.getNeighbour(GBlinkEdgeType.edge)),GemColor.blue);
            setNeighbours(cin,_inside.get(c.getNeighbour(GBlinkEdgeType.edge)),GemColor.blue);
            setNeighbours(cout,_outside.get(c.getNeighbour(GBlinkEdgeType.edge)),GemColor.blue);
            setNeighbours(din,_inside.get(d.getNeighbour(GBlinkEdgeType.edge)),GemColor.blue);
            setNeighbours(dout,_outside.get(d.getNeighbour(GBlinkEdgeType.edge)),GemColor.blue);
        }

        // initialize other yellow edges
        for (GemVertex v: _gem.getVertices()) {
            if (v.getYellow() != null)
                continue;

            GemVertex u = v.getBlue();

            GemColor c = GemColor.yellow;
            while (u.getYellow() != null) {
                u = u.getNeighbour(c);
                c = (c == GemColor.yellow ? GemColor.blue : GemColor.yellow);
            }

            if (u == v)
                throw new RuntimeException("Not a valid Gem.");

            this.setNeighbours(u, v, GemColor.yellow);
        }

        // set handle number of this gem
        _gem.setHandleNumber(blink.getNumberOfAlternatingZigZags());

    }

    public Gem getGem() {
        return _gem;
    }
}
