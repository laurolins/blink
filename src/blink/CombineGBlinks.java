package blink;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

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
public class CombineGBlinks {

    ArrayList<GBlink> _base;

    ArrayList<GBlink> _result;


    public CombineGBlinks(ArrayList<GBlink> base, int max, String fileName) throws IOException {
        _base = base;

        long t0 = System.currentTimeMillis();

        ArrayList[] lists = new ArrayList[max+1];
        lists[1] = base;
        for (int ii=2;ii<=max;ii++) {
            lists[ii] = new ArrayList<GBlink>();
        }

        int count = lists[1].size();
        int maxNumGEdges=max;
        int k=2;
        boolean somethingNew = true;
        while (k<=max && somethingNew) {
            System.out.println(String.format("\nProcessing step %6d combined maps size %6d time elapsed %6.2f seg.",
                               k,
                               count,
                               (System.currentTimeMillis()-t0)/1000.0));
            somethingNew = false;
            int kk = 1;
            int ii = -1;

            for (GBlink B: (ArrayList<GBlink>) base) {
                ii++;
                System.out.print((kk++)+" ");
                if (kk % 100 == 0)
                    System.out.println("");
                ArrayList<GBlinkVertex> Bz = B.getOneVertexForEachGZigzag();
                int jj = -1;
                for (GBlink G : (ArrayList<GBlink>)lists[k - 1]) {
                    jj++;
                    if (B.getNumberOfGEdges() + G.getNumberOfGEdges() <= maxNumGEdges) {
                        ArrayList<GBlinkVertex> Gz = G.getOneVertexForEachGZigzag();
                        for (int i=0;i<Bz.size();i++) {
                            for (int j=0;j<Gz.size();j++) {
                                System.out.println("Combining "+ii+" with "+jj+" at level "+kk);
                                GBlink BB = B.copy();
                                GBlink GG = G.copy();
                                GBlinkVertex vBB = BB.findVertex(Bz.get(i).getLabel());
                                GBlinkVertex vGG = GG.findVertex(Gz.get(j).getLabel());
                                if (vBB.hasEvenLabel()) vBB = vBB.getNeighbour(GBlinkEdgeType.edge);
                                if (vGG.hasEvenLabel()) vGG = vGG.getNeighbour(GBlinkEdgeType.edge);
                                GBlink.merge(BB,vBB,GG,vGG);

                                if (!lists[k].contains(BB)) {
                                    lists[k].add(BB);
                                    somethingNew = true;
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
            k++;
        }

        System.out.println(String.format("\nFinished: combined maps size %6d time elapsed %6.2f seg.",
                           count,
                           (System.currentTimeMillis()-t0)/1000.0));

        //
        HashSet<GBlink> completeSet = new HashSet<GBlink>();
        for (int kk=1;kk<=max;kk++) {
            ArrayList<GBlink> list = (ArrayList<GBlink>) lists[kk];
            completeSet.addAll(list);
        }

        ArrayList<GBlink> completeList = new  ArrayList<GBlink>(completeSet);
        Collections.sort(completeList);
        _result = completeList;

        if (fileName != null) {
            System.out.println("Writing File...");
            PrintWriter pr = new PrintWriter(new FileWriter(fileName));
            for (GBlink G : completeList) {
                pr.println(G.getBlinkWord().toString());
                pr.flush();
            }
            pr.close();
        }
    }

    public ArrayList<GBlink> getResult() {
        return new ArrayList<GBlink>(_result);
    }


}
