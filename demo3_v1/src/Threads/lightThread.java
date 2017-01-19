package Threads;

import lejos.hardware.sensor.NXTColorSensor;
import lejos.robotics.SampleProvider;


public class lightThread implements Runnable{
	NXTColorSensor light;
	globalLightData gld;
	
	public lightThread(globalLightData gld, NXTColorSensor light) {
		this.gld = gld;
		this.light = light;
	}

	public void run() {
		SampleProvider lightSampleProvider = light.getRGBMode();
		float[] s1 = new float[lightSampleProvider.sampleSize()];
		while (true) {
			try {
				lightSampleProvider.fetchSample(s1, 0);
			      }
			catch (Exception ex) {}
			gld.setLightValue((int)(s1[0]*100));
		}
		
	}

}
