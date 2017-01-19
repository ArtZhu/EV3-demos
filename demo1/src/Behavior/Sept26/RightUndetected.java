package Behavior.Sept26;

import lejos.hardware.Sound;
import lejos.utility.Delay;
import assgt1.v1.*;

public class RightUndetected extends MyRobot implements Behavior{
	private Priority priority = new Priority(1);
	private double turnConst;
	private double stdDist;
	
	public RightUndetected(double _turnConst, double _stdDist){
		turnConst = _turnConst;
		stdDist = _stdDist;
	}
	
	@Override
	public void behave() {
//		Sound.beep();
		_position.update(robotTrack / 2.0 + stdDist);
		_position.turnRight();
		_position.update(robotTrack / 2.0);
		
		//
		stopImm();
		
		set(90, 90);
		rotate((int) (stdDist/ wheelCircumference * 360));
		waitComplete();
		
		set(90, 90);
		turn1Wheel(90, turnConst, "left");
		waitComplete();
		
		set(90, 90);
		forward();
		Sound.beep();
		frontDist.fetchSample(frontSample, 0);
		sideDist.fetchSample(sideSample, 0);
		int i2 = 0;
		while(sideSample[0] > 0.25){
			if(_position.isBack()){
				Sound.systemSound(true, 4);
				stopImm();
				closeSensors();
//				dance1(2);
				System.out.println("done");
				Delay.msDelay(20000);
				System.exit(0);
			}

			
			left.resetTachoCount();
			right.resetTachoCount();
			
			frontDist.fetchSample(frontSample, 0);
			sideDist.fetchSample(sideSample, 0);
//			System.out.println("f[" + i2 + "]: " + frontSample[0]);
//			System.out.println("s[" + i2 + "]: " + sideSample[0]);
			i2++;
			Delay.msDelay(100);
			

			double trvlDist = travelDistance();
			_position.update(trvlDist);
//			System.out.println("TRVL: " + trvlDist);
//			System.out.println(_position.toString());

			//in case very short distance in front of the next wall
//			if(frontSample[0] * 100 < robotLength + 1){
//				new FrontEncounter(1.04).behave();
//				waitComplete();
//			}
		}
		//need stop first? then rotate?
/*		rotate(180);
		waitComplete();
		
*/		
		Sound.systemSound(true, 3);
		//stop then?
	//	stopImm();
	//	waitComplete();
	//	Sound.beep();
	}

}
