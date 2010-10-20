package blink;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

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
public class SortBlinkIDs {
    public SortBlinkIDs() throws SQLException {
        int offset = 0;
        App.getRepositorio().insertOffsetOnBlinkIds(offset);
        ArrayList<BlinkEntry> bes = App.getRepositorio().getBlinks(0,100);
        for (int i=0;i<bes.size()-1;i++) {
            bes.get(i).set_id(bes.get(i).get_id()+offset);
        }
        Collections.sort(bes);
        App.getRepositorio().updateBlinksIDs(bes);
        System.exit(0);
    }
    public static void main(String[] args) throws SQLException {
        new SortBlinkIDs();
    }
}
