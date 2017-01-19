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
		return waveFrontBug2_2.AVOIDOBSTACLE;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		suppressed = false;
		if(waveFrontBug2_2.Debug) {
			if(sound) Sound.systemSound(true, 2);
			LCD.clear();
			LCD.drawString("aroundObstacle", 0, 1);
		}

		
	}

	public void suppress() {
		suppressed = true;	
	}
}
