package blink.cli;

import java.util.ArrayList;

import blink.GBlink;
import blink.GBlinkVertex;

/**
 * <p>A {@link CommandLineInterface} command that retrieves the
 * dual of a given vertex label of a given {@link GBlink}.
 * The vertex must be present at the given {@link GBlink}.</p>
 *
 * <p>Copyright: Copyright (c) 2013</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FunctionDualLabel extends Function {
    public FunctionDualLabel() {
        super("dualLabel","Dual of a g-blink vertex label");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        GBlink A = ((GBlink) params.get(0)).copy();
        int a = ((Number) params.get(1)).intValue();
        GBlinkVertex va = A.findVertex(a);
        A.goToDual();
        return new Integer(va.getLabel());
    }
}
