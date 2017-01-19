package adapter;

import algorithm.*;

public class genGraphTestDrive {
	public static void main(String[] args){
		Search2.setSource(new Point(3, 4));
		Search2.setGoal(new Point(7, 8));
		genGraph g1 = new genGraph("obstacle.txt");		
	}
}
