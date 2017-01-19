package Bug;

import java.util.*;
import java.io.*;
import java.net.*;

import algorithm.Point;
import algorithm.Point2;
import Threads.*;
import ProfTests.*;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.utility.Delay;
import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.hardware.sensor.NXTColorSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.sensor.NXTLightSensor;
//correct version
/**
 * Bug2 Screen
 * 
 * 1	Behavior
 * 2	SensorSample
 * 3	leftString/rightString
 * 4	
 * 5
 * 6	
 * 
 */
public class waveFrontBug2{
/*******************
 *  debug
 */	
	//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
	static boolean Debug = true;
	
	//PORT AND IP  -----------------------------------------------------------------------------------------------------------
	static final String IPAddress = "136.167.109.36";
	static final int port = 6983;
	
	//print ------------------------------------------------------------------------------------------------------------------
	static int count = 10;
	
/*******************
 * Robot setups
 */	
	//Search Graph -----------------------------------------------------------------------------------------------------------
	static int[][] InitialGraph;
	static int gX, gY ;
	static LinkedList<Waypoint> path1 = new LinkedList<Waypoint>();
	
	//Robot Statistics -------------------------------------------------------------------------------------------------------
	
	//turning
	static final double turnConst = 0.81;  // 0.86 0.95
	
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
	static int stdSpeed = 10;
	static int stdRotateSpeed = 45;
	
	//Sensor------------------------------------------------------------------------------------------------------------------
	static NXTUltrasonicSensor frontSonic = new NXTUltrasonicSensor(SensorPort.S2);
//							   right	
	static NXTUltrasonicSensor sideSonic = new NXTUltrasonicSensor(SensorPort.S3);

	//Data--------------------------------------------------------------------------------------------------------------------
	static globalUltrasonicData frontSonicData = new globalUltrasonicData();
	static globalUltrasonicData sideSonicData = new globalUltrasonicData();
	
	//String------------------------------------------------------------------------------------------------------------------
	static String leftString = "Turn left";
	static String rightString = "Turn right";
	
	//Step--------------------------------------------------------------------------------------------------------------------
	static double step = 10;
	
	//Read Map ---------------------------------------------------------------------------------------------------------------
	static double cellLength = 30;
	static double mapLength = 270;
	
/*******************
 * behavior groups
 */
	//Go to destination+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	static Queue<Point> pathReceived;
	static List<Point> pathPoint;
	static Waypoint[] path;
	static boolean arrived() {
		Pose p = poseProvider.getPose();
		return Math.abs(p.getX() - path[0].x) + Math.abs(p.getY() - path[0].y) < 4.0;	//just larger than waypoint checking
	}
	//Destination & Waypoints-------------------------------------------------------------------------------------------------
	public static Waypoint currentWaypoint;
	static double _M;
	//optimize--
	
	//Avoid obstacles+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//------------------------------------------------------------------------------------------------------------------------
	// AVOID OBSTACLE FLAG -- Activate when the obstacble encountered
	//									till before robot is back on track
	static boolean AVOIDOBSTACLE = false;
	static double proximity = 1.0;
	
	//frontEncounter----------------------------------------------------------------------------------------------------------
	static Pose currObsEncounterPose;
	static Waypoint[] encounterPath = new Waypoint[2];

	//IMPORTANT
	private static boolean approx = true;
	private static double approxRange = 5.0;
	@SuppressWarnings("deprecation")
	public static void toCenter(){
		Pose p = poseProvider.getPose();
		float X = p.getX();
		float Y = p.getY();
		if(approx) 
			if(Math.abs((X%cellLength - cellLength/2.0)) + Math.abs((Y%cellLength - cellLength/2.0) * (Y%cellLength - cellLength/2.0)) < approxRange)
				return;
		int cellWidth = (int) cellLength;
		int x1 = cellWidth / 2 + ((int) X / cellWidth) * cellWidth;
		int y1 = cellWidth / 2 + ((int) Y / cellWidth) * cellWidth;
		System.out.printf("AIMING CENTER: (%d, %d) \n", x1, y1);
		nav.goTo(x1, y1);
		Delay.msDelay(50);
		while(nav.isMoving()){
			Delay.msDelay(50);
		}
		//turn heading back
		Pose p1 = poseProvider.getPose();
		pilot.rotate(p.getHeading() - p1.getHeading(), false);
		nav.waitForStop();
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//My bug
	//
	@SuppressWarnings({"deprecation", "unused"})
	public static void main(String[] aArg) throws Exception {
		Thread uFront = new Thread(new sonicThread(frontSonicData, frontSonic));
		uFront.setDaemon(true);
		uFront.start();
		
		Thread uSide = new Thread(new sonicThread(sideSonicData, sideSonic));
		uSide.setDaemon(true);
		uSide.start();
		
		Brick brick = BrickFinder.getDefault();
			brick.getKey("Escape").addKeyListener(new KeyListener() {
				public void keyPressed(Key k) {
					pilot.stop();
					frontSonic.close();
					System.exit(1);
				}
				public void keyReleased(Key k) {
					pilot.stop();
					frontSonic.close();
					System.exit(1);
				}
			});	
			
		//
		pathReceived = null;
		boolean insReceived = false;
		Socket s = null; ObjectInputStream o = null;
		try {
	        s = new Socket(IPAddress, port);    //create socket with laptop ip and port number
	        o = new ObjectInputStream(s.getInputStream());
	        pathReceived =  (Queue<Point>) o.readObject();
	        InitialGraph = (int[][]) o.readObject();
	        Point goal = (Point) o.readObject();
	        gX = goal.X;
	        gY = goal.Y;
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
	        e.printStackTrace();
	        o.close();
	        s.close();
	        System.out.println("\n 333333 \n");
	    } 
	    o.close();
	    s.close();
	    
	    //Simplification
	    pathPoint = new LinkedList<Point>();
	  	Point x = (pathReceived.poll());
	  	pathPoint.add(x);
	  	addWaypointss();
		
		pilot.reset();
	
		((DifferentialPilot) nav.getMoveController()).setAngularSpeed(stdRotateSpeed);
		((DifferentialPilot) nav.getMoveController()).setLinearSpeed(stdSpeed);
		((DifferentialPilot) nav.getMoveController()).addMoveListener((MoveListener) poseProvider);

		
		Waypoint p = path[path.length - 1];
		System.out.printf("start at: (%f, %f)\n", p.x, p.y);
		poseProvider.setPose(new Pose(p.x, p.y, 0));
		Behavior b0 = new go();							//go should follow the path
		Behavior b1 = new recalculation();
		
		Behavior[] bArray = {b0, b1};
//		Behavior[] bArray = {b0};
		Arbitrator arby = new Arbitrator(bArray);
		
//		LCD.drawString("I'm ready", 0, 6);
		Button.waitForAnyPress();
		arby.start();	
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	//Supportive methods
	// 
	
	public static void addWaypointss(){ 	////////dfsajklfdsajkfl;dsaiomcandjaiofhecn;mko
		int x = pathReceived.size();
  		for(int i=1; i<=x; i++){
  			Point curr = pathReceived.poll();
  			if(i==1) {
  				pathPoint.add(curr);
  				continue;
  			}
  			if(curr.X - pathPoint.get(pathPoint.size() - 1).X == pathPoint.get(pathPoint.size() - 1).X - pathPoint.get(pathPoint.size() - 2).X ||
  					curr.Y - pathPoint.get(pathPoint.size() - 1).Y == pathPoint.get(pathPoint.size() - 1).Y - pathPoint.get(pathPoint.size() - 2).Y){
  				pathPoint.remove(pathPoint.size() - 1);
  				pathPoint.add(curr);
  			}
  			else pathPoint.add(curr);	
  		}
  	
  		for(Point p: pathPoint)
  			System.out.print("( " + String.valueOf(p.X) + ", " + String.valueOf(p.Y)+ " ) \n");
  		System.out.println(pathPoint.size());

  		//translation
  		path = new Waypoint[pathPoint.size()];
  		for(int i=0; i<path.length; i++){
  			path[i] = translate(pathPoint.get(i));
  			System.out.print("( " + String.valueOf(path[i].x) + ", " + String.valueOf(path[i].y)+ " ) \n");
  		}
  		
  		//add Waypoints
  		int i = path.length - 2;
  		while(i >= 0){
  	//		path1.add(wp);
  			nav.addWaypoint(path[i]);
  			i--;
  		}
	}
	
	public static void addWaypointss(LinkedList<Point> q){ 	////////dfsajklfdsajkfl;dsaiomcandjaiofhecn;mko
		int x = q.size();
  		for(int i=1; i<=x; i++){
  			Point curr = q.poll();
  			if(i==1) {
  				pathPoint.add(curr);
  				continue;
  			}
  			if(curr.X - pathPoint.get(pathPoint.size() - 1).X == pathPoint.get(pathPoint.size() - 1).X - pathPoint.get(pathPoint.size() - 2).X ||
  					curr.Y - pathPoint.get(pathPoint.size() - 1).Y == pathPoint.get(pathPoint.size() - 1).Y - pathPoint.get(pathPoint.size() - 2).Y){
  				pathPoint.remove(pathPoint.size() - 1);
  				pathPoint.add(curr);
  			}
  			else pathPoint.add(curr);	
  		}
  	
  		for(Point p: pathPoint)
  			System.out.print("( " + String.valueOf(p.X) + ", " + String.valueOf(p.Y)+ " ) \n");
  		System.out.println(pathPoint.size());

  		//translation
  		path = new Waypoint[pathPoint.size()];
  		for(int i=0; i<path.length; i++){
  			path[i] = translate(pathPoint.get(i));
  			System.out.print("( " + String.valueOf(path[i].x) + ", " + String.valueOf(path[i].y)+ " ) \n");
  		}
  		
  		//add Waypoints
  		int i = path.length - 2;
  		while(i >= 0){
  	//		path1.add(wp);
  			nav.addWaypoint(path[i]);
  			i--;
  		}
	}
	
	public static Waypoint translate(Point p){
		int poseX = (int) ((p.Y - 0.5) * cellLength);
		int poseY = (int) ((InitialGraph.length - 2 - p.X + 0.5) * cellLength);
		return new Waypoint(poseX, poseY);
	}
	
	
	public static int f1(double d){
		int ans = ((int) (d / 45.0)) % 8;
		if(ans < 0)
			return ans + 7;
		return ans;
	}
	
	public static int dirTo(Waypoint p1, Waypoint p2){
		double x = p1.x - p2.x;
		double y = p1.y - p2.y;
		if(x > -2.0 && x < 2.0)
			if(y < -2.0)
				return 1;
			if(y > 2.0)
				return 3;
		if(y > -2.0 && y < 2.0)
			if(x < -2.0)
				return 0;
			if(x > 2.0)
				return 2;
		else
			return -1;
	}
	
	public static boolean closeTo(Waypoint p1, Waypoint p2){
		double x = p1.x - p2.x;
		double y = p1.y - p2.y;
		return (Math.abs(x) < 1.0 && Math.abs(y) < 1.0);
	}

	@SuppressWarnings("deprecation")
	public static void set(){
		((DifferentialPilot) waveFrontBug2.nav.getMoveController()).setAngularSpeed(waveFrontBug2.stdRotateSpeed);
		((DifferentialPilot) waveFrontBug2.nav.getMoveController()).setLinearSpeed(waveFrontBug2.stdSpeed);
	}
	
	public static double distTo(Waypoint p1, Waypoint p2){
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
	}
	
	public static void mark(Point p){
		InitialGraph[p.X][p.Y] = -1; 
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//!!!!
	//
	public static void dance(){
		Sound.beep();
		pilot.setAngularSpeed(135);
		pilot.rotate(360);
	}
	
	
	public static void warning(){
		pilot.stop();
		Sound.systemSound(true, 4);
		LCD.clear();
		LCD.drawString("I can't go to destination anymore!", 1, 1);
		Delay.msDelay(20000);
		System.exit(0);
	}
}