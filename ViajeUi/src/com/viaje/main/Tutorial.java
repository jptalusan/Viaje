package com.viaje.main;

import com.viaje.main.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

public class Tutorial extends FragmentActivity {
	private static boolean isFromDrawer = false;
	private ViewPager myPager;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent intent = getIntent();
		isFromDrawer = intent.getBooleanExtra(UiMainActivity.FROM_DRAWER, false);
		MyPagerAdapter adapter = new MyPagerAdapter();
		myPager = (ViewPager) findViewById(R.id.myfivepanelpager);
		myPager.setAdapter(adapter);
		myPager.setCurrentItem(0);		
	}

	public void ButtonCalls(View v)
	{
		switch(v.getId()) {
		case R.id.done:
		case R.id.skip:
			Intent intent = new Intent (this, UiMainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			finish();
			break;
		case R.id.next:
			myPager.setCurrentItem(1);
			break;
		case R.id.again:
			myPager.setCurrentItem(1);
			break;
		}
	}
    
    @Override
    public void onBackPressed() {
    	if(isFromDrawer) {
    		super.onBackPressed();
    	} else {
    		Intent intent = new Intent (this, UiMainActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		this.startActivity(intent);
    		finish();
    	}
    }
    
	private static class MyPagerAdapter extends PagerAdapter {

		public int getCount() {
			if(isFromDrawer) {
				return 6;
			} else {
				return 7;				
			}
		}

		public Object instantiateItem(View collection, int position) {

			LayoutInflater inflater = (LayoutInflater) collection.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			int resId = 0;
			if(isFromDrawer) {
				switch (position) {
				case 0:
					resId = R.layout.tutoriala;
					break;
				case 1:
					resId = R.layout.tutorialb;
					break;
				case 2:
					resId = R.layout.tutorialc;
					break;
				case 3:
					resId = R.layout.tutoriald;
					break;
				case 4:
					resId = R.layout.tutoriale;
					break;
				case 5:
					resId = R.layout.tutorialf;
					break;
				default:
					break;
				}
			} else {
				switch (position) {
				case 0:
					resId = R.layout.tutorialaa;
					break;
				case 1:
					resId = R.layout.tutoriala;
					break;
				case 2:
					resId = R.layout.tutorialb;
					break;
				case 3:
					resId = R.layout.tutorialc;
					break;
				case 4:
					resId = R.layout.tutoriald;
					break;
				case 5:
					resId = R.layout.tutoriale;
					break;
				case 6:
					resId = R.layout.tutorialf;
					break;
				default:
					break;
				}
			}
			View view = inflater.inflate(resId, null);

			((ViewPager) collection).addView(view, 0);

			return view;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);

		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == ((View) arg1);

		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}

	}

}