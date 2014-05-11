package com.viaje.main;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.viaje.main.R;
import com.viaje.webinterface.JSONWebInterface;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private LoginTask loginTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	// For writinng to Pref
	private SharedPreferences settings; 
	private SharedPreferences.Editor prefEditor;
	
	private boolean hasBeenClicked = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		settings = this.getSharedPreferences("ViajePrefs", Context.MODE_PRIVATE);
		if(settings.getBoolean("loggedin", false)) {
			Intent intent = new Intent (this, UiMainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			this.startActivity(intent);
			finish();
		}
		
		findViewById(R.id.sign_up_button).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(LoginActivity.this, RegisterUser.class);
						LoginActivity.this.startActivity(intent);
					}
				});
		
		findViewById(R.id.log_in_button).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						userPressedLogIn();
					}
				});
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void userPressedLogIn()
	{
		hasBeenClicked = true;
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setVisibility(View.VISIBLE);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setVisibility(View.VISIBLE);
		Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
		mSignInButton.setVisibility(View.VISIBLE);
		Button mSignUpButton = (Button) findViewById(R.id.sign_up_button);
		mSignUpButton.setVisibility(View.GONE);
		Button mLogInButton = (Button) findViewById(R.id.log_in_button);
		mLogInButton.setVisibility(View.GONE);
				
		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (loginTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			loginTask = new LoginTask(mEmail, mPassword);
			loginTask.execute();
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
    @Override
    public void onBackPressed() {
    	if(hasBeenClicked) {
    		hasBeenClicked = false;
    		mEmailView = (EditText) findViewById(R.id.email);
    		mEmailView.setVisibility(View.GONE);
    		mPasswordView = (EditText) findViewById(R.id.password);
    		mPasswordView.setVisibility(View.GONE);
    		Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
    		mSignInButton.setVisibility(View.GONE);
    		Button mSignUpButton = (Button) findViewById(R.id.sign_up_button);
    		mSignUpButton.setVisibility(View.VISIBLE);
    		Button mLogInButton = (Button) findViewById(R.id.log_in_button);
    		mLogInButton.setVisibility(View.VISIBLE);
    	} else {
    		super.onBackPressed();
    		finish();
    	}
    }
    
	class LoginTask extends AsyncTask<Void, Void, String> {
		String email;
		String password;
		
		public LoginTask(String email, String password) {
			this.email = email;
			this.password = password;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			JSONWebInterface webInterface = new JSONWebInterface();	
//			webInterface.sendRegisterRequest("test.user@email.com", "Test User", "qwertyuiop");
			String str = webInterface.sendLoginRequest(email, password);
//			webInterface.sendGetIncidentsRequest(-1, "incident", null, 1, 20, 1);
//			String str = webInterface.sendPostReportRequest(userId, type, lat, lon, location, desc);
			
			return str;
		}

		@Override
		protected void onPostExecute(String result) {
			String username = null;
			try {
				JSONObject jsonObjOutput = new JSONObject(result);
				username = jsonObjOutput.get("name").toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			showProgress(false);
			//TODO: get result and check if errorcode is present,
			//errorcode: 2 invalid username and password
			if (!result.equals("")) {
				JSONObject jsonObjOutput;
				int _userId = -1;
				try {
					jsonObjOutput = new JSONObject(result);
					_userId = Integer.parseInt(jsonObjOutput.get("user_id").toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
		        prefEditor = settings.edit();
		        prefEditor.putString("email", mEmailView.getText().toString());
		        prefEditor.putString("password", mPassword);
		        prefEditor.putString("username", username);
		        prefEditor.putInt("userId", _userId);
		        prefEditor.putBoolean("loggedin", true);
		        prefEditor.commit();
				boolean firstTime = settings.getBoolean("firstTime", true);
		        if (firstTime)
		        {
			        prefEditor.putBoolean("firstTime", false);
			        prefEditor.commit();
					Intent intent = new Intent(LoginActivity.this, Tutorial.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					LoginActivity.this.startActivity(intent);
					finish();
		        } else {
					Intent intent = new Intent (LoginActivity.this, UiMainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					LoginActivity.this.startActivity(intent);
					finish();
		        }

			} else {
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}

		}
		
		@Override
		protected void onCancelled() {
			loginTask = null;
			settings = getSharedPreferences("ViajePrefs", MODE_PRIVATE);
	        prefEditor = settings.edit();
	        prefEditor.remove("email");
	        prefEditor.remove("password");
	        prefEditor.remove("username");
	        prefEditor.putInt("userId", -1);
	        prefEditor.putBoolean("loggedin", false);
	        prefEditor.commit();
			showProgress(false);
		}
	}
}
