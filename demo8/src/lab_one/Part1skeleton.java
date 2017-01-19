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
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.Move;
import lejos.utility.Delay;
import lejos.hardware.Button;

public class Part1skeleton {
	static double wheelDiameter = 5.6;
	static double robotTrack = 16.7;
	static RegulatedMotor left;
	static RegulatedMotor right;
	static NXTUltrasonicSensor sonic;
	static float turnRadius = (float) wheelDiameter / 2; // wheel diameter is
															// 5.6 cm (wheel is
															// marked 56 mm)

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Get an instance of the Ultrasonic sensor
		Brick brick = BrickFinder.getDefault();

		left = new NXTRegulatedMotor(brick.getPort("B"));
		right = new NXTRegulatedMotor(brick.getPort("A"));

		Button.waitForAnyPress();

		left.setSpeed(360);
		right.setSpeed(360);
		left.setAcceleration(100);
		right.setAcceleration(100);

		sonic = new NXTUltrasonicSensor(SensorPort.S2);
		// get an instance of this sensor in measurement mode
		SampleProvider distance = sonic.getDistanceMode();

	//	left.synchronizeWith(new RegulatedMotor[] {right});
	//	left.startSynchronization();
		left.forward();
		right.forward();

		// Initialize an array of floats for fetching samples
		float[] sample = new float[distance.sampleSize()];
		Delay.msDelay(500);// done just to bring US sensor to equilibrium state

		distance.fetchSample(sample, 0);
		int i = 0;
		while (sample[0] < 0.5) {
			distance.fetchSample(sample, 0);
			System.out.println("d[" + i + "]: " + sample[0]);
			i++;
			Delay.msDelay(100);
		}
		
		left.setAcceleration(2880);
		right.setAcceleration(2880);
		left.stop(true);
		right.stop();

	//	left.endSynchronization();

		sonic.close();// important
	}

}

