package blink.cli;

import java.util.ArrayList;
import java.util.Collections;

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
public class FunctionListFunctions extends Function {
    public FunctionListFunctions() {
        super("lf","List Available Functions");
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

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       FunctionMap map = CommandLineInterface.getInstance().getFunctionMap();
       ArrayList<String> list = new ArrayList<String>(map.getKeySet());
       Collections.sort(list);

       if (params.size() > 0) {
           for (int i=list.size()-1;i>=0;i--) {
               String funcName = list.get(i);
               boolean found = false;
               for (Object o: params) {
                   String st = (String) o;
                   if (funcName.indexOf(st) != -1) {
                       found = true;
                       break;
                   }
               }
               if (!found)
                   list.remove(i);
           }
       }

       StringBuffer sb = new StringBuffer();
       boolean first = true;
       for (String key: list) {
           if (!first)
               sb.append("\n");
           sb.append(String.format("%-50s %s",key,map.getFunction(key).getShortDescription()));
           first = false;
       }
       return sb.toString();
    }
}
