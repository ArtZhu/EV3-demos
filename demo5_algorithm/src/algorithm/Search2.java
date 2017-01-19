package algorithm;

//Search.java
//change to lowercase search
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import adapter.genGraph;

public class Search2{
	private Search2(){}
    public static int[][] InitialGraph; //never do search on this graph, it is for reset graph.
    
     
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Search from source, i.e. path from source
    static int sX;    
    static int sY;
	public static Point source;	//source
	
    private static int[][] graph;	    public static int[][] getGraph(){return graph;}
    
    //Point predicate---------------------------------------------------------------------------------------------------------
    static Predicate<Point> notObs = (p -> graph[p.X][p.Y] != -1);
	static Predicate<Point> free = (p -> graph[p.X][p.Y] == 0);

	//Path predicate----------------------------------------------------------------------------------------------------------
	static Predicate<Point> hasPathTo = (free.negate().and(notObs));
	static Predicate<Point> notBack = (p -> p.X!=sX || p.Y!=sY);
	static BiPredicate<Point, Point> oneLess = ((p1, p2) -> graph[p1.X][p1.Y] - graph[p2.X][p2.Y] == -1);
	
	//BestPath predicate------------------------------------------------------------------------------------------------------
	static BiPredicate<Point, Point> oneMore = ((p1, p2) -> graph[p1.X][p1.Y] - graph[p2.X][p2.Y] == 1);
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	//Search from goal, i.e. path to goal
	static int gX;
	static int gY;
	public static Point goal;		//goal

    public static int[][] graph_goal;	public static int[][] getgraph_goal(){return graph_goal;}
    
  //Point predicate---------------------------------------------------------------------------------------------------------
    static Predicate<Point> notObs1 = (p -> graph_goal[p.X][p.Y] != -1);
	static Predicate<Point> free1 = (p -> graph_goal[p.X][p.Y] == 0);

	//Path predicate----------------------------------------------------------------------------------------------------------
	static Predicate<Point> hasPathTo1 = (free1.negate().and(notObs1));
	static Predicate<Point> notBack1 = (p -> p.X!=gX || p.Y!=gY);
	static BiPredicate<Point, Point> oneLess1 = ((p1, p2) -> graph_goal[p1.X][p1.Y] - graph_goal[p2.X][p2.Y] == -1);
	
	//BestPath predicate------------------------------------------------------------------------------------------------------
	static BiPredicate<Point, Point> oneMore1 = ((p1, p2) -> graph_goal[p1.X][p1.Y] - graph_goal[p2.X][p2.Y] == 1);	
	static BiPredicate<Point, Point> pathFlag = ((p1, p2) -> p1.Y - p2.Y != 0);
	static BiPredicate<Point, Point> arriveAt = ((p1, p2) -> p1.X==p2.X && p1.Y==p2.Y);
	
	//Control Flag------------------------------------------------------------------------------------------------------------
	static boolean PRINT = true;
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	/**
	 * First Thing to do
	 */
    public static void setSource(Point _source){
    	source = _source;
    	sX = source.X;
    	sY = source.Y;
    }
    
    
    public static void setGoal(Point _goal){
    	goal = _goal;
    	gX = goal.X;
    	gY = goal.Y;
    }
    
	/**
     * STATIC
     * IMPORTANT //set the graph and search.
     */
    public static void setGraph(int[][] _graph){
    	InitialGraph = copy(_graph);
    	resetGraph();
    	waveFront(); waveFront1();
    	printGraph();
    }
    private static void resetGraph(){graph = copy(InitialGraph); graph_goal = copy(InitialGraph);}
    
    private static int[][] copy(int[][] _graph){
    	int[][] newGraph = new int[_graph.length][];
    	for(int i=0; i<_graph.length; i++)
    		newGraph[i] = new int[_graph[0].length];
    	for(int i=0; i<_graph.length; i++)
    		for(int j=0; j<_graph.length; j++)
    			newGraph[i][j] = _graph[i][j];
    	return newGraph;
    }
    
    /**
     * THE ACTUAL SEARCH FROM SOURCE, on graph
     */
    private static void waveFront(){
    	if(!free.test(source)){
			System.out.println("Cannot start at obstacle; graph not searched");
			return;
    	}
        Queue<Point> queue = new LinkedList<Point>();
        graph[sX][sY] = 1;
        
        queue.add(source);
        while(!queue.isEmpty()){
            Point curr = queue.poll();
            int val = graph[curr.X][curr.Y] + 1;
            neighbor(curr).stream().filter(free).forEach(p -> {
            	graph[p.X][p.Y] = val;
            	queue.add(p);
            });
        }
    }
    
    /**
     * THE ACTUAL SEARCH FROM GOAL, on graph_goal
     */
    private static void waveFront1(){
    	if(!free1.test(goal)){
			System.out.println("Cannot set goal at obstacle; graph not searched");
			return;
    	}
        Queue<Point> queue = new LinkedList<Point>();
        graph_goal[gX][gY] = 1;
        

        queue.add(goal);
        while(!queue.isEmpty()){
            Point curr = queue.poll();
            int val = graph_goal[curr.X][curr.Y] + 1;
            neighbor(curr).stream().filter(free1).forEach(p -> {
            	graph_goal[p.X][p.Y] = val;
            	queue.add(p);
            });
        }
    }
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     
    /**
     * IMPORTANT //mark a spot on the initial graph and redo the search.
     * @param x
     * @param y
     */
    public static void mark(Point... points){
    	Arrays.asList(points).forEach(p -> InitialGraph[p.X+1][p.Y+1] = -1);
    	resetGraph();
    	waveFront(); waveFront1();
    	printGraph();
    }	
    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    /**
     * Random Path, might not be the best, on graph, i.e. from source
     */
    public static Iterable<Point> pathTo(Point p){
    	if(!hasPathTo.test(p)) return null;
    	Stack<Point> path = new Stack<Point>();
    	Point curr = p;
    	path.push(p);
    	while(notBack.test(curr)){
    		findPath:
    			for(Point p1: neighbor(curr)){
    				if(oneLess.test(p1, curr)){
    					curr = p1;
    					path.push(curr);
    					break findPath;
    				}
    			}
    	}
    	return path;
    }	


    /**
     * Random Path, on graph_goal, i.e. to goal
     */
    public static Iterable<Point> pathFrom(Point p){
        if(!hasPathTo.test(p)) return null;
        Stack<Point> path = new Stack<Point>();
        Point curr = p;
		path.push(p);
        while(notBack1.test(curr)){
			findPath:
			for(Point p1: neighbor(curr)){
				if(oneLess1.test(p1, curr)){
					curr = p1;
					path.push(curr);
					break findPath;
				}
			}
        }
        return path;
    }
    
    /**
     * best path finding method
     */
    private static LinkedList<Point> bestPath;
    public static LinkedList<Point> bestPathFrom(Point p){
        if(!hasPathTo1.test(p)) return null;
        
    	Point2 curr = new Point2(p);
    	bestPath = new LinkedList<Point>();
    	bestPath.push(curr);
    	int step = 0;
    	while(notBack1.test(curr)){
    		System.out.printf("curr = (%d, %d)\n", curr.X, curr.Y);
    		curr = bestPathStep(curr, step == 0);
    		step ++;
    	}
    	bestPath.addFirst(bestPath.pollLast());
    	return bestPath;
    }
    
    private static Point2 bestPathStep(Point2 p, boolean step0){
        List<Point2> neighborhood = new ArrayList<Point2>();
		for(Point2 newP: neighbor(p)){
			if(!notBack1.test(newP)) {
				bestPath.add(newP);
				return newP;
			}
			if(oneLess1.test(newP, p)){
				if(!step0){
					if(pathFlag.test(p, newP) != p.dir){
						newP.dir = !p.dir;
						newP.turn = p.turn + 1;
						neighborhood.add(newP);
					}
					else{
						newP.dir = p.dir;
						newP.turn = p.turn;
						neighborhood.add(newP);
					}
					System.out.printf("Direction is Y: %b \n", pathFlag.test(p, newP));
				}
				else {
					newP.dir = pathFlag.test(p, newP);
					newP.turn = 0;
					neighborhood.add(newP);
				}
			}
		}
		
		Point2 bestchoice = neighborhood.get(0);
		for(Point2 choice: neighborhood){
			if(choice.turn < bestchoice.turn)
				bestchoice = choice;
		}
		bestPath.push(bestchoice);
		return bestchoice;
    }
		
    //supporting methods 
    private static List<Point2> neighbor(Point p){
    	List<Point2> ans = new ArrayList<Point2>();
    	ans.add(new Point2(new Point(p.X, p.Y + 1)));
    	ans.add(new Point2(new Point(p.X, p.Y - 1)));
    	ans.add(new Point2(new Point(p.X - 1, p.Y)));
    	ans.add(new Point2(new Point(p.X + 1, p.Y)));
    	return ans;
    }

    //printing
    public static void printGraph(){
    	if(PRINT){
    		System.out.println("Search2.graph: ");
	    	for(int[] row: graph){
	    		for(int column: row)
	    			System.out.print(fill(String.valueOf(column), 4));
	    		System.out.print("\n");
	    	}
	    	System.out.println();
    		System.out.println("Search2.graph_goal: ");
	    	for(int[] row: graph_goal){
	    		for(int column: row)
	    			System.out.print(fill(String.valueOf(column), 4));
	    		System.out.print("\n");
	    	}
	    	System.out.println();
    	}
    }
    
    private static String fill(String s, int length){
    	length = length - s.length();
    	for(int i=0; i<length; i++)
    		s = s+" ";
    	return s;
    }
    
    public static void printBestPath(Point p){
        LinkedList<Point> path = Search2.bestPathFrom(p);
    	if(path != null){
	    	while(!path.isEmpty()){ 
	    		Point p_ = path.pop();
	    		System.out.printf("(%d, %d) \n",p_.X, p_.Y);
	    	}
    	}
    	else System.out.print("NOPATH!");
    }
    
    public static void printPathFrom(Point p){
        Iterable<Point> path = Search2.pathFrom(p);
    	if(path != null){
	    	for(Point p_: path){
	    		System.out.printf("(%d, %d) \n",p_.X, p_.Y);
	    	}
    	}
    	else System.out.print("NOPATH!");
    }
    
    
}
