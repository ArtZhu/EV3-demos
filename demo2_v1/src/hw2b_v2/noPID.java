package hw2b_v2;

import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class noPID implements Behavior{
	private boolean suppressed = false;
	
	public boolean takeControl() {
		return true;
	}

	public void action() {
		suppressed = false;
		
		LCD.drawString("noPID",  0,  3);
		

		part3b_v2.left.controlMotor(25,1 );
		part3b_v2.right.controlMotor(25, 1);
		Delay.msDelay(50);
	
	}

	public void suppress() {
		suppressed = true;
		
	}

}
