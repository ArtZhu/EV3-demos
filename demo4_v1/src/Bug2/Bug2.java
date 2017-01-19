package Bug2;

import Threads.*;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.*;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
//import lejos.robotics.navigation.DifferentialPilot;
import ProfTests.DifferentialPilot;
//
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;
import lejos.hardware.sensor.NXTUltrasonicSensor;
//correct version
/**
 * Bug2 Screen
 * 
 * 1	Behavior
 * 2	SensorSample
 * 3	Behavior subPeriod
 * 4	Behavior subPeriod2, sometimes result
 * 5
 * 6
 * 
 */
public class Bug2 {
/*******************
 *  debug
 */	
	//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
	static boolean Debug = true;
	
/*******************
 * Robot setups
 */	
	//Robot Statistics -------------------------------------------------------------------------------------------------------
	static final double robotLength = 15.0;
	static final double wheelDiameter = 5.6;
	static final double wheelCircumference = wheelDiameter * Math.PI;
	static final double robotTrack = 14.5;
	
	//turning
	static final double turnConst = 1.01;
//	static final double turnConst1 = 0.905;
	
	//Robot Controller -------------------------------------------------------------------------------------------------------
/*
//	static TachoMotorPort left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // right
//	static TachoMotorPort right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // left
//	static Brick brick = BrickFinder.getDefault();
//	static NXTRegulatedMotor lm = new NXTRegulatedMotor(brick.getPort("A"));
//	static NXTRegulatedMotor rm = new NXTRegulatedMotor(brick.getPort("B"));
*/	
	
	//Pilot & PoseProvider & Navigator ---------------------------------------------------------------------------------------
	@SuppressWarnings("deprecation")
	static DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, robotTrack, Motor.A, Motor.B);
	static OdometryPoseProvider poseProvider = new OdometryPoseProvider(pilot);
	static Navigator nav = new Navigator(pilot, poseProvider);
	static int stdSpeed = 10;
	static int stdRotateSpeed = 45;
	
	//Sensor------------------------------------------------------------------------------------------------------------------
//	static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S2);
	static NXTUltrasonicSensor frontSonic = new NXTUltrasonicSensor(SensorPort.S2);
//							   right	
	static NXTUltrasonicSensor sideSonic = new NXTUltrasonicSensor(SensorPort.S3);
	
	//Data--------------------------------------------------------------------------------------------------------------------
//	static globalData touchedData = new globalData();
	static globalUltrasonicData frontSonicData = new globalUltrasonicData();
	static globalUltrasonicData sideSonicData = new globalUltrasonicData();
	
	//String------------------------------------------------------------------------------------------------------------------
	static String leftString = "Turn left";
	static String rightString = "Turn right";
	
/*******************
 * behavior groups
 */
	//Go to destination+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Destination & Waypoints-------------------------------------------------------------------------------------------------
	private static final double _X = 200;
	private static final double _Y = 0;
	static final Pose destination = new Pose((int) _X,(int) _Y, 0);
	static final double _M = _Y / _X;
	static boolean arrived() {
		return (poseProvider.getPose().getX() - _X) * (poseProvider.getPose().getX() - _X)
									+ (poseProvider.getPose().getY() - _Y) * (poseProvider.getPose().getY() - _Y) < 1.0;
	}
	
	//Avoid obstacles+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//------------------------------------------------------------------------------------------------------------------------
	// AVOID OBSTACLE FLAG -- Activate when the obstacble encountered
	//									till before robot is back on track
	static boolean AVOIDOBSTACLE = false;
	
	//frontEncounter----------------------------------------------------------------------------------------------------------
	static Pose currObsEncounterPose;
	
	//PIDObstacle-------------------------------------------------------------------------------------------------------------		
	// initialize PID constants
	static final double Kp = 0.5;
	static final double Ki = 0.0001;
	static final double Kd = 0.9;
	
	static int Tp = 25;
	static double aTurn = 0.0;
	static double desiredDist = 10;		//To wall
	
	//PIDnumbers
	static double error;
	static double accumError = 0;
	static double lastError = 0;
	static double errorDiff;
	
	public static void resetPID(){ accumError = 0;}
	public static boolean isSonicData1 = true;
	
	static final double proximity = 5; //Unit???
	
	//rightNoWall-------------------------------------------------------------------------------------------------------------
	static final double goOutDist = 12.5; 	//distance the robot move forward before turning right with 1 wheel
	
	@SuppressWarnings("unused")
	public static void main(String[] aArg) throws Exception {
//		Thread t = new Thread(new touchThread(touchedData, ts));
//		t.setDaemon(true);
//		t.start();

		Thread uFront = new Thread(new sonicThread(frontSonicData, frontSonic));
		uFront.setDaemon(true);
		uFront.start();
		
		Thread uSide = new Thread(new sonicThread(sideSonicData, sideSonic));
		uSide.setDaemon(true);
		uSide.start();

		// establish a fail-safe: pressing Escape quits
		Brick brick = BrickFinder.getDefault(); // get specifics about this
			// robot
			brick.getKey("Escape").addKeyListener(new KeyListener() {
				@SuppressWarnings("deprecation")
				public void keyPressed(Key k) {
					pilot.stop();
//					lm.stop(true); rm.stop();
					frontSonic.close(); sideSonic.close();
//					light.close();
//					ts.close();
					System.exit(1);
				}

				@SuppressWarnings("deprecation")
				public void keyReleased(Key k) {
					pilot.stop();
//					lm.stop(true); rm.stop();
					frontSonic.close(); sideSonic.close();
//					light.close();
//					ts.close();
					System.exit(1);
				}
			});	
		
		LCD.drawString("I'm ready", 0, 1);
		Button.waitForAnyPress();
		
//		touchedData.resetTouched();
//		sonicData.setSonicValue((int) desiredDist);
		
		pilot.reset();
		resetALLFLAGS();
		
		pilot.setAngularAcceleration(stdRotateSpeed);
		pilot.setLinearSpeed(stdSpeed);
		pilot.addMoveListener(poseProvider);
		
		Behavior b0 = new go();							//go should follow the line
		Behavior b1 = new PIDobstacle();				//PID the obstacle till back on line
		Behavior b11= new aroundObstacle();
		Behavior b2 = new rightNoWall();
		Behavior b3 = new frontEncounter();
		
		Behavior[] bArray = {b0, b11, b2, b3};
		Arbitrator arby = new Arbitrator(bArray);		
		arby.start();
	}
	
	public static void resetALLFLAGS(){
		Sound.beep();
		
		//Bug2
		AVOIDOBSTACLE = false;
		isSonicData1 = true;
		
		//aroundObstacle
		aroundObstacle.checkProximity = false;
		
		//PIDobstacle
		PIDobstacle.checkProximity = false;	
	}

}