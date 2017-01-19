package Threads;

public class Point2 extends Point{
	public boolean dir;
	public int turn;
	
	public Point2(int x, int y, int _turn, boolean _dir){
		super(x, y);
		turn = _turn;
		dir = _dir;
	}
	
	public Point2(Point p){
		super(p.X, p.Y);
		turn = 0;
		dir = true;
	}
}

