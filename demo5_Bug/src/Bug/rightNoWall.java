package Bug;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import ProfTests.*;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class rightNoWall implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = false;
	
	public boolean takeControl() {
		if(waveFrontBug.AVOIDOBSTACLE){
			return waveFrontBug.rightNoWall();
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(waveFrontBug.Debug){
			Sound.systemSound(true, 3);
			LCD.clear();
			LCD.drawString("rightNoWall", 0, 1);
		}
		waveFrontBug.toCenter();		
		((DifferentialPilot) waveFrontBug.nav.getMoveController()).setRotateSpeed(waveFrontBug.stdRotateSpeed);
		((DifferentialPilot) waveFrontBug.nav.getMoveController()).rotate(-1 * 90 * waveFrontBug.turnConst);
		
		//find wall
		((DifferentialPilot) waveFrontBug.nav.getMoveController()).setTravelSpeed(waveFrontBug.stdSpeed);
		((DifferentialPilot) waveFrontBug.nav.getMoveController()).steer(0);
		while(waveFrontBug.rightNoWall()){
			LCD.drawString("findingWall", 0, 3);
			Delay.msDelay(50);
		}
		
		waveFrontBug.toCenter();
		
		if(waveFrontBug.Debug){
			((DifferentialPilot) waveFrontBug.nav.getMoveController()).stop();
			LCD.drawString("           ", 0, 3);
			LCD.drawString("foundWall", 0, 4);
			if(sound) Sound.systemSound(true, 3);
			Delay.msDelay(1000);
		}
	}

	public void suppress() {
		suppressed = true;	
	}

}

