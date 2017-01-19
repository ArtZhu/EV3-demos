package Bug;


import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;
import ProfTests.*;

public class aroundObstacle implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = false;
	
	static boolean checkProximity = false;  //controls the robot not to think he passed obstacle 
										//					right after he encountered it
	
	public boolean takeControl() {
		//this behavior overrides "go" when we are in the behavior group of AVOIDOBSTACLE
		return waveFrontBug2.AVOIDOBSTACLE;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		suppressed = false;
		if(waveFrontBug2.Debug) {
			if(sound) Sound.systemSound(true, 2);
			LCD.clear();
			LCD.drawString("aroundObstacle", 0, 1);
		}

		//if we are back to where we started to avoid obstacle, then we are stuck
		Pose p = waveFrontBug2.poseProvider.getPose();
		Waypoint fake = new Waypoint(p.getX(), p.getY());
		
		
		//control pilot
		waveFrontBug2.set();
		if(!waveFrontBug2.pilot.isMoving())	waveFrontBug2.pilot.steer(0);

		//only when we already left the proximity range that we set flag to true
		if(!checkProximity){
			Waypoint[] track = waveFrontBug2.onTrack(fake);
			if(track == null){
				Delay.msDelay(50);
				checkProximity = true;
			}
		}
	
		if(checkProximity){
			Waypoint avoidSource = new Waypoint(waveFrontBug2.currObsEncounterPose.getX(), waveFrontBug2.currObsEncounterPose.getY());
/*			if(waveFrontBug2.closeTo(fake, avoidSource)){
				waveFrontBug2.warning();
			}
*/			Waypoint[] track = waveFrontBug2.onTrack(fake);
			if(track != null)	{
				while(waveFrontBug2.pathPoint1.poll() != track[0]){}
				waveFrontBug2.nav.stop();
				waveFrontBug2.AVOIDOBSTACLE = false;
				System.out.print("EXIT AVOID\n");
			}
		}
		

		Delay.msDelay(10);
	}

	public void suppress() {
		suppressed = true;	
	}
}
