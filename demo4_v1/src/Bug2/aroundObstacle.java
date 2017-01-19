package Bug2;


import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class aroundObstacle implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = true;
	
	static boolean checkProximity = false;  //controls the robot not to think he passed obstacle 
										//					right after he encountered it
	
	public boolean takeControl() {
		//this behavior overrides "go" when we are in the behavior group of AVOIDOBSTACLE
		return Bug2.AVOIDOBSTACLE;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug2.Debug) {
			LCD.clear();
			LCD.drawString("aroundObstacle", 0, 1);
		}
		
		//control pilot
		
		Bug2.pilot.setLinearSpeed(Bug2.stdSpeed);
		if(!Bug2.pilot.isMoving())	Bug2.pilot.steer(0);

//		Bug2.pilot.forward();
		
	//	if(sound) Sound.twoBeeps(); 
		//only when we already left the proximity range that we set flag to true
		if(!checkProximity){
			if(!backOnTrack()){
				Delay.msDelay(50);
				checkProximity = true;
			}
		}
	
		//if we are backOnTrack then it is equivalent to we are starting from the beginning
		//													with a pose not (0, 0, 0) though
		if(checkProximity){
			if(backOnTrack())	{
				//then check if our current pose is actually closer to destination 
				//						than the point where we encounter the obstacle
				Pose curr = Bug2.poseProvider.getPose();
				Point currPoint = new Point(curr.getX(), curr.getY());
				Point encounterPoint = new Point(Bug2.currObsEncounterPose.getX(), Bug2.currObsEncounterPose.getY());
				if(sound) Sound.systemSound(true, 4);
				if(Bug2.destination.distanceTo(currPoint) < Bug2.destination.distanceTo(encounterPoint)){
					Bug2.pilot.stop();
					Bug2.resetALLFLAGS();
				}
				
				//allows us to continue the AVOIDOBSTACLE behavior group
				else checkProximity = false; 
			}
		}
		
		float[] points = new float[Bug2.poseProvider.sampleSize()];
		Bug2.poseProvider.fetchSample(points, 0);
//		System.out.printf("pose: %.1f, %.1f, %.1f\n",
//				points[0], points[1], points[2]);
		
		Delay.msDelay(10);
	}

	public void suppress() {
		suppressed = true;	
	}

	public boolean backOnTrack(){return (Math.abs((Bug2._M * Bug2.poseProvider.getPose().getX()) - Bug2.poseProvider.getPose().getY()) < Bug2.proximity);}
}
