package blink;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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
public class FindDifferentHGNormQIClassesWithTheSameMingem {
    public FindDifferentHGNormQIClassesWithTheSameMingem() throws IOException, ClassNotFoundException, SQLException {
        ArrayList<ClassHGNormQI> list = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);

        // mount map
        HashMap<Long,ArrayList<ClassHGNormQI>> map = new HashMap<Long,ArrayList<ClassHGNormQI>>();
        for (ClassHGNormQI c: list) {
            for (Long id: c.getMinGemIDs()) {
                ArrayList<ClassHGNormQI> value = map.get(id);
                if (value == null) {
                    value = new ArrayList<ClassHGNormQI>();
                    map.put(id,value);
                }
                value.add(c);
            }
        }


        // Log the problems
        for (long id: map.keySet()) {
            ArrayList<ClassHGNormQI> value = map.get(id);
            if (value.size() <= 1)
                continue;
            String st = "Same MinGem on "+value.size()+" HG x Norm(QI) classes: ";
            for (ClassHGNormQI c: value) {
                st+=c.getStringOfQIs();
                st+=" - ";
            }
            System.out.println(st);
        }




    }
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        //App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);


        new FindDifferentHGNormQIClassesWithTheSameMingem();

        //BlinkEntry b1 = App.getRepositorio().getBlinkByQI(App.MAX_EDGES,132);
        //BlinkEntry b2 = App.getRepositorio().getBlinkByQI(App.MAX_EDGES,777);

        //QI q1 = b1.getBlink().optimizedQuantumInvariant(3,12);
        //QI q2 = b2.getBlink().optimizedQuantumInvariant(3,12);

        /*
        QI q1 = App.getRepositorio().getQIs(777);
        QI q2 = App.getRepositorio().getQIs(154);

        System.out.println("QI 1:\n"+q1.toString());
        System.out.println("QI 2:\n"+q2.toString());
        System.out.println("compareNormalizedEntries = "+q1.compareNormalizedEntries(q2));*/


        System.exit(0);

    }
}
