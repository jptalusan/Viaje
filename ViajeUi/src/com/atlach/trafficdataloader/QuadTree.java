package com.atlach.trafficdataloader;

import java.util.ArrayList;
import java.util.List;

//import android.util.Log;

public class QuadTree {
	public static final int MAX_OBJECTS = 5; 
	public static final int MAX_LEVELS = 5;
	
	private int level = 0;
	private List<MonitoredLocation> objects;
	private Rectangle bounds;
	private QuadTree[] nodes;
	
	public QuadTree(int pLevel, Rectangle pBounds) {
		level = pLevel;
		objects = new ArrayList<MonitoredLocation>();
		bounds = pBounds;
		nodes = new QuadTree[4];
	}
	
	public void clear() {
		objects.clear();
		
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].clear();
				nodes[i] = null;
			}
		}
	}
	
	public void split() {
		double subWidth = bounds.getWidth() / 2;
		double subHeight = bounds.getHeight() / 2;
		double x = bounds.getX();
		double y = bounds.getY();

		//Log.i("QuadTree", "Splitting QuadTree at level " + level);
		
		nodes[0] = new QuadTree(level+1, new Rectangle(x + subWidth, y, subWidth, subHeight));
		nodes[1] = new QuadTree(level+1, new Rectangle(x, y, subWidth, subHeight));
		nodes[2] = new QuadTree(level+1, new Rectangle(x, y + subHeight, subWidth, subHeight));
		nodes[3] = new QuadTree(level+1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
		
	}
	
    private int getIndex(MonitoredLocation pPoint) {
		int index = -1;
		double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
		double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);
 
		// Object can completely fit within the top quadrants
		boolean topQuadrant = (pPoint.getY() < horizontalMidpoint);
		// Object can completely fit within the bottom quadrants
		boolean bottomQuadrant = (pPoint.getY() > horizontalMidpoint);
 
		// Object can completely fit within the left quadrants
		if (pPoint.getX() < verticalMidpoint) {
				if (topQuadrant) {
				  index = 1;
				}
				else if (bottomQuadrant) {
				  index = 2;
				}
		 }
		 // Object can completely fit within the right quadrants
		 else if (pPoint.getX() > verticalMidpoint) {
		  if (topQuadrant) {
				 index = 0;
		  }
		  else if (bottomQuadrant) {
				 index = 3;
		  }
		}
 
		return index;
    }
    
    public void insert(MonitoredLocation pPoint) {
    	if (nodes[0] != null) {
    		int index = getIndex(pPoint);
    		
        	//Log.i("[QuadTree]", "Inserting to Index " + index );
     
    		if (index != -1) {
    			nodes[index].insert(pPoint);
    			return;
    		}
    	}

    	//Log.i("[QuadTree]", "Inserting location to QuadTree: " + pPoint.getX() + ", " + pPoint.getY());
        objects.add(pPoint);
     
    	if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
    		if (nodes[0] == null) { 
    				split(); 
    		 }
    	 
    		int i = 0;
    		while (i < objects.size()) {
    			int index = getIndex(objects.get(i));
            	//Log.i("[QuadTree]", "Moving (" + objects.get(i).toString() + ") to Index " + index );
    			if (index != -1) {
    				nodes[index].insert(objects.remove(i));
    			} else {
    				i++;
    			}
    		}
    	}
    }
    
    public List<MonitoredLocation> retrieve(List<MonitoredLocation> returnObjects, MonitoredLocation pPoint) {
		int index = getIndex(pPoint);
		if (index != -1 && nodes[0] != null) {
			//Log.i("QuadTree", "Moving to node " + index);
			nodes[index].retrieve(returnObjects, pPoint);
		}
		
		//Log.i("QuadTree", "Object List Size: " + objects.size());
 
		returnObjects.addAll(objects);
		
		//Log.i("QuadTree", "Retrieval: " + returnObjects.size());
 
		return returnObjects;
    }
    
    public static class Rectangle {
    	private double width = 0.0;
    	private double height = 0.0;
    	private double x = 0.0;
    	private double y = 0.0;
    	
    	public Rectangle(double xVal, double yVal, double w, double h) {
    		x = xVal;
    		y = yVal;
    		width = w;
    		height = h;
    	}
    	
    	public double getWidth() {
    		return width;
    	}
    	public double getHeight() {
    		return height;
    	}
    	public double getX() {
    		return x;
    	}
    	public double getY() {
    		return y;
    	}
    	public void setWidth(double width) {
    		this.width = width;
    	}
    	public void setHeight(double height) {
    		this.height = height;
    	}
    	public void setX(double x) {
    		this.x = x;
    	}
    	public void setY(double y) {
    		this.y = y;
    	}
    }
}