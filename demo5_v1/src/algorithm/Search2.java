package algorithm;

//Search.java
//change to lowercase search
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Search2{
    static int sX;    
    static int sY;
	final Point source;	//source
    static int[][] graph;
    static int[][] InitialGraph; //never do search on this graph, it is for reset graph.
    static Predicate<Point> notObs = (p -> graph[p.X][p.Y] != -1);
	static Predicate<Point> free = (p -> graph[p.X][p.Y] == 0);

	static Predicate<Point> hasPathTo = (free.and(notObs)).negate();
	static Predicate<Point> notBack = (p -> p.X!=sX || p.Y!=sY);
	static BiPredicate<Point, Point> oneLess = ((p1, p2) -> graph[p1.X][p1.Y] - graph[p2.X][p2.Y] == -1);
	
	//
	static BiPredicate<Point, Point> oneMore = ((p1, p2) -> graph[p1.X][p1.Y] - graph[p2.X][p2.Y] == 1);
	static BiPredicate<Point, Point> pathFlag = ((p1, p2) -> p1.Y - p2.Y != 0);
	
    public Search2(int[][] _graph, int _sX, int _sY){
    	if(_graph != null){
    		graph = _graph;
    		InitialGraph = graph;
    	}
        sX = _sX;
        sY = _sY;
		source = new Point(sX, sY);
		if(free.test(new Point(sX, sY)))
			waveFront();
		else
			System.out.println("Cannot start at obstacle");
    }
    
    private static void waveFront(){
        Queue<Point> queue = new LinkedList<Point>();
        graph[sX][sY] = 1;
        
        queue.add(new Point(sX, sY));
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
     * IMPORTANT //mark a spot on the initial graph and redo the search.
     * @param x
     * @param y
     */
    public void mark(int x, int y){
    	InitialGraph[x+1][y+1] = -1;
    	graph = InitialGraph; 
    	waveFront();
    	printGraph();
    }	
    
    /**
     * STATIC
     * IMPORTANT //mark a spot on the initial graph and redo the search.
     * @param x
     * @param y
     */
    public static void setGraph(int[][] _graph){
    	InitialGraph = _graph;
    	for(int i=0; i<InitialGraph.length; i++)
    		for(int j=0; j<InitialGraph[0].length; j++)
    			if(InitialGraph[i][j] != -1)
    				InitialGraph[i][j] = 0;
    	graph = InitialGraph;
//    	waveFront();
    	printGraph();
    }	
    
    public Iterable<Point> pathTo(int x, int y){
    	Point p = new Point(x, y);
        if(!hasPathTo.test(p)) return null;
        Stack<Point> path = new Stack<Point>();
        Point curr = p;
		path.push(p);
        while(notBack.test(curr)){
			List<Point> neighborhood = neighbor(curr);
			//Find one path
			findPath:
			for(Point p1: neighborhood){
				if(oneLess.test(p1, curr)){
					curr = p1;
					path.push(curr);
					//find one viable path the break;
					break findPath;
				}
			}
        }
        return path;
    }

    //best path finding method
    private Queue<Point> bestPath = new LinkedList<Point>();
    public Queue<Point> bestPathTo(int x, int y){
    	Point2 curr = new Point2(x, y, 0, true);
    	bestPath = new LinkedList<Point>();
    	while(notBack.test(curr)){
    		curr = bestPathStep(curr);
    	}
    	return bestPath;
    }
    private Point2 bestPathStep(Point2 p){
        if(!hasPathTo.test(p)) return null;

        List<Point2> neighborhood = new ArrayList<Point2>();
		for(Point p1: neighbor(p)){
			if(!notBack.test(p1)) {
				bestPath.add(point2(p1));
				return point2(p1);
			}
			if(oneLess.test(p1, p)){
				if(pathFlag.test(p, p1) != p.dir){
					Point2 newP = point2(p1);
					newP.dir = !p.dir;
					newP.turn = p.turn + 1;
					neighborhood.add(newP);
				}
				else{
					Point2 newP = point2(p1);
					newP.dir = p.dir;
					newP.turn = p.turn;
					neighborhood.add(newP);
				}
			}
		}
		
		Point2 bestchoice = neighborhood.get(0);
		for(Point2 choice: neighborhood){
			if(choice.turn < bestchoice.turn)
				bestchoice = choice;
		}
		bestPath.add(bestchoice);
		return bestchoice;
    }
		
    //supporting methods 
    private static List<Point> neighbor(Point p){
    	List<Point> ans = new ArrayList<Point>();
    	ans.add(new Point(p.X, p.Y + 1));
    	ans.add(new Point(p.X, p.Y - 1));
    	ans.add(new Point(p.X - 1, p.Y));
    	ans.add(new Point(p.X + 1, p.Y));
    	return ans;
    }
    
    public Point2 point2(Point p){
    	return new Point2(p.X, p.Y, -1, true);
    }

    public Point point(Point2 p2){
    	return new Point(p2.X, p2.Y);
    }

    //printing
    public static void printGraph(){
    	for(int[] row: graph){
    		for(int column: row)
    			System.out.print(fill(String.valueOf(column), 4));
    		System.out.print("\n");
    	}	
    }
    
    private static String fill(String s, int length){
    	length = length - s.length();
    	for(int i=0; i<length; i++)
    		s = s+" ";
    	return s;
    }
    
    
}
