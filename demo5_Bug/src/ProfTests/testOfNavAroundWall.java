package ProfTests;

import Threads.*;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;

public class testOfNavAroundWall {
	// Instantiate Sensors
	private static globalUltrasonicData sonicData = new globalUltrasonicData();
	private static globalEOPDData EOPDData = new globalEOPDData();
	private static HiTechnicEOPD eopd = new HiTechnicEOPD(SensorPort.S1);
	private static NXTUltrasonicSensor us = new NXTUltrasonicSensor(
			SensorPort.S2);

	// Instantiate Pilot/PoseProvider
	private static double wheelDiameter = 5.6;
	private static double trackWidth = 14.5;
	@SuppressWarnings("deprecation")
	private static DifferentialPilot pilot = new DifferentialPilot(
			wheelDiameter, trackWidth, Motor.B, Motor.A);
	private static Navigator nav = new Navigator(pilot);
	@SuppressWarnings("unused")
	private static OdometryPoseProvider opp = new OdometryPoseProvider(pilot);

	
	//some waypoints
	private static Waypoint finish = new Waypoint(100, 0, 0);

	public static void main(String[] args) {
		float[] points = new float[opp.sampleSize()];
		int count = 0;
		int distance;

		Thread e = new Thread(new EOPDThread(EOPDData, eopd));
		e.setDaemon(true);
		e.start();

		Thread u = new Thread(new sonicThread(sonicData, us));
		u.setDaemon(true);
		u.start();

		// establish a fail-safe: pressing Escape quits
		Brick brick = BrickFinder.getDefault(); // get specifics about this
		// robot
		brick.getKey("Escape").addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				pilot.stop();
				us.close();
				eopd.close();
				Motor.A.close();
				Motor.B.close();
				System.exit(1);
			}

			@Override
			public void keyReleased(Key k) {
				System.exit(1);
			}
		});
		
		pilot.setLinearSpeed(10);  //sets straight ahead speed
		pilot.setAngularSpeed(50); //sets turning speed
		pilot.addMoveListener(opp);  //adds the listerner to capture x,y,heading values
		
		
		//nav.goTo(finish);
		pilot.steer(0); // start moving

		while (true){
			if (EOPDData.getEOPDValue() < 95) {
				nav.stop();  //stops pilot as well
				break;
			}
		}
		pilot.rotate(90); // left turn with +

		while (true) {
			pilot.steer(0);
			distance = sonicData.getSonicValue();
			while (distance < 50) { // go around wall
				opp.fetchSample(points, 0);
				System.out.printf("p[%d]: %.2f, %.2f\n", count, points[0],
						points[1]);
				distance = sonicData.getSonicValue();
			}
			//pilot.stop();  //or use nav.stop()
			nav.stop();
			pilot.travel(10);
			pilot.rotate(-90); // right turn with -
			pilot.travel(20);
			distance = sonicData.getSonicValue();
		}
		
	}
}
