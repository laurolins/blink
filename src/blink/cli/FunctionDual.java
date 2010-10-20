package blink.cli;

import java.util.ArrayList;

import blink.GBlink;

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
public class FunctionDual extends Function {
    public FunctionDual() {
        super("dual","Dual of a g-blink");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.get(0) instanceof GBlink) {
            return ((GBlink) params.get(0)).dual();
        }
        else {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            ArrayList list = (ArrayList) params.get(0);
            for (Object b: list) {
                result.add(((GBlink) b).dual());
            }
            return result;
        }
    }
}
