package com.viaje.main;

import java.util.List;
import java.util.Locale;

import com.example.otpxmlgetterUI.UtilsUI;
import com.viaje.main.R;
import com.viaje.webinterface.JSONWebInterface;
import com.viaje.webinterface.JSONWebInterface.ServerStatsElement;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DisplayStatistics extends Fragment
{
	private static double GAS_COST_PER_LITER = 51.00;
	public View rootView;
	private SharedPreferences settings; 
	private RelativeLayout relativeLayout;
	ProgressDialog mDialog;
	private int _userId = -1;
	public static FindRouteFragment newInstance(Context context) 
	{
		FindRouteFragment newActivityForFindRoute = new FindRouteFragment();
		return newActivityForFindRoute;
	}
			 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
		settings = getActivity().getSharedPreferences("ViajePrefs", FragmentActivity.MODE_PRIVATE);
		settings.edit();
	    if (rootView!= null) {
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
	    try {
	    	rootView = (ViewGroup) inflater.inflate(R.layout.layout_statistics, null);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }
		getActivity().getActionBar().setTitle("Statistics");
		
		_userId = settings.getInt("userId", -1);	
		GetRouteStatisticsTask getRouteStats = new GetRouteStatisticsTask(_userId, 6, 10);
		getRouteStats.execute();
		GetSearchStatisticsTask getSearchStats = new GetSearchStatisticsTask(_userId, 5, 10);
		getSearchStats.execute();	

		relativeLayout = (RelativeLayout)rootView.findViewById(R.id.relativeLayout);
		relativeLayout.setVisibility(8);
		return rootView;
	}	 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	class GetRouteStatisticsTask extends AsyncTask<Void, Void, String> {
		int userId;
		int count;
		int daysPast;
		
		public GetRouteStatisticsTask(int userId, int count, int daysPast) {
			this.userId = userId;
			this.count = count;
			this.daysPast = daysPast;
		}
		
		@Override
		protected void onPreExecute() {
	        super.onPreExecute();

	        mDialog = new ProgressDialog(getActivity());
	        mDialog.setMessage("Loading Statistics");
	        mDialog.show();
		}
		
		@Override
		protected String doInBackground(Void... params) {	
			JSONWebInterface webInterface = new JSONWebInterface();
			String str = webInterface.sendGetTopRoutesRequest(userId, count, daysPast);
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			if(!result.equals("")) {
				JSONWebInterface webInterface = new JSONWebInterface();			
				List<ServerStatsElement> extracted_list = webInterface.extractTopStatistics(result);
				extractRouteList(extracted_list);
			}
		}
	}
	
	class GetSearchStatisticsTask extends AsyncTask<Void, Void, String> {
		int userId;
		int count;
		int daysPast;
		
		public GetSearchStatisticsTask(int userId, int count, int daysPast) {
			this.userId = userId;
			this.count = count;
			this.daysPast = daysPast;
		}
		
		@Override
		protected String doInBackground(Void... params) {	
			JSONWebInterface webInterface = new JSONWebInterface();
			String str = webInterface.sendGetTopSearchesRequest(userId, count, daysPast);
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			double taxi_cost;
			if(!result.equals("")) {
				JSONWebInterface webInterface = new JSONWebInterface();			
				List<ServerStatsElement> extracted_list = webInterface.extractTopStatistics(result);
				extractSearchList(extracted_list);
			}
			String temp_cost = settings.getString("totalCost", "0.0");
			String temp_dist = settings.getString("totalDistance", "0.0");
			if(temp_dist.equals("0.0")) {
				taxi_cost = 0.0;
			} else {
				taxi_cost = 40 + 3.50 * ((Double.parseDouble(temp_dist) - 1000) / 250);
			}
			
			double car_cost = ((Double.parseDouble(temp_dist))/13420)*GAS_COST_PER_LITER;
			TextView totalTaxicost = (TextView)rootView.findViewById(R.id.taxiTotalCost);
			TextView totalCarcost = (TextView)rootView.findViewById(R.id.carTotalCost);
			TextView totaldist = (TextView)rootView.findViewById(R.id.totalDistance);
			totalTaxicost.setText("Php " + Double.toString(UtilsUI.round(taxi_cost - Double.parseDouble(temp_cost), 2)));
			totalCarcost.setText("Php " + Double.toString(UtilsUI.round(car_cost - Double.parseDouble(temp_cost), 2)));
			double dist = Double.parseDouble(temp_dist);
			totaldist.setText(Double.toString(UtilsUI.round(dist/1000, 2)) + " km");
			mDialog.dismiss();
			relativeLayout.setVisibility(0);
		}
	}
	
	private void extractRouteList(List<ServerStatsElement> extracted_list) {
		String[] extracts = {"", "", "", "", "", ""};
		int idx = 0;
		String index;
		for(int i = 0; i < extracted_list.size(); i++) {
			//extracts[i] = extracted_list.get(i).count + " : " + properCase(extracted_list.get(i).value);
			index = String.valueOf(idx);
			extracts[i] = "TOP " + index + " " + properCase(extracted_list.get(i).value);
			System.out.println("Extract: " + extracted_list.get(i).value);
			System.out.println("Extract: " + extracted_list.get(i).count);
			idx++;
		}
		TextView top1 = (TextView)rootView.findViewById(R.id.top1Route);
		TextView top2 = (TextView)rootView.findViewById(R.id.top2Route);
		TextView top3 = (TextView)rootView.findViewById(R.id.top3Route);
		TextView top4 = (TextView)rootView.findViewById(R.id.top4Route);
		TextView top5 = (TextView)rootView.findViewById(R.id.top5Route);
		top1.setText(extracts[1]);
		top2.setText(extracts[2]);
		top3.setText(extracts[3]);
		top4.setText(extracts[4]);
		top5.setText(extracts[5]);
	}
	
	private void extractSearchList(List<ServerStatsElement> extracted_list) {
		String[] extracts = {"", "", "", "", ""};
		int idx = 1;
		String index;
		for(int i = 0; i < extracted_list.size(); i++) {
			//extracts[i] = extracted_list.get(i).count + " : " + properCase(extracted_list.get(i).value);
			index = String.valueOf(idx);
			extracts[i] = "TOP " + index + " " + properCase(extracted_list.get(i).value);
			System.out.println("Extract: " + extracted_list.get(i).value);
			System.out.println("Extract: " + extracted_list.get(i).count);
			idx++;
		}
		TextView top1 = (TextView)rootView.findViewById(R.id.top1Search);
		TextView top2 = (TextView)rootView.findViewById(R.id.top2Search);
		TextView top3 = (TextView)rootView.findViewById(R.id.top3Search);
		TextView top4 = (TextView)rootView.findViewById(R.id.top4Search);
		TextView top5 = (TextView)rootView.findViewById(R.id.top5Search);
		top1.setText(extracts[0]);
		top2.setText(extracts[1]);
		top3.setText(extracts[2]);
		top4.setText(extracts[3]);
		top5.setText(extracts[4]);
	}
	
	String properCase (String inputVal) {
	    // Empty strings should be returned as-is.
	    if (inputVal.length() == 0) return "";
	    // Strings with only one character uppercased.
	    if (inputVal.length() == 1) return inputVal.toUpperCase(Locale.ENGLISH);
	    // Otherwise uppercase first letter, lowercase the rest.
	    return inputVal.substring(0,1).toUpperCase(Locale.ENGLISH)
	        + inputVal.substring(1).toLowerCase(Locale.ENGLISH);
	}
}