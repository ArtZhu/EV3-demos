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
public class part1 {
	static NXTColorSensor light = new NXTColorSensor(SensorPort.S3);
	//static NXTLightSensor light = new NXTLightSensor(SensorPort.S3);
	
	public static void main(String[] aArg) throws Exception {
		String leftString = "Turn left ";
		String rightString = "Turn right";
		int white,black,blackWhiteThreshold;
		TachoMotorPort left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // right
		TachoMotorPort right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // left

		final int forward = 1;
		final int stop = 3;
		final int flt = 4;
		final int power = 70;
		
		// Use the light sensor as a reflection sensor
		SampleProvider lightDist = light.getRGBMode();  //color light with a source
		//SampleProvider lightDist = light.getRedMode();  //light with a source
		/*****
		 *  ambient mode just taking in samples of the ambient mode
		 */
		
		//now calibrate light/color sensor
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
		blackWhiteThreshold = 30;
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
		float[] lightSample = new float[lightDist.sampleSize()];

		LCD.clear();
		
		//
		// initialize PID constants
		final double Kp = .825;
		//added 0 number of 0;
		final double Ki = 0.00001;
		final double Kd = 0.35;

		// unaltered turn power
		final int Tp = 60;

		//.6; 0.15; 40;
		//.9?
		//
		double error;
		double accumError = 0;
		double lastError = 0;
		double errorDiff;
				
				
		while (!Button.ESCAPE.isDown()) {

			
			
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
			int upPower = (int) (Tp + turn);
			int downPower = (int) (Tp - turn);
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
				left.controlMotor(downPower, forward);
				right.controlMotor(upPower, forward);

				//MotorPort.B.controlMotor(power, forward);
				//MotorPort.A.controlMotor(0, stop);
			}
			//LCD.drawInt(light.readValue(), 3, 9, 0);
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
	}
}