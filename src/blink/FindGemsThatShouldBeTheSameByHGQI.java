package blink;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class FindGemsThatShouldBeTheSameByHGQI {

    StringBuffer _result = new StringBuffer();

    public FindGemsThatShouldBeTheSameByHGQI() throws SQLException, FileNotFoundException, IOException,
            ClassNotFoundException {


        PrintWriter pw = new PrintWriter("res/gemsToFindPath.txt");
        ArrayList<ClassHGNormQI> list = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);
        StringBuffer sb = new StringBuffer();
        int i=1;
        for (ClassHGNormQI c: list) {
            HashSet<Long> ids = new HashSet<Long>();
            c.load();
            for (BlinkEntry b: c.getBlinks()) {
                if (b.getMinGem() != -1L || b.getMinGem() != 0L)
                    ids.add(b.getMinGem());
            }
            if (ids.size() < 2)
                continue;

            sb.setLength(0);
            sb.append("index: "+(i++)+"\thg: "+c.get_hg()+"\tqi: "+c.getStringOfQIs()+" -> \t");
            for (Long l: ids) {
                sb.append(l+" ");
            }
            System.out.println(""+sb.toString());
            _result.append(sb.toString()+"\n");
            pw.println(sb.toString());
            pw.flush();
        }
        pw.close();
    }

    public String getResult() {
        return _result.toString();
    }

    public static void main(String[] args) throws SQLException, FileNotFoundException, ClassNotFoundException,
            IOException {
        new FindGemsThatShouldBeTheSameByHGQI();
        System.exit(0);
    }
}
