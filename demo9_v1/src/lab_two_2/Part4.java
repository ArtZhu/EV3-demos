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

public class Part4 extends Lab2 {
	private static double turnConst = 0.905;
	
	public static void main(String[] args) {
		touchThreadInit();
		
		setupSafeButton();
		
		ifReady("4");
        
		double _prevSideDist = 0;
	    double _sideDist = 0;
        double _frontDist = 0;
        
        label1:
        while(true){
        	//before starting P2
        	set(90, 90);
    		forward();
    		
    		//records
    		List<Double> _frontSamples = new ArrayList<Double>();
        	List<Double> _sideSamples = new ArrayList<Double>();
        	frontDist.fetchSample(frontSample, 0);
    		sideDist.fetchSample(sideSample, 0);
    		int i = 0;
    		
    		//nothing detected in front
    		while (frontSample[0] > 0.30) {
            	frontDist.fetchSample(frontSample, 0);
        		sideDist.fetchSample(sideSample, 0);
    			System.out.println("f[" + i + "]: " + frontSample[0]);
				System.out.println("s[" + i + "]: " + sideSample[0]);
				
				//records
				_sideSamples.add((double) sideSample[0]);
				_frontSamples.add((double) frontSample[0]);
    			i++;
    			Delay.msDelay(100);						
    		}
    		
    		//detected something in front
    		//then stop
			stopImm();
			
			//if nothing detected on the sides
    		if(_sideSamples.size() == 0 || _sideSamples.get(_sideSamples.size() - 1) >= 1.0){
    			//backup a bit then turn 90 degrees
    			set(90, 90);
    			backward();
    			Delay.msDelay(1000);
    			stopImm();
    			set(90, 90);
    			turn(-1 * 90, turnConst);
    			continue label1;
    		}
    		
    		//if something detected on the sides
    		else{
    			double turnAngle = _angle(_frontSamples, _sideSamples);
				System.out.println(turnAngle);
    			turn((int) turnAngle, turnConst);
    		}
        	
    		Sound.beep();
    		Sound.beep();
        	//double beep then start P2
    		for(int turns = 0; turns < 4; turns ++){
				set(90, 90);
				backward();

				frontDist.fetchSample(frontSample, 0);
				i = 0;
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
