package PathFix.Sept26;

import assgt1.v1.*;
import java.util.List;

public class diagToParallel_rough extends MyRobot implements PathFixer{
	//sideSonic on left
	private double angle;
	private double turnConst;
	
	public diagToParallel_rough(List<Double> frontDistList, List<Double> sideDistList, double _turnConst){
		angle = _angle(frontDistList, sideDistList);
		turnConst = _turnConst;
	}
	public void fix(){
		turn((int) angle, turnConst);			
	}
	
	public double getAngle(){
		return angle;
	}

	//calculate the angle between robot and the wall on the side of the side sensor	
	//require two not small lists
   	private double _angle(List<Double> frontDistList, List<Double> sideDistList){
   		int i = sideDistList.size();
   		int j = frontDistList.size();
   		Double height = 100 * (sideDistList.get(i - 1) - sideDistList.get(i* 2/3));
   		System.out.println(height);
   		Double side  = 100 * (frontDistList.get(j - 1) - frontDistList.get(j* 2/3));
   		System.out.println(side);
   		return Math.atan(height/side) / Math.PI * 180;
   	}
	@Override
	public void fixRight() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void fixLeft() {
		// TODO Auto-generated method stub
		
	}
   	
   	
   	
}
