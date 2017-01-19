package Test;
import lejos.hardware.sensor.HiTechnicIRSeekerV2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import Threads.*;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;
import lejos.robotics.subsumption.*;
import lejos.robotics.pathfinding.Path;


//192.168.1.4
//136.167.209.137
public class testIR {
	private static globalIRData data;
	private static HiTechnicIRSeekerV2 Seeker;
	private static float seek;
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		Seeker = new HiTechnicIRSeekerV2(SensorPort.S3);
		data = new globalIRData();
		Socket s = null;
		ObjectOutputStream b = null;
		IRAngle a;
		DifferentialPilot pilot = new DifferentialPilot(5.6, 17.0, Motor.B, Motor.A);
		PoseProvider pp;  //need this since the new version of nav has a built in odometry provider
		Navigator nav = new Navigator(pilot);

		pilot.setTravelSpeed(10);
		pilot.setRotateSpeed(20);
		
		pp = nav.getPoseProvider();  //needed to set initial pose
		pilot.addMoveListener((MoveListener) pp);

		//Thread i = new Thread(new IRThread(data, Seeker) );
		//i.setDaemon(true);
		//i.start();
		SampleProvider sonicSampleProvider = Seeker.getModulatedMode();
		float[] s1 = new float[sonicSampleProvider.sampleSize()];
		
		//s = new Socket("10.200.11.181", 5000);
//		s = new Socket("136.167.205.144", 5000);
//		b = new ObjectOutputStream(s.getOutputStream());
//		int cnt = 0;
		
		Delay.msDelay(10);
		while(!Button.ENTER.isDown()){
			sonicSampleProvider.fetchSample(s1, 0);
			//seek = data.getIRValue();
			seek = s1[0];
			LCD.clear();
			LCD.drawString(("angle: " + seek), 0, 0);
			Delay.msDelay(30);
			//Delay.usDelay(10);
//			a = new IRAngle(cnt,seek);
			//System.out.println(seek);
//			b.writeObject(a);
//			b.flush();
//			cnt++;

//			if (Math.abs(seek) <= 120){
//				pilot.rotate(seek);
//			}
				
		}
		
	}

}