package lab_two;
//correct version
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

import java.util.*;
//touchSensor on back side
//frontSonic front
public class Part2 {
	static double accConst = 2.0;
	static double turnConst = 0.905;
	public static NXTTouchSensor ts;
	static NXTUltrasonicSensor frontSonic;
	static double wheelDiameter = 5.6;
	static double robotTrack = 14.5;
	static double robotLength = 24;							//entire robot length with back TouchSensor pushed in
	static RegulatedMotor left ;
	static RegulatedMotor right ;
	static float turnRadius = (float) wheelDiameter / 2; 
	static List<Double> dists = new ArrayList<Double>();
	
	
	public static void main(String[] args) {
		Brick brick = BrickFinder.getDefault(); // get specifics about this
												// robot
		//Motors
		left = new NXTRegulatedMotor(brick.getPort("A"));			
		right = new NXTRegulatedMotor(brick.getPort("B"));
		
		//Sensors
		ts = new NXTTouchSensor(SensorPort.S4);
		frontSonic = new NXTUltrasonicSensor(SensorPort.S2);
		
		//Ultrasonic
		SampleProvider frontDist = frontSonic.getDistanceMode();
		float[] frontSample = new float[frontDist.sampleSize()];
		Delay.msDelay(500);

		//touchSensor
		globalData touchedData = new globalData(); // set up global data for
													// touch thread
		Thread t = new Thread(new touchThread(touchedData, ts)); // create touch
																	// thread
		t.setDaemon(true); // when the main thread terminates, the created
							// thread terminates
							// if this is false, the created thread continues
							// when the main thread terminates
		t.start();
		
		LCD.drawString("I'm ready", 0, 0); // Writes program name to LCD
		Button.waitForAnyPress(); // Waits for button press
		LCD.clear();
		
		
		 // establish a fail-safe: pressing Escape quits
        brick.getKey("Escape").addKeyListener(new KeyListener() {
          //      @Override
                public void keyPressed(Key k) {
    				left.stop(true);
    				right.stop();
                   	ts.close();
        				left.close();
        				right.close();
                        System.exit(1);
                }

             //   @Override
                public void keyReleased(Key k) {
    				left.stop(true);
    				right.stop();
                	ts.close();
    				left.close();
    				right.close();
                    System.exit(1);
                }
        });

        double _frontDist = 0;
        
        while(true){
        	for(int turns = 0; turns < 4; turns ++){
        		left.setAcceleration(90);		//change
                right.setAcceleration(90);		//change
                left.setSpeed(180);				//change
                right.setSpeed(180);				//change
                left.backward();				
                right.backward();
        	
        		frontDist.fetchSample(frontSample, 0);
        		int i = 0;
        		while (frontSample[0] > 0.20) {
        			//if back hit wall then
        			if(touchedData.getTouched()){
                      //I hit a wall!
        				Sound.beep();
        				
    					left.setAcceleration(2880);
    					right.setAcceleration(2880);
    	        		left.stop(true);
    	        		right.stop();
    	        		// stopped then reset TachoCount
    	        		left.resetTachoCount();
    	        		right.resetTachoCount();
    	        		// after resetting TachoCount, go again
    	        		left.setAcceleration(135);		//change
    	                right.setAcceleration(135);		//change
    	                left.setSpeed(270);				//change
    	                right.setSpeed(270);				//change
    	        		left.forward();
    	        		right.forward();

    	        		frontDist.fetchSample(frontSample, 0);
    	        		Delay.msDelay(1000);
             
        			}
        			
        			touchedData.resetTouched();
        			frontDist.fetchSample(frontSample, 0);
        			System.out.println("f[" + i + "]: " + frontSample[0]);
    				i++;
    				Delay.msDelay(100);				
        		}
    		
        		
        		//if ever the robot is less than 25cm to the wall in front
        		
        		//get TachoCount and front distance
        		int leftTacho = left.getTachoCount();
    			int rightTacho = right.getTachoCount();
        		_frontDist = 100 * frontSample[0];
    			
        		//then stop
    			left.setAcceleration(2880);
    			right.setAcceleration(2880);
    			left.stop(true);
    			right.stop();
    			
    			//calculation
    			int Tacho = (int) (leftTacho + rightTacho) / 2;
    			double trvlDist = (double) Tacho / 360.0 * wheelDiameter * Math.PI;
    			double length = fix(trvlDist, _frontDist);
    			
    			System.out.println("Length is " + length);
    			
    			dists.add((Double) length);
    			
    			//after finishing calculations, turn
    			turn(90);											// change
        	}
        	//after all 4 turns are done
    		frontSonic.close();
    		
    		Double lengthSum = 0.0;
    		for(int x = 0; x < dists.size(); x ++){
    			lengthSum += dists.get(x);
    			System.out.println(dists.get(x));
    	}
    	System.out.println("totalLength is " + lengthSum);
    		
    		//Display it for 20s
    		Delay.msDelay(20000);
    		
    		System.exit(0);
        }
        

	}
	
	//clockwise
	
   	private static void turn(int degree){
    	left.setAcceleration(360);
    	right.setAcceleration(360);
    	//
    	double turnAngle = turnConst * (robotTrack * Math.PI * degree / 360) /(wheelDiameter * Math.PI) * 360;
		
    	left.rotate((int) (1 * turnAngle), true);
    	right.rotate((int) (-1 * turnAngle));	
   	}
   	
   	
   	
	private static int desiredStopAcc(RegulatedMotor m, double desiredStopDist){
		int _speed = m.getSpeed();
		int _acceleration = (int) (_speed * _speed / ((accConst * desiredStopDist / (wheelDiameter * Math.PI)) * 360));
		return _acceleration;
	}
	
	private static double fix(double length, double frontDist){
		return length + frontDist + robotLength;
	}

}
