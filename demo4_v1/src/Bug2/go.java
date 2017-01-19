package Bug2;

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
	private final Pose dest = Bug2.destination;
	
	public boolean takeControl() {
		return true;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug2.Debug) {
			LCD.clear();
			LCD.drawString("go to destination", 0, 1); 
		}
		
//		((DifferentialPilot) Bug2.nav.getMoveController()).setLinearSpeed(Bug2.stdSpeed);
//		Bug2.nav.goTo(dest.getX(), dest.getY(), dest.getHeading());
//		((DifferentialPilot) Bug2.nav.getMoveController()).steer(0);
//		((DifferentialPilot) Bug2.nav.getMoveController()).forward();
//		Bug2.nav.goTo(dest.getX(), dest.getY());
		
		Bug2.nav.goTo(dest.getX(), dest.getY());
		
		if(Bug2.arrived()) {
			//Sound.systemSound(true, 4);
			System.exit(0);
		}
		if(!Bug2.nav.isMoving()) ((DifferentialPilot) Bug2.nav.getMoveController()).steer(0);
		Delay.msDelay(50);
		
	}

	public void suppress() {
		suppressed = true;	
	}
}
