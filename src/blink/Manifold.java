package blink;

import java.io.IOException;
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
public class Manifold {

    private ArrayList<BlinkEntry> _entries = new ArrayList<BlinkEntry>();

    private long _gemId;

    private int _number;
    public void setNumber(int number) {
        _number = number;
    }
    public int getNumber() {
        return _number;
    }

    public ArrayList<BlinkEntry> getEntries() {
        this.sort();
        return (ArrayList<BlinkEntry>)_entries.clone();
    }

    public ArrayList<BlinkEntry> getEntriesOnTheSameOrientationAsTheMinimum() {
        ArrayList<BlinkEntry> list = this.getEntries();
        BlinkEntry be = this.getMinCodeMinEdgesBlinkEntry();
        long qi = be.get_qi();
        for (int i=list.size()-1;i>=0;i--) {
            if (list.get(i).get_qi() != qi)
                list.remove(i);
        }
        return list;
    }

    private void sort() {
        Collections.sort(_entries);
    }

    private int _numberOnComplexity;
    public void setNumberOnComplexity(int number) {
        _numberOnComplexity = number;
    }
    public int getNumberOnComplexity() {
        return _numberOnComplexity;
    }

    public Manifold(long gemId) {
        _gemId = gemId;
    }

    public int blinkComplexity() {
        BlinkEntry be = this.getMinCodeMinEdgesBlinkEntry();
        return be.get_numEdges();
    }

    public void add(BlinkEntry be) {
        if (be.getMinGem() != _gemId)
            throw new RuntimeException();
        _entries.add(be);
    }

    public HomologyGroup getHomologuGroup() {
        BlinkEntry be = this.getMinCodeMinEdgesBlinkEntry();
        return be.getBlink().homologyGroupFromGBlink();
    }

    public QI calculateQI(int maxR) {
        BlinkEntry be = this.getMinCodeMinEdgesBlinkEntry();
        return be.getBlink().optimizedQuantumInvariant(3,maxR);
    }

    public int numberOfBlinks() {
        return _entries.size();
    }

    public int numberOfBlinksOnTheSameOrientation() {
        return this.getEntriesOnTheSameOrientationAsTheMinimum().size();
    }

    public GemPrimeStatus getGemPrimeStatus() throws ClassNotFoundException, IOException, SQLException {
        GemEntry ge = App.getRepositorio().getGemById(_gemId);
        if (ge == null) {
            System.out.println("Null gemId " + _gemId);
            return GemPrimeStatus.UNDEFINED;
        }
        else return ge.getGemPrimeStatus();
    }

    public GemEntry getGemEntry() throws ClassNotFoundException, IOException, SQLException {
        return App.getRepositorio().getGemById(_gemId);
    }

    public BlinkEntry getMaxCodeMinEdgesBlinkEntry()  {
        BlinkEntry maxCodeMinEdges = null;
        for (BlinkEntry be: _entries) {
            if (maxCodeMinEdges == null) maxCodeMinEdges = be;
            else if (maxCodeMinEdges.get_numEdges() > be.get_numEdges() ||
                     (
                             maxCodeMinEdges.get_numEdges() == be.get_numEdges() &&
                             maxCodeMinEdges.getBlink().compareTo(be.getBlink()) < 0
                     )) {
                maxCodeMinEdges = be;
            }
        }
        return maxCodeMinEdges;
    }

    public BlinkEntry getMinCodeMinEdgesBlinkEntry()  {
        BlinkEntry minCodeMinEdges = null;
        for (BlinkEntry be: _entries) {
            if (minCodeMinEdges == null) minCodeMinEdges = be;
            else if (minCodeMinEdges.get_numEdges() > be.get_numEdges() ||
                     (
                             minCodeMinEdges.get_numEdges() == be.get_numEdges() &&
                             minCodeMinEdges.getBlink().compareTo(be.getBlink()) > 0
                     )) {
                minCodeMinEdges = be;
            }
        }
        return minCodeMinEdges;
    }

    public String get_hg() {
        return getMaxCodeMinEdgesBlinkEntry().get_hg();
    }

    public int get_numElements() {
        return _entries.size();
    }

    public long get_qi() {
        return getMaxCodeMinEdgesBlinkEntry().get_qi();
    }

    // gambiarra local
    private static HashMap<Long,GemEntry> _gemEntries;
    static {
        try {
            ArrayList<GemEntry> list = App.getRepositorio().getGems();
            _gemEntries = new HashMap<Long,GemEntry>();
            for (GemEntry e: list) {
                _gemEntries.put(e.getId(),e);
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public long getGemId() {
        return _gemId;
    }

    public String getMinGemCodes() {
        String result = "";
        ArrayList<Long> S = new ArrayList<Long>();
        for (BlinkEntry be: _entries)
            if (S.indexOf(be.getMinGem()) == -1)
                S.add(be.getMinGem());
        Collections.sort(S);
        boolean first = true;
        for (long l: S) {
            if (!first)
                result+=", ";
            GemEntry ge = _gemEntries.get(l);

            result+=l+(l != -1 ? "m"+ge.getTSClassSize() : "");
            result+=(l != -1 ? "v"+ge.getNumVertices() : "");
            if (ge.getNumVertices() <= 30)
                result+="r" + ge.getNumVertices()+"-"+ge.getCatalogNumber();
            first = false;
        }
        return result;
    }
}
