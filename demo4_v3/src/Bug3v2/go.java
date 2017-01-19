package Bug3v2;

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
	private final Pose dest = Bug3v2.destination;
	private boolean sound = false;
	
	public boolean takeControl() {
		return true;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug3v2.Debug) {
			LCD.clear();
			LCD.drawString("go to destination", 0, 1); 
		}
		
		//Start going
		Bug3v2.nav.goTo(dest.getX(), dest.getY());
		
		if(Bug3v2.arrived()) {
			if(sound) Sound.systemSound(true, 4);
			System.exit(0);
		}
		
		if(!Bug3v2.nav.isMoving()) 
			((DifferentialPilot) Bug3v2.nav.getMoveController()).steer(0);

		//set flag once to go into PID mode
		if(Bug3v2.sideSonicData.getSonicValue() < Bug3v2.antennaShort){
			Bug3v2.AVOIDOBSTACLE = true;
			Bug3v2.resetPID();
		}	
		
		//
		Delay.msDelay(50);
	}

	public void suppress() {
		suppressed = true;	
	}
}
