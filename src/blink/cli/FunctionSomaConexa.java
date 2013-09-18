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
public class FunctionSomaConexa extends Function {
    public FunctionSomaConexa() {
        super("consum","somaConexa");
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
        if(params.get(0) instanceof Gem && params.get(1) instanceof Gem && params.get(2) instanceof Number && params.get(3) instanceof Number){
        	Gem G3 = (Gem) params.get(0);
        	Gem G4 = (Gem) params.get(1);
        	Gem G1 = G3.copy();
        	Gem G2 = G4.copy();
        	int novo = G1.getNumVertices()-1;
        	int L1 = (Integer) params.get(2);
        	int L2 = (Integer) params.get(3)+novo-1;
        	int dif1 = G1.getNumVertices() - L1;
        	int dif2 = (Integer) params.get(3)-1;
        	for(int i = 1;i<=G1.getNumVertices();i++){
        		int newlabel = G1.getVertex(i).getLabel();
        		newlabel+=dif1;
        		if(newlabel > G1.getNumVertices()) newlabel -= G1.getNumVertices();
        		G1.getVertex(i).setLabel(newlabel);
        	}
        	while(G1.getVertex(G1.getNumVertices()).getLabel() != G1.getNumVertices()){
        		GemVertex vtemp = G1.removerVertice(1);
        		G1.inserirVertice(vtemp);
        	}
        	for(int i = 1;i<=G2.getNumVertices();i++){
        		int newlabel = G2.getVertex(i).getLabel();
        		newlabel-=dif2;
        		if(newlabel < 1 ) newlabel += G2.getNumVertices();
        		G2.getVertex(i).setLabel(newlabel);
        	}
        	while(G2.getVertex(1).getLabel() != 1){
        		GemVertex vtemp = G2.removerVertice(1);
        		G2.inserirVertice(vtemp);
        	}

        	for(int i = 1;i<=G2.getNumVertices();i++){        	
        		G2.getVertex(i).setLabel(novo++);
        	}
        	
        	GemVertex u = G1.getVertex(G1.getNumVertices()), v = G2.getVertex(1), t1, t2;
        	t1 = u.getYellow(); t2 = v.getYellow();
        	t1.setYellow(t2);
        	t2.setYellow(t1);
        	t1 = u.getBlue(); t2 = v.getBlue();
        	t1.setBlue(t2);
        	t2.setBlue(t1);
        	t1 = u.getRed(); t2 = v.getRed();
        	t1.setRed(t2);
        	t2.setRed(t1);
        	t1 = u.getGreen(); t2 = v.getGreen();
        	t1.setGreen(t2);
        	t2.setGreen(t1);
        	
        	G1.removerVertice(G1.getNumVertices());
        	
        	for(int i = 2;i<=G2.getNumVertices();i++){
        		G1.inserirVertice(G2.getVertex(i));
        	}
        	G1.goToCodeLabel().getCode();
        	return G1;
        	
        }
        return null;
    }
}
