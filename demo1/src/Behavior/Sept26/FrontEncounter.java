package Behavior.Sept26;

import lejos.hardware.Sound;
import assgt1.v1.*;

public class FrontEncounter extends MyRobot implements Behavior{
	private Priority priority = new Priority(0);
	private double turnConst;
	
	public FrontEncounter(double _turnConst){
		turnConst = _turnConst;
	}
	@Override
	public void behave() {
		//
		_position.turnLeft();
		
		set(90, 90);
		turn(90, turnConst);
		frontDist.fetchSample(frontSample, 0);
		sideDist.fetchSample(sideSample, 0);
		waitComplete();
//		Sound.beep();
	}

}
