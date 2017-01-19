package adapter;

import ProfTests.DifferentialPilot;
import Threads.sonicThread;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class go implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = true;
	
	public boolean takeControl() {
		return true;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(waveFrontBug.Debug) {
			LCD.clear();
			LCD.drawString("go to destination", 0, 1); 
		}
		
		//Start going
		waveFrontBug.nav.followPath();
		
		if(waveFrontBug.arrived()) {
			if(sound) Sound.systemSound(true, 4);
			System.exit(0);
		}
		
		//
		Delay.msDelay(50);
	}

	public void suppress() {
		suppressed = true;	
	}
}
