package Bug3;

import ProfTests.DifferentialPilot;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;

public class frontEncounter  implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = false;
	
	public boolean takeControl() {
		int frontDist = Bug3.frontSonicData.getSonicValue();
		int eopdDist = Bug3.eopdData.getEOPDValue();
		boolean b = frontDist < Bug3.antennaShort || eopdDist < 95;
		if(Bug3.Debug)
			if(b) {
				if(sound) Sound.twoBeeps();
			}
		return b;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug3.Debug) 	{
			LCD.clear();
			LCD.drawString("frontEncounter", 0, 1);
			if(sound) Sound.systemSound(true, 3);
		}
		
		Bug3.nav.stop();
		
		//firstTime(not yet in sequence) different angle
		float angle = Bug3.poseProvider.getPose().getHeading() + 90;	//CONVEX RECTANGLE OBSTACLES
		((DifferentialPilot) Bug3.nav.getMoveController()).setAngularSpeed(Bug3.stdRotateSpeed);
		((DifferentialPilot) Bug3.nav.getMoveController()).rotate(angle * Bug3.turnConst);
		
		if(sound) Sound.beep();
	
		//reset PID
		Bug3.resetPID();
		
		//Last, we encountered something;
		//				we are to AVOIDOBSTACLE
		Bug3.AVOIDOBSTACLE = true;
		
		((DifferentialPilot) Bug3.nav.getMoveController()).stop();
		
		LCD.drawInt(((int)Bug3.poseProvider.getPose().getX() * 10000), 0, 5);
		LCD.drawInt(((int)Bug3.poseProvider.getPose().getY() * 10000), 0, 6);
	}

	public void suppress() {
		suppressed = true;	
	}

}