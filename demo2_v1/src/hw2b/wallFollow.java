package hw2b;

import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class wallFollow implements Behavior{
	private boolean suppressed = false;
	
	public boolean takeControl() {
		return true;
	}

	public void action() {
		suppressed = false;
		
		LCD.drawString("b1",  0,  3);
		

			int sideDist = part3b.sonicData.getSonicValue();
			
			if(part3b.isSonicData1){
				part3b.desiredDist = (sideDist *2 + part3b.desiredDist) / 3;
				part3b.isSonicData1 = false;
			}
	
				
			// set various error values
			part3b.error = part3b.desiredDist - sideDist;
			part3b.accumError += part3b.error;
			part3b.errorDiff = part3b.error - part3b.lastError;
			part3b.lastError = part3b.error;
	
			// set PID values
			//
			double P = part3b.Kp * Math.abs(part3b.error);
			double I = part3b.Ki * part3b.accumError;
			double D = part3b.Kd * part3b.errorDiff;
	
			double turn = P + I + D;
			LCD.drawInt((int) (turn * 100000), 0, 4);
			int upPower = (int) (part3b.Tp + turn);
			int downPower = (int) (part3b.Tp - turn);
				if (part3b.error > 0) {
					// too close, turn right
					LCD.drawString(part3b.rightString, 0, 1);
					part3b.left.controlMotor((int) (upPower*(1 + part3b.aTurn)), 1);
					part3b.right.controlMotor((int) (downPower * ( 1- part3b.aTurn)), 1);
						
			
					//MotorPort.B.controlMotor(0, stop);
					//MotorPort.A.controlMotor(power, forward);
				} else {
					// too far, turn left
					LCD.drawString(part3b.leftString, 0, 1);
			
					part3b.left.controlMotor(downPower, 1);
					part3b.right.controlMotor(upPower, 1);
					//MotorPort.B.controlMotor(power, forward);
					//MotorPort.A.controlMotor(0, stop);
				}
				
		
	/*
				String s;
				if(part3b.flag){s = "true";}
				else{s = "false";}
				LCD.drawString(s, 0, 2);
	*/
		Delay.msDelay(50);
	
	}

	public void suppress() {
		suppressed = true;
		
	}

}
