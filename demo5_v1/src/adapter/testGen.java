package adapter;

import algorithm.*;

public class testGen {
	public static void main(String[] args){
		genGraph g1 = new genGraph("test.txt", 30, 269.9, 269.9);
		Search2 s = new Search2(null, 8, 8);

		s.printGraph();
		Iterable<Point> path = s.bestPathTo(1, 1);
		path.forEach(p -> System.out.printf("(%d, %d) \n", p.X, p.Y));
	}
}
