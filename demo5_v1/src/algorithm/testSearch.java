package algorithm;

public class testSearch {
	public static void main(String[] args){
		//		Column	0   1	2	3	4	5	6
		int[] row0  = {-1, -1, -1, -1, -1, -1, -1};
		int[] row1  = {-1,  0,  0, -1,  0,  0, -1};
		int[] row2  = {-1,  0,  0, -1,  0,  0, -1};
		int[] row3  = {-1,  0,  0, -1,  0,  0, -1};
		int[] row4  = {-1,  0,  0,  0,  0,  0, -1};
		int[] row5  = {-1,  0,  0,  0, -1,  0, -1};
		int[] row6  = {-1,  0, 	0, -1,  0,  0, -1};
		int[] row7  = {-1,  0,  0, -1,  0,  0, -1};
		int[] row8  = {-1,  0,  0, -1,  0,  0, -1};
		int[] row9  = {-1,  0,  0, -1,  0,  0, -1};
		int[] row10 = {-1, -1, -1, -1, -1, -1, -1};
		int[][] graph = {row0, row1, row2, row3, row4, row5, row6, row7, row8, row9, row10};
		
		Search2 s1 = new Search2(graph, 9, 5);
		
		s1.printGraph();
		
		Iterable<Point> path = s1.pathTo(9, 1);
		if(path != null)
			path.forEach(p -> System.out.printf("(%d, %d) \n",p.X, p.Y));
		else System.out.print("NOPATH!");
		
		
		System.out.println();
		
		
		Iterable<Point> bestPath = s1.bestPathTo(9, 1);
		if(path != null)
			path.forEach(p -> System.out.printf("(%d, %d) \n",p.X, p.Y));
		else System.out.print("NOPATH!");
	}
}
