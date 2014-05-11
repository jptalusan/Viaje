package com.atlach.trafficdataloader;

public class MonitoredLocation extends Point {
    public String name = "";
    public String area = "";

    public MonitoredLocation(String area, String name, double lat, double lon) {
    	super(lon, lat);
        this.name = name;
        this.area = area;
    }
    
    public double getLon() {
    	return this.getX();
    }

    public double getLat() {
    	return this.getY();
    }
    
    @Override
    public String toString() {
    	return ("" + this.getLon() + ", " + this.getLat()); 
    }
}
