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
public class waveFrontBug{
/*******************
 *  debug
 */	
	//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
	static boolean Debug = true;
	
	//PORT AND IP  -----------------------------------------------------------------------------------------------------------
	static final String IPAddress = "136.167.109.36";
	static final int port = 6983;
	
/*******************
 * Robot setups
 */	
	//Search Graph -----------------------------------------------------------------------------------------------------------
	static int[][] graph;
	static int gX = 7, gY = 8;
	static Waypoint[] path;
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
	static boolean arrived() {
		Pose p = poseProvider.getPose();
		return Math.abs(p.getX() - path[0].x) + Math.abs(p.getY() - path[0].y) < 2.0;
	}
	//Destination & Waypoints-------------------------------------------------------------------------------------------------
	public static int currStartIndex = 0;
	public static Waypoint currentWaypoint;
	static double _M;
	//optimize--
	static final void update(){
		if(currStartIndex < path.length - 2){
			currStartIndex ++;
			int i = currStartIndex;
			double _X = path[i+1].x - path[i].x  + 0.001;
			double _Y = path[i+1].y - path[i].y  + 0.001;
			_M = _Y/_X;		
		}
	}
	
	//Avoid obstacles+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//------------------------------------------------------------------------------------------------------------------------
	// AVOID OBSTACLE FLAG -- Activate when the obstacble encountered
	//									till before robot is back on track
	static boolean AVOIDOBSTACLE = false;
	static double proximity = 1.0;
	public static boolean backOnTrack(){
		Pose p = poseProvider.getPose();
		return (Math.abs((_M * (p.getX() - path[currStartIndex].x)) - (p.getY() - path[currStartIndex].y)) < proximity);
	}
	
	//frontEncounter----------------------------------------------------------------------------------------------------------
	static Pose currObsEncounterPose;

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
		nav.goTo(cellWidth / 2 + ((int) X / cellWidth) * cellWidth, cellWidth / 2 + ((int) Y / cellWidth) * cellWidth);
		while(nav.isMoving()){
			Delay.msDelay(50);
		}
		//turn heading back
		Pose p1 = poseProvider.getPose();
		pilot.rotate(p1.getHeading() - p.getHeading());
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
		Queue<Point> pathReceived = null;
		boolean insReceived = false;
		Socket s = null; ObjectInputStream o = null;
		try {
	        s = new Socket(IPAddress, port);    //create socket with laptop ip and port number
	        o = new ObjectInputStream(s.getInputStream());
	        pathReceived =  (Queue<Point>) o.readObject();
	        graph = (int[][]) o.readObject();
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
        
		for(Point p: pathReceived)
			System.out.print("( " + String.valueOf(p.X) + ", " + String.valueOf(p.Y)+ " ) \n");
		System.out.println(pathReceived.size());
	    
		//Simplification
		List<Point> pathPoint = new ArrayList<Point>();
		pathPoint.add(pathReceived.poll());
//		System.out.println(pathPoint.get(0));
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
		
		pilot.reset();
		resetALLFLAGS();
	
		((DifferentialPilot) nav.getMoveController()).setAngularSpeed(stdRotateSpeed);
		((DifferentialPilot) nav.getMoveController()).setLinearSpeed(stdSpeed);
		((DifferentialPilot) nav.getMoveController()).addMoveListener((MoveListener) poseProvider);
	
		Waypoint p = path[path.length - 1];
		System.out.printf("start at: (%f, %f)\n", p.x, p.y);
		poseProvider.setPose(new Pose(p.x, p.y, 0));
		Behavior b0 = new go();							//go should follow the path
		Behavior b1 = new aroundObstacle();
		Behavior b2 = new rightNoWall();
		Behavior b3 = new frontEncounter();
		
	//	Behavior[] bArray = {b0, b1, b2, b3};
		Behavior[] bArray = {b0};
		Arbitrator arby = new Arbitrator(bArray);
		
	//	LCD.drawString("I'm ready", 0, 6);
		currentWaypoint = path[path.length - 2];
		Button.waitForAnyPress();
		arby.start();	
	}

	public static void resetALLFLAGS(){
//		Sound.beep();
		
		//Bug2
		AVOIDOBSTACLE = false;
		
		//aroundObstacle
		aroundObstacle.checkProximity = false;	
	}
	
	public static Waypoint translate(Point p){
		int poseX = (int) ((p.Y - 0.5) * cellLength);
		int poseY = (int) ((graph.length - 2 - p.X + 0.5) * cellLength);
		return new Waypoint(poseX, poseY);
	}
	
	private static int f(double d){
		int ans = ((int) (d / 45.0) - 2) % 8;
		if(ans < 0)
			return ans + 7;
		return ans;
	}
	public static boolean rightNoWall(){
		Pose p = poseProvider.getPose();
		Point point = Point.actualPoint(p.getX(), p.getY());
		Point right;
		switch(f1(p.getHeading())){
			case 0: right = new Point(point.X + 1, point.Y);
					break;
			case 1: right = new Point(point.X, point.Y + 1);
					break;
			case 2: right = new Point(point.X, point.Y + 1);
					break;
			case 3: right = new Point(point.X - 1, point.Y);
					break;
			case 4: right = new Point(point.X - 1, point.Y);
					break;
			case 5: right = new Point(point.X, point.Y - 1);
					break;
			case 6: right = new Point(point.X, point.Y - 1);
					break;
			case 7: right = new Point(point.X + 1, point.Y);
					break;
			default: right = null;
					break;
		}
		return sideSonicData.getSonicValue() > 20 && (graph[right.X][right.Y]!= -1);
	}
	
	private static int f1(double d){
		int ans = ((int) (d / 45.0)) % 8;
		if(ans < 0)
			return ans + 7;
		return ans;
	}
	
	public static boolean frontEncounter(){
		Pose p = poseProvider.getPose();
		Point point = Point.actualPoint(p.getX(), p.getY());
		Point point2 = Point.actualPoint(currentWaypoint.x, currentWaypoint.y);
		if(point.X == point2.X && point.Y == point2.Y) {
			toCenter(); 
			resetALLFLAGS();
			return false;
		}
		
		Point front;
		switch(f1(p.getHeading())){
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
			default: {	
				System.out.println("defaultFront");
				front = new Point(point.X, point.Y + 1);
			}
					break;
		}
		boolean x = frontSonicData.getSonicValue() < 20;
		boolean y = false;
		try{
		 y = graph[front.X][front.Y] == -1;
		}catch(Exception e){System.out.println(f1(p.getHeading()));}
		return  x||y;
	}
	
	public static void dance(){
		Sound.beep();
		pilot.setAngularSpeed(135);
		pilot.rotate(720);
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