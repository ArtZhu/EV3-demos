package Wrestler;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class Setup1 implements Behavior{
	private boolean suppressed = false;
	private boolean flag = true;
	
	public boolean takeControl() {
		return flag;
	}

	public void action() {

		Wrestler1.lm.setSpeed(200);
		Wrestler1.rm.setSpeed(200);
		Wrestler1.lm.setAcceleration(400);
		Wrestler1.rm.setAcceleration(400);
		Wrestler1.lm.backward();
		Wrestler1.rm.backward();
		
		while(Wrestler1.lightData.getLightValue() > Wrestler1.blackWhiteThreshold){
			Delay.msDelay(50);
		}
		Wrestler1.lm.setSpeed(480);
		Wrestler1.rm.setSpeed(480);
		int angle = (int) (((Wrestler1.distToBlack - Wrestler1.robotLength) / Wrestler1.wheelCircumference) * 360);
		System.out.print("angle = ");
		System.out.print(angle);
		Wrestler1.lm.rotate(angle, true);
		Wrestler1.rm.rotate(angle);
		Wrestler1.lm.waitComplete();
		Wrestler1.rm.waitComplete();
		
		flag = false;
	}

	public void suppress() {
		suppressed = true;	
	}

}
