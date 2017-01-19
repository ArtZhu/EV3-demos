

import java.io.Serializable;

//supporting class
public class Point implements Serializable{
	public final int X, Y;
	public Point(int _x,int _y){
		X = _x;
		Y = _y;
	}
	
	public static Point actualPoint(double x, double y){
		if(x != 0) x = x-0.1; 
		if(y != 0) y = y-0.1;
		return new Point(((int) x/30) + 1, ((int) y/30) + 1);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Point)
			return ((Point) o).X == this.X && ((Point) o).Y == this.Y;
		return false;
	}
	
	public String toString(){
		return "Point: (" + X + " , " + Y + " )\n";
	}
	
}


