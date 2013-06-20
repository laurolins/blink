package blink.cli;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import blink.App;
import blink.Gem;
import blink.GemEntry;
import blink.GemPackedLabelling;
import blink.GemVertex;

public class FunctionIsGemResoluble extends Function {
	public FunctionIsGemResoluble() {
		super("isGemRes","description");
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
		if (params.get(0) instanceof Gem) {
			ArrayList<GemVertex> vertexList =
					((Gem) params.get(0)).getVertices();
			boolean result = isResoluble(vertexList); //G1
			if(!result){
				genGN(vertexList);
				result = result || isResoluble(vertexList); //G2
				if(!result){
					genGN(vertexList);
					result = result || isResoluble(vertexList); //G2
				}
			}
			return result;
		}
		return "Error!";
	}

	private void genGN(ArrayList<GemVertex> vertexList){
		GemVertex aux;
		for(GemVertex gv : vertexList){
			aux = gv.getBlue();
			gv.setBlue(gv.getRed());
			gv.setRed(gv.getGreen());
			gv.setGreen(aux);
		}
	}

	private int[] getGroups(ArrayList<GemVertex> vertexList
			, int from, int to){
		int[] vertex_mark = new int[vertexList.size() + 1];
		int group = 0;
		for(GemVertex gv : vertexList)
			selectGroup(gv, vertex_mark, ++group, from, to);
		return vertex_mark;
	}

	private void selectGroup(GemVertex gv, int[] vertex_mark, 
			int group, int from, int to){
		int vertexnum = gv.getLabel(); 
		if(vertex_mark[vertexnum] != 0)
			return;
		vertex_mark[vertexnum] = group;
		selectGroup(
				((from < 2)?((from < 1)?(gv.getYellow()):(gv.getBlue()))
					:((from < 3)?(gv.getRed()):(gv.getGreen()))), 
				vertex_mark, group, to, from);
	}
	
	private void populateGraphWithDm(HashSet<Integer>[] ledges, 
			int[] groups1, int[] groups2, int[] groups3){
		int i, j, size;
		size = groups1.length;
		for(i = 0; i < size; ++i)
			for(j = i + 1; j < size; ++j)
				if(groups1[i] == groups1[j]
						&& groups2[i] == groups2[j]
						&& groups3[i] != groups3[j]){
					ledges[i].add(j);
					ledges[j].add(i);
				}
	}
	
	private void setDm2(HashSet<Integer>[] ledges,
			ArrayList<GemVertex> vertexList){
		int[] groups1, groups2, groups3;
		groups1 = getGroups(vertexList, 0, 2);
		groups2 = getGroups(vertexList, 1, 3);
		groups3 = getGroups(vertexList, 2, 3);
		populateGraphWithDm(ledges, groups1, groups2, groups3);
	}
	
	private void setDm3(HashSet<Integer>[] ledges,
			ArrayList<GemVertex> vertexList){
		int[] groups1, groups2, groups3;
		groups1 = getGroups(vertexList, 0, 3);
		groups2 = getGroups(vertexList, 1, 2);
		groups3 = getGroups(vertexList, 3, 2);
		populateGraphWithDm(ledges, groups1, groups2, groups3);
	}
	
	private void graphInit(HashSet<Integer>[] ledges,
			ArrayList<GemVertex> vertexList) {
		int counter = 0;
		HashSet<Integer> hsaux;
		for(GemVertex gv : vertexList){
			hsaux = new HashSet<Integer>();
			hsaux.add(gv.getYellow().getLabel());
			hsaux.add(gv.getBlue().getLabel());			
			ledges[++counter] = hsaux;
		}
	}
	
	private boolean isConnected(HashSet<Integer>[] ledges){
		LinkedList<Integer> alaux = new LinkedList<>();
		Integer iaux;
		
		alaux.add(1);		
		while(!alaux.isEmpty()){
			iaux = alaux.remove();
			if(ledges[iaux] != null){
				alaux.addAll(ledges[iaux]);
				ledges[iaux] = null;
			}
		}
		
		for(HashSet<Integer> hsi : ledges)
			if(hsi != null)
				return false;
			
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean isResoluble(ArrayList<GemVertex> vertexList){
		HashSet<Integer>[] ledges = new HashSet[vertexList.size() + 1];
		graphInit(ledges, vertexList);
		
		setDm2(ledges, vertexList);
		setDm3(ledges, vertexList);
		
		return isConnected(ledges);
	}

	public static void main(String[] args) throws Exception {
		FunctionIsGemResoluble figr = new FunctionIsGemResoluble();
		ArrayList<Gem> alg = new ArrayList<>();
		FileOutputStream fos = new FileOutputStream("gem.txt");
		String aux;
		boolean res = true;
//		for(GemEntry ge : App.getRepositorio().getGems()){
//		}
//		int min = -1, minv = Integer.MAX_VALUE;
		for(int i = 1;i < 75634;i++){
//			Gem g = App.getRepositorio().getGemById(i).getGem();
////			res = figr.isResoluble(g.getVertices());
//////			res = figr.isResoluble(App.getRepositorio().getGemById(i).getGem().getVertices());
////			if(!res && (g.getVertices().size() < minv)){
////				minv = g.getVertices().size();
////				min = i;
////			}
			alg.clear();
			GemEntry ge = App.getRepositorio().getGemById(i);
			alg.add(ge.getGem());
			aux = ge.getLabelling().getLettersString(",") + " " +
					(figr.hardwork(alg, null)) + "\n";
			fos.write(aux.getBytes());
		}
//		System.out.println(minv + " " + min);
//		System.out.println(res);
		//res = figr.isResoluble((new Gem(new GemPackedLabelling("eabcdhfgmijklpnosqrvtuywxhosvyeipgxurnmqabltckwdjfvyhosxlcwtqpfkdgnjeriaumb"))).getVertices());
//		res = figr.isResoluble((new Gem(new GemPackedLabelling("cabfdeighljknminlckgfjamedhbgfnkjmlceidabh"))).getVertices());
		//System.out.println(res);
		//System.out.println(figr.isResoluble(g.getVertices()));
		//System.out.println(g.getVertices().size());
//		fos.close();
//		alg.add(App.getRepositorio().getGemById(49).getGem());
//		System.out.println(figr.hardwork(alg, null));
	}
}
