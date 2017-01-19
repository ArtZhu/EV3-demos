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
//correctversion
public class frontHuggerPID {
	static NXTRegulatedMotor lm;
	static NXTRegulatedMotor rm;
	static Brick brick = BrickFinder.getDefault(); // get specifics about this

	public static void main(String[] args) {
		// initialize sensor
		NXTUltrasonicSensor us = new NXTUltrasonicSensor(SensorPort.S3);
		TachoMotorPort left = (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // right
		TachoMotorPort right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // left

		// get an instance of this sensor in measurement mode
		SampleProvider dist = us.getDistanceMode();
		// Initialize an array of floats for fetching samples
		float[] sample = new float[dist.sampleSize()];

		// move modes
		final int forward = 1;
		final int backward = 2;
		// final int stop = 3;
		final int flt = 4;

		// configure values
		final int desiredDistance = 15;
		// final int noWall = 250;

		// initialize PID constants
		final double Kp = .9;
		final double Ki = 0.00001;
		final double Kd = 0.45;

		// unaltered turn power
		final int Tp = 70;

		double error;
		double accumError = 0;
		double lastError = 0;
		double errorDiff;

		int distance;

		// before wall follower starts
		LCD.drawString("Press LEFT", 0, 2);
		LCD.drawString("to start", 0, 3);
		LCD.drawString("distance =", 0, 0);
		while (!Button.LEFT.isDown()) {
			dist.fetchSample(sample, 0);
			distance = Math.min((int) (sample[0] * 100), 100);
			LCD.drawInt(distance, 3, 9, 0);

		}

		// wall follower
		LCD.drawString("Press ESCAPE", 0, 2);
		LCD.drawString("to stop", 0, 3);
		while (!Button.ESCAPE.isDown()) {

			dist.fetchSample(sample, 0);
			distance = Math.min((int) (sample[0] * 100), 100);

			// uncomment so that he stops at the end of the wall instead of
			// attempting to follow it's turn

			// if (currentDistance >= noWall){
			// MotorPort.A.controlMotor(0, stop);
			// MotorPort.C.controlMotor(0, stop);
			//
			// }
			//
			// else{

			// set various error values
			error = distance - desiredDistance;
			accumError += error;
			errorDiff = error - lastError;
			lastError = error;

			// set PID values
			double P = Kp * error;
			double I = Ki * accumError;
			double D = Kd * errorDiff;

			double turn = P + I + D;

			int upPower = (int) ((Tp + turn)/2);
			int downPower = (int) ((Tp - turn)/2);

			// turn the Motors
			if (error > 0) {
				left.controlMotor((int) (downPower * 0.5 + downPower * error / 10), forward);
				right.controlMotor((int) (downPower * 0.5 + downPower * error / 10), forward);
			} else {
				// turning away from the wall works less successfully since
				// sometimes the wall is too close to register
				// potential solution: making the desired distance further from
				// the wall to avoid the risk
				right.controlMotor((int) (downPower * 0.5 + downPower * -1 * error / 10), backward);
				left.controlMotor((int) (downPower * 0.5 + downPower * -1 * error / 10), backward);
			}

			// }

		}

		// Stop car gently with free wheel drive
		left.controlMotor(0, flt);
		right.controlMotor(0, flt);
		left.close();
		right.close();
		us.close();
		LCD.clear();
		LCD.drawString("Program stopped", 0, 0);
		Delay.msDelay(1000);

	}
}