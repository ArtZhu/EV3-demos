

import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.Pose;


public class sonicThread implements Runnable{
	NXTUltrasonicSensor sonic;
	globalUltrasonicData data;
	final int dir;
	private int left = 0;
	private int front = 1;
	
	public sonicThread(globalUltrasonicData data, NXTUltrasonicSensor sonic, int _dir) {
		this.data = data;
		this.sonic = sonic;
		dir = _dir;
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
			if((int)(s1[0]*100) < 30)
				add();
		}
		
	}
	
	private void add(){
		
		Pose p = Seeker1.poseProvider.getPose();
		
		int sector = Seeker1.f1(p.getHeading());
		Point ad = Point.actualPoint(p.getX(), p.getY());
		
		
		if(dir != left){
			if(sector == 0 || sector == 7){
				ad = new Point(ad.X + 1, ad.Y);
			}
			if(sector == 1 || sector == 2){
				ad = new Point(ad.X, ad.Y + 1);
			}
			if(sector == 3 || sector == 4){
				ad = new Point(ad.X - 1, ad.Y);
			}
			if(sector == 5 || sector == 6){
				ad = new Point(ad.X, ad.Y + 1);
			}
		}
		else{
			if(sector == 0 || sector == 7){
				ad = new Point(ad.X, ad.Y + 1);
			}
			if(sector == 1 || sector == 2){
				ad = new Point(ad.X - 1, ad.Y);
			}
			if(sector == 3 || sector == 4){
				ad = new Point(ad.X, ad.Y - 1);
			}
			if(sector == 5 || sector == 6){
				ad = new Point(ad.X + 1, ad.Y );
			}
		}
		
		
		Seeker1.addCount(ad);
//		Seeker.count.remove(ad);
//		Seeker.count.add(ad);
		if(dir == left)
			System.out.println("left");
		else
			System.out.println("front");
		System.out.println("trying to add" + ad);
		
		Seeker1.InitialGraph[ad.X][ad.Y] = Seeker1.cup;
	}
}
