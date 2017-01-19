package Bug;

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
	private boolean sound = true;
	private int i = 10;
	
	public boolean takeControl() {
		return true;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(waveFrontBug.Debug) {
	//		Sound.systemSound(true, 1);
			LCD.clear();
			LCD.drawString("go to destination", 0, 1);
//			Waypoint w = waveFrontBug.nav.getWaypoint();
//			LCD.drawString("( " + String.valueOf(w.x) + ", " + String.valueOf(w.y)+ " )", 0, 6);
		}

		
		//Start going
		((DifferentialPilot) waveFrontBug.nav.getMoveController()).setAngularSpeed(waveFrontBug.stdRotateSpeed);
		((DifferentialPilot) waveFrontBug.nav.getMoveController()).setLinearSpeed(waveFrontBug.stdSpeed);
//		waveFrontBug.pilot.setRotateSpeed(waveFrontBug.stdRotateSpeed);
//		waveFrontBug.pilot.setTravelSpeed(waveFrontBug.stdSpeed);
		waveFrontBug.nav.followPath();
//		waveFrontBug.pilot.steer(0);
		
/*		if(!waveFrontBug.nav.isMoving() && !waveFrontBug.path1.isEmpty()){
//			waveFrontBug.currentWaypoint = waveFrontBug.path1.poll();
			waveFrontBug.pilot.setRotateSpeed(waveFrontBug.stdRotateSpeed);
			waveFrontBug.pilot.setTravelSpeed(waveFrontBug.stdSpeed);
			
//			waveFrontBug.nav.goTo(waveFrontBug.currentWaypoint);
		}
*/
		Pose p = waveFrontBug.poseProvider.getPose();
//		LCD.clear();
/*		Waypoint w = waveFrontBug.nav.getWaypoint();
		try{
			System.out.println("WAYP: (" + String.valueOf((int) (w.x)) + ", " + String.valueOf((int) (w.y)) + ")");
		}catch(Exception e){}
		try{
			System.out.println("CURR: (" + String.valueOf((int) p.getX()) + ", " + String.valueOf((int) p.getY()) + ")");
		}catch(Exception e){}
*/		
		//This prints 0.0
//		System.out.println("SPEED = " + String.valueOf(waveFrontBug.nav.getMoveController().getTravelSpeed()) + " \n");
//		LCD.drawString("(" + String.valueOf(p.getX()) + ", " + String.valueOf(p.getY()) + ")", 0, 6);
/*
		Waypoint wp =  waveFrontBug.path[waveFrontBug.currStartIndex + 1];
		waveFrontBug.currentWaypoint = wp;
		if(Math.abs(wp.x - p.getX()) <  2.0 && Math.abs(wp.y - p.getY()) <  2.0){
			i = 10;
		}
		
		if(i >= 0){
			i--;
		}
		
		if(i == 0){
			waveFrontBug.update();
		}
*/		if(waveFrontBug.arrived()) {
			Sound.beep();
			waveFrontBug.dance();
			System.exit(0);
		}
		
		Delay.msDelay(50);
	}

	public void suppress() {
		suppressed = true;	
	}
}
