package blink.cli;
import blink.GBlink;
import blink.Gem;
import blink.GemVertex;

import java.util.ArrayList;
import java.util.Random;
//-> src/blink/cli/CommandLineInterface.java -> 128 -> , new Fun...

/**
 * <p>
 * A {@link CommandLineInterface} command that generates a random
 * {@link GBlink} with 8 times the number given as parameter.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FunctionGenerateRandomGem extends Function {
	public FunctionGenerateRandomGem() {
		super("randg","Generate random Gem with 8*N vertexes");
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
		int N;
		Random rand;
		if( params.size() >= 1 && params.get(0) instanceof Number ) {
			N = ((Number)params.get(0)).intValue();
		} else throw new EvaluationException("You have to pass N as parameter");
		if( params.size() == 2 && params.get(1) instanceof Number ) {
			rand = new Random(((Number)params.get(1)).intValue());
		} else if( params.size() == 1 ) {
			rand = new Random(System.currentTimeMillis());
		} else throw new EvaluationException("The second parameter can be a seed to the pseudorandom number generator");

				
		int[] link = new int[4*N];
		link[0] = 1;
		link[1] = 0;
		link[2] = 3;
		link[3] = 2;
		
		for( int i = 1 ; i < N ; ++i ) {
			int x, y, s;
			do {
				x = rand.nextInt(4*i);
				y = nextEdge(link[x], -1);
			} while( x == y );
			for( s = 1 ; y != x ; ++s ) y = nextEdge(link[y],-1);
			int choice = rand.nextInt(s-1)+1;
			while( choice-- != 0 ) y = nextEdge(link[y],-1);

			addVertex(link, i, x, y);
		}
		for( int i = 0 ; i < N ; ++i ) if( rand.nextBoolean() ) spinVertex(link, i);

		Gem g = new Gem();
		GemVertex[] vert = new GemVertex[8*N];
		for( int i = 0 ; i < 8*N ; ++i ) {
			vert[i] = g.newVertex(i+1);
		}
		for( int i = 0 ; i < 4*N ; ++i ) {
			vert[2*i].setGreen(vert[2*i+1]);
			vert[2*i+1].setGreen(vert[2*i]);

			vert[2*i].setRed(vert[ 2*nextEdge(link[i], -1)+1 ]);
			vert[2*i+1].setRed(vert[ 2*link[nextEdge(i, 1)] ]);

			if( i % 2 == 0 ) {
				int v;
				for( v = link[i] ; v % 2 == 1 ; v = link[nextEdge(v,2)] );
				vert[2*i].setYellow(vert[ 2*nextEdge(v,-1)+1 ]);
				vert[2*i+1].setYellow(vert[ 2*nextEdge(i,-1) ]);
			} else {
				int v;
				vert[2*i].setYellow(vert[ 2*nextEdge(i, 1)+1 ]);
				for( v = link[nextEdge(i,1)] ; v % 2 == 1 ; v = link[nextEdge(v,2)] );  // ?
				vert[2*i+1].setYellow(vert[ 2*v ]);
			}
			vert[2*i].setBlue(vert[ i/4*8 + (3-2*i%8+8)%8 ]);
			vert[2*i+1].setBlue(vert[ i/4*8 + (2-2*i%8+8)%8 ]);
		}
		/* Debug
		for( int i = 0 ; i < 8*N ; ++i ) {
			System.out.print(vert[i].getLabel() + " -> ");
			System.out.print(vert[i].getGreen().getLabel() + ", ");
			System.out.print(vert[i].getRed().getLabel() + ", ");
			System.out.print(vert[i].getYellow().getLabel() + ", ");
			System.out.print(vert[i].getBlue().getLabel() + "\n");
		}
		*/
		return g.getVersionWithoutFourClusters();
	}

	static int nextEdge(int x, int d) {
		int e = (x+d+4)%4;
		return x/4*4 + e;
	}
	static void addVertex(int[] link, int n, int x, int y) {
		link[4*n] = x;
		link[4*n+1] = link[x];
		link[4*n+2] = y;
		link[4*n+3] = link[y];
		for( int i = 0 ; i < 4 ; ++i ) link[ link[4*n+i] ] = 4*n + i;
	}
	static void swapEdges(int[] link, int x, int y) {
		int tmp = link[x];
		link[x] = link[y];
		link[y] = tmp;

		x = link[x];
		y = link[y];
		tmp = link[x];
		link[x] = link[y];
		link[y] = tmp;
	}
	static void spinVertex(int[] link, int v) {
		for( int i = 0; i < 3; ++i) swapEdges(link, 4*v + i, 4*v + i + 1);
	}

}
