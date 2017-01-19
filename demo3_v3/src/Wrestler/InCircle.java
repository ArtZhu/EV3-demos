package Wrestler;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class InCircle implements Behavior{
	private boolean suppressed = false;
	
	public boolean takeControl() {
		if(Wrestler1.lightData.getLightValue() > 30){
			return true;
		}
		return false;
	}

	public void action() {
		suppressed = false;
		
		LCD.drawString("InCircle",  0,  3);
		
		Wrestler1.left.controlMotor(25, 1);
		Wrestler1.right.controlMotor(25, 1);
		Delay.msDelay(50);
	
	}

	public void suppress() {
		suppressed = true;
		
	}

}
