package Threads_prof;

import lejos.hardware.sensor.NXTTouchSensor;
import lejos.robotics.SampleProvider;

public class touchThread implements Runnable {
	private globalData touchedData;
	private NXTTouchSensor ts;
	
	public touchThread(globalData touchedData, NXTTouchSensor ts) {
		this.touchedData = touchedData;
		this.ts = ts;
	}

	public void run() {
		SampleProvider touched = ts.getTouchMode();
		float[] s1 = new float[touched.sampleSize()];
		while (true) {
			try {
				touched.fetchSample(s1, 0);
			      }
			catch (Exception ex) {}
			if (s1[0] == 1)
				touchedData.setTouched();
		}
	}
}
