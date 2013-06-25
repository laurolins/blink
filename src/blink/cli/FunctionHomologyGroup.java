package blink.cli;

import java.util.ArrayList;
import java.util.List;

import blink.BlinkEntry;
import blink.GBlink;
import blink.HomologyGroup;

/**
 * <p>
 * A {@link CommandLineInterface} command that retrieves the homology
 * group of a given (or a list of) {@link GBlink}.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FunctionHomologyGroup extends Function {
    public FunctionHomologyGroup() {
        super("hg","Homology Group");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        Object a = params.get(0);

        ArrayList<GBlink> blinks = new ArrayList<GBlink>();

        if (a instanceof BlinkEntry) {
            blinks.add(((BlinkEntry) a).getBlink());
        } else if (a instanceof GBlink) {
            blinks.add((GBlink) a);
        } else if (a instanceof List) {
            for (Object o : (List) a) {
                if (o instanceof BlinkEntry) {
                    blinks.add(((BlinkEntry) o).getBlink());
                } else if (o instanceof GBlink) {
                    blinks.add((GBlink) o);
                }
            }
        } else
            throw new EvaluationException("first argument of qi must be");

        if (blinks.size() > 1) {
            ArrayList<HomologyGroup> hgs = new ArrayList<HomologyGroup>();
            for (GBlink b : blinks) {
                hgs.add(b.copy().homologyGroupFromGBlink());
            }
            return hgs;
        } else if (blinks.size() > 0) {
            return blinks.get(0).copy().homologyGroupFromGBlink();
        } else
            throw new RuntimeException();
    }
}
