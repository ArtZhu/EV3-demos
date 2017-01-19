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
		if(waveFrontBug2.AVOIDOBSTACLE){
			return rightNoWall();
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		suppressed = false;
		if(waveFrontBug2.Debug){
			if(sound) Sound.systemSound(true, 3);
			LCD.clear();
			LCD.drawString("rightNoWall", 0, 1);
			System.out.print("rightNoWall\n");
		}
		
		waveFrontBug2.toCenter();		
		waveFrontBug2.set();
		((DifferentialPilot) waveFrontBug2.nav.getMoveController()).rotate(-1 * 90);
		
		//find wall
		waveFrontBug2.set();
		((DifferentialPilot) waveFrontBug2.nav.getMoveController()).steer(0);
		while(rightNoWall()){
			LCD.drawString("findingWall", 0, 3);
			Delay.msDelay(50);
		}
		
		waveFrontBug2.toCenter();
		
		while(waveFrontBug2.nav.isMoving()){
			Delay.msDelay(50);
		}
		
		if(waveFrontBug2.Debug){
			((DifferentialPilot) waveFrontBug2.nav.getMoveController()).stop();
			LCD.drawString("           ", 0, 3);
			LCD.drawString("foundWall", 0, 4);
			Delay.msDelay(100);
		}
	}

	public void suppress() {
		suppressed = true;	
	}
	
	public static boolean rightNoWall(){
		Pose p = waveFrontBug2.poseProvider.getPose();
		Point point = Point.actualPoint(waveFrontBug2.mapLength - p.getY(), p.getX());
		Point right;
		switch(waveFrontBug2.f1(p.getHeading())){
			case 0: right = new Point(point.X + 1, point.Y);
					break;
			case 1: right = new Point(point.X, point.Y + 1);
					break;
			case 2: right = new Point(point.X, point.Y + 1);
					break;
			case 3: right = new Point(point.X - 1, point.Y);
					break;
			case 4: right = new Point(point.X - 1, point.Y);
					break;
			case 5: right = new Point(point.X, point.Y - 1);
					break;
			case 6: right = new Point(point.X, point.Y - 1);
					break;
			case 7: right = new Point(point.X + 1, point.Y);
					break;
			default: right = null;
					break;
		}
		
		int sideDist = waveFrontBug2.sideSonicData.getSonicValue();
		if((sideDist > 25) && (waveFrontBug2.graph[right.X][right.Y]!= -1)){
			System.out.printf("RIGHTNOWALL\n");
			System.out.printf("SideDist: %d\n", sideDist);
			System.out.printf("Right: (%d, %d)\n", right.X, right.Y);
			return true;
		}
		return false;
	}	

}

