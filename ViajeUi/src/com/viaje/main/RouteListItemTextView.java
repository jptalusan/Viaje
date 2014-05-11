package com.viaje.main;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RouteListItemTextView extends LinearLayout {
	private Context mContext = null;
	private TextView mFieldNameTextView = null;
	private TextView mFieldValueTextView = null;
	
	public RouteListItemTextView(Context c) {
		super(c);
		
		mContext = c;
		mFieldNameTextView = new TextView(mContext);
		mFieldValueTextView = new TextView(mContext);
		
		LayoutParams tmpLayoutParams;
		tmpLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.25f);
		addView(mFieldNameTextView, tmpLayoutParams);
		
		tmpLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.75f);
		addView(mFieldValueTextView, tmpLayoutParams);
		
		this.setWeightSum(1.0f);
	}
	
	public void setFieldNameText(String s) {
		if (mFieldNameTextView == null) {
			System.out.println("mFieldNameTextView does not exist!");
			return;
		}
		mFieldNameTextView.setText(s);
	}
	
	public void setText(String s) {
		if (mFieldValueTextView == null) {
			System.out.println("mFieldValueTextView does not exist!");
			return;
		}
		mFieldValueTextView.setText(s);
		
	}
}
	