import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.HiTechnicIRSeekerV2;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;


public class grabRelease {
	/*******************
	 *  debug
	 */	
		//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
		static boolean Debug = true;
		static boolean PRINT = true;
		static boolean network = false;
		
		//PORT AND IP  -----------------------------------------------------------------------------------------------------------
		static final String IPAddress = "136.167.109.36";
		static final int port = 6983;
			
	/*******************
	 * Robot setups
	 */	
		//Search Graph -----------------------------------------------------------------------------------------------------------
		static int graphX = 5;
		static int graphY = 5;
		static int[][] graph;
		static int[][] InitialGraph;
		static int sX = 2, sY = 1 ;
		static LinkedList<Waypoint> path1 = new LinkedList<Waypoint>();
		static int cellsX = 8;
		static int cellsY = 8;
		static double cellWidth = 30.0;
		
		
		//Robot Statistics -------------------------------------------------------------------------------------------------------
		
		//turning
		static final double turnConst = 0.83;  // 0.86 0.95
			
		static final double robotLength = 15.0;
		static final double wheelDiameter = 5.4;
		static final double wheelCircumference = wheelDiameter * Math.PI;
		static final double robotTrack = 14.5 * turnConst;  //14.5	

			
		//Robot Controller -------------------------------------------------------------------------------------------------------

		//Pilot & PoseProvider & Navigator ---------------------------------------------------------------------------------------
		@SuppressWarnings("deprecation")
		static DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, robotTrack, Motor.A, Motor.B);
		static Navigator nav = new Navigator(pilot);
		static PoseProvider poseProvider = nav.getPoseProvider();
		static int stdSpeed = 15;
		static int stdRotateSpeed = 45;
		static LinkedList<Point> possibility = new LinkedList<Point>();
		static NXTRegulatedMotor lclaw = Motor.C;
		static NXTRegulatedMotor rclaw = Motor.D;
		static int grabSpeed = 180;
		
		static int warehouseX = 2;
		static int warehouseY = 2;

		private static int grabDist = 10;
		private static int clawAngle = 45;
		
	public static void main(String[] args){
			
		release();
		
		boolean flag = true;
		while(!Button.ESCAPE.isDown()){
			Button.waitForAnyPress();
			if(flag){
				flag = false;
				grab();
			}
			else{
				flag = true;
				release();
			}
		}
		
	}
	
	private static void grab(){
		rclaw.setSpeed(grabSpeed);
		lclaw.setSpeed(grabSpeed);
		lclaw.rotate(clawAngle, true);
		rclaw.rotate(clawAngle);
	}
	private static void release(){
		rclaw.setSpeed(grabSpeed);
		lclaw.setSpeed(grabSpeed);
		lclaw.rotate(-1 * clawAngle, true);
		rclaw.rotate(-1 * clawAngle);
	}
	
	private static LinkedList<Waypoint> reviseLast(LinkedList<Waypoint> path){
		int s = path.size() - 1;
		Waypoint last = path.get(s);
		Waypoint prev = path.get(s-1);
		
		if(last.x - prev.x == 0)
			if(last.y - prev.y > 0)
				path.set(s, new Waypoint(last.x, last.y - grabDist));
			else
				path.set(s, new Waypoint(last.x, last.y + grabDist));
		else if(last.x - prev.x > 0)
			path.set(s, new Waypoint(last.x - grabDist, last.y));
		else
			path.set(s, new Waypoint(last.x + grabDist, last.y));
		
		return path;
	}
	
	public static int f1(double d){
		int ans = ((int) (d - 90 / 45.0)) % 8;
		if(ans < 0)
			return ans + 7;
		return ans;
	}
	
	private static LinkedList<Waypoint> simplify(LinkedList<Waypoint> path){
		LinkedList<Waypoint> ans = new LinkedList<Waypoint>();
		ans.add(path.get(0));
		ans.add(path.get(1));
		boolean ydir = (path.get(1).x - path.get(0).x == 0);
		for(int i = 2; i<path.size(); i++){
			Waypoint curr = path.get(i);
			Waypoint prev = path.get(i-1);
			if(ydir){
				if(curr.x - prev.x == 0){
					ans.pollLast();	
				}
			}else{
				if(curr.x - prev.x != 0){
					ans.pollLast();
				}
			}
			ans.add(curr);
		}
		return ans;
	}
	
	private static Waypoint translate(Point p){
		double wx = (p.X - 0.5) * cellWidth;
		double wy = (p.Y - 0.5) * cellWidth;
		return new Waypoint(wx, wy);
	}
	
	private static int compare(Point p1, Point p2) {
		double d1 = Math.sqrt(p1.X - 5) + Math.sqrt(p1.Y - 1);
		double d2 = Math.sqrt(p2.X - 5) + Math.sqrt(p2.Y - 1);
		return (int) (d1 - d2); 
	}
	
	static int gX, gY;
	 //Point predicate---------------------------------------------------------------------------------------------------------
    static boolean notObs(Point p){return graph[p.X][p.Y] != -1 || graph[p.X][p.Y] != -2;}
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
 
	public static void waveFront(Point goal){
		gX = goal.X;
		gY = goal.Y;
		graph = copy(InitialGraph);
		
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
	private static List<Point> neighbor(Point p){
		List<Point> ans = new ArrayList<Point>();
		ans.add(new Point(p.X, p.Y + 1));
		ans.add(new Point(p.X, p.Y - 1));
		ans.add(new Point(p.X - 1, p.Y));
		ans.add(new Point(p.X + 1, p.Y));
		return ans;
	}
	
	private static boolean black(Point p){
		return (p.X + p.Y) % 2 == 0;
	}
	
	//black -2, white -1;
	private static void genGraph(){
		InitialGraph = new int[graphX + 2][graphY + 2];
		for(int i=0; i<graphX; i++){
			InitialGraph[i][0] = -1;
			InitialGraph[i][graphY + 1] = -1;
		}
		for(int j=0; j<graphY; j++){
			InitialGraph[0][j] = -1;
			InitialGraph[graphX + 1][j] = -1;
		}
	}

	public static int[] connect() throws IOException{
		int[] ans = {};
		Socket s = null; ObjectInputStream o = null;
		try {
	        s = new Socket(IPAddress, port);    //create socket with laptop ip and port number
	        o = new ObjectInputStream(s.getInputStream());
	        ans =  (int[]) o.readObject();
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	        o.close();
	        s.close();
	        System.out.println("\n 111111 \n");
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	        o.close();
	        s.close();
	        System.out.println("\n 222222 \n");
	    } catch (IOException e) { 
	        o.close();
	        s.close();
	        e.printStackTrace();
	        System.out.println("\n 333333 \n");
	    } 
	    o.close();
	    s.close();
	    return ans;
	}
		
}
