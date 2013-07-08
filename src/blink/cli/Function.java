package blink.cli;

import java.util.ArrayList;

/**
 * <p>
 * Title: blink.cli.Function
 * </p>
 * 
 * <p>
 * An abstraction for any function that will be used by
 * {@link CommandLineInterface}; in other words, any function that will be
 * "user-driven" has to extend this class.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public abstract class Function {
    String _name;
    String _shortDescription;
    
    public Function(String name, String shortDescription) {
    	_name = name;
        _shortDescription = shortDescription;
    }
    
    public String getName() {
        return _name;
    }

    public String getShortDescription() {
        return _shortDescription;
    }

    public abstract Object evaluate(ArrayList<Object> params, DataMap localMap)  throws EvaluationException;
    
}
