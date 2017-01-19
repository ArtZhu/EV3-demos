package Wrestler;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class Setup implements Behavior{
	private boolean suppressed = false;
	private boolean flag = true;
	
	public boolean takeControl() {
		return flag;
	}

	public void action() {

		Wrestler.lm.setSpeed(200);
		Wrestler.rm.setSpeed(200);
		Wrestler.lm.setAcceleration(400);
		Wrestler.rm.setAcceleration(400);
		Wrestler.lm.backward();
		Wrestler.rm.backward();
		
		while(Wrestler.lightData.getLightValue() > Wrestler.blackWhiteThreshold){
			Delay.msDelay(50);
		}
		Wrestler.lm.setSpeed(480);
		Wrestler.rm.setSpeed(480);
		int angle = (int) (((Wrestler.distToBlack - Wrestler.robotLength) / Wrestler.wheelCircumference) * 360);
		System.out.print("angle = ");
		System.out.print(angle);
		Wrestler.lm.rotate(angle, true);
		Wrestler.rm.rotate(angle);
		Wrestler.lm.waitComplete();
		Wrestler.rm.waitComplete();
		
		flag = false;
	}

	public void suppress() {
		suppressed = true;	
	}

}
