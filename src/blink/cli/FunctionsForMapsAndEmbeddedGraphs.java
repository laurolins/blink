package blink.cli;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import util.Library;

import maps.EmbeddedGraph;
import maps.Map;
import maps.grapheditor.Editor;

public class FunctionsForMapsAndEmbeddedGraphs {
}

class FunctionDrawEmbeddedGraph extends Function {
	public FunctionDrawEmbeddedGraph() {
		super("drawg","Draw embedded graph");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		ArrayList<EmbeddedGraph> graphs = new ArrayList<EmbeddedGraph>();
		if (params.size() > 0) {
			for (Object o: params) {
				EmbeddedGraph eg = (EmbeddedGraph) o;
				graphs.add(eg);
			}
		}

		if (graphs.size() > 0) {
			JFrame f = new JFrame("View Embedded Graphs");
			f.setBounds(0, 0, 800, 600);
			if (graphs.size() > 1) {
				JTabbedPane tp = new JTabbedPane();
				for (EmbeddedGraph eg: graphs) {
					Editor e = new Editor();
					e.setGraph(eg);
					tp.addTab("EGraph", e.getDrawPanel());
				}
				f.setContentPane(tp);
			}
			else {
				Editor e = new Editor();
				e.setGraph(graphs.get(0));
				f.setContentPane(e.getDrawPanel());
			}
			f.setVisible(true);
			return null;
		}
		else {
			JDialog f = new JDialog ((JFrame)null,"Create Embedded Graph",true);
			f.setBounds(0, 0, 800, 600);
			Editor e = new Editor();
			f.setContentPane(e.getDrawPanel());
			f.setVisible(true);
			return e.getGraph();
		}
	}
}

class FunctionMapOfEmbeddedGrap extends Function {
	public FunctionMapOfEmbeddedGrap() {
		super("map","Get map for embedded graph");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		ArrayList<EmbeddedGraph> graphs = new ArrayList<EmbeddedGraph>();
		if (params.size() > 0) {
			for (Object o: params) {
				EmbeddedGraph eg = (EmbeddedGraph) o;
				graphs.add(eg);
			}
		}
		
		ArrayList<Map> result = new ArrayList<Map>();
		for (EmbeddedGraph e: graphs) {
			result.add(e.getMap());
		}
		if (result.size() == 1)
			return result.get(0);
		else 
			return result;
	}
}

class FunctionGamma extends Function {
	public FunctionGamma() {
		super("gamma","Get all map permutations: map, dual, antidual, phial, antiphial, antimap");
	}
	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		Map m = (Map) params.get(0);
		ArrayList<Map> result = new ArrayList<Map>();
		result.add(m.copy());
		result.add(m.dual());
		result.add(m.antidual());
		result.add(m.phial());
		result.add(m.antiphial());
		result.add(m.antimap());
		return result;
	}
}

class FunctionBigon extends Function {
	public FunctionBigon() {
		super("bigon","Bigons of a map");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		Map map = (Map) params.get(0);
		Integer i = (Integer) params.get(1);
		Integer j = (Integer) params.get(2);
		return map.getBigons(Map.EdgeType.getEdgeTypeFromDefaultIndex(i), 
				Map.EdgeType.getEdgeTypeFromDefaultIndex(j));
	}
}

class FunctionSaveGraph extends Function {
	public FunctionSaveGraph() {
		super("savegraph","Save graph");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		String filename = (String) params.get(0);
		ArrayList<EmbeddedGraph> graphs = new ArrayList<EmbeddedGraph>();
		for (int i=1;i<params.size();i++)
			graphs.add((EmbeddedGraph)params.get(i));
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (EmbeddedGraph g: graphs) {
				bw.append(g.getSaveString());
				bw.append("\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		}
		return null;
	}
}

class FunctionLoadGraph extends Function {
	public FunctionLoadGraph() {
		super("loadgraph","Load graph");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		String filename = (String) params.get(0);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line;
			ArrayList<EmbeddedGraph> graphs = new ArrayList<EmbeddedGraph>();
			while ((line = br.readLine()) != null) {
				graphs.add(new EmbeddedGraph(line));
			}
			br.close();
			return graphs;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		}
	}
}

class FunctionDrawBigon extends Function {
	public FunctionDrawBigon() {
		super("drawbigon","Draw Bigon");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		params = extractObjectInASingleList(params);		
		ArrayList<EmbeddedGraph> egraphs = new ArrayList<EmbeddedGraph>();
		for (Object obj: params) {
			Map.Bigon b = (Map.Bigon) obj;
			EmbeddedGraph g = b.asGraph();
			egraphs.add(g);
		}		
		
		JFrame f = new JFrame("View Embedded Graphs");
		f.setBounds(0, 0, 800, 600);
		if (egraphs.size() > 1) {
			JTabbedPane tp = new JTabbedPane();
			int i = 1;
			for (EmbeddedGraph eg: egraphs) {
				Editor e = new Editor();
				e.setGraph(eg);
				tp.addTab("bigon "+i, e.getDrawPanel());
				i++;
			}
			f.setContentPane(tp);
		}
		else {
			Editor e = new Editor();
			e.setGraph(egraphs.get(0));
			f.setContentPane(e.getDrawPanel());
			e.fitView();
		}
		f.setVisible(true);
		return null;
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




class FunctionBigonGraph extends Function {
	public FunctionBigonGraph() {
		super("bigongraph","Draw Bigon");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		params = extractObjectInASingleList(params);		
		ArrayList<EmbeddedGraph> egraphs = new ArrayList<EmbeddedGraph>();
		for (Object obj: params) {
			Map.Bigon b = (Map.Bigon) obj;
			EmbeddedGraph g = b.asGraph();
			egraphs.add(g);
		}
		return egraphs;
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





/*
class FunctionDrawBigons extends Function {
	public Bigons() {
		super("drawbigons","Draw embedded graph");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		ArrayList<EmbeddedGraph> graphs = new ArrayList<EmbeddedGraph>();
		if (params.size() > 0) {
			for (Object o: params) {
				EmbeddedGraph eg = (EmbeddedGraph) o;
				graphs.add(eg);
			}
		}

		if (graphs.size() > 0) {
			JFrame f = new JFrame("View Embedded Graphs");
			f.setBounds(0, 0, 800, 600);
			if (graphs.size() > 1) {
				JTabbedPane tp = new JTabbedPane();
				for (EmbeddedGraph eg: graphs) {
					Editor e = new Editor();
					e.setGraph(eg);
					tp.addTab("EGraph", e.getDrawPanel());
				}
				f.setContentPane(tp);
			}
			else {
				Editor e = new Editor();
				e.setGraph(graphs.get(0));
				f.setContentPane(e.getDrawPanel());
			}
			f.setVisible(true);
			return null;
		}
		else {
			JDialog f = new JDialog ((JFrame)null,"Create Embedded Graph",true);
			f.setBounds(0, 0, 800, 600);
			Editor e = new Editor();
			f.setContentPane(e.getDrawPanel());
			f.setVisible(true);
			return e.getGraph();
		}
	}
}
 */