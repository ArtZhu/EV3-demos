package Bug;

import algorithm.Point;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import ProfTests.*;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class rightNoWall implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = true;
	
	public boolean takeControl() {
		if(waveFrontBug2_2.AVOIDOBSTACLE){
			return rightNoWall();
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		suppressed = false;
		if(waveFrontBug2_2.Debug){
			if(sound) Sound.systemSound(true, 3);
			LCD.clear();
			LCD.drawString("rightNoWall", 0, 1);
			System.out.print("rightNoWall\n");
		
		
		if(waveFrontBug2_2.Debug){
			((DifferentialPilot) waveFrontBug2_2.nav.getMoveController()).stop();
			LCD.drawString("           ", 0, 3);
			LCD.drawString("foundWall", 0, 4);
			Delay.msDelay(100);
		}
	}

	public void suppress() {
		suppressed = true;	
	}
	
	public static boolean rightNoWall(){
		
	}	

}

