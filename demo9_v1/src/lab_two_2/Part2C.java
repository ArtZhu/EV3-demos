package lab_two_2;

import lab_two.globalData;
import lab_two.touchThread;
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

import java.util.*;

public class Part2C extends Lab2{
	//touchSensor on back side
	//sideSonic on left
	//frontSonic front
	//Sensors
	private static double turnConst = 0.905;// 0.905 for wood, 1.083 for carpet
	
	public static void main(String[] args) {
		touchThreadInit();
		
		setupSafeButton();
		
		ifReady("2");

		double _frontDist = 0;

		while(true){
			for(int turns = 0; turns < 4; turns ++){
				set(90, 90);
				backward();

				frontDist.fetchSample(frontSample, 0);
				int i = 0;
				while (frontSample[0] > 0.20) {
					if(touchedData.getTouched()){
						Sound.beep();
						stopImm();
						left.resetTachoCount();
						right.resetTachoCount();
						set(135, 270);
						forward();
						frontDist.fetchSample(frontSample, 0);
						Delay.msDelay(1000);

					}

					touchedData.resetTouched();
					frontDist.fetchSample(frontSample, 0);
					System.out.println("f[" + i + "]: " + frontSample[0]);
					i++;
					Delay.msDelay(100);				
				}

				//get TachoCount, get front distance, and calculate length 
				double trvlDist = travelDistance();
				_frontDist = 100 * frontSample[0];
				double length = fix2(trvlDist, _frontDist);

				//then stop and print
				stopImm();
				System.out.println("Length is " + length);
				dists.add((Double) length);

				//finish up and turn
				turn(90, turnConst);											
			}
			//after all 4 turns are done
			closeSensors();

			Double lengthSum = 0.0;
			for(int x = 0; x < dists.size(); x++){
				lengthSum += dists.get(x);
				System.out.println(dists.get(x));
			}
			System.out.println("totalLength is " + lengthSum);

			//Display it for 20s
			Delay.msDelay(20000);
			System.exit(0);
		}
	}	
}
