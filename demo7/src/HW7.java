import lejos.hardware.Brick;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;
import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import Threads.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class HW7 {
	private static globalUltrasonicData data = new globalUltrasonicData();
	private static NXTUltrasonicSensor us = new NXTUltrasonicSensor(SensorPort.S1);
	
	//-----------
	static final double turnConst = 0.83;  // 0.86 0.95
	
	static final double robotLength = 15.0;
	static final double wheelDiameter = 5.4;
	static final double wheelCircumference = wheelDiameter * Math.PI;
	static final double robotTrack = 14.5 * turnConst;  //14.5	
	//-----------
	@SuppressWarnings({"deprecation", "unchecked" })
	
	public static void main(String[] args) throws IOException {
		
		final DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, robotTrack, Motor.B, Motor.A);
        Navigator nav = new Navigator(pilot);

        pilot.setTravelSpeed(20);
        pilot.setRotateSpeed(15);
        

        pp = nav.getPoseProvider();
        pilot.addMoveListener((MoveListener) pp);
        
        Thread u = new Thread(new sonicThread(data, us));
		u.setDaemon(true);
		u.start();
		
		int mag = 30;
       
        pp.setPose(new Pose(0*mag, 0*mag, 90));
        
        //Connect();
        //Button.waitForAnyPress();
        
        int x = 0;
        int y = 0;
        int i = 0;
        int j = 0;
        
        int [][] graph = new int [5][5];
        
        for (i = 0; i < 5; i ++){
        	for (j = 0; j < 5; j ++){
        		graph[i][j] = 0;
        	}
        }
        
        graph[0][0] = 100; //mark starting position as visited
     
        //tentative obstacles
        //graph[0][3] = 100;
        //graph[2][2] = 100;
        //graph[4][3] = 100;
        //graph[4][4] = 100;
        
        ArrayList<Integer> alist = new ArrayList<Integer>();
        
        int cups = 0;
        
        
        while(cups < 4) {
        LCD.drawString("CURR HEAD:"+ pp.getPose().getHeading(), 1, 1);
        	
        if (pp.getPose().getHeading() > 80 && pp.getPose().getHeading() < 100) {//headed upward
        	if (data.getSonicValue() < 30) {
            	graph[x][y+1] = 100;
            	cups++;
            	alist.add(x);
            	alist.add(y+1);
            	LCD.drawString("adding..."+ x + "and" + (y+1), 1, 2+cups);
            }
        	
        	if (x - 1 >= 0 && graph[x-1][y] == 0) {//go left
        		x = x - 1;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 180));
    			
        		//try nav.goTo as well
        		
        		LCD.drawString("TO LEFT"+ pp.getPose().getHeading() + " " + x + " " + y, 1, 2);
        		nav.followPath();
        		while(!nav.pathCompleted()){
        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        		graph[x][y] = 100;//mark visited
        	}
        	else if (y + 1 < 5 && graph[x][y+1] == 0) {//go forward
        		y = y + 1;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 90));
        		LCD.drawString("TO FOR"+ pp.getPose().getHeading() + " " + x + " " + y, 1, 2);
        		nav.followPath();
        		while(!nav.pathCompleted()){
        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        		graph[x][y] = 100;
        	}
        	else if (x + 1 < 5 && graph[x+1][y] == 0) {//go right
        		x = x + 1;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 0));
        		LCD.drawString("TO RIGHT"+ pp.getPose().getHeading() + " " + x + " " + y, 1, 2);
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        		graph[x][y] = 100;
        	}
        	else {//even if the backward is marked visited, go back
        		y = y - 1;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, -90));
        		LCD.drawString("TO BACK" + pp.getPose().getHeading() + " " + x + " " + y, 1, 2);
        		nav.followPath();
        		while(!nav.pathCompleted()){
        			
        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        }
        
        if (pp.getPose().getHeading() > -10 && pp.getPose().getHeading() < 10) {//headed rightward
        	if (data.getSonicValue() < 30) {
            	graph[x+1][y] = 100;
            	cups++;
            	alist.add(x+1);
            	alist.add(y);
            	LCD.drawString("adding..."+ (x+1) + "and" + y, 1, 2+cups);
            }
        	
        	if (y + 1 < 5 && graph[x][y+1] == 0) {
        		y = y + 1;
        		graph[x][y] = 100;//mark visited
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 90));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else if (x + 1 < 5 && graph[x+1][y] == 0) {
        		x = x + 1;
        		graph[x][y] = 100;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 0));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else if (y - 1 >= 0 && graph[x][y-1] == 0) {
        		y = y - 1;
        		graph[x][y] = 100;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, -90));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else {//even if the backward is marked visited, go back
        		x = x - 1;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 180));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        }
        
        if (pp.getPose().getHeading() < -80 && pp.getPose().getHeading() > -100) {//headed downward
        	if (data.getSonicValue() < 30) {
            	graph[x][y-1] = 100;
            	cups++;
            	alist.add(x);
            	alist.add(y-1);
            	LCD.drawString("adding..."+ x + "and" + (y-1), 1, 2+cups);
            }
        	if (x + 1 < 5 && graph[x+1][y] == 0) {
        		x = x + 1;
        		graph[x][y] = 100;//mark visited
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 0));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else if (y - 1 >= 0 && graph[x][y-1] == 0) {
        		y = y - 1;
        		graph[x][y] = 100;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, -90));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else if (x - 1 >= 0 && graph[x-1][y] == 0) {
        		x = x - 1;
        		graph[x][y] = 100;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 180));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else {//even if the backward is marked visited, go back
        		y = y + 1;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 90));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        }
        
        if (pp.getPose().getHeading() < 190 && pp.getPose().getHeading() > 170) {//head leftward
        	if (data.getSonicValue() < 30) {
            	graph[x-1][y] = 100;
            	cups++;
            	alist.add(x-1);
            	alist.add(y);
            	LCD.drawString("adding..."+ (x-1) + "and" + y, 1, 2+cups);
            }
        	if (y - 1 >= 0 && graph[x][y-1] == 0) {
        		y = y - 1;
        		graph[x][y] = 100;//mark visited
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, -90));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else if (x - 1 >= 0 && graph[x-1][y] == 0) {
        		x = x - 1;
        		graph[x][y] = 100;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 180));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else if (y + 1 < 5 && graph[x][y+1] == 0) {
        		y = y + 1;
        		graph[x][y] = 100;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 90));
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        	else {//even if the backward is marked visited, go back
        		x = x + 1;
        		nav.addWaypoint(new Waypoint(x*mag, y*mag, 0));		
        		nav.followPath();
        		while(!nav.pathCompleted()){

        			if(closeEnough(x*mag, y*mag))
        				{nav.clearPath();  break; }
        			//do nuthing
        		}
        	}
        }
        }
        Send(alist);
        
	}
	
	
	static PoseProvider pp;
	static boolean closeEnough(int x, int y){
		Pose p = pp.getPose();
		return Math.abs(p.getX() - x) + Math.abs(p.getY() - y) < 2;
	}
	
	static Socket t;
	static ObjectOutputStream p;
	static ServerSocket s;
	public static void Connect() throws IOException {
		t = null;
		p = null;
		s = new ServerSocket(6983);
		System.out.println("SERVER STARTED");
		t = s.accept();
		System.out.println("SERVER CONNECTED");
	}
	
	public static void Send(ArrayList<Integer> w) throws IOException{
		p = new ObjectOutputStream(t.getOutputStream());
		p.writeObject(w);
		p.flush();
		p.close();
		t.close();
		s.close();
	}
}
