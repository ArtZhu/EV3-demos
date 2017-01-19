package Threads;

import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.robotics.SampleProvider;



public class EOPDThread implements Runnable{
	HiTechnicEOPD EOPD;
	globalEOPDData data;
	
	public EOPDThread(globalEOPDData data, HiTechnicEOPD EOPD) {
		this.data = data;
		this.EOPD = EOPD;
	}

	public void run() {

		SampleProvider sonicSampleProvider = EOPD.getShortDistanceMode();
		float[] s1 = new float[sonicSampleProvider.sampleSize()];
		while (true) {
			try {
				sonicSampleProvider.fetchSample(s1, 0);
			      }
			catch (Exception ex) {}
			data.setEOPDValue((int)(s1[0]*100));
		}
		
	
		
	}

}
