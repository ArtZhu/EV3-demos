import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.function.Function;


//Seeker.java


public class Seeker {
/*******************
 *  debug
 */	
	//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
	static boolean Debug = true;
	static boolean PRINT = true;
	
	//PORT AND IP  -----------------------------------------------------------------------------------------------------------
	static final String IPAddress = "136.167.109.36";
	static final int port = 6983;

	//Search Graph -----------------------------------------------------------------------------------------------------------
	static int[][] graph;
	static int[][] InitialGraph;
	static int gX, gY ;
	static int cellsX = 8;
	static int cellsY = 8;
	static double cellWidth = 30.0;
	
	public static void main(String[] args){
		
		genInitGraph();
		printGraph();
	}//main(String[])

	
	/*****
	 * one time InitialGraph setup
	 */
	static final String fileName = "obstacle.txt";
	private static int f(String x){return (int) (Double.valueOf(x) / cellWidth) + 1;}
	private static void genInitGraph(){
		InitialGraph = new int[cellsY][cellsX];
		for(int i=0; i<InitialGraph.length; i++) {
			InitialGraph[i][0] = -1;
			InitialGraph[i][cellsX-1] = -1;
		}
		
		for(int i=0; i<cellsX; i++){
			InitialGraph[0][i] = -1;
			InitialGraph[cellsY-1][i] = -1;
		}
		
		//READ FILE
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
		}catch(FileNotFoundException e) {System.out.println("fileNotFound");}
		
		
		Scanner sc = new Scanner(is);
		while(sc.hasNextLine()){
			String[] coord = sc.nextLine().split(" ");
			System.out.printf("(%s, %s)\n", coord[0], coord[1]);
			InitialGraph[f(coord[0])][f(coord[1])] = -1;
		}
		sc.close();
	}//genGraph
	
	//printing
    public static void printGraph(){
    	if(PRINT){
    		System.out.println("InitialGraph: ");
	    	for(int[] row: InitialGraph){
	    		for(int column: row)
	    			System.out.print(fill(String.valueOf(column), 4));
	    		System.out.print("\n");
	    	}	
    	}
    	System.out.println();
    }
    
    private static String fill(String s, int length){
    	length = length - s.length();
    	for(int i=0; i<length; i++)
    		s = s+" ";
    	return s;
    }
    
}
