package com.viaje.main;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.atlach.trafficdataloader.TripInfo.Route;
import com.example.otpxmlgetterUI.UtilsUI;
import com.viaje.main.R;
import com.viaje.webinterface.JSONWebInterface;
 
/**
 * @author mwho
 *
 */
public class Tab1Fragment extends ListFragment implements OnItemClickListener, OnClickListener {
	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
    private RouteListAdapter routeListAdapter = null;
    private ArrayList<Route> routeList = null;
    private Bundle argumentsBundle = null;
	private String totalDist = "";
	private String totalCost = "";
	private String totalTraffic = "";
	private View mView = null;
	private SharedPreferences settings; 
	private SharedPreferences.Editor prefEditor;
	private double dist; 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}
		
		mView = inflater.inflate(R.layout.tab_frag1_layout, container, false);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		argumentsBundle = getArguments();

		if (argumentsBundle == null) {
			return;
		}

		/* Get Parcelable from Bundle */
		// routeList =
		// argumentsBundle.getParcelableArrayList("tab1_route_list");
		ArrayList<RouteParcelData> dataList = argumentsBundle
				.getParcelableArrayList("tab_route_list");
		routeList = new ArrayList<Route>();

		if (dataList == null) {
			return;
		}

		for (int i = 0; i < dataList.size(); i++) {
			RouteParcelData data = dataList.get(i);
			Route route = new Route();

			route.name = data.name;
			route.mode = data.mode;
			route.dist = data.dist;
			route.agency = data.agency;
			route.start = data.start;
			route.end = data.end;
			route.points = data.points;
			route.cond = data.cond;
			route.costMatrix = data.costMatrix;

			routeList.add(route);
		}

		if (routeList == null) {
			return;
		}
		dist = argumentsBundle.getDouble("tab_trip_dist");
		String units = dist < 1000 ? " m" : " km";
		double rounded = UtilsUI.round(dist, 2);
		totalDist = Double.toString((dist < 1000) ? rounded : (UtilsUI.round(rounded/1000, 2))) + units;
		System.out.println(rounded);
		totalCost = Double.toString(argumentsBundle.getDouble("tab_trip_cost"));
		totalTraffic = Integer.toString(argumentsBundle.getInt("tab_trip_cond"));

		routeListAdapter = new RouteListAdapter(getActivity(),
				R.layout.custom_route_list_item, routeList);
		setListAdapter(routeListAdapter);
		getListView().setOnItemClickListener(this);
		
		if (mView == null) {
			Toast.makeText(this.getActivity(), "FAIL.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		TextView condTextView = (TextView) mView.findViewById(R.id.traff_cond_text);
		TextView distTextView = (TextView) mView.findViewById(R.id.total_dist_text);
		TextView costTextView = (TextView) mView.findViewById(R.id.total_cost_text);
		
		if (distTextView != null)
			distTextView.setText(totalDist);
		
		if (costTextView != null)
			costTextView.setText(totalCost);
		
		if (condTextView != null)
			condTextView.setText(totalTraffic);
		
		ImageButton favorite = (ImageButton) mView.findViewById(R.id.favorite);
		favorite.setOnClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Add update function for route in map
		System.out.println("Clicked!");
		UtilsUI util = new UtilsUI();
		String mode = util.knowTransportationOnly(routeList.get(arg2).agency, 
				routeList.get(arg2).name, 
				routeList.get(arg2).mode);
		((FindRouteResultActivity) getActivity()).updateMap(routeList.get(arg2).points, mode);
	}

	public static class RouteParcelCreator implements
			Parcelable.Creator<RouteParcelData> {
		@Override
		public RouteParcelData createFromParcel(Parcel source) {
			return new RouteParcelData(source);
		}

		@Override
		public RouteParcelData[] newArray(int size) {
			return new RouteParcelData[size];
		}

	}

	public static class RouteParcelData implements Parcelable {
		private String name = "";
		private String mode = "";
		private String dist = "";
		private String agency = "";
		private String start = "";
		private String end = "";
		private String points = "";
		private String cond = "";
		private double[] costMatrix = {0.0, 0.0, 0.0, 0.0};

		/* Constructor */
		/* This puts the data back together? */
		public RouteParcelData(Parcel source) {
			name = source.readString();
			mode = source.readString();
			dist = source.readString();
			agency = source.readString();
			start = source.readString();
			end = source.readString();
			points = source.readString();
			cond = source.readString();

			source.readDoubleArray(costMatrix);
		}

		public RouteParcelData(Route route) {
			name = route.name;
			mode = route.mode;
			dist = route.dist;
			agency = route.agency;
			start = route.start;
			end = route.end;
			points = route.points;
			cond = route.cond;
			costMatrix = route.costMatrix;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(name);
			dest.writeString(mode);
			dest.writeString(dist);
			dest.writeString(agency);
			dest.writeString(start);
			dest.writeString(end);
			dest.writeString(points);
			dest.writeString(cond);
			dest.writeDoubleArray(costMatrix);
		}

	}

	@Override
	public void onClick(View v) {
		UtilsUI util = new UtilsUI();
		switch(v.getId()) {
		case(R.id.favorite):
			settings = getActivity().getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
			int userId = settings.getInt("userId", -1);
			if(util.isNetworkAvailable(getActivity())) {
				StoreFavoriteRouteTask storeFavoriteRoute = new StoreFavoriteRouteTask(userId, routeList);
				storeFavoriteRoute.execute();
				Toast.makeText(getActivity(), "Added to Favorites", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), "No network connection", Toast.LENGTH_SHORT).show();
			}			
			break;
		}
	}
	
	class StoreFavoriteRouteTask extends AsyncTask<Void, Void, String> {
		int userId;
		List<Route> routeList;
		
		public StoreFavoriteRouteTask(int userId, List<Route> routeList) {
			this.userId = userId;
			this.routeList = routeList;
		}
		
		@Override
		protected String doInBackground(Void... params) {	
			JSONWebInterface webInterface = new JSONWebInterface();
			settings = getActivity().getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);		
			String temp_cost = settings.getString("totalCost", "0.0");
			String temp_dist = settings.getString("totalDistance", "0.0");
			String temp_traf = settings.getString("totalTraffic", "0.0");
			double d_cost = Double.parseDouble(temp_cost);
			double d_dist = Double.parseDouble(temp_dist);
			double d_traf = Double.parseDouble(temp_traf);
			d_cost = d_cost + Double.parseDouble(totalCost);
			d_dist = d_dist + dist;
			d_traf = d_traf + Double.parseDouble(totalTraffic);
	        prefEditor = settings.edit();
	        prefEditor.putString("totalDistance", Double.toString(d_dist));
	        prefEditor.putString("totalCost", Double.toString(d_cost));
	        prefEditor.putString("totalTraffic", Double.toString(d_traf));
	        prefEditor.commit();
			String str = webInterface.sendSubmitRoutesRequest(userId, routeList);
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			//
		}
	}
}