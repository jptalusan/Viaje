package com.viaje.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.abeanie.SavedRoutesList;
import com.example.otpxmlgetterUI.GPSTrackerUI;
import com.example.otpxmlgetterUI.ReverseGeocodeUI;
import com.example.otpxmlgetterUI.UtilsUI;
import com.viaje.main.R;

public class UiMainActivity extends FragmentActivity 
{
	/* Declaration of navigation drawer stuff */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
	//private String[] mMenuTitle;
	private List<NavigationMenu> ViajeNavigationMenu = new ArrayList<NavigationMenu>();
	//for backing
	private final static String MAIN_TO_FRAGMENT = "TAG_FRAGMENT";
	public final static String FROM_DRAWER = "TRUE";
	TextView title = null;
	String clicked_item = ""; 
	ActionBar bar;
	public int drawer_position = 0;
	private CreatePopupMenu incidentPopUp;
	/* Declaration for Find Route */
	static Context context;
	static View FindRouteView;
	
	/* 20130929 */
	Typeface face;
	
	/* Fragments Initialization */
	Fragment findRouteFragment = new FindRouteFragment();
	//ListFragment saveRouteFragment = new SavedRouteActivity();
	Fragment IfNoNetworkFragment = new IfNoNetworkFragment();
	Fragment displayStatisticsFragment = new DisplayStatistics();
	Fragment displaySettingsFragment = new ModifySettings();
	
    final String[] MenuOptions ={
            "com.example.viajeui.FindRouteActivity",
            "com.example.viajeui.SavedRouteActivity",
            "com.example.viajeui.ReportIncidentActivity",
            "com.example.viajeui.DisplayStatistics",
            "com.example.viajeui.DisplayAbout",
            "com.example.viajeui.DisplayHelp"
            };
    
	static ImageButton getStartLocBtn;	
	static ImageButton getOtherLocBtn;
	static ImageButton getEndLocBtn;

	private SharedPreferences settings; 
	private SharedPreferences.Editor prefEditor;
	
	public boolean isNetworkEnabled;
	private String username;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_main);
        /* Enable ActionBar application icon to behave as action to toggle navigation drawer */	
		/* FIXME when opening app, action bar is black */ //Check IWANTALL APP on how to fix
        face = Typeface.createFromAsset(getAssets(),"fonts/SohoGothic.ttf");
        bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.abs_layout);
        bar.setBackgroundDrawable(getResources().getDrawable(R.color.blueViaje));
        
        /* for DEMO */
        copyAssets(); 
        
        /* Set the activity as Context (to use fragment) */
        context = this;
		settings = getSharedPreferences("ViajePrefs", MODE_PRIVATE);
		prefEditor = settings.edit();
        username = settings.getString("username", "User");
    	GetOTPServerAddrTask serverTask = new GetOTPServerAddrTask();
    	serverTask.execute();

        /* Generate title */
        mTitle = mDrawerTitle = getTitle();
        
        /* Acquire string for menu items */
        //mMenuTitle = getResources().getStringArray(R.array.menu_array);
        
        /* Locate DrawerLayout */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        
        /* Locate ListView in DrawerLayout */
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        /* Locate drawer icon. When this is clicked, drawer is open or closed */
        ImageButton navDrawerOpen = (ImageButton)findViewById(R.id.openNavDrawer);
        navDrawerOpen.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
                if(incidentPopUp.isPopUpMenuIsShowing()) {
                	incidentPopUp.closePopup();
                	incidentPopUp.setPopUpMenuIsShowing(false);
                }
				if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				} else {
					mDrawerLayout.openDrawer(Gravity.LEFT);		
				}
			}
        });
                		        
        /* Set a custom shadow that overlays the main content when the drawer opens */
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        /* Populates list in navigation drawer */
        populateNavigationDrawerList();
        populateListView();
        
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
     
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon     
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) 
        {
            @Override
			public void onDrawerClosed(View view) 
            {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
			public void onDrawerOpened(View drawerView) 
            {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
              
        if (savedInstanceState == null) 
        {
        	/* 20130926 - Set selectItem to 1 to go to Get Directions */
            selectItem(1);
        }
        
        /* NOTE: To hide action bar */
        /* getActionBar().hide();*/ 
        
        /* 20130914 Call for pop up window when report incident button
         * in action bar is clicked
         */
        ImageButton icReportIncident = (ImageButton)findViewById(R.id.buttonReportIncident);
        incidentPopUp = new CreatePopupMenu(this, UiMainActivity.this);
        icReportIncident.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				GPSTrackerUI gps = new GPSTrackerUI(UiMainActivity.this);
				if(gps.isGPSEnabled()) {
					System.out.println("viaje clicked report incident button");
					if (!incidentPopUp.isPopUpMenuIsShowing())
					{
						if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
							mDrawerLayout.closeDrawer(Gravity.LEFT);
						}
						incidentPopUp.showPopup();
				    	GPSTrackerUI mGPS = new GPSTrackerUI(UiMainActivity.this);
						final ReverseGeocodeUI rGC = new ReverseGeocodeUI(UiMainActivity.this);
	
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
	}
	
	@Override
	public void onResume() {
		super.onResume();
        title = (TextView) findViewById(R.id.title);
        title.setText("viaje");
        title.setTypeface(face);
        title.setTextSize(22.0f);
        mDrawerList.setItemChecked(1, true);
	}
	
	private void populateNavigationDrawerList()
	{
		ViajeNavigationMenu.add(new NavigationMenu(R.drawable.ic_navmenu_user, username));

		/* Get Direction  */
		ViajeNavigationMenu.add(new NavigationMenu(R.drawable.ic_navmenu_get_direction_2, R.string.string_find_route));
		
		/* Saved Routes */
		ViajeNavigationMenu.add(new NavigationMenu(R.drawable.ic_navmenu_save_route, R.string.string_saved_route));

		/* Statistics*/
		ViajeNavigationMenu.add(new NavigationMenu(R.drawable.ic_navmenu_settings, R.string.string_settings));
		
//		/* About */
//		ViajeNavigationMenu.add(new NavigationMenu(R.drawable.ic_navmenu_about, R.string.string_about));
		
		/* Help */
		ViajeNavigationMenu.add(new NavigationMenu(R.drawable.ic_navmenu_help, R.string.string_help));
		
		/* Log-out */
		ViajeNavigationMenu.add(new NavigationMenu(R.drawable.ic_navmenu_log_out, R.string.string_log_out));
	}

	private void populateListView()
	{
		ArrayAdapter<NavigationMenu> adapter = new MyListAdapter();
		ListView list = (ListView) findViewById(R.id.left_drawer);
		list.setAdapter(adapter);
	}

	private class MyListAdapter extends ArrayAdapter<NavigationMenu>
	{
		public MyListAdapter()
		{
			super(UiMainActivity.this, R.layout.layout_drawer_menu_item, ViajeNavigationMenu);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View itemView = convertView;
			if (itemView == null)
			{
				itemView = getLayoutInflater().inflate(R.layout.layout_drawer_menu_item, parent, false);
			}
			
			NavigationMenu currentMenuItem = ViajeNavigationMenu.get(position);
			
			ImageView imageView = (ImageView)itemView.findViewById(R.id.item_icon);
			imageView.setImageResource(currentMenuItem.getIconMenuID());
			
			TextView menuText = (TextView)itemView.findViewById(R.id.item_txtCondition);
			if(position == 0) {
				menuText.setText(username);
			} else {
				menuText.setText(currentMenuItem.getStringPageTitle());				
			}
			
			return itemView;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ui_main, menu);		
		return super.onCreateOptionsMenu(menu);
	}
	
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) 
    {
        // If the nav drawer is open, hide action items related to the content view
        /* TODO add the icons in action bar here */
        /* boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList); */
        /* menu.findItem(R.id.action_websearch).setVisible(!drawerOpen); */
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) 
        {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) 
        {
        /* TODO add actions for selected action bars here */
        /*
        	case R.id.action_websearch:
        		// create intent to perform web search for this planet
        		Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        		intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
        		// catch event that there's no activity to handle intent
        		if (intent.resolveActivity(getPackageManager()) != null)
        		{
        			startActivity(intent);
        		} 
        		else 
        		{
        			Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
        		}
        		return true;*/
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }
	
	/* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener 
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
            selectItem(position);
        }
    }

    // TODO: must fix title and clicked item on drawer when back is pressed
	public void selectItem(int position) {
		UtilsUI util = new UtilsUI();
		title = (TextView) findViewById(R.id.title);
		clicked_item = getResources().getStringArray(R.array.menu_array)[position]; 
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		FragmentManager fm = this.getSupportFragmentManager();
		Intent intent;
		// Locate Position
		this.drawer_position = position;
		switch (position) {
		case 0:
			/* TODO ADD FRAGMENT FOR SETTINGS */
			if(util.isNetworkAvailable(context)) {
				ft.replace(R.id.content_frame, displayStatisticsFragment, MAIN_TO_FRAGMENT);
			} else {
				ft.replace(R.id.content_frame, IfNoNetworkFragment, MAIN_TO_FRAGMENT);
			}
			ft.addToBackStack(null);
			break;
		case 1:
			if(util.isNetworkAvailable(context)) {
				ft.replace(R.id.content_frame, findRouteFragment, MAIN_TO_FRAGMENT);
			} else {
				ft.replace(R.id.content_frame, IfNoNetworkFragment, MAIN_TO_FRAGMENT);
			}

			//Remove all fragments on backstack
			for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {    
			    fm.popBackStack();
			}
			break;
		case 2:
			intent = new Intent (context, SavedRoutesList.class);
			context.startActivity(intent);
			break;
		case 3:
			ft.replace(R.id.content_frame, displaySettingsFragment, MAIN_TO_FRAGMENT);
			ft.addToBackStack(null);
			break;
//		case 4:
//			ft.replace(R.id.content_frame, displayAboutFragment, MAIN_TO_FRAGMENT);
//			ft.addToBackStack(null);
//			break;
		case 4:
			intent = new Intent (context, Tutorial.class);
			intent.putExtra(FROM_DRAWER, true);
			context.startActivity(intent);
			break;
		case 5:
	        prefEditor = settings.edit();
	        prefEditor.remove("email");
	        prefEditor.remove("password");
	        prefEditor.remove("username");
	        prefEditor.putBoolean("loggedin", false);
	        prefEditor.commit();
			intent = new Intent (context, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
			finish();
			break;
		}
		if(position == 1) 
		{
			title.setText("viaje");
			title.setTextSize(22.0f);
			title.setTypeface(face);
		} else if(position == 4) {
			//do nothing
		} else {
			title.setText(clicked_item);
			title.setTextSize(18.0f);
			title.setTypeface(null, Typeface.NORMAL);
		}
		
		/* Commit fragment */
		ft.commit();
		mDrawerList.setItemChecked(position, true);
		// Close drawer
		mDrawerLayout.closeDrawer(mDrawerList);
	}

    @Override
    public void setTitle(CharSequence title) 
    {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
    
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) 
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    // TODO : prevent memory leaks 
    // http://stackoverflow.com/questions/8482606/when-a-fragment-is-replaced-and-put-in-the-back-stack-or-removed-does-it-stay
    @Override
    public void onBackPressed() {
    	title.setText("viaje");
    	title.setTypeface(face);
    	title.setTextSize(22.0f);
        mDrawerList.setItemChecked(1, true);
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                if(incidentPopUp.isPopUpMenuIsShowing()) {
                	incidentPopUp.closePopup();
                	incidentPopUp.setPopUpMenuIsShowing(false);
                }
				if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				} else {
					mDrawerLayout.openDrawer(Gravity.LEFT);		
				}
                return true;
        }

        return super.onKeyDown(keycode, e);
    }
    
    /**
     * For Demo only
     * 
     */
    private void copyAssets() {
	    final String root_sd = Environment.getExternalStorageDirectory().toString();
	    //root_sd = storage/sdcard0/Viaje
	    File SDCardRoot = new File( root_sd + "/Viaje" );
	    boolean success = true;
	    if (!SDCardRoot.exists()) {
	        success = SDCardRoot.mkdir();
	    }
	    if (success) {
	        // Do something on success
	    } else {
	        // Do something else on failure 
	    }
	    File file = new File(root_sd + "/Viaje/Ateneo_to_Eastwood.txt");
	    if(!file.exists()) {
	        AssetManager assetManager = getAssets();
	        String[] files = null;
	        try {
	            files = assetManager.list("");
	        } catch (IOException e) {
	            Log.e("tag", "Failed to get asset file list.", e);
	        }
	        for(String filename : files) {
	        	if (filename.endsWith(".txt")) {
		            InputStream in = null;
		            OutputStream out = null;
		            try {
		              in = assetManager.open(filename);
		              File outFile = new File(SDCardRoot, filename);
		              out = new FileOutputStream(outFile);
		              copyFile(in, out);
		              in.close();
		              in = null;
		              out.flush();
		              out.close();
		              out = null;
		            } catch(IOException e) {
		                Log.e("tag", "Failed to copy asset file: " + filename, e);
		            }
	        	}
	        }
	    }

    }
    
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }
    
	class GetOTPServerAddrTask extends AsyncTask<Void, Void, String> {

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
		
		private String getOTPServerAddress() {
			InputStream addrUrlStream = null;
			String addrStr = "";
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet get = new HttpGet("https://dl.dropboxusercontent.com/u/18334976/OTPServerAddress.txt");
				//if header is not set, response is in json format
				HttpResponse response = httpclient.execute(get);
				addrUrlStream = response.getEntity().getContent();
			} catch (Exception e) {
				Log.e("[GET REQUEST]", "Network exception", e);
				return null;
			}

			/* Store Trip URL response into a string */
			addrStr = this.getStringFromInputStream(addrUrlStream);
			
			/* Close our file stream */
			try {
				addrUrlStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return addrStr;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String str = getOTPServerAddress();
			
			return str;
		}
		
		@Override
		protected void onPostExecute(String servername) {
 	        prefEditor = settings.edit();
        	prefEditor.putString("otpserver", servername);
	        prefEditor.commit();
		}
	}
}
