package adapter;

import java.util.function.*;

import ProfTests.DifferentialPilot;
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
public class waveFrontBug{
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
	static final double turnConst = 0.92;
	
	//Robot Controller -------------------------------------------------------------------------------------------------------

	//Pilot & PoseProvider & Navigator ---------------------------------------------------------------------------------------
	static DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, robotTrack, Motor.A, Motor.B);
	static OdometryPoseProvider poseProvider = new OdometryPoseProvider(pilot);
	static Navigator nav = new Navigator(pilot, poseProvider);
	static int stdSpeed = 10;
	static int stdRotateSpeed = 45;
	
	//Sensor------------------------------------------------------------------------------------------------------------------
	static NXTUltrasonicSensor frontSonic = new NXTUltrasonicSensor(SensorPort.S2);
	
	//Data--------------------------------------------------------------------------------------------------------------------
	static globalUltrasonicData frontSonicData = new globalUltrasonicData();
	
	//String------------------------------------------------------------------------------------------------------------------
	static String leftString = "Turn left";
	static String rightString = "Turn right";
	
	//Step--------------------------------------------------------------------------------------------------------------------
	static double step = 10;
	
	//Read Map ---------------------------------------------------------------------------------------------------------------
	static String format = " ";
	static double cellLength = 30;
	static double mapLength = 350;
	
/*******************
 * behavior groups
 */
	
	static double antennaLong = 35;
	static double antennaShort = 20;
	
	//Go to destination+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Destination & Waypoints-------------------------------------------------------------------------------------------------
	static int X;
	static int Y;
	
	private static final Function<Integer, Double> distFromSource = x -> cellLength * (x - 1/2.0);
	public static final Function<Double, Integer> toCellDist = x -> (int) (x/cellLength);
	private static double _X = distFromSource.apply(X);
	private static double _Y = distFromSource.apply(Y);
	static final Pose destination = new Pose((int) _X,(int) _Y, 0);
	static final double _M = _Y / _X;
//	private static final BiPredicate<Integer, Integer> arrivedAt = x, y -> 
	static boolean arrived() {
		return (poseProvider.getPose().getX() - _X) * (poseProvider.getPose().getX() - _X)
									+ (poseProvider.getPose().getY() - _Y) * (poseProvider.getPose().getY() - _Y) < 1.0;
	}

	//Wait for instructions -------------------------------------------------------------------------------------------------
	static boolean insReceived = false;
	
	public static void main(String[] aArg) throws Exception {
		Thread uFront = new Thread(new sonicThread(frontSonicData, frontSonic));
		uFront.setDaemon(true);
		uFront.start();
		
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
			
		pilot.reset();
		
		pilot.setAngularAcceleration(stdRotateSpeed);
		pilot.setLinearSpeed(stdSpeed);
		pilot.addMoveListener(poseProvider);
		
		Behavior b0 = new go();							//go should follow the line
		Behavior b1 = new waitForInstructions();
		
		Behavior[] bArray = {b0, b1};
		Arbitrator arby = new Arbitrator(bArray);
		
		LCD.drawString("I'm ready", 0, 1);
		Button.waitForAnyPress();
		arby.start();	
	}

	public static void dance(){
		pilot.setRotateSpeed(135);
		pilot.rotate(720);
		Sound.systemSound(true, 3);
	}
}