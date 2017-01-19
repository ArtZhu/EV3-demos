package assgt1.v1;

import lejos.hardware.Sound;
import lejos.utility.Delay;
import Behavior.Sept26.FrontEncounter;
import Behavior.Sept26.RightUndetected;
import CalcFix.Sept26.CalcFixer;
import CalcFix.Sept26.CalcFixer_1;
import PathFix.Sept26.*;
import assgt1.v1.MyRobot;

public class Assgt1 extends MyRobot{
	private static double turnConst1 = 0.885; //Somehow 1.04 is better; //carpetTurnConst; // 0.91 for floor
	private static double turnConst2 = 0.875;
//	private static double _frontDist = 0;	
//	private static List<Behavior> behaviors = new ArrayList<Behavior>(Arrays.asList(new FrontEncounter(turnConst), new RightUndetected(turnConst)));
	private static boolean turned;
	private static boolean beforeTurn1 = true;
	
	public static void main(String[] args){
		go();
	}
	
	public static void go(){
//		touchThreadInit();
		
	    setupSafeButton();
	    
	    _position.reset();
	    
	    PathFixer pf = new PathFixer_1(left, right, sideDistList, 90);
	    ifReady("Assgt1");

		start();
		
		int i = 0;

		set(90, 180);
		forward();
		while(true){
			left.resetTachoCount();
			right.resetTachoCount();
			if(turned){
				i = 0;
				turned = false;
			}
			
			frontDist.fetchSample(frontSample, 0);
			sideDist.fetchSample(sideSample, 0);
//			System.out.println("f[" + i + "]: " + frontSample[0]);
//			System.out.println("s[" + i + "]: " + sideSample[0]);
			i++;
			Delay.msDelay(100);
			
			//sideDistList in meters
			sideDistList.add((double) (100 * sideSample[0]));
			if(sideDistList.size() > 2){
				pf.fixRight();
			}
			
			if(frontSample[0] < 0.15){
				//fix the actual travel distance.
				//sideDistList in meters
//				CalcFixer cf = new CalcFixer_1(90, 100, trvlDist, sideDistList, wheelCircumference);
//				cf.fix();
//				double actualDist = cf.getAnswer();
//				_position.update(actualDist);

				//
				
				//
				stopImm();
				new FrontEncounter(turnConst2).behave();
				waitComplete();
				
				beforeTurn1 = false;
				turned = true;
				
				//
				left.resetTachoCount();
				right.resetTachoCount();
				set(90, 180);
				forward();
			}
			
			if(sideSample[0] > 0.20){
//				Sound.beep();


				
				//fix the actual travel distance.
				//sideDistList in meters
//				CalcFixer cf = new CalcFixer_1(90, 100, trvlDist, sideDistList, wheelCircumference);
//				cf.fix();
//				double actualDist = cf.getAnswer();
				
				//update it to position
//				_position.update(actualDist);
				
				//

				
				//
//				sideDistList.remove(sideSample[0]);
				
				//
				new RightUndetected(turnConst1, 12.5).behave();
				
				turned = true;
				beforeTurn1 = false;
				
				//
				left.resetTachoCount();
				right.resetTachoCount();
				set(90, 180);
				forward();	
			}
			
			if(!beforeTurn1){
				if(_position.isBack()){
					Sound.systemSound(true, 4);
					stopImm();
					closeSensors();
//					dance1(2);
					System.out.println("done");
					Delay.msDelay(20000);
					System.exit(0);
				}
			}
			
			
			double trvlDist = travelDistance();
			System.out.println(_position.toString());
			_position.update(trvlDist);
		}
		
	
	
	}//go()
	
    
	
	private static void start(){
//		Sound.systemSound(true, 2);
		set(90, 180);
		forward();
		frontDist.fetchSample(frontSample, 0);
		sideDist.fetchSample(sideSample, 0);
		int i2 = 0;
		while(sideSample[0] > 0.4){
			frontDist.fetchSample(frontSample, 0);
			sideDist.fetchSample(sideSample, 0);
//			System.out.println("f[" + i2 + "]: " + frontSample[0]);
//			System.out.println("s[" + i2 + "]: " + sideSample[0]);
			i2++;
			Delay.msDelay(100);
			
			//in case very short distance in front of the next wall
/*			if(frontSample[0] * 100 < robotLength + 1){
				(new FrontEncounter(turnConst)).behave();
			}
*/		}
		rotate(90);
		waitComplete();
//		stopImm();
//		Sound.beep();
//		Sound.systemSound(true,  2);
	}

}
