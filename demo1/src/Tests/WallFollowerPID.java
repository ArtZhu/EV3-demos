package Tests;

//implements behavior;
//action
//suppress
//takeControl()
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.hardware.sensor.NXTUltrasonicSensor;

public class WallFollowerPID {

	static NXTRegulatedMotor lm;
	static NXTRegulatedMotor rm;
	static Brick brick = BrickFinder.getDefault();
	final static int forward = 1;
	
	public static void main(String[] args) {
		NXTUltrasonicSensor us = new NXTUltrasonicSensor(SensorPort.S2);
		TachoMotorPort left = MotorPort.A.open(TachoMotorPort.class);
		TachoMotorPort right = MotorPort.B.open(TachoMotorPort.class);
				
		while(true){		
			//

			double desiredDistance = 30;
			double kp, ki, kd;
			double distance;
			
			double Tp = 70;
			
			
			double error;
			double accumError = 0;
			double lastError = 0;
			double errorDiff;
			
	
			
			error = distance - desiredDistance;
			accumError += error;
			errorDiff = error - lastError;
			lastError = error;
			
			double P = kp * error;
			double I = ki * accumError;
			double D = kd * errorDiff;
			
			double turn = P + I + D;
			
			int upPower = (int) (Tp + turn);
			int downPower = (int) (Tp - turn);
			
			if ( error > 0) {
				left.controlMotor(downPower,  forward);
				right.controlMotor(downPower, forward);
			}
			else {
				//opposite
			}
		}
		
	}
}
