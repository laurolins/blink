package blink.cli;

import java.util.ArrayList;

import blink.GBlink;
import blink.GBlinkVertex;

/**
 * <p>
 * A {@link CommandLineInterface} command that merges two
 * {@link GBlink} <i>A</i> and <i>B</i>, at two given vertices <i>a</i> and
 * <i>b</i>, where:<br>
 * - <i>a</i> belongs to <i>A</i><br>
 * - <i>b</i> belongs to <i>B</i><br>
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FunctionMergeGBlinks extends Function {
    public FunctionMergeGBlinks() {
        super("merge","Merge g-blinks on angle edges incident to vertex");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.size() == 4) {
            GBlink A = ((GBlink) params.get(0)).copy();
            int a = ((Number) params.get(1)).intValue();
            GBlink B = ((GBlink) params.get(2)).copy();
            int b = ((Number) params.get(3)).intValue();
            GBlinkVertex va = A.findVertex(a);
            GBlinkVertex vb = B.findVertex(b);
            GBlink.merge(A,va,B,vb);
            return A;
        }
        else return null;
    }
}
