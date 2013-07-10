package blink.cli;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * <p>
 * Description: A {@link CommandLineInterface} command that displays detailed
 * information about a given function.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FunctionHelp extends Function {
    public FunctionHelp() {
        super("help","Help on functions: help(\"<func>\")");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }


    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
    	String st = (String) params.get(0);
//    	String st = "gem";
        //String helpDir = App.getProperty("helpdir");
        //if (helpDir == null)
        String helpDir = "help/";
        File file = new File(helpDir+st+".htm");

        URL url = new URL("file:///" + file.getCanonicalPath());
        JEditorPane htmlPane = new JEditorPane(url);
        htmlPane.setEditable(false);

        JFrame f = new JFrame("Gem Drawing: " + CommandLineInterface.getInstance()._currentCommand.replace('\n', ' '));
        linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
        f.setContentPane(new JScrollPane(htmlPane));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);

        return null;
   }

}
