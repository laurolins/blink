package blink;

import java.awt.Dimension;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

/**
 * <p>Title: Blink Cyclic Representation</p>
 */
public class BlinkCR {
    public BlinkCR() {
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
        int[][] vertices = {{1,2,3,4,5,18,17,16},{6,7,8,9,10,19,20,18},{11,12,13,14,15,16,21,19},{21,17,20},
                           {1},
                           {2},
                           {3},
                           {4},
                           {5},
                           {6},
                           {7},
                           {8},
                           {9},
                           {10},
                           {11},
                           {12},
                           {13},
                           {14},
                           {15}
        };
        int reds[] = {};


        // int[][] vertices = {{1,2},{1},{2}};
        // int reds[] = {1};
        // int[][] vertices = {{1,2},{4,3,1},{2,3,5},{4,5}};
        // int reds[] = {};
        // int[][] vertices = {{1,2},{2,3},{3,4},{4,1}};
        // int reds[] = {};
        // int[][] vertices = {{1,2,5,6},{6,5,3,4},{4,3,2,1}};
        // int reds[] = {5,6};
        // int[][] vertices = {{1,2,3,4,5,6},{9,8,7,3,2,1},{6,5,4,7,8,9}};
        // int reds[] = {};
        // int[][] vertices = {{1,2},{2,3},{3,1}};
        // int reds[] = {};
        // int[][] vertices = {{1,5,4,2,1},{3,2},{3,4,5}};
        // int reds[] = {1,4};
        // int[][] vertices = {{1},{1,2,3},{2,4,5},{5,6},{4},{6,3}};
        // int reds[] = {1};
        /*
                 int[][] vertices = {
                           {1},
                           {2},
                           {5},
                           {6},
                           {8},
                           {9},
                           {1,2,3,4},
                           {3,5,6,7},
                           {4,7,8,9}
                 };
                 int reds[] = {1,2,5,6,8,9};*/


        //int[][] vertices = { {1,3,2}, {5,4,3}, {2,4,6,8}, {1,8,7}, {7,6,5}};
        //int reds[] = {};

        //int[][] vertices = { {1}, {6}, {1,2,3}, {3,4}, {4,5}, {2,6,5} };
        //int reds[] = {};
        GBlink b = new GBlink(vertices, reds);

        b.goToCodeLabelAndDontCareAboutSpaceOrientation();
        b.write();

        ArrayList<MapD> list = new ArrayList<MapD>();
        list.add(new MapD(b));

        // desenhar o mapa
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(150, 150));
        f.setContentPane(new DrawPanelMultipleMaps(list, 1, 1));
        f.setVisible(true);
        // desenhar o mapa

        QI myqi = b.optimizedQuantumInvariant(3, 15);
        myqi.print();



        BlinkDB db = (BlinkDB) App.getRepositorio();

        long t0 = System.currentTimeMillis();

        QIRepository R = new QIRepository();
        HashMap<BlinkEntry, QI> _map = new HashMap<BlinkEntry, QI>();

        long minmax[] = db.getMinMaxBlinkIDs();
        int delta = 10000;
        long k = minmax[0];
        while (k <= minmax[1]) {

            // System.out.println(String.format("From id %d to id %d", k,k+delta-1));


            long t = System.currentTimeMillis();
            ArrayList<QI> bs = db.getQIByIDInterval(k, k+delta-1);
            // System.out.println(String.format("Retrieved %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            t = System.currentTimeMillis();


            for (QI qi : bs) {
                if (qi.isInNeighborhood(4, myqi.getEntryByR(4).get_real(), myqi.getEntryByR(4).get_imaginary()) &&
                    qi.isInNeighborhood(5, myqi.getEntryByR(5).get_real(), myqi.getEntryByR(5).get_imaginary()))
                    System.out.println("QI: "+qi.get_id());
            }

            // update index
            k = k+delta;
        }
        System.out.println(String.format("Total Time to calculate QIs %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));
        // System.exit(0);



    }
}
