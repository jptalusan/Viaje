package com.example.otpxmlgetterUI;

import com.viaje.main.FindRouteFragment;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

//usage :button.setOnClickListener(new addListenerOnTextChange(this, appTypeInt, isPresent, playId));
public class addListenerOnTextChange implements TextWatcher {
	private Context mContext;
	private ArrayAdapter<String> mAdapter;
	FindRouteFragment mFindRouteActivity;
	AutoCompleteTextView mAutoCompleteTextView;

	public addListenerOnTextChange(Context context, AutoCompleteTextView autoCompleteTextView, ArrayAdapter<String> adapter, 
			FindRouteFragment findRouteActivity) {
    	super();
    	this.mContext = context;
    	this.mAutoCompleteTextView = autoCompleteTextView;
    	this.mAdapter = adapter;
    	this.mFindRouteActivity = findRouteActivity;
    	
    }

	@Override
	public void afterTextChanged(Editable s) {	
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (count%3 == 1) {
			mAdapter.clear();
			GetPlaces task = new GetPlaces(mAutoCompleteTextView, mContext, mFindRouteActivity);
			task.execute(mAutoCompleteTextView.getText().toString());
		}

	}
}
