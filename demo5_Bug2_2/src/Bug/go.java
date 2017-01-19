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
	private static int count = 0;private static int count2 = 0;
	public boolean takeControl() {
		return true;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		suppressed = false;
		if(waveFrontBug2_2.Debug) {
			LCD.clear();
			LCD.drawString("go to destination", 0, 1);
			if(count == 0){
				System.out.print("go to destination\n");
				count = 100;
			}
			else
				count--;
		}

		//if arrived
		if(waveFrontBug2_2.arrived()) {
			Sound.beep();
			waveFrontBug2_2.dance();
			System.exit(0);
		}
		
		//
		waveFrontBug2_2.set();
		if(!waveFrontBug2_2.nav.isMoving()){
			if(count2 == 0){
				System.out.printf("Flag = %b\n", frontEncounter.flag);
				count2 = 100;
			}
			else
				count2 --;
			
			Pose p = waveFrontBug2_2.poseProvider.getPose();
			Waypoint fake = new Waypoint(p.getX(), p.getY());
			Waypoint curr = waveFrontBug2_2.pathPoint1.get(0);
			Waypoint next = waveFrontBug2_2.pathPoint1.get(1);
			

			Point curr1 = Point.actualPoint(waveFrontBug2_2.mapLength - curr.getY(), curr.getX());
			Point fake1 = Point.actualPoint(waveFrontBug2_2.mapLength - fake.getY(), fake.getX());
			if((curr1.X == fake1.X && curr1.Y == fake1.Y) 
					/*			||	(curr1.X == fake1.X && curr1.Y == fake1.Y+1)
								||	(curr1.X == fake1.X && curr1.Y == fake1.Y-1)
								||	(curr1.X == fake1.X+1 && curr1.Y == fake1.Y)
								||	(curr1.X == fake1.X-1 && curr1.Y == fake1.Y)
					*/				)
								frontEncounter.flag = false;
			if(waveFrontBug2_2.closeTo(fake, curr)){
				Delay.msDelay(500);
				int heading = waveFrontBug2_2.f1(p.getHeading());
				heading = (heading + 1) % 8 / 2;
				int angle = (waveFrontBug2_2.dirTo(curr, next) - heading ) * 90;
				if(angle > 180)
					angle = angle - 360;
				else if(angle < -180)
					angle = 360 + angle;
				if(sound) Sound.beep();
				System.out.printf("Turning %d \n", angle);
				waveFrontBug2_2.pilot.rotate(angle);
			//	Pose pc = waveFrontBug2.poseProvider.getPose();
			//	waveFrontBug2.poseProvider.setPose(new Pose(p.getX(), p.getY(), p.getHeading()+angle));
				System.out.printf("Pose SET: (%d, %d, %d) \n", (int) p.getX(), (int) p.getY(), (int) p.getHeading());
				frontEncounter.flag = true;
				waveFrontBug2_2.pathPoint1.remove(0);
			}
			//start going
			Waypoint wp = waveFrontBug2_2.pathPoint1.get(0);
			System.out.printf("GO TO: (%f, %f)\n", wp.x, wp.y);
			waveFrontBug2_2.nav.goTo(wp);
		}
	
		Delay.msDelay(50);
	}

	public void suppress() {
		suppressed = true;	
	}
}
