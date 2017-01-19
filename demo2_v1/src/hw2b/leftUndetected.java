package hw2b;

import java.util.Arrays;

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

public class leftUndetected implements Behavior{
	private boolean suppressed = false;
	
	public boolean takeControl() {
		int value = part3b.sonicData.getSonicValue();
		if( value > 25){
			LCD.drawInt(value, 0, 6);
			return true;
		}
		return false;
	}

	public void action() {
		part3b.touchedData.resetTouched();
		
		suppressed = false;
		LCD.drawString("b2", 0, 3);
		
		//update direction				
		part3b.left.controlMotor(50, 3);
		part3b.right.controlMotor(50, 3);
				
		Delay.msDelay(50);
		//action
		part3b.left.close();
		part3b.right.close();
			
		Brick brick = BrickFinder.getDefault(); 
		part3b.lm = new NXTRegulatedMotor(brick.getPort("A"));			
		part3b.rm = new NXTRegulatedMotor(brick.getPort("B"));

			part3b.lm.setAcceleration(270);
			part3b.rm.setAcceleration(270);
			part3b.lm.setSpeed(270);
			part3b.rm.setSpeed(270);
			part3b.lm.synchronizeWith(new RegulatedMotor[] {part3b.rm});
			part3b.lm.startSynchronization();
			part3b.lm.rotate((int) (part3b.stdDist/ part3b.wheelCircumference * 360 / 2), true);
			part3b.rm.rotate((int) (part3b.stdDist/ part3b.wheelCircumference * 360 / 2));
			part3b.lm.endSynchronization();
			part3b.lm.waitComplete();
			part3b.rm.waitComplete();
			
			part3b.lm.setAcceleration(270);
			part3b.rm.setAcceleration(270);
			part3b.lm.setSpeed(270);
			part3b.rm.setSpeed(270);
				
			part3b.lm.synchronizeWith(new RegulatedMotor[] {part3b.rm});
			part3b.lm.startSynchronization();
			turn1Wheel(90, part3b.turnConst1, "right", part3b.lm, part3b.rm);
			part3b.lm.endSynchronization();
			part3b.lm.waitComplete();
			part3b.rm.waitComplete();

/*
			lm.stop(true);
			rm.stop();
			lm.waitComplete();
			rm.waitComplete();
*/				
		part3b.lm.close();
		part3b.rm.close();

		part3b.left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class);
		part3b.right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class);	
				
		
		beforeLeftDetected.setFlag();
		if(beforeLeftDetected.flag){
			Sound.beep();
		}
		/*		part3b.flag = false;
				String s;
				if(part3b.flag){s = "true";}
				else{s = "false";}
				LCD.drawString(s, 0, 2);

				*/
		
		part3b.resetPID();
		suppressed = true;
	}

	public void suppress() {
		suppressed = true;
	}
	
   	private static void turn1Wheel(int degree, double turnConst, String wheel, NXTRegulatedMotor lm, NXTRegulatedMotor rm){
    	lm.setAcceleration(270);
    	rm.setAcceleration(270);
    	lm.setSpeed(270);
    	rm.setSpeed(270);
    	double turnAngle = turnConst *  (part3b.robotTrack * 2 * Math.PI * (degree / 360.0)) /(part3b.wheelDiameter * Math.PI) * 360.0;
    	if(wheel.equals("left")){
    		lm.rotate((int) turnAngle); 
    		lm.waitComplete();
    	}
    	if(wheel.equals("right")){
    		rm.rotate((int) turnAngle);    
    		rm.waitComplete();
    	}
   	}
}
