package CalcFix.Sept26;

import java.util.List;

import assgt1.v1.MyRobot;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.robotics.RegulatedMotor;

public class CalcFixer_1 implements CalcFixer{
	private int speed;
	private int msDelay;
	private double trvlDist;
	private double answer;
	private List<Double> sideDistList;
	private double wheelCircumference; 
	
	/*
	 * EVERYTHING MUST BE IN CM!
	 */
	public CalcFixer_1(int _speed, int _msDelay, double _trvlDist, List<Double> _sideDistList, double _wheelCircumference){
		speed = _speed;
		msDelay = _msDelay;
		trvlDist = _trvlDist;
		sideDistList = _sideDistList;
		wheelCircumference = _wheelCircumference;
	}

	//gives the supposed travelDistance of if no Odometry issues.
	//speed in degrees/sec, trvlDist as recorded by the TachoCount
	public void fix(){
		double actualLength = 0;
		double intendedLength = 0;
		double difference = 0;
		double _speed = (speed/360) * wheelCircumference;
		double secDelay = msDelay / 1000;
		for(int i = 1; i < sideDistList.size(); i++){
			Double s1 = sideDistList.get(i-1);
			Double s2 = sideDistList.get(i);

			//create triangle
			Double height = s2 - s1;
			Double hypoteneuse = _speed * secDelay;
			Double _side = Math.sqrt(hypoteneuse * hypoteneuse + height * height);
				
			actualLength += hypoteneuse;
			intendedLength += _side;
			difference += height;
		}
		answer = intendedLength/actualLength * trvlDist;
	}	
	
	public double getAnswer(){
		return answer;
	}
	
}
