package Bug3;

import ProfTests.*;
import Threads.*;
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
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
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
public class Bug3 {
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
	static final double turnConst = 1.04;
	static final double turnConst1 = 0.905;
	
	//Robot Controller -------------------------------------------------------------------------------------------------------
/*
//	static TachoMotorPort left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // right
//	static TachoMotorPort right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // left
//	static Brick brick = BrickFinder.getDefault();
//	static NXTRegulatedMotor lm = new NXTRegulatedMotor(brick.getPort("A"));
//	static NXTRegulatedMotor rm = new NXTRegulatedMotor(brick.getPort("B"));
*/	
	
	//Pilot & PoseProvider & Navigator ---------------------------------------------------------------------------------------
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
	static HiTechnicEOPD eopd = new HiTechnicEOPD(SensorPort.S4);
	
	//Data--------------------------------------------------------------------------------------------------------------------
//	static globalData touchedData = new globalData();
	static globalUltrasonicData frontSonicData = new globalUltrasonicData();
	static globalUltrasonicData sideSonicData = new globalUltrasonicData();
	static globalEOPDData eopdData = new globalEOPDData();
	
	//String------------------------------------------------------------------------------------------------------------------
	static String leftString = "Turn left";
	static String rightString = "Turn right";
	
	//Step--------------------------------------------------------------------------------------------------------------------
	static double step = 10;
	
/*******************
 * behavior groups
 */
	
	static double antennaLong = 35;
	static double antennaShort = 20;
	
	//Go to destination+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Destination & Waypoints-------------------------------------------------------------------------------------------------
	private static final double _X = 150;
	private static final double _Y = 0;
	static final Pose destination = new Pose((int) _X,(int) _Y, 0);
	static final double _M = _Y / _X;
	static boolean arrived() {
		return (poseProvider.getPose().getX() - _X) * (poseProvider.getPose().getX() - _X)
									+ (poseProvider.getPose().getY() - _Y) * (poseProvider.getPose().getY() - _Y) < 2.0;
	}
	
	//Avoid obstacles+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//------------------------------------------------------------------------------------------------------------------------
	// AVOID OBSTACLE FLAG -- Activate when the obstacble encountered
	//									till before robot is back on track
	static boolean AVOIDOBSTACLE = false;
	
	//frontEncounter----------------------------------------------------------------------------------------------------------
//	static Pose currObsEncounterPose;
	
	//PIDObstacle-------------------------------------------------------------------------------------------------------------		
	// initialize PID constants
	static final double Kp = 7.0;
	static final double Ki = 0.0001;
	static final double Kd = 5.0;
	
	static int Tp = 25;
	static double aTurn = 0.0;
	static double desiredDist = (antennaShort);		//To wall
	
	//PIDnumbers
	static double error;
	static double accumError = 0;
	static double lastError = 0;
	static double errorDiff;
	
	public static void resetPID(){ 
		accumError = 0; 
		Bug3.isSonicData1 = true;
		}
	public static boolean isSonicData1 = true;
	
	static final double proximity = 0.5;
	public static boolean backOnTrack(){return (_M * poseProvider.getPose().getX()) - poseProvider.getPose().getY() < proximity;}
	
	//if destination direction has no obstacle		//UNIT
	public static boolean goodTogo(){
			//a bit overshoot
			int frontDist = frontSonicData.getSonicValue();
			int sideDist = sideSonicData.getSonicValue();
			if(sideDist < antennaLong){
				return false;
			}
			boolean b = frontDist > antennaLong && sideDist > antennaLong;
			if(b){
				LCD.drawString("front" + String.valueOf(frontDist), 0, 6);
				nav.getMoveController().travel(goOutDist);
//				Delay.msDelay(500);
			}
			
			return b;
	}
	
/*	
	public static boolean goodTogo(){
		double stdHeading = Math.atan(_Y/_X) / Math.PI * 180;	//unit?
		double range = Math.abs(stdHeading - poseProvider.getPose().getHeading());
		if(range < 45) {
			//a bit overshoot
			int frontDist = frontSonicData.getSonicValue();
			boolean b = frontDist > antennaLong;
			if(b){
				LCD.drawString("front" + String.valueOf(frontDist), 0, 6);
//				nav.getMoveController().travel(goOutDist);
//				Delay.msDelay(500);
			}
			
			return b;
		}
		else {
			//a bit overshoot
			int sideDist = sideSonicData.getSonicValue();
			boolean b =  sideDist > antennaLong;
			if(b){
				LCD.drawString("side" + String.valueOf(sideDist) , 0, 6);
				nav.getMoveController().travel(goOutDist);
				Delay.msDelay(500);
			}
			return b;
		}
	}
*/	
	
/*
	static int visionAngle = 30;
	public static boolean goodTogo(){
		double stdHeading = Math.atan(_Y/_X) / Math.PI * 180;	//unit?
		double range1 = (stdHeading - poseProvider.getPose().getHeading()) % 360;
		if(range1 < visionAngle || range1 > 360 - visionAngle) {
			//a bit overshoot
			int frontDist = frontSonicData.getSonicValue();
			boolean b = frontDist > antennaLong;
			if(b){
				LCD.drawString("front" + String.valueOf(frontDist), 0, 6);
			}
			
			return b;
		}
		else {
			//if destination on LHS, return true
			if(range1 < 360-visionAngle && range1 > 180 + visionAngle) return true;
			
			//if on RHS, a bit overshoot
			int sideDist = sideSonicData.getSonicValue();
			boolean b =  sideDist > antennaLong;
			if(b){
				LCD.drawString("side" + String.valueOf(sideDist) , 0, 6);
				((DifferentialPilot) nav.getMoveController()).rotate(-1 * 90 * turnConst, true);
				nav.getMoveController().travel(goOutDist);
				while(nav.isMoving()) Delay.msDelay(10);
				b = goodTogo();
			}
			return b;
		}
	}
*/
	//rightNoWall-------------------------------------------------------------------------------------------------------------
	static final double goOutDist = antennaShort; 	//distance the robot move forward before turning right with 1 wheel
	
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

		Thread e = new Thread(new EOPDThread(eopdData, eopd));
		e.setDaemon(true);
		e.start();
		
		// establish a fail-safe: pressing Escape quits
		Brick brick = BrickFinder.getDefault(); // get specifics about this
			// robot
			brick.getKey("Escape").addKeyListener(new KeyListener() {
				public void keyPressed(Key k) {
					pilot.stop();
//					lm.stop(true); rm.stop();
					frontSonic.close(); sideSonic.close();
//					light.close();
//					ts.close();
					System.exit(1);
				}

				public void keyReleased(Key k) {
					pilot.stop();
//					lm.stop(true); rm.stop();
					frontSonic.close(); sideSonic.close();
//					light.close();
//					ts.close();
					System.exit(1);
				}
			});	
		

		
//		touchedData.resetTouched();
//		sonicData.setSonicValue((int) desiredDist);
		
		pilot.reset();
		resetALLFLAGS();
		
		pilot.setAngularAcceleration(stdRotateSpeed);
		pilot.setLinearSpeed(stdSpeed);
		pilot.addMoveListener(poseProvider);
		
		Behavior b0 = new go();							//go should follow the line
		Behavior b1 = new PIDobstacle();				//PID the obstacle till back on line
		Behavior b2 = new frontEncounter();
		
		Behavior[] bArray = {b0, b1, b2};
		Arbitrator arby = new Arbitrator(bArray);
		
		LCD.drawString("I'm ready", 0, 1);
		Button.waitForAnyPress();
		arby.start();	
	}
	
	public static void resetALLFLAGS(){
		Sound.beep();
		
		//
		AVOIDOBSTACLE = false;
		isSonicData1 = true;
		
	}
	
	public static void dance(){
		pilot.setRotateSpeed(135);
		pilot.rotate(720);
		Sound.systemSound(true, 3);
	}
}