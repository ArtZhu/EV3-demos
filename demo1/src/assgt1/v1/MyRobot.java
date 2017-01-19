package assgt1.v1;

import java.util.ArrayList;
import java.util.List;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/*
 * EVERYTHING MUST BE IN CM!
 */
public abstract class MyRobot {
	//Brick
	protected static Brick brick = BrickFinder.getDefault(); // get specifics about this
	
	//sensors and motors
	protected static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S4);
	protected static NXTUltrasonicSensor frontSonic = new NXTUltrasonicSensor(SensorPort.S2);
	protected static NXTUltrasonicSensor sideSonic = new NXTUltrasonicSensor(SensorPort.S3);
	protected static RegulatedMotor left = new NXTRegulatedMotor(brick.getPort("A"));
	protected static RegulatedMotor right = new NXTRegulatedMotor(brick.getPort("B"));

	//samples
	protected static SampleProvider frontDist = frontSonic.getDistanceMode();
	protected static SampleProvider sideDist = sideSonic.getDistanceMode();
	protected static float[] frontSample = new float[frontDist.sampleSize()];
	protected static float[] sideSample = new float[sideDist.sampleSize()];

	//tsData
	protected static globalData touchedData = new globalData();

	//position records
	protected static position _position = new position();
	protected static List<Double> frontDistList = new ArrayList<Double>();
	protected static List<Double> sideDistList = new ArrayList<Double>();
	
	//robot statistics
	protected static double wheelDiameter = 5.6;
	protected static double robotTrack = 14.5;
	protected static double robotLength = 24;
	protected static double wheelCircumference = wheelDiameter * Math.PI;

	//specific instances
	protected final static double woodTurnConst = 0.905;
	protected final static double carpetTurnConst = 1.083;
	
	
////////////////////////////////////////////////////////////////
/*
* Starts shared methods here
* 
*  
*/
////////////////////////////////////////////////////////////////

	////////////////////////////
	/*
	 * 
	 * Robot normal methods
	 * 
	 * 
	 */
	////////////////////////////
		/********************
		/*
		 * Initialization methods
		 * 
		 *  
		 *
		 */

	//initialize touchThread
	protected static void touchThreadInit(){
		//touchSensor		
		Thread t = new Thread(new touchThread(touchedData, ts)); // create touch
					// thread
		t.setDaemon(true); // when the main thread terminates, the created
		// thread terminates
		// if this is false, the created thread continues
		// when the main thread terminates
		t.start();
	}

	// establish a fail-safe: pressing Escape quits
	protected static void setupSafeButton(){        
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
	}

	//print ready and wait for button
	protected static void ifReady(String name){
		String msg = "I'm " + name + "; I'm ready!";
		LCD.drawString(msg, 0, 0); // Writes program name to LCD

		Button.waitForAnyPress(); // Waits for button press

		LCD.clear();
	}	
	
	/********************
	/*
	* robot calculations
	* 
	*  
	*
	*/	

	//calculate travelDistance
   	protected static double travelDistance(){
   		double leftTacho = left.getTachoCount();
   		double rightTacho = right.getTachoCount();
   		double Tacho =(leftTacho + rightTacho) / 2.0;
   		System.out.println(Tacho);
   		double trvlDist = Tacho / 360.0 * wheelDiameter * Math.PI;
   		return trvlDist;
   	}
   	
	//calculate the acceleration needed for side motors m/m' to stop at the desiredStopDist   	
	protected static int desiredStopAcc(RegulatedMotor m, double desiredStopDist, double accConst){
		int _speed = m.getSpeed();
		int _acceleration = (int) (_speed * _speed * (wheelDiameter * Math.PI) / (accConst * desiredStopDist * 360));
		return _acceleration;
	}
	
	//sum up a list of distances. display each while summing up
	protected static double sum(List<Double> list, String msg){
		double sum = 0.0;
		for(int i = 0; i < list.size(); i++){
			sum += list.get(i); 		
			System.out.println(msg + "[" + i + "]: " + list.get(i));
		}
		return sum;
	}
	
	/********************
	/*
	* robot movements
	* 
	*  
	*
	*/
	public static void waitComplete(){
		left.waitComplete();
		right.waitComplete();
	}
	
	//turn a robot counterclockwise if positive degrees
   	public static void turn(int degree, double turnConst){
    	set(90, 90);
    	double turnAngle = turnConst *  (robotTrack * Math.PI * (degree / 360.0)) /(wheelDiameter * Math.PI) * 360.0;
    	
   		left.synchronizeWith(new RegulatedMotor[] {right});
   		left.startSynchronization();	
    	left.rotate((int) (-1 * turnAngle), true);
    	right.rotate((int) turnAngle);	
    	left.endSynchronization();
   	}
   	
   	//
   	public static void turn1Wheel(int degree, double turnConst, String wheel){
    	set(90, 90);
    	double turnAngle = turnConst *  (robotTrack * 2 * Math.PI * (degree / 360.0)) /(wheelDiameter * Math.PI) * 360.0;
    	System.out.println(turnAngle);
    	if(wheel == "left"){
    		left.rotate((int) turnAngle); 
    		waitComplete();
    	}
    	if(wheel == "right"){
    		right.rotate((int) turnAngle);    
    		waitComplete();
    	}
   	}
   	
   	
   	//stop immediately
   	protected static void stopImm(){
		left.setAcceleration(2880);
		right.setAcceleration(2880);
		left.stop(true);
		right.stop();
   	}
   	
   	//set acceleration and speed
   	protected static void set(int acceleration, int speed){
		left.setAcceleration(acceleration);
		right.setAcceleration(acceleration);
		left.setSpeed(speed);
		right.setSpeed(speed);
		
   	}
   	
   	//forward()
   	protected static void forward(){
   		left.synchronizeWith(new RegulatedMotor[] {right});
   		left.startSynchronization();
   		left.forward();
   		right.forward();
   		left.endSynchronization();
   	}
   	
   	//backward()
   	protected static void backward(){
   		left.synchronizeWith(new RegulatedMotor[] {right});
   		left.startSynchronization();
   		left.backward();
   		right.backward();
   		left.endSynchronization();
   	}
   	
   	//rotate()
   	protected static void rotate(int angle){
   		left.synchronizeWith(new RegulatedMotor[] {right});
   		left.startSynchronization();
   		left.rotate(angle, true);
   		right.rotate(angle);
   		left.endSynchronization();
   	}
   	
   	//dance
   	protected static void dance1(int sec){
   		left.synchronizeWith(new RegulatedMotor[] {right});
   		left.startSynchronization();
   		set(90, 180);
   		left.forward();
   		right.backward();
   		Delay.msDelay(sec * 1000);
   		left.endSynchronization();
   	}
   	
   	/********************
	/*
	* robot close
	* 
	*  
	*
	*/
   	
   	protected static void closeSensors(){
   		frontSonic.close();
   		sideSonic.close();
   		ts.close();
   	}
   	
////////////////////////////////////////////////////////////////
/*
* Starts get****** methods here
* 
*  
*/
////////////////////////////////////////////////////////////////
   	public static double getWheelCircumference(){
   		return wheelCircumference;
   	}
   	
   	public static List<Double> getSideDistList(){
   		return sideDistList;
   	}
}
