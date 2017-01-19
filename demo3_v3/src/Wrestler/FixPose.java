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
			Wrestler1.lm.setSpeed(Wrestler1.rotateFast);
			Wrestler1.rm.setSpeed(Wrestler1.rotateSlow);
			Wrestler1.lm.setAcceleration(Wrestler1.rotateAccF);
			Wrestler1.rm.setAcceleration(Wrestler1.rotateAccS);
			Wrestler1.lm.rotateTo(Wrestler1.rotateFar);				//ratio equal to ratio of rotateFast:rotateSlow
			Wrestler1.rm.rotateTo(Wrestler1.rotateClose);
			Wrestler1.lm.waitComplete();
			Wrestler1.rm.waitComplete();
			while(!suppressed && (Wrestler1.lightData.getLightValue() < Wrestler1.blackWhiteThreshold || 
								        Wrestler1.bwData.getLightValue() < Wrestler1.blackWhiteThreshold)){
				Thread.yield();
			}
		}
		else{
			Wrestler1.lm.setSpeed(Wrestler1.rotateSlow);
			Wrestler1.rm.setSpeed(Wrestler1.rotateFast);
			Wrestler1.lm.setAcceleration(Wrestler1.rotateAccS);
			Wrestler1.rm.setAcceleration(Wrestler1.rotateAccF);
			Wrestler1.lm.rotateTo(Wrestler1.rotateClose);				//ratio equal to ratio of rotateFast:rotateSlow
			Wrestler1.rm.rotateTo(Wrestler1.rotateFar);
			Wrestler1.lm.waitComplete();
			Wrestler1.rm.waitComplete();
			while(!suppressed && (Wrestler1.lightData.getLightValue() < Wrestler1.blackWhiteThreshold || 
			        					Wrestler1.bwData.getLightValue() < Wrestler1.blackWhiteThreshold)){
				Thread.yield();
			}
		}
		leftFast = (!leftFast);
	}

	public void suppress() {
		suppressed = true;	
	}

}
