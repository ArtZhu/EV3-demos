package lab_two_2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class position {
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
		direction += 1;
		if(direction > 3){
			direction += -4;
		}
	}
	
	public void turnLeft(){
		direction += -1;
		if(direction < 0)
			direction +=4;
	}
	
	public boolean back(){
		return (x == 0 && y == 0);
	}
	//
	public void updateX(double dx){
		x += dx;
	}
	
	public void updateY(double dy){
		y += dy;
	}
}

interface strat{
	public void setD(double d);
	public void update(position p);
}

class xUp implements strat{
	private static double dx;
	
	public void setD(double d){
		dx = d;
	}
	
	public void update(position p){
		p.updateX(dx);
	}
}

class xDown implements strat{
	private static double dx;
	
	public void setD(double d){
		dx = d;
	}
	
	public void update(position p){
		p.updateX(-1 * dx);
	}
}

class yUp implements strat{
	private static double dy;
	
	public void setD(double d){
		dy = d;
	}
	
	public void update(position p){
		p.updateX(dy);
	}
	
}

class yDown implements strat{
	private static double dy;
	
	public void setD(double d){
		dy = d;
	}
	
	public void update(position p){
		p.updateX(-1 * dy);
	}
}

