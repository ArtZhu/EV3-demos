package Wrestler;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class toCenter implements Behavior{
	private boolean suppressed = false;
	private boolean flag = true;
	
	
	public boolean takeControl() {
		return flag;
	}

	public void action() {
		suppressed = false;
		
		LCD.drawString("InCircle",  0,  3);
		
		Wrestler.left.controlMotor(25, 1);
		Wrestler.right.controlMotor(25, 1);
		Delay.msDelay(50);
	
	}

	public void suppress() {
		suppressed = true;
		
	}

}
