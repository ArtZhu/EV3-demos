package lab_two_2;


import java.util.*;

import lejos.hardware.Sound;
import lejos.utility.Delay;


public class Assgt1 extends Lab2{
	private static double turnConst = 0.905; //0.905wood, 1.083 carpet
	private static double _frontDist = 0;
	private static List<Double> sideDistList = new ArrayList<Double>();
	private static position _position = new position();
	
	
	public static void go(){
		touchThreadInit();
		
	    setupSafeButton();
	    
	    ifReady("Assgt1");

		start();
		
		int i = 0;
		while(true){
			left.resetTachoCount();
			right.resetTachoCount();
			frontDist.fetchSample(frontSample, 0);
			sideDist.fetchSample(sideSample, 0);

			sideDistList.add((double) sideSample[0]);
			
			double trvlDist = travelDistance();
			_position.update(trvlDist);
			if(i > 2){
				alongTrack(sideDistList, 90);
				if(_position.back()){
					Sound.systemSound(true,  3);
					dance1(3);
					System.exit(0);
				}
			}
			
			if(frontSample[0] *100 < robotLength){
				turnLeft();
				set(90, 90);
				forward();
			}
			
			if(sideSample[0] > 1.0){
				turnRight();
				set(90, 90);
				forward();	
			}
			i++;
		}
		
		
	
	}//go()
	
    
	
	private static void start(){
		Sound.systemSound(true, 2);
		set(90, 90);
		forward();
		frontDist.fetchSample(frontSample, 0);
		sideDist.fetchSample(sideSample, 0);
		int i2 = 0;
		while(sideSample[0] > 1.0){
			frontDist.fetchSample(frontSample, 0);
			sideDist.fetchSample(sideSample, 0);
			System.out.println("f[" + i2 + "]: " + frontSample[0]);
			System.out.println("s[" + i2 + "]: " + sideSample[0]);
			i2++;
			Delay.msDelay(100);
			
			//in case very short distance in front of the next wall
			if(frontSample[0] * 100 < robotLength + 1){
				turnLeft();
			}
		}
		Sound.systemSound(true,  2);
	}
	
	private static void turnRight(){
		//update position
		_position.turnRight();

		Sound.beep();
		stopImm();
		int goOutAngle = (int) (robotLength / wheelCircumference * 360) ; 
		set(90, 90);
		left.rotate(goOutAngle, true);
		right.rotate(goOutAngle);
		turn(90, turnConst);
		frontDist.fetchSample(frontSample, 0);
		sideDist.fetchSample(sideSample, 0);
		int i2 = 0;
		while(sideSample[0] > 1.0){
			frontDist.fetchSample(frontSample, 0);
			sideDist.fetchSample(sideSample, 0);
			System.out.println("f[" + i2 + "]: " + frontSample[0]);
			System.out.println("s[" + i2 + "]: " + sideSample[0]);
			i2++;
			Delay.msDelay(100);
			
			//in case very short distance in front of the next wall
			if(frontSample[0] * 100 < robotLength + 1){
				turnLeft();
			}
		}
		
		Sound.beep();
		stopImm();
		
	}
	
	private static void turnLeft(){
		//update position
		_position.turnLeft();
		
		Sound.beep();
		stopImm();
		set(90, 90);
		turn(-1 * 90, turnConst);
		frontDist.fetchSample(frontSample, 0);
		sideDist.fetchSample(sideSample, 0);
		Sound.beep();
	}
}
