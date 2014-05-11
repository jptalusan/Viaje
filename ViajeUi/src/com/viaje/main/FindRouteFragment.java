package com.viaje.main;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.atlach.trafficdataloader.TrafficAlertInfo;
import com.example.otpxmlgetterUI.ClearableEditText;
import com.example.otpxmlgetterUI.GPSTrackerUI;
import com.example.otpxmlgetterUI.ReverseGeocodeUI;
import com.example.otpxmlgetterUI.UtilsUI;
import com.example.otpxmlgetterUI.addListenerOnKey;
import com.example.otpxmlgetterUI.addListenerOnTextChange;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.viaje.webinterface.JSONWebInterface;

public class FindRouteFragment extends Fragment implements 
OnClickListener, 
OnMyLocationButtonClickListener,
OnMapClickListener, 
OnMapLongClickListener
{			
	//private static int ZOOM_PADDING = 5;
	public View rootView;
	/* Declaration of clearable edit text */
	private static ClearableEditText autocompleterStart = null;
	private static ClearableEditText autocompleterDest = null;
	private static ArrayAdapter<String> adapter = null;
		
	private static ImageButton getStartLocBtn;
	private static ImageButton getEndLocBtn;
	private static ImageButton getCriteriaDistanceBtn;
	private static ImageButton getCriteriaCostBtn;
	private static ImageButton getCriteriaTransferBtn;
	private static ImageButton switchLocationsBtn;
    
    /* Input to next activity */
	static String Criteria = "CRITERIA";
	static String Destination = "DESTINATION";
	public static String CURRENT_LOCATION = "My Current Location";
	String st_criteria;
	String st_destination;
	
	/* Google map */
	private GoogleMap map;
    private UiSettings mUiSettings;
    
    UtilsUI util = new UtilsUI();
   
	private LinearLayout criteria_layout;
	private ImageView image_divider;
	private Marker markerStart = null;
	private Marker markerTemp = null;
	private Marker markerEnd = null;
	private double startLoc[] = {0.0, 0.0};
	private double endLoc[] = {0.0, 0.0};
	private String outputName = "";
	
	private double mLat = 0.0;
	private double mLong = 0.0;
	private LatLng coords = null;
	
	private SharedPreferences settings; 
	private SharedPreferences.Editor prefEditor;
	private MarkerOptions markerAlerts;
	private Marker incidentMarkers;
	public static FindRouteFragment newInstance(Context context) 
	{
		System.out.println("viaje fragment for Find Route is called");	
		FindRouteFragment newActivityForFindRoute = new FindRouteFragment();		
	    return newActivityForFindRoute;
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
		System.out.println("viaje view for find route fragment will be displayed");
		getActivity();
		settings = getActivity().getSharedPreferences("ViajePrefs", FragmentActivity.MODE_PRIVATE);
		prefEditor = settings.edit();
		
		super.onCreateView(inflater, container, savedInstanceState);
	    if (rootView!= null) {
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
	    try {
	    	rootView = inflater.inflate(R.layout.layout_find_route, container, false);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }
		
		adapter = new ArrayAdapter<String>(getActivity(),R.layout.item_list);
		adapter.setNotifyOnChange(true);

		autocompleterStart = (ClearableEditText)rootView.findViewById(R.id.message_start_location);
        autocompleterDest  = (ClearableEditText)rootView.findViewById(R.id.message_end_location);

 		autocompleterStart.setOnKeyListener(new addListenerOnKey(getActivity(), autocompleterStart));
        autocompleterStart.setAdapter(adapter);
        autocompleterStart.addTextChangedListener(new addListenerOnTextChange(getActivity(), autocompleterStart, adapter, this));
        
        autocompleterDest.setOnKeyListener(new addListenerOnKey(getActivity(), autocompleterDest));
        autocompleterDest.setAdapter(adapter);
        autocompleterDest.addTextChangedListener(new addListenerOnTextChange(getActivity(), autocompleterDest, adapter, this));
        
		getStartLocBtn = (ImageButton)rootView.findViewById(R.id.get_location_start);	
		getEndLocBtn = (ImageButton)rootView.findViewById(R.id.get_location_end);
				
		getStartLocBtn.setOnClickListener(new locatorButtonClickListener());
		getEndLocBtn.setOnClickListener(new locatorButtonClickListener());
		
		getCriteriaDistanceBtn = (ImageButton)rootView.findViewById(R.id.button_less_distance);
		getCriteriaCostBtn = (ImageButton)rootView.findViewById(R.id.button_less_cost);
		getCriteriaTransferBtn = (ImageButton)rootView.findViewById(R.id.button_less_transfer);

		getCriteriaDistanceBtn.setOnClickListener(new CriteriaButtonClickListener());
		getCriteriaCostBtn.setOnClickListener(new CriteriaButtonClickListener());
		getCriteriaTransferBtn.setOnClickListener(new CriteriaButtonClickListener());
	
		/* Switch Locations button */		
		switchLocationsBtn = (ImageButton)rootView.findViewById(R.id.button_switch_location);
		switchLocationsBtn.setOnClickListener(this);
		
		criteria_layout = (LinearLayout)rootView.findViewById(R.id.criteria_choices);
		image_divider = (ImageView)rootView.findViewById(R.id.shape_criteria_divider);
		setUpMapIfNeeded();

		if(settings.getBoolean("showIncidents", true)) {
	    	GetIncidentsLists getIncidents = new GetIncidentsLists(1, "incidents", 1, 40, 7);
	    	getIncidents.execute();
		} else {
			map.clear();	
		}
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
	}
	
	/* Listen to locator button click */
    private class locatorButtonClickListener implements ImageButton.OnClickListener 
    {
		@SuppressLint("CutPasteId")
		@Override
        public void onClick(View button) {
			/* Check what button is passed */
			ImageButton otherButton = null;
			String string_start;
			String string_end;
			if (button == (ImageButton)rootView.findViewById(R.id.get_location_start))
			{
				System.out.println("viaje start locator clicked");	
				otherButton = (ImageButton)rootView.findViewById(R.id.get_location_end);
			}
			else if (button == (ImageButton)rootView.findViewById(R.id.get_location_end))
			{
				System.out.println("viaje end locator clicked");	
				otherButton = (ImageButton)rootView.findViewById(R.id.get_location_start);
			}
			showCurrentLocation(coords);
            if (button.isSelected()){
                button.setSelected(false);
                if (button == (ImageButton)rootView.findViewById(R.id.get_location_start))
                {
                	autocompleterStart.setText("");
                }
                else
                {
                	autocompleterDest.setText("");
                }
            } else {
                if(otherButton.isSelected()) {
                	otherButton.setSelected(false);
                    if (button == (ImageButton)rootView.findViewById(R.id.get_location_start))
                    {
                    	autocompleterStart.setText("");
                    }
                    else
                    {
                    	autocompleterDest.setText("");
                    }
                }
                button.setSelected(true);
                //...Handled toggle on
                string_start = autocompleterStart.getText().toString();
                string_end = autocompleterDest.getText().toString();
                if (button == (ImageButton)rootView.findViewById(R.id.get_location_start))
                {
                	autocompleterStart.setText(CURRENT_LOCATION);
                	if (string_end.equals(CURRENT_LOCATION))
                	{
                		autocompleterDest.setText("");
                	}
                }
                else
                {
                	autocompleterDest.setText(CURRENT_LOCATION);
                	if (string_start.equals(CURRENT_LOCATION))
                	{
                		autocompleterStart.setText("");
                	}
                }
            }
        }
    }
    
    private class CriteriaButtonClickListener implements ImageButton.OnClickListener 
    {
    	
		@SuppressLint("CutPasteId")
		@Override
        public void onClick(View button) 
		{
			InputMethodManager inputManager = (InputMethodManager)            
					  getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
					    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
					    InputMethodManager.HIDE_NOT_ALWAYS);
			/* Check what button is passed */
			ImageButton otherCriteria1 = null;
			ImageButton otherCriteria2 = null;
			
			if (button == (ImageButton)rootView.findViewById(R.id.button_less_distance))
			{
				System.out.println("viaje start locator clicked");	
				otherCriteria1 = (ImageButton)rootView.findViewById(R.id.button_less_cost);
				otherCriteria2 = (ImageButton)rootView.findViewById(R.id.button_less_transfer);
			}
			else if (button == (ImageButton)rootView.findViewById(R.id.button_less_cost))
			{
				otherCriteria1 = (ImageButton)rootView.findViewById(R.id.button_less_distance);
				otherCriteria2 = (ImageButton)rootView.findViewById(R.id.button_less_transfer);
			}
			else if (button == (ImageButton)rootView.findViewById(R.id.button_less_transfer))
			{
				otherCriteria1 = (ImageButton)rootView.findViewById(R.id.button_less_cost);
				otherCriteria2 = (ImageButton)rootView.findViewById(R.id.button_less_distance);
			}
			
            if (button.isSelected())
            {
            	/* Note: When selected, do not deselect criteria */
                /* Display route result */
            } else 
            {
            	button.setSelected(true);
                if(otherCriteria1.isSelected() || otherCriteria2.isSelected())
                {
                	otherCriteria1.setSelected(false);
                	otherCriteria2.setSelected(false);
                }
                
                //...Handled toggle on
    			if (button == (ImageButton)rootView.findViewById(R.id.button_less_distance))
    			{
    				/* Display fastest route */
    				st_criteria = "Distance";
    			}
    			else if (button == (ImageButton)rootView.findViewById(R.id.button_less_cost))
    			{
    				/* Display cheapest route */
    				st_criteria = "Cost";
    			}
    			else if (button == (ImageButton)rootView.findViewById(R.id.button_less_transfer))
    			{
    				/* Display route with less transfer */
    				st_criteria = "Traffic";
    			}
    			else
    			{
    				st_criteria = "";
    			}
    			
    			int _userId = settings.getInt("userId", -1);
		        prefEditor.putString("from", autocompleterStart.getText().toString());
		        prefEditor.putString("to", autocompleterDest.getText().toString());
		        prefEditor.commit();
		        StoreSearchedTask storeSearch = new StoreSearchedTask(_userId, 
		        		autocompleterStart.getText().toString(), 
		        		autocompleterDest.getText().toString());
		        storeSearch.execute();
    			UtilsUI util = new UtilsUI();
    			try {
					util.planTripUtil(getActivity(), autocompleterStart.getText().toString(), autocompleterDest.getText().toString(), startLoc, endLoc, coords, st_criteria);
				} catch (IOException e) {
					e.printStackTrace();
				}
    			getCriteriaDistanceBtn.setSelected(false);
    			getCriteriaCostBtn.setSelected(false);
    			getCriteriaTransferBtn.setSelected(false);
    			if(markerStart != null) {
    				markerStart.remove();
    			}
    			if(markerEnd != null) {
    				markerEnd.remove();
    			}
    		
            }
        }
    }
    	
    /* Google Map Related */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
        	map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }
    
    private void setUpMap() {   
    	mUiSettings = map.getUiSettings();
    	mUiSettings.setMyLocationButtonEnabled(false);	
    	GPSTrackerUI mGPS = new GPSTrackerUI(getActivity());
		mLat = mGPS.getLatitude();
		mLong = mGPS.getLongitude();

		map.setMyLocationEnabled(true);
		map.setOnMyLocationButtonClickListener(this);
		GoogleMapOptions options = new GoogleMapOptions();
		MapFragment.newInstance(options);
		
		/* Show last location if cannot get latest location */
		if ((mLat < 1.0) && (mLong < 1.0)) {
			mLat = Double.parseDouble(settings.getString("Latitude", "0.0"));
			mLong = Double.parseDouble(settings.getString("Longitude", "0.0"));
		}
		coords = new LatLng(mLat, mLong);
        prefEditor.putString("Latitude", Double.toString(mLat));
        prefEditor.putString("Longitude", Double.toString(mLong));
        prefEditor.commit();
		showCurrentLocation(coords);
		map.setOnMapClickListener(this);
		map.setOnMapLongClickListener(this);
    }
    
    private void showCurrentLocation(LatLng coords) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
	    .target(coords)      // Sets the cernter of the map to Current Location
	    .zoom(17)                   // Sets the zoom
	    .bearing(0)                // Sets the orientation of the camera to north
	    .tilt(0)                   // Sets the tilt of the camera to 30 degrees
	    .build();                   // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    
    public void mapUpdate(AutoCompleteTextView mAutoComplete) {
    	ReverseGeocodeUI rGC = new ReverseGeocodeUI(getActivity());
    	double coords[] = {0.0, 0.0};    	
    	try {
			coords = rGC.getCoordinatesFromString(mAutoComplete.getText().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if(rGC.canGetCoordinates && rGC.isLocationValid) {
	    	if(coords[0] != 0.0 && coords[1] != 0.0 && coords != null) {    	
	    		if(mAutoComplete == autocompleterStart) {
	    			startLoc = coords;
	    		} else {
	    			endLoc = coords;
	    		}
	    	}
    	}
    }

    public void onMapClick(LatLng point) {
    }

    public void onMapLongClick(final LatLng point) {
		if(markerTemp != null) {
			markerTemp.remove();
		}
		final ReverseGeocodeUI rGC = new ReverseGeocodeUI(getActivity());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());	    	 
	    builder.setTitle("Set Location");
	    //builder.setIcon(R.drawable.icon);
	    builder.setMessage("Set location as...");
	    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		dialog.cancel();
        	}
	    });

	    builder.setNeutralButton("End", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
    			if(markerEnd != null) {
    				markerEnd.remove();
    			}
    			endLoc[0] = point.latitude;
    			endLoc[1] = point.longitude;
    			markerEnd = map.addMarker(new MarkerOptions()
    	        .position(point)
    	        .title("End")
    	        .draggable(false)
    	        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_end)));		
    			try {
    				outputName = rGC.getStringFromCoordinates(point.latitude, point.longitude);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			autocompleterDest.setText(outputName);
    	    	hideOrShowLayout();
            }
        });

	    builder.setNegativeButton("Start", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
				if(markerStart != null) {
					markerStart.remove();
				}
				startLoc[0] = point.latitude;
				startLoc[1] = point.longitude;
				markerStart = map.addMarker(new MarkerOptions()
		        .position(point)
		        .title("Start")
		        .draggable(false)
		        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_start)));		
				try {
					outputName = rGC.getStringFromCoordinates(point.latitude, point.longitude);
				} catch (IOException e) {
					e.printStackTrace();
				}
				autocompleterStart.setText(outputName);
		    	hideOrShowLayout();
	        }
	    });

	    // Showing Alert Message
    	builder.show();
    }
    
    //End google maps related methods
	@Override
	public void onClick(View v) {
		double temp[] = {0.0, 0.0};

		switch (v.getId()) {
		case (R.id.button_switch_location):
	    	System.out.println("viaje switch arrow is clicked");
	    	
	    	/* Rotate image to self */	
			Animation rotation = new RotateAnimation(0.0f, 180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotation.setFillAfter(true);
			rotation.setDuration(400);
		
			/* Switch Locations */
			String tempString = autocompleterDest.getText().toString();
			autocompleterDest.setText(autocompleterStart.getText().toString());
			autocompleterStart.setText(tempString);
			ImageButton startButton = null;
			ImageButton endButton = null;
			startButton = (ImageButton)rootView.findViewById(R.id.get_location_start);
			endButton = (ImageButton)rootView.findViewById(R.id.get_location_end);

			showCurrentLocation(coords);
			if (startButton.isSelected() || endButton.isSelected()) {
	            if (startButton.isSelected()){
	            	startButton.setSelected(false);
	            	endButton.setSelected(true);
	            } else {
	            	startButton.setSelected(true);
	            	endButton.setSelected(false);
	            }
			}
			temp = startLoc;
			startLoc = endLoc;
			endLoc = temp;
			/* Start Animation */
			switchLocationsBtn.startAnimation(rotation);
			break;
		}
	}

	@Override
	public boolean onMyLocationButtonClick() {
		System.out.println("Test");
		return false;
	}
	
	public void hideOrShowLayout() {
		if (autocompleterStart.getText().toString().equals("") || autocompleterDest.getText().toString().equals(""))
		{
			criteria_layout.setVisibility(View.GONE);
			image_divider.setVisibility(View.GONE);
		}
		else
		{
			criteria_layout.setVisibility(View.VISIBLE);
			image_divider.setVisibility(View.VISIBLE);
		}
	}

	class StoreSearchedTask extends AsyncTask<Void, Void, String> {
		int userId;
		String from;
		String to;
		
		public StoreSearchedTask(int userId, String from, String to) {
			this.userId = userId;
			this.from = from;
			this.to = to;
		}
		
		@Override
		protected String doInBackground(Void... params) {	
			ReverseGeocodeUI rGC = new ReverseGeocodeUI(getActivity());
			JSONWebInterface webInterface = new JSONWebInterface();
			try {
					if(from.equals(CURRENT_LOCATION)) {
						from = rGC.getStringFromCoordinates(coords.latitude, coords.longitude);
					} else if(to.equals(CURRENT_LOCATION)) {
						to = rGC.getStringFromCoordinates(coords.latitude, coords.longitude);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			String str = webInterface.sendSubmitSearchRequest(userId, from, to);
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			//
		}
	}
	
	//FIXME: Add different icon per incident?
	public void createIncidentMarkers(TrafficAlertInfo alertInfo) {
		MarkerOptions markerAlerts = null;
		for(int i = 0; i < alertInfo.alerts.size(); i++) {
			
			/* 20130930 - start TODO set condition for if and if-else depending on reported incident */
			if(alertInfo.alerts.get(i).description.contains("Collision")) {
				System.out.println(alertInfo.alerts.get(i).lat + " : " + alertInfo.alerts.get(i).lon + " : "  + alertInfo.alerts.get(i).description);
				markerAlerts = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_collision));
			} else if (alertInfo.alerts.get(i).description.contains("NPAV")) {
				markerAlerts = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_npav));
			}
			else if (alertInfo.alerts.get(i).description.contains("NPLV"))
			{
				markerAlerts = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_nplv));
			}
			else if (alertInfo.alerts.get(i).description.contains("PATV"))
			{
				markerAlerts = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_patv));
			}
			else if (alertInfo.alerts.get(i).description.contains("Standstill Traffic"))
			{
				markerAlerts = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_standstill));
			}
			else if (alertInfo.alerts.get(i).description.contains("Heavy Traffic"))
			{
				markerAlerts = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_heavy));
			}
			else if (alertInfo.alerts.get(i).description.contains("Moderate Traffic"))
			{
				markerAlerts = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_moderate));
			} else {
				//Change pin to default incidents? (BTW I modified all incidents to be incidents (no more hazard/report/etc) for now
				markerAlerts = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_moderate));
			}
			/* 20130930 - end */
			incidentMarkers = map.addMarker(markerAlerts);
		}
	}
	
	class GetIncidentsLists extends AsyncTask<Void, Void, String> {
		int userId;
		String type;
		int page;
		int count;
		int daysPast;
		TrafficAlertInfo alertInfo;
		JSONWebInterface webInterface;
		
		public GetIncidentsLists(int userId, String type, int page, int count, int daysPast) {
			this.userId = userId;
			this.type = type;
			this.page = page;
			this.count = count;
			this.daysPast = daysPast;
		}
		
		//FIXME: not just all incidents
		@Override
		protected String doInBackground(Void... params) {
			JSONWebInterface webInterface = new JSONWebInterface();
			String str = webInterface.sendGetAllIncidentsRequest(1, "incident", 1, 40, 7);		
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			if(!result.equals("")) {
				JSONWebInterface webInterface = new JSONWebInterface();
				alertInfo = webInterface.extractAlerts(result);
				createIncidentMarkers(alertInfo);
			}
		}
	}
}
