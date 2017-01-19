package lab_two_2;

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
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Part1 extends Lab2{
	
	public static void go(){
		touchThreadInit();
		
	    setupSafeButton();
	    
	    ifReady("1");

        set(90, 90);
        backward();
        
        while(true){
        	sideDist.fetchSample(sideSample, 0);
    		int i = 0;
    		double _sideDist = 0;
    		double prevSideDist = 0;
    		while (sideSample[0] < 0.40) {
    			//if back hit wall
    			if(touchedData.getTouched()){	
    				Sound.beep();
					stopImm();
	        		left.resetTachoCount();
	        		right.resetTachoCount();
	        		set(90, 90);
	        		forward();
	        		Delay.msDelay(1500);
    			}
    			touchedData.resetTouched();
    			sideDist.fetchSample(sideSample, 0);
    			prevSideDist = _sideDist;
    			_sideDist = 100 * sideSample[0];
    			System.out.println("d[" + i + "]: " + _sideDist);
    			i++;
    			Delay.msDelay(100);
    		}
    		

    		double trvlDist = travelDistance();
    		double length = fix(trvlDist, prevSideDist);
    		System.out.println("Length: " + length);

			left.resetTachoCount();
			right.resetTachoCount();
			stopImm();
    		
    		closeSensors();
    		
    		Delay.msDelay(20000);
    		System.exit(0);
        }

	}
		
	
	public static void main(String[] args) {
		go();
	}
}
