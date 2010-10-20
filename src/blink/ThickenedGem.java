package blink;

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
public class ThickenedGem extends Gem implements IGemVertexLabeler {
    private Gem _originalGem;
    private HashMap<GemVertex,HashMap<String,ThickenedGemVertex>> _map;
    private HashMap<GemVertex,ThickenedGemVertex> _mapInfo;
    public ThickenedGem(Gem originalGem) {
        super();

        _originalGem = originalGem;
        _map = new HashMap<GemVertex,HashMap<String,ThickenedGemVertex>>();
        _mapInfo = new HashMap<GemVertex,ThickenedGemVertex>();

        // create vertices of thickened gem
        int k=1;
        for (GemVertex v: _originalGem.getVertices()) {
            HashMap<String,ThickenedGemVertex> mapv = new HashMap<String,ThickenedGemVertex>();
            for (GemColor p[] : GemColor.PERMUTATIONS) {
                String stColorPermutation = GemColor.getColorsCompactString(p);
                GemVertex vv = this.newVertex(k++);
                ThickenedGemVertex tv = new ThickenedGemVertex(vv,v.getLabel(),p);
                _mapInfo.put(vv,tv);
                // System.out.println(""+vv);
                mapv.put(stColorPermutation,tv);
            }
            _map.put(v,mapv);
        }

        // create edges of thickened gem
        for (GemVertex v: _originalGem.getVertices()) {
            for (GemColor p[] : GemColor.PERMUTATIONS) {
                String stColorPermutation0 = GemColor.getColorsCompactString(p);
                String stColorPermutation1 = GemColor.getColorsCompactString(p[1],p[0],p[2],p[3]);
                String stColorPermutation2 = GemColor.getColorsCompactString(p[0],p[2],p[1],p[3]);
                String stColorPermutation3 = GemColor.getColorsCompactString(p[0],p[1],p[3],p[2]);

                ThickenedGemVertex tv = _map.get(v).get(stColorPermutation0);
                ThickenedGemVertex tv0 = _map.get(v.getNeighbour(p[3])).get(stColorPermutation0);
                ThickenedGemVertex tv1 = _map.get(v).get(stColorPermutation1);
                ThickenedGemVertex tv2 = _map.get(v).get(stColorPermutation2);
                ThickenedGemVertex tv3 = _map.get(v).get(stColorPermutation3);

                if (tv.getVertex().getLabel() < tv0.getVertex().getLabel())
                    this.setNeighbours(tv.getVertex(),tv0.getVertex(),GemColor.yellow);
                if (tv.getVertex().getLabel() < tv1.getVertex().getLabel())
                    this.setNeighbours(tv.getVertex(),tv1.getVertex(),GemColor.blue);
                if (tv.getVertex().getLabel() < tv2.getVertex().getLabel())
                    this.setNeighbours(tv.getVertex(),tv2.getVertex(),GemColor.red);
                if (tv.getVertex().getLabel() < tv3.getVertex().getLabel())
                    this.setNeighbours(tv.getVertex(),tv3.getVertex(),GemColor.green);
            }
        }
    }

    public ThickenedGemVertex getThickenedGemVertex(GemVertex v) {
        return _mapInfo.get(v);
    }

    public String getLabel(GemVertex v) {
        ThickenedGemVertex tv = _mapInfo.get(v);
        String label = v.getLabel()+";"+tv.getOriginalLabel()+":"+GemColor.getColorsCompactString(tv.getColorPermutation());
        return label;
    }
}

class ThickenedGemVertex {
    private int _originalVertexLabel;
    private GemColor[] _pColors;
    private GemVertex _vertex;
    public ThickenedGemVertex(GemVertex vertex, int originalLabel, GemColor[] pColors) {
        _vertex = vertex;
        _originalVertexLabel = originalLabel;
        _pColors = pColors;
    }
    public GemVertex getVertex() {
        return _vertex;
    }
    public int getOriginalLabel() {
        return _originalVertexLabel;
    }
    public GemColor[] getColorPermutation() {
        return _pColors;
    }
}
