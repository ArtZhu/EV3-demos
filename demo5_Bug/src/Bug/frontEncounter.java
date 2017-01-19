package Bug;

import ProfTests.DifferentialPilot;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;

public class frontEncounter  implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = false;
	
	public boolean takeControl() {
		return waveFrontBug.frontEncounter();
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(waveFrontBug.Debug) 	{
			Sound.systemSound(true, 4);
			LCD.clear();
			LCD.drawString("frontEncounter", 0, 1);
			if(sound) Sound.systemSound(true, 3);
		}
		 
		//for the first time we encounter an obstacle, we are not yet in the behavior group AVOIDOBSTACLE,
		//				then we are to record the pose we encountered.
		//				This pose will be used later to make sure that 
		//					we leave the proximity of our optimal path before we started to detect if we are back in proximity
		if(!waveFrontBug.AVOIDOBSTACLE) {
			waveFrontBug.nav.stop();
			waveFrontBug.currObsEncounterPose = waveFrontBug.poseProvider.getPose();
		}
		
		waveFrontBug.toCenter();
		((DifferentialPilot) waveFrontBug.nav.getMoveController()).setAngularSpeed(waveFrontBug.stdRotateSpeed);
		((DifferentialPilot) waveFrontBug.nav.getMoveController()).rotate(90 * waveFrontBug.turnConst);
		
		if(sound) Sound.beep();
	
		//Last, we encountered something;
		//				we are to AVOIDOBSTACLE
		waveFrontBug.AVOIDOBSTACLE = true;
	}

	public void suppress() {
		suppressed = true;	
	}

}