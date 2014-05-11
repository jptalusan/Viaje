package com.example.otpxmlgetterUI;

import java.util.ArrayList;

public class OTPResponseUI {
	/* Object fields */
	public String date = "";
	public String origin = "";
	public String destination = "";
	public ArrayList<Itinerary> itineraries = null;
	private int routes = 0;
	
	public OTPResponseUI() {
		itineraries = new ArrayList<Itinerary>();
		return;
	}
	
	public int addItinerary(){
		Itinerary temp = new Itinerary();
		
		/* Add itinerary object to ArrayList */
		if (itineraries.add(temp) == false) {
			return -1;
		}
		
		routes++;
		
		/* Return index of itinerary object */
		return itineraries.indexOf(temp);
	}
	
	public void addLegToItinerary(int itineraryId, String routeName, String mode, String dist, String routeId, String start, String end, String polyLine) {
		Itinerary temp = itineraries.get(itineraryId);
		
		temp.addLeg(routeName, mode, dist, routeId, start, end, polyLine);
	}
	
	public int getRoutes() {
		return routes;
	}
	
	public class Itinerary {
		/* Object fields */
		private int transfers = 0;
		public ArrayList<Leg> legs = null;
		
		public Itinerary() {
			legs = new ArrayList<Leg>();
		};
		
		public int addLeg(String routeName, String mode, String dist, String routeId, String start, String end, String polyLine) {
			Leg temp = new Leg();
			
			temp.name = routeName;
			temp.mode = mode;
			temp.dist = dist;
			temp.routeId = routeId;
			temp.start = start;
			temp.end = end;
			temp.polyLine = polyLine;
			
			if (legs.add(temp) == false) {
				/* Failed */
				return -1;
			}

			transfers++;
			
			return legs.indexOf(temp);
		}
		
		public int getTransfers() {
			return transfers;
		}
		
		public class Leg {
			/* Object fields */
			public String name = "";
			public String mode = "";
			public String dist = "";
			public String routeId = "";
			public String start = "";
			public String end = "";
			public String polyLine = "";
		}
	}

}
