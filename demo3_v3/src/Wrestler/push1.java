package Wrestler;
import lejos.hardware.lcd.LCD;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class push1 implements Behavior{
	private boolean suppressed = false;
	
	public boolean takeControl() {
		boolean go = false;
		for(int i = 0; i<3; i++){
			go = go || (Wrestler1.sonicDataL.getSonicValue() < 25 || Wrestler1.sonicDataR.getSonicValue() < 25);
			Delay.msDelay(10);
		}
		return go;
	}

	public void action() {
		Wrestler1.lm.setAcceleration(2880);
		Wrestler1.rm.setAcceleration(2880);
		Wrestler1.lm.setSpeed(Wrestler1.pushSpeed);
		Wrestler1.rm.setSpeed(Wrestler1.pushSpeed);
		if(Wrestler1.sonicDataL.getSonicValue() < 25){
			//left negative, right positive
			turn(-1 * 65, Wrestler1.turnConstFast);
			Wrestler1.lm.setAcceleration(2880);
			Wrestler1.rm.setAcceleration(2880);
			Wrestler1.lm.setSpeed(Wrestler1.pushSpeed);
			Wrestler1.rm.setSpeed(Wrestler1.pushSpeed);
			Wrestler1.lm.forward();
			Wrestler1.rm.forward();
			

			while(Wrestler1.lm.isMoving()){
				if(Wrestler1.lightData.getLightValue() < Wrestler1.blackWhiteThreshold){
					Wrestler1.lm.stop(true);
					Wrestler1.rm.stop();
	//				System.exit(0);	
				}
				Delay.msDelay(50);
			}
		}
		else{
			//left positive, right negative
			turn(65, Wrestler1.turnConstFast);
			Wrestler1.lm.setAcceleration(2880);
			Wrestler1.rm.setAcceleration(2880);
			Wrestler1.lm.setSpeed(Wrestler1.pushSpeed);
			Wrestler1.rm.setSpeed(Wrestler1.pushSpeed);
			Wrestler1.lm.forward();
			Wrestler1.rm.forward();

			while(Wrestler1.lm.isMoving()){
				if(Wrestler1.lightData.getLightValue() < Wrestler1.blackWhiteThreshold){
					Wrestler1.lm.stop(true);
					Wrestler1.rm.stop();
	//				System.exit(0);	
				}
				Delay.msDelay(50);
			}
		}
	}

	public void suppress() {
		suppressed = true;	
	}

   	private static void turn(int degree, double turnConst){
    	double turnAngle = turnConst *  (Wrestler1.robotTrack * Math.PI * (degree / 360.0)) 
    									/(Wrestler1.wheelDiameter * Math.PI) * 360.0;
    	
//    	Wrestler1.lm.synchronizeWith(new RegulatedMotor[] {Wrestler1.rm});
//    	Wrestler1.lm.startSynchronization();	
    	Wrestler1.lm.rotate((int) (-1 * turnAngle), true);
    	Wrestler1.rm.rotate((int) turnAngle);	
//    	Wrestler1.lm.endSynchronization();
    	Wrestler1.lm.waitComplete();
    	Wrestler1.rm.waitComplete();	
   	}
}
