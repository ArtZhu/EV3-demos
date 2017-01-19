package hw2b;

import Threads_prof.globalData;
import Threads_prof.globalLightData;
import Threads_prof.globalUltrasonicData;
import Threads_prof.lightThread;
import Threads_prof.sonicThread;
import Threads_prof.touchThread;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.utility.Delay;
import lejos.hardware.sensor.NXTColorSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.sensor.NXTLightSensor;
//correct version
/**
 * The light sensor should be connected to port 3. The left motor should be
 * connected to port A and the right motor to port B.
 * 
 * controlMotor public void controlMotor(int power,int mode)
 * 
 * Low-level method to control a motor. Specified by: controlMotor in interface
 * BasicMotorPort Parameters: power - power from 0-100 mode - 1=forward,
 * 2=backward, 3=stop, 4=float
 */
public class part3b {
	static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S2);
	static NXTUltrasonicSensor sideSonic = new NXTUltrasonicSensor(SensorPort.S3);
	
	//Data
	// static NXTSoundSensor sound = new NXTSoundSensor(SensorPort.S4);
//	static DifferentialPilot pilot = new DifferentialPilot(5.6, 17.00, Motor.A, Motor.B);
	
	static globalData touchedData = new globalData();
	static globalUltrasonicData sonicData = new globalUltrasonicData();
	
	//
	static String leftString = "Turn left ";
	static String rightString = "Turn right";
	static TachoMotorPort left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // right
	static TachoMotorPort right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // left
	static NXTRegulatedMotor lm;
	static NXTRegulatedMotor rm;
	
	static final double robotLength = 24.0;
	static final double wheelDiameter = 5.6;
	static final double wheelCircumference = wheelDiameter * Math.PI;
	static final double robotTrack = 14.5;
	static final double turnConst = 0.875;
	static final double turnConst1 = 0.885;
	
	// initialize PID constants
	static final double Kp = 0.5;
	static final double Ki = 0.0001;
	static final double Kd = 0.9;
	
	static int Tp = 25;
	static double aTurn = 0.0;
	static double desiredDist = 12.5;
	static final double stdDist = 12.5; //distance the robot move forward before turning left with 1 wheel
	
	//PID
	static double error;
	static double accumError = 0;
	static double lastError = 0;
	static double errorDiff;
	
//	public static boolean flag = false;
	
	public static boolean finish = false;

	
	public static void resetPID(){ accumError = 0;}
	
	public static boolean isSonicData1 = true;
	
	public static void main(String[] aArg) throws Exception {
		Thread t = new Thread(new touchThread(touchedData, ts));
		t.setDaemon(true);
		t.start();

		Thread u = new Thread(new sonicThread(sonicData, sideSonic));
		u.setDaemon(true);
		u.start();

		// establish a fail-safe: pressing Escape quits
		Brick brick = BrickFinder.getDefault(); // get specifics about this
			// robot
			brick.getKey("Escape").addKeyListener(new KeyListener() {
				public void keyPressed(Key k) {
					finish = true;
					//	pilot.stop();
					left.controlMotor(50, 3);
					right.controlMotor(50, 3);
					sideSonic.close();
					ts.close();
					System.exit(1);
				}

				public void keyReleased(Key k) {
					finish = true;
					left.controlMotor(50, 3);
					right.controlMotor(50, 3);
					sideSonic.close();
					ts.close();
					System.exit(1);
				}
			});	
		
		LCD.drawString("I'm ready", 0, 1);
		Button.waitForAnyPress();
		
		touchedData.resetTouched();
		sonicData.setSonicValue((int) desiredDist);
		
		Behavior b0 = new noPID();
		Behavior b1 = new wallFollow();
		Behavior b2 = new leftUndetected();
		Behavior b3 = new beforeLeftDetected();
		Behavior b4 = new frontTouch();
		Behavior[] bArray = {b1, b2, b3, b4};
		Arbitrator arby = new Arbitrator(bArray);
		arby.start();
	}

}