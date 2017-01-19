package adapter;


import algorithm.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.*;

public class genGraph {
	private static double cellWidth;
	private final int cellsY;
	private final int cellsX;
	private int[][] graph;
	private double cellLength = 30;
	
	public genGraph(String fileName, double _cellWidth, double horizon, double vertical){
		cellWidth = _cellWidth;
		cellsY =(int) (horizon/cellWidth) + 1 + 2;
		cellsX =(int) (horizon/cellWidth) + 1 + 2;
		graph = new int[cellsX][];
		for(int i=0; i<graph.length; i++) {
			graph[i] = new int[cellsY];
			graph[i][0] = -1;
			graph[i][cellsY-1] = -1;
		}
		
		for(int i=0; i<cellsY; i++){
			graph[0][i] = -1;
			graph[cellsX-1][i] = -1;
		}
		
		
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
		}catch(FileNotFoundException e) {System.out.println("fileNotFound");}
		
		
		Scanner sc = new Scanner(is);
		while(sc.hasNextLine()){
			String[] coord = sc.nextLine().split(" ");
			Function<Integer, Integer> f = x ->(int) (x / cellLength) + 1;
			graph[f.apply(Integer.valueOf(coord[0]))][f.apply(Integer.valueOf(coord[1]))] = -1;
		}
		
		sc.close();
		Search2.setGraph(graph);

		FrameInput fi = new FrameInput();
		fi.run();
	}
	
	public int[][] fetchGraph(){
		return graph;
	}

	class FrameInput extends JFrame{
		private JPanel mainPanel;
		private JButton[][] btnGraph;
		private JPanel[] panels;

		public FrameInput() {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			
			btnGraph = new JButton[cellsX - 2][];
			for(int i=0; i<btnGraph.length; i++) 
				btnGraph[i] = new JButton[cellsY - 2];
			for(int i=0; i<btnGraph.length; i++){
				for(int j=0; j<btnGraph[i].length; j++){
					btnGraph[i][j] = new JButton(String.valueOf(graph[i+1][j+1]));
					final Integer i1 = i; final Integer j1 = j;
					btnGraph[i][j].addActionListener(evt -> {
						toggle(i1, j1);
					});
				}
			}
			
			panels = new JPanel[cellsX - 2];
			for(int i=0; i<panels.length; i++) {
				panels[i] = new JPanel();
				panels[i].setLayout(new BoxLayout(panels[i], BoxLayout.X_AXIS));
				final Integer i1 = i;
				Arrays.stream(btnGraph[i]).forEach(btn -> panels[i1].add(btn));
			}
			
			Arrays.stream(panels).forEach(panel -> mainPanel.add(panel));
			
			JPanel setPanel = new JPanel();
			setPanel.setLayout(new BoxLayout(setPanel, BoxLayout.X_AXIS));
			JButton setBtn = new JButton("SET");
			setBtn.addActionListener(evt -> Search2.setGraph(graph));
			setPanel.add(setBtn);
			mainPanel.add(setPanel);
		}
		
		public void run(){
			setTitle("MapInput");
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setLocation(200,200);
			setContentPane(mainPanel);
			setVisible(true);
			this.pack();
		}
		
		private void toggle(int i, int j){
			if(graph[i+1][j+1] == -1) {
				graph[i+1][j+1] = 0;
				btnGraph[i][j].setText("0");
			}
			else {
				graph[i+1][j+1] = -1;
				btnGraph[i][j].setText("-1");
			}
		}
	}
}