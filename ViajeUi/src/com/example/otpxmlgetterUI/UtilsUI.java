package com.example.otpxmlgetterUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.atlach.trafficdataloader.TripInfo.Route;
import com.google.android.gms.maps.model.LatLng;
import com.viaje.main.FindRouteFragment;
import com.viaje.main.FindRouteResultActivity;

//Class for various utility methods used in application
public class UtilsUI {
	private boolean isTranspoKnown = false;
	
	public final static String F_LAT = "flat";
	public final static String F_LONG = "flong";
	public final static String T_LAT = "tlat";
	public final static String T_LONG = "tlong";
	public final static String S_NAME = "sname";
	public final static String START = "sLoc";
	public final static String END = "eLoc";
	public final static String CRITERIA = "criteria";
	
	public final static String EXTRA_CONVERTEDJSONFILE = "com.example.otpxmlgetter.CONVERTEDJSONFILE";
	public final static String EXTRA_FROM = "com.example.otpxmlgetter.FROM";
	public final static String EXTRA_TO = "com.example.otpxmlgetter.TO";	
	
	public AlertDialog alert;
	public String modeOfTransit = "WALK";
	public boolean checkIfEmptyString(String string) {

		if (string.matches("")) {
		    return true;
		} else {
			return false;
		}
	}
	
	public File[] getFileList(String path) {
		Log.d("Files", "Path: " + path);
		File f = new File(path);        
		File file[] = f.listFiles();
		Log.d("Files", "Size: "+ file.length);
		for (int i=0; i < file.length; i++)
		{
		    Log.d("Files", "FileName:" + file[i].getName());
		}
		return file;
	}
	
	public boolean isNetworkAvailable(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting(); 
	}	
	
	public void showWarning(boolean isNetworkAvailable, Context context){
		Log.e("Utils", "isNetworkAvailable: " + isNetworkAvailable);
		if(!isNetworkAvailable) {
//            Button planTrip = (Button) ((Activity) context).findViewById(R.id.planTrip);
//            planTrip.setEnabled(false);
//            Button getCurrent = (Button) ((Activity) context).findViewById(R.id.getCurrentLocation);
//            getCurrent.setEnabled(false);
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
            
            dlgAlert.setMessage("You need to be connected to the net in order to plan a trip, but you can still view the Saved Routes.");
            dlgAlert.setTitle("Viaje Warning");
            dlgAlert.setPositiveButton("Ok",
        	    new DialogInterface.OnClickListener() {
        	        @Override
					public void onClick(DialogInterface dialog, int which) {
        	          //dismiss the dialog
        	        	alert.dismiss();
        	        }
        	    });
            	dlgAlert.show();
        		alert = dlgAlert.create();
		}
	}
	
	public void planTripUtil(Context context, String sStart, String sDestination, 
			double[] start, double[] end, LatLng coords, String Criteria) throws IOException {		
		double sFromLat = 0.0;
		double sFromLong = 0.0;
		double sToLat = 0.0;
		double sToLong = 0.0;
		if(sStart.equals(FindRouteFragment.CURRENT_LOCATION)) {
			start[0] = coords.latitude;
			start[1] = coords.longitude;
		} else if (sDestination.equals(FindRouteFragment.CURRENT_LOCATION)) {
			end[0] = coords.latitude;
			end[1] = coords.longitude;		
		}
		
		sFromLat = start[0];
		sFromLong = start[1];
		sToLat = end[0];
		sToLong = end[1];
		
		if (start[0] != 0.0 && end[0] != 0.0) {
			Intent intent = new Intent (context, FindRouteResultActivity.class);
			intent.putExtra(START, sStart);
			intent.putExtra(END, sDestination);
			intent.putExtra(F_LAT, Double.toString(sFromLat));
			intent.putExtra(F_LONG, Double.toString(sFromLong));
			intent.putExtra(T_LAT, Double.toString(sToLat));
			intent.putExtra(T_LONG, Double.toString(sToLong));
			intent.putExtra(CRITERIA, Criteria);
			context.startActivity(intent);
		} else {
			Toast.makeText(context, "Enter both locations by choosing from dropdown menu!", Toast.LENGTH_SHORT).show();	
		}
	}
	
	public String getCurrentLocationUtil(Context context) throws IOException {
		GPSTrackerUI mGPS = new GPSTrackerUI(context);
		boolean getLoc = mGPS.canGetLocation;
		double mLat=mGPS.getLatitude();
		double mLong=mGPS.getLongitude();
		String currLoc;
		ReverseGeocodeUI revGC = new ReverseGeocodeUI(context);
		if(getLoc) {
			currLoc = revGC.getStringFromCoordinates(mLat, mLong);
			//stringHolder.setText(currLoc, TextView.BufferType.EDITABLE);
			return currLoc;
		}else{
			Toast.makeText(context, "can't get the location!", Toast.LENGTH_SHORT).show();
		}
		return "";
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
	    return bd.doubleValue();
	}
	
	public double[] calculateFare(String startRail, String endRail, int modeOfTransit) {
		double[] fare = {0.0, 0.0, 0.0, 0.0};
		int indexStart = 0;
		int indexEnd = 0;
		int indexSum = 0;
		List<String> LRT1 = Arrays.asList(
				"Roosevelt LRT",
				"LRT Balintawak",
				"Monumento LRT",
				"5th Ave LRT",
				"R. Papa LRT",
				"Abad Santos LRT",
				"Blumentritt LRT",
				"Tayuman LRT",
				"Bambang LRT",
				"Doroteo Jose LRT",
				"Carriedo LRT",
				"Central Terminal LRT",
				"UN Ave LRT",
				"Pedro Gil LRT",
				"Quirino Ave LRT",
				"Vito Cruz LRT",
				"Gil Puyat LRT",
				"Libertad LRT",
				"EDSA LRT",
				"Baclaran LRT"
		);
		List<String> LRT2 = Arrays.asList(
				"Santolan LRT",
				"Katipunan LRT",
				"Anonas LRT",
				"Cubao LRT",
				"Betty Go Belmonte LRT",
				"Gilmore LRT",
				"J. Ruiz LRT",
				"V. Mapa LRT",
				"Pureza LRT",
				"Legarda LRT",
				"Recto LRT"
		);
		List<String> MRT3 = Arrays.asList(
				"North Avenue MRT",
				"Quezon MRT",
				"Kamuning MRT",
				"Cubao MRT",
				"Santolan MRT",
				"Ortigas MRT",
				"Shaw MRT",
				"Boni MRT",
				"Guadalupe MRT",
				"Buendia MRT",
				"Ayala MRT",
				"Magellanes MRT",
				"Taft Ave MRT"
		);
		switch(modeOfTransit) {
		case 1:
			indexStart = LRT1.indexOf(startRail);
			indexEnd = LRT1.indexOf(endRail);
			indexSum = Math.abs(indexStart - indexEnd);
			if(indexSum <= 4) {
				fare[0] = 12.0;
				fare[1] = 12.0;
			} else if (5 <= indexSum &&  indexSum <= 8) {
				fare[0] = 15.0;
				fare[1] = 13.0;
			} else if (9 <= indexSum &&  indexSum <= 12) {
				fare[0] = 15.0;
				fare[1] = 14.0;
			} else {
				fare[0] = 15.0;
				fare[1] = 15.0;
			}
			break;
		case 2:
			indexStart = LRT2.indexOf(startRail);
			indexEnd = LRT2.indexOf(endRail);
			indexSum = Math.abs(indexStart - indexEnd);
			if(indexSum <= 3) {
				fare[0] = 12.0;
			} else if (4 <= indexSum &&  indexSum <= 6) {
				fare[0] = 13.0;
			} else if (7 <= indexSum &&  indexSum <= 9) {
				fare[0] = 14.0;
			} else {
				fare[0] = 15.0;
			}
			break;
		case 3:
			indexStart = MRT3.indexOf(startRail);
			indexEnd = MRT3.indexOf(endRail);
			indexSum = Math.abs(indexStart - indexEnd);
			if(indexSum <= 2) {
				fare[0] = 10.0;
			} else if (3 <= indexSum &&  indexSum <= 4) {
				fare[0] = 11.0;
			} else if (5 <= indexSum &&  indexSum <= 6) {
				fare[0] = 12.0;
			} else if (7 <= indexSum &&  indexSum <= 8) {
				fare[0] = 13.0;
			} else if (9 <= indexSum &&  indexSum <= 10) {
				fare[0] = 14.0;
			} else {
				fare[0] = 15.0;
			}
			break;
		}

		return fare;
	}
	public double[] calculateFare(double distance, int modeOfTransit) {		
		double[] fare = {0.0, 0.0, 0.0, 0.0};
		float coeff = 4f;
		double distanceRounded = (Math.ceil(distance/1000));
		double fareRegularEst = 0.0;
		double fareDiscountedEst = 0.0;
		switch(modeOfTransit) {
		case 0: //JEEP
			if(distance < 4000) {
				fare[0] = 8.00; //regular price
				fare[1] = 6.50; //discounted
			} else {
				fareRegularEst = 1.4*(distanceRounded) + 2.402173913;				
				fare[0] = Math.round(fareRegularEst*coeff)/coeff;
				fareDiscountedEst = 1.119626889*(distanceRounded) + 1.980912735;
				fare[1] = Math.round(fareDiscountedEst*coeff)/coeff;
			}
			break;
		case 1: //BUS REGULAR
			if(distance < 5000) {
				fare[0] = 10.00; //regular price
				fare[1] = 8.00;  //discounted
				fare[2] = 12.00; //regular price
				fare[3] = 9.50;  //discounted
			} else {
				fareRegularEst = 1.850378788*(distanceRounded) + 0.728409091;				
				fare[0] = Math.round(fareRegularEst*coeff)/coeff;
				fareDiscountedEst = 1.479978355*(distanceRounded) + 0.606168831;
				fare[1] = Math.round(fareDiscountedEst*coeff)/coeff;
				fareRegularEst = 2.199801587*(distanceRounded) + 1.006547619;			
				fare[2] = Math.round(fareRegularEst*coeff)/coeff;
				fareDiscountedEst = 1.759181097*(distanceRounded) + 0.828841991;
				fare[3] = Math.round(fareDiscountedEst*coeff)/coeff;
			}
			break;
		}
		return fare;
	}
	
    public List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((lat / 1E5)),
                    ((lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
    
	public static InputStream getInputStreamFromUrl(String url) {
		InputStream content = null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			//if header is not set, response is in json format
			get.setHeader("Accept", "application/json");
			HttpResponse response = httpclient.execute(get);
			content = response.getEntity().getContent();
		} catch (Exception e) {
			Log.e("[GET REQUEST]", "Network exception", e);
		}
		return content;
	}
  
	public String inputStreamToString(InputStream is) {
		String line = "";
		StringBuilder total = new StringBuilder();
		
		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		// Read response until the end
		try {
			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
		} catch (IOException e) {
			
		}
      return total.toString();
  }
	
	public double[] knowTransportation (String dist, String routeId, String routeName, String mode, String start, String end) {
		double[] cost = {0.0, 0.0, 0.0, 0.0};
		int i = 0;
		if(routeId.length() != 0) {
			if((routeId.substring(6, 9).equals("PUJ")) && !(mode.equals("WALK"))) {
				this.modeOfTransit = "JEEP";
				i = 0;
				cost = calculateFare(Double.parseDouble(dist), i);
			} else if ((routeId.substring(6, 9).equals("PUB")) && !(mode.equals("WALK"))) {
				this.modeOfTransit = "BUS";
				i = 1;
				cost = calculateFare(Double.parseDouble(dist), i);
			} else if ((routeId.substring(6, 9).equals("880")) && !(mode.equals("WALK"))) {
				this.modeOfTransit = routeName;
				if(routeName.equals("LRT 1")) {
					i = 1;
				} else if (routeName.equals("LRT 2")) {
					i = 2;
				} else if (routeName.equals("MRT-3")) {
					i = 3;
				}
				cost = calculateFare(start, end, i);
			} else {
				this.modeOfTransit = "WALK";
				cost[0] = 0.0;
				cost[1] = 0.0;
				cost[2] = 0.0;
				cost[3] = 0.0;
			}
		}
		
		isTranspoKnown = true;
		
		return cost;
	}
	
	public String knowTransportationOnly (String routeId, 
			String routeName, 
			String mode) {
		if(routeId.length() != 0) {
			if((routeId.substring(6, 9).equals("PUJ")) && !(mode.equals("WALK"))) {
				this.modeOfTransit = "JEEP";
			} else if ((routeId.substring(6, 9).equals("PUB")) && !(mode.equals("WALK"))) {
				this.modeOfTransit = "BUS";
			} else if ((routeId.substring(6, 9).equals("880")) && !(mode.equals("WALK"))) {
				this.modeOfTransit = routeName;
			} else {
				this.modeOfTransit = "WALK";
			}
		}
		return modeOfTransit;
	}
	
	/**
	 * @param context used to check the device version and DownloadManager information
	 * @return true if the download manager is available
	 */
	public static boolean isDownloadManagerAvailable(Context context) {
	    try {
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
	            return false;
	        }
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
	                PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	/* Methods for TripDataUtilsExtension Interface */
	public String getCorrectModeOfTransit(Route route) {
		if (!isTranspoKnown) {
			this.knowTransportation(route.dist, route.agency, route.name, route.mode, route.start, route.end);
			return "";
		}
		isTranspoKnown = false;
		return this.modeOfTransit;
	}
	
	public double[] getRouteCostMatrix(Route route) {
		return this.knowTransportation(route.dist, route.agency, route.name, route.mode, route.start, route.end);
	}
}

