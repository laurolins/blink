package blink.cli;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import blink.App;
import blink.DrawPanel;
import blink.GBlink;
import blink.Gem;
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
        	System.out.println(G1.isGem() + " " + G2.isGem());
        	int novo = G1.getNumVertices()-1;
        	int L1 = (Integer) params.get(2);
        	int L2 = (Integer) params.get(3)+novo-1;
        	int dif1 = G1.getNumVertices() - L1;
        	int dif2 = (Integer) params.get(3)-1;
       		//System.out.println(G1.isGem() + " ai 2 " + G2.isGem());
        	for(int i = 1;i<=G1.getNumVertices();i++){
        		int newlabel = G1.getVertex(i).getLabel();
        		newlabel+=dif1;
        		if(newlabel > G1.getNumVertices()) newlabel -= G1.getNumVertices();
        		G1.getVertex(i).setLabel(newlabel);
        	}
        	//System.out.println(G1.isGem() + " ai 3 " + G2.isGem());
        	while(G1.getVertex(G1.getNumVertices()).getLabel() != G1.getNumVertices()){
        		GemVertex vtemp = G1.removerVertice(1);
        		//System.out.println(vtemp.getLabel());
        		G1.inserirVertice(vtemp);
        	}
        	for(int i = 1;i<=G2.getNumVertices();i++){
        		int newlabel = G2.getVertex(i).getLabel();
        		newlabel-=dif2;
        		//System.out.print(newlabel+ " ");
        		if(newlabel < 1 ) newlabel += G2.getNumVertices();
        		//System.out.println(newlabel);
        		G2.getVertex(i).setLabel(newlabel);
        	}
        	//System.out.println(G2.getStringWithNeighbours());
        	while(G2.getVertex(1).getLabel() != 1){
        		GemVertex vtemp = G2.removerVertice(1);
        	//	System.out.println("gema 2 " +vtemp.getLabel());
        		G2.inserirVertice(vtemp);
        	}

        	for(int i = 1;i<=G2.getNumVertices();i++){        	
        		G2.getVertex(i).setLabel(novo++);
        	}
        	System.out.println(G1.isGem() + " " + G2.isGem());
//        	for(int i = 0;i<G2.getNumVertices();i++){
//        		if(true){
//        			System.out.print(G2.getVertex(i+1).getLabel()+" ---> ");
//        			GemVertex t = G2.getVertex(i+1);
//        			System.out.print(t.getYellow().getLabel());
//        			System.out.print(", "+t.getBlue().getLabel());
//        			System.out.print(", "+t.getRed().getLabel());
//        			System.out.println(", "+t.getGreen().getLabel());
//        		}
//        	}
        	
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
        	//G2.removerVertice(1);
        	
//        	System.out.println("tamanho "+G1.getNumVertices());
        	for(int i = 2;i<=G2.getNumVertices();i++){
//        		System.out.println(G2.getVertex(i)+" "+i+ " " + G2.getNumVertices());
//        		System.out.println(G2.getVertex(i) == null);
        		G1.inserirVertice(G2.getVertex(i));
        	}
        	//System.out.println("vai");
        	//G1.ArrumarNumeracao();
        	for(int i = 1;i<=G1.getNumVertices();i++){
        		try{
        			G1.ArrumarGema(G1.getVertex(i));
        		}catch(Exception e){
        			System.out.println("erro "+i);
        		}
        	}
        	//System.out.println("check "+G1.check());
        	G1.warrantyParityLabelling();
//        	G1.getLastGoToCodeLabellingRootVertexLabel();
//        	System.out.println(G1.getStringWithNeighbours());
        //	System.out.println("foi");
//        	for(int i = 0;i<G1.getNumVertices();i++){
//        		if(true){
//        			System.out.print((i+1)+" --> ");
//        			GemVertex t = G1.getVertex(i+1);
//        			System.out.print(t.getYellow().getLabel());
//        			System.out.print(", "+t.getBlue().getLabel());
//        			System.out.print(", "+t.getRed().getLabel());
//        			System.out.println(", "+t.getGreen().getLabel());
//        		}
//        	}
        	
        	return G1;
        	
        	//System.out.println(G1.getStringWithNeighbours());
        	//G1 = G1.ArumarGema(t1);
        	//G1.getStringWithNeighbours();
        	//System.out.println(G1.getStringWithNeighbours());
//        	System.out.println("depois");
//        	for(int i = 0;i<G1.getNumVertices();i++){
//        		if(true){
//        			System.out.print((i+1)+" -> ");
//        			GemVertex t = G1.getVertex(i+1);
//        			System.out.print(t.getYellow().getLabel());
//        			System.out.print(", "+t.getBlue().getLabel());
//        			System.out.print(", "+t.getRed().getLabel());
//        			System.out.println(", "+t.getGreen().getLabel());
//        		}
//        	}
        }
        return null;
    }
}
