package com.viaje.main;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.otpxmlgetterUI.UtilsUI;
import com.viaje.main.R;
import com.viaje.webinterface.JSONWebInterface;

/* 20130914 Pop Up menu button is clicked */
public class CreatePopupMenu extends UiMainActivity implements OnClickListener
{
	Context mContext;
	UiMainActivity mUiMainActivity;
	Activity mActivity;
	private PopupWindow popupMenu;
	private PopupWindow popupCollision;
	private PopupWindow popupFlood;
	private PopupWindow popupTraffic;
	private PopupWindow popupSectionBlock;
	private PopupWindow popupCrime;
	private PopupWindow popupConstruction;
	private boolean PopUpMenuIsShowing = false;
	private RelativeLayout viewGroupLocation = null;
	
	private Button btn_heavy;
	private Button btn_standstill;
	private Button btn_minor_collision;
	private Button btn_major_collision;
	private Button btn_patv;
	private Button btn_nplv;
	private Button btn_npav;
	private Button btn_moderate;
	
	private Button btn_report_collision;
	private Button btn_report_flood;
	private Button btn_report_traffic;
	private Button btn_report_block;
	private Button btn_report_construction;
	private Button btn_report_crime;
	
	private SharedPreferences settings; 
	private String currentLocation;
	private String latitude;
	private String longitude;
	private int userId;	
	private int hour;
	private int minute;
	private int am_pm;
	private double lat;
	private double lon;
	private String username;
	
	public CreatePopupMenu(Context context, Activity activity) {
		this.mContext = context;
		//this.mUiMainActivity = uiMainActivity;
		this.mActivity = activity;
	}
	
	public void showPopup() {
		System.out.println("viaje show popup menu");
		/* Flag to indicate that popup window is showing */
		setPopUpMenuIsShowing(true);
		
		/* Hide enter location when popup is shown */
			viewGroupLocation = (RelativeLayout) mActivity.findViewById(R.id.enter_location_field);
			if(viewGroupLocation != null) {
				viewGroupLocation.setVisibility(View.GONE);
			}

		new JSONWebInterface();
			
		/* Get coordinates from Pref */
		settings = mContext.getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
		userId = settings.getInt("userId", -1);
		currentLocation = settings.getString("CurrentLocation", "unknown");
		latitude = settings.getString("Latitude", "0.0");
		longitude = settings.getString("Longitude", "0.0");
		lat = Double.parseDouble(latitude);
		lon = Double.parseDouble(longitude);
		Calendar c = Calendar.getInstance(); 
		hour = c.get(Calendar.HOUR);
		minute = c.get(Calendar.MINUTE);
		am_pm = c.get(Calendar.AM_PM);
		username = settings.getString("username", "");
		
		/* TODO modify animation of popup window */
		RelativeLayout viewGroup = (RelativeLayout) mActivity.findViewById(R.id.layout_report_incident);
		LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View reportIncidentLayout = layoutInflater.inflate(R.layout.activity_show_report_incident_pop_up, viewGroup);
		
		popupMenu = new PopupWindow(mContext);
	    popupMenu.setContentView(reportIncidentLayout);
	    popupMenu.setBackgroundDrawable(new ColorDrawable(0));
	    popupMenu.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    //popupMenu.setFocusable(true); /* When this is set to TRUE, popup window is dismissed when user clicks outside window */
	    popupMenu.showAtLocation(reportIncidentLayout, Gravity.CENTER, 0, 0);
	    popupMenu.setOutsideTouchable(false);
	    
	    /* Report Incident click */
	    ImageButton close = (ImageButton) reportIncidentLayout.findViewById(R.id.button_close);
	    close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popupMenu.dismiss();
				if(viewGroupLocation != null) {
					viewGroupLocation.setVisibility(View.VISIBLE);
				}
				setPopUpMenuIsShowing(false);
			}
		});
	    
	    Button construction = (Button) reportIncidentLayout.findViewById(R.id.button_construction);
	    Button collision = (Button) reportIncidentLayout.findViewById(R.id.button_collision);
	    Button crime = (Button) reportIncidentLayout.findViewById(R.id.button_crime);
	    Button flood = (Button) reportIncidentLayout.findViewById(R.id.button_flood);
	    Button traffic = (Button) reportIncidentLayout.findViewById(R.id.button_traffic);
	    Button roadblock = (Button) reportIncidentLayout.findViewById(R.id.button_roadblock);
	    construction.setOnClickListener(this);
	    collision.setOnClickListener(this);
	    crime.setOnClickListener(this);
	    flood.setOnClickListener(this);
	    traffic.setOnClickListener(this);
	    roadblock.setOnClickListener(this);
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_collision:
			//Toast.makeText(mContext, "collision", Toast.LENGTH_SHORT).show();
			closeMainPopUp();
			showCollisionPopUpWindow();
			break;
		case R.id.button_construction:
			//Toast.makeText(mContext, "construction", Toast.LENGTH_SHORT).show();
			closeMainPopUp();
			showConstructionPopUpWindow();
			break;
		case R.id.button_crime:
			//Toast.makeText(mContext, "crime", Toast.LENGTH_SHORT).show();
			closeMainPopUp();
			showCrimeBlockPopUpWindow();
			break;
		case R.id.button_flood:
			//Toast.makeText(mContext, "flood", Toast.LENGTH_SHORT).show();
			closeMainPopUp();
			showFloodPopUpWindow();
			break;
		case R.id.button_traffic:
			//Toast.makeText(mContext, "traffic", Toast.LENGTH_SHORT).show();
			closeMainPopUp();
			showTrafficPopUpWindow();
			break;
		case R.id.button_roadblock:
			//Toast.makeText(mContext, "roadblock", Toast.LENGTH_SHORT).show();
			closeMainPopUp();
			showSectionBlockBlockPopUpWindow();
			break;
		}
		//Go to report incident layout?
		
	}
    /* Use setContentView(View contentView) to change content of popup window */
	        
	/* 20130914 Pop Up Window button is clicked */
	public void showCollisionPopUpWindow()
	{
		/* TODO modify animation of popup window */
		RelativeLayout viewGroupCollision = (RelativeLayout) mActivity.findViewById(R.id.layout_report_collision);
		LayoutInflater layoutInflaterCollision = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View reportCollisionLayout = layoutInflaterCollision.inflate(R.layout.popupwindow_collision, viewGroupCollision);
		popupCollision = new PopupWindow(mContext);
		
		popupCollision.setContentView(reportCollisionLayout);
		popupCollision.setBackgroundDrawable(new ColorDrawable(0));
		popupCollision.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popupCollision.showAtLocation(reportCollisionLayout, Gravity.CENTER, 0, 0);
		popupCollision.setOutsideTouchable(false);
		
		TextView location = (TextView) reportCollisionLayout.findViewById(R.id.location);
		location.setText(currentLocation);
		location.postInvalidate();
		TextView time = (TextView) reportCollisionLayout.findViewById(R.id.time);
		String new_minute;
		if(minute < 10) {
			new_minute = "0" + Integer.toString(minute);
		} else {
			new_minute = Integer.toString(minute);
		}
		time.setText(Integer.toString(hour) + ":" + new_minute + (am_pm == 1 ? " pm" : " am"));
		
		btn_report_collision = (Button) reportCollisionLayout.findViewById(R.id.button_send);
		btn_report_collision.setEnabled(false);
		final TextView txt_collision_title = (TextView) reportCollisionLayout.findViewById(R.id.ReportTitle);
		
		btn_minor_collision = (Button) reportCollisionLayout.findViewById(R.id.button_collision_minor);
		btn_minor_collision.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				/* Set text to minor collision */
				Button otherButton = (Button)reportCollisionLayout.findViewById(R.id.button_collision_major);
				if (otherButton.isSelected())
				{
					otherButton.setSelected(false);
				}

				if (btn_minor_collision.isSelected())
				{
					txt_collision_title.setText("Collision");
					btn_minor_collision.setSelected(false);
					btn_report_collision.setEnabled(false);
				}
				else{
					txt_collision_title.setText("Minor Collision");
					btn_minor_collision.setSelected(true);
					btn_report_collision.setEnabled(true);
				}				
			}
		});
		
		btn_major_collision = (Button) reportCollisionLayout.findViewById(R.id.button_collision_major);
		btn_major_collision.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				/* Set text to minor collision */
				Button otherButton = (Button)reportCollisionLayout.findViewById(R.id.button_collision_minor);
				if (otherButton.isSelected())
				{
					otherButton.setSelected(false);
				}

				if (btn_major_collision.isSelected())
				{
					txt_collision_title.setText("Collision");
					btn_major_collision.setSelected(false);
					btn_report_collision.setEnabled(false);
				}
				else{
					txt_collision_title.setText("Major Collision");
					btn_major_collision.setSelected(true);
					btn_report_collision.setEnabled(true);
				}
			}
		});
		
		btn_report_collision.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				closePopup();	
				setPopUpMenuIsShowing(false);
				
				ReportIncidentTask report = new ReportIncidentTask(userId, "incident", lat, lon, currentLocation, txt_collision_title.getText().toString());
				report.execute();
			}
		});
		Button btn_cancel = (Button) reportCollisionLayout.findViewById(R.id.button_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				returnPopUpWhenCancel();
			}
		});
		
	}
	
	/* 20130914 Pop Up Window button is clicked */
	public void showFloodPopUpWindow()
	{
		/* TODO modify animation of popup window */
		RelativeLayout viewGroupFlood = (RelativeLayout) mActivity.findViewById(R.id.layout_report_flood);
		LayoutInflater layoutInflaterFlood = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View reportFloodLayout = layoutInflaterFlood.inflate(R.layout.popupwindow_flood, viewGroupFlood);
		popupFlood = new PopupWindow(mContext);
		
		popupFlood.setContentView(reportFloodLayout);
		popupFlood.setBackgroundDrawable(new ColorDrawable(0));
		popupFlood.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popupFlood.showAtLocation(reportFloodLayout, Gravity.CENTER, 0, 0);
		popupFlood.setOutsideTouchable(false);
		
		TextView location = (TextView) reportFloodLayout.findViewById(R.id.location);
		location.setText(currentLocation);
		location.postInvalidate();
		TextView time = (TextView) reportFloodLayout.findViewById(R.id.time);
		String new_minute;
		if(minute < 10) {
			new_minute = "0" + Integer.toString(minute);
		} else {
			new_minute = Integer.toString(minute);
		}
		time.setText(Integer.toString(hour) + ":" + new_minute + (am_pm == 1 ? " pm" : " am"));
		
		btn_report_flood = (Button) reportFloodLayout.findViewById(R.id.button_send);
		btn_report_flood.setEnabled(false);
		
		final TextView txt_flood_title = (TextView) reportFloodLayout.findViewById(R.id.ReportTitle);
		
		btn_patv = (Button) reportFloodLayout.findViewById(R.id.button_patv);
		btn_patv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportFloodLayout.findViewById(R.id.button_nplv);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportFloodLayout.findViewById(R.id.button_npav);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_patv.isSelected())
				{
					txt_flood_title.setText("Flood");
					btn_patv.setSelected(false);
					btn_report_flood.setEnabled(false);
				}
				else{
					txt_flood_title.setText("PATV Flood");
					btn_patv.setSelected(true);
					btn_report_flood.setEnabled(true);
				}
			}
		});
		
		btn_nplv = (Button) reportFloodLayout.findViewById(R.id.button_nplv);
		btn_nplv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportFloodLayout.findViewById(R.id.button_patv);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportFloodLayout.findViewById(R.id.button_npav);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_nplv.isSelected())
				{
					txt_flood_title.setText("Flood");
					btn_nplv.setSelected(false);
					btn_report_flood.setEnabled(false);
				}
				else{
					txt_flood_title.setText("NPLV Flood");
					btn_nplv.setSelected(true);
					btn_report_flood.setEnabled(true);
				}
			}
		});
		
		btn_npav = (Button) reportFloodLayout.findViewById(R.id.button_npav);
		btn_npav.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportFloodLayout.findViewById(R.id.button_patv);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportFloodLayout.findViewById(R.id.button_nplv);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_npav.isSelected())
				{
					txt_flood_title.setText("Flood");
					btn_npav.setSelected(false);
					v.setEnabled(false);
				}
				else{
					txt_flood_title.setText("NPAV Flood");
					btn_npav.setSelected(true);
					btn_report_flood.setEnabled(true);
				}
			}
		});
		
		btn_report_flood.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				closePopup();	
				setPopUpMenuIsShowing(false);				
				ReportIncidentTask report = new ReportIncidentTask(userId, "incident", lat, lon, currentLocation, txt_flood_title.getText().toString());
				report.execute();

			}
		});
		Button btn_cancel = (Button) reportFloodLayout.findViewById(R.id.button_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				returnPopUpWhenCancel();
			}
		});
		
	}

	/* 20130914 Pop Up Window button is clicked */
	public void showTrafficPopUpWindow()
	{
		/* TODO modify animation of popup window */
		RelativeLayout viewGroupTraffic = (RelativeLayout) mActivity.findViewById(R.id.layout_report_traffic);
		LayoutInflater layoutInflaterTraffic = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View reportTrafficLayout = layoutInflaterTraffic.inflate(R.layout.popupwindow_traffic, viewGroupTraffic);
		popupTraffic = new PopupWindow(mContext);
		
		popupTraffic.setContentView(reportTrafficLayout);
		popupTraffic.setBackgroundDrawable(new ColorDrawable(0));
		popupTraffic.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popupTraffic.showAtLocation(reportTrafficLayout, Gravity.CENTER, 0, 0);
		popupTraffic.setOutsideTouchable(false);
		
		TextView location = (TextView) reportTrafficLayout.findViewById(R.id.location);
		location.setText(currentLocation);
		location.postInvalidate();
		TextView time = (TextView) reportTrafficLayout.findViewById(R.id.time);
		String new_minute;
		if(minute < 10) {
			new_minute = "0" + Integer.toString(minute);
		} else {
			new_minute = Integer.toString(minute);
		}
		time.setText(Integer.toString(hour) + ":" + new_minute + (am_pm == 1 ? " pm" : " am"));
		
		btn_report_traffic = (Button) reportTrafficLayout.findViewById(R.id.button_send);
		btn_report_traffic.setEnabled(false);
		
		final TextView txt_traffic_title = (TextView) reportTrafficLayout.findViewById(R.id.ReportTitle);
		
		btn_moderate = (Button) reportTrafficLayout.findViewById(R.id.button_moderate);
		btn_moderate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportTrafficLayout.findViewById(R.id.button_heavy);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportTrafficLayout.findViewById(R.id.button_standstill);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_moderate.isSelected())
				{
					txt_traffic_title.setText("Traffic");
					btn_moderate.setSelected(false);
					btn_report_traffic.setEnabled(false);
				}
				else{
					txt_traffic_title.setText("Moderate Traffic");
					btn_moderate.setSelected(true);
					btn_report_traffic.setEnabled(true);
				}
			}
		});
		
		btn_heavy = (Button) reportTrafficLayout.findViewById(R.id.button_heavy);
		btn_heavy.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportTrafficLayout.findViewById(R.id.button_moderate);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportTrafficLayout.findViewById(R.id.button_standstill);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_heavy.isSelected())
				{
					txt_traffic_title.setText("Traffic");
					btn_heavy.setSelected(false);
					btn_report_traffic.setEnabled(false);
				}
				else{
					txt_traffic_title.setText("Heavy Traffic");
					btn_heavy.setSelected(true);
					btn_report_traffic.setEnabled(true);
				}
			}
		});
		
		btn_standstill = (Button) reportTrafficLayout.findViewById(R.id.button_standstill);
		btn_standstill.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportTrafficLayout.findViewById(R.id.button_moderate);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportTrafficLayout.findViewById(R.id.button_heavy);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_standstill.isSelected())
				{
					txt_traffic_title.setText("Traffic");
					btn_standstill.setSelected(false);
					btn_report_traffic.setEnabled(false);
				}
				else{
					txt_traffic_title.setText("Standstill Traffic");
					btn_standstill.setSelected(true);
					btn_report_traffic.setEnabled(true);
				}
			}
		});
		
		btn_report_traffic = (Button) reportTrafficLayout.findViewById(R.id.button_send);
		btn_report_traffic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				closePopup();
				setPopUpMenuIsShowing(false);				
				ReportIncidentTask report = new ReportIncidentTask(userId, "incident", lat, lon, currentLocation, txt_traffic_title.getText().toString());
				report.execute();
				/* Send report in background */
			}
		});
		Button btn_cancel = (Button) reportTrafficLayout.findViewById(R.id.button_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				returnPopUpWhenCancel();
			}
		});
		
	}
	
	public void showSectionBlockBlockPopUpWindow()
	{
		/* TODO modify animation of popup window */
		RelativeLayout viewGroupSectionBlock = (RelativeLayout) mActivity.findViewById(R.id.layout_report_road_block);
		LayoutInflater layoutInflaterSectionBlock = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View reportSectionBlockLayout = layoutInflaterSectionBlock.inflate(R.layout.popupwindow_roadblock, viewGroupSectionBlock);
		popupSectionBlock = new PopupWindow(mContext);
		
		popupSectionBlock.setContentView(reportSectionBlockLayout);
		popupSectionBlock.setBackgroundDrawable(new ColorDrawable(0));
		popupSectionBlock.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popupSectionBlock.showAtLocation(reportSectionBlockLayout, Gravity.CENTER, 0, 0);
		popupSectionBlock.setOutsideTouchable(false);
		
		TextView location = (TextView) reportSectionBlockLayout.findViewById(R.id.location);
		location.setText(currentLocation);
		location.postInvalidate();
		TextView time = (TextView) reportSectionBlockLayout.findViewById(R.id.time);
		String new_minute;
		if(minute < 10) {
			new_minute = "0" + Integer.toString(minute);
		} else {
			new_minute = Integer.toString(minute);
		}
		time.setText(Integer.toString(hour) + ":" + new_minute + (am_pm == 1 ? " pm" : " am"));
		
		btn_report_block = (Button) reportSectionBlockLayout.findViewById(R.id.button_send);
		btn_report_block.setEnabled(false);
		
		final TextView txt_traffic_title = (TextView) reportSectionBlockLayout.findViewById(R.id.ReportTitle);
		
		final Button btn_one = (Button) reportSectionBlockLayout.findViewById(R.id.button_block_lane_1);
		btn_one.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportSectionBlockLayout.findViewById(R.id.button_block_lane_2);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportSectionBlockLayout.findViewById(R.id.button_block_lane_3);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_one.isSelected())
				{
					txt_traffic_title.setText("No. of Lanes Blocked");
					btn_one.setSelected(false);
					btn_report_block.setEnabled(false);
				}
				else{
					txt_traffic_title.setText("One Lane Blocked");
					btn_one.setSelected(true);
					btn_report_block.setEnabled(true);
				}
			}
		});
		
		final Button btn_two = (Button) reportSectionBlockLayout.findViewById(R.id.button_block_lane_2);
		btn_two.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportSectionBlockLayout.findViewById(R.id.button_block_lane_1);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportSectionBlockLayout.findViewById(R.id.button_block_lane_3);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_two.isSelected())
				{
					txt_traffic_title.setText("No. of Lanes Blocked");
					btn_two.setSelected(false);
					btn_report_block.setEnabled(false);
				}
				else{
					txt_traffic_title.setText("Two Lanes Blocked");
					btn_two.setSelected(true);
					btn_report_block.setEnabled(true);
				}
			}
		});
		
		final Button btn_three = (Button) reportSectionBlockLayout.findViewById(R.id.button_block_lane_3);
		btn_three.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportSectionBlockLayout.findViewById(R.id.button_block_lane_1);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportSectionBlockLayout.findViewById(R.id.button_block_lane_2);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_three.isSelected())
				{
					txt_traffic_title.setText("No. of Lanes Blocked");
					btn_three.setSelected(false);
					btn_report_block.setEnabled(false);
				}
				else{
					/* 20130929 */
					txt_traffic_title.setText("More Lanes Blocked");
					btn_three.setSelected(true);
					btn_report_block.setEnabled(true);
				}
			}
		});
		
		btn_report_block.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				closePopup();
				setPopUpMenuIsShowing(false);
				ReportIncidentTask report = new ReportIncidentTask(userId, "incident", lat, lon, currentLocation, txt_traffic_title.getText().toString());
				report.execute();			
				/* Send report in background */
			}
		});
		Button btn_cancel = (Button) reportSectionBlockLayout.findViewById(R.id.button_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				returnPopUpWhenCancel();
			}
		});
		
	}
	
	public void showConstructionPopUpWindow()
	{
		/* TODO modify animation of popup window */
		RelativeLayout viewGroupConstruction = (RelativeLayout) mActivity.findViewById(R.id.layout_report_construction);
		LayoutInflater layoutInflaterConstruction = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View reportConstructionLayout = layoutInflaterConstruction.inflate(R.layout.popupwindow_construction, viewGroupConstruction);
		popupConstruction = new PopupWindow(mContext);
		
		popupConstruction.setContentView(reportConstructionLayout);
		popupConstruction.setBackgroundDrawable(new ColorDrawable(0));
		popupConstruction.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popupConstruction.showAtLocation(reportConstructionLayout, Gravity.CENTER, 0, 0);
		popupConstruction.setOutsideTouchable(false);
		
		TextView location = (TextView) reportConstructionLayout.findViewById(R.id.location);
		location.setText(currentLocation);
		location.postInvalidate();
		TextView time = (TextView) reportConstructionLayout.findViewById(R.id.time);
		String new_minute;
		if(minute < 10) {
			new_minute = "0" + Integer.toString(minute);
		} else {
			new_minute = Integer.toString(minute);
		}
		time.setText(Integer.toString(hour) + ":" + new_minute + (am_pm == 1 ? " pm" : " am"));
		
		btn_report_construction= (Button) reportConstructionLayout.findViewById(R.id.button_send);
		btn_report_construction.setEnabled(false);
		
		final TextView txt_traffic_title = (TextView) reportConstructionLayout.findViewById(R.id.ReportTitle);
		
		final Button btn_street_light = (Button) reportConstructionLayout.findViewById(R.id.button_street_light);
		btn_street_light.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportConstructionLayout.findViewById(R.id.button_traffic_light);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportConstructionLayout.findViewById(R.id.button_bumpy_road);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_street_light.isSelected())
				{
					txt_traffic_title.setText("Construction");
					btn_street_light.setSelected(false);
					btn_report_construction.setEnabled(false);
				}
				else{
					txt_traffic_title.setText("Fix Street Light");
					btn_street_light.setSelected(true);
					btn_report_construction.setEnabled(true);
				}
			}
		});
		
		final Button btn_traffic_light = (Button) reportConstructionLayout.findViewById(R.id.button_traffic_light);
		btn_traffic_light.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportConstructionLayout.findViewById(R.id.button_street_light);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportConstructionLayout.findViewById(R.id.button_bumpy_road);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_traffic_light.isSelected())
				{
					txt_traffic_title.setText("Construction");
					btn_traffic_light.setSelected(false);
					btn_report_construction.setEnabled(false);
				}
				else{
					txt_traffic_title.setText("Fix Traffic Light");
					btn_traffic_light.setSelected(true);
					btn_report_construction.setEnabled(true);
				}
			}
		});
		
		final Button btn_bumpy_road = (Button) reportConstructionLayout.findViewById(R.id.button_bumpy_road);
		btn_bumpy_road.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//popupCollision.dismiss();
				Button otherButton1 = (Button)reportConstructionLayout.findViewById(R.id.button_street_light);			
				if (otherButton1.isSelected())
				{
					otherButton1.setSelected(false);
				}
				Button otherButton2 = (Button)reportConstructionLayout.findViewById(R.id.button_traffic_light);
				if (otherButton2.isSelected())
				{
					otherButton2.setSelected(false);
				}

				if (btn_bumpy_road.isSelected())
				{
					txt_traffic_title.setText("Construction");
					btn_bumpy_road.setSelected(false);
					btn_report_construction.setEnabled(false);
				}
				else{
					txt_traffic_title.setText("Fix Road");
					btn_bumpy_road.setSelected(true);
					btn_report_construction.setEnabled(true);
				}
			}
		});
		
		btn_report_construction.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				closePopup();
				setPopUpMenuIsShowing(false);				
				ReportIncidentTask report = new ReportIncidentTask(userId, "incident", lat, lon, currentLocation, txt_traffic_title.getText().toString());
				report.execute();							
				/* Send report in background */
			}
		});
		Button btn_cancel = (Button) reportConstructionLayout.findViewById(R.id.button_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				returnPopUpWhenCancel();
			}
		});
		
	}
	
	public void showCrimeBlockPopUpWindow()
	{
		/* TODO modify animation of popup window */
		RelativeLayout viewGroupCrime = (RelativeLayout) mActivity.findViewById(R.id.layout_report_crime);
		LayoutInflater layoutInflaterCrime = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View reportCrimeLayout = layoutInflaterCrime.inflate(R.layout.popupwindow_crime, viewGroupCrime);
		popupCrime = new PopupWindow(mContext);
		
		popupCrime.setContentView(reportCrimeLayout);
		popupCrime.setBackgroundDrawable(new ColorDrawable(0));
		popupCrime.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popupCrime.showAtLocation(reportCrimeLayout, Gravity.CENTER, 0, 0);
		popupCrime.setOutsideTouchable(false);
		
		TextView location = (TextView) reportCrimeLayout.findViewById(R.id.location);
		location.setText(currentLocation);
		location.postInvalidate();
		TextView time = (TextView) reportCrimeLayout.findViewById(R.id.time);
		String new_minute;
		if(minute < 10) {
			new_minute = "0" + Integer.toString(minute);
		} else {
			new_minute = Integer.toString(minute);
		}
		time.setText(Integer.toString(hour) + ":" + new_minute + (am_pm == 1 ? " pm" : " am"));
		
		btn_report_crime = (Button) reportCrimeLayout.findViewById(R.id.button_send);
		btn_report_crime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				closePopup();
				setPopUpMenuIsShowing(false);
				ReportIncidentTask report = new ReportIncidentTask(userId, "report", lat, lon, currentLocation, "HELP");
				report.execute();
				/* Send report in background */
			}
		});
		Button btn_cancel = (Button) reportCrimeLayout.findViewById(R.id.button_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Close pop up */
				returnPopUpWhenCancel();
			}
		});
	}
	
	public void closePopup() {

		closeEnterLocationView();
		closeMainPopUp();
		
		//Should close all other children as well
		if(popupTraffic != null) {
			popupTraffic.dismiss();
		}
		if(popupCollision != null) {
			popupCollision.dismiss();
		}
		if(popupFlood != null) {
			popupFlood.dismiss();
		}
		
		if(popupSectionBlock!= null){
			popupSectionBlock.dismiss();
		}
		
		if(popupCrime != null){
			popupCrime.dismiss();
		}
		
		if (popupConstruction != null){
			popupConstruction.dismiss();
		}
		
		setPopUpMenuIsShowing(false);
	}
	
	public void returnPopUpWhenCancel(){
		if(popupTraffic != null) {
			popupTraffic.dismiss();
		}
		if(popupCollision != null) {
			popupCollision.dismiss();
		}
		if(popupFlood != null) {
			popupFlood.dismiss();
		}
		
		if(popupSectionBlock!= null){
			popupSectionBlock.dismiss();
		}
		
		if(popupCrime != null){
			popupCrime.dismiss();
		}
		
		if (popupConstruction != null){
			popupConstruction.dismiss();
		}
		
		showPopup();
	}
	
	public void closeMainPopUp(){
		if (popupMenu != null)
		{
			popupMenu.dismiss();
		}		
	}

	public boolean isPopUpMenuIsShowing() {
		return PopUpMenuIsShowing;
	}

	public void setPopUpMenuIsShowing(boolean popUpMenuIsShowing) {
		PopUpMenuIsShowing = popUpMenuIsShowing;
	}
	
	public void closeEnterLocationView(){
		if(viewGroupLocation != null) {
			viewGroupLocation.setVisibility(View.VISIBLE);
		}
	}

	class ReportIncidentTask extends AsyncTask<Void, Void, String> {
		int userId;
		String type;
		double lat;
		double lon;
		String location;
		String desc;
		
		public ReportIncidentTask(int userId, String type, double lat, double lon, String location, String desc) {
			this.userId = userId;
			this.type = type;
			this.lat = lat;
			this.lon = lon;
			this.location = location;
			this.desc = desc;
		}
		
		@Override
		protected String doInBackground(Void... params) {	
			UtilsUI util = new UtilsUI();
			String str;
			JSONWebInterface webInterface = new JSONWebInterface();
			if(util.isNetworkAvailable(mContext)) {
				str = webInterface.sendPostReportRequest(userId, "incident", lat, lon, location, desc, username);
			} else {
				Toast.makeText(mContext, "No network connection", Toast.LENGTH_SHORT).show();
				str = "";
			}
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			//
		}
	}
}