package blink;

import java.awt.Toolkit;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

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
public class TryToConnectGemsFromTheSameHGQI {
    private ArrayList<GemsThatShouldBeConnected> _list;
    private GemGraph _G;
    private int _solutionCount = 0;
    private int _arcsAdded = 0;
    private int _problemsCount = 0;

    private int _repetitions = 10;
    private int _u = 1;
    private long _time = 25000L;

    static HashSet<Long> _SGem8 = new HashSet<Long>();
    static {
        long arrmingem8[] = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 108, 154, 11, 12, 13, 14, 15, 16, 101, 679, 124, 52, 17, 18, 680, 53, 19, 20, 21, 22,
                23, 24, 25, 181, 120, 26, 27, 948, 28, 103, 29, 563, 85, 156, 157, 94, 58, 350, 415, 809, 30, 158, 31, 104,
                219, 159, 112, 32, 105, 106, 107, 291, 33, 564, 111, 110, 416, 286, 84, 82, 78, 369, 162, 417, 163, 389, 34,
                418, 35, 36, 37, 434, 164, 789, 172, 38, 39, 40, 790, 810, 791, 113, 41, 114, 115, 50, 904, 133, 141,
                792, 121, 118, 165, 56, 119, 42, 188, 43, 419, 167, 681, 168, 122, 44, 45, 565, 123, 682, 137, 431, 683,
                566, 420, 46, 950, 125, 421, 170, 171, 684, 47, 179, 51, 807, 173, 793, 794, 126, 174, 567, 194, 568, 175,
                180, 422, 49, 127, 128, 214, 129, 920, 423, 428, 795, 130, 177, 685, 182, 183, 131, 132, 796, 178, 134, 667,
                686, 425, 426, 427, 915, 135, 440, 687, 429, 186, 136, 688, 689, 797, 55, 430, 569, 452, 570, 138, 690, 57,
                987, 184, 185, 432, 573, 811, 187, 189, 436, 621, 190, 191, 192, 435, 691, 59, 437, 924, 798, 197, 438, 399,
                193, 439, 441, 442, 60, 61, 443, 445, 799, 577, 692, 195, 142, 345, 1049, 196, 62, 446, 578, 63, 64, 65, 66,
                812, 67, 68 };
        for (long i: arrmingem8)
            _SGem8.add(i);
    }


    public TryToConnectGemsFromTheSameHGQI(long time, int repetitions, int u) throws SQLException, IOException, ClassNotFoundException {
        _time = time;
        _repetitions = repetitions;
        _u = u;

        //
        this.mountGemGraphAndGemsThatShouldBeConnected();

        Random rnd = new Random(System.currentTimeMillis());
        for (int i=_list.size()-1;i>0;i--) {
            int index = rnd.nextInt(i+1);
            GemsThatShouldBeConnected ei = _list.get(index);
            _list.set(index,_list.get(i));
            _list.set(i,ei);
        }


        _problemsCount = _list.size();

        //_repetitions = 5;
//        _time = 600 * 60 * 1000L;
        //_time = 20000L;
        int round = 1;
        while (_list.size() > 0) {
            for (int i = _list.size() - 1; i >= 0; i--) {
                System.out.println(String.format("Iter(%d): %4d / %4d     Solution/Problems: %4d / %4d     Arcs: %4d",
                                                 round,(_list.size()-i), _list.size(),
                                                 _solutionCount, _problemsCount, _arcsAdded));
                if (tryConnecting(_list.get(i)))
                    _list.remove(i);
            }
            //_repetitions = 5;
            // _time *= 2;
            round++;
        }
    }

    private boolean tryConnecting(GemsThatShouldBeConnected gemsTSBC) throws ClassNotFoundException, SQLException,
            IOException {

        // log
        System.out.println("Trying "+gemsTSBC.toString());

        // mount list of ids that can be the source of some arc
        HashSet<Long> S = new HashSet<Long>();
        for (GemEntry e: gemsTSBC.getSources()) {
            S.addAll(_G.getConnectedComponentGemEntriesIds(e.getId()));
            //S.add(e.getId());
        }

        //
        GemPathRepository R = new GemPathRepository();

        HashSet<Long> XX = new HashSet<Long>();
        for (GemEntry e: gemsTSBC.getGemEntries()) {
            XX.addAll(_G.getConnectedComponentGemEntriesIds(e.getId()));
            //S.add(e.getId());
        }
        // try 3 times to find an arrow from each starting point
        // or exit if it becomes connected
        for (int i=0;i<_repetitions;i++) {
            for (long id: S) { //(HashSet<Long>)S.clone()
                GemEntry source = _G.getGemEntry(id);

                //if (source.getNumVertices() > 50)
                //    continue;

                //if (!_SGem8.contains(source.getId()))
                //    continue;


                Gem sourceGem = source.getGem();

                GemSimplificationPathFinder A = new GemSimplificationPathFinder(sourceGem, _u, _time,source.getTSClassSize());
                Gem targetGem = A.getBestAttractorFound();
                int tsClassSize = A.getBestAttractorTSClassSize();
                boolean tsRepresentant = A.isBestAttractorTSClassRepresentant();
                Path path = A.getBestPath();

                boolean add = false;
                if (sourceGem.compareTo(targetGem) != 0) {
                    add = R.addPathIfItDoesNotExist(sourceGem, targetGem, tsClassSize, tsRepresentant, path);
                    if (add) {
                        Toolkit.getDefaultToolkit().beep();

                        GemEntry target = R.getLastGemEntryAdded();
                        GemPathEntry gemPathAdded = R.getLastGemPathEntryAdded();
                        _G.addEdge(source,target,gemPathAdded);

                        _arcsAdded++;
                        System.out.println("Added arc "+source.getId()+" -> "+target.getId());

                        // check connection
                        if (_G.isConnected(XX)) {
                            _solutionCount++;
                            System.out.println("Solved");

                            //AudioStream as = new AudioStream(new FileInputStream("tada.wav"));
                            //AudioPlayer.player.start(as);
                            //AudioPlayer.player.stop(as);

                            Library.playSound("solucao.wav",3000);
                            /*
                            Toolkit.getDefaultToolkit().beep();
                            Toolkit.getDefaultToolkit().beep();
                            Toolkit.getDefaultToolkit().beep();
                            Toolkit.getDefaultToolkit().beep();
                            Toolkit.getDefaultToolkit().beep();*/

                            return true;
                        }

                        // use on next iteration
                        // S.add(target.getId());
                    }
                }
            }
        }
        return false;
    }

    private void mountGemGraphAndGemsThatShouldBeConnected() throws SQLException, IOException, ClassNotFoundException {
        _G = new GemGraph();
        _list = new ArrayList<GemsThatShouldBeConnected>();
        ArrayList<ClassHGNormQI> list = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);
        for (ClassHGNormQI c : list) {
            int maxSource = 1;
            int sourceCount = 0;
            c.load();
            HashSet<Long> ids = new HashSet<Long>();
            HashSet<Long> sources = c.getMinIdsWithFewestGems();
            GemsThatShouldBeConnected gemsTSBC = new GemsThatShouldBeConnected();
            for (BlinkEntry b : c.getBlinks()) {
                if (b.getMinGem() != -1L && b.getMinGem() != 0L) {
                    if (!ids.contains(b.getMinGem())) {
                        ids.add(b.getMinGem());
                        // System.out.println("Search for "+b.getMinGem());
                        gemsTSBC.add(_G.getGemEntry(b.getMinGem()));
                        if (sourceCount < maxSource && sources.contains(b.getMinGem())) {
                            sourceCount++;
                            gemsTSBC.addSource(_G.getGemEntry(b.getMinGem()));
                        }
                    }
                }
            }
            if (ids.size() < 2) // || ids.size() > 3)
                continue;
            else {
                _list.add(gemsTSBC);
            }
        }
    }

    class GemsThatShouldBeConnected {
        private ArrayList<GemEntry> _list;
        private ArrayList<GemEntry> _sources;
        public GemsThatShouldBeConnected() {
            _list = new ArrayList<GemEntry>();
            _sources = new ArrayList<GemEntry>();
        }
        public void addSource(GemEntry e) {
            _sources.add(e);
        }
        public void add(GemEntry e) {
            _list.add(e);
        }
        public ArrayList<GemEntry> getGemEntries() {
            return _list;
        }
        public ArrayList<GemEntry> getSources() {
            return _sources;
        }
        public String toString() {
            StringBuffer buf = new StringBuffer();
            boolean first = true;
            for (GemEntry e: _list) {
                if (!first)
                    buf.append(',');
                buf.append(e.getId());
                first = false;
            }

            first = true;
            buf.append("   sources: ");
            for (GemEntry e: _sources) {
                if (!first)
                    buf.append(',');
                buf.append(e.getId());
                first = false;
            }

            return buf.toString();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
        while (true) {
            new TryToConnectGemsFromTheSameHGQI(600000L, 1, 2);
            GemGraph.updateMinGem();
        }
    }
}
