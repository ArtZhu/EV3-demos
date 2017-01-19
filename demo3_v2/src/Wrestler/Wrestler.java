package Wrestler;

import Threads.globalData;
import Threads.globalLightData;
import Threads.globalUltrasonicData;
import Threads.lightThread;
import Threads.sonicThread;
import Threads.touchThread;
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
public class Wrestler {
//	static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S2);
	static NXTUltrasonicSensor sideSonicL = new NXTUltrasonicSensor(SensorPort.S2);
	static NXTUltrasonicSensor sideSonicR = new NXTUltrasonicSensor(SensorPort.S3);
	static NXTColorSensor light = new NXTColorSensor(SensorPort.S4);
	
	//Data
	// static NXTSoundSensor sound = new NXTSoundSensor(SensorPort.S4);
//	static DifferentialPilot pilot = new DifferentialPilot(5.6, 17.00, Motor.A, Motor.B);
	
//	static globalData touchedData = new globalData();
	static globalUltrasonicData sonicDataL = new globalUltrasonicData();
	static globalUltrasonicData sonicDataR = new globalUltrasonicData();
	static globalLightData lightData = new globalLightData();
	
	//
	static String leftString = "Turn left";
	static String rightString = "Turn right";
//	static TachoMotorPort left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // right
//	static TachoMotorPort right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // left

	static Brick brick = BrickFinder.getDefault();
	static NXTRegulatedMotor lm = new NXTRegulatedMotor(brick.getPort("A"));
	static NXTRegulatedMotor rm = new NXTRegulatedMotor(brick.getPort("B"));;
	
	static final double robotLength = 15.0;
	static final double wheelDiameter = 5.6;
	static final double wheelCircumference = wheelDiameter * Math.PI;
	static final double robotTrack = 14.5;
	static final double blackWhiteThreshold = 30;
	
	
	//WrestlerSpecific
	static final double turnConstFast = 0.82;
	static final double turnConstSlow = 0.794;
	
	//Arena
	static final double arenaDiameter = 90.0;
	static final double effectiveRatio = 0.66;
	static final double effectiveDiameter = arenaDiameter * effectiveRatio;
	static final double effectiveRadius = effectiveDiameter / 2.0;
	
	//Setup
	static final double distToBlack = (1 - effectiveRatio) * arenaDiameter / 2;
	
	//Rotate
	static final int parts = 12;
	static final double insideAngle = (parts - 2) * 180 / parts;
	static final double longPart = Math.sin((360.0 / (2 * parts)) / 180 * Math.PI) * effectiveDiameter;
	
	//*****************
	static final double RATIO = longPart / robotTrack;		//------ long over total
	static final double fastToSlowRatio = (1-RATIO) / RATIO;	//------ short over long
	
	static final int rotateFast = 200;
	static final int rotateSlow = (int) (rotateFast * fastToSlowRatio);			
	
	static final int rotateAccF = 2880;
	static final int rotateAccS = 2880;
	
	static final double arcLength = (2 * Math.PI * longPart * insideAngle) / 360;
	static final double arcLong = arcLength;
	static final double arcShort = arcLength * fastToSlowRatio;
	
	static final int rotateFar 		= (int) ((arcLong / wheelCircumference) * 360
														* turnConstFast);
	static final int rotateClose 	= (int) ((arcShort / wheelCircumference) * 360
														* turnConstSlow);
	
	
	public static void main(String[] aArg) throws Exception {
//		Thread t = new Thread(new touchThread(touchedData, ts));
//		t.setDaemon(true);
//		t.start();

		Thread uL = new Thread(new sonicThread(sonicDataL, sideSonicL));
		uL.setDaemon(true);
		uL.start();
		
		Thread uR = new Thread(new sonicThread(sonicDataR, sideSonicR));
		uR.setDaemon(true);
		uR.start();
		
		Thread l = new Thread(new lightThread(lightData, light));
		l.setDaemon(true);
		l.start();

		// establish a fail-safe: pressing Escape quits
		Brick brick = BrickFinder.getDefault(); // get specifics about this
			// robot
			brick.getKey("Escape").addKeyListener(new KeyListener() {
				public void keyPressed(Key k) {
					//	pilot.stop();
					lm.stop(true); rm.stop();
//					sideSonic.close();
//					ts.close();
					System.exit(1);
				}

				public void keyReleased(Key k) {
					lm.stop(true); rm.stop();
//					sideSonic.close();
//					ts.close();
					System.exit(1);
				}
			});	
		
		LCD.drawString("I'm ready", 0, 1);
		Button.waitForAnyPress();
		
//		touchedData.resetTouched();
//		sonicData.setSonicValue((int) desiredDist);
		
		
		Behavior b0 = new Rotate();
		Behavior b1 = new push();
		Behavior b2 = new Setup();
		
		Behavior[] bArray = {b0, b1, b2};
		Arbitrator arby = new Arbitrator(bArray);
		arby.start();
	}

}