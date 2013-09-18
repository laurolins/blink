package blink.cli;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import blink.App;
import blink.DrawPanel;
import blink.GBlink;
import blink.Gem;
import blink.GemColor;
import blink.GemVertex;
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
public class FunctionGetCode extends Function {
    public FunctionGetCode() {
        super("getcode","gerar codigo canonico");
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
        if(params.get(0) instanceof Gem ){
        	Gem G2 = (Gem) params.get(0);
        	Gem G1 = G2.copy();
        	G1.goToCodeLabel().getCode();
        	return G1;
        	
        }
        return null;
    }
}
