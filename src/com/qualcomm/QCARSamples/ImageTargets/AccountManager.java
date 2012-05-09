package com.qualcomm.QCARSamples.ImageTargets;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class AccountManager {
	public static final String DOMAIN = "warm-journey-4949.herokuapp.com";
	/**
	 * Domain of the server with forward slashes.
	 */
	public static final String SLASH_DOMAIN = "//" + DOMAIN;
	
	protected static final String CSRFTOKEN = "csrfmiddlewaretoken";
	
	private static final String TAG = "CreateHuntThread";
	
	private class LoginThread extends Thread {

		private static final String TAG = "LoginThread";

		/**
		 * The account information.
		 */
		private String username, password;

		/**
		 * Logs the user in with the given username and password.
		 * 
		 * @param username
		 *            the username of the account
		 * @param password
		 *            the password of the account
		 */
		public LoginThread(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		public void run() {

			Log.i(TAG, "Logging in as " + username);

			// needed to display toasts
			Looper.prepare();

			String url = "http:" + SLASH_DOMAIN + "/accounts/";

			// the create hunt page must first be downloaded for the csrfToke
			HttpGet get = new HttpGet(url);
			
			DefaultHttpClient client = sInstance.getClient();
			
			HttpResponse response;
			
			try {
				// get page from server
				response = client.execute(get);
				
				HttpEntity entity = response.getEntity();
				
				String csrfToken = getCSRFToken(entity);
				
				entity.consumeContent();
				// send a response back to the server
				HttpPost post = new HttpPost(url);

				// include these values
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				// username and password of user
				params.add(new BasicNameValuePair(COLUMN_USERNAME, username));
				params.add(new BasicNameValuePair(COLUMN_PASSWORD, password));
				params.add(new BasicNameValuePair(CSRFTOKEN, csrfToken));
				// include csrf token
				
				// encode the parameters before sending
				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
				post.setEntity(ent);
				// send response
				HttpResponse responsePOST = client.execute(post);

				// get server's response to data
				HttpEntity resEntity = responsePOST.getEntity();
				
				// convert the html response into string and look for "Home Page"
				boolean loggered=false;
				String str= "";
				Scanner scanner;
				scanner = new Scanner(resEntity.getContent());
				while(scanner.hasNext() & !loggered) {
					String line= scanner.nextLine();
					str += line;
					if (line.contains("HomePage")) loggered=true;
					
				}
					DebugLog.LOGD(str);
				
				
				// server responded?
				if (loggered) {

					Log.i(TAG, "Logged in as " + username);

					// save the password and the username
					handler.post(new Runnable() {
						
						public void run() {
							// store the username and password
							sInstance.setPassword(password);
							sInstance.setUsername(username);
							// no longer loading
							sInstance.setLoading(false);
							// save to disk
							sInstance.store();
							// now logged in
							sInstance.setLoggedin(true);
						}
					});

					resEntity.consumeContent();

					return;
				} else {
					Log.e(TAG, "Could not log in.");
				}
			} catch (ClientProtocolException e) {
				Log.e(TAG, e.toString());
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
			// done
			handler.post(new Runnable() {
				
				public void run() {
					sInstance.setLoading(false);
				}
			});

			Toast.makeText(context, context.getString(R.string.error_login), Toast.LENGTH_SHORT);
		}
	}

	/**
	 * Separate thread for logging out.
	 * 
	 * @author Eric Leong
	 * 
	 */
	private class LogoutThread extends Thread {

		private static final String TAG = "LogoutThread";

		@Override
		public void run() {

			Log.i(TAG, "Logging out from " + username + "'s account.");

			// needed to display toasts
			Looper.prepare();

			String url = "http:" + SLASH_DOMAIN + "/accounts/logout/";

			HttpGet get = new HttpGet(url); // construct a get request
			// construct a client
			DefaultHttpClient client = sInstance.getClient();
			// receive data from the server
			HttpResponse response;

			try {
				// get page from server
				response = client.execute(get);
				// get page from response
				HttpEntity entity = response.getEntity();

				// server responded?
				if (entity != null) {

					Log.i(TAG, "Logged out");

					// note that we have logged out
					handler.post(new Runnable() {
						
						public void run() {
							// nullify data and save to disk
							sInstance.setPassword("");
							sInstance.setUsername("");
							sInstance.store();
							// also log out
							sInstance.setLoading(false);
							sInstance.setLoggedin(false);
						}
					});

					entity.consumeContent();

					return;
				}
			} catch (ClientProtocolException e) {
				Log.e(TAG, e.toString());
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	/**
	 * This thread does the actual work of downloading and parsing user profile
	 * data.
	 
	private class ProfileThread extends Thread {

		private static final String TAG = "ProfileThread";

		/**
		 * Parses the JSON file of hints.
		 * 
		 * @param string
		 *            The string of downloaded data.
		 
		private void parseProfile(String string) {
			try {
				// interpret String as a JSON array
				JSONArray json = new JSONArray(string);
				JSONObject jprofile = json.getJSONObject(0);

				// grab variables
				JSONObject jfields = jprofile.getJSONObject("fields");
				final String email = jfields.getString(COLUMN_EMAIL);

				JSONObject juserprofile = json.getJSONObject(1);
				// grab variables
				JSONObject juserfields = juserprofile.getJSONObject("fields");
				final int points = juserfields.getInt("points");

				// post the new hint to the current instance of HuntManager
				handler.post(new Runnable() {
					@Override
					public void run() {
						// set the account's email and number of points
						sInstance.setEmail(email);
						sInstance.setPoints(points);
						// notify observers
						sInstance.notifyObservers();
					}
				});
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}
		}

		@Override
		public void run() {
			// not sure if this is completely necessary
			Looper.prepare();

			String url = SLASH_DOMAIN + "/accounts/profile/" + HuntManager.JSON;

			// get data from the specified url
			URI uri;
			try {
				uri = new URI("http", url, null);
				HttpGet get = new HttpGet(uri); // construct a get request
				// construct a client
				DefaultHttpClient client = sInstance.getClient();
				// receive data from the server
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				// turn data stream into a string
				String str = HuntManager.convertStreamToString(entity.getContent());

				parseProfile(str);
			} catch (URISyntaxException e) {
				Toast.makeText(context, context.getString(R.string.error_connect),
						Toast.LENGTH_SHORT);
				Log.e(TAG, e.toString());
			} catch (ClientProtocolException e) {
				Toast.makeText(context, context.getString(R.string.error_connect),
						Toast.LENGTH_SHORT);
				Log.e(TAG, e.toString());
			} catch (IOException e) {
				Toast.makeText(context, context.getString(R.string.error_connect),
						Toast.LENGTH_SHORT);
				Log.e(TAG, e.toString());
			}
		}
	}
	
	*/
	
	/**
	 * Separate thread for signing up a user on the server.
	 * 
	 * @author Eric Leong
	 * 
	 */
	private class SignupThread extends Thread {

		private static final String TAG = "SignupThread";

		/**
		 * Input to the account creation form on the site.
		 */
		private String username, password1, password2;

		/**
		 * Creates an account with the given parameters.
		 * 
		 * @param username
		 *            the desired username
		 * @param password
		 *            the desired password
		 * @param email
		 *            the user's email
		 */
		public SignupThread(String username, String password1, String password2) {
			this.username = username;
			this.password1 = password1;
			this.password2 = password2;
		}

		@Override
		public void run() {
			
			Log.i(TAG, "Signing up as " + username);
			
			// needed to display toasts
			Looper.prepare();

			String uri = "http:" + SLASH_DOMAIN + "/polls/create/";

			// the signup page must first be downloaded for the csrfToken

			HttpGet get = new HttpGet(uri); // construct a get request
			// construct a client
			DefaultHttpClient client = sInstance.getClient();
			// receive data from the server
			HttpResponse response;

			try {
				// get page from server
				response = client.execute(get);
				// get page from response
				HttpEntity entity = response.getEntity();

				// get the csrfToken from the page
				String csrfToken = getCSRFToken(entity);
				
				entity.consumeContent();

				// send a response back to the server
				HttpPost post = new HttpPost(uri);

				// include these values
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				// username, password, and email
				params.add(new BasicNameValuePair(COLUMN_USERNAME, username));
				params.add(new BasicNameValuePair(COLUMN_PASSWORD + "1", password1));
				params.add(new BasicNameValuePair(COLUMN_PASSWORD + "2", password2));
				// include csrf token
				params.add(new BasicNameValuePair(CSRFTOKEN, csrfToken));
				// encode the parameters before sending
				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
				post.setEntity(ent);
				// send response
				HttpResponse responsePOST = client.execute(post);

				// get server's response to data
				HttpEntity resEntity = responsePOST.getEntity();

				boolean signered=false;
				String strs= "";
				Scanner scanner;
				scanner = new Scanner(resEntity.getContent());
				while(scanner.hasNext() & !signered) {
					String line= scanner.nextLine();
					strs += line;
					DebugLog.LOGI(line);
					if (line.contains("HomePage")) signered=true;
					
				}
					DebugLog.LOGD(strs);

					
				// server responded and response is valid
				if (signered) {

					// no longer loading
					handler.post(new Runnable() {
						
						public void run() {
							// store account details
							sInstance.setPassword(password);
							sInstance.setUsername(username);
							
							sInstance.setLoading(false);
							sInstance.store();
							sInstance.setLoggedin(true);
						}
					});

					resEntity.consumeContent();

					return;
				}

				resEntity.consumeContent();

			} catch (ClientProtocolException e) {
				Log.e(TAG, e.toString());
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
			// no longer loading
			handler.post(new Runnable() {
				
				public void run() {
					sInstance.setLoading(false);
				}
			});

			Toast.makeText(context, context.getString(R.string.error_login), Toast.LENGTH_SHORT);
		}
	}

	/**
	 * String for signing up.
	 */
	public static final String TREASUREHUNT_SIGNUP_EXTRA = "org.eid103.treasurehunt.signup";

	/**
	 * Holds the single instance of a {@link AccountManager} that is shared by
	 * the process.
	 */
	private static AccountManager sInstance;

	protected static String COLUMN_USERNAME = "username";
	protected static String COLUMN_PASSWORD = "password";
	protected static String COLUMN_EMAIL = "email";

	/**
	 * Creates a {@link AccountManager}.
	 * 
	 * @param context
	 *            a reference to the local environment
	 * @return The {@link AccountManager} shared by the given process.
	 */
	public static AccountManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new AccountManager(context.getApplicationContext());
		}
		return sInstance;
	}

	/**
	 * The client to be used for logged in users.
	 */
	private DefaultHttpClient client;

	/**
	 * The environment of the activity that started this {@link AccountManager}.
	 */
	private Context context;

	/**
	 * Post changes to account information.
	 */
	private Handler handler = new Handler();


	/**
	 * <code>True</code> if we are in the process of loading.
	 */
	private boolean loading;

	/**
	 * <code>True</code> if the user is logged in.
	 */
	private boolean loggedin;

	/**
	 * The number of points this current account has.
	 */
	private int points;

	/**
	 * Details of the current account.
	 */
	private String username, password, email;

	/**
	 * Observers interested in changes to the current search results.
	 */
	private ArrayList<WeakReference<DataSetObserver>> observers = new ArrayList<WeakReference<DataSetObserver>>();

	/**
	 * Creates a {@link AccountManager} given the application's current
	 * environment.
	 * 
	 * @param context
	 *            the current environment of the activity that created this
	 *            {@link AccountManager}
	 * 
	 * @see Context
	 */
	private AccountManager(Context context) {
		this.context = context;
		client = new DefaultHttpClient();
	}
	
	public void addObserver(DataSetObserver observer) {
		WeakReference<DataSetObserver> obs = new WeakReference<DataSetObserver>(observer);
		observers.add(obs);
	}

		
	public DefaultHttpClient getClient() {
		return client;
	}

	/**
	 * @return The current account's email.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return The current account's password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return The current account's points.
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * @return The current account's username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return Whether or not a thread is in the process of loading data.
	 */
	public boolean isLoading() {
		return loading;
	}

	/**
	 * @return Whether or not there is a user logged in.
	 */
	public boolean isLoggedin() {
		return loggedin;
	}

	/**
	 * Load current account's profile.
	 
	public void loadProfile() {
		new ProfileThread().start();
	}
	*/
	
	/**
	 * Logs the account in with the specified account information.
	 * 
	 * @param username
	 *            the account's username
	 * @param password
	 *            the account's password
	 */
	public void login(String username, String password) {
		new LoginThread(username, password).start();
	}

	/**
	 * Logs the current account out.
	 */
	public void logout() {
		new LogoutThread().start();
	}

	/**
	 * Called when something changes in our data set. Cleans up any weak
	 * references that are no longer valid along the way.
	 */
	private void notifyObservers() {
		final ArrayList<WeakReference<DataSetObserver>> observers = this.observers;

		// iterators are used to remove objects cleanly
		for (ListIterator<WeakReference<DataSetObserver>> li = observers.listIterator(); li
				.hasNext();) {
			WeakReference<DataSetObserver> weak = li.next();
			DataSetObserver obs = weak.get();
			if (obs != null) {
				obs.onChanged();
			} else {
				li.remove();
			}
		}
	}


	private void setLoading(boolean loading) {
		this.loading = loading;
	}

	private void setLoggedin(boolean loggedin) {
		this.loggedin = loggedin;
		notifyObservers();
	}

	private void setPassword(String password) {
		this.password = password;
	}
/*
	private void setPoints(int points) {
		this.points = points;
	}
*/
	private void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Creates a new account with the given account information.
	 * 
	 * @param username
	 *            the desired username
	 * @param password
	 *            the desired password
	 * @param email
	 *            the user's email
	 */
	public void signup(String username, String password, String password2) {
		new SignupThread(username, password, password2).start();
	}
	
	protected static final String getCSRFToken(HttpEntity entity) {
		String[] searchStrings = { CSRFTOKEN };
		String[] searchQuotes = { "\'" };
		return getValue(entity, searchStrings, searchQuotes)[0];
	}
	
	protected static final String getValue(HttpEntity entity, String search, String searchQuote) {
		// just calls the array version with one-element arrays
		String[] searchStrings = { search };
		String[] searchQuotes = { searchQuote };
		return getValue(entity, searchStrings, searchQuotes)[0];
	}
	
	/**
	 * Gets the values of the given variables in the given page.
	 * 
	 * @param entity
	 *            the page to search
	 * @param searchStrings
	 *            the names of the variables to search for
	 * @param searchQuotes
	 *            the quote the values are surrounded by
	 * @return The values of the variables in the page with the given names.
	 */
	protected static final String[] getValue(HttpEntity entity, String[] searchStrings,
			String[] searchQuotes) {
		// prepare list of found values
		String[] found = new String[searchStrings.length];
		Scanner scanner;
		try {
			scanner = new Scanner(entity.getContent());

			while (scanner.hasNext()) { // scan through each line of the page
				String line = scanner.nextLine();
				// for each line, scan for each of the strings to search for
				for (int i = 0; i < searchStrings.length; i++) {
					// check if line contains string
					if (line.contains(searchStrings[i])) {
						// search for the value of the variable, starting from
						// the for the location of the variable name
						int start = line.indexOf(
								"value=" + searchQuotes[i],
								line.indexOf("name=" + searchQuotes[i] + searchStrings[i]
										+ searchQuotes[i]));
						// starting from the end of value=', search for the end
						// quote
						int end = line.indexOf(searchQuotes[i], start + 8);

						// pull value out from the line
						found[i] = line.substring(start + 7, end);
					}
				}
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} catch (StringIndexOutOfBoundsException e) {
			Log.e(TAG, e.toString());
		}
		return found;
	}

	
	protected static final boolean isValidUpload(HttpEntity entity) {
		Scanner scanner;
		String line;
		try {
			scanner = new Scanner(entity.getContent());

			// scan through each line of the page
			while (scanner.hasNext()) {
				line = scanner.nextLine();
				// django spits out an error list if we make an error in the
				// submission
				if (line.contains("errorlist")) {
					// if the line is too big for logger, split it up
					for (int i = 0; i < line.length(); i += 200) {
						Log.e(TAG, line.substring(i, Math.min(line.length(), i + 200)));
					}
					// there was an error
					return false;
				}
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, e.toString());
			return false;
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}
	
	public void store(){
		
	}

}
