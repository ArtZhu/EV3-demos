package Threads;

import lejos.hardware.sensor.HiTechnicIRSeekerV2;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class IRThread implements Runnable {
	HiTechnicIRSeekerV2 IR;
	globalIRData data;

	public IRThread(globalIRData data, HiTechnicIRSeekerV2 IR) {
		this.data = data;
		this.IR = IR;
	}

	@Override
	public void run() {
		SampleProvider sonicSampleProvider = IR.getModulatedMode();
		float[] s1 = new float[sonicSampleProvider.sampleSize()];
		while (true) {
			try {
				sonicSampleProvider.fetchSample(s1, 0);
				data.setIRValue((s1[0]));
				Delay.msDelay(30);
			} catch (Exception ex) {
			}
		}

	}

}