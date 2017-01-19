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
		int light_val = Wrestler1.lightData.getLightValue();
		Wrestler1.error = Wrestler1.blackWhiteThreshold - light_val;
		Wrestler1.accumError += Wrestler1.error;
		Wrestler1.errorDiff = Wrestler1.error - Wrestler1.lastError;
		Wrestler1.lastError = Wrestler1.error;

		// set PID values
		//
		double P = Wrestler1.Kp * Math.abs(Wrestler1.error);
		double I = Wrestler1.Ki * Wrestler1.accumError;
		double D = Wrestler1.Kd * Wrestler1.errorDiff;

		double turn = P + I + D;
		LCD.drawInt((int) (light_val), 0, 4);
		int upPower = (int) (Wrestler1.Tp + turn);
		int downPower = (int) (Wrestler1.Tp - turn);
			if (Wrestler1.error < 0) {
				// On white, turn right
				LCD.drawString(Wrestler1.rightString, 0, 1);
				Wrestler1.left.controlMotor(upPower, 1);
				Wrestler1.right.controlMotor(downPower, 1);
			} else {
				// On black, turn left
				LCD.drawString(Wrestler1.leftString, 0, 1);
				double up1 = upPower+ Wrestler1.aTurn * turn;
				double down1 = downPower - Wrestler1.aTurn * turn;
				if(up1 > 100){
					up1 = 95;
				}
				Wrestler1.left.controlMotor((int) down1, 1);
				Wrestler1.right.controlMotor((int) up1, 1);
			}
		
		Delay.msDelay(10);
	}

	public void suppress() {
		suppressed = true;
		
	}

}
