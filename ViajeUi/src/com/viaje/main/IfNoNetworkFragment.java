package com.viaje.main;

import com.viaje.main.R;

import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class IfNoNetworkFragment extends Fragment
{
	private View rootView;
	
	public static FindRouteFragment newInstance(Context context) 
	{
		FindRouteFragment newActivityForFindRoute = new FindRouteFragment();
		return newActivityForFindRoute;
	}
			 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
	    if (rootView!= null) {
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
	    try {
	    	rootView = (ViewGroup) inflater.inflate(R.layout.layout_ifnonetwork, null);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }
		getActivity().getActionBar().setTitle("Viaje");
		ImageButton refresh = (ImageButton)rootView.findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((UiMainActivity) getActivity()).selectItem(1);
			}
		});
		return rootView;
	}	 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
 
}