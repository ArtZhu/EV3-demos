package assgt1.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class position_2 {
	private static double x = 0;
	private static double y = 0;
	private static int direction = 0; //0N, 1E, 2S, 3W
	private List<strat> stratList = new ArrayList<strat>(Arrays.asList(new yUp(), new xUp(), new yDown(), new xDown()));
	
	
	public void update(double trvlDist){
		strat s = stratList.get(direction);
		s.setD(trvlDist);
		s.update(this);
	}
	
	public void turnRight(){
		direction ++;
		if(direction == 4){
			direction = 0;
		}
	}
	
	public void turnLeft(){
		direction = direction -1;
		if(direction == -1)
			direction = 3;
	}
	
	///APPROXIMATE
	public boolean isBack(){
		return (x < 15.0 && x > -15.0 && y < 15.0 && y > -15.0);
	}
	
	//
	protected void updateX(double dx){
		x += dx;
	}
	
	protected void updateY(double dy){
		y += dy;
	}
	
	public void reset(){
		x = 0;
		y = 0;
	}
	
	public String toString(){
		return "X: " + ((int) x) + "\nY: " + ((int) y);
	}
	
	public int getDirection(){
		return direction;
	}
	
}

interface strat2{
	public void setD(double d);
	public void update();
}

class xUp2 implements strat2{
	private static double dx;
	
	public void setD(double d){
		dx = d;
	}
	
	public void update(){
		updateX(dx);
	}
}

class xDown2 implements strat2{
	private static double dx;
	
	public void setD(double d){
		dx = d;
	}
	
	public void update(position_2 p){
		p.updateX(-1 * dx);
	}
}

class yUp2 implements strat2{
	private static double dy;
	
	public void setD(double d){
		dy = d;
	}
	
	public void update(position_2 p){
		p.updateY(dy);
	}
	
}

class yDown2 implements strat2{
	private static double dy;
	
	public void setD(double d){
		dy = d;
	}
	
	public void update(position_2 p){
		p.updateY(-1 * dy);
	}
}

