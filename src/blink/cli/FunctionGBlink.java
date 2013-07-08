package blink.cli;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import blink.App;
import blink.BlinkEntry;
import blink.ClassEntry;
import blink.GBlink;
import blink.Library;

/**
 * <p>
 * A {@link CommandLineInterface} command that retrieves a {@link GBlink}. The
 * retrieval can be through id or code. A list of ids or codes can be passed as
 * parameter, although this list may contain either only ids or only codes.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FunctionGBlink extends Function {

    public FunctionGBlink() {
        super("gblink","G-Blink multi-constructor");
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
        // cyclic representation
        String cr = null;
        try { cr = localData.getData("cr").toString(); } catch (Exception e) {}

        // example
        // "1, 2, 1 3 5 2, 3 4, 4 5, R 1 4"
        if (cr != null) {
            ArrayList<ArrayList<Integer>> table = new ArrayList<ArrayList<Integer>>();
            ArrayList<Integer> reds = new ArrayList<Integer>();

            StringTokenizer st1 = new StringTokenizer(cr,",");
            while (st1.hasMoreTokens()) {
                StringTokenizer st2 = new StringTokenizer(st1.nextToken()," ");
                ArrayList<Integer> list = new ArrayList<Integer>();
                if (st2.hasMoreTokens()) {
                    String firstToken = st2.nextToken();
                    boolean red = false;
                    if (firstToken.toUpperCase().equals("R")) {
                        red = true;
                    }
                    else list.add(Integer.parseInt(firstToken));
                    while (st2.hasMoreTokens()) {
                        list.add(Integer.parseInt(st2.nextToken()));
                    }
                    if (!red)
                        table.add(list);
                    else
                        reds = list;
                }
            }

            int[][] table2 = new int[table.size()][];
            int i = 0;
            for (ArrayList<Integer> list: table) {
                table2[i++] = Library.arrayListToArray(list);
            }
            int reds2[] = Library.arrayListToArray(reds);

            return new GBlink(table2,reds2);
        }

        if (params.get(0) instanceof ClassEntry) {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            for (BlinkEntry be: App.getRepositorio().getBlinksByClass((ClassEntry) params.get(0))) {
                result.add(be.getBlink());
            }
            return result;
        }
        if (params.get(0) instanceof String) {
            StringTokenizer st = new StringTokenizer((String) params.get(0), " ");
            int[] code = Library.stringToIntArray(st.nextToken(), ",");
            int[] reds = {};
            if (st.hasMoreTokens())
                reds = Library.stringToIntArray(st.nextToken(), ",");
            GBlink G = new GBlink(code, reds);
            return G;
        }
        if (params.size() == 1 && (params.get(0) instanceof Integer || params.get(0) instanceof Long)) {
            long id = ((Number)params.get(0)).longValue();
            try {
                ArrayList<BlinkEntry> list = App.getRepositorio().getBlinksByIDs(id);
                if (list.size() == 0)
                    return null;
                else
                    return (list.get(0)).getBlink();
            } catch (SQLException ex) {
                throw new EvaluationException(ex.getMessage());
            }
        }
        if (params.size() > 2) {
            long idsArray[] = new long[params.size()];
            int i = 0;
            for (Object o: params) {
                idsArray[i++] = ((Number) o).longValue();
            }

            StringBuffer sb = new StringBuffer();
            // ArrayList<GBlink> listGBlinks = new ArrayList<GBlink>();
            for (BlinkEntry be: App.getRepositorio().getBlinksByIDs(idsArray)) {
                GBlink G = be.getBlink();
                sb.append("\n"+G.getBlinkWord().toString());
            }
            return sb.toString();
        }
        if (params.get(0) instanceof List) {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            for (Object o: (List) params.get(0)) {
                if (o instanceof Number) {
                    ArrayList<BlinkEntry> list = App.getRepositorio().getBlinksByIDs(((Number) o).longValue());
                    if (list.size() == 1)
                        result.add((list.get(0)).getBlink());
                }
                if (o instanceof GBlink) {
                    result.add((GBlink) o);
                }
                else if (o instanceof ClassEntry) {
                    for (BlinkEntry be: App.getRepositorio().getBlinksByClass((ClassEntry) o)) {
                        result.add(be.getBlink());
                    }
                }
            }
            return result;
        }
        else return null;
    }
}


