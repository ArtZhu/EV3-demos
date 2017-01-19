package adapter;


import algorithm.*;
import algorithm.Point;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.util.function.*;

public class genGraph {
	private genGraph(){}
	private static double horizon = 269.9;
	private static double vertical = 269.9;
	private static double cellWidth = 30;
	private static int cellsY = 11;
	private static int cellsX = 11;
	private static int[][] graph_gen;
	
	private static boolean PRINT = true;
	
	private static void refresh(){printGraph();Search2.setGraph(graph_gen);}
	
	//SEND POINTS
	private static int ID = 6983;
	
	public static void set(double _horizon, double _vertical, double _cellWidth){
		if(_horizon != 0) _horizon = _horizon-0.1; if(_vertical != 0) _vertical = _vertical-0.1;
		horizon = _horizon;
		vertical = _vertical;
		cellWidth = cellWidth;
		cellsY =(int) (horizon/cellWidth) + 1 + 2;
		cellsX =(int) (horizon/cellWidth) + 1 + 2;
	}
	
	public genGraph(String fileName){
		graph_gen = new int[cellsX][];
		for(int i=0; i<graph_gen.length; i++) {
			graph_gen[i] = new int[cellsY];
			graph_gen[i][0] = -1;
			graph_gen[i][cellsY-1] = -1;
		}
		
		for(int i=0; i<cellsY; i++){
			graph_gen[0][i] = -1;
			graph_gen[cellsX-1][i] = -1;
		}
		
		//READ FILE
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
		}catch(FileNotFoundException e) {System.out.println("fileNotFound");}
		
		
		Scanner sc = new Scanner(is);
		while(sc.hasNextLine()){
			String[] coord = sc.nextLine().split(" ");
			System.out.printf("(%s, %s)\n", coord[0], coord[1]);
			Function<String, Integer> f = x ->(int) (Double.valueOf(x) / cellWidth) + 1;
			graph_gen[f.apply(coord[0])][f.apply(coord[1])] = -1;
		}
		sc.close();
		
		refresh();

//		FrameInput fi = new FrameInput();
//		fi.run();
		try{
			sendPoints(Search2.bestPathFrom(Search2.source), Search2.InitialGraph, Search2.goal);
		}catch(IOException e){}		
	}

	class FrameInput extends JFrame{
		private JPanel mainPanel;
		private JButton[][] btngraph;
		private JPanel[] panels;

		public FrameInput() {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			
			btngraph = new JButton[cellsX - 2][];
			for(int i=0; i<btngraph.length; i++) 
				btngraph[i] = new JButton[cellsY - 2];
			for(int i=0; i<btngraph.length; i++){
				for(int j=0; j<btngraph[i].length; j++){
					btngraph[i][j] = new JButton(String.valueOf(graph_gen[i+1][j+1]));
					final Integer i1 = i; final Integer j1 = j;
					btngraph[i][j].addActionListener(evt -> {
						toggle(i1, j1);
					});
				}
			}
			
			panels = new JPanel[cellsX - 2];
			for(int i=0; i<panels.length; i++) {
				panels[i] = new JPanel();
				panels[i].setLayout(new BoxLayout(panels[i], BoxLayout.X_AXIS));
				final Integer i1 = i;
				Arrays.stream(btngraph[i]).forEach(btn -> panels[i1].add(btn));
			}
			
			Arrays.stream(panels).forEach(panel -> mainPanel.add(panel));
			
			JPanel setPanel = new JPanel();
			setPanel.setLayout(new BoxLayout(setPanel, BoxLayout.X_AXIS));
			JButton setBtn = new JButton("SET");
			JButton pathFromBtn = new JButton("PathFrom");
			JButton bestPathBtn = new JButton("BestPath");
			setBtn.addActionListener(evt -> refresh());
			pathFromBtn.addActionListener(evt -> Search2.printPathFrom(Search2.source));
			bestPathBtn.addActionListener(evt -> Search2.printBestPath(Search2.source));
			setPanel.add(setBtn); setPanel.add(pathFromBtn); setPanel.add(bestPathBtn);
			
			JButton sendBtn = new JButton("SENDPOINT");
			sendBtn.addActionListener(evt -> {
				try{
					sendPoints(Search2.bestPathFrom(Search2.source), Search2.graph_goal, Search2.goal);
				}catch(IOException e){}								//What will happen here
			});
			setPanel.add(sendBtn);
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
			if(graph_gen[i+1][j+1] == -1) {
				graph_gen[i+1][j+1] = 0;
				btngraph[i][j].setText("0");
			}
			else {
				graph_gen[i+1][j+1] = -1;
				btngraph[i][j].setText("-1");
			}
		}
	}
	
	 //printing
    public static void printGraph(){
    	if(PRINT){
    		System.out.println("genGraph.graph_gen: ");
	    	for(int[] row: graph_gen){
	    		for(int column: row)
	    			System.out.print(fill(String.valueOf(column), 4));
	    		System.out.print("\n");
	    	}	
    	}
    	System.out.println();
    }
    
    private static String fill(String s, int length){
    	length = length - s.length();
    	for(int i=0; i<length; i++)
    		s = s+" ";
    	return s;
    }
    
    public static void sendPoints(Queue<Point> q, int[][] graph, Point goal) throws IOException {
        Point temp;
        for(Point p: q) System.out.println(p);
        System.out.println(q.size());
        Socket t = null;
        ObjectOutputStream p = null;                       //create an Object stream
        ServerSocket s = new ServerSocket(ID); // create a TCP socket, this is overkill
        System.out.println("Server started");           
        t = s.accept();                                               // wait for client (robot) to connect 
        System.out.println("server connected");
        p = new ObjectOutputStream(t.getOutputStream());  //wrap the output stream with an object output stream
        p.writeObject(q); 
        p.writeObject(graph);
        p.writeObject(goal);//so we can write the entire serializable  object
        p.flush();                                                                     //flush it all out
        p.close();
        t.close();
        s.close();
    }

}