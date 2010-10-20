package blink;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
public class BlinkCatalog {
    private HashMap<Long,BlinkEntry> _blinksMap = new HashMap<Long,BlinkEntry>();
    public BlinkCatalog() throws SQLException {
        // build map of gems
        ArrayList<BlinkEntry> blinks = App.getRepositorio().getBlinks(0, 9);
        Collections.sort(blinks);

        HashMap<Integer,Integer> mapComplexity2NumBlinks = new HashMap<Integer,Integer>();
        for (int ii=0;ii<blinks.size();ii++) {
            BlinkEntry be = blinks.get(ii);
            _blinksMap.put(be.get_id(),be);

            int mComplexity = be.get_numEdges();
            Integer count = mapComplexity2NumBlinks.get(mComplexity);
            if (count == null) {
                count = 0;
                mapComplexity2NumBlinks.put(mComplexity,count);
            }
            count++;
            mapComplexity2NumBlinks.put(mComplexity,count);
            be.setCatalogNumber(count.intValue());
        }
    }

    public ArrayList<BlinkEntry> getBlinks() {
        ArrayList<BlinkEntry>  result = new ArrayList<BlinkEntry>(_blinksMap.values());
        Collections.sort(result);
        return result;
    }

    public int getNumberOfBlinks() {
        return _blinksMap.size();
    }

    public BlinkEntry getBlink(int number) {
        return _blinksMap.get(number);
    }
}
