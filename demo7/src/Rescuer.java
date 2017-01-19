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
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.HiTechnicIRSeekerV2;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;


public class Rescuer {
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

		private static int grabDist = 12;
		private static int clawAngle = 60;
		
	public static void main(String[] args){
		
		genGraph();
		ArrayList<Integer> receivedList = new ArrayList<Integer>();

		int[] received = {45, 45, 25, 100, 100, 100, 140, 100};
					//	   2,  2,  1,   4,   4,   4,   5,   4
		
		if(network){
			try{
				receivedList = connect();
			}catch(IOException e){
				System.exit(1);
			}

			for(int i=0; i< 8; i++)
				received[i] = receivedList.get(i);
		}
		
		
		List<Point> black = new ArrayList<Point>();
		List<Point> white = new ArrayList<Point>();
		for(int i=0; i<8; i+=2){
			Point p = Point.actualPoint(received[i], received[i+1]);
			LCD.drawString(p.toString(), 0, i/2);
			if(black(p)){
				graph[p.X][p.Y] = -2; 	//
				black.add(p);
			}
			else{
				graph[p.X][p.Y] = -1;
				white.add(p);
			}
		}
	
		Point[] order = new Point[4];
		if(compare(black.get(0), black.get(1)) <= 0){
			order[0] = black.get(0);
			order[3] = black.get(1);
		}
		else{
			order[0] = black.get(1);
			order[3] = black.get(0);
		}
		if(compare(white.get(0), white.get(1)) <= 0){
			order[1] = white.get(0);
			order[2] = white.get(1);
		}else{
			order[1] = white.get(1);
			order[2] = white.get(0);
		}
		
		Point[] destination = {
				new Point(5, 1), new Point(4, 1),
				new Point(5, 2), new Point(4, 2)
		};
		
		for(Point p: order){
			InitialGraph[p.X][p.Y] = -1;
		}
		
		pilot.addMoveListener((MoveListener) nav.getPoseProvider());
		pilot.reset();
		Waypoint start = translate(destination[0]);
		poseProvider.setPose(new Pose(start.x, start.y, 90));
		LCD.drawString("Start: (" + start.x + ", " + start.y + ")", 0, 6);
		
		pilot.setAngularSpeed(stdRotateSpeed);
		pilot.setLinearSpeed(stdSpeed);
		
		release();
	
		
		
	//	LCD.drawString("I'm ready", 0, 0);
		Button.waitForAnyPress();
		LCD.clear();
		
		boolean moved = false;
		for(Point p: order){
			if((p.X == 5 && p.Y == 0) || (p.X == 5 && p.Y == 1) || (p.X == 4 && p.Y == 0) || (p.X == 4 && p.Y == 1)){
				moved = true;
				move(p);
			}
		}
		
		if(moved){
			waveFront(new Point(5, 0));
			
			Pose pose0 = poseProvider.getPose();
			
			LinkedList<Point> pathToOrig = pathFrom(Point.actualPoint(pose0.getX(), pose0.getY()));
			
			LinkedList<Waypoint> pToO = new LinkedList<Waypoint>();
			
			for(Point po: pathToOrig)
				pToO.add(translate(po));
			
			pToO = simplify(pToO);
			
			for(Waypoint wayp: pToO){
				nav.goTo(wayp);
				Point check = Point.actualPoint(wayp.x, wayp.y);
				while(nav.isMoving()){if(closeTo(check.X, check.Y)) break;}
			}
		}
		for(int i=0; i<4; i++){
			LCD.drawString("Go (" + order[i].X + ", " + order[i].Y + ")", 0, 0);
			waveFront(order[i]);
			LinkedList<Point> pp = pathFrom(destination[i]);
			LinkedList<Waypoint> path = new LinkedList<Waypoint>();
			for(Point p:pp){
				path.add(translate(p));
			}
			path = simplify(path);	
			Iterator<Waypoint> it = path.descendingIterator();
			LinkedList<Waypoint> path1 = new LinkedList<Waypoint>();
			while(it.hasNext())
				path1.add(it.next());
			
			path1 =	reviseLast(path1);
			nav.clearPath();
			for(int j=0; j<path1.size(); j++){
				if(j!=0){
	//				nav.addWaypoint(path1.get(j));
					nav.goTo(path1.get(j));
					Point p = Point.actualPoint(path1.get(j).x, path1.get(j).y);
					while(nav.isMoving()){if(closeTo(p.X, p.Y)) break;}
				}
				System.out.println("path1: ");
				System.out.println(path1.get(j));
			}
			
	//		nav.followPath();
	//		while(!nav.pathCompleted()){
		//		LCD.clear(1);
		//		Pose p = poseProvider.getPose();
		//		LCD.drawString("(" + p.getX() + ", " + p.getY() + ")", 0, 1);
	//		}
			
			Delay.msDelay(1000);
			grab();
			
			LinkedList<Waypoint> path2 = new LinkedList<Waypoint>();
			it = path.iterator();
			while(it.hasNext())
				path2.add(it.next());
			path2 = reviseLast(path2);
			
		//	nav.clearPath();
			for(int j=1; j<path2.size(); j++){
				if(j!=0){
					nav.goTo(path2.get(j));
					Point p = Point.actualPoint(path2.get(j).x, path2.get(j).y);
					while(nav.isMoving()){if(closeTo(p.X, p.Y)) break;}
			//		nav.addWaypoint(path2.get(j));
				}
				System.out.println("path2: ");
				System.out.println(path2.get(j));
			}
			
		//	nav.followPath();
		//	while(!nav.pathCompleted()){if(closeTo(destination[i].X, destination[i].Y)) break;}
		
			release();
			
			pilot.travel(-1 * (cellWidth - grabDist));
			
			InitialGraph[order[i].X][order[i].Y] = 0;
			InitialGraph[destination[i].X][destination[i].Y] = -1;
			
			System.out.println("new InitialGraph");
			printGraph();
			
			
			Pose pose = poseProvider.getPose();
			if(i!=3){
				waveFront(destination[i+1]);
				System.out.println("new destination");
				List<Point> p1 = pathFrom(Point.actualPoint(pose.getX(), pose.getY()));
		//		nav.clearPath();
				for(int k=p1.size() - 2; k>=0; k--){
					nav.goTo(translate(p1.get(k)));
					while(nav.isMoving()){if(closeTo(p1.get(k).X, p1.get(k).Y)) break;}
		//			nav.addWaypoint(translate(p1.get(k)));
					System.out.println(translate(p1.get(k)));
				}
				if(p1.size() == 0){
					nav.goTo(translate(destination[i+1]));
					while(nav.isMoving()){if(closeTo(destination[i+1].X, destination[i+1].Y)) break;}
		//			nav.addWaypoint(translate(destination[i+1]));
				}
		//		nav.followPath();
		//		while(!nav.pathCompleted()){if(closeTo(p1.get(0).X, p1.get(0).Y)) break;}
				Sound.beep();
			}
			else{
				nav.goTo(165, 85);
				while(!nav.pathCompleted()){if(closeTo(6, 3)) break;}
				Delay.msDelay(2000);
				System.exit(0);
			}
			
		}
	}
	
	private static void move(Point p){
		Point next = new Point(p.X, p.Y + 2);
		if(!free(next))
			next = new Point(p.X + 2, p.Y);
		LinkedList<Waypoint> l = new LinkedList<Waypoint>();
		l.add(translate(p));
		l = reviseLast(l);
		nav.goTo(l.get(0));
		while(nav.isMoving()){if(closeTo(p.X, p.Y))break;}
		
		grab();
		
		InitialGraph[p.X][p.Y] = 0; 
		waveFront(next);
		
		LinkedList<Point> pp = pathFrom(p);
		
		LinkedList<Waypoint> ppw = new LinkedList<Waypoint>();
		
		for(Point p1: pp){
			ppw.add(translate(p1));
		}
		ppw = simplify(ppw);
		
		for(Waypoint wp: ppw){
			nav.goTo(wp);
			Point x = Point.actualPoint(wp.x, wp.y);
			while(nav.isMoving()){if(closeTo(x.X, x.Y))break;}
		}
		
		release();
		
		pilot.travel(-1 * (cellWidth - grabDist));
		
		InitialGraph[next.X][next.Y] = -1;
				
	}
	
	
	private static boolean closeTo(int x, int y){
		Waypoint w = translate(new Point(x, y));
		Pose p = poseProvider.getPose();
		int sector = f1(p.getHeading());
		if(sector == 0 || sector == 7){
			return Math.abs((w.x - grabDist - p.getX())) + Math.abs(w.y - p.getY()) < 1.0;
		}
		if(sector == 1 || sector == 2){
			return Math.abs((w.x - p.getX())) + Math.abs(w.y - grabDist - p.getY()) < 1.0;
		}
		if(sector == 3 || sector == 4){
			return Math.abs((w.x + grabDist - p.getX())) + Math.abs(w.y - p.getY()) < 1.0;
		}
		if(sector == 5 || sector == 6){
			return Math.abs((w.x - p.getX())) + Math.abs(w.y + grabDist - p.getY()) < 1.0;
		}
		System.out.println("Should never get here");
		return false;
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

	public static ArrayList<Integer> connect() throws IOException{
		ArrayList<Integer> ans = new ArrayList<Integer>();
		Socket s = null; ObjectInputStream o = null;
		try {
	        s = new Socket(IPAddress, port);    //create socket with laptop ip and port number
	        o = new ObjectInputStream(s.getInputStream());
	        ans =  (ArrayList<Integer>) o.readObject();
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
}
