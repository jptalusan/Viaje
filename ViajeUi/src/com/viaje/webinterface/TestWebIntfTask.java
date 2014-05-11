package com.viaje.webinterface;

import android.os.AsyncTask;

/*
 * This Class is for Example/Testing purposes only.
 * 
 * However, if you are going to use the JSONWebInterface class, this class shows basically how you would do it.
 * 	1. Create an AsyncTask inner class within your main class
 * 		e.g.
 * 				public YourClass {
 * 					...
 * 					class LoginAsyncTask extends AsyncTask<Void, Void, Void> {
 * 						@Override
 * 						protected Void doInBackground(Void... params) {
 * 							...
 * 							return null;
 * 						}
 * 					}
 * 				} 
 *  2. Instantiate JSONWebInterface inside doInBackground and do whatever you want with it
 *  	e.g.
 *  		...
 *  		class LoginAsyncTask extends AsyncTask<Void, Void, String> {
 * 				@Override
 * 				protected Void doInBackground(Void... params) {
 * 					JSONWebInterface webInterface = new JSONWebInterface();
 * 
 * 					String response = webInterface.sendLoginRequest("test.user@email.com", "qwertyuiop");
 * 					return response;
 * 				}
 * 				...
 *  		}
 */

public class TestWebIntfTask extends AsyncTask<Void, Void, String> {

//	How to test: Insert the following somewhere in your code:
//
//		...
//		new TestWebIntfTask().execute();
//		...
	
	
	@Override
	protected String doInBackground(Void... params) {
		JSONWebInterface webInterface = new JSONWebInterface();
//		webInterface.sendRegisterRequest("test.user@email.com", "Test User", "qwertyuiop");
		String str = webInterface.sendLoginRequest("test.user@email.com", "qwertyuiop");
//		webInterface.sendGetIncidentsRequest(-1, "incident", null, 1, 20, 1);
//		webInterface.sendPostReportRequest(-1, "incident", 121.057, 14.5877, "Unknown", "Describe the incident here");
		
		return str;
	}
	
	@Override
	protected void onPostExecute(String result) {
		//
	}
	
}
