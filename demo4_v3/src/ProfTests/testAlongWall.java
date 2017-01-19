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

public class testAlongWall {
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

	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		float[] points = new float[opp.sampleSize()];
		int count = 0;

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
		
		int distance = sonicData.getSonicValue();
		pilot.steer(0);  //start moving
		
		while (distance < 30){  //no opening
			opp.fetchSample(points, 0);
			System.out.printf("p[%d]: %.1f, %.1f, %.1f\n", count,
					points[0], points[1], points[2]);
			
			distance = sonicData.getSonicValue();
			count++;
		}
		pilot.stop();
		//opp.fetchSample(points, 0); //too far?
		System.out.printf("open starts: %.1f, %.1f\n", points[0], points[1]);
		System.out.printf("p[%d]: %.1f, %.1f, %.1f\n", count,points[0], points[1], points[2]);
		Button.waitForAnyPress();
		pilot.steer(0);
		while (distance > 30){  //follow opening
			opp.fetchSample(points, 0);
			System.out.printf("p[%d]: %.1f, %.1f, %.1f\n", count,
					points[0], points[1], points[2]);
			
			distance = sonicData.getSonicValue();
			count++;
		}
		pilot.stop();
		//opp.fetchSample(points, 0); //too far?
		System.out.printf("open ends: %.1f, %.1f\n", points[0], points[1]);
		System.out.printf("p[%d]: %.1f, %.1f, %.1f\n", count,points[0], points[1], points[2]);
		Button.waitForAnyPress();
	}
}
