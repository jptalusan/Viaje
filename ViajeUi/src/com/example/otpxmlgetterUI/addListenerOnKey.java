package com.example.otpxmlgetterUI;

import com.viaje.main.R;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

//button.setOnClickListener(new addListenerOnKey(this, appTypeInt, isPresent, playId));
public class addListenerOnKey implements OnKeyListener {

	private AutoCompleteTextView mAutoCompleteTextView;
	private AutoCompleteTextView autocompleterStart;
	private AutoCompleteTextView autocompleterDest;
	private Context mContext;

	public addListenerOnKey (Context context, AutoCompleteTextView autoCompleteTextView) {
		this.mAutoCompleteTextView = autoCompleteTextView;
		this.mContext = context;
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		final InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		autocompleterStart = (AutoCompleteTextView)	((Activity) mContext).findViewById(R.id.message_start_location);
		autocompleterDest = (AutoCompleteTextView)	((Activity) mContext).findViewById(R.id.message_end_location);
        // If the event is a key-down event on the "enter" button
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
             (keyCode == KeyEvent.KEYCODE_ENTER))
        {
        	if (mAutoCompleteTextView == autocompleterStart) {
          	  	autocompleterStart.clearFocus();
                autocompleterDest.requestFocus();
                return true;
        	} else if (mAutoCompleteTextView == autocompleterDest) {
        	  	autocompleterDest.clearFocus();
        	  	imm.hideSoftInputFromWindow(autocompleterDest.getWindowToken(), 0);
        	}
        }
        return false;
	}
}
