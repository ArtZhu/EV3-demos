package hw2;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
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
public class part4 {
	static NXTColorSensor light = new NXTColorSensor(SensorPort.S3);
	static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S2);
//	static NXTUltrasonicSensor frontSonic = new NXTUltrasonicSensor(SensorPort.S2);
	//static NXTLightSensor light = new NXTLightSensor(SensorPort.S3);
	
	//
	//Data
//	protected static SampleProvider frontDist = frontSonic.getDistanceMode();
//	protected static float[] frontSample = new float[frontDist.sampleSize()];
	protected static globalData touchedData = new globalData();
	
	//
	static String leftString = "Turn left ";
	static String rightString = "Turn right";
	static int white,black,blackWhiteThreshold;
	static TachoMotorPort left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // right
	static TachoMotorPort right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // left
	static SampleProvider lightDist = light.getRGBMode();  //color light with a source
	
	static final int forward = 1;
	static final int stop = 3;
	static final int flt = 4;
	
	static final double robotLength = 24.0;
	static final double wheelDiameter = 5.6;
	static final double wheelCircumference = wheelDiameter * Math.PI;
	static final double robotTrack = 14.5;
	static final double turnConst = 1.0;
	
	// initialize PID constants
	static final double Kp = 0.9;
	static final double Ki = 0.00001;
	static final double Kd = 0.45;

	static final double aTurn = 0.7;
	
	static final int TpUp = 40;
	static final int TpDown = 30;
	static int Tp = TpUp;
	
	//PID
	static double accumError = 0;
	//direction
	static int dir = 0;
	
	public static void main(String[] aArg) throws Exception {
		//touchSensor		
		Thread t = new Thread(new touchThread(touchedData, ts)); // create touch
					// thread
		t.setDaemon(true); // when the main thread terminates, the created
		// thread terminates
		// if this is false, the created thread continues
		// when the main thread terminates
		t.start();

		
		/*	
		Calibrate c = new Calibrate(lightDist);
		//calibrate white
		white = c.calWhite();
		Delay.msDelay(1000);
		//calibrate black
		black = c.calBlack();
		Delay.msDelay(1000);
		//find average
		blackWhiteThreshold = c.getAvgEx();
*/
		black = 21;
		white = 55;
		blackWhiteThreshold = 40;
		LCD.clear();
		LCD.drawString("white: " + white,0,1);
		LCD.drawString("black: " + black,0,2);
		LCD.drawString("threshold: " + blackWhiteThreshold,0,3);
		Button.waitForAnyPress();
		

		// Follow line until ESCAPE is pressed
		LCD.drawString("Press ESCAPE", 0, 4);
		LCD.drawString("to stop ", 0, 5);
		Delay.msDelay(1000);
		// get an instance of this sensor in measurement mode
		//SampleProvider lightDist = light.getAmbientMode();  //light without a source
		// Initialize an array of floats for fetching samples
		
		while(true){
			direction();
			LCD.drawInt(Tp, 0,  6);
		}
	}

	
	public static void direction(){
		float[] lightSample = new float[lightDist.sampleSize()];

		// unaltered turn power

		//.6; 0.15; 40;
		//.9?
		//
		double error;
		double accumError = 0;
		double lastError = 0;
		double errorDiff;
				
		int i = 0;
				
		while (!Button.ESCAPE.isDown()) {
			if(i == 10){
				i = 0;
//				frontDist.fetchSample(frontSample, 0);
				
				if(touchedData.getTouched()){
					turnAround();
				}	
				touchedData.resetTouched();
			}
			i++;
			
			lightDist.fetchSample(lightSample, 0);
			
			// set various error values
			error = blackWhiteThreshold - lightSample[0] * 100;
			accumError += error;
			errorDiff = error - lastError;
			lastError = error;

			// set PID values
			//
			double P = Kp * Math.abs(error);
			double I = Ki * accumError;
			double D = Kd * errorDiff;

			double turn = P + I + D;
			LCD.drawInt((int) (turn * 100000), 0, 4);
			int upPower = (int) (Tp + turn);
			int downPower = (int) (Tp - turn);
			if(dir == 1){
				if (error < 0) {
					// On white, turn right
					LCD.drawString(rightString, 0, 1);
					left.controlMotor(upPower, forward);
					right.controlMotor(downPower, forward);
		
					//MotorPort.B.controlMotor(0, stop);
					//MotorPort.A.controlMotor(power, forward);
				} else {
					// On black, turn left
					LCD.drawString(leftString, 0, 1);
					double up1 = upPower+ aTurn * turn;
					double down1 = downPower - aTurn * turn;
					if(up1 > 100){
						up1 = 95;
					}
					left.controlMotor((int) down1, forward);
					right.controlMotor((int) up1, forward);
	
					//MotorPort.B.controlMotor(power, forward);
					//MotorPort.A.controlMotor(0, stop);
				}
			}
			else if(dir == 0){
				if (error > 0) {
					// On black, turn right
					LCD.drawString(rightString, 0, 1);
					double up1 = upPower+ aTurn * turn;
					double down1 = downPower - aTurn * turn;
					if(up1 > 100){
						up1 = 95;
					}
					left.controlMotor((int) up1, forward);
					right.controlMotor((int) down1, forward);
		
					//MotorPort.B.controlMotor(0, stop);
					//MotorPort.A.controlMotor(power, forward);
				} else {
					// On white, turn left
					LCD.drawString(leftString, 0, 1);
					left.controlMotor(downPower, forward);
					right.controlMotor(upPower, forward);
	
					//MotorPort.B.controlMotor(power, forward);
					//MotorPort.A.controlMotor(0, stop);
				}
			}
			
			Delay.msDelay(10);

		}
		
		//light.close();
		// Stop car gently with free wheel drive
		left.controlMotor(0, flt);
		right.controlMotor(0, flt);
		//MotorPort.B.controlMotor(0, flt);
		//MotorPort.A.controlMotor(0, flt);
		LCD.clear();
		LCD.drawString("Program stopped", 0, 0);
		Delay.msDelay(1000);
		left.close();
		right.close();
		
		
		System.exit(0);
	}

	
	public static void turnAround(){
		//update direction
		if(dir == 0){
			dir = 1;
			Tp = TpUp;
		}
		else if(dir == 1){
			dir = 0;
			Tp = TpDown;
		}
		
		left.controlMotor(50, stop);
		right.controlMotor(50, stop);
		
		//action
		left.close();
		right.close();
		
		Brick brick = BrickFinder.getDefault(); 
		NXTRegulatedMotor lm = new NXTRegulatedMotor(brick.getPort("A"));			
		NXTRegulatedMotor rm = new NXTRegulatedMotor(brick.getPort("B"));

		//GetBack!
/*		lm.synchronizeWith(new RegulatedMotor[] {rm});
   		lm.startSynchronization();
*/   		
		lm.setAcceleration(360);
		rm.setAcceleration(360);
		lm.setSpeed(180);
		rm.setSpeed(180);
		
		
		lm.rotate((int) (-1 * robotLength/ wheelCircumference * 360 ), true);
		rm.rotate((int) (-1 * robotLength/ wheelCircumference * 360 ));
		lm.waitComplete();
		rm.waitComplete();
//		lm.endSynchronization();

		lm.stop(true);
		rm.stop();
		lm.waitComplete();
		rm.waitComplete();
		
		turn(180, lm, rm);
		lm.waitComplete();
		rm.waitComplete();
		
		lm.close();
		rm.close();
		
		left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class);
		right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class);
		
		//reset I at last
		accumError = 0;
	}
	
	private static void turn(int degree, NXTRegulatedMotor lm, NXTRegulatedMotor rm){
    	lm.setAcceleration(360);
    	rm.setAcceleration(360);
    	//
    	double turnAngle = turnConst * (robotTrack * Math.PI * degree / 360) /(wheelDiameter * Math.PI) * 360;
		
    	lm.synchronizeWith(new RegulatedMotor[] {rm});
   		rm.startSynchronization();
    	lm.rotate((int) (1 * turnAngle), true);
    	rm.rotate((int) (-1 * turnAngle));	
    	lm.endSynchronization();
   	}
   	
}