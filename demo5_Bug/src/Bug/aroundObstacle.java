package Bug;


import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;
import ProfTests.*;

public class aroundObstacle implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = true;
	
	static boolean checkProximity = false;  //controls the robot not to think he passed obstacle 
										//					right after he encountered it
	
	public boolean takeControl() {
		//this behavior overrides "go" when we are in the behavior group of AVOIDOBSTACLE
		return waveFrontBug.AVOIDOBSTACLE;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(waveFrontBug.Debug) {
	//		Sound.systemSound(true, 2);
			LCD.clear();
			LCD.drawString("aroundObstacle", 0, 1);
		}
		
		//control pilot
		
		waveFrontBug.pilot.setLinearSpeed(waveFrontBug.stdSpeed);
		if(!waveFrontBug.pilot.isMoving())	waveFrontBug.pilot.steer(0);

		//only when we already left the proximity range that we set flag to true
		if(!checkProximity){
			if(!waveFrontBug.backOnTrack()){
				Delay.msDelay(50);
				checkProximity = true;
			}
		}
	
		//if we are backOnTrack then it is equivalent to we are starting from the beginning
		//													with a pose not (0, 0, 0) though
		if(checkProximity){
			if(waveFrontBug.backOnTrack())	{
				//then check if our current pose is actually closer to destination 
				//						than the point where we encounter the obstacle
/*				Pose curr = waveFrontBug.poseProvider.getPose();
				Point currPoint = new Point(curr.getX(), curr.getY());
				Point encounterPoint = new Point(waveFrontBug.currObsEncounterPose.getX(), waveFrontBug.currObsEncounterPose.getY());
				if(sound) Sound.systemSound(true, 4);
				if(waveFrontBug.destination.distanceTo(currPoint) < waveFrontBug.destination.distanceTo(encounterPoint)){
*/					waveFrontBug.pilot.stop();
					waveFrontBug.resetALLFLAGS();
//				}
				
				//allows us to continue the AVOIDOBSTACLE behavior group
//				else checkProximity = false; 
			}
		}
		
		Pose p = waveFrontBug.poseProvider.getPose();
/*		if(Math.abs(p.getX() - waveFrontBug.currObsEncounterPose.getX()) + Math.abs(p.getY() - waveFrontBug.currObsEncounterPose.getY()) < waveFrontBug.proximity){
			waveFrontBug.warning();
		}
*/		Delay.msDelay(10);
	}

	public void suppress() {
		suppressed = true;	
	}
}
