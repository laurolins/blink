package blink.cli;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import blink.DrawPanel;
import blink.GBlink;
import blink.Gem;
import blink.LnkGen;
import blink.MapD;
import blink.PanelBlinkViewer;
import blink.PanelGemViewer;
import blink.PanelMapViewer;

/**
 * <p>
 * A {@link CommandLineInterface} command that draws a {@link Gem}
 * or {@link GBlink}.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author Lauro Didier Lins
 * @version 1.0
 */
public class FunctionGenLnk extends Function {
    public FunctionGenLnk() {
        super("lnk","Generate lnk file from g-blink. (gblink, filename)");
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

    public Object hardwork(ArrayList params, DataMap localMap) throws EvaluationException, Exception {
        if (params.get(0) instanceof GBlink) {
        	String filename = (String) params.get(1);
            GBlink G = (GBlink) params.get(0);
            LnkGen lnk = new LnkGen(G);
    		lnk.changeAffineTransform(0.1, 0.0, 0.0, 500.0, 500.0);
    		lnk.genLnkFile(filename);
        }
        return "Done!";
    }
}
