package Bug3;

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
		return Bug3.AVOIDOBSTACLE;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug3.Debug) LCD.drawString("PIDObstacle", 0, 1);
		int sideDist = Bug3.sideSonicData.getSonicValue();
		LCD.drawInt(sideDist, 0, 2);
		
		//first time in PID then set the desiredDist
		if(Bug3.isSonicData1){
			Bug3.desiredDist = (sideDist * 1 + Bug3.desiredDist) / 2;
			Bug3.isSonicData1 = false;
		}

		// set various error values
		Bug3.error = sideDist - Bug3.desiredDist;
		Bug3.accumError += Bug3.error;
		Bug3.errorDiff = Bug3.error - Bug3.lastError;
		Bug3.lastError = Bug3.error;

		// set PID values
		double P = Bug3.Kp * Bug3.error;
		double I = Bug3.Ki * Bug3.accumError;
		double D = Bug3.Kd * Bug3.errorDiff;
		double angle = (P + I + D) * 1;
		LCD.drawInt((int) (angle * 100000), 0, 4);
		
		//control pilot
		((DifferentialPilot) Bug3.nav.getMoveController()).setTravelSpeed(Bug3.stdSpeed);
//		Bug3.nav.goTo(Bug3.destination.getX(), Bug3.destination.getY());
		((DifferentialPilot) Bug3.nav.getMoveController()).steer(-1 * angle);
		
		if(Bug3.Debug){
			// too close, turn left
			if (Bug3.error > 0) LCD.drawString(Bug3.leftString, 0, 3);
			// too far, turn right
			else				LCD.drawString(Bug3.rightString, 0, 3);
		}
		
		
		//exist behavior if no wall on right
		if(Bug3.goodTogo()){
			((DifferentialPilot) Bug3.nav.getMoveController()).stop();
			Delay.msDelay(50);
			Bug3.AVOIDOBSTACLE = false;
		}
		
		LCD.drawInt(((int)Bug3.poseProvider.getPose().getX() * 10000), 0, 5);
		LCD.drawInt(((int)Bug3.poseProvider.getPose().getY() * 10000), 0, 6);
		Delay.msDelay(50);
	}

	public void suppress() {
		suppressed = true;	
	}

}
