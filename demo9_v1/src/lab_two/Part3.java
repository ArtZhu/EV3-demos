package lab_two;
//correct version
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

//sideSonic side
public class Part3 {
	static double accConst = 2;
	static double turnConst = 1.04;
	static NXTUltrasonicSensor sideSonic;
	static double wheelDiameter = 5.6;
	static double robotTrack = 14.5;
	static RegulatedMotor left ;
	static RegulatedMotor right ;
	static float turnRadius = (float) wheelDiameter / 2; 
	static List<Double> dists = new ArrayList<Double>();
	
	static double robotLength = 24;
	
	
	public static void main(String[] args) {
		Brick brick = BrickFinder.getDefault(); // get specifics about this
												// robot
		//Motors
		left = new NXTRegulatedMotor(brick.getPort("A"));			
		right = new NXTRegulatedMotor(brick.getPort("B"));
		
		//Sensors
		sideSonic = new NXTUltrasonicSensor(SensorPort.S3);
		
		//Ultrasonic
		SampleProvider sideDist = sideSonic.getDistanceMode();
		float[] sideSample = new float[sideDist.sampleSize()];
		Delay.msDelay(500);
		
		LCD.drawString("I'm ready", 0, 0); 
		Button.waitForAnyPress(); 
		LCD.clear();
		
		 // establish a fail-safe: pressing Escape quits
        brick.getKey("Escape").addKeyListener(new KeyListener() {
          //      @Override
                public void keyPressed(Key k) {
    				left.stop(true);
    				right.stop();
        			left.close();
        			right.close();
                    System.exit(1);
                }

             //   @Override
                public void keyReleased(Key k) {
    				left.stop(true);
    				right.stop();
    				left.close();
    				right.close();
                    System.exit(1);
                }
        });

        
        double firstCount = 0;
        
        for(int turns = 0; turns < 1; turns ++){
        	//first go back until sideSensor detects nothing
        	left.setAcceleration(90);
        	right.setAcceleration(90);
        	left.setSpeed(90);
    		right.setSpeed(90);
    		left.backward();
    		right.backward();
    		sideDist.fetchSample(sideSample, 0);
        	int i = 0;
        	while (sideSample[0] < 0.40) {
        		sideDist.fetchSample(sideSample, 0);
    			i++;
    			Delay.msDelay(100);
        	}
        	Delay.msDelay(1000);
        	left.setAcceleration(2880);
        	right.setAcceleration(2880);
        	left.stop(true);
    		right.stop();
        	
    		//then start from where it detects nothing.
        	left.setAcceleration(90);
        	right.setAcceleration(90);
        	left.setSpeed(90);
    		right.setSpeed(90);
    		left.forward();
    		right.forward();
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
        	left.forward();
        	right.forward();
        	while (sideSample[0] < 0.20) {
        		sideDist.fetchSample(sideSample, 0);
    			System.out.println("s[" + i + "]: " + sideSample[0]);
    			i++;
    			Delay.msDelay(100);						
        	}
        		
        	//detects nothing then get TachoCount
        	int leftTacho = left.getTachoCount();
    		int rightTacho = right.getTachoCount();
    		System.out.println(rightTacho);
    		Sound.beep();
    		
    		//go forward for 2.5s so we can turn
    		Delay.msDelay(2500);
    		
    		//then stop
			left.setAcceleration(2880);
			right.setAcceleration(2880);
			left.stop(true);
			right.stop();
			
			//and calculate distance
    		int Tacho = (int) (leftTacho + rightTacho) / 2;	
    		double trvlDist = (double) Tacho / 360.0 * wheelDiameter * Math.PI;
    		double length = trvlDist;// - 1 * firstCount;						//--------
    		System.out.println("Length is " + length);
    		dists.add((Double) length);
    			
 
			//turn
			turn(90);    			
    			
        	}
        //close sensors
    	sideSonic.close();
    		
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
        

	
   	private static void turn(int deg){
   		left.setAcceleration(90);
    	right.setAcceleration(90);
    	double turnAngle = turnConst * (robotTrack * Math.PI * deg / 360) /(wheelDiameter * Math.PI) * 360;
    		
    	left.rotate((int) (-1 * turnAngle), true);
    	right.rotate((int) (1 * turnAngle));	
   	}
}


