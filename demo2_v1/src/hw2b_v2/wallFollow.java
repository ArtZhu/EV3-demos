package hw2b_v2;

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
		

			int sideDist = part3b_v2.sonicData.getSonicValue();
			
			if(part3b_v2.isSonicData1){
				part3b_v2.desiredDist = (sideDist *2 + part3b_v2.desiredDist) / 3;
				part3b_v2.isSonicData1 = false;
			}
	
				
			// set various error values
			part3b_v2.error = part3b_v2.desiredDist - sideDist;
			part3b_v2.accumError += part3b_v2.error;
			part3b_v2.errorDiff = part3b_v2.error - part3b_v2.lastError;
			part3b_v2.lastError = part3b_v2.error;
	
			// set PID values
			//
			double P = part3b_v2.Kp * Math.abs(part3b_v2.error);
			double I = part3b_v2.Ki * part3b_v2.accumError;
			double D = part3b_v2.Kd * part3b_v2.errorDiff;
	
			double turn = P + I + D;
			LCD.drawInt((int) (turn * 100000), 0, 4);
			int upPower = (int) (part3b_v2.Tp + turn);
			int downPower = (int) (part3b_v2.Tp - turn);
				if (part3b_v2.error > 0) {
					// too close, turn right
					LCD.drawString(part3b_v2.rightString, 0, 1);
					part3b_v2.left.controlMotor((int) (upPower*(1 + part3b_v2.aTurn)), 1);
					part3b_v2.right.controlMotor((int) (downPower * ( 1- part3b_v2.aTurn)), 1);
				} else {
					// too far, turn left
					LCD.drawString(part3b_v2.leftString, 0, 1);
			
					part3b_v2.left.controlMotor(downPower, 1);
					part3b_v2.right.controlMotor(upPower, 1);
				}
				
		Delay.msDelay(50);
	
	}

	public void suppress() {
		suppressed = true;
		
	}

}
