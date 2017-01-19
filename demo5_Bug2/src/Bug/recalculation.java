package Bug;

import algorithm.Point;
import algorithm.Point2;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

import java.util.*;

public class recalculation implements Behavior{
	private boolean suppressed = false;
	private int count = 10;
	private boolean recalc = false;
	
	public boolean takeControl() {
		if(!recalc){
			Pose p = waveFrontBug2.poseProvider.getPose();
			Point fake = Point.actualPoint(waveFrontBug2.mapLength - p.getY(), p.getX());
			Waypoint c = waveFrontBug2.nav.getWaypoint();
			Point curr = Point.actualPoint(waveFrontBug2.mapLength - c.getY(), c.getX());
			if(count == 0){
				System.out.printf("fake: (%d, %d)\n", fake.X, fake.Y);
				System.out.printf("curr: (%d, %d)\n", curr.X, curr.Y);
				count = 25;
			}
			else count--;
			if(fake.X == curr.X && fake.Y == curr.Y) return false;
		}
		if(waveFrontBug2.frontSonicData.getSonicValue()<20){
			recalc = true;
			return true;
		}
		return recalc;
	}

	public void action() {
		waveFrontBug2.nav.stop();
		waveFrontBug2.toCenter();
		
		while(waveFrontBug2.pilot.isMoving())
			Delay.msDelay(50);
		
		waveFrontBug2.nav.clearPath();
		
		Pose p = waveFrontBug2.nav.getPoseProvider().getPose();
		Point source = Point.actualPoint(waveFrontBug2.mapLength - p.getY(), p.getX());
		
		waveFrontBug2.mark(front());
		
		waveFront();
		LinkedList<Point> pathR = pathFrom(source);
  		waveFrontBug2.addWaypointss(pathR);
  		
		waveFrontBug2.set();
		waveFrontBug2.nav.followPath();
		Delay.msDelay(1000);
		recalc = false;
	}

	public void suppress() {
		suppressed = true;
	}	
	
	public Point front(){
		Pose p = waveFrontBug2.poseProvider.getPose();
		Point point = Point.actualPoint(waveFrontBug2.mapLength - p.getY(), p.getX());
		Point front;
		switch(waveFrontBug2.f1(p.getHeading())){
			case 0: front = new Point(point.X, point.Y + 1);
					break;
			case 1: front = new Point(point.X - 1, point.Y);
					break;
			case 2: front = new Point(point.X - 1, point.Y);
					break;
			case 3: front = new Point(point.X, point.Y - 1);
					break;
			case 4: front = new Point(point.X, point.Y - 1);
					break;
			case 5: front = new Point(point.X + 1, point.Y);
					break;
			case 6: front = new Point(point.X + 1, point.Y);
					break;
			case 7: front = new Point(point.X, point.Y + 1);
					break;
			default: front = null;
					break;
		}
		
		return front;
	}
	
	//Search
	
	//Search from goal, i.e. path to goal
		static int gX = 0;
		static int gY = -100;
		public static Point goal;		//goal
		
		private static int[][] graph;
	    
	  //Point predicate---------------------------------------------------------------------------------------------------------
	    static boolean notObs(Point p){return graph[p.X][p.Y] != -1;}
		static boolean free(Point p){return graph[p.X][p.Y] == 0;}
		//Path predicate----------------------------------------------------------------------------------------------------------
		static boolean hasPathTo(Point p){ return !free(p) && notObs(p);}
		static boolean notBack(Point p){return p.X!=gX || p.Y!=gY;}
		static boolean oneLess(Point p1, Point p2){return graph[p1.X][p1.Y] - graph[p2.X][p2.Y] == -1;}
		
		//BestPath predicate------------------------------------------------------------------------------------------------------
		static boolean oneMore(Point p1, Point p2){return graph[p1.X][p1.Y] - graph[p2.X][p2.Y] == 1;}
		static boolean pathFlag(Point p1, Point p2){return p1.Y - p2.Y != 0;}
		static boolean arriveAt(Point p1, Point p2){return p1.X==p2.X && p1.Y==p2.Y;}
		
	 private static int[][] copy(int[][] _graph){
	    	int[][] newGraph = new int[_graph.length][];
	    	for(int i=0; i<_graph.length; i++)
	    		newGraph[i] = new int[_graph[0].length];
	    	for(int i=0; i<_graph.length; i++)
	    		for(int j=0; j<_graph.length; j++)
	    			newGraph[i][j] = _graph[i][j];
	    	return newGraph;
	    }
	 
	public void waveFront(){
		if(gY == -100){
			gX = waveFrontBug2.gX;
			gY = waveFrontBug2.gY;
			goal = new Point(gX, gY);
		}
		graph = copy(waveFrontBug2.InitialGraph);
		
		if(!free(goal)){
			System.out.println("Cannot set goal at obstacle; graph not searched");
			return;
    	}
        Queue<Point> queue = new LinkedList<Point>();
        graph[gX][gY] = 1;
        

        queue.add(goal);
        while(!queue.isEmpty()){
            Point curr = queue.poll();
            int val = graph[curr.X][curr.Y] + 1;
            for(Point pn: neighbor(curr)){
            	if(free(pn)){
            		graph[pn.X][pn.Y] = val;
            		queue.add(pn);
            	}
            }
        }
	}
	
	 /**
     * Random Path, on graph_goal, i.e. to goal
     */
    public static LinkedList<Point> pathFrom(Point p){
        if(!hasPathTo(p)) return null;
        LinkedList<Point> path = new LinkedList<Point>();
        Point curr = p;
		path.push(p);
        while(notBack(curr)){
			findPath:
			for(Point p1: neighbor(curr)){
				if(oneLess(p1, curr)){
					curr = p1;
					path.push(curr);
					break findPath;
				}
			}
        }
        return path;
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
	
}
