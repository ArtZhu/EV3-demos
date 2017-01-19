package lab_one;
//correctcopy
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
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
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Move;
import lejos.utility.Delay;
import lejos.hardware.Button;

public class Part2 {
		public static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S4);
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
			left = new NXTRegulatedMotor(brick.getPort("A"));			
			right = new NXTRegulatedMotor(brick.getPort("B"));
			
			pilot = new DifferentialPilot(wheelDiameter, robotTrack, left, right);

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

		
			int i = 0;

	        pilot.setTravelSpeed(10);
	        pilot.setAcceleration(4);
	        pilot.forward();
	        
	        label1:
			while (true){

				//System.out.println("d[" + i + "]: " + touchedData.getTouched());
				if (touchedData.getTouched()) {//what to do if touched?
					pilot.setAcceleration(32);
					pilot.stop();
					Sound.beep();
					
					double backWardAngle = - 13/(Math.PI * wheelDiameter) * 360;
					
					left.rotate((int) backWardAngle, true);
					right.rotate((int) backWardAngle);
						
					Sound.beep();
					pilot.setRotateSpeed(60);
					pilot.rotate(175);
					pilot.forward();	
					
					Delay.msDelay(2000);
					break label1;
					
				}
				touchedData.resetTouched();
				i++;
			}
			
			
		}


}
