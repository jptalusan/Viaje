package com.example.otpxmlgetterUI;
import java.io.File;

import com.viaje.main.FindRouteFragment;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

public class DownloadFileFromURL {
	private Context mContext;
	private String mStart, mEnd;
	private String fileName;
	private boolean isFileExists;
	private double[] startCoords = {0.0, 0.0};
	private double[] endCoords = {0.0, 0.0};
	private String totalCost;
	private String totalDist;
	private String totalTraffic;
	
	public DownloadFileFromURL(Context context, 
			String start, 
			String end, 
			String startLat,
			String startLon, 
			String endLat, 
			String endLon, 
			double totalCost, 
			double totalDist, 
			int totalTraffic) {
		this.mContext = context;
		this.mStart = start;
		this.mEnd = end;
		this.startCoords[0] = Double.parseDouble(startLat);
		this.startCoords[1] = Double.parseDouble(startLon);
		this.endCoords[0] = Double.parseDouble(endLat);
		this.endCoords[1] = Double.parseDouble(endLon);
		this.totalCost = Double.toString(totalCost);
		this.totalDist = Double.toString(totalDist);
		this.totalTraffic = Integer.toString(totalTraffic);
	}
	
	public boolean hasDownloadedFile(String url) throws Exception {
		ReverseGeocodeUI rGC = new ReverseGeocodeUI(mContext);
		//TODO: Add checking for this so file name is ok
		if(mStart.equals(FindRouteFragment.CURRENT_LOCATION)) {
			mStart = rGC.getStringFromCoordinates(startCoords[0], startCoords[1]);
		} else if(mEnd.equals(FindRouteFragment.CURRENT_LOCATION)) {
			mEnd = rGC.getStringFromCoordinates(endCoords[0], endCoords[1]);
		}
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);	    	 
	    // Setting Dialog Title
	    alertDialog.setTitle("Save Route");	 
	    // Setting Dialog Message
	    alertDialog.setMessage("Do you want to save this route?");	 
	    // Setting Icon to Dialog
	    //alertDialog.setIcon(R.drawable.delete);
	
	    final String root_sd = Environment.getExternalStorageDirectory().toString();
	    //root_sd = storage/sdcard0/Viaje
	    File SDCardRoot = new File( root_sd + "/Viaje" );
	    boolean success = true;
	    if (!SDCardRoot.exists()) {
	        success = SDCardRoot.mkdir();
	    }
	    if (success) {
	        // Do something on success
	    } else {
	        // Do something else on failure 
	    }
	    fileName = mStart + "_To_" + mEnd + "_" + totalCost + "_" + totalDist + "_" + totalTraffic + ".txt";
		File file = new File(SDCardRoot, fileName);

		isFileExists = file.exists();
		if(isFileExists) {
			Toast.makeText(mContext, "Route already exists", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDescription(mStart + " To " + mEnd);
		request.setTitle("Viaje Route Download");
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.allowScanningByMediaScanner();
		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		request.setDestinationInExternalPublicDir("/Viaje", fileName);
	
		// get download service and enqueue file
		final DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		
		// Dialog continuation
	    // Setting Positive "Yes" Button
	    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog,int which) {
		    	manager.enqueue(request); //Download file
		    	Toast.makeText(mContext, "Route saved to: " + root_sd + "/Viaje", Toast.LENGTH_SHORT).show();   
	        }
	    });
	
	    // Setting Negative "NO" Button
	    alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        dialog.cancel();
	        }
	    });
	    // Showing Alert Message
	    alertDialog.show();
	    return true;   
	}
}

// URGENT: Use again if there is a problem with using download manager

/*public class DownloadFileFromURL extends AsyncTask<String, String, String> {
	private Context mContext;
	private String mStart, mEnd;
	private ProgressDialog pDialog;
	private String fileName;
	private boolean isFileExists;
	
	public DownloadFileFromURL(Context context, String start, String end) {
		this.mContext = context;
		this.mStart = start;
		this.mEnd = end;
	}
    *//**
     * Before starting background thread
     * Show Progress Bar Dialog
     * *//*
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onCreateDialog();
    }
	*//**
     * Downloading file in background thread
     * *//*
    @Override
    protected String doInBackground(String... f_url) {
        int count = 0;
        try {
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int lenghtOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

    	    String root_sd = Environment.getExternalStorageDirectory().toString();
    	    //root_sd = storage/sdcard0/Viaje
    	    File SDCardRoot = new File( root_sd + "/Viaje" );
    	    boolean success = true;
    	    if (!SDCardRoot.exists()) {
    	        success = SDCardRoot.mkdir();
    	    }
    	    if (success) {
    	        // Do something on success
    	    } else {
    	        // Do something else on failure 
    	    }
    		//create a new file, specifying the path, and the filename
    		//which we want to save the file as.
    	    
    	    // maybe add popup box warning to rename as iterator index
    	    fileName = mStart + "_To_" + mEnd + ".txt";
    		File file = new File(SDCardRoot, fileName);
    		//int i = 0;
    		isFileExists = file.exists();
    		if(isFileExists) {
    			return null;
    			//this.fileName = mStart + "_To_" + mEnd + Integer.toString(i) + ".txt";
    			//i++;
    		}
    		OutputStream output = new FileOutputStream(file);
            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress(""+(int)((total*100)/lenghtOfFile));
                
                // writing data to file
                output.write(data, 0, count);
            }
            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    *//**
     * Updating progress bar
     * *//*
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        pDialog.setProgress(Integer.parseInt(progress[0]));
   }

    *//**
     * After completing background task
     * Dismiss the progress dialog
     * **//*
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
    	pDialog.dismiss();
    	if(isFileExists) {
    		Toast.makeText(mContext, "File already exists.", Toast.LENGTH_SHORT).show();
    	} else {
        	Toast.makeText(mContext, "File downloaded to: storage/sdcard0/Viaje", Toast.LENGTH_SHORT).show();    		
    	}
    }
    
    protected Dialog onCreateDialog() {
        pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Downloading file. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(true);
        pDialog.show();
        return pDialog;
    }
}
*/

