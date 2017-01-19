package Bug2;
import ProfTests.DifferentialPilot;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class rightNoWall implements Behavior{
	@SuppressWarnings("unused")
	private boolean suppressed = false;
	private boolean sound = false;
	
	public boolean takeControl() {
		if(Bug2.AVOIDOBSTACLE){
			int sideDist = Bug2.sideSonicData.getSonicValue();
			if(Bug2.Debug)
				if(sideDist > 35) {
					LCD.drawString("rightNoWall", 0, 1);
					//Sound.beep();
				}
			return sideDist > 35;	
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void action() {
		if(Bug2.Debug){
			LCD.clear();
			LCD.drawString("rightNoWall", 0, 1);
		}
		//Go over a bit then turn
		((DifferentialPilot) Bug2.nav.getMoveController()).setLinearSpeed(Bug2.stdSpeed);
		((DifferentialPilot) Bug2.nav.getMoveController()).travel(Bug2.goOutDist);
		
		((DifferentialPilot) Bug2.nav.getMoveController()).setAngularSpeed(Bug2.stdRotateSpeed);
		((DifferentialPilot) Bug2.nav.getMoveController()).rotate(-1 * 90 * Bug2.turnConst);
		
		//find wall
		((DifferentialPilot) Bug2.nav.getMoveController()).setLinearSpeed(Bug2.stdSpeed);
		((DifferentialPilot) Bug2.nav.getMoveController()).steer(0);
		while(Bug2.sideSonicData.getSonicValue() > 35){
			LCD.drawString("findingWall", 0, 3);
			Delay.msDelay(50);
		}
		
		
		
		if(Bug2.Debug){
			((DifferentialPilot) Bug2.nav.getMoveController()).stop();
			LCD.drawString("           ", 0, 3);
			LCD.drawString("foundWall", 0, 4);
			if(sound) Sound.systemSound(true, 3);
			Delay.msDelay(1000);
		}
		
		//reset PID;
		Bug2.resetPID();
		Bug2.isSonicData1 = true;
	}

	public void suppress() {
		suppressed = true;	
	}

}

