package com.viaje.webinterface;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atlach.trafficdataloader.TrafficAlertInfo;
import com.atlach.trafficdataloader.TrafficDataManager;
import com.atlach.trafficdataloader.TrafficAlertInfo.TrafficAlert;
import com.atlach.trafficdataloader.TripInfo.Route;
import com.google.android.gms.maps.model.LatLng;

public class JSONWebInterface {
	private static final String BASE_SERVER_URL = "http://api.travelbudapp.com:8090";
	private static final int STATUS_OK = 0;
	private static final int ERROR_NO_USER_ID = -1;
	private static final int STATUS_FAILED = -2;
	private boolean hasUserId = false;
	private int _userId = -1;
	
	/** Public Functions **/
	public JSONWebInterface () {
		return;
	}
	
	/** Public Functions for Sending Requests to the web server **/
	public String sendLoginRequest(String email, String password) {
		try {
			String jsonStr = createLoginRequest(email, password);
			
			HttpURLConnection urlConn = getServerConnection("/user/login");

			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {

				String respStr = receiveJSONResponse(urlConn);
				if (respStr.equals("")) {
					System.out.println("Warning: JSON Response is empty.");
					return respStr;
				}
				
				/** Parse the _userId value from the returned JSON object **/
				/** TODO You may want to move this to a different spot later */
				try {
					System.out.println(respStr);
					// Create the JSON object from the returned text
					JSONObject jsonObjOutput = new JSONObject(respStr);
					
					_userId = Integer.parseInt(jsonObjOutput.get("user_id").toString());
					
					this.hasUserId = true;
					
					// Output the complete object
					System.out.println("JSON Response Received: " + jsonObjOutput.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				/** END **/
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	public String sendRegisterRequest(String email, String name, String password) {
		try {
			String jsonStr = createRegisterRequest(email, name, password);
			
			HttpURLConnection urlConn = getServerConnection("/user/register");
			
			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {
				String respStr = receiveJSONResponse(urlConn);
				if (respStr.equals("")) {
					System.out.println("Warning: JSON Response is empty.");
					return respStr;
				}

				/** Parse the _userId value from the returned JSON object **/
				/** TODO You may want to move this to a different spot later */
				try {
					System.out.println(respStr);
					// Create the JSON object from the returned text
					JSONObject jsonObjOutput = new JSONObject(respStr);
					
					_userId = Integer.parseInt(jsonObjOutput.get("user_id").toString());
					
					this.hasUserId = true;
					
					// Output the complete object
					System.out.println("JSON Response Received: " + jsonObjOutput.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				/** END **/
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	public String sendPostReportRequest(int userId, String type, double lat, double lon, String location, String desc, String username) {
		try {
			String jsonStr = createPostReportRequest(type, lat, lon, location, desc, username);
			
			int uid = (userId >= 0) ? userId : _userId;
			HttpURLConnection urlConn = getServerConnection("/data/submit?user_id=" + Integer.toString(uid));
			
			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {
				String respStr = receiveJSONResponse(urlConn);
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	public String sendSubmitRoutesRequest(int userId, List<Route> routes) {
		try {
			String jsonStr = createSubmitRoutesRequest(routes);
			
			int uid = (userId >= 0) ? userId : _userId;
			HttpURLConnection urlConn = getServerConnection("/stats/route/submit?user_id=" + Integer.toString(uid));
			
			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {
				String respStr = receiveJSONResponse(urlConn);
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	public String sendSubmitSearchRequest(int userId, String from, String to) {
		try {
			String jsonStr = createSubmitSearchRequest(from, to);
			
			int uid = (userId >= 0) ? userId : _userId;
			HttpURLConnection urlConn = getServerConnection("/stats/search/submit?user_id=" + Integer.toString(uid));
			
			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {
				String respStr = receiveJSONResponse(urlConn);
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	public String sendGetIncidentsRequest(int userId, String type, List<Route> routeList, int page, int count, int daysPast) {
		try {
			String jsonStr = createGetIncidentsRequest(type, routeList, page, count, daysPast);

			int uid = (userId >= 0) ? userId : _userId;
			HttpURLConnection urlConn = getServerConnection("/data/search?user_id=" + Integer.toString(uid) + "");
			
			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {
				String respStr = receiveJSONResponse(urlConn);
				if (respStr.equals("")) {
					System.out.println("Warning: JSON Response is empty.");
					return respStr;
				}
				
				// DEBUG
				System.out.println("Received JSON Response: " + respStr);
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	public String sendGetAllIncidentsRequest(int userId, String type, int page, int count, int daysPast) {
		try {
			String jsonStr = createGetAllIncidentsRequest(type, page, count, daysPast);

			int uid = (userId >= 0) ? userId : _userId;
			HttpURLConnection urlConn = getServerConnection("/data/search?user_id=" + Integer.toString(uid) + "");
			
			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {
				String respStr = receiveJSONResponse(urlConn);
				if (respStr.equals("")) {
					System.out.println("Warning: JSON Response is empty.");
					return respStr;
				}
				
				// DEBUG
				System.out.println("Received JSON Response: " + respStr);
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	public String sendGetTopRoutesRequest(int userId, int count, int daysPast) {
		try {
			String jsonStr = createGetTopStatsRequest(count, daysPast);
			
			int uid = (userId >= 0) ? userId : _userId;
			HttpURLConnection urlConn = getServerConnection("/stats/route/get_top?user_id=" + Integer.toString(uid));
			
			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {
				String respStr = receiveJSONResponse(urlConn);
				if (respStr.equals("")) {
					System.out.println("Warning: JSON Response is empty.");
					return respStr;
				}
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	public String sendGetTopSearchesRequest(int userId, int count, int daysPast) {
		try {
			String jsonStr = createGetTopStatsRequest(count, daysPast);
			
			int uid = (userId >= 0) ? userId : _userId;
			HttpURLConnection urlConn = getServerConnection("/stats/search/get_top?user_id=" + Integer.toString(uid));
			
			if (sendJSONRequest(urlConn, jsonStr) == STATUS_OK) {
				String respStr = receiveJSONResponse(urlConn);
				if (respStr.equals("")) {
					System.out.println("Warning: JSON Response is empty.");
					return respStr;
				}
				
				return respStr;
			}
		} catch (IOException e) {
			System.out.println("IOException occurred!");
			e.printStackTrace();
			return "";
		}
		return "";
	}
	
	/** Public Functions for parsing JSON requests **/
	
	public TrafficAlertInfo extractAlerts(String jsonString) {
		String jsonStr = jsonString;
		TrafficAlertInfo alertInfo = new TrafficAlertInfo();
		
		if (jsonString.charAt(0) != '{') {
			jsonStr = "{ \"alerts\" : " + jsonStr + " }";
		}
		
		try {
			System.out.println(jsonStr);
			// Create the JSON object from the returned text
			JSONObject jsonObject = new JSONObject(jsonStr);

			JSONArray alerts = jsonObject.getJSONArray("alerts");
			int alertsLength = alerts.length();

			/*--------- alerts START -----------*/
			for(int i = 0; i < alertsLength; i++) {
				/*--------- child node START -----------*/
				JSONObject alertsChildNode = alerts.getJSONObject(i);
				
				String username = alertsChildNode.get("user_name").toString();
				String location = alertsChildNode.get("location").toString();
				String desc = alertsChildNode.get("description").toString();
				String type = alertsChildNode.get("type").toString();
				String timestamp = alertsChildNode.get("created_at").toString();
				
				JSONArray alertCoords = alertsChildNode.getJSONArray("coords");
				
				double lon = 0.0;
				double lat = 0.0;
				
				if (alertCoords.length() == 2) {
					lon = alertCoords.getDouble(0);
					lat = alertCoords.getDouble(1);
				}
				
				alertInfo.alerts.add(new TrafficAlert(username, location, desc, type, timestamp, lon, lat));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return alertInfo;
	}
	
	public List<ServerStatsElement> extractTopStatistics(String jsonString) {
		String jsonStr = jsonString;
		List<ServerStatsElement> statsList = new ArrayList<ServerStatsElement>();
		
		if (jsonString.charAt(0) != '{') {
			jsonStr = "{ \"stats\" : " + jsonStr + " }";
		}
		
		try {
			System.out.println(jsonStr);
			// Create the JSON object from the returned text
			JSONObject jsonObject = new JSONObject(jsonStr);

			JSONArray stats = jsonObject.getJSONArray("stats");
			int statsLength = stats.length();

			/*--------- stats START -----------*/
			for(int i = 0; i < statsLength; i++) {
				/*--------- child node START -----------*/
				JSONObject alertsChildNode = stats.getJSONObject(i);
				
				String elemName = alertsChildNode.get("_id").toString();
				int count = alertsChildNode.getInt("n");
				
				statsList.add(new ServerStatsElement(elemName, count));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return statsList;
	}
	
	/** Miscellaneous Public Functions **/
	public int getUserId() {
		if (hasUserId) {
			return this._userId;
		}
		return ERROR_NO_USER_ID;
	}
	
	/** Private JSON Request Creation Functions **/
	private String createRegisterRequest(String email, String name, String password) {
		JSONObject request = new JSONObject();
		
		try {
			request.put("email", email);
			request.put("name", name);
			request.put("password", password);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		/* Return the json string*/
		return request.toString();
	}
	
	private String createLoginRequest(String email, String password) {
		JSONObject request = new JSONObject();
		
		try {
			request.put("email", email);
			request.put("password", password);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		/* Return the json string*/
		return request.toString();
	}
	
	private String createGetIncidentsRequest(String type, List<Route> routeList, int page, int count, int daysPast) {
		JSONObject request = new JSONObject();
		
		try {
			JSONArray routeCoords = getRouteCoordsJSONArray(routeList);

			request.put("type", type);
			request.put("page", page);
			request.put("count", count);
			request.put("days_past", daysPast);
			
			request.put("route", routeCoords);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		/* Return the json string*/
		return request.toString();
	}
	
	private String createGetAllIncidentsRequest(String type, int page, int count, int daysPast) {
		JSONObject request = new JSONObject();
		
		try {
			request.put("type", type);
			request.put("page", page);
			request.put("count", count);
			request.put("days_past", daysPast);
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		/* Return the json string*/
		return request.toString();
	}
	
	private String createPostReportRequest(String type, double lat, double lon, String location, String desc, String username) {
		JSONObject request = new JSONObject();
		
		try {
			request.put("type", type);
			request.put("lon", lon);
			request.put("lat", lat);
			request.put("location", location);
			request.put("description", desc);
			request.put("user_name", username);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		/* Return the json string*/
		return request.toString();
	}
	

	private String createSubmitRoutesRequest(List<Route> routes) {
		JSONObject request = new JSONObject();
		
		try {
			JSONArray routesArray = getRouteNamesJSONArray(routes);
			
			request.put("route", routesArray);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		/* Return the json string*/
		return request.toString();
	}
	
	private String createSubmitSearchRequest(String from, String to) {
		JSONObject request = new JSONObject();
		
		try {
			request.put("query", from + " to " + to);
			request.put("from", from);
			request.put("to", to);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		/* Return the json string*/
		return request.toString();
	}
	
	private String createGetTopStatsRequest(int count, int daysPast) {
		JSONObject request = new JSONObject();
		
		try {
			request.put("count", count);
			request.put("days_past", daysPast);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		/* Return the json string*/
		return request.toString();
	}
	
	/* Private Utility Functions */
	
	private JSONArray getRouteNamesJSONArray(List<Route> routeList) {
		JSONArray routeNames = new JSONArray();
		
		if (routeList == null) {
			JSONObject routeObj = new JSONObject();
			
			try {
				routeObj.put("id", "dummy_id");
				routeObj.put("name", "dummy_route_name");
			
				routeNames.put(routeObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return routeNames;
		}
		
		try {
			for (int i = 0; i < routeList.size(); i++) {
				JSONObject routeObj = new JSONObject();
				
				routeObj.put("id", routeList.get(i).agency);
				routeObj.put("name", routeList.get(i).name);
				
				routeNames.put(routeObj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			/* return an empty JSON Array instead if we managed to mess this one up */
			return (new JSONArray());
		}
		
		return routeNames;
	}
	
	private JSONArray getRouteCoordsJSONArray(List<Route> routeList) {
		JSONArray routeCoords = new JSONArray();
		
		if (routeList == null) {
			JSONArray coordObj = new JSONArray();
			
			try {
				coordObj.put(121.05848908424376);
				coordObj.put(14.592668539269024);
			
				routeCoords.put(coordObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return routeCoords;
		}
		
		try {
			for (int i = 0; i < routeList.size(); i++) {
				List<LatLng> coordList = TrafficDataManager.decodePolyToLatLng(routeList.get(i).points);
				
				for (int j = 0; j < coordList.size(); j++) {
					JSONArray coordObj = new JSONArray();
					
					/* Get the lon and lat values */
					coordObj.put(coordList.get(j).longitude);
					coordObj.put(coordList.get(j).latitude);
					
					/* Then put it in the routeCoords JSON Array */
					routeCoords.put(coordObj);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			/* return an empty JSON Array instead if we managed to mess this one up */
			return (new JSONArray());
		}
		return routeCoords;
	}
	
	private HttpURLConnection getServerConnection(String requestSuffix) throws IOException {
		URL url;
		HttpURLConnection urlConn;
		
		url = new URL(BASE_SERVER_URL + "" +requestSuffix);
		
		System.out.println("POST Request URL: " + url.toString());
		
		urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setRequestMethod("POST");
		urlConn.setDoOutput(true);
		urlConn.setDoInput(true);
		urlConn.setRequestProperty("Content-Type", "application/json");
		urlConn.setRequestProperty("Accept", "application/json");
		urlConn.setConnectTimeout(60000);
		urlConn.setReadTimeout(60000);
		
		return urlConn;
	}
	
	private int sendJSONRequest(HttpURLConnection urlConn, String jsonStr) throws IOException {
		urlConn.setRequestProperty("Content-Length", Integer.toString(jsonStr.length()));

		try {
			if (jsonStr.equals("") == false) {
				urlConn.getOutputStream().write(jsonStr.getBytes());
				urlConn.getOutputStream().flush();
				// Debug
				System.out.println("Request Body Written:" + jsonStr + "");
			}
			
			urlConn.connect();
		} catch (SocketTimeoutException e) {
			System.out.println("Connection Timed Out!");
			return STATUS_FAILED;
		}
		
		return STATUS_OK;
	}
	
	
	@SuppressWarnings("deprecation")
	private void printErrorResponse(HttpURLConnection conn) throws IOException {
		DataInputStream input = new DataInputStream(conn.getErrorStream());

		String str;
		
		// FIXME Uses deprecated method --Francis
		// http://nicholaschase.com/2011/01/03/creating-and-using-json-objects-with-java/
		// http://mycenes.wordpress.com/tag/httpurlconnection/
		System.out.println("Response: " + conn.getResponseMessage());
		System.out.println("Response code: " + conn.getResponseCode());
		while (null != ((str = input.readLine()))) {
			//Debug
			System.out.println("> " + str);
		}

		input.close();
	}
	
	@SuppressWarnings("deprecation")
	private String receiveJSONResponse(HttpURLConnection conn) throws IOException {
		String fullStr = "";
		try {
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				printErrorResponse(conn);
				return "";
			}
			
			DataInputStream input = new DataInputStream(conn.getInputStream());
			String str;
	
			// FIXME Uses deprecated method --Francis
			// http://nicholaschase.com/2011/01/03/creating-and-using-json-objects-with-java/
			// http://mycenes.wordpress.com/tag/httpurlconnection/
			while (null != ((str = input.readLine()))) {
				fullStr += str;
			}
		} catch (SocketTimeoutException e) {
			System.out.println("Connection Timed Out!");
			return "";
		}
		
		return fullStr;
	}
	
	public static class ServerStatsElement {
		public String value = "";
		public int count = 0;
		public ServerStatsElement (String val, int c) {
			this.value = val;
			this.count = c;
		}
	}
}