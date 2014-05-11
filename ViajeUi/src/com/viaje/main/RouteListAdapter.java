package com.viaje.main;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.atlach.trafficdataloader.TripInfo.Route;
import com.example.otpxmlgetterUI.UtilsUI;
import com.viaje.main.R;

public class RouteListAdapter extends ArrayAdapter<Route> {
	private Context mContext = null;
	private List<Route> routeList = null;
	
	private ImageView mRouteTraffIcon = null;
	private ImageView mRouteTrafficBackground = null;
	private TextView mRouteName = null;
	private TextView mRouteFromLoc = null;
	private TextView mRouteToLoc = null;
	private TextView mRouteTotalDist = null;
	private TextView mRouteTotalCost = null;
	private TextView mRouteTrafficCond = null;

	private SharedPreferences settings; 
	private String from;
	private String to;
	
	public RouteListAdapter(Context context, int textViewResourceId,
			List<Route> routes) {
		super(context, textViewResourceId, routes);

		mContext = context;
		routeList = routes;
	}
	

	@Override
	public int getCount() {
		return this.routeList.size();
	}
	
	@Override
	public Route getItem(int index) {
		if ((index > routeList.size()) &&
			(index < 0)) {
			return null;
		}
		return routeList.get(index);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		settings = mContext.getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
		from = settings.getString("from", "Start");
		to = settings.getString("to", "End");
		
		double distance = 0.0;
		double[] cost = {0.0, 0.0, 0.0, 0.0};
		String modeOfTransit = "WALK";
		String units = "";
		View row = convertView;

		if (row == null)
		{
			// ROW INFLATION
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.custom_route_list_item, parent, false);
		}

		// Get item
		Route routeItem = getItem(position);

		mRouteTraffIcon = (ImageView) row.findViewById(R.id.route_traffic_icon);

		distance = Double.parseDouble(routeItem.dist) < 1000 ? Double.parseDouble(routeItem.dist) : Double.parseDouble(routeItem.dist)/1000;
		cost = routeItem.costMatrix;
		modeOfTransit = routeItem.mode;

		units = Double.parseDouble(routeItem.dist) < 1000 ? " m" : " km";	
		
		mRouteName = (TextView) row.findViewById(R.id.route_name_text);
		mRouteFromLoc = (TextView) row.findViewById(R.id.route_fromLocation_text);
		mRouteToLoc = (TextView) row.findViewById(R.id.route_toLocation_text);
		mRouteTotalDist = (TextView) row.findViewById(R.id.route_totalDist_text);		
		mRouteTotalCost = (TextView) row.findViewById(R.id.route_totalCost_text);
		mRouteTrafficCond = (TextView) row.findViewById(R.id.route_trafficCondition_text);
		
		mRouteName.setText((routeItem.name.equals("") ? "Path" : properCase(routeItem.name)));

	
		if(routeItem.start.equals("road") || 
				routeItem.start.equals("platform") || 
				routeItem.start.equals("service road") || 
				routeItem.start.equals("parking aisle")) {
			routeItem.start = from;
		}
		
		if(routeItem.end.equals("road") || 
				routeItem.end.equals("platform") || 
				routeItem.end.equals("service road") || 
				routeItem.end.equals("parking aisle")) {
			routeItem.end = to;
		}
		
		mRouteFromLoc.setText(routeItem.start);
		
		mRouteToLoc.setText(routeItem.end);
		
		mRouteTotalDist.setText(Double.toString(UtilsUI.round(distance, 2)) + units);
		

			if(cost[2] == 0.0) {
				if(cost[1] == 0.0) {
					mRouteTotalCost.setText("Php" + cost[0]);
				} else {
					mRouteTotalCost.setText("Reg. Php " + cost[0] + "\nDisc. Php " + cost[1]);
				}
			} else {
				mRouteTotalCost.setText("Reg. Php " + cost[0] + "\nDisc. Php " + cost[1] + "\nReg. AC Php " + cost[2] + "\nDisc. AC Php " + cost[3]);
			}

		
		/* 20130928 Graphically represent the traffic conditions on each route */
		mRouteTrafficBackground = (ImageView) row.findViewById(R.id.route_traffic_icon_background);

		/* Graphically represent the traffic conditions on each route */

			if (routeItem.cond.contains("Heavy")) {
				mRouteTrafficBackground.setImageResource(R.drawable.ic_transportation_background_red);
				mRouteTrafficCond.setText("Heavy");
			} else if (routeItem.cond.contains("Medium")) {
				mRouteTrafficBackground.setImageResource(R.drawable.ic_transportation_background_orange);
				mRouteTrafficCond.setText("Medium");
			} else if (routeItem.cond.contains("Light")) {
				mRouteTrafficBackground.setImageResource(R.drawable.ic_transportation_background_green);
				mRouteTrafficCond.setText("Light");
			}
			else
			{
				/* 20130929 - When traffic is unknown, set to default background (blue) */
				mRouteTrafficBackground.setImageResource(R.drawable.ic_transportation_background_blue);
				mRouteTrafficCond.setText(routeItem.cond.equals("") ? "Unknown" : routeItem.cond);
			}
	

		/* 20130928 moved. to draw above background */
		if(modeOfTransit.equals("WALK")) {
			mRouteTraffIcon.setImageResource(R.drawable.ic_transportation_walk);
			mRouteTrafficBackground.setImageResource(R.drawable.ic_transportation_background_blue);
		} else if(modeOfTransit.equals("BUS")) {
			mRouteTraffIcon.setImageResource(R.drawable.ic_transportation_bus);
		} else if(modeOfTransit.equals("JEEP")) {
			mRouteTraffIcon.setImageResource(R.drawable.ic_transportation_jeepney);
		} else {
			mRouteTraffIcon.setImageResource(R.drawable.ic_transportation_train);
			mRouteTrafficBackground.setImageResource(R.drawable.ic_transportation_background_blue);
		}
		return row;
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
