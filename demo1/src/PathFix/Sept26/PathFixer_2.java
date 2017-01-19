package PathFix.Sept26;

import java.util.List;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.robotics.RegulatedMotor;

public class PathFixer_2 implements PathFixer{
	private static RegulatedMotor left;
	private static RegulatedMotor right;
	private static List<Double> sideDistList;
	private static int stdSpeed;
	
	//sideDistList in meters
	public PathFixer_2(RegulatedMotor _left, RegulatedMotor _right, List<Double> _sideDistList, int _stdSpeed){
		left = _left;
		right = _right;
		sideDistList = _sideDistList;
		stdSpeed = _stdSpeed;
	}
	
	//sideSonic on right
	public void fixRight() {
		double dSpeed = stdSpeed / 90 * 10;
	   	Double std = sideDistList.get(0);
	   	Double last = sideDistList.get(sideDistList.size() - 1);
	   	Double prev = sideDistList.get(sideDistList.size() - 2);
	   	if(last > std){
	   		if(last < prev){
	   			left.setSpeed(stdSpeed);
	   			right.setSpeed(stdSpeed);
	   			left.synchronizeWith(new RegulatedMotor[] {right});
	   			left.startSynchronization();
	   			left.forward();
	   			right.forward();
	   			left.endSynchronization();
	   		}
	   		else if(last >= prev){
	   			left.setSpeed(stdSpeed + (int) dSpeed);
	   			right.setSpeed(stdSpeed);
	   			left.synchronizeWith(new RegulatedMotor[] {right});
	   			left.startSynchronization();
	   			left.forward();
	   			right.forward();
	   			left.endSynchronization();
	   		}	
	   	}
	   	else if (last < std){
	   		if(last <= prev){
	   			left.setSpeed(stdSpeed);
	   			right.setSpeed(stdSpeed + (int) dSpeed);
	   			left.synchronizeWith(new RegulatedMotor[] {right});
	   			left.startSynchronization();
	   			left.forward();
	   			right.forward();
	   			left.endSynchronization();
	   		}
	   		else if(last > prev){
	   			left.setSpeed(stdSpeed);
	   			right.setSpeed(stdSpeed);
	   			left.synchronizeWith(new RegulatedMotor[] {right});
	   			left.startSynchronization();
	   			left.forward();
	   			right.forward();
	   			left.endSynchronization();
	   		}
	   	}
	   	//if approximately on track
	   	else{
	   		if(last < prev){
	   			left.setSpeed(stdSpeed);
	   			right.setSpeed(stdSpeed + (int) dSpeed);
	   			left.synchronizeWith(new RegulatedMotor[] {right});
	   			left.startSynchronization();
	   			left.forward();
	   			right.forward();
	   			left.endSynchronization();
	   		}
	   		else if(last > prev){
	   			left.setSpeed(stdSpeed + (int) dSpeed);
	   			right.setSpeed(stdSpeed);
	   			left.synchronizeWith(new RegulatedMotor[] {right});
	   			left.startSynchronization();
	   			left.forward();
	   			right.forward();
	   			left.endSynchronization();
	   		}
	   		else{
	   			left.setSpeed(stdSpeed);
	   			right.setSpeed(stdSpeed);
	   			left.synchronizeWith(new RegulatedMotor[] {right});
	   			left.startSynchronization();
	   			left.forward();
	   			right.forward();
	   			left.endSynchronization();
	   		}
	   	}
	}
	   	
	public void fixLeft(){
		
	}
	
}
