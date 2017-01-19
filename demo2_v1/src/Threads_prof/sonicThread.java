package Threads_prof;

import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;


public class sonicThread implements Runnable{
	NXTUltrasonicSensor sonic;
	globalUltrasonicData data;
	
	public sonicThread(globalUltrasonicData data, NXTUltrasonicSensor sonic) {
		this.data = data;
		this.sonic = sonic;
	}

	public void run() {
		SampleProvider sonicSampleProvider = sonic.getDistanceMode();
		float[] s1 = new float[sonicSampleProvider.sampleSize()];
		while (true) {
			try {
				sonicSampleProvider.fetchSample(s1, 0);
			      }
			catch (Exception ex) {}
			data.setSonicValue((int)(s1[0]*100));
		}	
	}
}
