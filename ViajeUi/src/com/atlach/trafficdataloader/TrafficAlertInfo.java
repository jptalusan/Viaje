package com.atlach.trafficdataloader;

import java.util.ArrayList;
import java.util.List;

public class TrafficAlertInfo {
	public List<TrafficAlert> alerts = null;
	
	public TrafficAlertInfo() {
		alerts = new ArrayList<TrafficAlert>();  
	}
	
	public static class TrafficAlert {
		public String username = "";
		public String location = "";
		public String description = "";
		public String type = "";
		public double lon = 0.0;
		public double lat = 0.0;
		public String timestamp = "";
		
		public TrafficAlert(String user, String loc, String desc, String type, String time, double lon, double lat) {
			this.username = user;
			this.location = loc;
			this.description = desc;
			this.type = type;
			this.timestamp = time;
			this.lon = lon;
			this.lat = lat;
		}
		
		public String toString(){
			String loc = (location.equals("") == false) ? location : ("(" + Double.toString(lon) + ", " + Double.toString(lat) + ")");
			
			return (">" + type + ": " + description + " at " + loc + " (source: " + username + ")");
		}
	}
}
