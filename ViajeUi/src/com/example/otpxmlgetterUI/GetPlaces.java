package com.example.otpxmlgetterUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.viaje.main.FindRouteFragment;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

class GetPlaces extends AsyncTask<String, Void, ArrayList<String>>
{
	public ArrayAdapter<String> adapter = null;
	public AutoCompleteTextView mAutoComplete;
	public Context mContext;
	//private double coords[] = {0.0, 0.0};
	private FindRouteFragment mFindRouteActivity;
	
	public GetPlaces(AutoCompleteTextView autocomplete, Context context, FindRouteFragment findRouteActivity) {
		this.mAutoComplete = autocomplete;
		this.mContext = context;
		this.mFindRouteActivity = findRouteActivity;
	}
	@Override
	// three dots is java for an array of strings
	protected ArrayList<String> doInBackground(String... args)
	{
		//Log.d("gottaGo", "doInBackground");
		ArrayList<String> predictionsArr = new ArrayList<String>();
	
		try
		{
			URL googlePlaces = new URL(
			// URLEncoder.encode(url,"UTF-8");
			//"https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+ URLEncoder.encode(args[0].toString(), "UTF-8") +"&types=geocode&language=en&location=14.60905,121.02226&radius=100&sensor=true&key=AIzaSyAf2MyIT-XjujSiq3WCok7JW3NADBoMoqc");
			/* Added location and radius parameter to restrict */
			"https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+ URLEncoder.encode(args[0].toString(), "UTF-8") +"&types=geocode&language=en&components=country:PH&bounds=14.60905,121.02226&radius=100&sensor=true&key=AIzaSyAf2MyIT-XjujSiq3WCok7JW3NADBoMoqc");
			URLConnection tc = googlePlaces.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
			tc.getInputStream()));
			
			String line;
			StringBuffer sb = new StringBuffer();
			//take Google's legible JSON and turn it into one big string.
			while ((line = in.readLine()) != null) {
			sb.append(line);
			}
			//turn that string into a JSON object
			JSONObject predictions = new JSONObject(sb.toString());	
			//now get the JSON array that's inside that object            
			JSONArray ja = new JSONArray(predictions.getString("predictions"));
			
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo = (JSONObject) ja.get(i);
				//add each entry to our array
		        String oldStr = jo.getString("description");
		        String mm = ", Metro Manila";
		        String ph = ", Philippines";

		        oldStr = oldStr.replace(mm, "");
		        oldStr = oldStr.replace(ph, "");
				predictionsArr.add(oldStr);
			}
		} catch (IOException e)
		{
			Log.e("YourApp", "GetPlaces : doInBackground", e);
		} catch (JSONException e)
		{
			Log.e("YourApp", "GetPlaces : doInBackground", e);
		}
		return predictionsArr;
	}
	
	@Override
	protected void onPostExecute(ArrayList<String> result)
	{
		//Log.d("YourApp", "onPostExecute : " + result.size());
		//update the adapter
		adapter = new ArrayAdapter<String>(mContext, com.viaje.main.R.layout.item_list);
		adapter.setNotifyOnChange(true);
		//attach the adapter to textview
		mAutoComplete.setAdapter(adapter);
		for (String string : result)
		{
			//Log.d("YourApp", "onPostExecute : result = " + string);
			adapter.add(string);
			adapter.notifyDataSetChanged();
		}
		if(result.size() > 0)
		{
			mFindRouteActivity.mapUpdate(mAutoComplete);
		}


		mFindRouteActivity.hideOrShowLayout();
		//Log.d("YourApp", "onPostExecute : autoCompleteAdapter" + adapter.getCount());
	}
}
