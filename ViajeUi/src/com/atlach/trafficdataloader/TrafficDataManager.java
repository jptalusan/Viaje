package com.atlach.trafficdataloader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import com.atlach.trafficdataloader.HistDataHelper.HistData;
import com.atlach.trafficdataloader.HistDataHelper.LineInfo;
import com.atlach.trafficdataloader.QuadTree.Rectangle;
import com.atlach.trafficdataloader.TrafficAlert.TrafficAlertInfo;
import com.atlach.trafficdataloader.TripInfo.Route;
import com.atlach.trafficdataloader.TripInfo.Trip;
import com.google.android.gms.maps.model.LatLng;

public class TrafficDataManager {
	private TrafficDataUpdateEvent updateEvent = null;
	private TripInfo tdmTripInfo = null; /* TODO: This will be used with Part#2 and Part#3 later */
	private QuadTree monitoredLocQuadTree = null;
	private Context mContext = null;
	private ArrayList<RouteCoordObject> lastCoordList = null;
	private File mServerTrafficFile = null;
	private File mServerHistDataFile = null;
	private boolean wasHistDataUpdated = false;
	
	public static final int STATUS_OK = 0;
	public static final int STATUS_FAILED = -1;
	public static final int STATUS_TIMEOUT = -2;
//	private static final String TRAFFIC_FILE_URL = "http://dl.dropboxusercontent.com/u/18334976/TrafficRec.txt";
	private static final String TRAFFIC_FILE_URL = "http://travelbudapp.com:8090/mmda_traffic.txt";
	
	public ArrayList<MatchedLocation> matchedLocationList = null;
	public ArrayList<MonitoredLocation> monitoredLocationList = null;
	public ArrayList<File> lineViewFileList = null;
	public boolean isUpdateThreadRunning = false;
	public TrafficDataUpdateRunnable trafficDataUpdateTask = null;
	public HistDataUpdateRunnable histDataUpdateTask = null;
	
	/* Constructor */
	public TrafficDataManager(TrafficDataUpdateEvent event, TripInfo tripInfo, Context c) {
		matchedLocationList = new ArrayList<MatchedLocation>();
		lineViewFileList = new ArrayList<File>();
		updateEvent = event;
		tdmTripInfo = tripInfo;

		/* This stores all known MMDA-monitored traffic locations in a list and a quadtree */
		monitoredLocationList = initMonitoredLocList();
		mContext = c;
	}
	
	/* Starts the Hist Data Update Thread */
	public void startHistDataUpdateTask(boolean isUpdated) {
		if ((isUpdateThreadRunning == false) && (histDataUpdateTask == null)) {
			histDataUpdateTask = new HistDataUpdateRunnable(isUpdated);
			
			new Thread(histDataUpdateTask).start();
		}
	}
	
	/* Starts the Traffic Data Update Thread */
	public void startTrafficDataUpdateTask() {
		if ((isUpdateThreadRunning == false) && (trafficDataUpdateTask == null)) {
			trafficDataUpdateTask = new TrafficDataUpdateRunnable();
			
			new Thread(trafficDataUpdateTask).start();

			/* For quick reference, the functions run by the thread are as follows:
			 * - generateMatchedLocationList()      [Part#2 and Part#3]
			 * - downloadLineViewFiles()            [Part#4]
			 * - extractTrafficDataFromFiles()      [Part#5]
			 */
		}
	}
	
	public void startTrafficDataUpdateTask(int day, String time, String weather, boolean isUpdated) {
		if ((isUpdateThreadRunning == false) && (trafficDataUpdateTask == null)) {
			trafficDataUpdateTask = new TrafficDataUpdateRunnable(day, time, weather, isUpdated);
			
			new Thread(trafficDataUpdateTask).start();

			/* For quick reference, the functions run by the thread are as follows:
			 * - generateMatchedLocationList()      [Part#2 and Part#3]
			 * - downloadLineViewFiles()            [Part#4]
			 * - extractTrafficDataFromFiles()      [Part#5]
			 */
		}
	}
	
	/* Attempts to get the updated traffic data */
	/* This function will block execution if said data is not yet ready */
	public void getTrafficData(TripInfo tripInfoObject) {
		/* For Part #6: Updates the traffic conditions in the TripInfoObject passed as
		 * a parameter to this method */
		if (isUpdateThreadRunning == true) {
			/* Block execution of this function while data is not yet available */
			System.out.println("getTrafficData: Blocked execution due to still running thread");
			return;
		}

		Iterator<Trip> tripIter = tripInfoObject.trips.iterator();
		
		while (tripIter.hasNext()) {
			Trip tmpTrip = tripIter.next();
			Log.i("GetTrafficData", "Processing trip");
			int tripId = tripInfoObject.trips.indexOf(tmpTrip);
			Iterator<Route> routeIter = tmpTrip.routes.iterator();
			
			while (routeIter.hasNext()) {
				Route tmpRoute = routeIter.next();
				Log.i("GetTrafficData", "Processing route " + tmpRoute.name);
				int routeId = (tripInfoObject.trips.get(tripId)).routes.indexOf(tmpRoute);
				
				Iterator<MatchedLocation> matchIter = matchedLocationList.iterator();
				
				boolean hasLight = false;
				boolean hasMedium = false;
				boolean hasHeavy = false;
				int directionality = TripDataParser.getRouteDirectionality(tmpRoute);
				
				while(matchIter.hasNext()) {
					MatchedLocation tempMatchedObj = matchIter.next();
					
					if (tmpRoute.name.equals(tempMatchedObj.route) == true) {
						String matchCondition = "";
						if (directionality == TripDataParser.ROUTE_NORTHBOUND) {
							matchCondition = tempMatchedObj.conditionNB;
						} else if (directionality == TripDataParser.ROUTE_SOUTHBOUND){
							matchCondition = tempMatchedObj.conditionSB;
						} else {
							System.out.println("Failed directionality GET for " + tmpRoute.name);
						}
						
						/* Mechanism for cycling across ALL valid traffic conditions for this route
						 * and calculating the most appropriate result */
						if (matchCondition.equals("Light Traffic")) {
							hasLight = true;
						} else if (matchCondition.equals("Medium Traffic")) {
							hasMedium = true;
						} else if (matchCondition.equals("Heavy Traffic")) {
							hasHeavy = true;
						} else {
						}
					}
				}
				
				String overallCondition = "";
//				int averagedCondition = matchedObjects > 0 ? conditionValue / matchedObjects : 0;
				
//				Log.i("GetTrafficData", "Averaged condition: " + (double)(matchedObjects > 0 ? ((double) conditionValue) / ((double) matchedObjects) : 0));
				
//				switch (averagedCondition) {
//					case 1:
//						overallCondition = "Light Traffic";
//						break;
//					case 2:
//						overallCondition = "Medium Traffic";
//						break;
//					case 3:
//						overallCondition = "Heavy Traffic";
//						break;
//					default:
//						overallCondition = "Not Known";
//				}
				
				overallCondition += ( hasLight ? "Light" : "" );
				overallCondition += ( hasMedium ? "/" :  "");
				overallCondition += ( hasMedium ? "Medium" : "" );
				overallCondition += ( hasHeavy ? "/" :  "");
				overallCondition += ( hasHeavy ? "Heavy" : "" );
				
				//FIXME: this is cheating
				int x=(Math.random()<0.5)?0:1;
				if ( hasLight && hasHeavy ) {
					if (x == 0) {
						overallCondition = overallCondition.replace("Light/", "");
					} else {
						overallCondition = overallCondition.replace("/Heavy", "");						
					}
				}
				
				if ( !hasLight && !hasMedium && !hasHeavy ) {
					overallCondition = "Not Known";
				}
				
				tripInfoObject.trips.get(tripId).routes.get(routeId).cond = overallCondition;
				Log.i("GetTrafficData","Set traffic condition for route " + tripInfoObject.trips.get(tripId).routes.get(routeId).name +
						": " + tripInfoObject.trips.get(tripId).routes.get(routeId).cond );
			}
		}
		
		return;
	}
	
	public TrafficAlertInfo correlateWithAlertData(TripInfo tripInfo, TrafficAlertInfo alertInfo) {
		/* TODO: How will you do the correlation? */
		
		return null;
	}


	/* Basic usage sequence for public methods:
	 *
	 * 	*Calling class should implement TrafficDataUpdateEvent to allow for callback-style functionality
	 *  *Should also have a prepared TripInfo object containing a list of Trips and Routes involved in
	 *	 those Trips
	 *
	 * ....
	 * {
	 *		...
	 * 		tdm = new TrafficDataManager(this, tripInfo, c);
	 *
	 * 		tdm.startTrafficDataUpdateTask();
	 * 		...
	 * }
	 *
	 * public void onTrafficDataUpdate() {
	 *		tdm.getTrafficData(tripInfo);
	 * }
	 **/

	
	private int generateMatchedLocationList() {
		lastCoordList =  new ArrayList<RouteCoordObject>();
		
		/* From Part #2: Generate the coordList */
		/* Cycle through each Trip in the TripInfo object */
		Iterator<Trip> tripIter = tdmTripInfo.trips.iterator();
		
		while (tripIter.hasNext()) {
			Trip tmpTrip = tripIter.next();
			Iterator<Route> routeIter = tmpTrip.routes.iterator();
			
			/* Cycle through each Route in the enclosing Trip object */
			while (routeIter.hasNext()) {
				Route tmpRoute = routeIter.next();

				/* Decode the polyline for this route and add the list of coordinates to
				 * our running list of coordinates */
				lastCoordList.addAll(decodeRoutePoly(tmpRoute.name, tmpRoute.points));
			}
		}

		/* From Part #3: Generate the matchedLocationList */
		/* For each element in coordList */
		int matchCounter = 0; 
		for (int i = 0; i < lastCoordList.size(); i++) {
			final LatLng tmpCoord = lastCoordList.get(i).coord;
			MonitoredLocation tmpLoc = new MonitoredLocation("", "", tmpCoord.latitude, tmpCoord.longitude);
			List<MonitoredLocation> neighborList = new ArrayList<MonitoredLocation>();
			
			monitoredLocQuadTree.retrieve(neighborList, tmpLoc);
			
			/* For each neighboring coordinate, determine which one is nearest */
			int nearestCandidateIndex = -1;
			double nearestDistance = 9000; /* OVER 9000! Kilometers. Seriously this is just some arbitrary large number. */
			
			for (int j = 0; j < neighborList.size(); j++) {
				final LatLng neighborCoord = new LatLng(neighborList.get(j).getLat(), neighborList.get(j).getLon());
				double candidateDistance = getRealDistance(tmpCoord, neighborCoord, 'K');
				
				if (nearestDistance > candidateDistance) {
					/* Found a closer neighbor */
					nearestDistance = candidateDistance;
					nearestCandidateIndex = j;
				}
			}
			
			if (nearestCandidateIndex < 0) {
				/* Coordinate has no nearest neighbors */
				Log.d("[ORPHAN]", "Coordinate has no nearest neighbors: " + 
						lastCoordList.get(i).route + "(" + tmpCoord.toString() + ")" );
				continue;
			}
			
			/* At this point, we probably have a nearestCandidate */
			MonitoredLocation nearest = neighborList.get(nearestCandidateIndex);
			
			/* Add this to the matchedLocationList */
			matchedLocationList.add(new MatchedLocation(matchCounter++, nearest.name, nearest.area, lastCoordList.get(i).route, "", ""));
		}
		
		return STATUS_OK;
	}
	
	private int downloadServerHistDataFile() {
		System.out.println("Started downloading historical traffic data file...");
		URL url;
		File file = null;
		try {
			url = new URL("http://travelbudapp.com:8090/TrafficData.hist");
	        String downloadFilename = URLUtil.guessFileName(("http://travelbudapp.com:8090/TrafficData.hist"), null, null);
	        file = new File(mContext.getFilesDir(), downloadFilename);
	        URLConnection connection = url.openConnection();
	        connection.setConnectTimeout(60000);
	        connection.setReadTimeout(60000);
	        InputStream in = new BufferedInputStream(connection.getInputStream());
	        
	        FileOutputStream fOutStream = new FileOutputStream(file, false);

	        byte data[] = new byte[1024];

	        int count;
	        
	        while ((count = in.read(data)) != -1) {
	            fOutStream.write(data, 0, count);
	        }
	        
	        System.out.println("[downloadServerHistDataFile] Total bytes downloaded: " + count);
	        
	        fOutStream.flush();
	        fOutStream.close();
	        in.close();
	        
	        mServerHistDataFile = file;
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return STATUS_FAILED;
		} catch (SocketTimeoutException e) {
			if ((file != null) && (file.exists())) {
		        mServerHistDataFile = file;
			}
			return STATUS_TIMEOUT;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return STATUS_FAILED;
		}

		System.out.println("Done downloading historical traffic data file.");
		return STATUS_OK;
	}
	
	private int loadServerHistDataFile(int day, String time, String weather) {
		int returnStatus = STATUS_OK;

		if (mServerHistDataFile == null) {
			/* A null value for mServerHistDataFile means that the calling thread did
			 * not run the downloadServerHistDataFile() method first. This could mean
			 * that either (1) there is a HistData file on this phone and it is still
			 * up-to-date, or (2) the HistData file does not exist at all and this
			 * method has been called unintentionally. */
			System.out.println("[loadServerHistDataFile] Loading Hist Data file...");
			mServerHistDataFile = new File(mContext.getFilesDir(), "TrafficData.hist");
		}
		
		/* Check for the case where the hist data file absolutely doesn't exist on
		 * this phone yet. This means that the downloadServerHistDataFile() should
		 * have been called first. */
		if (mServerHistDataFile.exists() == false) {
			System.out.println("[loadServerHistDataFile] Hist Data File is Unavailable!");
			System.out.println("[loadServerHistDataFile] Attempting to download Hist Data file...");
			int result = downloadServerHistDataFile();
			if (result == STATUS_TIMEOUT) {
		    	System.out.println("[TrafficDataManager] Hist Data File could not be updated at this time.");
			} else if (result != STATUS_OK) {
		    	System.out.println("[TrafficDataManager] Could not update Hist Data file!");
	    		return STATUS_FAILED;
			} else {
				wasHistDataUpdated = true;
			}
		}
		
		System.out.println("Processing historical traffic data file...");
		HistDataHelper hdh =  new HistDataHelper();
		
		/* Get the list of tags */
		List<String> histDataTags = hdh.getHistDataFileInfo(mServerHistDataFile, false).tagList;
		
		if (histDataTags == null) {
			System.out.println("[loadServerHistDataFile] Hist Data File has no valid tags!");
		}
		
		/* Now, get the most likely tag match */
		String closestMatchStr = hdh.getMostLikelyTagMatch(day, weather, histDataTags);
		System.out.println("[loadServerHistDataFile] Obtained Most Likely Tag Match: " + closestMatchStr);
		
		
		/* Load the hist data tag from a file */
		HistData matchedHistData = null;
		try {
			System.out.println("[loadServerHistDataFile] Loading Hist Data Tag from file...");
			matchedHistData = hdh.loadHistDataTagFromFile(mServerHistDataFile, closestMatchStr, false);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		if (matchedHistData == null) {
			return STATUS_FAILED;
		}
		
		/* Get the closest time match */
		
		int closestTimeIdx = -1;
		int leastTimeDiff = 4200;
		
		int matchTime = Integer.parseInt(time);
		
		for (int i = 0; i < matchedHistData.dataList.size(); i++) {
			int compTime = Integer.parseInt(matchedHistData.dataList.get(i).timestamp);

			if (Math.abs(matchTime - compTime) < leastTimeDiff) {
				leastTimeDiff = Math.abs(matchTime - compTime);
				closestTimeIdx = i;
				
			}
		}
		
		/* Decode the line data for the closest time index into the matched location list */
		if (closestTimeIdx >= 0) {
			System.out.println("[loadServerHistDataFile] Obtained closest time val: " + matchedHistData.dataList.get(closestTimeIdx).timestamp + "!");
			System.out.println("[loadServerHistDataFile] Decoding Line Data into Matched Condition list...");
			returnStatus = decodeLineDataToMatchedCond(matchedHistData.dataList.get(closestTimeIdx));
		} else {
			System.out.println("[loadServerHistDataFile] Error: Failed to obtain closest time match.");
		}

		System.out.println("Done processing historical traffic data file.");
		return returnStatus;
	}
	
	private int decodeLineDataToMatchedCond(LineInfo lineInfo) {
		int returnStatus = STATUS_OK;
		
		String dataStr = HistDataHelper.decodeLineDataString(lineInfo.lineDataStr);
		String traffCondStr[] = { "Unknown", "Light Traffic", "Medium Traffic", "Heavy Traffic" };
		
		String loc = "";
		int sb = 0;
		int nb = 0;
		
		int locIndex = -1;
		
		for (int i = 0; i < dataStr.length(); i++) {
			if ((i % 2) == 0) {
				/* Increment the location index counter */
				locIndex++;
				
				loc = HistDataHelper.getLocationName(locIndex);
				
				/* Means we're dealing with southbound traffic data */
				sb = HistDataHelper.base64chars.indexOf(dataStr.charAt(i));
			} else {
				/* Means we're dealing with northbound traffic data */
				nb = HistDataHelper.base64chars.indexOf(dataStr.charAt(i));
				
				/* Set the matched location condition */
				setMatchedLocationCond(loc, traffCondStr[sb], traffCondStr[nb]);
				
				/* Neutralize everything again */
				loc = "";
				sb = 0;
				nb = 0;
			}
		}
		
		return returnStatus;
	}
	
	private int downloadServerTrafficFile() {
		URL url;
		try {
			url = new URL(TRAFFIC_FILE_URL);
	        URLConnection connection = url.openConnection();
	        connection.setConnectTimeout(60000);
	        connection.setReadTimeout(60000);
	        String downloadFilename = URLUtil.guessFileName(("http://travelbudapp.com:8090/mmda_traffic.txt"), null, null);
	        InputStream in = new BufferedInputStream(connection.getInputStream());
	        
	        File file = new File(mContext.getFilesDir(), downloadFilename);
	        FileOutputStream fOutStream = new FileOutputStream(file, false);

	        byte data[] = new byte[1024];

	        int count;
	        
	        while ((count = in.read(data)) != -1) {
	            fOutStream.write(data, 0, count);
	        }
	        
	        fOutStream.flush();
	        fOutStream.close();
	        in.close();
	        
	        mServerTrafficFile = file;
	        
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return STATUS_FAILED;
		} catch (IOException e) {
			e.printStackTrace();
			return STATUS_FAILED;
		}
		
		return STATUS_OK;
	}
	
	private int loadServerTrafficFile() {
		int returnStatus = STATUS_OK;
		String line = "";
		FileInputStream trafficFileStream = null;

		System.out.println("Processing server traffic file...");
		if (mServerTrafficFile == null) {
			System.out.println("Server Traffic File is Unavailable or Invalid!");
			return STATUS_FAILED;
		}

		try {
			
			trafficFileStream = new FileInputStream(mServerTrafficFile);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(trafficFileStream));
			/* Read the file until the end */
			String lineName = "";
			String lineArea = "";
			String lineCondSB = "";
			String lineCondNB = "";
			int lineNum = 1;
			
			while ((line = rd.readLine()) != null) {
				/* Extract line area */
				lineArea = getCSVField(line, 0, lineNum).trim();
				
				/* Extract line name */
				lineName = getCSVField(line, 1, lineNum).trim();
				
				/* Extract line condition SB */
				lineCondSB = getCSVField(line, 2, lineNum).trim();
				
				if (lineCondSB.contains("1")) {
					lineCondSB = "Light Traffic";
				} else if (lineCondSB.contains("2")) {
					lineCondSB = "Medium Traffic";
				} else if (lineCondSB.contains("3")) {
					lineCondSB = "Heavy Traffic";
				} else {
					lineCondSB = "Condition Unknown";
				}
				
				/* Extract line condition NB */
				lineCondNB = getCSVField(line, 3, lineNum).trim();
				
				if (lineCondNB.contains("1")) {
					lineCondNB = "Light Traffic";
				} else if (lineCondNB.contains("2")) {
					lineCondNB = "Medium Traffic";
				} else if (lineCondNB.contains("3")) {
					lineCondNB = "Heavy Traffic";
				} else {
					lineCondNB = "Condition Unknown";
				}
				
				// DEBUG
				System.out.println(lineName + " / " + lineArea + " - " + lineCondSB + " : " + lineCondNB);
				
				/* 20130930: Fix so that invalid data are avoided */
				if ((lineName.equals("") == false) &&
					(lineArea.equals("") == false) &&
					(lineCondSB.equals("") == false) &&
					(lineCondNB.equals("") == false)) 
				{
					setMatchedLocationCond(lineName, lineCondSB, lineCondNB);
				}
				
				/* Reset strings before moving to the next line */
				lineName = "";
				lineArea = "";
				lineCondSB = "";
				lineCondNB = "";
				
				/* Update the line number */
				lineNum++;
			}
			
			/* Close and Nullify */
			trafficFileStream.close();
			trafficFileStream = null;

		/* Handle Exceptions */
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			returnStatus = STATUS_FAILED;
		} catch (IOException e) {
			e.printStackTrace();
			returnStatus = STATUS_FAILED;
		}
		System.out.println("Done processing server traffic file.");
		return returnStatus;
	}
	
	private String getCSVField(String line, int field, int lineNum) {
		int startIdx = 0;
		int endIdx = 0;
		int fieldIdx = 0;
		
		for (fieldIdx = 0; fieldIdx <= field; fieldIdx++) {
			/* Update indices */
			startIdx = (endIdx == 0) ? endIdx : endIdx +1;
			endIdx = line.indexOf(",", startIdx);
			
			/* Perform sanity checks on our start and end indices */
			if ((endIdx >= line.length()) || (startIdx > endIdx) || (startIdx < 0) || (endIdx < 0)) 
			{
				Log.e("Extraction", "Possibly incorrect indices (" + startIdx + ", " + endIdx + ") at line #" + lineNum);
				return "";
			}
		}
		
		/* The ff condition should be true: fieldIdx == field+1; otherwise, something went wrong */
		if (fieldIdx != field + 1) {
			Log.e("Extraction", "Something went wrong!");
			return "";
		}
		
		return line.substring(startIdx, endIdx);
	}
	
	private int downloadLineViewFiles() {
		/* See Part#4 in traffic_data_extraction.txt */
		
		String areaName [] = {"EDSA","COMMONWEALTH","QUEZON AVE","ESPANA","C5","ORTIGAS","MARCOS HIGHWAY","ROXAS BLVD","SLEX"};
		String lvNames[] = {"edsa", "commonwealth","quezon-ave","espana","c5","ortigas","marcos-highway","roxas-blvd","slex"};
		
		for (int i = 0; i < lvNames.length; i++) {
		    try {
				downloadLineFile(lvNames[i], areaName[i]);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	            return STATUS_FAILED;
	        }
		}
        
		return STATUS_OK;
	}
	
	private int downloadLineFile(String lineViewUrl, String areaName) throws IOException {
		URL url = new URL("http://mmdatraffic.interaksyon.com/line-view-" + lineViewUrl  +".php");	
        URLConnection connection = url.openConnection();
        String downloadFilename = URLUtil.guessFileName(("http://mmdatraffic.interaksyon.com/line-view-" + lineViewUrl  +".php"), null, null);
        InputStream in = new BufferedInputStream(connection.getInputStream());

        File file = new File(mContext.getFilesDir(), downloadFilename);
        FileOutputStream fOutStream = new FileOutputStream(file, false);

        byte data[] = new byte[1024];

        int count;
        
        while ((count = in.read(data)) != -1) {
            fOutStream.write(data, 0, count);
        }
        
        fOutStream.flush();
        fOutStream.close();
        in.close();
        
        /* Once successfully downloaded */
        lineViewFileList.add(file);
        
		return STATUS_OK;
		
	}
	
	private int extractTrafficDataFromFiles() {
		/* For Part #5: Parse each line view file and extract the necessary information */
		/* For each file in the lineViewFileList, */
		int returnStatus = STATUS_OK;
		String line = "";
		File tmpLineViewFile = null;
		FileInputStream lineViewFileStream = null;
		
		Iterator<File> fileListIter = lineViewFileList.iterator();
		
		while(fileListIter.hasNext()) {
			try {
				tmpLineViewFile = fileListIter.next();
				lineViewFileStream = new FileInputStream(tmpLineViewFile);
				
				BufferedReader rd = new BufferedReader(new InputStreamReader(lineViewFileStream));
				/* Read the file until the end */
				boolean isSeekingTrafficConditionSB = false;
				boolean isSeekingTrafficConditionNB = false;
				String lineName = "";
				String lineCondSB = "";
				String lineCondNB = "";
				int lineNum = 1;
				
				while ((line = rd.readLine()) != null) {
					/* Check if this line contains a line name */
					if ( line.contains("<div class=\"line-name\">") == true ) {
						/* Check if we are still seeking a traffic condition */
						if ((isSeekingTrafficConditionSB == true) || 
							(isSeekingTrafficConditionNB == true)) {
							/* Malformed line-view file */
							break;
						}
						
						/* Extract line name */
						int startIdx = line.indexOf("<p>") + 3;
						int endIdx = line.indexOf("<a");
						
						if ((endIdx >= line.length()) ||
							(startIdx > endIdx) ||
							(startIdx < 0) ||
							(endIdx < 0))
						{
							Log.e("Extraction", "Possibly incorrect indices (" + startIdx + ", " + endIdx + ") at line #" + lineNum);
						} else {
							lineName = line.substring(startIdx, endIdx);
							lineCondSB = "";
							lineCondNB = "";
							isSeekingTrafficConditionSB = true;
							isSeekingTrafficConditionNB = false;
							continue;
						}
					}
					
					if (isSeekingTrafficConditionSB == true) {
						if (line.contains("<div class=\"light\">") == true) {
							lineCondSB = "Light Traffic";
							isSeekingTrafficConditionSB = false;
							isSeekingTrafficConditionNB = true;
						} else if (line.contains("<div class=\"mod\">") == true) {
							lineCondSB = "Medium Traffic";
							isSeekingTrafficConditionSB = false;
							isSeekingTrafficConditionNB = true;
						} else if (line.contains("<div class=\"heavy\">") == true) {
							lineCondSB = "Heavy Traffic";
							isSeekingTrafficConditionSB = false;
							isSeekingTrafficConditionNB = true;
						}
					}
					
					if (isSeekingTrafficConditionNB == true) {
						if (line.contains("<div class=\"light\">") == true) {
							lineCondNB = "Light Traffic";
							isSeekingTrafficConditionNB = false;
						} else if (line.contains("<div class=\"mod\">") == true) {
							lineCondNB = "Medium Traffic";
							isSeekingTrafficConditionNB = false;
						} else if (line.contains("<div class=\"heavy\">") == true) {
							lineCondNB = "Heavy Traffic";
							isSeekingTrafficConditionNB = false;
						}
					}
					
					/* If the line name and condition strings are filled, attempt to match
					 * it against any of the matchedLocationList elements.
					 */ 
					if ((lineName.equals("") == false) 
						&& (lineCondSB.equals("") == false) 
						&& (lineCondNB.equals("") == false)) 
					{
						/* Set traffic conditions for all matching elements */
						setMatchedLocationCond(lineName, lineCondSB, lineCondNB);
						
						/* Set back the line name and condition values */
						lineName = "";
						lineCondSB = "";
						lineCondNB = "";
					}
					lineNum++;
				}
				
				/* Close and Nullify before the next round */
				lineViewFileStream.close();
				tmpLineViewFile = null;
				lineViewFileStream = null;

			/* Handle Exceptions */
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				returnStatus = STATUS_FAILED;
				break;
			} catch (IOException e) {
				e.printStackTrace();
				returnStatus = STATUS_FAILED;
				break;
			}
		}
		return returnStatus;
	}
	
	private void setMatchedLocationCond(String loc, String condSB, String condNB) {
		Iterator<MatchedLocation> matchIter = matchedLocationList.iterator();
		
		while(matchIter.hasNext()) {
			MatchedLocation tempMatchedObj = matchIter.next();
			
			if (tempMatchedObj.name.contains(loc) == true) {
				matchedLocationList.get(tempMatchedObj.id).conditionSB = condSB;
				matchedLocationList.get(tempMatchedObj.id).conditionNB = condNB;
			}
		}
	}
	
	/************************************************/
	/** THE FOLLOWING ARE MOSTLY UTILITY FUNCTIONS **/
	/************************************************/
	private ArrayList<RouteCoordObject> decodeRoutePoly(String routeName, String encoded) {
		ArrayList<RouteCoordObject> poly = new ArrayList<RouteCoordObject>();
		
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(new RouteCoordObject(routeName, p));
        }
        return poly;
    }
	
	public static List<LatLng> decodePolyToLatLng(String encoded) {
		List<LatLng> poly = new ArrayList<LatLng>();
		

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
		
		return poly;
	}
	

	public static List<LatLng> decodePolyToEndpoints(String encoded) {
		List<LatLng> poly = new ArrayList<LatLng>();

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        boolean firstElement = true;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            if (firstElement) {
	            LatLng p = new LatLng((((double) lat / 1E5)),
	                    (((double) lng / 1E5)));
	            poly.add(p);
            }
        }
        if (!firstElement) {
	        LatLng p = new LatLng((((double) lat / 1E5)),
	                (((double) lng / 1E5)));
	        poly.add(p);
        }
		
		return poly;
	}
	
	private ArrayList<MonitoredLocation> initMonitoredLocList(){
		ArrayList<MonitoredLocation> list = new ArrayList<MonitoredLocation>();
		
		/* A VERY LONG array list */
		list.add(new MonitoredLocation("Edsa", "Balintawak", 14.657288775015218, 121.00016059408192));
		list.add(new MonitoredLocation("Edsa", "Kaingin Road", 14.657421115943517, 121.01098330745701));
		list.add(new MonitoredLocation("Edsa", "Muñoz", 14.657587190720669, 121.01971926221852));
		list.add(new MonitoredLocation("Edsa", "Bansalangin", 14.65618333590464, 121.02733271250729));
		list.add(new MonitoredLocation("Edsa", "North Ave.", 14.655367227629975, 121.03024022707943));
		list.add(new MonitoredLocation("Edsa", "Trinoma", 14.651276260692319, 121.03287281522755));
		list.add(new MonitoredLocation("Edsa", "Quezon Ave.", 14.644119155496664, 121.03748755583767));
		list.add(new MonitoredLocation("Edsa", "NIA Road", 14.637138287596326, 121.04205938110356));
		list.add(new MonitoredLocation("Edsa", "Timog", 14.63315208643025, 121.04467453489308));
		list.add(new MonitoredLocation("Edsa", "Kamuning", 14.63079562616363, 121.04586141238217));
		list.add(new MonitoredLocation("Edsa", "New York - Nepa Q-Mart", 14.625658299372533, 121.0481909109116));
		list.add(new MonitoredLocation("Edsa", "Monte De Piedad", 14.623561290815394, 121.0491632116795));
		list.add(new MonitoredLocation("Edsa", "Aurora Blvd.", 14.621552503937846, 121.05007650384907));
		list.add(new MonitoredLocation("Edsa", "Mc Arthur - Farmers", 14.618038380381716, 121.0517019225121));
		list.add(new MonitoredLocation("Edsa", "P. Tuazon", 14.616431831021794, 121.05247573981289));
		list.add(new MonitoredLocation("Edsa", "Main Ave.", 14.613620990234462, 121.05369212160115));
		list.add(new MonitoredLocation("Edsa", "Santolan", 14.610882786658824, 121.05500238070492));
		list.add(new MonitoredLocation("Edsa", "White Plains - Connecticut", 14.599694729435262, 121.05968015322689));
		list.add(new MonitoredLocation("Edsa", "Ortigas Ave.", 14.593223798826235, 121.05839805731777));
		list.add(new MonitoredLocation("Edsa", "SM Megamall", 14.584483976807324, 121.05558307895664));
		list.add(new MonitoredLocation("Edsa", "Shaw Blvd.", 14.581265184524534, 121.05361567864422));
		list.add(new MonitoredLocation("Edsa", "Reliance", 14.576786052961792, 121.05032326707844));
		list.add(new MonitoredLocation("Edsa", "Pioneer - Boni", 14.572497630723673, 121.04724945554737));
		list.add(new MonitoredLocation("Edsa", "Guadalupe", 14.566900750767404, 121.04547651538853));
		list.add(new MonitoredLocation("Edsa", "Orense", 14.561660685910049, 121.04284795055393));
		list.add(new MonitoredLocation("Edsa", "Kalayaan - Estrella", 14.559757772984634, 121.0405358863831));
		list.add(new MonitoredLocation("Edsa", "Buendia", 14.554999120794458, 121.03494884500508));
		list.add(new MonitoredLocation("Edsa", "Ayala Ave.", 14.550328636239861, 121.0294234944344));
		list.add(new MonitoredLocation("Edsa", "Arnaiz - Pasay Road", 14.547507864934849, 121.02603184113507));
		list.add(new MonitoredLocation("Edsa", "Magallanes", 14.54041099284951, 121.01682918200497));
		list.add(new MonitoredLocation("Edsa", "Malibay", 14.53869613003046, 121.0101343883038));
		list.add(new MonitoredLocation("Edsa", "Tramo", 14.537851026196295, 121.00320356020931));
		list.add(new MonitoredLocation("Edsa", "Taft Ave.", 14.53749273235132, 121.00060986409191));
		list.add(new MonitoredLocation("Edsa", "F.B. Harrison", 14.537053951416523, 120.99247472414974));
		list.add(new MonitoredLocation("Edsa", "Roxas Boulevard", 14.537060997572251, 120.99248027955932));
		list.add(new MonitoredLocation("Edsa", "Macapagal Ave.", 14.53626596098194, 120.98896371254925));
		list.add(new MonitoredLocation("Edsa", "Mall of Asia", 14.53519, 120.98423));
		list.add(new MonitoredLocation("Commonwealth", "Philcoa", 14.653688847632143, 121.05301237260744));
		list.add(new MonitoredLocation("Commonwealth", "Batasan", 14.68707829291204, 121.08700132524416));
		list.add(new MonitoredLocation("Commonwealth", "St. Peter&#39;s Church", 14.681349412324787, 121.08468389665529));
		list.add(new MonitoredLocation("Commonwealth", "Ever Gotesco", 14.677765026087656, 121.08305126464847));
		list.add(new MonitoredLocation("Commonwealth", "Diliman Preparatory School", 14.674779696055621, 121.08127212678835));
		list.add(new MonitoredLocation("Commonwealth", "Zuzuaregi", 14.671001760390658, 121.07822513734743));
		list.add(new MonitoredLocation("Commonwealth", "General Malvar Hospital", 14.666912382899428, 121.072925092334));
		list.add(new MonitoredLocation("Commonwealth", "Tandang Sora Eastside", 14.66425528449887, 121.06891250764772));
		list.add(new MonitoredLocation("Commonwealth", "Tandang Sora Westside", 14.663757074958804, 121.06814003145143));
		list.add(new MonitoredLocation("Commonwealth", "Central Ave", 14.66161891281118, 121.06492138063356));
		list.add(new MonitoredLocation("Commonwealth", "Magsaysay Ave", 14.65827669520902, 121.05977153932497));
		list.add(new MonitoredLocation("Commonwealth", "University Ave", 14.65553645391221, 121.05560875093386));
		list.add(new MonitoredLocation("Quezon Ave.", "Elliptical Road", 14.650039534952587, 121.04713165590286));
		list.add(new MonitoredLocation("Quezon Ave.", "Agham Road", 14.646167750622343, 121.0407989604187));
		list.add(new MonitoredLocation("Quezon Ave.", "Bantayog Road", 14.645232232153946, 121.03929692337036));
		list.add(new MonitoredLocation("Quezon Ave.", "Edsa", 14.644126732230413, 121.03751325437545));
		list.add(new MonitoredLocation("Quezon Ave.", "SGT. Esguera", 14.642857202001968, 121.03547609006804));
		list.add(new MonitoredLocation("Quezon Ave.", "Scout Albano", 14.641253963411117, 121.03291594812393));
		list.add(new MonitoredLocation("Quezon Ave.", "Scout Borromeo", 14.639685213332823, 121.03037723729133));
		list.add(new MonitoredLocation("Quezon Ave.", "Scout Santiago", 14.63822544821936, 121.027796952219));
		list.add(new MonitoredLocation("Quezon Ave.", "Timog", 14.637443010120712, 121.02654167840004));
		list.add(new MonitoredLocation("Quezon Ave.", "Scout Reyes", 14.636587904594695, 121.02517777511596));
		list.add(new MonitoredLocation("Quezon Ave.", "Scout Magbanua", 14.634903635404616, 121.02244326302528));
		list.add(new MonitoredLocation("Quezon Ave.", "Roces Avenue", 14.633430862429774, 121.02001184055328));
		list.add(new MonitoredLocation("Quezon Ave.", "Roosevelt Avenue", 14.632647109656546, 121.01872035691261));
		list.add(new MonitoredLocation("Quezon Ave.", "Dr. Garcia Sr.", 14.631827020943778, 121.01743289658546));
		list.add(new MonitoredLocation("Quezon Ave.", "Scout Chuatoco", 14.630573524995924, 121.01573908159256));
		list.add(new MonitoredLocation("Quezon Ave.", "G. Araneta Ave.", 14.628329931041687, 121.01327681371689));
		list.add(new MonitoredLocation("Quezon Ave.", "Sto. Domingo", 14.626487286121066, 121.0112986845684));
		list.add(new MonitoredLocation("Quezon Ave.", "Biak na Bato", 14.625152007708186, 121.00981274077415));
		list.add(new MonitoredLocation("Quezon Ave.", "Banawe", 14.623518259517574, 121.00801834294319));
		list.add(new MonitoredLocation("Quezon Ave.", "Cordillera", 14.621749541459335,  121.00609117576599));
		list.add(new MonitoredLocation("Quezon Ave.", "D. Tuazon", 14.620128744759247, 121.00424983927726));
		list.add(new MonitoredLocation("Quezon Ave.", "Speaker Perez", 14.619496773549573, 121.00355917045593));
		list.add(new MonitoredLocation("Quezon Ave.", "Apo Avenue", 14.618912814963403, 121.00292214581489));
		list.add(new MonitoredLocation("Quezon Ave.", "Kanlaon", 14.618243207207367, 121.00219258496284));
		list.add(new MonitoredLocation("Quezon Ave.", "Mayon", 14.61774100005078, 121.00164273211479));
		list.add(new MonitoredLocation("ESPAÑA", "Lerma", 14.605211041356437, 120.98788988602638));
		list.add(new MonitoredLocation("ESPAÑA", "P.Noval", 14.606667132656822, 120.98954749119758));
		list.add(new MonitoredLocation("ESPAÑA", "Gov. Forbes - Lacson", 14.609651959899876, 120.9928291739273));
		list.add(new MonitoredLocation("ESPAÑA", "Vicente Cruz", 14.61171536025725, 120.99506613624573));
		list.add(new MonitoredLocation("ESPAÑA", "Antipolo", 14.612966368399238, 120.99643674505234));
		list.add(new MonitoredLocation("ESPAÑA", "A. Maceda", 14.614418515184347, 120.99802729499817));
		list.add(new MonitoredLocation("ESPAÑA", "Bluementritt", 14.615480042687352, 120.99909883749962));
		list.add(new MonitoredLocation("ESPAÑA", "Welcome Rotunda", 14.617723767929137, 121.00161474955559));
		list.add(new MonitoredLocation("C5", "Libis Flyover", 14.61500871277126, 121.0748094157219));
		list.add(new MonitoredLocation("C5", "Eastwood", 14.607259946574683, 121.07856987276078));
		list.add(new MonitoredLocation("C5", "Green Meadows", 14.601405690037016, 121.07932223238946));
		list.add(new MonitoredLocation("C5", "Ortigas Ave.", 14.58981221902904, 121.07994852819444));
		list.add(new MonitoredLocation("C5", "J. Vargas", 14.582615472035616, 121.07688678660394));
		list.add(new MonitoredLocation("C5", "Lanuza", 14.57758993954966, 121.07340796151162));
		list.add(new MonitoredLocation("C5", "Bagong Ilog", 14.564850376080365, 121.06990365543366));
		list.add(new MonitoredLocation("C5", "Kalayaan", 14.556341780863319, 121.06313644208909));
		list.add(new MonitoredLocation("C5", "Market! Market!", 14.54946067691889, 121.05710147180558));
		list.add(new MonitoredLocation("C5", "Tandang Sora", 14.663870000000001, 121.06897000000001));
		list.add(new MonitoredLocation("C5", "Capitol Hills", 14.663105410018082, 121.07417664718628));
		list.add(new MonitoredLocation("C5", "University of the Philippines", 14.655092091801786, 121.07423857078561));
		list.add(new MonitoredLocation("C5", "C.P. Garcia", 14.648199804281386, 121.07432440147409));
		list.add(new MonitoredLocation("C5", "Miriam College", 14.642449145540192, 121.07462480888375));
		list.add(new MonitoredLocation("C5", "Ateneo De Manila University", 14.63869141341423, 121.07451752052316));
		list.add(new MonitoredLocation("C5", "Xavierville", 14.633687924311955, 121.07428148612985));
		list.add(new MonitoredLocation("C5", "Aurora Boulevard", 14.631715563229765, 121.07415274009713));
		list.add(new MonitoredLocation("C5", "P. Tuazon", 14.623472978281454, 121.07415274009713));
		list.add(new MonitoredLocation("C5", "Bonny Serrano", 14.614524128977378, 121.07065513954171));
		list.add(new MonitoredLocation("Ortigas", "Santolan", 14.607529969235154, 121.03917753850862));
		list.add(new MonitoredLocation("Ortigas", "Madison", 14.604877342643361, 121.04272341882631));
		list.add(new MonitoredLocation("Ortigas", "Roosevelt", 14.603454981740796, 121.04457950746462));
		list.add(new MonitoredLocation("Ortigas", "Club Filipino", 14.601749693880944, 121.04679501211092));
		list.add(new MonitoredLocation("Ortigas", "Wilson", 14.601035908864343, 121.04783839141771));
		list.add(new MonitoredLocation("Ortigas", "Connecticut", 14.599675817574107, 121.0497803107445));
		list.add(new MonitoredLocation("Ortigas", "La Salle Greenhills", 14.596810254207044, 121.05357831870958));
		list.add(new MonitoredLocation("Ortigas", "POEA", 14.594100393643716, 121.05721539413378));
		list.add(new MonitoredLocation("Ortigas", "EDSA Shrine", 14.593228247513233, 121.05840629493639));
		list.add(new MonitoredLocation("Ortigas", "San Miguel Ave", 14.590653319724465, 121.06136745368883));
		list.add(new MonitoredLocation("Ortigas", "Meralco Ave", 14.588690955336753, 121.06389945899889));
		list.add(new MonitoredLocation("Ortigas", "Medical City", 14.588992060003331, 121.06929606353685));
		list.add(new MonitoredLocation("Ortigas", "Lanuza Ave", 14.589147803634779, 121.07125944053575));
		list.add(new MonitoredLocation("Ortigas", "Greenmeadows Ave", 14.589241249760775, 121.07252544319078));
		list.add(new MonitoredLocation("Ortigas", "C5 Flyover", 14.589807132502871, 121.0799296811764));
		list.add(new MonitoredLocation("Marcos Highway", "San Benildo School", 14.621703601615591, 121.10644899901126));
		list.add(new MonitoredLocation("Marcos Highway", "Robinson&#39;s Metro East", 14.620317684627894, 121.10001169737552));
		list.add(new MonitoredLocation("Marcos Highway", "F. Mariano Ave", 14.619840138173368, 121.09755479391788));
		list.add(new MonitoredLocation("Marcos Highway", "Amang Rodriguez", 14.618770845607932, 121.092533698642));
		list.add(new MonitoredLocation("Marcos Highway", "Dona Juana", 14.619269157354042, 121.08933650549625));
		list.add(new MonitoredLocation("Marcos Highway", "LRT-2 Station", 14.622248622748131, 121.08632170256351));
		list.add(new MonitoredLocation("Marcos Highway", "SM City Marikina", 14.625643295377506, 121.08304940756534));
		list.add(new MonitoredLocation("Roxas Blvd.", "Coastal Road", 14.504511358849365, 120.99062083934783));
		list.add(new MonitoredLocation("Roxas Blvd.", "Airport Road", 14.527370415707448, 120.99358468030928));
		list.add(new MonitoredLocation("Roxas Blvd.", "Baclaran", 14.531457195304439, 120.99315955018042));
		list.add(new MonitoredLocation("Roxas Blvd.", "Edsa Extension", 14.537030308852493, 120.99246888135909));
		list.add(new MonitoredLocation("Roxas Blvd.", "Buendia", 14.552226162674515, 120.98946614836692));
		list.add(new MonitoredLocation("Roxas Blvd.", "Pablo Ocampo", 14.55957317628984, 120.98708032344817));
		list.add(new MonitoredLocation("Roxas Blvd.", "Quirino", 14.564279809433428, 120.98500965808867));
		list.add(new MonitoredLocation("Roxas Blvd.", "Rajah Sulayman", 14.568655356487671, 120.98307712649344));
		list.add(new MonitoredLocation("Roxas Blvd.", "U.N. Avenue", 14.578809250429936, 120.9778280434513));
		list.add(new MonitoredLocation("Roxas Blvd.", "Finance Road", 14.582865225805314, 120.97541673754691));
		list.add(new MonitoredLocation("Roxas Blvd.", "Anda Circle", 14.590370854563018, 120.97107290004729));
		list.add(new MonitoredLocation("Roxas Blvd.", "Pedro Gil", 14.573030000000001, 120.98103));
		list.add(new MonitoredLocation("SLEX", "Alabang Exit", 14.422879951647001, 121.04542958509448));
		list.add(new MonitoredLocation("SLEX", "Sucat Exit", 14.45376027982941, 121.04528072249416));
		list.add(new MonitoredLocation("SLEX", "Bicutan Exit", 14.486912537445594, 121.04503932368281));
		list.add(new MonitoredLocation("SLEX", "Merville Exit", 14.516272754951705, 121.02983253967295));
		list.add(new MonitoredLocation("SLEX", "Nichols", 14.524289561227562, 121.02557989727977));
		list.add(new MonitoredLocation("SLEX", "Magallanes", 14.540436212995646, 121.01681327974242));
		list.add(new MonitoredLocation("SLEX", "C5 On-ramp", 14.511439162004292, 121.03249194991115));
		
		/* Set up the QuadTree as well */
		setupQuadTree(list);
		
		return list;
	}
	
	public void saveCoordList() throws IOException {
        System.out.println("Saving coordlist...");
        String root_dir_str = Environment.getExternalStorageDirectory().toString();
        File root_dir = new File(root_dir_str + "/Viaje");
        File file = new File(root_dir, "lastCoordList.txt");
//        FileOutputStream fOutStream = new FileOutputStream(file, false);
        System.out.println("lastCoordList size is " + lastCoordList.size());
        FileWriter fw = new FileWriter(file);
        for (int i = 0; i < lastCoordList.size(); i++) {
            fw.write(lastCoordList.get(i).getWriteableString() + "\n");
        }
        
        fw.flush();
        fw.close();
        
        System.out.println("Finished saving coordlist to " + file.getAbsolutePath() + "!");
        
		return;
	}
	
	public boolean getHistDataUpdateStatus() {
		return wasHistDataUpdated;
	}
	
	public void resetHistDataUpdateStatus() {
		wasHistDataUpdated = false;
	}
	
	private void setupQuadTree(ArrayList<MonitoredLocation> list) {
		monitoredLocQuadTree = new QuadTree(0, new Rectangle(120.87,14.34,0.31,0.44));
		
		monitoredLocQuadTree.clear();
		for (int i = 0; i < list.size(); i++) {
			monitoredLocQuadTree.insert(list.get(i));
		}
	}
	
	//Get Real distance between 2 points in Kilometers K (unit)
	/* TODO: Note: Check how precise these conversions will be */
	public double getRealDistance(LatLng start, LatLng end, char unit) {
	    double lat1 = start.latitude;
	    double lon1 = start.longitude;
	    double lat2 = end.latitude;
	    double lon2 = end.longitude;
	    
		double theta = lon1 - lon2;
	    double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
	    
	    dist = Math.acos(dist);
	    dist = rad2deg(dist);
	    dist = dist * 60 * 1.1515;
	    
	    if (unit == 'K') {
	    	dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
	    }
	    
	    return (dist);
	}
	
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts decimal degrees to radians             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
		
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts radians to decimal degrees             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}
	
	class HistDataUpdateRunnable implements Runnable {
		private boolean isHistDataUpdated = false;

		public HistDataUpdateRunnable(boolean isUpdated) {
			this.isHistDataUpdated = isUpdated;
		}
		@Override
		public void run() {
	    	System.out.println("[TrafficDataManager] Thread Started.");
    		if (!this.isHistDataUpdated) {
    			mServerHistDataFile = null;
		    	System.out.println("[TrafficDataManager] Updating Hist Data file...");
		    	
		    	int result = downloadServerHistDataFile();
    			if (result == STATUS_TIMEOUT) {
    		    	System.out.println("[TrafficDataManager] Hist Data File could not be updated at this time.");
    			} else if (result != STATUS_OK) {
    		    	System.out.println("[TrafficDataManager] Could not update Hist Data file!");
    		    	isUpdateThreadRunning = false;
			    	/* Trigger the callback upon finishing */
			    	if (updateEvent != null) {
			    		updateEvent.onTrafficDataUpdate(STATUS_FAILED);
			    	}
    	    		return;
    			} else {
    				wasHistDataUpdated = true;
    			}
    		}
	    	
	    	System.out.println("[TrafficDataManager] Thread Finished.");
	    	isUpdateThreadRunning = false;
	    	
	    	/* Trigger the callback upon finishing */
	    	if (updateEvent != null) {
	    		updateEvent.onTrafficDataUpdate(STATUS_OK);
	    	}
		}
	}
	
	class TrafficDataUpdateRunnable implements Runnable {
		private int day = -1;
		private String time = "";
		private String weather = "";
		private boolean useHistData = false;
		private boolean isHistDataUpdated = false;
		
		public TrafficDataUpdateRunnable() {
			this.useHistData = false;
		}
		
		public TrafficDataUpdateRunnable(int d, String t, String w, boolean isUpdated) {
			this.day = d;
			this.time = t;
			this.weather = w;
			
			this.useHistData = true;
			this.isHistDataUpdated = isUpdated;
		}
		
	    public void run() {
	    	isUpdateThreadRunning = true;
	    	System.out.println("[TrafficDataManager] Thread Started.");
	    	
	    	if (generateMatchedLocationList() != STATUS_OK) {
		    	isUpdateThreadRunning = false;
		    	/* Trigger the callback upon finishing */
		    	if (updateEvent != null) {
		    		updateEvent.onTrafficDataUpdate(STATUS_FAILED);
		    	}
	    		return;
	    	}
	    	
	    	
	    	if (this.useHistData) {
	    		/* Case 1: Use Historical Traffic Data instead of the Current one */
	    		/* If the HistData file we have is not up to date, then download
	    		 * the most recent one from the server */
	    		if (!this.isHistDataUpdated) {
	    			mServerHistDataFile = null;
    		    	System.out.println("[TrafficDataManager] Updating Hist Data file...");
    		    	
    		    	int result = downloadServerHistDataFile();
	    			if (result == STATUS_TIMEOUT) {
	    		    	System.out.println("[TrafficDataManager] Hist Data File could not be updated at this time.");
	    			} else if (result != STATUS_OK) {
	    		    	System.out.println("[TrafficDataManager] Could not update Hist Data file!");
	    		    	isUpdateThreadRunning = false;
				    	/* Trigger the callback upon finishing */
				    	if (updateEvent != null) {
				    		updateEvent.onTrafficDataUpdate(STATUS_FAILED);
				    	}
	    	    		return;
	    			} else {
	    				wasHistDataUpdated = true;
	    			}
	    		}
	    		
	    		/* Load the HistData file */
	    		if (loadServerHistDataFile(day, time, weather) != STATUS_OK) {
			    	isUpdateThreadRunning = false;
			    	/* Trigger the callback upon finishing */
			    	if (updateEvent != null) {
			    		updateEvent.onTrafficDataUpdate(STATUS_FAILED);
			    	}
		    		return;
	    		}
	    		
	    		
	    		
	    	} else {
	    		/* Case 2: Use Current Traffic Data instead of the Historical Data */
		    	if (downloadServerTrafficFile() != STATUS_OK) {
		    		System.out.println("Failed to download traffic file from server!");
		    		System.out.println("Attempting to download from alternate sources...");
		    		/* Upon failing to draw traffic info from the server, 
		    		 * default to using the MMDA site directly instead.
		    		 */
			    	if (downloadLineViewFiles() != STATUS_OK) {
				    	isUpdateThreadRunning = false;
				    	/* Trigger the callback upon finishing */
				    	if (updateEvent != null) {
				    		updateEvent.onTrafficDataUpdate(STATUS_FAILED);
				    	}
			    		return;
			    	}
			    	
			    	if (extractTrafficDataFromFiles() != STATUS_OK) {
				    	isUpdateThreadRunning = false;
				    	/* Trigger the callback upon finishing */
				    	if (updateEvent != null) {
				    		updateEvent.onTrafficDataUpdate(STATUS_FAILED);
				    	}
			    		return;
			    	}
		    	} else {
		    		if (loadServerTrafficFile() != STATUS_OK) {
				    	isUpdateThreadRunning = false;
				    	/* Trigger the callback upon finishing */
				    	if (updateEvent != null) {
				    		updateEvent.onTrafficDataUpdate(STATUS_FAILED);
				    	}
			    		return;
		    		}
		    	}
	    	}
	    	
//	    	/*** FOR TESTING ***/
//	    	/* Print the contents of the MatchedLocationList */
//	    	for (int i = 0; i < matchedLocationList.size(); i++) {
//	    		MatchedLocation tmp = matchedLocationList.get(i);
//	    		String msg = tmp.name + " (" + tmp.area + ") for " + tmp.route + " : " + tmp.condition;
//	    		Log.d("[TEST]", msg);
//	    	}
	    	
	    	System.out.println("[TrafficDataManager] Thread Finished.");
	    	isUpdateThreadRunning = false;
	    	
	    	/* Trigger the callback upon finishing */
	    	if (updateEvent != null) {
	    		updateEvent.onTrafficDataUpdate(STATUS_OK);
	    	}
	    }
	}
	
	class RouteCoordObject {
		public String route = "";
		public LatLng coord = null;
		
		public RouteCoordObject(String route, LatLng coord) {
			this.route = route;
			this.coord = coord;
		}
		
		public String getWriteableString() {
			return ("" + (route.equals("") == true ? "Walk": route) + ", " + coord.latitude + ", " + coord.longitude +",");
		}
	}
	
	class MatchedLocation {
		public int id = -1;
		public String name = "";
		public String area = "";
		public String route = "";
		public String conditionSB = "";
		public String conditionNB = "";
		private boolean updated;
		
		public MatchedLocation(int id, String loc, String area, String route, String condSB, String condNB) {
			this.id = id;
			this.name = loc;
			this.area = area;
			this.route = route;
			this.conditionSB = condSB;
			this.conditionNB = condNB;
			this.updated = false;
		}
		
		public boolean isUpdated() {
			return updated;
		}
		
		public void setAsUpdated() {
			updated = true;
		}
		
		@Override
		public String toString() {
			return ("" + this.name + ", " + this.area + " - " + this.conditionSB + " (SB) + " + this.conditionNB + " (NB)"); 
		}
	}
}
