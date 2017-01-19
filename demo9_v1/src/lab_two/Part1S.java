package lab_two;
//correct version
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

public class Part1S {
	//TouchSensor in the back
	//UltraSonic side
	static double accConst = 1.7;
	public static NXTTouchSensor ts;
	static NXTUltrasonicSensor sideSonic;
	static double wheelDiameter = 5.6;
	static double robotTrack = 14.5;
	static double robotLength = 17;							//robotLength is the length
															//	  from the back with touchsensor pushed in 
															//    to the middle of the side sensor
	static RegulatedMotor left ;
	static RegulatedMotor right ;
	static float turnRadius = (float) wheelDiameter / 2;  //wheel diameter is 5.6 cm (wheel is marked 56 mm)
 
	public static void main(String[] args) {
		Brick brick = BrickFinder.getDefault(); // get specifics about this
												// robot
		//Motors
		left = new NXTRegulatedMotor(brick.getPort("A"));			
		right = new NXTRegulatedMotor(brick.getPort("B"));
		
		//Sensors
		ts = new NXTTouchSensor(SensorPort.S4);
		sideSonic = new NXTUltrasonicSensor(SensorPort.S3);
		
		//Initialize Ultrasonic
		SampleProvider sideDistance = sideSonic.getDistanceMode();
		float[] sideSample = new float[sideDistance.sampleSize()];
		Delay.msDelay(500);

		//Initialize touchSensor
		globalData touchedData = new globalData(); // set up global data for
													// touch thread
		
		Thread t = new Thread(new touchThread(touchedData, ts)); // create touch
																	// thread
		t.setDaemon(true); // when the main thread terminates, the created
							// thread terminates
							// if this is false, the created thread continues
							// when the main thread terminates
		t.start();
		// Delay.msDelay(5000);
		
		
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

        
        left.setAcceleration(90);		//change
        right.setAcceleration(90);		//change
        left.setSpeed(90);				//change
        right.setSpeed(90);				//change
        left.backward();				//synchronize?
        right.backward();
        
        while(true){
        	sideDistance.fetchSample(sideSample, 0);
    		int i = 0;
    		double firstCount = 100 * sideSample[0];
    		while (sideSample[0] < 0.40) {
    			//if hit wall
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
	        		left.setAcceleration(90);		//change
	                right.setAcceleration(90);		//change
	                left.setSpeed(90);				//change
	                right.setSpeed(90);
	        		left.forward();
	        		right.forward();
	        		left.waitComplete();
	        		right.waitComplete();
	        //		Delay.msDelay(1500);
    			}
    			
    			touchedData.resetTouched();
    			sideDistance.fetchSample(sideSample, 0);
    			System.out.println("d[" + i + "]: " + sideSample[0]);
    			i++;
    			Delay.msDelay(100);
    		}
    		
    		int leftTacho = left.getTachoCount();
       		int rightTacho = right.getTachoCount();
       		int Tacho = (int) (leftTacho + rightTacho) / 2;
    	
       		double trvlDist = (double) Tacho / 360.0 * wheelDiameter * Math.PI;
    		double length = fix(trvlDist, firstCount);
    		System.out.println("Length is " + length);

			left.setAcceleration(2880);
			right.setAcceleration(2880);
    		left.stop(true);
    		right.stop();
    		
    		sideSonic.close();
    		
    		Delay.msDelay(20000);
    		System.exit(0);
        }

	}
	
	private static double fix(double length, double sideDist){
		return length - sideDist + robotLength;					
	}

}
