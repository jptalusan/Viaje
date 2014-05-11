package com.atlach.trafficdataloader;

import java.util.ArrayList;

/* Short Desc: 	Storage object for Trip info */
/* 				Trip Info > Trips > Routes   */ 

public class TripInfo {
	private int tripCount = 0;
	public ArrayList<Trip> trips = null;
	public String date;
	public String origin;
	public String destination;
	
	public TripInfo() {
		trips = new ArrayList<Trip>();
	}
	
	public int addTrip() {
		Trip temp = new Trip();
		
		if (trips.add(temp) == false) {
			/* Failed */
			return -1;
		}
		
		tripCount++;
		
		return trips.indexOf(temp);
	}
	
	public int addRouteToTrip(int tripId, String routeName, String mode, String dist, String agency, String start, String end, String points) {
		int result = -1;
		Trip temp = trips.get(tripId);
		
		if (temp != null) {
			result = temp.addRoute(routeName, mode, dist, agency, start, end, points);
		}
		
		return result;
	}
	
	public int getTripCount() {
		return tripCount;
	}
	
	public static class Trip {
		public double totalDist = 0.0;
		public double totalCost = 0.0;
		public int totalTraffic = 0;
		
		private int transfers = 0;
		public ArrayList<Route> routes = null;
		
		public Trip() {
			routes = new ArrayList<Route>();
		};
		
		public int addRoute(String routeName, String mode, String dist, String agency, String start, String end, String points) {
			Route temp = new Route();
			
			temp.name = routeName;
			temp.mode = mode;
			temp.dist = dist;
			temp.agency = agency;
			temp.start = start;
			temp.end = end;
			temp.points = points;
			
			if (routes.add(temp) == false) {
				/* Failed */
				return -1;
			}
	
			transfers++;
			
			return routes.indexOf(temp);
		}
		
		public int getTransfers() {
			return transfers;
		}
	}
	
	public static class Route {
		/* Object fields */
		public String name = "";
		public String mode = "";
		public String dist = "0.0";
		public String agency = "";
		public String start = "";
		public String end = "";
		public String points = "";
		public String cond = "";
		//public String cost = "0.0";
		
		public double costMatrix[] = {0.0, 0.0, 0.0, 0.0};
		
		public double getRegularCost(boolean isDiscounted) {
			if (isDiscounted) {
				return costMatrix[1];
			}
			return costMatrix[0];
		}
		
		public double getSpecialCost(boolean isDiscounted) {
			if (isDiscounted) {
				return costMatrix[2];
			}
			return costMatrix[3];
		}
	}
}
