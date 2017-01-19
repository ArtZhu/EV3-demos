package Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lejos.hardware.Sound;
import lejos.utility.Delay;
import Behavior.Sept26.Behavior;
import Behavior.Sept26.FrontEncounter;
import Behavior.Sept26.RightUndetected;
import PathFix.Sept26.PathFixer;
import PathFix.Sept26.PathFixer_1;
import assgt1.v1.MyRobot;

public class PathFixTest extends MyRobot{
	private static double turnConst = 1.04; //carpetTurnConst;
	private static double _frontDist = 0;	
	private static List<Behavior> behaviors = new ArrayList<Behavior>(Arrays.asList(new FrontEncounter(turnConst), new RightUndetected(turnConst, 0)));
	private static double trvlDist;
	
	public static void main(String[] args){
		go();
	}
	
	public static void go(){
	    setupSafeButton();
	    
	    ifReady("TrvlDist");
		
	    PathFixer pf = new PathFixer_1(left, right, sideDistList, 90);
		int i = 0;
   		left.resetTachoCount();
   		right.resetTachoCount();

		set(90, 90);
		forward();
   		
		while(true){
			sideDist.fetchSample(sideSample, 0);
			sideDistList.add((double) sideSample[0]);
			System.out.println("d[" + i + "]: " + sideSample[0]);
    		i++;
    		if(sideDistList.size() > 2){
    			pf.fixRight();
    		}
    		
	    	if (sideSample[0] > 0.40) {
	    		stopImm();
	    		closeSensors();
	    		
	    		Delay.msDelay(20000);
	    		System.exit(0);
	    	}
	    	Delay.msDelay(100);
	        }
	}
}
