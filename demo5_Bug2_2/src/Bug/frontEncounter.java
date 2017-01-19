package Bug;

import algorithm.Point;
import ProfTests.DifferentialPilot;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class frontEncounter  implements Behavior{
	private boolean suppressed = false;
	private boolean sound = true;
	static boolean flag = true;
	
	public boolean takeControl() {
		if(!flag)
			return false;
		

		
		if(waveFrontBug2_2.frontSonicData.getSonicValue() < 30 || (waveFrontBug2_2.graph[front.X][front.Y] == -1)){
			System.out.printf("FRONTENCOUNTER\n");
			return true;
		}
		else{
			return false;
		}
	//	return frontEncounter();
	}

	@SuppressWarnings("deprecation")
	public void action() {
		suppressed = false;
		
	}

	public void suppress() {
		suppressed = true;	
	}

/*	
	private boolean frontEncounter(){
		Pose p = waveFrontBug2.poseProvider.getPose();
		Waypoint fake = new Waypoint(p.getX(), p.getY());
		Waypoint curr = waveFrontBug2.pathPoint1.get(0);
		Point point = Point.actualPoint(waveFrontBug2.mapLength - p.getY(), p.getX());
		Point front;
		switch(waveFrontBug2.f1(p.getHeading())){
			case 0: front = new Point(point.X, point.Y + 1);
					break;
			case 1: front = new Point(point.X - 1, point.Y);
					break;
			case 2: front = new Point(point.X - 1, point.Y);
					break;
			case 3: front = new Point(point.X, point.Y - 1);
					break;
			case 4: front = new Point(point.X, point.Y - 1);
					break;
			case 5: front = new Point(point.X + 1, point.Y);
					break;
			case 6: front = new Point(point.X + 1, point.Y);
					break;
			case 7: front = new Point(point.X, point.Y + 1);
					break;
			default: front = null;
					break;
		}
		
		if(waveFrontBug2.frontSonicData.getSonicValue() < 30 || (waveFrontBug2.graph[front.X][front.Y] == -1)){
			System.out.printf("FRONTENCOUNTER\n");
			return true;
		}
		else{
			return false;
		}
*/	/*	
		if(waveFrontBug2.count == 0){
			System.out.printf("currentPoint(%d,  %d)\n", point.X, point.Y);
			System.out.print("Heading: " + String.valueOf(waveFrontBug2.f1(p.getHeading()) + "\n"));
			System.out.print("Front: " + front.toString() + "\n");
			waveFrontBug2.count = 100;
		}
		else{
			waveFrontBug2.count--;
		}
	*/
	/*
		int frontData = waveFrontBug2.frontSonicData.getSonicValue();
		if((frontData < 30)||(waveFrontBug2.graph[front.X][front.Y] == -1)){
			System.out.print("FRONTENCOUTNER\n");
			System.out.printf("frontDist: %d\n", frontData);
			System.out.printf("currentPoint: (%d,  %d)\n", point.X, point.Y);
			System.out.printf("Heading: %d \n", (int) p.getHeading());
			System.out.print("f1(Heading): " + String.valueOf(waveFrontBug2.f1(p.getHeading()) + "\n"));
			System.out.print("Front: " + front.toString() + "\n");
			System.out.print("---------------------\n");
			return true;
		}
		else{
			return false;
		}
	*/

	//}

}