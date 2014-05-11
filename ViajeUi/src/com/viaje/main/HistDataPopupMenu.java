package com.viaje.main;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

public class HistDataPopupMenu implements OnItemSelectedListener, OnClickListener {
	private Context mContext;
	private Activity mActivity;
	private PopupWindow mPopupWindow;
	private HistDataPopupEvent mPopupEvent;
	
	private String mSelectedDay = "Sunday";
	private String mSelectedTime = "12:00 AM";
	private String mSelectedWeather = "Clear";
	
	public static final int STATUS_OK = 0;
	public static final int STATUS_FAILED = -1; 
	
	public HistDataPopupMenu(Context context, Activity activity, HistDataPopupEvent e) {
		this.mContext = context;
		this.mActivity = activity;
		this.mPopupEvent = e;
	}

	public void showPopup() {
		ViewGroup vg = (LinearLayout) mActivity.findViewById(R.id.histdata_opts_popup);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View histDataPopupLayout = inflater.inflate(R.layout.layout_histdata_opts_popup, vg);

		mPopupWindow = new PopupWindow(mContext);
		mPopupWindow.setContentView(histDataPopupLayout);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
		mPopupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    //mPopupWindow.setFocusable(true); /* When this is set to TRUE, popup window is dismissed when user clicks outside window */
		mPopupWindow.showAtLocation(histDataPopupLayout, Gravity.CENTER, 0, 0);
		mPopupWindow.setOutsideTouchable(false);

		/* Set listeners for the spinners */
		int spinnerId[] = { R.id.daySpinner, R.id.timeSpinner, R.id.weatherSpinner };
		int spinArrayId[] = { R.array.day_of_wk_array, R.array.time_of_day_array, R.array.weather_array };
		for (int i = 0; i < spinnerId.length; i++) {
			Spinner spinner = (Spinner) histDataPopupLayout.findViewById(spinnerId[i]);
			
			if (spinner != null) {
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext.getApplicationContext(), 
																					 spinArrayId[i], 
																					 R.layout.spinner_item);
				spinner.setAdapter(adapter);
				spinner.setOnItemSelectedListener(this);
			}
		}
		
		/* Set listeners for the buttons */
		int buttonId[] = {R.id.histdata_ok, R.id.histdata_cancel};
		LinearLayout btnPanel = (LinearLayout) histDataPopupLayout.findViewById(R.id.histdata_opts_btn_panel);
		for (int i = 0; i < buttonId.length; i++) {
			Button button = (Button) btnPanel.findViewById(buttonId[i]);
			
			if (button != null) {
				button.setOnClickListener(this);
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		switch(parent.getId()) {
			case R.id.daySpinner:
				mSelectedDay = parent.getItemAtPosition(pos).toString();
				break;
			case R.id.timeSpinner:
				mSelectedTime = parent.getItemAtPosition(pos).toString();
				break;
			case R.id.weatherSpinner:
				mSelectedWeather = parent.getItemAtPosition(pos).toString();
				break;
			default:
				break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	private int getIntDayOfWeek(String str) {
		final String dayOfWeekStr[] = { "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday" };
		final String compStr = str.toLowerCase(Locale.ENGLISH).trim();
		
		int dayIndex = 0;
		for (dayIndex = 0; dayIndex < dayOfWeekStr.length; dayIndex++) {
			if (compStr.equals(dayOfWeekStr[dayIndex])) {
				break;
			}
		}
		
		if (dayIndex >= dayOfWeekStr.length) {
			TimeZone tz = TimeZone.getTimeZone("GMT+8");
			int today = Calendar.getInstance(tz).get(Calendar.DAY_OF_WEEK);
			
			return today;	// Default to "today"
		}
		
		return dayIndex;
	}
	
	private int getIntTimeOfDay(String str) {
		String initStr = str.trim();
		String divStr[] = initStr.split(":| ");
		
		int time = -1;

		int hour = Integer.parseInt(divStr[0]);
		
		if (divStr[2].contains("PM")) {
			time = (hour + 12) * 100;
		} else if (divStr[0].equals("12")) {
			time = 0;
		} else {
			time = hour * 100;
		}
		
		if (time < 0) {
			TimeZone tz = TimeZone.getTimeZone("GMT+8");
			int currentHour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY);
			
			return (currentHour * 100); // Default to "this hour"
		}
		
		return time;
	}
	
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.histdata_ok) {
			//Add settings here if user wants to automate historical data
			mPopupEvent.onHistDataOptsFinalized(STATUS_OK, 
												getIntDayOfWeek(mSelectedDay), 
												getIntTimeOfDay(mSelectedTime), 
												mSelectedWeather);
		} else if (v.getId() == R.id.histdata_cancel) {
			mPopupEvent.onHistDataOptsFinalized(STATUS_FAILED, STATUS_FAILED, STATUS_FAILED, null);
			closePopup();
		}
	}

	public void closePopup() {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}
}
