package Wrestler;
import lejos.hardware.lcd.LCD;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class push implements Behavior{
	private boolean suppressed = false;
	
	public boolean takeControl() {
		return (Wrestler.sonicDataL.getSonicValue() < 12 || Wrestler.sonicDataR.getSonicValue() < 12);
	}

	public void action() {
		Wrestler.lm.setAcceleration(2880);
		Wrestler.rm.setAcceleration(2880);
		Wrestler.lm.setSpeed(720);
		Wrestler.rm.setSpeed(720);
		if(Wrestler.sonicDataL.getSonicValue() < 12){
			//left negative, right positive
			turn(90, Wrestler.turnConstFast);
			Wrestler.lm.forward();
			Wrestler.rm.forward();
			if(Wrestler.lightData.getLightValue() > Wrestler.blackWhiteThreshold){
				Wrestler.lm.stop(true);
				Wrestler.rm.stop(true);
			}
		}
		else{
			//left positive, right negative
			turn(-1 * 90, Wrestler.turnConstFast);
			Wrestler.lm.forward();
			Wrestler.rm.forward();
			if(Wrestler.lightData.getLightValue() > Wrestler.blackWhiteThreshold){
				Wrestler.lm.stop(true);
				Wrestler.rm.stop(true);
			}
		}
	}

	public void suppress() {
		suppressed = true;	
	}

   	private static void turn(int degree, double turnConst){
    	double turnAngle = turnConst *  (Wrestler.robotTrack * Math.PI * (degree / 360.0)) 
    									/(Wrestler.wheelDiameter * Math.PI) * 360.0;
    	
    	Wrestler.lm.synchronizeWith(new RegulatedMotor[] {Wrestler.rm});
    	Wrestler.lm.startSynchronization();	
    	Wrestler.lm.rotate((int) (-1 * turnAngle), true);
    	Wrestler.rm.rotate((int) turnAngle);	
    	Wrestler.lm.endSynchronization();
   	}
}
