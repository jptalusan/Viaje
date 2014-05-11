package com.atlach.trafficdataloader;

public class Point {
	protected double x;
	protected double y;
	
	public Point(double xVal, double yVal) {
		x = xVal;
		y = yVal;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
}
