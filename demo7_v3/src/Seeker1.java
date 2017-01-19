import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.HiTechnicIRSeekerV2;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;


public class Seeker1 {
	/*******************
	 *  debug
	 */	
		//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
		static boolean Debug = true;
		static boolean PRINT = true;
		static boolean network = false;
		
		//PORT AND IP  -----------------------------------------------------------------------------------------------------------
		static final String IPAddress = "10.200.11.166";
		static final int port = 6983;
			
	/*******************
	 * Robot setups
	 */	
		//Search Graph -----------------------------------------------------------------------------------------------------------
		static int graphX = 5;
		static int graphY = 5;
		static int[][] graph;
		static int[][] InitialGraph;
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
		
		//Sensor -----------------------------------------------------------------------------------------------------------------
		static NXTUltrasonicSensor leftSonic = new NXTUltrasonicSensor(SensorPort.S1);
		static NXTUltrasonicSensor frontSonic = new NXTUltrasonicSensor(SensorPort.S2);
		
		static globalUltrasonicData frontData = new globalUltrasonicData();
		static globalUltrasonicData leftData = new globalUltrasonicData();		
		
		static int wall = -1;
		static int visit = -2;
		static int open = 0;
		static int cup = -3;
		
		public static boolean free(int x, int y){
			if(ignoreVisit)
				return graph[x][y] != wall && graph[x][y] != cup;
			else
				return graph[x][y] == open;
		}
		
		public static boolean visited(int x, int y){
			return graph[x][y] == visit;
		}
		
		
		private static boolean ignoreVisit = false;
//		private static int search = 100;
//		private static int ignoreVisitSearch = 101;
//		private static int getOut = 102;
//		private static int state = search;
		
		
		static ArrayList<Point> count = new ArrayList<Point>();
	//	static HashMap<Point, Integer> count = new HashMap<Point, Integer>();
		static synchronized void addCount(Point p){
			if(!Seeker1.count.contains(p))
				Seeker1.count.add(p);
		}
		
		static synchronized int getCountSize(){
			return count.size();
		}
		
		private static int left = 0;
		private static int front = 1;
		
	public static void main(String[] args){
		Thread frontU = new Thread(new sonicThread(frontData, frontSonic, front));
		frontU.setDaemon(true);
		frontU.start();
		
		Thread leftU = new Thread(new sonicThread(leftData, leftSonic, left));
		leftU.setDaemon(true);
		leftU.start();
		
		genGraph();
		
		
	
		
		pilot.addMoveListener((MoveListener) nav.getPoseProvider());
		pilot.reset();
		poseProvider.setPose(new Pose(15, 15, 90));
		
		pilot.setAngularSpeed(stdRotateSpeed);
		pilot.setLinearSpeed(stdSpeed);
	
		
		curr = new Point(1, 1);
		prev = new Point(1, 0);
		LCD.drawString("I'm ready", 0, 0);
		Button.waitForAnyPress();
		LCD.clear();
		
	
		
		while(getCountSize() != 4){
				Pose p = poseProvider.getPose();
				curr = Point.actualPoint(p.getX(), p.getY());
			
				if(!takeLeft()){
					if(!takeFront())
						takeRight();
				}
				graph[curr.X][curr.Y] = visit;
				//printGraph();
				System.out.println("Count: " + getCountSize());
		}
		Sound.beep();
		
			waveFront(new Point(1, 1));
			Pose p = poseProvider.getPose();
			Iterator<Point> it = pathFrom(Point.actualPoint(p.getX(), p.getY())).descendingIterator();
			LinkedList<Waypoint> path = new LinkedList<Waypoint>();
			while(it.hasNext())
				path.add(translate(it.next()));
			
			path = simplify(path);
			for(Waypoint wp: path){
				nav.goTo(wp);
				Point p1 = Point.actualPoint(wp.x, wp.y);
				while(nav.isMoving()){if(closeTo(p1.X, p1.Y)) break;};
			}
			pilot.travel(30);
			Delay.msDelay(2000);
	
		if(network){
			ArrayList<Integer> count1 = new ArrayList<Integer>();
			try{
				for(Point point11: count){
					count1.add(point11.X); count1.add(point11.Y);
				}
				sendPoints(count1);
			}catch(Exception e){
				Sound.beep();
			}		
		}
			
		System.exit(0);
	}
	
	private static Point curr;
	private static Point prev;
	
	
	private static boolean freeFront(){return frontData.getSonicValue() > 30;}

	public static void takeRight(){
		if(curr.X - prev.X > 0){
			prev = curr;
			{
				pilot.rotate(-90);
			}
			if(free(curr.X, curr.Y - 1) && freeFront()){
				System.out.println("TOOK RIGHT");
			}
			else{
				pilot.rotate(-90);
				ignoreVisit = true;
				System.out.println("TURNED AROUND");
			}
			pilot.travel(30);
			Pose p = poseProvider.getPose();
			curr = Point.actualPoint(p.getX(), p.getY());
			
			LCD.clear();
			LCD.drawString(curr.toString(), 0, 5);
			return;
		}
		if(curr.X - prev.X < 0){
			prev = curr;
			{
				pilot.rotate(-90);
			}
			if(free(curr.X, curr.Y + 1) && freeFront()){
				System.out.println("TOOK RIGHT");
			}
			else{
				pilot.rotate(-90);
				ignoreVisit = true;
				System.out.println("TURNED AROUND");
			}
			pilot.travel(30);
			Pose p = poseProvider.getPose();
			curr = Point.actualPoint(p.getX(), p.getY());
			
			LCD.clear();
			LCD.drawString(curr.toString(), 0, 5);
			return;
		}
		if(curr.Y - prev.Y > 0){
			prev = curr;
			pilot.rotate(-90);
			if(free(curr.X + 1, curr.Y) && freeFront()){
				System.out.println("TOOK RIGHT");
			}
			else{
				pilot.rotate(-90);
				ignoreVisit = true;
				System.out.println("TURNED AROUND");
			}
			pilot.travel(30);
			Pose p = poseProvider.getPose();
			curr = Point.actualPoint(p.getX(), p.getY());
			
			LCD.clear();
			LCD.drawString(curr.toString(), 0, 5);
			return;
		}
		if(curr.Y - prev.Y < 0){
			prev = curr;
			pilot.rotate(-90);
			if(free(curr.X - 1, curr.Y) && freeFront()){
				System.out.println("TOOK RIGHT");
			}
			else{
				pilot.rotate(-90);
				ignoreVisit = true;
				System.out.println("TURNED AROUND");
			}
			pilot.travel(30);
			Pose p = poseProvider.getPose();
			curr = Point.actualPoint(p.getX(), p.getY());

			LCD.clear();
			LCD.drawString(curr.toString(), 0, 5);
			return;
		}
	}
	
	private static boolean freeSide(){
		return leftData.getSonicValue() > 30;
	}
	
	public static boolean takeLeft(){
		if(curr.X - prev.X > 0){
			if(free(curr.X, curr.Y + 1)  && freeSide()){
				if(!visited(curr.X, curr.Y + 1))
					ignoreVisit = false;
				prev = curr;
				pilot.rotate(90);
				pilot.travel(30);
				LCD.clear();
				LCD.drawString(curr.toString(), 0, 5);
				return true;
			}				
		}
		if(curr.X - prev.X < 0){
			if(free(curr.X, curr.Y - 1)  && freeSide()){
				if(!visited(curr.X, curr.Y - 1))
					ignoreVisit = false;
				prev = curr;
				pilot.rotate(90);
				pilot.travel(30);
				Pose p = poseProvider.getPose();
				curr = Point.actualPoint(p.getX(), p.getY());
				
				LCD.clear();
				LCD.drawString(curr.toString(), 0, 5);
				return true;
			}
		}
		if(curr.Y - prev.Y > 0){
			if( free(curr.X - 1, curr.Y)  && freeSide()){
				if(!visited(curr.X - 1, curr.Y))
					ignoreVisit = false;
				prev = curr;
				pilot.rotate(90);
				pilot.travel(30);
				Pose p = poseProvider.getPose();
				curr = Point.actualPoint(p.getX(), p.getY());
				
				LCD.clear();
				LCD.drawString(curr.toString(), 0, 5);
				return true;
			}
		}
		if(curr.Y - prev.Y < 0){
			if(free(curr.X + 1, curr.Y)  && freeSide()){
				if(!visited(curr.X + 1, curr.Y))
					ignoreVisit = false;
				prev = curr;
				pilot.rotate(90);
				pilot.travel(30);
				Pose p = poseProvider.getPose();
				curr = Point.actualPoint(p.getX(), p.getY());
				
				LCD.clear();
				LCD.drawString(curr.toString(), 0, 5);
				return true;
			}
		}
		System.out.println("LEFT NOT TAKEN");
		return false;
	}
	
	public static boolean takeFront(){
		if(curr.X - prev.X > 0){
			if(free(curr.X + 1, curr.Y) && freeFront()){
				if(!visited(curr.X + 1, curr.Y))
					ignoreVisit = false;
				prev = curr;
				pilot.travel(30);
				Pose p = poseProvider.getPose();
				curr = Point.actualPoint(p.getX(), p.getY());
				
				LCD.clear();
				LCD.drawString(curr.toString(), 0, 5);
				return true;
			}
		}
		if(curr.X - prev.X < 0){
			if(free(curr.X - 1, curr.Y)  && freeFront()){
				if(!visited(curr.X - 1, curr.Y))
					ignoreVisit = false;
				prev = curr;
				pilot.travel(30);
				Pose p = poseProvider.getPose();
				curr = Point.actualPoint(p.getX(), p.getY());
				
				LCD.clear();
				LCD.drawString(curr.toString(), 0, 5);
				return true;
			}
		}
		if(curr.Y - prev.Y > 0){
			if( free(curr.X, curr.Y + 1)  && freeFront()){
				if(!visited(curr.X, curr.Y + 1))
					ignoreVisit = false;
				prev = curr;
				pilot.travel(30);
				Pose p = poseProvider.getPose();
				curr = Point.actualPoint(p.getX(), p.getY());
				
				LCD.clear();
				LCD.drawString(curr.toString(), 0, 5);
				return true;
			}
		}
		if(curr.Y - prev.Y < 0){
			if(free(curr.X, curr.Y - 1)  && freeFront()){
				if(!visited(curr.X, curr.Y - 1))
					ignoreVisit = false;
				prev = curr;
				pilot.travel(30);
				Pose p = poseProvider.getPose();
				curr = Point.actualPoint(p.getX(), p.getY());
				
				LCD.clear();
				LCD.drawString(curr.toString(), 0, 5);
				return true;
			}
		}
		System.out.println("FRONT NOT TAKEN");
		return false;
	}
	
	private static boolean closeTo(int x, int y){
		Waypoint w = translate(new Point(x, y));
		Pose p = poseProvider.getPose();
		int sector = f1(p.getHeading());
		if(sector == 0 || sector == 7){
			return Math.abs((w.x - p.getX())) + Math.abs(w.y - p.getY()) < 1.0;
		}
		if(sector == 1 || sector == 2){
			return Math.abs((w.x - p.getX())) + Math.abs(w.y- p.getY()) < 1.0;
		}
		if(sector == 3 || sector == 4){
			return Math.abs((w.x - p.getX())) + Math.abs(w.y - p.getY()) < 1.0;
		}
		if(sector == 5 || sector == 6){
			return Math.abs((w.x - p.getX())) + Math.abs(w.y- p.getY()) < 1.0;
		}
		System.out.println("Should never get here");
		return false;
	}
	
	public static int f1(double d){
		int ans = ((int) (d - 90 / 45.0)) % 8;
		if(ans < 0)
			return ans + 7;
		return ans;
	}
	
	private static LinkedList<Waypoint> simplify(LinkedList<Waypoint> path){
		if(path.size() <= 1)
			return path;
		LinkedList<Waypoint> ans = new LinkedList<Waypoint>();
		ans.add(path.get(0));
		ans.add(path.get(1));
		boolean ydir = (Math.abs(path.get(1).x - path.get(0).x) < 5.0);
		for(int i = 2; i<path.size(); i++){
			Waypoint curr = path.get(i);
			Waypoint prev = path.get(i-1);
			if(ydir){
				if(Math.abs(curr.x - prev.x) < 5.0){
					ans.pollLast();	
				}
			}else{
				if(Math.abs(curr.x - prev.x) > 5.0){
					ans.pollLast();
				}
			}
			ydir = (Math.abs(curr.x - prev.x) < 5.0);
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
		for(int i=0; i<=graphX + 1; i++){
			InitialGraph[i][0] = -1;
			InitialGraph[i][graphY + 1] = -1;
		}
		for(int j=0; j<=graphY + 1; j++){
			InitialGraph[0][j] = -1;
			InitialGraph[graphX + 1][j] = -1;
		}
		graph = copy(InitialGraph);
	}
	
	public static void printGraph(){
		for(int i=0; i<7; i++){
			for(int j=0; j<7; j++)
				System.out.print(fill(String.valueOf(InitialGraph[i][j]), 4));
				
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
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/***************/
	/* Send Points */
	/***************/
	static ObjectOutputStream outStream = null; 
	static Socket t = null;
	static ServerSocket s;
	private static void connect() throws IOException{        
		s = new ServerSocket(port); // create a TCP socket, this is overkill
		System.out.println("Server started");           
		t = s.accept();                                               // wait for client (robot) to connect 
		System.out.println("server connected");
		outStream = new ObjectOutputStream(t.getOutputStream());  //wrap the output stream with an object output stream
	}
	
	private static void sendPoints(ArrayList<Integer> coord) throws IOException {
		outStream.writeObject(coord); 
		//      outStream.writeObject(obstacles);//so we can write the entire serializable  object
		outStream.flush();                                                                     //flush it all out
		outStream.close();
		t.close();
		s.close();
	}
}
