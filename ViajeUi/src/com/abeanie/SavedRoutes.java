package com.abeanie;

public class SavedRoutes {
	private String to;
	private String from;
	private String cost;
	private String distance;
	private String traffic;
	
	// Constructor for the Phonebook class
	public SavedRoutes(String to, String from, String cost, String distance, String traffic) {
		super();
		this.to = to;
		this.from = from;
		this.cost = cost;
		this.distance = distance;
		this.traffic = traffic;
	}
	
	// Getter and setter methods for all the fields.
	// Though you would not be using the setters for this example,
	// it might be useful later.
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getCost() {
		return cost;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getTraffic() {
		return traffic;
	}
	public void setTraffic(String traffic) {
		this.traffic = traffic;
	}
}
