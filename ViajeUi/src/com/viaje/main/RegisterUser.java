package com.viaje.main;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import com.viaje.main.R;
import com.viaje.webinterface.JSONWebInterface;

public class RegisterUser extends Activity {

	private EditText fillup_first_name;
	private EditText fillup_last_name;
	private EditText fillup_email;
	private EditText fillup_password;
	private EditText fillup_confirm_password;
	
	private String first_name;
	private String last_name;
	private String email;
	private String password;
	private String confirm_password;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register_user);
		fillup_first_name = (EditText) findViewById(R.id.fillup_first_name);
		fillup_last_name = (EditText) findViewById(R.id.fillup_last_name);
		fillup_email = (EditText) findViewById(R.id.fillup_email);
		fillup_password = (EditText) findViewById(R.id.fillup_password);
		fillup_confirm_password = (EditText) findViewById(R.id.fillup_confirm_password);
		
		findViewById(R.id.btn_register).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						userPressedRegister();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register_user, menu);
		return true;
	}

	public void userPressedRegister()
	{
		fillup_password.setError(null);
		fillup_first_name.setError(null);
		
		// Store values at the time of the login attempt.
		first_name = fillup_first_name.getText().toString();
		last_name = fillup_last_name.getText().toString();
		email = fillup_email.getText().toString();
		password = fillup_password.getText().toString();
		confirm_password = fillup_confirm_password.getText().toString(); 

		boolean cancel = false;
		View focusView = null;

		// Check for a valid username.
		if (TextUtils.isEmpty(first_name)) {
			fillup_first_name.setError(getString(R.string.error_field_required));
			focusView = fillup_first_name;
			cancel = true;
		} else if (first_name.length() < 4) {
			fillup_first_name.setError(getString(R.string.error_invalid_name));
			focusView = fillup_first_name;
			cancel = true;
		}
		
		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			fillup_password.setError(getString(R.string.error_field_required));
			focusView = fillup_password;
			cancel = true;
		} else if (password.length() < 8) {
			fillup_password.setError(getString(R.string.error_invalid_pass_length));
			focusView = fillup_password;
			cancel = true;
		} else if(!password.equals(confirm_password)) {
			fillup_password.setError(getString(R.string.error_password_unmatch));
			focusView = fillup_confirm_password;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			fillup_email.setError(getString(R.string.error_field_required));
			focusView = fillup_email;
			cancel = true;
		} else if (!email.contains("@")) {
			fillup_email.setError(getString(R.string.error_invalid_email));
			focusView = fillup_email;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			RegisterTask register = new RegisterTask(email,
					first_name + " " + last_name,
					password);
			register.execute();		
		}
	}
	
	//FIXME: Crash bug when email already exists
	class RegisterTask extends AsyncTask<Void, Void, String> {
		String email;
		String name;
		String password;
		ProgressDialog mDialog;
		private SharedPreferences settings; 
		private SharedPreferences.Editor prefEditor;
		
		public RegisterTask(String email, String name, String password) {
			this.email = email;
			this.name = name;
			this.password = password;
		}
		
		@Override
		protected void onPreExecute() {
	        super.onPreExecute();
	        mDialog = new ProgressDialog(RegisterUser.this);
	        mDialog.setMessage("Registering...");
	        mDialog.show();
		}
		
		@Override
		protected String doInBackground(Void... params) {
			JSONWebInterface webInterface = new JSONWebInterface();
			
			String str = webInterface.sendRegisterRequest(email, name, password);
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONObject jsonObjOutput;
			int _userId = -1;
			
			if (result.equals("") == false) {
				try {
					jsonObjOutput = new JSONObject(result);
					_userId = Integer.parseInt(jsonObjOutput.get("user_id").toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			settings = getSharedPreferences("ViajePrefs", MODE_PRIVATE);
			prefEditor = settings.edit();
	        prefEditor.putString("email", email);
	        prefEditor.putString("password", password);
	        prefEditor.putString("username", name);
	        prefEditor.putInt("userId", _userId);
	        prefEditor.putBoolean("loggedin", true);
	        prefEditor.commit();
			mDialog.dismiss();
			
	        prefEditor.putBoolean("firstTime", false);
	        prefEditor.commit();
			Intent intent = new Intent(RegisterUser.this, Tutorial.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			RegisterUser.this.startActivity(intent);
			finish();
		}
	}
}
