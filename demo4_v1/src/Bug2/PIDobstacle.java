package Bug2;

import lejos.hardware.lcd.LCD;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class PIDobstacle implements Behavior{
	
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	
	static boolean checkProximity = false;  //controls the robot not to think he passed obstacle 
	//					right after he encountered it

	public boolean takeControl() {
		//this behavior overrides "go" when we are in the behavior group of AVOIDOBSTACLE
		return Bug2.AVOIDOBSTACLE;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		int sideDist = Bug2.sideSonicData.getSonicValue();
		
		if(Bug2.isSonicData1){
			Bug2.desiredDist = (sideDist *2 + Bug2.desiredDist) / 3;
			Bug2.isSonicData1 = false;
		}

		// set various error values
		Bug2.error = Bug2.desiredDist - sideDist;
		Bug2.accumError += Bug2.error;
		Bug2.errorDiff = Bug2.error - Bug2.lastError;
		Bug2.lastError = Bug2.error;

		// set PID values
		double P = Bug2.Kp * Math.abs(Bug2.error);
		double I = Bug2.Ki * Bug2.accumError;
		double D = Bug2.Kd * Bug2.errorDiff;

		double angle = (P + I + D) * 1;
		LCD.drawInt((int) (angle * 100000), 0, 4);
		
		//control pilot
		Bug2.pilot.setTravelSpeed(Bug2.stdSpeed);
		Bug2.pilot.steer(100, angle);
		
		if(Bug2.Debug){
			// too close, turn left
			if (Bug2.error > 0) LCD.drawString(Bug2.leftString, 0, 1);
			// too far, turn right
			else				LCD.drawString(Bug2.rightString, 0, 1);
		}
		
		//only when we already left the proximity range that we set flag to true
		if(!checkProximity){
			if(!Bug2.backOnTrack())	{
				Delay.msDelay(50);
				checkProximity = true;
			}
		}
				
		//if we are backOnTrack then it is equivalent to we are starting from the beginning
		//													with a pose not (0, 0, 0) though
		if(checkProximity){
			if(Bug2.backOnTrack())	{
				//then check if our current pose is actually closer to destination 
				//						than the point where we encounter the obstacle
				Pose curr = Bug2.poseProvider.getPose();
				Point currPoint = new Point(curr.getX(), curr.getY());
				Point encounterPoint = new Point(Bug2.currObsEncounterPose.getX(), Bug2.currObsEncounterPose.getY());
				if(Bug2.destination.distanceTo(currPoint) < Bug2.destination.distanceTo(encounterPoint)){
					Bug2.pilot.stop();
					Bug2.resetALLFLAGS();
				}
						
				//allows us to continue the AVOIDOBSTACLE behavior group
				else checkProximity = false; 
			}
		}
		
		Delay.msDelay(50);
	}

	public void suppress() {
		suppressed = true;	
	}

}
