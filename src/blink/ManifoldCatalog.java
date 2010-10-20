package blink;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class ManifoldCatalog {
    private HashMap<Integer,Manifold> _manifolds = new HashMap<Integer,Manifold>();
    public ManifoldCatalog() throws SQLException {
        // map minGem to Manifold
        HashMap<Long, Manifold> map = new HashMap<Long, Manifold>();

        // build map of gems
        ArrayList<BlinkEntry> gems = App.getRepositorio().getBlinks(0, 9);
        for (BlinkEntry be : gems) {
            long gemId = be.getMinGem();
            Manifold M = map.get(gemId);
            if (M == null) {
                M = new Manifold(gemId);
                map.put(gemId, M);
            }
            M.add(be);
        }

        ArrayList<Manifold> list = new ArrayList<Manifold>(map.values());

        Collections.sort(list,new Comparator(){
            public int compare(Object a, Object b) {
                Manifold ca = (Manifold) a;
                Manifold cb = (Manifold) b;
                return ca.getMaxCodeMinEdgesBlinkEntry().getBlink().compareTo(cb.getMaxCodeMinEdgesBlinkEntry().getBlink());
            }
        });

        HashMap<Integer,Integer> mapComplexity2NumBlinks = new HashMap<Integer,Integer>();
        for (int ii=0;ii<list.size();ii++) {
            Manifold m = list.get(ii);
            m.setNumber(ii+1);
            _manifolds.put(m.getNumber(),m);

            int mComplexity = m.blinkComplexity();
            Integer count = mapComplexity2NumBlinks.get(mComplexity);
            if (count == null) {
                count = 0;
                mapComplexity2NumBlinks.put(mComplexity,count);
            }
            count++;
            mapComplexity2NumBlinks.put(mComplexity,count);
            m.setNumberOnComplexity(count);
        }
    }

    public int getNumberOfManifolds() {
        return _manifolds.size();
    }

    public Manifold getManifold(int number) {
        return _manifolds.get(number);
    }

    public Manifold getManifoldFromMinGem(long minGem) {
        for (Manifold m: _manifolds.values()) {
            if (m.getGemId() == minGem)
                return m;
        }
        return null;
    }

}
