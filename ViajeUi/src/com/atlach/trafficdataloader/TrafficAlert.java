package com.atlach.trafficdataloader;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

//TODO: should i make private and getter/setter na lang?
public class TrafficAlert {
	String type;
	String activeFrom_Date;
	String activeFrom_Time;
	String activeTo_Date;
	String activeTo_Time;
	String user;
	String publicDescription;
	LatLng coordinates;
	
	TrafficAlert(String type, 
			String activeFrom_Date,
			String activeFrom_Time,
			String activeTo_Date,
			String activeTo_Time,
			String user,
			String publicDescription,
			LatLng coordinates) {
		this.type = type;
		this.activeFrom_Date = activeFrom_Date;
		this.activeFrom_Time = activeFrom_Time;
		this.activeTo_Date = activeTo_Date;
		this.activeTo_Time = activeTo_Time;
		this.user = user;
		this.publicDescription = publicDescription;
		this.coordinates = coordinates;
	}
	
	public static class TrafficAlertInfo {
		private ArrayList<TrafficAlert> alerts = null;
		
		public TrafficAlertInfo() {
			setAlerts(new ArrayList<TrafficAlert>());
		}

		public TrafficAlertInfo(ArrayList<TrafficAlert> pAlertList) {
			setAlerts(pAlertList);
		}

		public ArrayList<TrafficAlert> getAlerts() {
			return alerts;
		}

		public void setAlerts(ArrayList<TrafficAlert> alerts) {
			this.alerts = alerts;
		}
		
	}
}
