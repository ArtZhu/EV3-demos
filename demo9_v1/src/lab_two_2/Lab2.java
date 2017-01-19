package lab_two_2;

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

public abstract class Lab2 {
	protected static Brick brick = BrickFinder.getDefault(); // get specifics about this
											// robot
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
	
	//robot statistics
	protected static double wheelDiameter = 6.88;
	protected static double robotTrack = 17.2;
	protected static double robotLength = 24;
	protected static float turnRadius = (float) wheelDiameter / 2; 
	protected static double wheelCircumference = wheelDiameter * Math.PI;

	
	//part specific instances
	protected static List<Double> dists = new ArrayList<Double>();
		//where the robot should stop and turn 90 degrees
	protected static double desiredTurningDist = robotLength;
	
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
   		int leftTacho = left.getTachoCount();
   		int rightTacho = right.getTachoCount();
   		int Tacho = (int) (leftTacho + rightTacho) / 2;
	
   		double trvlDist = Tacho / 360 * wheelDiameter * Math.PI;
   		return trvlDist;
   	}
   	
	//fix SideUltraSonic sensors 
	protected static double fix(double trvlDist, double sideDist){
		return trvlDist - sideDist + robotLength;
	}
	
	//fix FrontUltraSonic sensors
	protected static double fix2(double trvlDist, double frontDist){
		return trvlDist + frontDist + robotLength;
	}
	
	//calculate the acceleration needed for side motors m/m' to stop at the desiredStopDist   	
	protected static int desiredStopAcc(RegulatedMotor m, double desiredStopDist, double accConst){
		int _speed = m.getSpeed();
		int _acceleration = (int) (_speed * _speed * (wheelDiameter * Math.PI) / (accConst * desiredStopDist * 360));
		return _acceleration;
	}
	
	protected static double sum(List<Double> list){
		double sum = 0.0;
		for(int i = 0; i < list.size(); i++){
			sum += list.get(i); 		
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
	
	//turn a robot clockwise certain degrees
   	protected static void turn(int degree, double turnConst){
    	left.setAcceleration(90);
    	right.setAcceleration(90);
    	double turnAngle = turnConst *  (robotTrack * Math.PI * (degree / 360)) /(wheelDiameter * Math.PI) * 360;
    		
    	left.rotate((int) (-1 * turnAngle), true);
    	right.rotate((int) turnAngle);	
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
   	
/////////////////////////////////
	/*
	 * 
	 * Robot correction methods
	 * 
	 * 
	 */
/////////////////////////////////
   	 	
   	/********************
	/*
	* robot calculation
	* 
	*  
	*
	*/
   	//fix with records
   	protected static double fix3(int speed, int msDelay, double length, List<Double> sideDistList){
		double actualLength = 0;
		double intendedLength = 0;
		double difference = 0;
		double _speed = (speed/360) * Math.PI * wheelDiameter;
		double secDelay = msDelay / 1000;
		for(int i = 1; i < sideDistList.size(); i++){
			Double s1 = sideDistList.get(i-1);
			Double s2 = sideDistList.get(i);

			//create triangle
			Double height = s2 - s1;
			Double hypoteneuse = _speed * secDelay;
			Double _side = Math.sqrt(hypoteneuse * hypoteneuse + height * height);
			
			actualLength += hypoteneuse;
			intendedLength += _side;
			difference += height;
		}
		return intendedLength/actualLength * length + robotLength;
	}
	
	//calculate the angle between robot and the wall on the side of the side sensor	
   	protected static double _angle(List<Double> frontDistList, List<Double> sideDistList){
   		int i = sideDistList.size();
   		int j = frontDistList.size();
   		Double height = 100 * (sideDistList.get(i - 1) - sideDistList.get(i/2));
   		System.out.println(height);
   		Double side  = 100 * (frontDistList.get(j - 1) - frontDistList.get(j/2));
   		System.out.println(side);
   		return Math.atan(height/side) / Math.PI * 180;
   	}
   	
   	/********************
	/*
	* robot movement
	* 
	*  
	*
	*/
   	
   	//moveParallel if robot is at deg degrees, dist distance away
   	protected static void moveParallel(int deg, double dist, double turnConst){
   		turn(90 - deg, turnConst);
   		int rotateAngle = (int) (dist / wheelCircumference * 360);
   		left.rotate(-1 * rotateAngle, true);
   		right.rotate(-1 * rotateAngle);
   		turn(-90, turnConst);
   	}
   	
   	//make robot move along a track
   	//sideSonic on left
   	// sideDistList must at least be of size 2.
   	protected static void alongTrack(List<Double> sideDistList, int stdSpeed){
   		Double std = sideDistList.get(0);
   		Double last = sideDistList.get(sideDistList.size() - 1);
   		Double prev = sideDistList.get(sideDistList.size() - 2);
   		if(last < prev){
   	   		left.setSpeed(stdSpeed + 10);
   	   		right.setSpeed(stdSpeed);
   		}
   		else if(last > prev){
   	   		left.setSpeed(stdSpeed);
   	   		right.setSpeed(stdSpeed + 10);
   		}
   		else{
   			left.setSpeed(stdSpeed);
   			right.setSpeed(stdSpeed);
   		}
   	}
  
}


//fixing part 3

/*
 * 	//if detect something, start recording
        	left.resetTachoCount();
        	right.resetTachoCount();
        		
        	sideDist.fetchSample(sideSample, 0);
        	i = 0;
        	List<Double> sideDistList = new ArrayList<Double>();
        	left.forward();
        	right.forward();
        	while (sideSample[0] < 0.20) {
        		sideDist.fetchSample(sideSample, 0);
    			System.out.println("s[" + i + "]: " + sideSample[0]);
//    			sideDistList.add((double) 100 * sideSample[0]);
    			i++;
    			Delay.msDelay(100);							//what number?
        	}
        		
        	//detects nothing then get Tacho and calculate distance
        	int leftTacho = left.getTachoCount();
    		int rightTacho = right.getTachoCount();
    		int Tacho = (int) (leftTacho + rightTacho) / 2;
        		
    		double trvlDist = Tacho / 360 * wheelDiameter * Math.PI;
    			
    		double length = fix(90, 100, trvlDist, sideDistList);
    		System.out.println("Length is " + length);
    		dists.add((Double) length);
    			
			left.setAcceleration(2880);
			right.setAcceleration(2880);
			left.stop(true);
			right.stop(); 
				
				//adjust the robot
				//?? speed
				int robotAngle = (int) _angle(90, 100, sideDistList);
				moveParallel(robotAngle, sideDistList.get(sideDistList.size() - 1) - sideDistList.get(sideDistList.size() - 2));
    			
				//turn
				turn(90);    			
    			
        	}
    		sideSonic.close();
    		
    		Double lengthSum = 0.0;
    		for(int x = 0; x < dists.size(); x ++){
    			lengthSum += dists.get(x);
    		}
    		
    		System.out.println("totalLength is " + lengthSum);
    		System.exit(0);
    }
        

	
   	private static void turn(int deg){
    	left.setAcceleration(90);
    	right.setAcceleration(90);
    	double turnAngle = (robotTrack * Math.PI * deg / 360) /(wheelDiameter * Math.PI) * 360;
    		
    	left.rotate((int) (-1 * turnAngle), true);
    	right.rotate((int) turnAngle);	
   	}
   	
	private static int desiredStopAcc(RegulatedMotor m, double desiredStopDist){
		int _speed = m.getSpeed();
		int _acceleration = (int) (_speed * _speed / ((accConst * desiredStopDist / (wheelDiameter * Math.PI)) * 360));
		return _acceleration;
	}
	
	
   */
