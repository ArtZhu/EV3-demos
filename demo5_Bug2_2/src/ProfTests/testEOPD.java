package ProfTests;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.HiTechnicEOPD;
import Threads.EOPDThread;
import Threads.globalEOPDData;
import ProfTests.*;
import Threads.*;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.utility.Delay;
import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.hardware.sensor.NXTColorSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.sensor.NXTLightSensor;

public class testEOPD {
	private static globalEOPDData EOPDData = new globalEOPDData();
	private static HiTechnicEOPD eopd = new HiTechnicEOPD(SensorPort.S4);

	public static void main(String[] args){
		Thread e = new Thread(new EOPDThread(EOPDData, eopd));
		e.setDaemon(true);
		e.start();
		
		Brick brick = BrickFinder.getDefault(); // get specifics about this
		// robot
		brick.getKey("Escape").addKeyListener(new KeyListener() {
			public void keyPressed(Key k) {
				eopd.close();
				System.exit(0);
			}

			public void keyReleased(Key k) {
				eopd.close();
				System.exit(0);
			}
		});	
		
		while(true){
			LCD.drawInt(EOPDData.getEOPDValue(), 0, 1);
			Delay.msDelay(10);
		}
	}
}
