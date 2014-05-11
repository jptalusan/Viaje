package com.example.otpxmlgetterUI;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;

public class JSONParserUI {
	public String route = "";
	public String mode = "";
	public String distance = "";
	public String routeId = "";
	public String fromName ="";
	public String toName ="";
	public String points ="";
	public int itineraryCount = 0;
	public boolean checkIfFirst = true;
	public List<LatLng> list = null;
	//public List<LatLng> listDEBUG[] = null;
	Context mContext;
	
	public JSONParserUI(Context context) {
		this.mContext = context;
	}
	
	public OTPResponseUI getTestOTPResponse(String JSONFile) {
		OTPResponseUI response = new OTPResponseUI();
		final String strJson = JSONFile;
		itineraryCount = getItineraryCount(JSONFile);

		UtilsUI util = new UtilsUI();
		for(int x = 0; x < itineraryCount; x++) {
			//listDEBUG[x] = util.decodePoly("");
		}
		list = util.decodePoly("");
		JSONObject jsonResponse;

		try {
			//Log.i("is it coming here=", "" + strJson);
			/******
			 * Creates a new JSONObject with name/value mappings from
			 * the JSON string.
			 ********/
			jsonResponse = new JSONObject(strJson);
			
			/*--------- plan -----------*/
			JSONObject plan = jsonResponse.optJSONObject("plan");
			/*--------- plan TO -----------*/
			JSONObject planFrom = plan.optJSONObject("from");
			/*--------- plan FROM -----------*/
			JSONObject planTo = plan.optJSONObject("to");
			/*--------- itineraries -----------*/
			JSONArray itineraries = plan.getJSONArray("itineraries");
			int itinerariesLength = itineraries.length();

			response.date = plan.get("date").toString();
			response.origin = planFrom.get("name").toString();
			response.destination = planTo.get("name").toString();
			
			/*--------- itineraries START -----------*/
			for(int i = 0; i < itinerariesLength; i++) {
				int itinId = response.addItinerary();
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
					
					//for now debug mode, just display first route
					List<LatLng> listTemp = util.decodePoly(points);
					if(checkIfFirst) {
						for(int l=0; l < listTemp.size() - 1; l++) {
							//place this in an array  so list[].add so you can pick which main route to use
							this.list.add(listTemp.get(l));
							//listDEBUG[i].add(listTemp.get(l));
						}	
					}
					response.addLegToItinerary(itinId, route, mode, distance, routeId, fromName, toName, points);			
				}
				checkIfFirst = false;
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}		
		return response;
	}
	
	
	public List<LatLng> getLatLng() {
		return this.list;
	}
	
	private int getItineraryCount(String fileName) {
		int itinerariesLength = 0;
		final String strJson = fileName;

		JSONObject jsonResponse;

		try {
			jsonResponse = new JSONObject(strJson);
			/*--------- plan -----------*/
			JSONObject plan = jsonResponse.optJSONObject("plan");
			/*--------- itineraries -----------*/
			JSONArray itineraries = plan.getJSONArray("itineraries");
			itinerariesLength = itineraries.length();
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		return itinerariesLength;
	}
			
	public int getItineraryCount() {
		return this.itineraryCount;
	}
}
