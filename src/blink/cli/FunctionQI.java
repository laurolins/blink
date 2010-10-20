package blink.cli;

import java.util.ArrayList;
import java.util.List;

import blink.BlinkEntry;
import blink.GBlink;
import blink.QI;

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
public class FunctionQI extends Function {

    public FunctionQI() {
        super("qi","Quantum Invariant");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.size() == 2) {
            Object a = params.get(0);

            ArrayList<GBlink> blinks = new ArrayList<GBlink>();

            if (a instanceof BlinkEntry) {
                blinks.add(((BlinkEntry)a).getBlink());
            }
            else if (a instanceof GBlink) {
                blinks.add((GBlink)a);
            }
            else if (a instanceof List) {
                for (Object o : (List) a) {
                    if (o instanceof BlinkEntry) {
                        blinks.add(((BlinkEntry)o).getBlink());
                    }
                    else if (o instanceof GBlink) {
                        blinks.add((GBlink)o);
                    }
                }
            }
            else throw new EvaluationException("first argument of qi must be");

            if (blinks.size() > 1) {
                ArrayList<QI> qiResults = new ArrayList<QI>();
                for (GBlink b : blinks) {
                    int rmax = Math.min(((Number) params.get(1)).intValue(), 50);
                    qiResults.add(b.copy().optimizedQuantumInvariant(3, rmax));
                }
                return qiResults;
            }
            else if (blinks.size() > 0) {
                int rmax = Math.min(((Number) params.get(1)).intValue(), 50);
                return blinks.get(0).copy().optimizedQuantumInvariant(3, rmax);
            }
            else throw new RuntimeException();

        }
        else return null;
    }
}
