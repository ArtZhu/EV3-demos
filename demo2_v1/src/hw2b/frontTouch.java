package hw2b;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class frontTouch implements Behavior{
	private boolean suppressed = false;
	
	public boolean takeControl() {
		if(part3b.touchedData.getTouched()){
			return true;
		}
		return false;
	}

	public void action() {
		suppressed = false;
		LCD.drawString("b4",  0,  3);
		
		//update direction				
		part3b.left.controlMotor(50, 3);
		part3b.right.controlMotor(50, 3);
		

		part3b.touchedData.resetTouched();
		//Necessary
//		Delay.msDelay(50);
				
		//action
		part3b.left.close();
		part3b.right.close();
				
		Brick brick = BrickFinder.getDefault(); 
		part3b.lm = new NXTRegulatedMotor(brick.getPort("A"));			
		part3b.rm = new NXTRegulatedMotor(brick.getPort("B"));

		//GetBack!
/*		lm.synchronizeWith(new RegulatedMotor[] {rm});
		lm.startSynchronization();
		*/   		

		part3b.lm.setAcceleration(360);
		part3b.rm.setAcceleration(360);
		part3b.lm.setSpeed(180);
		part3b.rm.setSpeed(180);
				
			
//		part3b.lm.synchronizeWith(new NXTRegulatedMotor[] {part3b.rm});
//		part3b.lm.startSynchronization();
		part3b.lm.rotate((int) (-1 * 11.0 / part3b.wheelCircumference * 360 ), true);
		part3b.rm.rotate((int) (-1 * 11.0 / part3b.wheelCircumference * 360));
		part3b.lm.waitComplete();
		part3b.rm.waitComplete();
//		part3b.lm.endSynchronization();
/*
		part3b.lm.stop(true);
		part3b.rm.stop();
		part3b.lm.waitComplete();
		part3b.rm.waitComplete();
*/				
		turnAngle(90, part3b.lm, part3b.rm);
		part3b.lm.waitComplete();
		part3b.rm.waitComplete();
				
		part3b.lm.close();
		part3b.rm.close();
		
		//reset I and touchedData at last
		Sound.beep();
		Delay.msDelay(500);
				
		part3b.touchedData.resetTouched();
				
		part3b.isSonicData1 = true;
		part3b.left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class);
		part3b.right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class);	
		
/*				
		String s;
		if(part3b.flag){s = "true";}
		else{s = "false";}
		LCD.drawString(s, 0, 2);
*/
		part3b.resetPID();
		part3b.touchedData.resetTouched();
		suppressed = true;
	}

	public void suppress() {
		suppressed = true;
	}
	
	private void turnAngle(int degree, NXTRegulatedMotor lm, NXTRegulatedMotor rm){
    	lm.setAcceleration(360);
    	rm.setAcceleration(360);
    	//
    	double turnAngle = part3b.turnConst * (part3b.robotTrack * Math.PI * degree / 360) /(part3b.wheelDiameter * Math.PI) * 360;
		
    	lm.synchronizeWith(new RegulatedMotor[] {rm});
   		rm.startSynchronization();
    	lm.rotate((int) (1 * turnAngle), true);
    	rm.rotate((int) (-1 * turnAngle));	
    	lm.endSynchronization();
   	}

}
