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

public class beforeLeftDetected implements Behavior{
	private boolean suppressed = false;
	public static boolean flag = true;
	public static void setFlag(){ flag = true;	}
	public static void resetFlag() { flag = false; }
	
	public boolean takeControl() {
		if(flag){
			return true;
		}
		return false;
	}

	public void action() {		
		suppressed = false;
		LCD.drawString("b3", 0, 3);	
		
		part3b.touchedData.resetTouched();
//		Sound.systemSound(true, 3);
		//???????????

		part3b.left.controlMotor(25, 1);
		part3b.right.controlMotor(25, 1);
			
		//		
		int sideDist = part3b.sonicData.getSonicValue();
		LCD.drawInt(sideDist, 0, 5);
		if(sideDist < 25){
			resetFlag();
			Delay.msDelay(100);
			part3b.isSonicData1 = true;
			Delay.msDelay(50);
				/*
				suppressed = true;
				Sound.beep();
				part3b.flag = true;
				*/
		}
		LCD.drawInt(sideDist, 0, 4);

		
		Delay.msDelay(50);
		part3b.resetPID();
/*		
		String s;
		if(part3b.flag){s = "true";}
		else{s = "false";}
		LCD.drawString(s, 0, 2);
*/
	}

	public void suppress() {
		suppressed = true;
	}
	
}
