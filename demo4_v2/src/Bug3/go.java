package Bug3;

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
	private final Pose dest = Bug3.destination;
	private boolean sound = false;
//	private int angleClock = -1;
	
	public boolean takeControl() {
		return true;
	}

	@SuppressWarnings("deprecation")
	public void action() {
//		if(angleClock == -1) angleClock = 1;
//		else angleClock = -1;
		
		if(Bug3.Debug) {
			LCD.clear();
			LCD.drawString("go to destination", 0, 1); 
		}
		
		//Start going
		Bug3.nav.goTo(dest.getX(), dest.getY());
		
		if(Bug3.arrived()) {
			if(sound) Sound.systemSound(true, 4);
			System.exit(0);
		}
		
		if(!Bug3.nav.isMoving()) 
			((DifferentialPilot) Bug3.nav.getMoveController()).steer(0);
//		else
//			((DifferentialPilot) Bug3.nav.getMoveController()).steer(angleClock);
		
			
		//set flag once to go into PID mode
		if(Bug3.sideSonicData.getSonicValue() < Bug3.antennaShort){
			Bug3.AVOIDOBSTACLE = true;
			Bug3.resetPID();
		}	
		
		//
		LCD.drawInt(((int)Bug3.poseProvider.getPose().getX() * 10000), 0, 5);
		LCD.drawInt(((int)Bug3.poseProvider.getPose().getY() * 10000), 0, 6);
		Delay.msDelay(50);
	}

	public void suppress() {
		suppressed = true;	
	}
}
