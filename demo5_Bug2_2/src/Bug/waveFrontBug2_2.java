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
public class waveFrontBug2_2{
/*******************
 *  debug
 */	
	//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
	static boolean Debug = true;
	static boolean sound = true;
	
	//PORT AND IP  -----------------------------------------------------------------------------------------------------------
	static final String IPAddress = "136.167.109.36";
	static final int port = 6983;
	
	//print ------------------------------------------------------------------------------------------------------------------
	static int count = 10;
	
/*******************
 * Robot setups
 */	
	//Search Graph -----------------------------------------------------------------------------------------------------------
	static int[][] graph;
	static int gX, gY ;
	static LinkedList<Waypoint> path1 = new LinkedList<Waypoint>();
	
	//Robot Statistics -------------------------------------------------------------------------------------------------------
	
	//turning
	static final double turnConst = 0.825;  // 0.86 0.95
	
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
	static LinkedList<Waypoint> pathPoint1 = new LinkedList<Waypoint>();
	static boolean arrived() {
		Pose p = poseProvider.getPose();
		return Math.abs(p.getX() - pathPoint1.getLast().x) + Math.abs(p.getY() - pathPoint1.getLast().y) < 4.0;	//just larger than waypoint checking
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
	static boolean checkProximity = false;
	static double proximity = 1.0;
	public static double[] slope(Waypoint w1, Waypoint w2){
		double x = w2.x - w1.x;
		double y = w2.y - w1.y;
		if(x == 0) x = 0.001;
		if(y == 0) y = 0.001;
		double m = y/x;
		double b = w2.y - w2.x * m; 
		return new double[]{m, b};
	}
	//curr pose on which track
	public static Waypoint[] onTrack(Waypoint fake){
		for(int i=0; i<pathPoint1.size() - 1; i++){
			Waypoint w1 = pathPoint1.get(i);
			Waypoint w2 = pathPoint1.get(i+1);
			
			double[] mb = slope(w1, w2);
			
			if((fake.y - fake.x * mb[0]) - mb[1] < 2.0){
				Waypoint[] ans = new Waypoint[]{w1, w2};
				if(inBetween(fake, ans)){
					return ans;
				}
			}
		}
		return null;	//null = not on track;
	}
	
	public static boolean inBetween(Waypoint wp, Waypoint[] track){
		Waypoint w1 = track[0];
		Waypoint w2 = track[1];
		return 	((wp.x <= w1.x && wp.y <= w1.y) && (wp.x >= w2.x && wp.y >= w2.y))||
				((wp.x <= w1.x && wp.y >= w1.y) && (wp.x >= w2.x && wp.y <= w2.y))||
				((wp.x >= w1.x && wp.y <= w1.y) && (wp.x <= w2.x && wp.y >= w2.y))||
				((wp.x >= w1.x && wp.y >= w1.y) && (wp.x <= w2.x && wp.y <= w2.y));
	}
	

	
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
	        graph = (int[][]) o.readObject();
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
		pathPoint = new ArrayList<Point>();
		pathPoint.add(pathReceived.poll());
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

		
		//
		for(Point p: pathPoint){
			System.out.print("( " + String.valueOf(p.X) + ", " + String.valueOf(p.Y)+ " ) \n");
			pathPoint1.addFirst(translate(p));
		}
		
		System.out.print("pathPoint1\n");
		for(Waypoint p: pathPoint1){
			System.out.printf("(%f, %f) \n", p.x, p.y);
		}		
		
		pilot.reset();
		resetALLFLAGS();
	
		((DifferentialPilot) nav.getMoveController()).setAngularSpeed(stdRotateSpeed);
		((DifferentialPilot) nav.getMoveController()).setLinearSpeed(stdSpeed);
		((DifferentialPilot) nav.getMoveController()).addMoveListener((MoveListener) poseProvider);

		
		Waypoint p = pathPoint1.get(0);
		System.out.printf("start at: (%f, %f)\n", p.x, p.y);
		poseProvider.setPose(new Pose(p.x, p.y, 0));
	
		LCD.drawString("I'm ready", 0, 6);
		currentWaypoint = pathPoint1.get(1);
		System.out.printf("going to: (%f, %f)\n", currentWaypoint.x, currentWaypoint.y);
		Button.waitForAnyPress();
		while(true){
			if(waveFrontBug2_2.arrived()) {
				Sound.beep();
				waveFrontBug2_2.dance();
				System.exit(0);
			}
		Pose p1 = waveFrontBug2_2.poseProvider.getPose();
		Waypoint fake = new Waypoint(p1.getX(), p1.getY());
		Waypoint curr = waveFrontBug2_2.pathPoint1.get(0);
		Point point = Point.actualPoint(waveFrontBug2_2.mapLength - p1.getY(), p1.getX());
		Point front;
		switch(waveFrontBug2_2.f1(p1.getHeading())){
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
	
		if(waveFrontBug2_2.frontSonicData.getSonicValue() < 30 || (waveFrontBug2_2.graph[front.X][front.Y] == -1)){
			System.out.printf("FRONTENCOUNTER\n");
			
			if(waveFrontBug2_2.Debug) {
				LCD.clear();
				LCD.drawString("frontEncounter", 0, 1);
				//if(sound) Sound.systemSound(true, 3);
			}
			 
			//for the first time we encounter an obstacle, we are not yet in the behavior group AVOIDOBSTACLE,
			//				then we are to record the pose we encountered.
			//				This pose will be used later to make sure that 
			//					we leave the proximity of our optimal path before we started to detect if we are back in proximity
			if(!waveFrontBug2_2.AVOIDOBSTACLE) {
				System.out.printf("AVOID START\n");
				waveFrontBug2_2.nav.stop();
				record();
			}
			
			Pose p2 = waveFrontBug2_2.poseProvider.getPose();
			System.out.printf("currentPoint: (%d,  %d)\n", (int) p2.getX(), (int) p2.getY());
			System.out.printf("GOING TO CENTER\n");
			waveFrontBug2_2.toCenter();
			p2 = poseProvider.getPose();
			System.out.printf("Reached Center: (%d,  %d)\n", (int) p2.getX(), (int) p2.getY());
			System.out.printf("Heading: %d\n", (int) p2.getHeading());
			System.out.printf("f1(Heading): %d\n", (int) waveFrontBug2_2.f1(p2.getHeading()));
			waveFrontBug2_2.set();
			((DifferentialPilot) waveFrontBug2_2.nav.getMoveController()).rotate(90, false);
			System.out.print("Turned\n");
			System.out.printf("Heading: %d\n", (int) p2.getHeading());
			System.out.printf("f1(Heading): %d\n", (int) waveFrontBug2_2.f1(p2.getHeading()));
			
			//if(sound) Sound.beep();
		
			//Last, we encountered something;
			//				we are to AVOIDOBSTACLE
			waveFrontBug2_2.nav.waitForStop();
			Delay.msDelay(200);
			waveFrontBug2_2.nav.waitForStop();
			Delay.msDelay(200);
			waveFrontBug2_2.nav.waitForStop();
			while(waveFrontBug2_2.pilot.isMoving())
				Delay.msDelay(50);
			if(!waveFrontBug2_2.AVOIDOBSTACLE)
				if(sound) Sound.systemSound(true, 2);
			waveFrontBug2_2.AVOIDOBSTACLE = true;
		}
			
		Pose p3 = waveFrontBug2_2.poseProvider.getPose();
		Point point1 = Point.actualPoint(waveFrontBug2_2.mapLength - p3.getY(), p3.getX());
		Point right;
		switch(waveFrontBug2_2.f1(p3.getHeading())){
			case 0: right = new Point(point1.X + 1, point1.Y);
					break;
			case 1: right = new Point(point1.X, point1.Y + 1);
					break;
			case 2: right = new Point(point1.X, point1.Y + 1);
					break;
			case 3: right = new Point(point1.X - 1, point1.Y);
					break;
			case 4: right = new Point(point1.X - 1, point1.Y);
					break;
			case 5: right = new Point(point1.X, point1.Y - 1);
					break;
			case 6: right = new Point(point1.X, point1.Y - 1);
					break;
			case 7: right = new Point(point1.X + 1, point1.Y);
					break;
			default: right = null;
					break;
		}
		
		int sideDist = waveFrontBug2_2.sideSonicData.getSonicValue();
		if((sideDist > 25) && (waveFrontBug2_2.graph[right.X][right.Y]!= -1)){
			System.out.printf("RIGHTNOWALL\n");

		
			waveFrontBug2_2.toCenter();		
			waveFrontBug2_2.set();
			((DifferentialPilot) waveFrontBug2_2.nav.getMoveController()).rotate(-1 * 90);
			
			//find wall
			waveFrontBug2_2.set();
			((DifferentialPilot) waveFrontBug2_2.nav.getMoveController()).steer(0);
			while(rightNoWall()){
			LCD.drawString("findingWall", 0, 3);
			Delay.msDelay(50);
			}
		
			waveFrontBug2_2.toCenter();
		
			while(waveFrontBug2_2.nav.isMoving()){
				Delay.msDelay(50);
			}
		}
		
		if(AVOIDOBSTACLE){
			//if we are back to where we started to avoid obstacle, then we are stuck
			Pose p5 = waveFrontBug2_2.poseProvider.getPose();
			Waypoint fake1 = new Waypoint(p5.getX(), p5.getY());
			
			//control pilot
			waveFrontBug2_2.set();
			if(!waveFrontBug2_2.pilot.isMoving())	waveFrontBug2_2.pilot.steer(0);

			//only when we already left the proximity range that we set flag to true
			if(!checkProximity){
				Waypoint[] track = waveFrontBug2_2.onTrack(fake1);
				if(track == null){
					Delay.msDelay(50);
					checkProximity = true;
				}
			}
		
			if(checkProximity){
				Waypoint avoidSource = new Waypoint(waveFrontBug2_2.currObsEncounterPose.getX(), waveFrontBug2_2.currObsEncounterPose.getY());
	/*			if(waveFrontBug2.closeTo(fake, avoidSource)){
					waveFrontBug2.warning();
				}
	*/			Waypoint[] track = waveFrontBug2_2.onTrack(fake1);
				if(track != null)	{
					while(waveFrontBug2_2.pathPoint1.poll() != track[0]){}
					waveFrontBug2_2.nav.stop();
					waveFrontBug2_2.AVOIDOBSTACLE = false;
					System.out.print("EXIT AVOID\n");
				}
			}
			

			Delay.msDelay(10);
		}
		
		//
		waveFrontBug2_2.set();
		if(!waveFrontBug2_2.nav.isMoving()){
						
			Pose p6 = waveFrontBug2_2.poseProvider.getPose();
			Waypoint fake6 = new Waypoint(p.getX(), p.getY());
			Waypoint curr6 = waveFrontBug2_2.pathPoint1.get(0);
			Waypoint next = waveFrontBug2_2.pathPoint1.get(1);
			

			Point curr1 = Point.actualPoint(waveFrontBug2_2.mapLength - curr6.getY(), curr6.getX());
			Point fake1 = Point.actualPoint(waveFrontBug2_2.mapLength - fake6.getY(), fake6.getX());
			if((curr1.X == fake1.X && curr1.Y == fake1.Y) 
					/*			||	(curr1.X == fake1.X && curr1.Y == fake1.Y+1)
								||	(curr1.X == fake1.X && curr1.Y == fake1.Y-1)
								||	(curr1.X == fake1.X+1 && curr1.Y == fake1.Y)
								||	(curr1.X == fake1.X-1 && curr1.Y == fake1.Y)
					*/				)
								frontEncounter.flag = false;
			if(waveFrontBug2_2.closeTo(fake6, curr6)){
				Delay.msDelay(500);
				int heading = waveFrontBug2_2.f1(p.getHeading());
				heading = (heading + 1) % 8 / 2;
				int angle = (waveFrontBug2_2.dirTo(curr6, next) - heading ) * 90;
				if(angle > 180)
					angle = angle - 360;
				else if(angle < -180)
					angle = 360 + angle;
				if(sound) Sound.beep();
				System.out.printf("Turning %d \n", angle);
				waveFrontBug2_2.pilot.rotate(angle);
			//	Pose pc = waveFrontBug2.poseProvider.getPose();
			//	waveFrontBug2.poseProvider.setPose(new Pose(p.getX(), p.getY(), p.getHeading()+angle));
				System.out.printf("Pose SET: (%d, %d, %d) \n", (int) p.getX(), (int) p.getY(), (int) p.getHeading());
				frontEncounter.flag = true;
				waveFrontBug2_2.pathPoint1.remove(0);
			}
			//start going
			Waypoint wp = waveFrontBug2_2.pathPoint1.get(0);
			System.out.printf("GO TO: (%f, %f)\n", wp.x, wp.y);
			waveFrontBug2_2.nav.goTo(wp);
		}
	
		Delay.msDelay(50);
		}
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	//Supportive methods
	//
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
		((DifferentialPilot) waveFrontBug2_2.nav.getMoveController()).setAngularSpeed(waveFrontBug2_2.stdRotateSpeed);
		((DifferentialPilot) waveFrontBug2_2.nav.getMoveController()).setLinearSpeed(waveFrontBug2_2.stdSpeed);
	}
	
	public static double distTo(Waypoint p1, Waypoint p2){
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
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
	
	
	private static void record(){
		System.out.printf("RECORDING");
		Pose p = waveFrontBug2_2.poseProvider.getPose();
		waveFrontBug2_2.currObsEncounterPose = waveFrontBug2_2.poseProvider.getPose();
		waveFrontBug2_2.encounterPath[0] = new Waypoint(p.getX(), p.getY());
		waveFrontBug2_2.encounterPath[1] = waveFrontBug2_2.pathPoint1.get(0);
	}
	
	public static boolean rightNoWall(){
		Pose p = poseProvider.getPose();
		Point point = Point.actualPoint(mapLength - p.getY(), p.getX());
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
		
		int sideDist = sideSonicData.getSonicValue();
		if((sideDist > 25) && (graph[right.X][right.Y]!= -1)){
			System.out.printf("RIGHTNOWALL\n");
			System.out.printf("SideDist: %d\n", sideDist);
			System.out.printf("Right: (%d, %d)\n", right.X, right.Y);
			return true;
		}
		return false;
	}	
/*
 * for(int i=0; i<pathPoint1.size(); i++){
			Waypoint curr = pathPoint1.get(i);
			if(closeTo(fake, curr)) {
				resetALLFLAGS();
				while(i>0) {
					pathPoint1.remove(0);
					i--;
				}
				return false;
			}
		}
 */
}