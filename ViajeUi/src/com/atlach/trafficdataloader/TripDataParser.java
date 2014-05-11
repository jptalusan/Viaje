package com.atlach.trafficdataloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atlach.trafficdataloader.TripInfo.Route;
import com.example.otpxmlgetterUI.UtilsUI;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.util.Log;

/* Short Desc: Parses JSON trip data into a TripInfo object */ 

public class TripDataParser {
	@SuppressWarnings("unused")
	private Context _context = null;

	public static final int ROUTE_DIR_UNKNOWN = 0;
	public static final int ROUTE_NORTHBOUND = 1;
	public static final int ROUTE_SOUTHBOUND = 2;

	public TripDataParser(Context c) {
		_context = c;
	}
	
	/** PUBLIC FUNCTIONS **/
	public TripInfo getTripInfoFromUrl(String urlStr) {
		InputStream tripUrlStream = null;
		String tripFileStr = "";
		
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet get = new HttpGet(urlStr);
			//if header is not set, response is in json format
			get.setHeader("Accept", "application/json");
			HttpResponse response = httpclient.execute(get);
			tripUrlStream = response.getEntity().getContent();
		} catch (Exception e) {
			Log.e("[GET REQUEST]", "Network exception", e);
			return null;
		}

		/* Store Trip URL response into a string */
		tripFileStr = this.getStringFromInputStream(tripUrlStream);
		
		/* Close our file stream */
		try {
			tripUrlStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/* Parse JSON format TripFile contents and return */
		return parseTripInfoFromJSON(tripFileStr);
	}
	
	public TripInfo getTripInfoFromFile(String filePath, String fileName) throws FileNotFoundException {
		File tripFile = new File(filePath, fileName);
		
		if (tripFile.exists() == false) {
			System.out.println("File does not exist");
			return null;
		}
		
		FileInputStream tripFileStream = new FileInputStream(tripFile);
		String tripFileStr = "";
		
		/* Store TripFile contents into a string */
		tripFileStr = this.getStringFromInputStream(tripFileStream);
		
		/* Close our file stream */
		try {
			tripFileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/* Parse JSON format TripFile contents and return */
		return parseTripInfoFromJSON(tripFileStr);
	}
	
	public ArrayList<TrafficAlert> getTrafficAlertInfoFromFile(String filePath, String fileName) throws FileNotFoundException {
		File alertFile = new File(filePath, fileName);
		
		if (alertFile.exists() == false) {
			System.out.println("File does not exist");
			return null;
		}
		
		/* Parse JSON format alertFile contents and return */
		return parseAlertInfoFromCSV(alertFile);
	}
	
	public static List<LatLng> getLatLngCoordList(TripInfo tripInfo, int tripId) {
		List<LatLng> tripCoordList = new ArrayList<LatLng>();
		
		for (int j = 0; j < tripInfo.trips.get(tripId).routes.size(); j++) {
			Route route = tripInfo.trips.get(tripId).routes.get(j);
			
			tripCoordList.addAll(TrafficDataManager.decodePolyToLatLng(route.points));
		}
		
		return tripCoordList;
	}
	
	public static int getRouteDirectionality(Route route) {
		if (route == null) {
			System.out.println("Could not determine route directionality!");
			return ROUTE_DIR_UNKNOWN;
		}
		List<LatLng> decodedRoute = TrafficDataManager.decodePolyToEndpoints(route.points);
		
		if (decodedRoute == null) {
			System.out.println("Could not determine route directionality!");
			return ROUTE_DIR_UNKNOWN;
		}
		int firstElem = 0;
		int lastElem = decodedRoute.size() - 1;
		
		if (firstElem == lastElem) {
			System.out.println("Could not determine route directionality!");
			return ROUTE_DIR_UNKNOWN;
		}
		
		double diff =  decodedRoute.get(lastElem).latitude - decodedRoute.get(firstElem).latitude;
		
		if (diff > 0) {
			return ROUTE_NORTHBOUND;
		}

		return ROUTE_SOUTHBOUND;
	}
	
	/** PRIVATE FUNCTIONS **/
	private String getStringFromInputStream(InputStream inStream) {
		String line = "";
		StringBuilder total = new StringBuilder();
		
		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(inStream));
		// Read response until the end
		try {
			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
		} catch (IOException e) {
			
		}
      return total.toString();
	}
	
	private ArrayList<TrafficAlert> parseAlertInfoFromCSV(File csvFile) throws FileNotFoundException {
		@SuppressWarnings("unused") /* TODO: Unused for now. Will do something with this later, I assume? */
		ArrayList<String> values = new ArrayList<String>();
		ArrayList<TrafficAlert> alertList = new ArrayList<TrafficAlert>();
		
		//InputStream is = getAssets().open("alerts.txt");
		//BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		BufferedReader br = new BufferedReader(new FileReader(csvFile));
	    try {
	        String line;
	        while ((line = br.readLine()) != null) {
	        	line=line.replaceAll(",,", ",***,");
	        	String[] RowData = line.split(",");
	        	//because not all rows are equal, remove extraneous. (kasalanan na ng mmda yan)
	             if(RowData.length == 9) {
		             for(int i = 0; i < RowData.length; i++)
		             {
		            	 if(isNumeric(RowData[7])) {
		            		 LatLng coordinates = new LatLng(Double.parseDouble(RowData[7]), Double.parseDouble(RowData[8]));
		            		 alertList.add(new TrafficAlert(RowData[0], RowData[1], RowData[2], 
		            				 						RowData[3], RowData[4], RowData[5], 
		            				 						RowData[6], coordinates));
		            	 }
		             }

	             } else {
	            	 continue;
	             }
	        }
	        
	    } catch (IOException ex) {
	        // handle exception
	    } finally {
	        try {
	        	br.close();
	        }
	        catch (IOException e) {
	            // handle exception
	        }
	    }
		return alertList;
	}
	
	private TripInfo parseTripInfoFromJSON(String jsonStr) {
		/* Initialize container variables */
		String route = "";
		String mode = "";
		String distance = "";
		String routeId = "";
		String fromName ="";
		String toName ="";
		String points ="";
		TripInfo tripInfo = new TripInfo();
		
		JSONObject jsonResponse;

		try {
			/******
			 * Creates a new JSONObject with name/value mappings from
			 * the JSON string.
			 ********/
			jsonResponse = new JSONObject(jsonStr);
			
			/*--------- plan -----------*/
			JSONObject plan = jsonResponse.optJSONObject("plan");
			
			/* Catch the case where the OTP server says the trip isnt possible --Francis */
			if (plan == null) {
				Log.e("TripDataParser", "Trip does not seem to be possible!");
				System.out.println(jsonStr);
				return null;
			}
			/*--------- plan TO -----------*/
			JSONObject planFrom = plan.optJSONObject("from");
			/*--------- plan FROM -----------*/
			JSONObject planTo = plan.optJSONObject("to");
			/*--------- itineraries -----------*/
			JSONArray itineraries = plan.getJSONArray("itineraries");
			int itinerariesLength = itineraries.length();

			tripInfo.date = plan.get("date").toString();
			tripInfo.origin = planFrom.get("name").toString();
			tripInfo.destination = planTo.get("name").toString();
			
			/*--------- itineraries START -----------*/
			for(int i = 0; i < itinerariesLength; i++)
			{
				int itinId = tripInfo.addTrip();
				
				JSONObject jsonChildNode = itineraries.getJSONObject(i);
				
				/*--------- legs -----------*/
				JSONArray legsArray = jsonChildNode.getJSONArray("legs");
				for(int j = 0; j < legsArray.length(); j++) {
					
					/*--------- legs START -----------*/
					JSONObject legsChildNode = legsArray.getJSONObject(j);
					
					JSONObject legGeomObjectInLegs = legsChildNode.optJSONObject("legGeometry");
					/*--------- from -----------*/
					JSONObject fromObjectInLegs = legsChildNode.optJSONObject("from");
					/*--------- to -----------*/
					JSONObject toObjectInLegs = legsChildNode.optJSONObject("to");
					if(!(legsChildNode.isNull("route"))) {
						route = legsChildNode.get("route").toString();
					}
					
					if(!(legsChildNode.isNull("mode"))) {
						mode = legsChildNode.get("mode").toString();
					}
					
					if(!(legsChildNode.isNull("distance"))) {
						distance = legsChildNode.get("distance").toString();
					}
					
					if(!(legsChildNode.isNull("routeId"))) {
						routeId = legsChildNode.get("routeId").toString();
					}
					
					if(!(fromObjectInLegs.isNull("name"))) {
						fromName = fromObjectInLegs.get("name").toString();
					}
					
					if(!(toObjectInLegs.isNull("name"))) {
						toName = toObjectInLegs.get("name").toString();
					}

					if(!(legGeomObjectInLegs.isNull("points"))) {
						points = legGeomObjectInLegs.get("points").toString();
					}
				
					int routeIndex = tripInfo.addRouteToTrip(itinId, route, mode, distance, routeId, fromName, toName, points);
					
					/* Workarounds to obtain the cost matrix and correct the mode of transit during parsing
					 *  --Francis  */
					/* Assign cost here */
					UtilsUI utils = new UtilsUI();
					double[] costMatrix = utils.getRouteCostMatrix(tripInfo.trips.get(itinId).routes.get(routeIndex));
					
					tripInfo.trips.get(itinId).routes.get(routeIndex).costMatrix[0] = costMatrix[0];
					tripInfo.trips.get(itinId).routes.get(routeIndex).costMatrix[1] = costMatrix[1];
					tripInfo.trips.get(itinId).routes.get(routeIndex).costMatrix[2] = costMatrix[2];
					tripInfo.trips.get(itinId).routes.get(routeIndex).costMatrix[3] = costMatrix[3];

					Route r = tripInfo.trips.get(itinId).routes.get(routeIndex);
					System.out.println("COST MATRIX: " + r.costMatrix[0] + ", " + r.costMatrix[1] + ", " + r.costMatrix[2] + ", " + r.costMatrix[3]);
					/* Assign correct mode of transpo */
					tripInfo.trips.get(itinId).routes.get(routeIndex).mode = utils.getCorrectModeOfTransit(r);
				}
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}
		
		return tripInfo;		
	}
	
	private static boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}
}
