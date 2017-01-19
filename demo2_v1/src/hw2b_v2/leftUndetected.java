package hw2b_v2;

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
		int value = part3b_v2.sonicData.getSonicValue();
		if( value > 25){
			LCD.drawInt(value, 0, 6);
			return true;
		}
		return false;
	}

	public void action() {
		part3b_v2.touchedData.resetTouched();
		
		suppressed = false;
		LCD.drawString("b2", 0, 3);
		
		//update direction				
		part3b_v2.left.controlMotor(50, 3);
		part3b_v2.right.controlMotor(50, 3);
				
		Delay.msDelay(50);
		//action
		part3b_v2.left.close();
		part3b_v2.right.close();
			
		Brick brick = BrickFinder.getDefault(); 
		part3b_v2.lm = new NXTRegulatedMotor(brick.getPort("A"));			
		part3b_v2.rm = new NXTRegulatedMotor(brick.getPort("B"));

			part3b_v2.lm.setAcceleration(270);
			part3b_v2.rm.setAcceleration(270);
			part3b_v2.lm.setSpeed(270);
			part3b_v2.rm.setSpeed(270);
			part3b_v2.lm.synchronizeWith(new RegulatedMotor[] {part3b_v2.rm});
			part3b_v2.lm.startSynchronization();
			part3b_v2.lm.rotate((int) (part3b_v2.stdDist/ part3b_v2.wheelCircumference * 360 / 2), true);
			part3b_v2.rm.rotate((int) (part3b_v2.stdDist/ part3b_v2.wheelCircumference * 360 / 2));
			part3b_v2.lm.endSynchronization();
			part3b_v2.lm.waitComplete();
			part3b_v2.rm.waitComplete();
			
			part3b_v2.lm.setAcceleration(270);
			part3b_v2.rm.setAcceleration(270);
			part3b_v2.lm.setSpeed(270);
			part3b_v2.rm.setSpeed(270);
				
			part3b_v2.lm.synchronizeWith(new RegulatedMotor[] {part3b_v2.rm});
			part3b_v2.lm.startSynchronization();
			turn1Wheel(90, part3b_v2.turnConst1, "right", part3b_v2.lm, part3b_v2.rm);
			part3b_v2.lm.endSynchronization();
			part3b_v2.lm.waitComplete();
			part3b_v2.rm.waitComplete();

/*
			lm.stop(true);
			rm.stop();
			lm.waitComplete();
			rm.waitComplete();
*/				
		part3b_v2.lm.close();
		part3b_v2.rm.close();

		part3b_v2.left =  (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class);
		part3b_v2.right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class);	
				
		part3b_v2.left.controlMotor(25, 1);
		part3b_v2.right.controlMotor(25, 1);
			
		//		
		int sideDist = part3b_v2.sonicData.getSonicValue();
		LCD.drawInt(sideDist, 0, 5);
		
		/*
		while(sideDist > 25){
			sideDist = part3b_v2.sonicData.getSonicValue();
			Delay.msDelay(50);
			if(part3b_v2.touchedData.getTouched()){
				Thread.yield();
			}
		}
		*/
		while(!suppressed && part3b_v2.touchedData.getTouched()){
			Thread.yield();
			if(part3b_v2.sonicData.getSonicValue() < 25) suppressed = true;
		}
		
		LCD.drawInt(sideDist, 0, 4);

		Delay.msDelay(50);
		part3b_v2.left.controlMotor(50, 3);
		part3b_v2.right.controlMotor(50, 3);
		part3b_v2.isSonicData1 = true;
		part3b_v2.resetPID();
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
    	double turnAngle = turnConst *  (part3b_v2.robotTrack * 2 * Math.PI * (degree / 360.0)) /(part3b_v2.wheelDiameter * Math.PI) * 360.0;
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
