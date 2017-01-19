package Bug3v2;

import ProfTests.DifferentialPilot;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;

public class frontEncounter  implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = false;
	
	public boolean takeControl() {
		int frontDist = Bug3v2.frontSonicData.getSonicValue();
		boolean b = frontDist < Bug3v2.antennaShort;
		if(Bug3v2.Debug)
			if(b) {
				if(sound) Sound.twoBeeps();
			}
		return b;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug3v2.Debug) 	{
			LCD.clear();
			LCD.drawString("frontEncounter", 0, 1);
			if(sound) Sound.systemSound(true, 3);
		}
		
		Bug3v2.nav.stop();
		
		//firstTime(not yet in sequence) different angle
		float angle = Bug3v2.poseProvider.getPose().getHeading() + 90;	//CONVEX RECTANGLE OBSTACLES
//		float angle = 90;	//CONVEX RECTANGLE OBSTACLES
		((DifferentialPilot) Bug3v2.nav.getMoveController()).setAngularSpeed(Bug3v2.stdRotateSpeed);
		((DifferentialPilot) Bug3v2.nav.getMoveController()).rotate(angle * Bug3v2.turnConst);
		
		if(sound) Sound.beep();
	
		//reset PID
		Bug3v2.resetPID();
		
		//Last, we encountered something;
		//				we are to AVOIDOBSTACLE
		Bug3v2.AVOIDOBSTACLE = true;
		
		((DifferentialPilot) Bug3v2.nav.getMoveController()).stop();
	}

	public void suppress() {
		suppressed = true;	
	}

}