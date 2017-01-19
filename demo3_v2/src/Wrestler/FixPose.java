package Wrestler;

import lejos.robotics.subsumption.Behavior;

public class FixPose implements Behavior{
	private boolean suppressed = false;
	private boolean leftFast = true;
	
	public boolean takeControl() {
		return true;
	}

	public void action() {
		if(leftFast){
			Wrestler.lm.setSpeed(Wrestler.rotateFast);
			Wrestler.rm.setSpeed(Wrestler.rotateSlow);
			Wrestler.lm.setAcceleration(Wrestler.rotateAccF);
			Wrestler.rm.setAcceleration(Wrestler.rotateAccS);
			Wrestler.lm.rotateTo(Wrestler.rotateFar);				//ratio equal to ratio of rotateFast:rotateSlow
			Wrestler.rm.rotateTo(Wrestler.rotateClose);
			Wrestler.lm.waitComplete();
			Wrestler.rm.waitComplete();
			while(!suppressed && (Wrestler.lightData.getLightValue() < Wrestler.blackWhiteThreshold || 
								        Wrestler.bwData.getLightValue() < Wrestler.blackWhiteThreshold)){
				Thread.yield();
			}
		}
		else{
			Wrestler.lm.setSpeed(Wrestler.rotateSlow);
			Wrestler.rm.setSpeed(Wrestler.rotateFast);
			Wrestler.lm.setAcceleration(Wrestler.rotateAccS);
			Wrestler.rm.setAcceleration(Wrestler.rotateAccF);
			Wrestler.lm.rotateTo(Wrestler.rotateClose);				//ratio equal to ratio of rotateFast:rotateSlow
			Wrestler.rm.rotateTo(Wrestler.rotateFar);
			Wrestler.lm.waitComplete();
			Wrestler.rm.waitComplete();
			while(!suppressed && (Wrestler.lightData.getLightValue() < Wrestler.blackWhiteThreshold || 
			        					Wrestler.bwData.getLightValue() < Wrestler.blackWhiteThreshold)){
				Thread.yield();
			}
		}
		leftFast = (!leftFast);
	}

	public void suppress() {
		suppressed = true;	
	}

}
