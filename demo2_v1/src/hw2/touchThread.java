package hw2;

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
			touched.fetchSample(s1, 0);
			if (s1[0] == 1)
				touchedData.setTouched();
		}
	}
}

/*
 Thread t = new Thread(new touchThread(touchedData, ts));
 
 t.setDaemon(true);
 
 t.start();

 while(true){
 
    if(touchedData.getTouched()){
        left.stop(true);
        right.stop();
        Sound.systemSound(true, 3);
 left.rotate(-360, true);
 right.rotate(360);
 left.resetTachoCount();
 right.resetTachoCount();
 left.startSynchronization();
 degrees = (int) Math.round((((robotTrack * Math.PI) / 2.0) / wheelCircunference) * 360.0)
 left.rotate((int) degrees, true);
 right.
 left.endSynchronization();
 left.waitComplete();
 right.
 s
    }
 }
*/