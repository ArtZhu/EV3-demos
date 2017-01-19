package Bug3v2;

import ProfTests.*;
import lejos.hardware.lcd.LCD;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class PIDobstacle implements Behavior{
	
	@SuppressWarnings("unused")
	private boolean suppressed = false;

	public boolean takeControl() {
		//this behavior overrides "go" when we are in the behavior group of AVOIDOBSTACLE
		return Bug3v2.AVOIDOBSTACLE;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug3v2.Debug) LCD.drawString("PIDObstacle", 0, 1);
		int sideDist = Bug3v2.sideSonicData.getSonicValue();
		LCD.drawInt(sideDist, 0, 2);
		
		//first time in PID then set the desiredDist
		if(Bug3v2.isSonicData1){
			Bug3v2.desiredDist = (sideDist * 1 + Bug3v2.desiredDist) / 2;
			Bug3v2.isSonicData1 = false;
		}

		// set various error values
		Bug3v2.error = sideDist - Bug3v2.desiredDist;
		Bug3v2.accumError += Bug3v2.error;
		Bug3v2.errorDiff = Bug3v2.error - Bug3v2.lastError;
		Bug3v2.lastError = Bug3v2.error;

		// set PID values
		double P = Bug3v2.Kp * Bug3v2.error;
		double I = Bug3v2.Ki * Bug3v2.accumError;
		double D = Bug3v2.Kd * Bug3v2.errorDiff;
		double angle = (P + I + D) * 1;
		LCD.drawInt((int) (angle * 100000), 0, 4);
		
		//control pilot
		((DifferentialPilot) Bug3v2.nav.getMoveController()).setTravelSpeed(Bug3v2.stdSpeed);
//		Bug3.nav.goTo(Bug3.destination.getX(), Bug3.destination.getY());
		((DifferentialPilot) Bug3v2.nav.getMoveController()).steer(-1 * angle);
		
		if(Bug3v2.Debug){
			// too close, turn left
			if (Bug3v2.error > 0) LCD.drawString(Bug3v2.leftString, 0, 3);
			// too far, turn right
			else				LCD.drawString(Bug3v2.rightString, 0, 3);
		}
		
		
		//exist behavior if no wall on right
		if(Bug3v2.goodTogo()){
			((DifferentialPilot) Bug3v2.nav.getMoveController()).stop();
			Delay.msDelay(50);
			Bug3v2.AVOIDOBSTACLE = false;
		}
		Delay.msDelay(50);
	}

	public void suppress() {
		suppressed = true;	
	}

}
