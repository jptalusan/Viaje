package com.viaje.main;

public interface HistDataPopupEvent {
	public void onHistDataOptsFinalized(int status, int dayOfWeek, int timeOfDay, String weather);
}
