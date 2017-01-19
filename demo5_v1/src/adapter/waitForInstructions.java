package adapter;

import ProfTests.*;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;
import algorithm.*;

public class waitForInstructions implements Behavior{
	private boolean suppressed = false;

	private Pose pose;

	@Override
	public boolean takeControl() {
		int sonicValue = waveFrontBug.frontSonicData.getSonicValue();
		if(waveFrontBug.insReceived){
			if(sonicValue < waveFrontBug.antennaLong){
				waveFrontBug.insReceived = false;
				pose = waveFrontBug.poseProvider.getPose();
				int heading = (int) ((pose.getHeading() / 90.0) % 4);
				Point p = new Point(waveFrontBug.toCellDist.apply((double) pose.getX()), waveFrontBug.toCellDist.apply((double) pose.getY()));
				switch (heading){
					case 0: p.Y = p.Y +1;
					case 1: p.X = p.X -1;
					case 2: p.Y = p.Y -1;
					case 3: p.X = p.X +1;
					default: Sound.beep();
				}
				
				//SEND INFO - p
				return true;
			}
			return false;
		}
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void action() {
		//back to center of the block
		int cellX = waveFrontBug.toCellDist.apply((double) pose.getX());
		int cellY = waveFrontBug.toCellDist.apply((double) pose.getY());
		
		double gotoX = (cellX + 1.0/2.0) * waveFrontBug.cellLength;
		double gotoY = (cellY + 1.0/2.0) * waveFrontBug.cellLength;
		
		if(Math.abs(pose.getX() - gotoX) < 1) waveFrontBug.pilot.travel(-1 * Math.abs(gotoY - pose.getY()));
		else waveFrontBug.pilot.travel(-1 * Math.abs(gotoX - pose.getX()));
		
		
		//while new path not here
		//WAIT FOR INFO	
		//setInfo
	}
	
	public void suppress() {
		suppressed = true;	
	}
	
    class Point{
    	int X, Y;
    	public Point(int _x,int _y){
    		X = _x;
    		Y = _y;
    	}
    }
}
