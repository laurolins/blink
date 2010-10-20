package blink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

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
public class Path {
    private ArrayList<Move> _moves;

    public Path() {
        _moves = new ArrayList<Move>();
    }

    public void reverse() {
        Collections.reverse(_moves);
    }

    public Path(String pathSt) {
        _moves = new ArrayList<Move>();
        StringTokenizer st = new StringTokenizer(pathSt,";");
        while (st.hasMoreTokens()) {
            String moveSt = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(moveSt," ");
            String moveType = st2.nextToken();

            if (moveType.equals("L")) { // labeling
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new RelabelMove(u,p));
            }
            else if (moveType.equals("D1")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new DipoleMove(u, p));
            }
            else if (moveType.equals("D2")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new DipoleMove(u, p));
            }
            else if (moveType.equals("D3")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new DipoleMove(u, p));
            }
            else if (moveType.equals("R2")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                int v = Integer.parseInt(st2.nextToken());
                this.addMove(new RhoMove(u,v,p[0],2));
            }
            else if (moveType.equals("R3")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                int v = Integer.parseInt(st2.nextToken());
                this.addMove(new RhoMove(u,v,p[0],3));
            }
            else if (moveType.equals("TS1")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new TSMove(u,p,TSMoveType.TS1));
            }
            else if (moveType.equals("TS2")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new TSMove(u,p,TSMoveType.TS2));
            }
            else if (moveType.equals("TS3")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new TSMove(u,p,TSMoveType.TS3));
            }
            else if (moveType.equals("TS4")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new TSMove(u,p,TSMoveType.TS4));
            }
            else if (moveType.equals("TS5")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new TSMove(u,p,TSMoveType.TS5));
            }
            else if (moveType.equals("TS6")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new TSMove(u,p,TSMoveType.TS6));
            }
            else if (moveType.equals("U")) {
                GemColor p[] = GemColor.parseColorsCompactString(st2.nextToken());
                int u = Integer.parseInt(st2.nextToken());
                this.addMove(new UMove(u, p[0]));
            }
        }
    }

    public Move getMove(int i) {
        return _moves.get(i);
    }

    public void addMove(Move m) {
        _moves.add(m);
    }

    public String toString() {
        return getSignature();
    }

    public String getSignature() {
        StringBuffer st = new StringBuffer();
        boolean first = true;
        for (Move m: _moves) {
            if (!first)
                st.append(";");
            st.append(m.getSignature());
            first = false;
        }
        return st.toString();
    }

    public int size() {
        return _moves.size();
    }


    public Gem getResultWhenAppliedTo(Gem g) {
        g = g.copy();

        Path p = this;
        for (int i=0;i<p.size();i++) {
            Move m = p.getMove(i);

            // System.out.println("applying "+m.getSignature());

            if (m instanceof DipoleMove) {
                DipoleMove dm = (DipoleMove) m;

                g = g.copy();
                GemVertex u = g.findVertex(dm.getU());
                Dipole d = new Dipole(u,dm.getColors());
                g.cancelDipole(d);

            }

            else if (m instanceof RhoMove) {
                RhoMove rm = (RhoMove) m;

                g = g.copy();
                RhoPair rp = new RhoPair(
                        g.findVertex(rm.getU()),
                        g.findVertex(rm.getV()),
                        rm.getColor(),
                        rm.foundAsA());
                g.applyRhoPair(rp);

            }

            else if (m instanceof TSMove) {
                TSMove tm = (TSMove) m;

                g = g.copy();
                TSMovePoint tsmp = new TSMovePoint(tm.getA(),tm.getP(),tm.getType());
                g.applyTSMove(tsmp);
                g.goToCodeLabel();

            }

            else if (m instanceof RelabelMove) {
                // RelabelMove rm = (RelabelMove) m;

                g = g.copy();
                g.goToCodeLabel();

            }

            else if (m instanceof UMove) {
                UMove um = (UMove) m;

                g = g.copy();

                Monopole monopole = new Monopole(g.findVertex(um.getA()),
                                                 um.getColor(),
                                                 0,0);
                g.uMove(monopole);
            }
        }

        return g;
    }

}
