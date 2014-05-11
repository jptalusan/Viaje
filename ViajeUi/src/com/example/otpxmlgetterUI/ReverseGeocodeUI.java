package com.example.otpxmlgetterUI;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

// This uses the Geocoder library to get coordinates from strings or strings from coordinates
public class ReverseGeocodeUI {
	
	private Context mContext;
	public boolean canGetCoordinates = false;
	public boolean isLocationValid = false;
	
	public ReverseGeocodeUI (Context context) {
		this.mContext = context;
	}
	
	public double[] getCoordinatesFromString (String location) throws IOException {
		UtilsUI util = new UtilsUI();
		double coordinates[] = {0.0, 0.0};
		if(util.checkIfEmptyString(location)) {
			this.canGetCoordinates = false;
		} else {
			Geocoder geocoder = new Geocoder(mContext);

			//geocoding
			List<Address> addressList;
				addressList = geocoder.getFromLocationName(location, 1, 14.479894, 120.970062, 14.774883, 121.061676 );
				if(addressList != null && addressList.size() > 0) {
				    // location exists
				    Address address = addressList.get(0);
				    if(address.hasLatitude() && address.hasLongitude()){
				        coordinates[0] = address.getLatitude();
				        coordinates[1] = address.getLongitude();
				        this.canGetCoordinates = true;
				        this.isLocationValid = true;
				    }
				} else { 
				    // location does not exist
					this.isLocationValid = false;
					this.canGetCoordinates = false;
				}
		}
		return coordinates;
	}


	public String getStringFromCoordinates (double Lat, double Long) throws IOException {		
		String location;
		Geocoder geocoder = new Geocoder(mContext);
		//reverse geocoding
		List<Address> addressList = geocoder.getFromLocation(Lat, Long, 1);
		if(addressList == null) {
			throw new IllegalArgumentException("Name has no content.");
		} else {
			Address address = addressList.get(0);
			location = (address.getFeatureName() + ", " + address.getLocality() +", " + address.getAdminArea() + ", " + address.getCountryName()).toString();
		}
		location = location.replace(", Metro Manila, Philippines", "");
		location = location.replace("null", "");
		return location;
	}
	
	public boolean canGetCoordinates() {
	    return this.canGetCoordinates;
	}
	
	public boolean isLocationValid() {
	    return this.canGetCoordinates;
	}
}