package com.viaje.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.viaje.main.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//TODO: add home/work feature, maybe pop up choice if clicked "location" button
public class ModifySettings extends Fragment implements OnClickListener
{
	private Button enable_alert;
	private TextView enable_alert_description;
	private CheckBox enable_alert_checkbox;

	private Button use_historical;
	private CheckBox use_historical_CB;
	
	private Button server_address;
	private static final int TEXT_ID = 0;
	
	private Button logout_button;
	private Button clear_stats;
	private String otpserverString;
	public View rootView;
	private SharedPreferences settings; 
	private SharedPreferences.Editor prefEditor;	
	private ViewGroup parent;
	private TextView currentServer;
	Intent intent;
	static Context context;
	
	public static ModifySettings newInstance(Context context) 
	{
		ModifySettings newActivityForModifySettings = new ModifySettings();
		return newActivityForModifySettings;
	}
			 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
	    if (rootView!= null) {
	        parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
	    try {
	    	rootView = inflater.inflate(R.layout.layout_settings, container, false);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }
	    
		settings = getActivity().getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
		otpserverString = settings.getString("otpserver", "0.0.0.0");
		String user= settings.getString("username", "username");
		String email = settings.getString("email", "email");
		
		TextView currentUser = (TextView)rootView.findViewById(R.id.text_user);
		TextView currentEmail = (TextView)rootView.findViewById(R.id.text_email);
		currentServer = (TextView)rootView.findViewById(R.id.btn_OTP_server_description);
		currentServer.setText("Current Server address is: " + otpserverString);
		currentEmail.setText(email);
		currentUser.setText(user);
		
		settings.getBoolean("debugmode", false);
		prefEditor = settings.edit();
		prefEditor.putBoolean("debugmode", false);
		prefEditor.commit();
		//Trying to fix back stack of fragments (not fixed)
		View abs_layout = inflater.inflate(R.layout.abs_layout, parent);		
		TextView title = (TextView)abs_layout.findViewById(R.id.title);
		ListView mDrawerList = (ListView)rootView.findViewById(R.id.left_drawer);
		System.out.println(mDrawerList == null);
		if(title != null) {
			title.setText("Settings");
		}

		/* 20130929 */
		enable_alert = (Button)rootView.findViewById(R.id.btn_enable_display_incident);
		enable_alert.setOnClickListener(this);
		/* TODO: on create, determine the user setting if enabled or disabled
		 * then set enable_alert to true if saved setting is enabled
		 * otherwise, set to false */

		enable_alert_checkbox = (CheckBox)rootView.findViewById(R.id.check_display_incident);
		/* TODO set same as enable_alert */
		enable_alert.setSelected(settings.getBoolean("showIncidents", true));
		enable_alert_checkbox.setChecked(settings.getBoolean("showIncidents", true));
		
		enable_alert_description = (TextView)rootView.findViewById(R.id.btn_enable_display_incident_description);
		
		/*Historical Data*/
		use_historical_CB = (CheckBox)rootView.findViewById(R.id.HistoricalDataCB);
		use_historical = (Button)rootView.findViewById(R.id.historicalData);
		use_historical.setOnClickListener(this);
		use_historical.setSelected(settings.getBoolean("useHistorical", true));
		use_historical_CB.setChecked(settings.getBoolean("useHistorical", true));
		/*end Historical Data*/
		
		server_address = (Button)rootView.findViewById(R.id.btn_OTP_server);
		server_address.setOnClickListener(this);
		
		logout_button = (Button)rootView.findViewById(R.id.btn_logout);
		logout_button.setOnClickListener(this);
		
		clear_stats = (Button)rootView.findViewById(R.id.clearStats);
		clear_stats.setOnClickListener(this);
		
		return rootView;
	}	 
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()) {
		case R.id.clearStats:
        	AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        	dialog.setTitle("Clear Statistics");
            dialog.setMessage("Are you sure you want to clear your statistics?");
            dialog.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        	        prefEditor = settings.edit();
                	prefEditor.putString("totalCost", "0.0");
                	prefEditor.putString("totalDistance", "0.0");
        	        prefEditor.commit();
        	        Toast.makeText(getActivity(), "Cleared", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
			break;
		case R.id.check_display_incident:
		case R.id.btn_enable_display_incident:
			if (enable_alert.isSelected())
			{
				enable_alert.setSelected(false);
				enable_alert_checkbox.setChecked(false);
				enable_alert_description.setText("Reported Incidents are not displayed on map");
				prefEditor = settings.edit();
				prefEditor.putBoolean("showIncidents", false);
				prefEditor.commit();
			}
			else
			{
				enable_alert.setSelected(true);
				enable_alert_checkbox.setChecked(true);
				enable_alert_description.setText("Reported Incidents are displayed on map");
				prefEditor = settings.edit();
				prefEditor.putBoolean("showIncidents", true);
				prefEditor.commit();
			}
			break;
		case R.id.historicalData:
		case R.id.HistoricalDataCB:
			if (use_historical.isSelected())
			{
				use_historical.setSelected(false);
				use_historical_CB.setChecked(false);
				prefEditor = settings.edit();
				prefEditor.putBoolean("useHistorical", false);
				prefEditor.commit();
			}
			else
			{
				use_historical.setSelected(true);
				use_historical_CB.setChecked(true);
				prefEditor = settings.edit();
				prefEditor.putBoolean("useHistorical", true);
				prefEditor.commit();
			}
			break;
		case R.id.btn_OTP_server:
			/* Displauy dialog box with edittext input */
			displayDialogBoxForServer();
			break;
		case R.id.btn_logout:
	        prefEditor = settings.edit();
	        prefEditor.remove("email");
	        prefEditor.remove("password");
	        prefEditor.remove("username");
	        prefEditor.putBoolean("loggedin", false);
	        prefEditor.commit();
			intent = new Intent (getActivity(), LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getActivity().startActivity(intent);
			getActivity().finish();
			break;
	  	}		
	}
	
	public void displayDialogBoxForServer()
	{
		AlertDialog.Builder server_dialog = new AlertDialog.Builder(getActivity());
		server_dialog.setTitle("Server Address");
		server_dialog.setMessage("What is the Server address that you will use?");
		final EditText input = new EditText(getActivity());
        input.setId(TEXT_ID);
        input.setText(otpserverString);
        server_dialog.setView(input);      
        
        server_dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        	 
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
	 	        prefEditor = settings.edit();
	        	prefEditor.putString("otpserver", input.getText().toString());
		        prefEditor.commit();
		        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
		        currentServer.setText("Current Server address is: " + input.getText().toString());
		        currentServer.invalidate();
				return;
            }
        });
        
        server_dialog.setNeutralButton("Identify Nearest Server", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	GetOTPServerAddrTask serverTask = new GetOTPServerAddrTask();
            	serverTask.execute();
            	return;
            }
        });
	    
        server_dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        
        server_dialog.show();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
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
	        currentServer.setText("Current Server address is: " + servername);
	        currentServer.invalidate();
		}
	}
}
