package blink.cli;

import java.util.ArrayList;

import blink.GBlink;

/**
 * <p>
 * A {@link CommandLineInterface} command that retrieves the representative of a
 * given (or a list of) {@link GBlink}. The flags <i>D</i>, <i>R</i> and
 * <i>RD</i> can be set, which stand for <i>dual</i>, <i>reflection</i> and
 * <i>reflection-dual</i> respectively.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FunctionRepresentantGBlink extends Function {
    public FunctionRepresentantGBlink() {
        super("rep","Representant of a g-blink");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
     try {
         Object result = hardwork(params, localData);
         return result;
     } catch (EvaluationException ex) {
         ex.printStackTrace();
         throw ex;
     }
     catch (Exception e) {
         e.printStackTrace();
         throw new EvaluationException(e.getMessage());
     }
 }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        boolean dual = true;
        boolean reflection = true;
        boolean refDual = true;

        try {
            dual = ((Number) localData.getData("D")).intValue() == 1;
        } catch (Exception e) {}
        try {
            reflection = ((Number) localData.getData("R")).intValue() == 1;
        } catch (Exception e) {}
        try {
            refDual = ((Number) localData.getData("RD")).intValue() == 1;
        } catch (Exception e) {}

        if (params.get(0) instanceof GBlink) {
            return ((GBlink) params.get(0)).getNewRepresentant(dual, reflection, refDual);
        }
        else {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            ArrayList list = (ArrayList) params.get(0);
            for (Object b: list) {
                result.add(((GBlink) b).getNewRepresentant(dual,reflection,refDual));
            }
            return result;
        }
    }

}
