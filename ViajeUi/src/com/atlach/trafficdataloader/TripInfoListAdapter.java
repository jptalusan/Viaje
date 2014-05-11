package com.atlach.trafficdataloader;

import java.util.ArrayList;
import java.util.Iterator;

import com.atlach.trafficdataloader.TripInfo.Trip;
import com.atlach.trafficdataloader.TripInfo.Route;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class TripInfoListAdapter extends BaseExpandableListAdapter {
	private Context mContext  = null;
	private TripInfo mTripInfo = null;
	private ArrayList<TripInfoListElement> mChildViews = null;
	private ArrayList<TextView> mGroupViews = null;

	public TripInfoListAdapter(Context c, TripInfo tripInfo) {
		mContext = c;
		mTripInfo = tripInfo;
		mChildViews = new ArrayList<TripInfoListElement>();
		mGroupViews = new ArrayList<TextView>();
		
		/* Derive the list elements */
		deriveElementList(tripInfo);
	}
	
	public void updateElements(TripInfo tripInfo) {
		if (tripInfo == null) {
			return;
		}
		
		/* Derive the list elements */
		deriveElementList(tripInfo);
	}
	
	@Override
	public Object getChild(int groupPos, int childPos) {
		return mTripInfo.trips.get(groupPos).routes.get(childPos);
	}

	@Override
	public long getChildId(int groupPos, int childPos) {
		if (mTripInfo.trips.get(groupPos).routes.size() < childPos) {
			return -1;
		}
		return childPos;
	}

	@Override
	public View getChildView(int groupPos, int childPos, boolean isLastChild, View convertView,
			ViewGroup parent) {
		if (mChildViews == null){
			return convertView;
		}
		
		/* Locate the matching child */
		Iterator<TripInfoListElement> iter = mChildViews.iterator();
		TripInfoListElement tmpElem = null;
		
		while(iter.hasNext()) {
			tmpElem = iter.next();
			
			if ((tmpElem.groupId == groupPos) && (tmpElem.childId == childPos)){
				return tmpElem;
			}
		}
		
		if (mChildViews.get(childPos) != null) {
			return mChildViews.get(childPos);
		}
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPos) {
		return mTripInfo.trips.get(groupPos).routes.size();
	}

	@Override
	public Object getGroup(int groupPos) {
		return mTripInfo.trips.get(groupPos);
	}

	@Override
	public int getGroupCount() {
		return mTripInfo.trips.size();
	}

	@Override
	public long getGroupId(int groupPos) {
		if (mTripInfo.trips.size() < groupPos) {
			return -1;
		}
		return groupPos;
	}

	@Override
	public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent) {
		if (mGroupViews == null){
			return convertView;
		}
		
		if (mGroupViews.get(groupPos) != null) {
			return mGroupViews.get(groupPos);
		}
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPos, int childPos) {
		return false;
	}

	private void deriveElementList(TripInfo tripInfo) {
		mChildViews = new ArrayList<TripInfoListElement>();
		mGroupViews = new ArrayList<TextView>();

		/* List of elements should make use of the leg and itinerary parts of the response only */
		/*	- Each itinerary should correspond to a "group" */
		/*	- Each leg should correspond to a "child" */
		
		Iterator<Trip> tripIter = tripInfo.trips.iterator();
		Iterator<Route> routeIter = null;
		Trip tmpTrip = null;
		Route tmpRoute = null;
		TextView tmpTextView = null;
		int tripIdx = 0;
		int routeIdx = 0;
		
		/* Loop through each Itinerary */
		while(tripIter.hasNext()) {
			float longestDist = 0;
			String longestLeg = "";
			tmpTrip = tripIter.next();
			
			routeIter = tmpTrip.routes.iterator();
			/* Loop through each Leg of the Itinerary and use that as basis for the list element content */
			while (routeIter.hasNext()) {
				tmpRoute = routeIter.next();
				
				if (Float.parseFloat(tmpRoute.dist) > longestDist){
					longestDist = Float.parseFloat(tmpRoute.dist);
					longestLeg = tmpRoute.name;
				}
				
				TripInfoListElement tmpElemTextView = new TripInfoListElement(mContext);
				tmpElemTextView.setText(tmpRoute.name  + " / " + tmpRoute.mode + " : " + tmpRoute.cond);
				tmpElemTextView.setPadding(30, 4, 0, 4);
				
				if (tmpRoute.cond.contains("Heavy")) {
					tmpElemTextView.setBackgroundColor(Color.RED);
				} else if (tmpRoute.cond.contains("Medium")) {
					tmpElemTextView.setBackgroundColor(Color.YELLOW);
				} else if (tmpRoute.cond.contains("Light")) {
					tmpElemTextView.setBackgroundColor(Color.GREEN);
				}
				
				tmpElemTextView.childId = routeIdx;
				tmpElemTextView.groupId = tripIdx;

				
				/* Finally, add the element to the element list */
				mChildViews.add(tmpElemTextView);
				
				routeIdx++;
			}

			/* Create Text View element for this itinerary */
			tmpTextView = new TextView(mContext);
			
			tmpTextView.setText("Route using " + longestLeg);
			tmpTextView.setTextSize(24);
			tmpTextView.setTypeface(Typeface.DEFAULT_BOLD);
			tmpTextView.setPadding(30, 4, 0, 4);
			
			mGroupViews.add(tmpTextView);
			
			
			routeIdx = 0;
			tripIdx++;
		}
		
		return;
	}
}
