package blink;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;


import edu.uci.ics.jung.utils.MutableInteger;

import linsoft.Pair;
import linsoft.graph.OrthogonalLayout;
import linsoft.graph.PREdge;
import linsoft.graph.PRVertex;
import linsoft.graph.PlanarRepresentation;

public class LnkGen {
	int colorPoint[];
	AffineTransform T;
	ArrayList<Point2D> points;
	int[] edges, edcross;

	public LnkGen(GBlink gblink){
		this.points = new ArrayList<Point2D>();

		PlanarRepresentation P = Library.getLinkPlanarRepresentation(gblink, -1);
		new OrthogonalLayout(P);

		HashMap<Integer, Integer> colors = 
				new HashMap<Integer, Integer>();
		HashMap<Point2D, SimpleEntry<Point2D, Integer>[]> crosses = 
				new HashMap<Point2D, SimpleEntry<Point2D, Integer>[]>();
		HashMap<GBlinkVertex, Point2D> vert2point = 
				new HashMap<GBlinkVertex, Point2D>();
		HashMap<Point2D, Integer[]> vert2crosses = 
				new HashMap<Point2D, Integer[]>();
		ArrayList<Integer[]> biedges = new ArrayList<Integer[]>();
		ArrayList<SimpleEntry<Integer, Integer>> crossedges = 
				new ArrayList<SimpleEntry<Integer,Integer>>();
		int color, colorIndex, index, index2;
		SimpleEntry<Point2D, Integer>[] points4;
		SimpleEntry<Point2D, Integer> point4init, point4end;
		Point2D upoint, vpoint, wpoint;
		SimpleEntry<Integer, Integer> edgentry;
		Integer[] bed, baux;

		double minX = +1e+20;
		double minY = +1e+20;
		double maxX = -1e+20;
		double maxY = -1e+20;
		

		for (GBlinkVertex v: gblink.getVertices()) {
			GBlinkVertex vv = v.getVertexAtTheSameGEdgeWithMinLabel();
			PRVertex pv = P.findVertexByObject(vv);
			upoint = new Point2D.Double(pv.getX(), pv.getY());
			vert2point.put(v, upoint);
			if(!crosses.containsKey(upoint)){
				crosses.put(upoint, new SimpleEntry[4]);
				vert2crosses.put(upoint, new Integer[2]);
			}
		}
		
		colorIndex=0;
		for (Variable z: gblink.getGZigZags()) {
			for (GBlinkVertex v: z.getVertices())
				colors.put(v.getLabel(), colorIndex);
			++colorIndex;
		}
		this.colorPoint = new int[colorIndex];
		for(index = 0; index < colorIndex; ++index)
			this.colorPoint[index] = -1;

		
		for (GBlinkVertex u : gblink.getVertices()) {
			if (u.hasEvenLabel()){
				continue;
			}

			GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.edge);
			PREdge e = P.findEdgeByObject(new Pair(u,v));
			double[] path = P.getPathOfEdge(e.getId());

			ArrayList<Point2D> gp = new ArrayList<Point2D>((int)(path.length/2));
			for (int i = 0; i < path.length; i += 2){
				gp.add(new Point2D.Double(path[i], path[i+1]));
				if (path[i] > maxX) maxX = path[i];
				if (path[i] < minX) minX = path[i];
				if (path[i+1] > maxY) maxY = path[i+1];
				if (path[i+1] < minY) minY = path[i+1];
			}

			upoint = vert2point.get(u);
			vpoint = vert2point.get(v);			
			if(path.length > 4){
				index = this.points.size();
				color = colors.get(v.getLabel());
				if(this.colorPoint[color] == -1)
					this.colorPoint[color] = index;

				for(index2 = 1; index2 < (gp.size() - 1); ++index2){
					this.points.add(gp.get(index2));
					bed = new Integer[2];
					bed[0] = this.points.size();
					bed[1] = bed[0] - 2;
					biedges.add(bed);
				}
				biedges.get(index)[1] = -1;
				index2 = biedges.size() - 1;
				biedges.get(index2)[0] = biedges.get(index2)[1];
				biedges.get(index2)[1] = -1;
				
				index2 = ((u.undercross())?2:0);
				points4 = crosses.get(upoint);
				if(points4[index2] != null) ++index2;
				points4[index2] = new SimpleEntry<Point2D, Integer>(this.points.get(index), index);
				index = this.points.size() - 1;
				index2 = ((v.undercross())?2:0);
				points4 = crosses.get(vpoint);
				if(points4[index2] != null) ++index2;
				points4[index2] = new SimpleEntry<Point2D, Integer>(this.points.get(index), index);
			} else {
				index2 = ((u.undercross())?2:0);
				points4 = crosses.get(upoint);
				if(points4[index2] != null) ++index2;
				points4[index2] = new SimpleEntry<Point2D, Integer>(vpoint, -1);
				index2 = ((v.undercross())?2:0);
				points4 = crosses.get(vpoint);
				if(points4[index2] != null) ++index2;
				points4[index2] = new SimpleEntry<Point2D, Integer>(upoint, -1);
			}
		}
		
		for(Entry<Point2D, SimpleEntry<Point2D, Integer>[]> map4point : crosses.entrySet()){
			points4 = map4point.getValue();
			if(points4[0] != null){
				if(points4[0].getValue() == -1){
					point4end = points4[0]; 
					points4[0] = points4[1]; 
					points4[1] = point4end; 
				}
				if(points4[0].getValue() != -1){
					point4init = points4[0];
					point4end = points4[1];
					vpoint = map4point.getKey();
					index2 = crossedges.size();
					edgentry = 
							new SimpleEntry<Integer, Integer>(point4init.getValue(), -1);
					crossedges.add(edgentry);
					vert2crosses.get(vpoint)[0] = index2;
					points4[0] = null;
					points4[1] = null;
					while(point4end.getValue() == -1){
						wpoint = point4end.getKey();
						points4 = crosses.get(wpoint);
						index = -1;
						if(points4[0] != null && points4[0].getKey().equals(vpoint))
							index = 1;
						else if(points4[1] != null && points4[1].getKey().equals(vpoint))
							index = 0;
						else if(points4[2] != null && points4[2].getKey().equals(vpoint))
							index = 3;
						else if(points4[3] != null && points4[3].getKey().equals(vpoint))
							index = 2;
						vert2crosses.get(wpoint)[((index > 1)?1:0)] = index2;
						points4[index + ((index%2==0)?1:-1)] = null;
						point4end = points4[index];
						points4[index] = null;
						vpoint = wpoint;
					}
					edgentry.setValue(point4end.getValue());
					bed = biedges.get(point4init.getValue());
					bed[(bed[0] == -1)?0:1] = point4end.getValue();
					bed = biedges.get(point4end.getValue());
					bed[(bed[0] == -1)?0:1] = point4init.getValue();
				}
			}

			points4 = map4point.getValue();
			if(points4[2] != null){
				if(points4[2].getValue() == -1){
					point4end = points4[2]; 
					points4[2] = points4[3]; 
					points4[3] = point4end; 
				}
				if(points4[2].getValue() != -1){
					point4init = points4[2];
					point4end = points4[3];
					points4[2] = null;
					points4[3] = null;
					vpoint = map4point.getKey();
					index2 = crossedges.size();
					edgentry = 
							new SimpleEntry<Integer, Integer>(point4init.getValue(), -1);
					crossedges.add(edgentry);
					vert2crosses.get(vpoint)[1] = index2;
					while(point4end.getValue() == -1){
						wpoint = point4end.getKey();
						points4 = crosses.get(wpoint);
						index = -1;
						if(points4[0] != null && points4[0].getKey().equals(vpoint))
							index = 1;
						else if(points4[1] != null && points4[1].getKey().equals(vpoint))
							index = 0;
						else if(points4[2] != null && points4[2].getKey().equals(vpoint))
							index = 3;
						else if(points4[3] != null && points4[3].getKey().equals(vpoint))
							index = 2;
						vert2crosses.get(wpoint)[((index > 1)?1:0)] = index2;
						points4[index + ((index%2==0)?1:-1)] = null;
						point4end = points4[index];
						points4[index] = null;
						vpoint = wpoint;
					}
					edgentry.setValue(point4end.getValue());
					bed = biedges.get(point4init.getValue());
					bed[(bed[0] == -1)?0:1] = point4end.getValue();
					bed = biedges.get(point4end.getValue());
					bed[(bed[0] == -1)?0:1] = point4init.getValue();
				}
			}

		}
		
		this.edges = new int[this.points.size()];
		for(int icolor : colorPoint){
			bed = biedges.get(icolor);
			while(bed != null){
				biedges.set(icolor, null);
				baux = biedges.get(bed[0]);
				if(baux != null){
					this.edges[icolor] = bed[0];
					icolor = bed[0];
					bed = baux;
				} else{
					this.edges[icolor] = bed[1];
					icolor = bed[1];
					bed = biedges.get(icolor);
				}
			}
		}
		
		for(SimpleEntry<Integer, Integer> onecedge : crossedges){
			index = onecedge.getKey();
			index2 = onecedge.getValue();
			if(this.edges[index] == index2)
				onecedge.setValue(index);
			else if(this.edges[index2] == index)
				onecedge.setValue(index2);
			else
				System.out.println("ERRORRRRR!!!!!");
		}
		
		this.edcross = new int[crosses.size() * 2];
		index = 0;
		for(Integer[] crosseds : vert2crosses.values()){
			this.edcross[index++] = crossedges.get(crosseds[0]).getValue();
			this.edcross[index++] = crossedges.get(crosseds[1]).getValue();
		}

		this.T = createAffineTransform(maxX, minX, maxY, minY);
	}

	private AffineTransform createAffineTransform(double maxX, double minX, double maxY, double minY){
		AffineTransform at = new AffineTransform();
		if (minX == maxX) {
			minX = minX - 0.5;
			maxX = maxX + 0.5;
		}
		if (minY == maxY) {
			minY = minY - 0.5;
			maxY = maxY + 0.5;
		}
		at.scale(1.0/(maxX-minX),1.0/(maxY-minY));
		at.translate(-minX,-minY);
		return at;
	}

	public void changeAffineTransform(double margin, double x0, double y0, double w, double h){
		this.T.translate(x0 + margin * w, y0 + h * margin);
		this.T.scale(w * (1.0 - 1.0 * margin), h *(1.0 - 1.0 * margin));
	}
	
	private void applyAffineTransform(){
		for(Point2D pt : this.points)
			this.T.transform(pt, pt);
	}
	
	private StringBuffer genContent(){
		StringBuffer sb = new StringBuffer("% Link Projection\n");
		genColors(sb);
		genPoints(sb);
		genEdges(sb);
		genCross(sb);
		sb.append("-1\n");
		return sb;
	}
	
	private void genColors(StringBuffer sb){
		sb.append(colorPoint.length + "\n");
		for(int color : colorPoint)
			sb.append("\t" + color + "\t" + color + "\n");
	}
	
	private void genPoints(StringBuffer sb){
		applyAffineTransform();
		sb.append(this.points.size() + "\n");
		for(Point2D pt : this.points)
			sb.append("\t" + Math.round(pt.getX()) + "\t" + Math.round(pt.getY()) + "\n");
	}
	
	private void genEdges(StringBuffer sb){
		sb.append(this.edges.length + "\n");
		for(int edge : this.edges)
			sb.append("\t" + edge + "\t" + this.edges[edge] + "\n");
	}
	
	private void genCross(StringBuffer sb){
		int index = (this.edcross.length/2);
		sb.append(index + "\n");
		index = 0;
		for(int cross : this.edcross){
			sb.append("\t" + cross);
			if((++index % 2) == 0)
				sb.append("\n");
		}
	}
	
	public void genLnkFile(String filename) throws Exception{
		FileOutputStream fos = new FileOutputStream(filename);
		fos.write(this.genContent().toString().getBytes());
	}

//	public static void main(String[] args) throws Exception {
//		GBlink gblink = App.getRepositorio().getBlinksByIDs(3000).get(0).getBlink();
//		LnkGen lnk = new LnkGen(gblink);
//		lnk.changeAffineTransform(0.1, 0.0, 0.0, 500.0, 500.0);
//		lnk.genLnkFile("test.lnk");
//	}
}
