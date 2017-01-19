package lab_two_2;

import java.util.ArrayList;
import java.util.List;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Part3C extends Lab2{
	private static double turnConst = 1.083;  					//0.905 on wood; 1.083 on carpet;
	
	public static void main(String[] args) {
		setupSafeButton();
		
		ifReady("3");

		double firstCount = 0;

		for(int turns = 0; turns < 4; turns ++){
			//first go back until sideSensor detects nothing
			set(90, 90);
			backward();
			sideDist.fetchSample(sideSample, 0);
			int i = 0;
			while (sideSample[0] < 0.40) {
				sideDist.fetchSample(sideSample, 0);
				i++;
				Delay.msDelay(100);
			}
			Delay.msDelay(1000);
			stopImm();

			//then start from where it detects nothing.
			set(90, 90);
			forward();
			sideDist.fetchSample(sideSample, 0);
			i = 0;
			while (sideSample[0] > 0.20) {
				sideDist.fetchSample(sideSample, 0);
				i++;
				Delay.msDelay(100);
			}

			//if detect something, start recording
			Sound.beep();
			left.resetTachoCount();
			right.resetTachoCount();

			sideDist.fetchSample(sideSample, 0);
			firstCount = sideSample[0];
			i = 0;
			forward();
			while (sideSample[0] < 0.20) {
				sideDist.fetchSample(sideSample, 0);
				System.out.println("s[" + i + "]: " + sideSample[0]);
				i++;
				Delay.msDelay(100);						
			}

			//detects nothing then get TachoCount
			double trvlDist = travelDistance();
			Sound.beep();

			//go forward for 2.5s so we can turn
			Delay.msDelay(2500);

			//then stop
			stopImm();

			//and calculate distance
			double length = trvlDist;						//--------
			System.out.println("Length: " + length);
			dists.add((Double) length);


			//turn
			turn(90, turnConst);    			

		}
		//close sensors
		closeSensors();

		//sum up the distance and print it
		Double lengthSum = 0.0;
		for(int x = 0; x < dists.size(); x ++){
			lengthSum += dists.get(x);
			System.out.println(dists.get(x));
		}
		System.out.println("totalLength is " + lengthSum);

		//let it display for 20s
		Delay.msDelay(20000);
		System.exit(0);
	}

}
   	