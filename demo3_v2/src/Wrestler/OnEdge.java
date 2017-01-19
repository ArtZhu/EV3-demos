package Wrestler;

import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class OnEdge implements Behavior{
	private boolean suppressed = false;
	
	public boolean takeControl() {
		return true;
	}

	public void action() {
		suppressed = false;
		
		// set various error values
		int light_val = Wrestler.lightData.getLightValue();
		Wrestler.error = Wrestler.blackWhiteThreshold - light_val;
		Wrestler.accumError += Wrestler.error;
		Wrestler.errorDiff = Wrestler.error - Wrestler.lastError;
		Wrestler.lastError = Wrestler.error;

		// set PID values
		//
		double P = Wrestler.Kp * Math.abs(Wrestler.error);
		double I = Wrestler.Ki * Wrestler.accumError;
		double D = Wrestler.Kd * Wrestler.errorDiff;

		double turn = P + I + D;
		LCD.drawInt((int) (light_val), 0, 4);
		int upPower = (int) (Wrestler.Tp + turn);
		int downPower = (int) (Wrestler.Tp - turn);
			if (Wrestler.error < 0) {
				// On white, turn right
				LCD.drawString(Wrestler.rightString, 0, 1);
				Wrestler.left.controlMotor(upPower, 1);
				Wrestler.right.controlMotor(downPower, 1);
			} else {
				// On black, turn left
				LCD.drawString(Wrestler.leftString, 0, 1);
				double up1 = upPower+ Wrestler.aTurn * turn;
				double down1 = downPower - Wrestler.aTurn * turn;
				if(up1 > 100){
					up1 = 95;
				}
				Wrestler.left.controlMotor((int) down1, 1);
				Wrestler.right.controlMotor((int) up1, 1);
			}
		
		Delay.msDelay(10);
	}

	public void suppress() {
		suppressed = true;
		
	}

}
