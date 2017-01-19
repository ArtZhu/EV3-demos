package hw2b_v2;

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
		if(part3b_v2.touchedData.getTouched()){
			return true;
		}
		return false;
	}

	public void action() {
		suppressed = false;
		LCD.drawString("b4",  0,  3);
		
		//update direction				
		part3b_v2.left.controlMotor(50, 3);
		part3b_v2.right.controlMotor(50, 3);
		

		part3b_v2.touchedData.resetTouched();
		//Necessary
//		Delay.msDelay(50);
				
		//action
		part3b_v2.left.close();
		part3b_v2.right.close();
				
		Brick brick = BrickFinder.getDefault(); 
		part3b_v2.lm = new NXTRegulatedMotor(brick.getPort("A"));			
		part3b_v2.rm = new NXTRegulatedMotor(brick.getPort("B"));

		//GetBack!
/*		lm.synchronizeWith(new RegulatedMotor[] {rm});
		lm.startSynchronization();
		*/   		

		part3b_v2.lm.setAcceleration(360);
		part3b_v2.rm.setAcceleration(360);
		part3b_v2.lm.setSpeed(180);
		part3b_v2.rm.setSpeed(180);
				
			
//		part3b_v2.lm.synchronizeWith(new NXTRegulatedMotor[] {part3b_v2.rm});
//		part3b_v2.lm.startSynchronization();
		part3b_v2.lm.rotate((int) (-1 * 11.0 / part3b_v2.wheelCircumference * 360 ), true);
		part3b_v2.rm.rotate((int) (-1 * 11.0 / part3b_v2.wheelCircumference * 360));
		part3b_v2.lm.waitComplete();
		part3b_v2.rm.waitComplete();
//		part3b_v2.lm.endSynchronization();
/*
		part3b_v2.lm.stop(true);
		part3b_v2.rm.stop();
		part3b_v2.lm.waitComplete();
		part3b_v2.rm.waitComplete();
*/				
		turnAngle(90, part3b_v2.lm, part3b_v2.rm);
		part3b_v2.lm.waitComplete();
		part3b_v2.rm.waitComplete();
				
		part3b_v2.lm.close();
		part3b_v2.rm.close();
		
		//reset I and touchedData at last
		Sound.beep();
		Delay.msDelay(500);
				
		part3b_v2.touchedData.resetTouched();
				
		part3b_v2.isSonicData1 = true;
		part3b_v2.left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class);
		part3b_v2.right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class);	
		
/*				
		String s;
		if(part3b_v2.flag){s = "true";}
		else{s = "false";}
		LCD.drawString(s, 0, 2);
*/
		part3b_v2.resetPID();
		part3b_v2.touchedData.resetTouched();
		suppressed = true;
	}

	public void suppress() {
		suppressed = true;
	}
	
	private void turnAngle(int degree, NXTRegulatedMotor lm, NXTRegulatedMotor rm){
    	lm.setAcceleration(360);
    	rm.setAcceleration(360);
    	//
    	double turnAngle = part3b_v2.turnConst * (part3b_v2.robotTrack * Math.PI * degree / 360) /(part3b_v2.wheelDiameter * Math.PI) * 360;
		
    	lm.synchronizeWith(new RegulatedMotor[] {rm});
   		rm.startSynchronization();
    	lm.rotate((int) (1 * turnAngle), true);
    	rm.rotate((int) (-1 * turnAngle));	
    	lm.endSynchronization();
   	}

}
