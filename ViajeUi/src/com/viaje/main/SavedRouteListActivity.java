package com.viaje.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.otpxmlgetterUI.GPSTrackerUI;
import com.example.otpxmlgetterUI.ReverseGeocodeUI;
import com.viaje.main.R;


public class SavedRouteListActivity extends ListActivity {
	ActionBar bar;
	View rootView;
	static String mText;
	private File file;
	private List<String> myList;
	private List<String> myListOrig;
	private ArrayAdapter<String> adapter;
	private String root_sd;
	public final static String EXTRA_JSON_FILE_NAME = "com.example.viajeui.JSON_FILE_NAME";
	public final static String EXTRA_FROM = "com.example.otpxmlgetter.FROM";
	public final static String EXTRA_TO = "com.example.otpxmlgetter.TO";
	
	private SharedPreferences settings; 
	private SharedPreferences.Editor prefEditor;
	
	private CreatePopupMenu incidentPopUp;
	
	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
		settings = getSharedPreferences("ViajePrefs", MODE_PRIVATE);
		prefEditor = settings.edit();
        bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.abs_saved_routes_layout);
        bar.setBackgroundDrawable(getResources().getDrawable(R.color.blueViaje));
        bar.setDisplayHomeAsUpEnabled(true);
        ImageButton actionBarPrevious = (ImageButton)findViewById(R.id.up_Button);
        actionBarPrevious.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
        });
	    myList = new ArrayList<String>();   
	    myListOrig  = new ArrayList<String>();
	    root_sd = Environment.getExternalStorageDirectory().toString();
	    //root_sd = storage/sdcard0
	    
	    file = new File( root_sd + "/Viaje" );
	    if (!file.isDirectory()) {
	    	file.mkdirs();
	    	Toast.makeText(this, "Created directory: " + root_sd + "/Viaje", Toast.LENGTH_SHORT).show();
	    } else {
		    File list[] = file.listFiles();
		    for( int i=0; i< list.length; i++)
		    {
	            if (list[i].getName().endsWith(".txt")) {
	            	myListOrig.add(list[i].getName());
	            	String name = list[i].getName().replace(".txt","");
	            	name = name.replace("_To_", "\nTo: ");
	            	name = name.replace("_to_", "\nTo: ");
	            	myList.add("From: " + name);
	            }
		    }

		    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myList);
		    setListAdapter(adapter);
		    //setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myList ));
		    registerForContextMenu(getListView());
//		    ListView lv = getListView();
//		    lv.setDividerHeight(3);
//
//		   lv.setOnItemLongClickListener(new OnItemLongClickListener() {
//
//		        @Override
//		        public boolean onItemLongClick(AdapterView<?> parent, View view,
//		                int arg2, long arg3) {
//
//		        	adapter.notifyDataSetChanged();
//		            return false;
//		        }
//
//		    });
	    }
        ImageButton icReportIncident = (ImageButton)findViewById(R.id.buttonReportIncident);
        incidentPopUp = new CreatePopupMenu(this, SavedRouteListActivity.this);
        icReportIncident.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				GPSTrackerUI gps = new GPSTrackerUI(SavedRouteListActivity.this);
				if(gps.isGPSEnabled()) {
					System.out.println("viaje clicked report incident button");
					if (!incidentPopUp.isPopUpMenuIsShowing())
					{
						incidentPopUp.showPopup();
				    	GPSTrackerUI mGPS = new GPSTrackerUI(SavedRouteListActivity.this);
						final ReverseGeocodeUI rGC = new ReverseGeocodeUI(SavedRouteListActivity.this);
	
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

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saved_route, menu);
    }
    
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        List<String> names = myListOrig;
        switch(item.getItemId()) {
        case R.id.menu_delete:
              File file = new File(root_sd + "/Viaje/" + names.get((int)info.id));
              boolean deleted = file.delete();
              adapter.remove(adapter.getItem(info.position));
              Toast.makeText(this, names.get((int)info.id) + " has been deleted.", Toast.LENGTH_SHORT).show();
              return deleted;
        default:
              return super.onContextItemSelected(item);
        }
    }
    
	/* --Some Parts Removed by Francis
	 * 		Reading the actual file was pushed further back to the JSON parser 
	 * 		 so that we won't have to deal with it here (this is an Activity 
	 * 		 after all).
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		String toString;
		String fromString;
	    super.onListItemClick(l, v, position, id);
    	//Read text from file
	    File list[] = file.listFiles();
	    
	    /* Part used to derive the FromString and ToString to be passed with the intent */
	    String[] fileNames = list[position].getName().split("_");
	    try {
	    	String[] temp = fileNames[2].split("\\.t");
	    if(fileNames.length < 3) {
	    	fromString = "";
	    	toString = "";
	    } else {
	    	fromString = fileNames[0];
	    	toString = temp[0];
	    }

		settings = getSharedPreferences("ViajePrefs", MODE_PRIVATE);
		prefEditor = settings.edit();
        prefEditor.putString("from", fromString);
        prefEditor.putString("to", toString);
        prefEditor.commit();
        
    	Intent intent = new Intent(this, FindRouteResultActivity.class);
    	intent.putExtra(EXTRA_JSON_FILE_NAME, list[position].getName());
    	intent.putExtra(EXTRA_FROM, fromString);
    	intent.putExtra(EXTRA_TO, toString);
    	
    	startActivity(intent);
	    } catch (Exception e) {
	    	System.out.println("ERROR: " + fileNames[0]);
	    }
	}

	@Override
	public void onBackPressed() {	
		super.onBackPressed(); 
		finish();
	}    
	
	@Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {     
		finish();
        return true;
    }
	
	@Override
	public void onResume() {
		super.onResume();
	}
}
