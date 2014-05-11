package com.viaje.main;

import com.viaje.main.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class RotateSwitchIcon extends Activity {

	 private ImageButton button;
	 private float _newAngle, _oldAngle;


	 /**
	  * Called when the activity is first created.
	  */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	     setContentView(R.layout.layout_find_route);
	     button = (ImageButton) findViewById(R.id.button_switch_location);
	     button.setOnClickListener((OnClickListener) this);
	     
	 }

	 public void onClick(View view) 
	 {
		 if (view.getId() == R.id.button_switch_location) 
		 {
	            _newAngle = _oldAngle + 90;

	            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) button.getLayoutParams();
	            int centerX = layoutParams.leftMargin + (button.getWidth()/2);
	            int centerY = layoutParams.topMargin + (button.getHeight()/2);
	            RotateAnimation animation = new RotateAnimation(_oldAngle, _newAngle, centerX, centerY);
	            animation.setDuration(0);
	            animation.setRepeatCount(0);
	            animation.setFillAfter(true);
	            button.startAnimation(animation);

	            _oldAngle = _newAngle;
	        }
	    }

}
