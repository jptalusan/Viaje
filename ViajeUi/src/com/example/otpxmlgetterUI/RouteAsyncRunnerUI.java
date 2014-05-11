package com.example.otpxmlgetterUI;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.atlach.trafficdataloader.TripDataParser;
import com.atlach.trafficdataloader.TripInfo;
import com.google.android.gms.maps.model.LatLng;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

//http://stackoverflow.com/questions/7990089/how-can-you-generate-a-loading-screen-in-android
public class RouteAsyncRunnerUI extends AsyncTask<Void, Void, Void>  {
	String[] mInputs = {"","","","","","","","","",""};
	private static int NUMBER_OF_INPUTS = 10;
	/* Input list 
	 * 0. fileName
	 * 1. offlineFrom
	 * 2. offlineTo
	 * 3. serverName
	 * 4. onlineFrom
	 * 5. onlineTo
	 * 6. sLat
	 * 7. sLon
	 * 8. eLat
	 * 9. eLon
	 */
	public boolean isOnline;
	Context mContext;

	private TripDataParser parser = null;
	private TripInfo tripInfo = null;
	
    private String startName = "";
    private String endName = "";
    public List<List<LatLng>> mTheList = null;
	ProgressDialog mDialog;
	public String urlForDownload;
	private SharedPreferences settings; 
	
    public RouteAsyncRunnerUI(String[] inputs, Context context, TripInfo t) {
    	for(int i = 0; i < NUMBER_OF_INPUTS; i++) {
    		if(inputs[i] == null) {
    			inputs[i] = "";
    			this.mInputs[i] = inputs[i];
    		} else {
        		this.mInputs[i] = inputs[i];
    		}
    	}
    	//this.displayFindRouteResult = displayFindRouteResult;
        this.mContext = context;
        this.tripInfo = t;
    }
    
	@Override
	protected void onPreExecute() {
        super.onPreExecute();

        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage("Preparing Route");
        mDialog.show();
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		parser = new TripDataParser(mContext);
		
		if(this.mInputs[4].equals("")) { //If offline viewer is needed
			this.isOnline = false;
			
			String root_sd = Environment.getExternalStorageDirectory().toString();
			String specific_sd = root_sd + "/Viaje";
			
			try {
				tripInfo = parser.getTripInfoFromFile(specific_sd, this.mInputs[0]);
			} catch (FileNotFoundException e) {
				Log.e("ERROR", "Trip File Not Found!");
				return null;
			}
		} else { //If online viewer is needed
			this.isOnline = true;
			this.setStartName(this.mInputs[4]); 
			this.setEndName(this.mInputs[5]); 
			
			double sCoord[] = {Double.parseDouble(mInputs[6]), Double.parseDouble(mInputs[7])};
			double eCoord[] = {Double.parseDouble(mInputs[8]), Double.parseDouble(mInputs[9])};
			
			//Change this: this is for debugging only
			settings = mContext.getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
			if(settings.getBoolean("debugmode", false)) {
				urlForDownload = "http://maps5.trimet.org/osm?mode=BICYCLE&toPlace=45.504966%2C-122.654349&fromPlace=45.5115225%2C-122.647633";
			} else {
				urlForDownload = "http://" + this.mInputs[3] + ":8080/opentripplanner-api-webapp/ws/plan?time=1:41pm&date=7/26/2013&mode=TRANSIT,WALK&min=QUICK&fromPlace=" 
			    		+ sCoord[0] + "%2C" + sCoord[1] + "&toPlace=" + eCoord[0] + "%2C" + eCoord[1];				
			}

		    tripInfo = parser.getTripInfoFromUrl(urlForDownload);
		}
		
		/* Catch the case where the OTP server says the trip isnt possible --Francis */
		if (tripInfo == null) {
			Log.e("RouteAsyncRunner", "Failed to retrieve trips from OTP Server!");
			return null;
		}
		
		if(mTheList == null) {
			mTheList = new ArrayList<List<LatLng>>();
		}
		
		for(int i = 0; i < tripInfo.trips.size(); i++) {
			mTheList.add(parser.getLatLngCoordList(tripInfo, i));
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void param) {
		System.out.println("onPostExecute");
		mDialog.dismiss();
	}
	
	public boolean getIsOnline() {
		return isOnline;
	}
	
	public String getStartName() {
		return startName;
	}

	public void setStartName(String startName) {
		this.startName = startName;
	}

	public String getEndName() {
		return endName;
	}

	public void setEndName(String endName) {
		this.endName = endName;
	}
	
	public List<List<LatLng>> getList() {
		return this.mTheList;
	}

	public void setList(List<List<LatLng>> list) {
		this.mTheList = list;
	}

	public TripInfo getTripInfo() {
		return tripInfo;
	}
}
