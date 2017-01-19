package lab_one;
//correctcopy
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
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
import lejos.robotics.navigation.Move;
import lejos.utility.Delay;
import lejos.hardware.Button;

public class Part3 {
		public static NXTTouchSensor ts;
		static NXTUltrasonicSensor sonic;
		static DifferentialPilot pilot;
		static double wheelDiameter = 6.88;
		static double robotTrack = 13.0;
		static RegulatedMotor left ;
		static RegulatedMotor right ;
		static float turnRadius = (float) wheelDiameter / 2;  //wheel diameter is 5.6 cm (wheel is marked 56 mm)
	 
		public static void main(String[] args) {
			double wheelCircunference = wheelDiameter * Math.PI;
			float degrees;
			Brick brick = BrickFinder.getDefault(); // get specifics about this
													// robot
			//Motors
			left = new NXTRegulatedMotor(brick.getPort("A"));			
			right = new NXTRegulatedMotor(brick.getPort("B"));
			
			pilot = new DifferentialPilot(wheelDiameter, robotTrack, left, right);
			
			//Sensors
			ts = new NXTTouchSensor(SensorPort.S3);
			sonic = new NXTUltrasonicSensor(SensorPort.S2);
			
			//Ultrasonic
			SampleProvider distance = sonic.getDistanceMode();
			float[] sample = new float[distance.sampleSize()];
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
			// Delay.msDelay(5000);
			
			LCD.drawString("Touch Test", 0, 0); // Writes program name to LCD
			
			//first move
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

	        pilot.setAcceleration(4);
	        pilot.setTravelSpeed(15);
	        pilot.forward();
	        
	        while(true){
	        	distance.fetchSample(sample, 0);
	    		int i = 0;
	    		while (sample[0] < 0.40) {
	    			if(touchedData.getTouched()){	
						pilot.setAcceleration(64);
		        		turn();
		        		
		        		pilot.forward();
	    			}
	    			touchedData.resetTouched();
	    			distance.fetchSample(sample, 0);
	    			System.out.println("d[" + i + "]: " + sample[0]);
	    			i++;
	    			Delay.msDelay(100);
	    		}
	    		

	    		left.stop(true);
	    		right.stop();
	    		sonic.close();
	    		System.exit(0);
	        }

		}
		
		private static void turn(){
			pilot.stop();
//			Sound.beep();
			pilot.setAcceleration(4);
			double backWardAngle = - 10/(Math.PI * wheelDiameter) * 360;
			
			left.rotate((int) backWardAngle, true);
			right.rotate((int) backWardAngle);
			
//			Sound.beep();
			pilot.setRotateSpeed(30);
			pilot.rotate(90);	
		}
		
		
		
}