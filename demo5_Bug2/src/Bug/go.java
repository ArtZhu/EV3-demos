package Bug;

import algorithm.Point;
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
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class go implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = false;
	private int count = 10;
	
	public boolean takeControl() {
		return true;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		suppressed = false;
		if(waveFrontBug2.Debug) {
			LCD.clear();
			LCD.drawString("go to destination", 0, 1);
		}

		//if arrived
		if(waveFrontBug2.arrived()) {
			Sound.beep();
			waveFrontBug2.dance();
			System.exit(0);
		}
		
		//
		waveFrontBug2.set();
		waveFrontBug2.nav.followPath();
	
		Delay.msDelay(50);
		
		Pose p = waveFrontBug2.poseProvider.getPose();
		Point p1 = Point.actualPoint(waveFrontBug2.mapLength - p.getY(), p.getX());
		if(count==0){
			System.out.printf("(%d, %d)\n", p1.X, p1.Y);
			count=50;
		}
		else
			count--;
	}

	public void suppress() {
		suppressed = true;	
	}
}
