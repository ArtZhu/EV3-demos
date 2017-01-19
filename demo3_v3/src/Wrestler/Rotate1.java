package Wrestler;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class Rotate1 implements Behavior{
	private boolean suppressed = false;
	private boolean leftFast = true;
	
	public boolean takeControl() {
		return true;
	}

	public void action() {
		int turn = (Wrestler1.rotateFar + Wrestler1.oneCycle);
		int turn1 = Wrestler1.rotateFar;
		if(leftFast){
			
			Wrestler1.lm.setSpeed(Wrestler1.rotateFast);
			Wrestler1.rm.setSpeed(Wrestler1.rotateSlow);
			Wrestler1.lm.setAcceleration(Wrestler1.rotateAccF);
			Wrestler1.rm.setAcceleration(Wrestler1.rotateAccS);
			Wrestler1.lm.rotate(-1 * turn1, true);				//ratio equal to ratio of rotateFast:rotateSlow
			Wrestler1.rm.rotate(-1 * Wrestler1.rotateClose);

			while(Wrestler1.lm.isMoving()){
				int distL = Wrestler1.sonicDataL.getSonicValue();
				int distR = Wrestler1.sonicDataR.getSonicValue();
				if(!suppressed && (distL < 15 || distR < 15))
									Thread.yield();
			}
			Wrestler1.lm.waitComplete();
			Wrestler1.rm.waitComplete();
		}
		else{
			Wrestler1.lm.setSpeed(Wrestler1.rotateSlow);
			Wrestler1.rm.setSpeed(Wrestler1.rotateFast);
			Wrestler1.lm.setAcceleration(Wrestler1.rotateAccS);
			Wrestler1.rm.setAcceleration(Wrestler1.rotateAccF);
			Wrestler1.lm.rotate(Wrestler1.rotateClose, true);				//ratio equal to ratio of rotateFast:rotateSlow
			Wrestler1.rm.rotate(turn1);

			while(Wrestler1.rm.isMoving()){
				int distL = Wrestler1.sonicDataL.getSonicValue();
				int distR = Wrestler1.sonicDataR.getSonicValue();
				if(!suppressed && (distL < 15 || distR < 15))
									Thread.yield();
			}
			Wrestler1.lm.waitComplete();
			Wrestler1.rm.waitComplete();
		}
		leftFast = (!leftFast);
	}

	public void suppress() {
		suppressed = true;	
	}

}
