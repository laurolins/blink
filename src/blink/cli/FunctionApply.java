package blink.cli;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * <p>
 * A {@link CommandLineInterface} command that applies a given
 * function into several objects. The function must be the first parameter where
 * as the list of objects must be applicable on the given function.
 * </p>
 * <p>
 * For visibility sake, this is the pattern to call this function:<br>
 * <i>apply(&lt;func&gt;, o1, o2, ...)</i>
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FunctionApply extends Function {

	public FunctionApply() {
		super("apply","Apply some method in object(s)");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		try {
			Object result = hardwork(params, localData);
			return result;
		} catch (EvaluationException ex) {
			throw ex;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EvaluationException(e.getMessage());
		}
	}

	private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
		ArrayList<Object> list = FunctionApply.extractObjectInASingleList(params);
		String methodName = (String) list.get(0);
		ArrayList<Object> result = new ArrayList<Object>();
		for (int i=1; i< list.size();i++) {
			Object o = list.get(i);
			Class c = o.getClass();
			Method m = c.getMethod(methodName);
			result.add(m.invoke(o));
		}
		if (result.size() == 1)
			return result.get(0);
		else
			return result;
	}
	
    public static ArrayList<Object> extractObjectInASingleList(Object o) {
    	ArrayList<Object> result = new ArrayList<Object>(); 
        Stack<Object> S= new Stack<Object>();
        S.push(o);        
        while (!S.isEmpty()) {
        	Object oo = S.pop();
        	if (oo instanceof List) {
        		List list = (List) oo;
        		for (int i=list.size()-1;i>=0;i--)
        			S.push(list.get(i));
        	}
        	else result.add(oo);
        }
        return result;    	
    }
}
