package examples;

import blink.Gem;
import blink.GemExhaustiveSimplifier;
import blink.GemPackedLabelling;
import blink.GemSimplificationPathFinder;
import blink.Path;

public class AttractorExample {

	public static void test(Gem gem_1, Gem gem_2) {

		long max_time = 100000;

		Gem gem_1_att = null;
		Gem gem_2_att = null;
		
		{ // Find Attractor of First Gem
			
			GemExhaustiveSimplifier exhaustive_simplifier = 
					new GemExhaustiveSimplifier(gem_1, max_time);

			gem_1_att = exhaustive_simplifier.getBestAttractorFound();
			// int target_gem_ts_class_size = exhaustive_simplifier.getBestAttractorTSClassSize();
	        // boolean target_gem_is_ts_representant = exhaustive_simplifier.isBestAttractorTSClassRepresentant();
	        Path path_to_gem_1_att = exhaustive_simplifier.getBestPath();

	        System.out.println("=======================================================");
	        System.out.println("=================== GEM 1 ATTRACTOR ===================");
	        System.out.println("Gem 1:");
	        System.out.println("Num. Vertices: "+gem_1.getNumVertices());
	        System.out.println(gem_1.getCurrentLabelling().getLettersString(","));

	        System.out.println("Path:");
	        System.out.println(path_to_gem_1_att.getSignature());
	        
	        System.out.println("Gem Att. 1:");
	        System.out.println("Num. Vertices: "+gem_1_att.getNumVertices());
	        System.out.println(gem_1_att.getCurrentLabelling().getLettersString(","));
	        System.out.println("=================== GEM 1 ATTRACTOR ===================");
	        System.out.println("=======================================================");
		}
		
		{ // Find Attractor of Second Gem 
			
			GemExhaustiveSimplifier exhaustive_simplifier = 
					new GemExhaustiveSimplifier(gem_2, max_time);

			gem_2_att = exhaustive_simplifier.getBestAttractorFound();
			// int target_gem_ts_class_size = exhaustive_simplifier.getBestAttractorTSClassSize();
	        // boolean target_gem_is_ts_representant = exhaustive_simplifier.isBestAttractorTSClassRepresentant();
	        Path path_to_gem_2_att = exhaustive_simplifier.getBestPath();

	        System.out.println("=======================================================");
	        System.out.println("=================== GEM 2 ATTRACTOR ===================");
	        System.out.println("Gem 2:");
	        System.out.println("Num. Vertices: "+gem_2.getNumVertices());
	        System.out.println(gem_2.getCurrentLabelling().getLettersString(","));

	        System.out.println("Path:");
	        System.out.println(path_to_gem_2_att.getSignature());
	        
	        System.out.println("Gem Att. 2:");
	        System.out.println("Num. Vertices: "+gem_2_att.getNumVertices());
	        System.out.println(gem_2_att.getCurrentLabelling().getLettersString(","));
	        System.out.println("=================== GEM 2 ATTRACTOR ===================");
	        System.out.println("=======================================================");
		}

		// 
        System.out.println("=======================================================");
        System.out.println("======================= RESULT ========================");
        System.out.println(gem_1_att.getCurrentLabelling().getLettersString(","));
        System.out.println(gem_2_att.getCurrentLabelling().getLettersString(","));
        System.out.println("======================= RESULT ========================");
        System.out.println("=======================================================");
		
//
//		int  starting_num_umoves = 3;
//		int  source_gem_ts_class_size = 1;
//		GemSimplificationPathFinder simplification_box = 
//				new GemSimplificationPathFinder(
//						source_gem, 
//						starting_num_umoves, 
//						max_time, 
//						source_gem_ts_class_size);
//

//		Gem target_gem = exhaustive_simplifier.getBestAttractorFound();
//		int target_gem_ts_class_size = exhaustive_simplifier.getBestAttractorTSClassSize();
//        boolean target_gem_is_ts_representant = exhaustive_simplifier.isBestAttractorTSClassRepresentant();
//        Path path_source_gem_to_target_gem = exhaustive_simplifier.getBestPath();
		
        // R 26, 3
        // R 24, 5
        
//        System.out.println("Source Gem:");
//        System.out.println("Num. Vertices: "+source_gem.getNumVertices());
//        System.out.println(source_gem.getCurrentLabelling().getLettersString(","));
//
//        System.out.println("Path:");
//        System.out.println(path_source_gem_to_target_gem.getSignature());
//        
//        System.out.println("Target Gem:");
//        System.out.println("Num. Vertices: "+target_gem.getNumVertices());
//        System.out.println(target_gem.getCurrentLabelling().getLettersString(","));

	}

	public static void simplify(Gem gem) {
		
		long max_time = 3000;

		Gem gem_att = null;
		
		GemExhaustiveSimplifier exhaustive_simplifier = 
				new GemExhaustiveSimplifier(gem, max_time);

		gem_att = exhaustive_simplifier.getBestAttractorFound();
		// int target_gem_ts_class_size = exhaustive_simplifier.getBestAttractorTSClassSize();
		// boolean target_gem_is_ts_representant = exhaustive_simplifier.isBestAttractorTSClassRepresentant();
		Path path_to_gem_1_att = exhaustive_simplifier.getBestPath();

		System.out.println("=======================================================");
		System.out.println("=================== GEM ATTRACTOR ====================");
		System.out.println("Gem 1, " + "Num. Vertices: "+gem.getNumVertices());
		System.out.println(gem.getCurrentLabelling().getLettersString(","));

		System.out.println("Path:");
		System.out.println(path_to_gem_1_att.getSignature());

		System.out.println("Gem Att. 1, " + "Num. Vertices: "+gem_att.getNumVertices());
		System.out.println(gem_att.getCurrentLabelling().getLettersString(","));
		System.out.println("=================== GEM ATTRACTOR =====================");
		System.out.println("=======================================================");
		
	}
	
	public static void test_R_24_32_R_24_34() {

		// Gem, Computers & Attractors for 3 Manifolds - Pag. 229
		//
		// R24 32 
		// NVert: 24  HG: 12^1 Handle: 0 Code: dabcgefjhilk,jiledckgbahf,hjekcbladfig
		// R24 34
		// NVert: 24  HG: 12^1 Handle: 0 Code: dabcgefjhilk,jiledckgbahf,ihegckdlafjb
		//
		Gem R_24_32 = new Gem(new GemPackedLabelling("dabcgefjhilkjiledckgbahfhjekcbladfig"));
		Gem R_24_34 = new Gem(new GemPackedLabelling("dabcgefjhilkjiledckgbahfihegckdlafjb"));
		
		test(R_24_32, R_24_34);
		
	}
	
	public static void test_R_26_3_R_24_5() {

		//
		// R26 3
		// NVert: 26  HG: 4^2 Handle: 0 Code: cabfdeighkjml,ildcjgfemhbka,dmgacieklbhfj
		// R24 5 
		// NVert: 24  HG: 4^2 Handle: 0 Code: cabfdeighljk,ildckgfjaheb,hkfbjielcgda
		//
		Gem R_26_3 = new Gem(new GemPackedLabelling("cabfdeighkjmlildcjgfemhbkadmgacieklbhfj"));
		Gem R_24_5 = new Gem(new GemPackedLabelling("cabfdeighljkildckgfjahebhkfbjielcgda"));

		test(R_26_3, R_24_5);

	}

	public static void test_U_1466() {
        
		//
		// U_1466
		//
        // NVert: 72  HG: 1^1 Handle: 0 
		//    Code: fabcdejghinklmqopsrvtuywxBzAECDHFGJI,qvwEJgfHAkjxuonsatprmbclzyiCBGdIDhFe,txCIhoBmdFyvpgselqukfrnAajDwHbzEiJGc

		Gem U_1466 = new Gem(new GemPackedLabelling("fabcdejghinklmqopsrvtuywxBzAECDHFGJIqvwEJgfHAkjxuonsatprmbclzyiCBGdIDhFetxCIhoBmdFyvpgselqukfrnAajDwHbzEiJGc"));

		simplify(U_1466);
	}
	
	
	public static void main(String[] args) {

		// test_R_24_32_R_24_34();
		// test_R_26_3_R_24_5();
		test_U_1466();

	}
	
}


//Gem source_gem = new Gem(
//new GemPackedLabelling("cabfdeighkjmlildcjgfemhbkadmgacieklbhfj"));
//
//// U[1466]: "fabcdejghinklmqopsrvtuywxBzAECDHFGJIqvwEJgfHAkjxuonsatprmbclzyiCBGdIDhFetxCIhoBmdFyvpgselqukfrnAajDwHbzEiJGc"
//
////GemPackedLabelling lbl = 
////new GemPackedLabelling("fabcdejghinklmqopsrvtuywxBzAECDHFGJIqvwEJgfHAkjxuonsatprmbclzyiCBGdIDhFetxCIhoBmdFyvpgselqukfrnAajDwHbzEiJGc");
////Gem source_gem = new Gem(lbl);
//
//int  starting_num_umoves = 3;
//long max_time = 100000;
//int  source_gem_ts_class_size = 1;
//
//GemExhaustiveSimplifier exhaustive_simplifier = 
//new GemExhaustiveSimplifier(
//		source_gem,
//		max_time);
//
////GemSimplificationPathFinder simplification_box = 
////new GemSimplificationPathFinder(
////		source_gem, 
////		starting_num_umoves, 
////		max_time, 
////		source_gem_ts_class_size);
//
//Gem target_gem = exhaustive_simplifier.getBestAttractorFound();
//
//int target_gem_ts_class_size = exhaustive_simplifier.getBestAttractorTSClassSize();
//
//boolean target_gem_is_ts_representant = exhaustive_simplifier.isBestAttractorTSClassRepresentant();
//
//Path path_source_gem_to_target_gem = exhaustive_simplifier.getBestPath();
//
//// R 26, 3
//// R 24, 5
//
//System.out.println("Source Gem:");
//System.out.println("Num. Vertices: "+source_gem.getNumVertices());
//System.out.println(source_gem.getCurrentLabelling().getLettersString(","));
//
//System.out.println("Path:");
//System.out.println(path_source_gem_to_target_gem.getSignature());
//
//System.out.println("Target Gem:");
//System.out.println("Num. Vertices: "+target_gem.getNumVertices());
//System.out.println(target_gem.getCurrentLabelling().getLettersString(","));

