package com.viaje.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

import com.example.otpxmlgetterUI.DownloadFileFromURL;
import com.example.otpxmlgetterUI.GPSTrackerUI;
import com.example.otpxmlgetterUI.ReverseGeocodeUI;
import com.example.otpxmlgetterUI.RouteAsyncRunnerUI;
import com.example.otpxmlgetterUI.UtilsUI;
import com.abeanie.SavedRoutesList;
import com.atlach.trafficdataloader.TrafficAlertInfo;
import com.atlach.trafficdataloader.TrafficDataManager;
import com.atlach.trafficdataloader.TrafficDataUpdateEvent;
import com.atlach.trafficdataloader.TripInfo.Route;
import com.google.android.gms.maps.CameraUpdate;
import com.atlach.trafficdataloader.TripInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.viaje.main.R;
import com.viaje.main.Tab1Fragment.RouteParcelData;
import com.viaje.webinterface.JSONWebInterface;
//http://thepseudocoder.wordpress.com/2011/10/13/android-tabs-viewpager-swipe-able-tabs-ftw/
//https://github.com/umano/AndroidSlidingUpPanel
public class FindRouteResultActivity extends FragmentActivity 
							 implements TabHost.OnTabChangeListener, 
							 			ViewPager.OnPageChangeListener, 
							 			TrafficDataUpdateEvent,
							 			HistDataPopupEvent {
	ActionBar bar;
	String st_criteria;
	String st_destination;
	String Criteria = "CRITERIA";
	String Destination = "DESTINATION";
	ImageButton button;
	TextView text_destination;
	TextView text_criteria;
	String sStart = "";
	String sDestination = "";
	private int SAVE = 41;
	double sFromLat = 0.0;
	double sFromLong = 0.0;
	double sToLat = 0.0;
	double sToLong = 0.0;

	/* 201309 - Use Viaje colors in map */
	private static final int BLUE_TINT = 0xff22a9db;
	private static final int RED_TINT = 0xffeb4604;
	
	private static final int USE_CURRENT_DATA = 0;
	private static final int USE_HIST_DATA = 1;
	private static final int USE_NO_DATA = 2;
	private static final long HIST_DATA_AGE_FOR_UPDATE = 86400;	 // seconds (At least one day)
//	private static final long HIST_DATA_AGE_FOR_UPDATE = 259200; // seconds (At least three days)
//	private static final long HIST_DATA_AGE_FOR_UPDATE = 604800; // seconds (At least one week)
	
	private HistDataPopupMenu mHistDataPopupMenu = null;
	private int mSelectedDay = -1;
	private int mSelectedTime = -1;
	private String mSelectedWeather = "";
	RelativeLayout map;
	
	private GoogleMap mMap;
	private Marker markerStart = null;
	private Marker markerIncidents = null;
	private Marker markerEnd = null;
	private Polyline polyLineRoute = null;
	private Polyline polyLineComplete = null;
	private UiSettings mUiSettings;
    private TabHost mTabHost;
    private ViewPager mViewPager;
	public List<LatLng> listRoute = null;
	public List<List<LatLng>> listRoutes = null;
    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, FindRouteResultActivity.TabInfo>();
    private PagerAdapter mPagerAdapter;
    private class TabInfo {
        private String tag;
        TabInfo(String tag, Class<?> clazz, Bundle args) {
            this.tag = tag;
        }
   }
    class TabFactory implements TabContentFactory {
    	 
        private final Context mContext;
 
        /**
         * @param context
         */
        public TabFactory(Context context) {
            mContext = context;
        }
 
        /** (non-Javadoc)
         * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
         */
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
 
    }
    
    //for offline
    private String fileName = "";
    private String offlineFrom = "";
    private String offlineTo = "";
	
    //for online
    private String onlineFrom = "";
    private String onlineTo = "";
    private String sname = "";
    private String urlForDownload = "";
    
    private String startLat = "";
    private String startLon = "";
    private String endLat = "";
    private String endLon = "";
    
    private String criteria = "";
    private boolean isOffline = false;
    private SlidingUpPanelLayout layout;
	private SharedPreferences settings;
	private SharedPreferences.Editor prefEditor;
	private String otpserverString;
	private TripInfo tripInfo = null;
	private int selTripIndex[] = {-1, -1, -1};
	
	private TrafficDataManager tdm = null;
	private ProgressDialog mDialog;
	
	/* GetRouteAsyncTask basically shadows RouteAsyncRunnerUI --Francis */
	private GetRouteAsyncTask getRoute;
	private TextView routeSummary = null;
	
	private int height = 0;
	private String[] inputs = null;
	
	private RelativeLayout expand;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		System.out.println("viaje will start find route result");
		setContentView(R.layout.activity_find_route_result);
		
        
		Intent intent = getIntent();
		fileName = intent.getStringExtra(SavedRoutesList.EXTRA_JSON_FILE_NAME); /* --Changed by Francis */
		offlineFrom = intent.getStringExtra(SavedRoutesList.EXTRA_FROM);
		offlineTo = intent.getStringExtra(SavedRoutesList.EXTRA_TO);
        
		settings = this.getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
		prefEditor = settings.edit();
		otpserverString = settings.getString("otpserver", "");
		
		sname = otpserverString;
		onlineFrom = intent.getStringExtra(UtilsUI.START);
		onlineTo = intent.getStringExtra(UtilsUI.END);
		
		startLat = intent.getStringExtra(UtilsUI.F_LAT);
		startLon = intent.getStringExtra(UtilsUI.F_LONG);
		endLat = intent.getStringExtra(UtilsUI.T_LAT);
		endLon = intent.getStringExtra(UtilsUI.T_LONG);
		
		criteria = intent.getStringExtra(UtilsUI.CRITERIA);
		
		isOffline = intent.getBooleanExtra("OFFLINE", false);
		inputs = new String[] {fileName, offlineFrom, offlineTo, sname, onlineFrom, onlineTo, startLat, startLon, endLat, endLon};
		for(int i = 0; i < 10; i++) {
			if(inputs[i] == null) {
				inputs[i] = "";
			}
		}
		
		getRoute = new GetRouteAsyncTask(inputs, this, tripInfo);
		getRoute.execute();
		urlForDownload = getRoute.urlForDownload;
		Log.d("ViajeUI", "URL FOR DOWNLOAD: "  + urlForDownload);
		//now here
		System.out.println("viaje set up action bar for fragment activity");
        bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.abs_route_result_layout);
        bar.setBackgroundDrawable(getResources().getDrawable(R.color.blueViaje));
        bar.setDisplayHomeAsUpEnabled(true);
        
        // Initialise the TabHost
        this.initialiseTabHost(savedInstanceState, criteria);
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString(criteria)); //set the tab as per the saved state
        }
        
        /* Defer Initialization of the ViewPager until we know that getRoute has sane values --Francis */
//        // Intialise ViewPager
//        this.intialiseViewPager();
        
        layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        layout.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
        routeSummary = (TextView)findViewById(R.id.DisplayRouteSummary);
        expand = (RelativeLayout)findViewById(R.id.DisplayRouteSummaryLayout);
        layout.setDragView(expand);
        //TODO: Get size of screen so dynamically resize sliding panel
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.y;
        
        /* When back button in action bar is clicked, go to FindRoute page */
        System.out.println("viaje back button stuff");
        ImageButton actionBarPrevious = (ImageButton)findViewById(R.id.up_Button);
        actionBarPrevious.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
        });

        if(!isOffline) {
        	routeSummary.setText("From  " + onlineFrom + "\nTo  " + onlineTo);
        } else {
        	routeSummary.setText("From  " + offlineFrom + "\nTo  " + offlineTo);
        }
        
        //Panel Settings (slide up panel)
        //FIXME: Still hardcoded for screen display. Already asked for help in library https://github.com/umano/AndroidSlidingUpPanel/issues/31
        
        layout.setPanelHeight(height/2);
        
        layout.setPanelSlideListener(new PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < 0.2) {
                    if (getActionBar().isShowing()) {
                        getActionBar().hide();
                    }
                } else {
                    if (!getActionBar().isShowing()) {
                        getActionBar().show();
                    }
                }
            }

            @Override
            public void onPanelExpanded(View panel) {
            	layout.setPanelHeight(height/2);
                layout.setDragView(expand);
            }

            @Override
            public void onPanelCollapsed(View panel) {
                layout.setDragView(expand);
            }
        });
        
        /* 20130914 Call for pop up window when report incident button
         * in action bar is clicked
         */
        ImageButton icReportIncident = (ImageButton)findViewById(R.id.buttonReportIncident);
        final CreatePopupMenu incidentPopUp = new CreatePopupMenu(this, FindRouteResultActivity.this);
        icReportIncident.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				GPSTrackerUI gps = new GPSTrackerUI(FindRouteResultActivity.this);
				if(gps.isGPSEnabled()) {
					System.out.println("viaje clicked report incident button");
					if (!incidentPopUp.isPopUpMenuIsShowing())
					{
						incidentPopUp.showPopup();
				    	GPSTrackerUI mGPS = new GPSTrackerUI(FindRouteResultActivity.this);
						final ReverseGeocodeUI rGC = new ReverseGeocodeUI(FindRouteResultActivity.this);
	
						double mLat = mGPS.getLatitude();
						double mLong = mGPS.getLongitude();
						try {
							String currentlocation = rGC.getStringFromCoordinates(mLat, mLong);
							prefEditor.putString("CurrentLocation", currentlocation);
						} catch (IOException e) {
							e.printStackTrace();
						}
				        prefEditor.putString("Latitude", Double.toString(mLat));
				        prefEditor.putString("Longitude", Double.toString(mLong));
				        prefEditor.commit();
						incidentPopUp.setPopUpMenuIsShowing(true);
					} else {
						incidentPopUp.setPopUpMenuIsShowing(false);
						incidentPopUp.closePopup();
					}
				} else {

				}
			}
        });
        
        mHistDataPopupMenu = new HistDataPopupMenu(this, FindRouteResultActivity.this, this);
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("OnResume Called");
        if(listRoute != null) {
        	setUpMapIfNeeded(listRoute);
        }
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
		mHistDataPopupMenu.closePopup();
    }

	protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }
    
    /**
     * Initialize ViewPager
     */
    private void intialiseViewPager() {
        List<Fragment> fragments = new Vector<Fragment>();
        
        Log.i("INFO", "Number of Trips: " + tripInfo.trips.size());
        
        /* TODO Identify the trips to cycle through */
        int leastDistIdx = -1;
        int leastCostIdx = -1;
        int leastTrafficIdx = -1;
        

    	double tripCostLeast = 9000.0;
    	double tripDistLeast = 90000000.0;
    	int tripCondLeast = 9000;
        
        for (int tripIter = 0; tripIter < tripInfo.trips.size(); tripIter++) {
        	double tripCostTotal = 0.0;
        	double tripDistTotal = 0.0;
        	int tripCondTotal = 0;
        	
        	int routeListSize = tripInfo.trips.get(tripIter).routes.size();
        	for (int routeIter = 0; routeIter < routeListSize; routeIter++) {
        		Route route = tripInfo.trips.get(tripIter).routes.get(routeIter);
        		
        		if ((route.cond.contains("Heavy") == false)) {
        			tripCondTotal += 16;
        		} else if ((route.cond.contains("Medium") == false)) {
        			tripCondTotal += 4;
        		} else if ((route.cond.contains("Light") == false)) {
        			tripCondTotal += 1;
        		}
        		
        		tripCostTotal += route.getRegularCost(false);
        		tripDistTotal += Double.parseDouble(route.dist);
        	}
        	
        	if (tripCostTotal < tripCostLeast) {
        		leastCostIdx = tripIter;
        		tripCostLeast = tripCostTotal;
        	}
        	
        	if (tripDistTotal < tripDistLeast) {
        		leastDistIdx = tripIter;
        		tripDistLeast = tripDistTotal;
        	}
        	
        	if (tripCondTotal < tripCondLeast) {
        		leastTrafficIdx = tripIter;
        		tripCondLeast = tripCondTotal;
        	}
        	
        	/* Save this for future reference */
        	tripInfo.trips.get(tripIter).totalCost = tripCostTotal;
        	tripInfo.trips.get(tripIter).totalDist = tripDistTotal;
        	tripInfo.trips.get(tripIter).totalTraffic = tripCondTotal;
        }

        selTripIndex[0] = leastCostIdx;
        selTripIndex[1] = leastDistIdx;
        selTripIndex[2] = leastTrafficIdx;
        
        /* Cycle through at most, 3 trips */
        for (int i = 0; i < selTripIndex.length; i++) {
        	
        	if (selTripIndex[i] == -1) {
        		System.out.println("Invalid selected trip index: " + i);
        		continue;
        	}
        	
        	/* Create the parcelable array list */
        	ArrayList<RouteParcelData> parcelDataList = new ArrayList<RouteParcelData>();
        	
        	int routeListSize = tripInfo.trips.get(selTripIndex[i]).routes.size();
        	for (int routeIter = 0; routeIter < routeListSize; routeIter++) {
        		parcelDataList.add(new RouteParcelData(tripInfo.trips.get(selTripIndex[i]).routes.get(routeIter)));
        	}

        	/* Create the bundle for the tab */
        	Bundle tabExtras = new Bundle();
        	tabExtras.putParcelableArrayList("tab_route_list", parcelDataList);
        	tabExtras.putDouble("tab_trip_dist", tripInfo.trips.get(selTripIndex[i]).totalDist);
        	tabExtras.putDouble("tab_trip_cost", tripInfo.trips.get(selTripIndex[i]).totalCost);
        	tabExtras.putInt("tab_trip_cond", tripInfo.trips.get(selTripIndex[i]).totalTraffic);
        	
        	fragments.add(Fragment.instantiate(this, Tab1Fragment.class.getName(), tabExtras));
        }
        

        this.mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        //
        this.mViewPager = (ViewPager)super.findViewById(R.id.viewpager);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        this.mViewPager.setOnPageChangeListener(this);
        
    	if ((0 < selTripIndex.length) &&
    			(selTripIndex[0] >= 0)) {
			listRoute = listRoutes.get(0);
			setUpMapIfNeeded(listRoute);
        	UtilsUI util = new UtilsUI();
        	if(util.isNetworkAvailable(this) && settings.getBoolean("showIncidents", false)) {
            	GetIncidentsLists getIncidents = new GetIncidentsLists(1, "incidents", tripInfo.trips.get(selTripIndex[0]).routes, 1, 20, 1);
            	getIncidents.execute();
        	}
    	}
    }
    
    /**
     * Initialize the Tab Host
     */
    /**
     * @param args
     */
    private void initialiseTabHost(Bundle args, String criteria) {
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        TabInfo tabInfo = null;

        FindRouteResultActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Distance").setIndicator(""), ( tabInfo = new TabInfo("Route 1", Tab1Fragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        FindRouteResultActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Cost").setIndicator(""), ( tabInfo = new TabInfo("Route 2", Tab2Fragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        FindRouteResultActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Traffic").setIndicator(""), ( tabInfo = new TabInfo("Route 3", Tab3Fragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);

        mTabHost.setOnTabChangedListener(this);
        mTabHost.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.ic_criteria_less_cost_tab_gray);
        mTabHost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.ic_criteria_less_distance_tab_gray);
        mTabHost.getTabWidget().getChildAt(2).setBackgroundResource(R.drawable.ic_criteria_less_traffic_tab_gray);
        
        // Default to first tab
        if(criteria == null || criteria.equals("Cost")) {
        	mTabHost.setCurrentTab(0);
        } else if(criteria.equals("Distance")) {
        	mTabHost.setCurrentTab(1);       	
        } else {
        	mTabHost.setCurrentTab(2);
        }
        setTabColor(mTabHost);
    }
    
    /**
     * Add Tab content to the Tabhost
     * @param activity
     * @param tabHost
     * @param tabSpec
     * @param clss
     * @param args
     */
    private static void AddTab(FindRouteResultActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        // Attach a Tab view factory to the spec
        tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }
    
    /** (non-Javadoc)
     * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
     */
    public void onTabChanged(String tag) {
        //TabInfo newTab = this.mapTabInfo.get(tag);
		if(markerIncidents != null) {
			markerIncidents.remove();
		}
		if(markerStart != null) {
			markerStart.remove();
		}
		if(markerEnd != null) {
			markerEnd.remove();
		}
		if(polyLineRoute != null) {
			polyLineRoute.remove();
		}
        int pos = this.mTabHost.getCurrentTab();
        setTabColor(this.mTabHost);
        /* Guard against the possibility that listRoutes was not set properly
         * due to lack of connectivity with the OTP server or a failed routing 
         * request --Francis
         */
        if ((listRoutes != null) && 
        	(pos < listRoutes.size())) {
        	
        	/* Guard against the possibility that pos maps to an invalid selected trip */
        	if ((pos < selTripIndex.length) &&
        			(selTripIndex[pos] >= 0)) {
	        	updateMap(listRoutes.get(selTripIndex[pos]));
	        	UtilsUI util = new UtilsUI();
	        	if(util.isNetworkAvailable(this) && settings.getBoolean("showIncidents", false)) {
		        	GetIncidentsLists getIncidents = new GetIncidentsLists(1, "incidents", tripInfo.trips.get(selTripIndex[pos]).routes, 1, 20, 1);
		        	getIncidents.execute();
	        	}
	        	this.mViewPager.setCurrentItem(pos);
        	}
        }
    }
    
    public static void setTabColor(TabHost tabhost) {
    	tabhost.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.ic_criteria_less_cost_tab_gray);
    	tabhost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.ic_criteria_less_distance_tab_gray);
    	tabhost.getTabWidget().getChildAt(2).setBackgroundResource(R.drawable.ic_criteria_less_traffic_tab_gray);

    	if(tabhost.getCurrentTab() == 0) { 
    		tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundResource(R.drawable.ic_criteria_less_cost_tab_blue); // selected
    	} else if(tabhost.getCurrentTab() == 1) {
    		tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundResource(R.drawable.ic_criteria_less_distance_tab_blue); // selected
    	} else {
    		tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundResource(R.drawable.ic_criteria_less_traffic_tab_blue); // selected
    	}
    }
    
    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
    }
    
    
    /* (non-Javadoc)
     * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
     */
    @Override
    public void onPageSelected(int position) {
        this.mTabHost.setCurrentTab(position);
    }
 
    /* (non-Javadoc)
     * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
     */
    @Override
    public void onPageScrollStateChanged(int state) {
    }
    
    
    /* Google Map Related */
    public void setUpMapIfNeeded(List<LatLng> mTheList) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap(mTheList);
            }
        }
    }
    
    public void setUpMap(List<LatLng> list) {
    	LatLngBounds.Builder b = new LatLngBounds.Builder();
    	mUiSettings = mMap.getUiSettings();
    	mUiSettings.setMyLocationButtonEnabled(false);
		
        mMap.setMyLocationEnabled(true);
        //mMap.setOnMyLocationButtonClickListener(this);
        
		int listSize = list.size();

		/* 20130929 - change color of polyline */
		//get polyline from expandable list elements
		PolylineOptions polyLineOptions = new PolylineOptions().width(8).color(BLUE_TINT).geodesic(true);
		for (int i = 0; i < listSize - 1; i++) {
			polyLineOptions.add(list.get(i));
			b.include(list.get(i));
		}
		LatLngBounds bounds = b.build();
		polyLineComplete = mMap.addPolyline(polyLineOptions);
		
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200, 200, 5);
		mMap.moveCamera(cu);
		
		/* 20130929 - Changed pin icon used for start */
		mMap.addMarker(new MarkerOptions()
        .position(list.get(0))
        .title("Start")
        .draggable(false)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_start)));		
        /* .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); */
        
		/* 20130929 - Changed pin icon used for end */
		mMap.addMarker(new MarkerOptions()
        .position(list.get(listSize - 1))
        .title("End")
        .draggable(false)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_end)));	
        /* .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));    */
    }
    
	public void updateMap(String argument, String modeOfTranspo) {
		if(layout.isExpanded()) {
			layout.setPanelHeight(height/2);
			layout.collapsePane();
		} else {
			
		}
		LatLngBounds.Builder b = new LatLngBounds.Builder();
    	UtilsUI util = new UtilsUI();
    	listRoute = util.decodePoly(argument);
		int listSize = listRoute.size();
		if(polyLineRoute != null) {
			polyLineRoute.remove();
		}
		
		/* 20130929 - Change polyline color and width */
		PolylineOptions polyLineOptions = new PolylineOptions().width(8).color(RED_TINT).geodesic(true);
		for (int i = 0; i < listSize - 1; i++) {
			polyLineOptions.add(listRoute.get(i));
			b.include(listRoute.get(i));
		}
		LatLngBounds bounds = b.build();
		polyLineRoute = mMap.addPolyline(polyLineOptions);
		
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150, 150, 20);
		mMap.animateCamera(cu);
		
		if(markerStart != null) {
			markerStart.remove();
		}
		
		/* 20130929 - Plot mode of transportation to map */
		if (modeOfTranspo == "JEEP")
		{
			MarkerOptions markerOptionsStartJeep = new MarkerOptions()
			.position(listRoute.get(0))
			.title("Start")
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_transportation_jeepney));
			
			markerStart = mMap.addMarker(markerOptionsStartJeep);
		}
		else if (modeOfTranspo == "WALK")
		{
			MarkerOptions markerOptionsStartWalk = new MarkerOptions()
			.position(listRoute.get(0))
			.title("Start")
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_transportation_walk));
			
			markerStart = mMap.addMarker(markerOptionsStartWalk);
		}
		else if (modeOfTranspo == "BUS")
		{
			MarkerOptions markerOptionsStartBus = new MarkerOptions()
			.position(listRoute.get(0))
			.title("Start")
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_transportation_bus));
			
			markerStart = mMap.addMarker(markerOptionsStartBus);
		}
		else
		{
			MarkerOptions markerOptionsStartTrain = new MarkerOptions()
			.position(listRoute.get(0))
			.title("Start")
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_transportation_train));
			
			markerStart = mMap.addMarker(markerOptionsStartTrain);
		}
		
//		MarkerOptions markerOptionsStart = new MarkerOptions()
//		.position(listRoute.get(0))
//		.title("Start")
//		.draggable(false)
//		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//		markerStart = mMap.addMarker(markerOptionsStart);
		
		if(markerEnd != null) {
			markerEnd.remove();
		}
		
		/* 20130929 - Change pin end to end icon */
		/* 20130930 - Change ic_pin_end to ic_pin_transfer_end ... since real end pin is not removed (to avoid confusion) */
		MarkerOptions markerOptionsDest = new MarkerOptions()
		.position(listRoute.get(listSize - 1))
		.title("End")
		.draggable(false)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_transfer_end));
		/* .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)); */
		
		markerEnd = mMap.addMarker(markerOptionsDest);
    }
     
	public void updateMap(List<LatLng> list) {
		LatLngBounds.Builder b = new LatLngBounds.Builder();
		int listSize = list.size();
		if(polyLineComplete != null) {
			polyLineComplete.remove();
		}
		
		/* 20130929 - Change polyline color */
		PolylineOptions polyLineOptions = new PolylineOptions().width(8).color(BLUE_TINT).geodesic(true);
		for (int i = 0; i < listSize - 1; i++) {
			polyLineOptions.add(list.get(i));
			b.include(list.get(i));
		}
		LatLngBounds bounds = b.build();
		polyLineComplete = mMap.addPolyline(polyLineOptions);
		
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300, 300, 5);
		mMap.animateCamera(cu);
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(!isOffline) {
			menu.add(0, SAVE, 1, "Save").setIcon(R.drawable.ic_navmenu_save_route)
	        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed(); 
		finish();
	} 
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {       
		double distance = tripInfo.trips.get(selTripIndex[0]).totalDist < 1000 
				? tripInfo.trips.get(selTripIndex[0]).totalDist 
				: tripInfo.trips.get(selTripIndex[0]).totalDist/1000;
	    switch(item.getItemId()){
	    case 41:
	    	DownloadFileFromURL downloader = new DownloadFileFromURL(this, 
	    			onlineFrom, 
	    			onlineTo, 
	    			startLat, 
	    			startLon, 
	    			endLat, 
	    			endLon
	    			,tripInfo.trips.get(selTripIndex[0]).totalCost
	    			,UtilsUI.round(distance, 2)
	    			,tripInfo.trips.get(selTripIndex[0]).totalTraffic);
	    	try {
	    		
	    		if (getRoute != null) {
	    			urlForDownload = getRoute.urlForDownload;
	    		}
	    		
	    		// Block if url is invalid
	    		if (urlForDownload == null) {
	    			Toast.makeText(this, "Invalid URL for Download!", Toast.LENGTH_SHORT).show();
	    			return false;
	    		}
	    		if(settings.getBoolean("debugmode", false)) {
	    			return downloader.hasDownloadedFile("http://maps5.trimet.org/osm?mode=BICYCLE&toPlace=45.504966%2C-122.654349&fromPlace=45.5115225%2C-122.647633");
	    		} else {
	    			return downloader.hasDownloadedFile(urlForDownload);
	    		}
	
			} catch (Exception e) {
				e.printStackTrace();
			}
	    case android.R.id.home: 
	    	onBackPressed();
            return true;
        }
	    return super.onOptionsItemSelected(item);
    }
	
	private class GetRouteAsyncTask extends RouteAsyncRunnerUI {
		private Context mTaskContext = null;
		public GetRouteAsyncTask(String[] inputs, Context context, TripInfo t) {
			super(inputs, context, t);
			mTaskContext = context;
		}

		@Override
	    protected void onPostExecute(Void param) {
			super.onPostExecute(param);
			System.out.println("OnPostExecute: grAsyncTask");
			
			tripInfo = getRoute.getTripInfo();
			/* Catch the occasion where we failed to obtain trip plans from the server --Francis */
			if (tripInfo == null) {
				Toast.makeText(mTaskContext, "Failed to obtain trip plans from server!", Toast.LENGTH_SHORT).show(); /* 20130930 - TODO Change error message to alert dialog box or simplify message to be understandable by user */
				return;
			}

			listRoutes = getList();

			System.out.println("Reached InitTrafficDataManager");
			/* Initialize the traffic data manager */
			initialiseTrafficDataManager();
		}
	}
	
	private void initialiseTrafficDataManager() {
		/* Changed this so that reloading saved routes will still attempt to re-
		 * obtain traffic data only if the device's internet is available
		 * 	--Francis   */
        if (new UtilsUI().isNetworkAvailable(this)) { //moved here by JP so no need to display message
			Log.i("INFO","Initializing Traffic Data Manager...");
			tdm = new TrafficDataManager(this, tripInfo, this);
			Log.i("INFO","Starting Traffic Data Update Task...");
			
			boolean useHistorical = settings.getBoolean("useHistorical", true);
			TimeZone tz = TimeZone.getTimeZone("GMT+8");
			int currentHour = (Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY))*100;
			int today = Calendar.getInstance(tz).get(Calendar.DAY_OF_WEEK);
			
			if(isOffline && useHistorical) {
				/* Call pop-up to allow user to select whether to:
				 *		(1) Use Latest Traffic Data
				 * 		(2) Use Historical Traffic Data
				 * 		(3) Do not display Traffic Data  
				 */
				showTrafficDataOptsDialog();
			} else if (isOffline && !useHistorical) {
				//Use system date and time
				onHistDataOptsFinalized(0 /*STATUS_OK*/, today, currentHour, "Clear");
			} else if (!isOffline && useHistorical) {
				//Popup historical data stuff
				showTrafficDataOptsDialog();
			} else { //Online and uncheck
				startTrafficDataOptTask(USE_CURRENT_DATA);
			}
			
//			tdm.startTrafficDataUpdateTask();	// Used by Option (1)
			
			
			/** TODO: Add a second pop-up to allow the user to choose the options
			 * 			for using the Historical Traffic Data:
			 * 		(1) Day of Week - (Sunday to Saturday, Spinner)
			 * 		(2) Time of Day - (00:00 to 23:00)
			 * 		(3) Weather		- (Clear, Cloudy, Light Rain, Medium Rain, Heavy Rain, Rain Storm
			 */
//			tdm.startTrafficDataUpdateTask(1, "1700", "moderate rains", false);	// Example of using Option (2)
        } else {
        	/* Note: This is the same as the case where we don't show any traffic data --Francis */
        	tdm = new TrafficDataManager(this, tripInfo, this);
        	startTrafficDataOptTask(USE_HIST_DATA);
        	Log.i("INFO","Executing InitViewPagerTask...");
    		new InitViewPagerTask(TrafficDataManager.STATUS_OK).execute();
        }
	}

	@Override
	public void onTrafficDataUpdate(int status) {
		Log.i("INFO","Traffic Data Update Finished!");
		if (status != TrafficDataManager.STATUS_OK) {
			Log.i("INFO","[onTrafficDataUpdate] Error: Failed to obtain traffic data!");
			Log.i("INFO","Executing InitViewPagerTask...");
			new InitViewPagerTask(TrafficDataManager.STATUS_FAILED).execute();
			return;
		}
		
		if (tdm.getHistDataUpdateStatus() == true) {
			/* If the Hist Data file was updated successfully, we should update
			 * the last known update time for it in the shared prefs file */
			settings = getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
			prefEditor = settings.edit();
			prefEditor.putLong("lastHistDataUpdate", System.currentTimeMillis());
	        prefEditor.commit();
	        
	        /* Reset the hist data update status in the TrafficDataManager */
	        tdm.resetHistDataUpdateStatus();
		}
		
		tdm.getTrafficData(tripInfo);
		
		/* FIXME: Yet another workaround. Android blocks the callback from directly 
		 * 			calling intialiseViewPager(). Instead, we have to pass it through 
		 * 			another AsyncTask (InitViewPagerTask), which is why we have this.
		 * 				--Francis
		 */
		Log.i("INFO","Executing InitViewPagerTask...");
		new InitViewPagerTask(TrafficDataManager.STATUS_OK).execute();
		
	}
	
	private class InitViewPagerTask extends AsyncTask<Void, Void, Void> {
		private int mStatus = 0;
		
		public InitViewPagerTask(int status) {
			mStatus = status;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}

		@Override
	    protected void onPostExecute(Void param) {
			Log.i("INFO","Done: InitViewPagerTask");
			
			if (mStatus != 0) {
				Toast.makeText(getApplicationContext(), "Error: Failed to obtain traffic data!", Toast.LENGTH_SHORT).show();
			}
			
			
			/* NOW you have my permission to initialize the view pager */
			intialiseViewPager();
			if (mDialog != null) {
				mDialog.dismiss();
			}
			return;
		}
	}
	
	public void buttonCalls(View v) {
		System.out.println("button called");
	    switch(v.getId()){
	    case R.id.DisplayRouteSummary:
	    	layout.setPanelHeight(height);
	    	break;
	    }
	}
	
	//FIXME: Add different icon per incident?
	public void createIncidentMarkers(TrafficAlertInfo alertInfo) {
		MarkerOptions markerOptionsStart = null;
		for(int i = 0; i < alertInfo.alerts.size(); i++) {
			
			/* 20130930 - start TODO set condition for if and if-else depending on reported incident */
			if(alertInfo.alerts.get(i).description.contains("Collision")) {
				System.out.println(alertInfo.alerts.get(i).lat + " : " + alertInfo.alerts.get(i).lon + " : "  + alertInfo.alerts.get(i).description);
				markerOptionsStart = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_collision));
			} else if (alertInfo.alerts.get(i).description.contains("NPAV")) {
				markerOptionsStart = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_npav));
			}
			else if (alertInfo.alerts.get(i).description.contains("NPLV"))
			{
				markerOptionsStart = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_nplv));
			}
			else if (alertInfo.alerts.get(i).description.contains("PATV"))
			{
				markerOptionsStart = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_patv));
			}
			else if (alertInfo.alerts.get(i).description.contains("Standstill Traffic"))
			{
				markerOptionsStart = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_standstill));
			}
			else if (alertInfo.alerts.get(i).description.contains("Heavy Traffic"))
			{
				markerOptionsStart = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_heavy));
			}
			else if (alertInfo.alerts.get(i).description.contains("Moderate Traffic"))
			{
				markerOptionsStart = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_moderate));
			} else {
				//Change pin to default incidents? (BTW I modified all incidents to be incidents (no more hazard/report/etc) for now
				markerOptionsStart = new MarkerOptions()
				.position(new LatLng(alertInfo.alerts.get(i).lat, alertInfo.alerts.get(i).lon))
				.title(alertInfo.alerts.get(i).type)
				.draggable(false)
				.snippet(alertInfo.alerts.get(i).description)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_report_incident_moderate));
			}
			/* 20130930 - end */
			
			markerIncidents = mMap.addMarker(markerOptionsStart);
		}
	}
	
	class GetIncidentsLists extends AsyncTask<Void, Void, String> {
		int userId;
		String type;
		List<Route> routeList;
		int page;
		int count;
		int daysPast;
		TrafficAlertInfo alertInfo;
		JSONWebInterface webInterface;
		
		public GetIncidentsLists(int userId, String type, List<Route> routeList, int page, int count, int daysPast) {
			this.userId = userId;
			this.type = type;
			this.routeList = routeList;
			this.page = page;
			this.count = count;
			this.daysPast = daysPast;
		}
		
		//FIXME: not just all incidents
		@Override
		protected String doInBackground(Void... params) {
			JSONWebInterface webInterface = new JSONWebInterface();
			String str = webInterface.sendGetIncidentsRequest(1, "incident", routeList, 1, 20, 1);		
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			//
			JSONWebInterface webInterface = new JSONWebInterface();
			alertInfo = webInterface.extractAlerts(result);
			createIncidentMarkers(alertInfo);
		}
	}
	
	private void startTrafficDataOptTask(int optId) {
		switch(optId) {
			case USE_CURRENT_DATA:
		        mDialog = new ProgressDialog(this);
		        mDialog.setMessage("Obtaining traffic data from internet...");
		        mDialog.show();
				/* If we're using the current traffic data, then start
				 * the normal Traffic Data Update Task */
				if (tdm != null) {
					tdm.startTrafficDataUpdateTask();
				}
				break;
			case USE_HIST_DATA:
				/* If we're using the historical traffic data, then we
				 * have to show the Hist Data Popup Window to the user */
				new ShowHistDataOptsTask().execute();
				break;
			case USE_NO_DATA:
			default:
				/* If we're not going to use any traffic data, skip to
				 * initialising and displaying the ViewPager instead */
	    		new InitViewPagerTask(TrafficDataManager.STATUS_OK).execute();
				break;
		}
	}
	
	private class ShowHistDataOptsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
	    protected void onPostExecute(Void param) {
			if (mHistDataPopupMenu != null) {
				mHistDataPopupMenu.showPopup();
			}
		}
	}
	
	private void showTrafficDataOptsDialog() {
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	dialog.setTitle("Viaje Traffic Data Options");
        //dialog.setMessage(getString(R.string.traffic_data_opts_text));
        
        if (new UtilsUI().isNetworkAvailable(this)) {
        	dialog.setMessage(getString(R.string.traffic_data_opts_text));
		    DialogInterface.OnClickListener useCurrentDataListener = 
		    		new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startTrafficDataOptTask(USE_CURRENT_DATA);
							
						}
		    		};
		    dialog.setNegativeButton(getString(R.string.use_current_data_btnText), useCurrentDataListener);
        } else {
        	dialog.setMessage(getString(R.string.traffic_data_opts_text_offline));
        }
        
	    DialogInterface.OnClickListener useHistDataListener = 
	    		new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startTrafficDataOptTask(USE_HIST_DATA);
						
					}
	    		};   

	    DialogInterface.OnClickListener useNoDataListener = 
	    		new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startTrafficDataOptTask(USE_NO_DATA);
						
					}
	    		};
        
        dialog.setNeutralButton(getString(R.string.use_hist_data_btnText), useHistDataListener);
        dialog.setPositiveButton(getString(R.string.use_no_data_btnText), useNoDataListener);
        //dialog.show();
        AlertDialog alertDialog = dialog.show();
        TextView messageText = (TextView)alertDialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
	}

	@Override
	public void onHistDataOptsFinalized(int status, int dayOfWeek, int timeOfDay, String weather) {
		if (status != HistDataPopupMenu.STATUS_OK) {
			/* Return as if the USE_NO_DATA option was selected instead, but let the user know
			 * that we have failed them via Toast message */
			Toast.makeText(this, "Error: Failed to use historical traffic data!", Toast.LENGTH_SHORT).show();
    		new InitViewPagerTask(TrafficDataManager.STATUS_OK).execute();
    		return;
		}
		
		mSelectedDay = dayOfWeek;
		mSelectedTime = timeOfDay;
		mSelectedWeather = weather;
		
		new FinalizeHistDataOptsTask().execute();
	}
	
	private void finalizeHistDataOpts(int dayOfWeek, int timeOfDay, String weather) {
        String timeStr = "0000";
        boolean isHistDataUpdated = true;
        
        if ((timeOfDay < 1000) && (timeOfDay >= 0)) {
        	timeStr = ("0" + Integer.toString(timeOfDay));
        } else if ((timeOfDay < 2400) && (timeOfDay >= 0)) {
        	timeStr = (Integer.toString(timeOfDay));
        }
        
		settings = getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
		
		long lastHistDataUpdate = settings.getLong("lastHistDataUpdate", 0);
		long histDataUpdateDiff = (System.currentTimeMillis()/1000) - (lastHistDataUpdate/1000);
		
		/* Check if we need to push for a Hist Data file update */
		if (histDataUpdateDiff >  HIST_DATA_AGE_FOR_UPDATE) {
			isHistDataUpdated = false;
		}
		
		mHistDataPopupMenu.closePopup();

		/* Now, show the progress dialog */
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Obtaining historical traffic data...");
        mDialog.show();
        
        /* Start the Traffic Data Update task */
		tdm.startTrafficDataUpdateTask(dayOfWeek, timeStr, weather.toLowerCase(Locale.ENGLISH), isHistDataUpdated);
	}

	private class FinalizeHistDataOptsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... param) {
			return null;
		}

		@Override
	    protected void onPostExecute(Void param) {
			finalizeHistDataOpts(mSelectedDay, mSelectedTime, mSelectedWeather);
		}
	}
}
