package blink;

public class GemVertex implements Comparable, Cloneable {
    private int _label = -1;
    private int _originalLabel = -1;
    private int _tempLabel = -1;
    private GemVertex _red;
    private GemVertex _blue;
    private GemVertex _yellow;
    private GemVertex _green;

    public GemVertex(int label) {
        _label = label;
    }

    public void copyLabelToOriginalLabel() {
        _originalLabel = _label;
    }

    public void copyLabelToTempLabel() {
        _tempLabel = _label;
    }

    public void copyTempLabelToLabel() {
        _label = _tempLabel;
    }

    public int getOriginalLabel() {
        return _originalLabel;
    }

    public int getTempLabel() {
        return _tempLabel;
    }

    public void setLabel(int label) {
        _label = label;
    }

    public boolean isLabelUndefined() {
        return _label == -1;
    }

    public void setLabelAsUndefined() {
        _label = -1;
    }

    public void setGreen(GemVertex v) {
        _green = v;
    }

    public void setRed(GemVertex v) {
        _red = v;
    }

    public void setYellow(GemVertex v) {
        _yellow = v;
    }

    public void setBlue(GemVertex v) {
        _blue = v;
    }

    public GemVertex getRed() {
        return _red;
    }

    public GemVertex getGreen() {
        return _green;
    }

    public GemVertex getYellow() {
        return _yellow;
    }

    public GemVertex getBlue() {
        return _blue;
    }

    public void applyPermutation(GemColor p[]) {
        GemVertex v[] = {_yellow,_blue,_red,_green};
        if (p[0] == GemColor.yellow) _yellow = v[0];
        else if (p[0] == GemColor.blue) _yellow = v[1];
        else if (p[0] == GemColor.red) _yellow = v[2];
        else if (p[0] == GemColor.green) _yellow = v[3];

        if (p[1] == GemColor.yellow) _blue = v[0];
        else if (p[1] == GemColor.blue) _blue = v[1];
        else if (p[1] == GemColor.red) _blue = v[2];
        else if (p[1] == GemColor.green) _blue = v[3];

        if (p[2] == GemColor.yellow) _red = v[0];
        else if (p[2] == GemColor.blue) _red = v[1];
        else if (p[2] == GemColor.red) _red = v[2];
        else if (p[2] == GemColor.green) _red = v[3];

        if (p[3] == GemColor.yellow) _green = v[0];
        else if (p[3] == GemColor.blue) _green = v[1];
        else if (p[3] == GemColor.red) _green = v[2];
        else if (p[3] == GemColor.green) _green = v[3];
    }



    public void setNeighbour(GemVertex v, GemColor c) {
        if (c == GemColor.blue) _blue = v;
        else if (c == GemColor.green) _green = v;
        else if (c == GemColor.yellow) _yellow = v;
        else if (c == GemColor.red) _red = v;
        else throw new RuntimeException();
    }

    public GemVertex getNeighbour(GemColor c) {
        if (c == GemColor.blue) return _blue;
        else if (c == GemColor.green) return _green;
        else if (c == GemColor.yellow) return _yellow;
        else if (c == GemColor.red) return _red;
        else throw new RuntimeException();
    }

    public GemVertex getNeighbour(int colorIndex) {
        return getNeighbour(GemColor.getByNumber(colorIndex));
    }

    public int getLabel() {
        return _label;
    }

    public boolean hasOddLabel() {
        return _label % 2 == 1;
    }

    public boolean hasEvenLabel() {
        return _label % 2 == 0;
    }

    public int compareTo(Object x) {
        GemVertex v = (GemVertex) x;
        return this.getLabel() - v.getLabel();
    }

    // -- flag --------------------------------
    private boolean _flag;

    public boolean getFlag() {
        return _flag;
    }

    public void setFlag(boolean flag) {
        _flag = flag;
    }

    // -- flag --------------------------------
    private Component _component[];
    public Component getComponent(int component) {
        if (_component == null)
            return null;
        else return _component[component];
    }
    public void setComponent(int index, Component c) {
        if (_component == null) _component = new Component[16];
        _component[index] = c;
    }
    // -- Component information ---------------

    public GemVertex copy() {
        try {
            GemVertex copy = (GemVertex)this.clone();
            copy._component = null;
            return copy;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("OOoooooppppssss");
        }
    }

}
