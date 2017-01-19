
public class Maze {
	public static void main(String[] args){Search();}
	
	static int[][] graph;
	private static int wall = -1;
	private static int visit = -2;
	private static int open = 0;
	private static int cup = -3;
	
	public static boolean ignoreVisit = false;
	
	public static boolean free(int x, int y){
		if(ignoreVisit)
			return graph[x][y] != wall && graph[x][y] != cup;
		else
			return graph[x][y] == open;
	}
	
	public static boolean visited(int x, int y){
		return graph[x][y] == visit;
	}
	
	public static void mark(int x, int y){
		graph[x][y] = cup;
	}
	
	static int sX = 1;
	static int sY = 1;
	
	public static void genGraph(){
		graph = new int[7][7];
		for(int i=0; i<7; i++){
			graph[0][i] = wall;
			graph[6][i] = wall;
			graph[i][0] = wall;
			graph[i][6] = wall;
		}
	}
	
	private static int[] cups = {	1, 4,
									2, 3,
									4, 4, 
									5, 4
	};
	public static void setCups(int[] cups){
		for(int i=0; i<8; i+=2){
			int x = cups[i];
			int y = cups[i+1];
			mark(x, y);
		}
	}
	
	private static int[] curr = {sX, sY};
	private static int[] prev = {sX, sY - 1};

	public static boolean traversed(){
		for(int i=0; i<7; i++)
			for(int j=0; j<7; j++)
				if(graph[i][j] == open)
					return false;
		return true;
	}
	
	public static void printGraph(){
		for(int i=0; i<7; i++){
			for(int j=0; j<7; j++)
				if(i == curr[0] && j == curr[1])
					System.out.print(fill("C", 4));
				else
					System.out.print(fill(String.valueOf(graph[i][j]), 4));
				
			System.out.print("\n");
		}
/*	    	for(int[] row: graph){
	    		for(int column: row)
	    			System.out.print(fill(String.valueOf(column), 4));
	    		System.out.print("\n");
	    	}	
*/    	System.out.println();
    }
    
    private static String fill(String s, int length){
    	length = length - s.length();
    	for(int i=0; i<length; i++)
    		s = s+" ";
    	return s;
    }
	
	public static void Search(){
		genGraph();
		setCups(cups);
		
		int count = 0;
		while(!traversed()){
			if(count == 200)
				break;
			count ++;
			if(!takeLeft()){
				if(!takeFront())
					takeRight();
			}
			graph[curr[0]][curr[1]] = visit;
			printGraph();
		}
	}
	
	public static void takeRight(){
		if(curr[0] - prev[0] > 0){
			prev[0] = curr[0]; prev[1] = curr[1];
			if(free(curr[0], curr[1] - 1)){
				curr[1]--;
				System.out.println("TOOK RIGHT");
			}
			else{
				curr[0]--;
				ignoreVisit = true;
				System.out.println("TURNED AROUND");
			}
			return;
		}
		if(curr[0] - prev[0] < 0){
			prev[0] = curr[0]; prev[1] = curr[1];
			if(free(curr[0], curr[1] + 1)){
				curr[1]++;
				System.out.println("TOOK RIGHT");
			}
			else{
				curr[0]++;
				ignoreVisit = true;
				System.out.println("TURNED AROUND");
			}
			
			return;
		}
		if(curr[1] - prev[1] > 0){
			prev[0] = curr[0]; prev[1] = curr[1];
			if(free(curr[0] + 1, curr[1])){
				System.out.println("TOOK RIGHT");
				curr[0]++;
			}
			else{
				curr[1]--;
				ignoreVisit = true;
				System.out.println("TURNED AROUND");
			}
			
			return;
		}
		if(curr[1] - prev[1] < 0){
			prev[0] = curr[0]; prev[1] = curr[1];
			if(free(curr[0] - 1, curr[1])){
				System.out.println("TOOK RIGHT");
				curr[0]--;
			}
			else{
				curr[1]++;
				ignoreVisit = true;
				System.out.println("TURNED AROUND");
			}
			
			return;
		}
	}
	
	public static boolean takeLeft(){
		if(curr[0] - prev[0] > 0){
			if(free(curr[0], curr[1] + 1)){
				if(!visited(curr[0], curr[1] + 1))
					ignoreVisit = false;
				prev[0] = curr[0]; prev[1] = curr[1];
				curr[1] ++;
				return true;
			}
		}
		if(curr[0] - prev[0] < 0){
			if(free(curr[0], curr[1] - 1)){
				if(!visited(curr[0], curr[1] - 1))
					ignoreVisit = false;
				prev[0] = curr[0]; prev[1] = curr[1];
				curr[1] --;
				return true;
			}
		}
		if(curr[1] - prev[1] > 0){
			if( free(curr[0] - 1, curr[1])){
				if(!visited(curr[0] - 1, curr[1]))
					ignoreVisit = false;
				prev[0] = curr[0]; prev[1] = curr[1];
				curr[0] --;
				return true;
			}
		}
		if(curr[1] - prev[1] < 0){
			if(free(curr[0] + 1, curr[1])){
				if(!visited(curr[0] + 1, curr[1]))
					ignoreVisit = false;
				prev[0] = curr[0]; prev[1] = curr[1];
				curr[0] ++;
				return true;
			}
		}
		System.out.println("LEFT NOT TAKEN");
		return false;
	}
	
	public static boolean takeFront(){
		if(curr[0] - prev[0] > 0){
			if(free(curr[0] + 1, curr[1])){
				if(!visited(curr[0] + 1, curr[1]))
					ignoreVisit = false;
				prev[0] = curr[0]; prev[1] = curr[1];
				curr[0] ++;
				return true;
			}
		}
		if(curr[0] - prev[0] < 0){
			if(free(curr[0] - 1, curr[1])){
				if(!visited(curr[0] - 1, curr[1]))
					ignoreVisit = false;
				prev[0] = curr[0]; prev[1] = curr[1];
				curr[0] --;
				return true;
			}
		}
		if(curr[1] - prev[1] > 0){
			if( free(curr[0], curr[1] + 1)){
				if(!visited(curr[0], curr[1] + 1))
					ignoreVisit = false;
				prev[0] = curr[0]; prev[1] = curr[1];
				curr[1] ++;
				return true;
			}
		}
		if(curr[1] - prev[1] < 0){
			if(free(curr[0], curr[1] - 1)){
				if(!visited(curr[0], curr[1] - 1))
					ignoreVisit = false;
				prev[0] = curr[0]; prev[1] = curr[1];
				curr[1] --;
				return true;
			}
		}
		System.out.println("FRONT NOT TAKEN");
		return false;
	}
}
