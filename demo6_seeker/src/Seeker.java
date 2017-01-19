import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.Function;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.HiTechnicIRSeeker;
import lejos.hardware.sensor.HiTechnicIRSeekerV2;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;
import ProfTests.DifferentialPilot;
import Threads.*;

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
		
/*******************
 * Robot setups
 */	
	//Search Graph -----------------------------------------------------------------------------------------------------------
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
	
	//Sensor------------------------------------------------------------------------------------------------------------------
	static HiTechnicIRSeekerV2 ir = new HiTechnicIRSeekerV2(SensorPort.S3);

	//Data--------------------------------------------------------------------------------------------------------------------
	static globalIRData irData = new globalIRData();
		
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args){
		Thread t = new Thread(new IRThread(irData, ir));
		t.setDaemon(true);
		t.start();
		
//		genInitGraph();
//		resetGraph();
		//traverse graph
		
		pilot.reset();
		pilot.addMoveListener((MoveListener) nav.getPoseProvider()); 
		
		float sXf = (float) ((sX - 0.5) * cellWidth);
		float sYf = (float) ((sY - 0.5) * cellWidth);
		poseProvider.setPose(new Pose(sXf, sYf, 90));
		
		
		try{connect();}catch(IOException e){LCD.clear(); LCD.drawString("CONNECTION ERROR", 0, 0);}
		
		Point[] points = {new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3), new Point(2, 4), new Point(2, 5), new Point(2, 6), 
				new Point(3, 6), new Point(4, 6), 
				new Point(4, 5), new Point(4, 4), new Point(4, 3), new Point(4, 2), new Point(4, 1), 
				new Point(5, 1), new Point(6, 1), 
				new Point(6, 2), new Point(6, 3), new Point(6, 4), new Point(6, 5), new Point(6, 6), new Point(6, 7)};

		LCD.drawString("I'm ready", 0, 1);
		Button.waitForAnyPress();
		LCD.clear();
		pilot.setAngularSpeed(stdRotateSpeed);
		pilot.setLinearSpeed(stdSpeed);
		
		int[] g = new int[4];
		
		int i = 0;
		boolean found = false;
		Point exact = new Point(0, 0);
	//	boolean flag = false;
	//	boolean prevflag = false;
		label1:
		for(i=2; i<points.length; i++){
			nav.goTo(translate(points[i]));
//			if(prevflag){
//				Sound.systemSound(true, 2);
//			}
			while(nav.isMoving()){}
		//	Sound.systemSound(true, 2);
			int l = 10;
			int[] ir = new int[l];
			for(int x=0; x<l; x++){
				ir[x] = (int) irData.getIRValue();
				Delay.msDelay(20);
			}
			if(!found){
				LCD.clear();
				LCD.drawString("Search", 0, 1);
				int count = 0;
				int count1 = 0;
				for(int ir1 : ir){
					/*
					if(ir1 == 60 || ir1 == -60 || ir1 == 30 || ir1 == -30){
						count1++;
					}
					if(count1>3){
						prevflag = flag;
						flag = true;
					}
					else{
						prevflag = flag;
						flag = false;
					}
						
					*/
					if(ir1 == 90 || ir1 == -90 || ir1 == 120 || ir1 == -120){
						count++;
					}
					if(count > 5){//&& prevflag){
						Pose ppp = poseProvider.getPose();
						g[2] = (int) ppp.getX(); g[3] = (int) ppp.getY();
						Sound.beep();
				
						Pose p; double heading; Point point;
						p = poseProvider.getPose();
						heading = p.getHeading();
						point = Point.actualPoint(p.getX(), p.getY());
						
						int sector = f1(heading);
						boolean rhs = (ir1 == -90 || ir1 == -120);
						if(sector == 0 || sector == 7){
							if(rhs) exact = new Point(point.X + 1, point.Y);
							else 	exact = new Point(point.X - 1, point.Y);
						}
						if(sector == 1 || sector == 2){
							if(rhs) exact = new Point(point.X, point.Y + 1);
							else 	exact = new Point(point.X, point.Y - 1);
						}
						if(sector == 3 || sector == 4){
							if(rhs) exact = new Point(point.X - 1, point.Y);
							else 	exact = new Point(point.X + 1, point.Y);
						}
						if(sector == 5 || sector == 6){
							if(rhs) exact = new Point(point.X, point.Y - 1);
							else 	exact = new Point(point.X, point.Y + 1);
						}
						
						found = true;
						LCD.clear(); LCD.drawString("EXIT!", 0, 0);
	
						g[0] = (int) ((exact.X - 0.5) * cellWidth);
						g[1] = (int) ((exact.Y - 0.5) * cellWidth);
						LCD.drawString("( " + String.valueOf(g[0]) + ", " + String.valueOf(g[1]) + " )", 0, 1);
						LCD.drawString("( " + String.valueOf(g[2]) + ", " + String.valueOf(g[3]) + " )", 0, 2);
						LCD.drawString(exact.toString(), 0, 3);
						break label1;
					}
				}
			}
		}
		
		Pose ppp = poseProvider.getPose();
		LCD.clear(6);
		LCD.drawString(Point.actualPoint(ppp.getX(), ppp.getY()).toString(), 0, 6);
	//	Sound.systemSound(true, 2);
		
		nav.clearPath();
		
		Waypoint old = translate(points[i]);
		nav.addWaypoint(old);
		boolean first = true;
		if(i<points.length / 2){
			for(int x = i+1; x>=0;x--){
				Waypoint curr = translate(points[x]);
				if(!first){
					if(x!=0){
						if((points[x-1].X == points[x].X && points[x].X == points[x+1].X) 
								|| (points[x-1].Y == points[x].Y && points[x].Y == points[x+1].Y)){
							
						}
						else{
							nav.addWaypoint(curr);
							old = curr;
						}
					}
					else{
						nav.addWaypoint(curr);
					}
				}
				
				if(first)
					first = false;
			}
		}
		else{
			for(int x = i-1; x<points.length;x++){
				Waypoint curr = translate(points[x]);
				if(!first){
					if(x!=points.length-1){
						if((points[x+1].X == points[x].X && points[x].X == points[x-1].X) || (points[x+1].Y == points[x].Y && points[x].Y == points[x-1].Y)){
							
						}
						else{
							nav.addWaypoint(curr);
							old = curr;
						}
					}
					else{
						nav.addWaypoint(curr);
					}
				}
				
				if(first)
					first = false;
			}
		}
		nav.followPath();
		while(nav.isMoving()){}
		Sound.systemSound(true, 3);
		//Connection
		try{sendPoints(g);}catch(IOException e){LCD.clear(); LCD.drawString("SEND ERROR", 0, 0);}
		
		Delay.msDelay(2000);
		System.exit(0);
	}//main(String[])

	
/*		
		
		// find line
		

		
		Point exact = possible(heading, point, ir);	
		Sound.systemSound(true, 3);
		//Leave the area!
		pilot.travel(20);
		while(pilot.isMoving()){}
		nav.stop();
		
		nav.followPath();
		Sound.systemSound(true, 2);
		while(exact == null){
			nav.followPath();
			ir = (int) irData.getIRValue();
			while( ir != 90 && ir != -90){
				Delay.msDelay(50);
				ir = (int) irData.getIRValue();
				LCD.clear();
				LCD.drawInt(ir, 0, 0);
				LCD.drawString("Find new column", 0, 1);
			}
			Sound.systemSound(true, 2);
			nav.stop();
			LCD.clear();
			LCD.drawString("Found new column", 0, 1);
			p = poseProvider.getPose();
			heading = p.getHeading();
			point = Point.actualPoint(p.getX(), p.getY());
			
			exact = possible(heading, point, ir);
		}
		
*/		
		//send info
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public static int f1(double d){
		int ans = ((int) (d - 90 / 45.0)) % 8;
		if(ans < 0)
			return ans + 7;
		return ans;
	}
	
	private static Point possible(double heading, Point p, float angle){
		System.out.println("Possibility --------------");
		for(Point poss: possibility){
			System.out.println(poss);
		}
		boolean rhs = (((int) angle) == 90);
		if(possibility.isEmpty()){
			switch(f1(heading)){
				case 0: {if(rhs)	
							for(int x = p.X + 1; x <= cellsX - 2; x++)
								possibility.add(new Point(x, p.Y));
						else
							for(int x = p.X - 1; x >= 1; x--)
								possibility.add(new Point(x, p.Y));
						break;}
				case 1: {if(rhs)	
							for(int y = p.Y + 1; y <= cellsY - 2; y++)
								possibility.add(new Point(p.X, y));
						else
							for(int y = p.Y - 1; y >= 1; y--)
								possibility.add(new Point(p.X, y));
						break;}
				case 2: {if(rhs)	
							for(int y = p.Y + 1; y <= cellsY - 2; y++)
								possibility.add(new Point(p.X, y));
						else
							for(int y = p.Y - 1; y >= 1; y--)
								possibility.add(new Point(p.X, y));
						break;}
				case 3: {if(rhs)	
							for(int x = p.X - 1; x >= 1; x--)
								possibility.add(new Point(x, p.Y));
						else
							for(int x = p.X + 1; x <= cellsX - 2; x++)
								possibility.add(new Point(x, p.Y));
						break;}
				case 4: {if(rhs)	
							for(int x = p.X - 1; x >= 1; x--)
								possibility.add(new Point(x, p.Y));
						else
							for(int x = p.X + 1; x <= cellsX - 2; x++)
								possibility.add(new Point(x, p.Y));
						break;}
				case 5: {if(rhs)	
							for(int y = p.Y - 1; y >= 1; y--)
								possibility.add(new Point(p.X, y));
						else
							for(int y = p.Y + 1; y <= cellsY - 2; y++)
								possibility.add(new Point(p.X, y));
						break;}
				case 6: {if(rhs)	
							for(int y = p.Y - 1; y >= 1; y--)
								possibility.add(new Point(p.X, y));
						else
							for(int y = p.Y + 1; y <= cellsY - 2; y++)
								possibility.add(new Point(p.X, y));
						break;}
				case 7: {if(rhs)	
						for(int x = p.X + 1; x <= cellsX - 2; x++)
							possibility.add(new Point(x, p.Y));
						else
							for(int x = p.X - 1; x >= 1; x--)
								possibility.add(new Point(x, p.Y));
						break;}
				default:
			}
			return null;
		}
		else{
			LinkedList<Point> swap = new LinkedList<Point>();
			switch(f1(heading)){
			case 0: {if(rhs)	
						for(int x = p.X + 1; x <= cellsX - 2; x++){
							Point p1 = new Point(x, p.Y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					else
						for(int x = p.X - 1; x >= 1; x--){
							Point p1 = new Point(x, p.Y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					break;}
			case 1: {if(rhs)	
						for(int y = p.Y + 1; y <= cellsY - 2; y++){
							Point p1 = new Point(p.X, y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					else
						for(int y = p.Y - 1; y >= 1; y--){
							Point p1 = new Point(p.X, y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					break;}
			case 2: {if(rhs)	
						for(int y = p.Y + 1; y <= cellsY - 2; y++){
							Point p1 = new Point(p.X, y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					else
						for(int y = p.Y - 1; y >= 1; y--){
							Point p1 = new Point(p.X, y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					break;}
			case 3: {if(rhs)	
						for(int x = p.X - 1; x >= 1; x--){
							Point p1 = new Point(x, p.Y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					else
						for(int x = p.X + 1; x <= cellsX - 2; x++){
							Point p1 = new Point(x, p.Y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					break;}
			case 4: {if(rhs)	
						for(int x = p.X - 1; x >= 1; x--){
							Point p1 = new Point(x, p.Y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					else
						for(int x = p.X + 1; x <= cellsX - 2; x++){
							Point p1 = new Point(x, p.Y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					break;}
			case 5: {if(rhs)	
						for(int y = p.Y - 1; y >= 1; y--){
							Point p1 = new Point(p.X, y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					else
						for(int y = p.Y + 1; y <= cellsY - 2; y++){
							Point p1 = new Point(p.X, y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					break;}
			case 6: {if(rhs)	
						for(int y = p.Y - 1; y >= 1; y--){
							Point p1 = new Point(p.X, y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					else
						for(int y = p.Y + 1; y <= cellsY - 2; y++){
							Point p1 = new Point(p.X, y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					break;}
			case 7: {if(rhs)	
					for(int x = p.X + 1; x <= cellsX - 2; x++){
						Point p1 = new Point(x, p.Y);
						if(possibility.contains(p1))
							swap.add(p1);
					}
					else
						for(int x = p.X - 1; x >= 1; x--){
							Point p1 = new Point(x, p.Y);
							if(possibility.contains(p1))
								swap.add(p1);
						}
					break;}
			default:
			}
			if(swap.size() == 1) return swap.get(0);
			else{
				possibility.clear();
				possibility.addAll(swap);
				return null;
			}
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//waveFront calc
	
		//Search from goal, i.e. path to goal
	static int gX = 0;
	static int gY = 4;
	public static Point goal = new Point(gX, gY);		//goal
	   
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
		 
	private static void waveFront(int x, int y){
		gX = x; gY = y;
		resetGraph();
	
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
	private static LinkedList<Point> pathFrom(Point p){
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
		

	private static Waypoint translate(Point p){
		double wx = (p.X - 0.5) * cellWidth;
		double wy = (p.Y - 0.5) * cellWidth;
		return new Waypoint(wx, wy);
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*****
	 * one time InitialGraph setup
	 */
	static LinkedList<Integer> obs = new LinkedList<Integer>();
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
	//		System.out.printf("(%s, %s)\n", coord[0], coord[1]);
			InitialGraph[f(coord[0])][f(coord[1])] = -1;
			obs.add(Integer.valueOf(coord[0])); obs.add(Integer.valueOf(coord[1]));
		}
		sc.close();
	}//genGraph
	
	private static void resetGraph(){graph = copy(InitialGraph);}
    private static int[][] copy(int[][] _graph){
    	int[][] newGraph = new int[_graph.length][];
    	for(int i=0; i<_graph.length; i++)
    		newGraph[i] = new int[_graph[0].length];
    	for(int i=0; i<_graph.length; i++)
    		for(int j=0; j<_graph.length; j++)
    			newGraph[i][j] = _graph[i][j];
    	return newGraph;
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
    
    private static void sendPoints(int[] goal) throws IOException {
        outStream.writeObject(goal); 
  //      outStream.writeObject(obstacles);//so we can write the entire serializable  object
        outStream.flush();                                                                     //flush it all out
        outStream.close();
        t.close();
        s.close();
    }
}

