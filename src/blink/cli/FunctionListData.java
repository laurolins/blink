package blink.cli;

import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>
 * A {@link CommandLineInterface} command that list every available data in the
 * current prompt session. For instance, if you declared the variables <i>x</i>
 * and <i>y</i>, then this command will display those variables and their types.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FunctionListData extends Function {
    public FunctionListData() {
        super("ld","List Available Data");
    }

    public Object evaluate(ArrayList params, DataMap localMap) throws EvaluationException {
        DataMap map = CommandLineInterface.getInstance().getDataMap();
        ArrayList<String> list = new ArrayList<String>(map.getKeySet());
        Collections.sort(list);
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (String key: list) {
            if (!first)
                sb.append("\n");
            sb.append(String.format("%-20s %s",key,map.getData(key).getClass()));
            first = false;
        }
        return sb.toString();
    }
}
