package Bug2;

import ProfTests.DifferentialPilot;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;

public class frontEncounter  implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = false;
	
	public boolean takeControl() {
		int frontDist = Bug2.frontSonicData.getSonicValue();
		if(Bug2.Debug)
			if(frontDist < 15) {
				//Sound.twoBeeps();
			}
		return frontDist<20;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug2.Debug) 	{
			LCD.clear();
			LCD.drawString("frontEncounter", 0, 1);
			if(sound) Sound.systemSound(true, 3);
		}
		 
		//for the first time we encounter an obstacle, we are not yet in the behavior group AVOIDOBSTACLE,
		//				then we are to record the pose we encountered.
		//				This pose will be used later to make sure that 
		//					we leave the proximity of our optimal path before we started to detect if we are back in proximity
		if(!Bug2.AVOIDOBSTACLE) {
			Bug2.nav.stop();
			Bug2.currObsEncounterPose = Bug2.poseProvider.getPose();
		}
		
		//Rotate to counterclockwise to avoid obstacle
		float angle;
		//firstTime(not yet in sequence) different angle
		if(!Bug2.AVOIDOBSTACLE) 
			angle = Bug2.currObsEncounterPose.getHeading() + 90;	//clockwise heading = positive?
		else angle = 90;
		((DifferentialPilot) Bug2.nav.getMoveController()).setAngularSpeed(Bug2.stdRotateSpeed);
		((DifferentialPilot) Bug2.nav.getMoveController()).rotate(angle * Bug2.turnConst);
		
		if(sound) Sound.beep();
	
		//reset PID
		Bug2.isSonicData1 = true;
		Bug2.resetPID();
		
		//Last, we encountered something;
		//				we are to AVOIDOBSTACLE
		Bug2.AVOIDOBSTACLE = true;
	}

	public void suppress() {
		suppressed = true;	
	}

}