package com.syrinxoon.mtaccountcenter;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import static android.provider.BaseColumns._ID;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNTS_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNT_NAME;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNT_API_KEY;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICES_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_ID;
import static com.syrinxoon.mtaccountcenter.Constants.STATUSES_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_TIMESTAMP; 
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_SERVICE;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_STAT;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_STATUS;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_VALUE;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_PERCENT;

public class ServersStatusService extends Service {
	
	//private static final String DEBUG_TAG = "ServerStatusService";
	private static String[] FROM = { _ID, ACCOUNT_NAME, ACCOUNT_API_KEY, };
	private static String ORDER_BY = _ID + " ASC";
	
	private Timer timer = new Timer();
	private int interval = 60 * 60 * 1000;
	private int level = 1;
	private String currentAccountAPIKey;
	private AppData database;
	private SQLiteDatabase dbr;
	
	private final static int ACTION_WARNINGS = 8;
	
	Hashtable<String, Integer> levels = new Hashtable<String, Integer>();
	
	private Handler mHandler = new Handler(); 
	private Handler guiThread;
	private ExecutorService requestThread;
	private Runnable requestTask;
	private Future<?> requestPending;
	
	NotificationManager mNotificationManager;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
		initThreading();
		
		levels.put("Warning", 1);
		levels.put("Critical", 2);
		
		database = new AppData(ServersStatusService.this);
		dbr = database.getReadableDatabase();
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		
		interval = new Integer(preferences.getString("servers_notifications_interval", "15")) * 60 * 1000;
		level = new Integer(preferences.getString("servers_notifications_level", "1"));
		
		timer.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				
				mHandler.post(
					new Runnable() { 
						public void run() { 
							
							Cursor cursor = getAccounts();
					    	
					    	if (cursor.getCount() != 0) {
					    		
					    		while (cursor.moveToNext()) { 
					    			
					    	        ServersStatusService.this.currentAccountAPIKey = cursor.getString(2);
					    			guiThread.removeCallbacks(requestTask);
					    		    guiThread.post(requestTask);
					    		    
					    		 }
					    	}
					    	
					    	cursor.deactivate();
							
			            } 
			        }     
			    );

			}

		}, 0, interval);
		
	}
	
	public void manageRequestResult(JSONObject result) {
		
		Boolean error = false;
		try {
			error = result.getBoolean("error");
		} catch (JSONException e) {
			error = false;
		}
		
		if (error == false) {
		
		JSONArray servicesWarnings = null;
		int serviceId = 0;
		int timeStamp = 0;
		
		try {
			servicesWarnings = result.getJSONArray("serviceWarning");
			timeStamp = result.getInt("timeStamp");
			serviceId = servicesWarnings.getInt(0);
		} catch (JSONException e) {
			servicesWarnings = null;
		}
		
		if (servicesWarnings != null) {
			
			for (int i = 1; i < servicesWarnings.length() + 1; i++) {
		    	   
				JSONArray serviceWarnings;
				
				try {
					serviceWarnings = servicesWarnings.getJSONArray(i);
					
					for (int j = 0; j < serviceWarnings.length(); j++) {
						
						JSONObject warning = serviceWarnings.getJSONObject(j);
						String stat = warning.getString("stat");
						String status = warning.getString("status");
						Double value = warning.getDouble("value");
						Boolean percent = warning.getBoolean("percent");
						
						int levelId = (Integer) levels.get(status);
						
						if (!warningExists(serviceId, timeStamp, stat, status, value, percent) && levelId >= level) {
							
							addWarning(serviceId, timeStamp, stat, status, value, percent);
						
							int icon = R.drawable.stat_notify_error;
							CharSequence tickerText = getResources().getText(R.string.ticker);
							long when = System.currentTimeMillis();

							Notification notification = new Notification(icon, tickerText, when);
							
							Cursor service = getService(serviceId);
							String serviceDBId = service.getString(0);
							String serviceName = service.getString(1);
							int serviceType = service.getInt(2);
						
							Context context = getApplicationContext();
							CharSequence contentTitle = getResources().getText(R.string.ticker);
							
							TypedValue statText = null;
							getResources().getValue(stat, statText, true);
							TypedValue statusText = null;
							getResources().getValue(status, statusText, true);
							String not_part_1 = getResources().getText(R.string.not_part_1).toString();
							String not_part_2 = getResources().getText(R.string.not_part_2).toString();
							String not_part_3 = getResources().getText(R.string.not_part_3).toString();
							String not_part_4 = getResources().getText(R.string.not_part_4).toString();
							
							CharSequence contentText = not_part_1 + " " + statText + " " + not_part_2 + " " + serviceName + " " + not_part_3 + " " + statusText + " " + not_part_4;
							
							Intent notificationIntent = new Intent(this, ActionsActivity.class);
							notificationIntent.putExtra("serviceName", serviceName);
							notificationIntent.putExtra("serviceType", serviceType);
							notificationIntent.putExtra("serviceId", serviceDBId);
							notificationIntent.putExtra("apiKey", ServersStatusService.this.currentAccountAPIKey);
							
							PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
							notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
							
						}
						
					}
					
					
				} catch (JSONException e) {
					
				}
		    	
		     }
			
		}
		
		}
		
	}
	
	private Cursor getAccounts() {

    	Cursor cursor = this.dbr.query(ACCOUNTS_TABLE, FROM, null, null, null, null, ORDER_BY);
    	return cursor;
    	
    }
	
	private Cursor getService(int serviceId) {
		
		String WHERE = SERVICE_ID + "=" + serviceId;
		
    	Cursor cursor = this.dbr.query(SERVICES_TABLE, null, WHERE, null, null, null, null);
    	return cursor;
		
	}
	
	private Boolean warningExists(int serviceId, int timeStamp, String stat, String status, Double value, Boolean percent) {
		
		Boolean result = false;
		String WHERE = STATUS_SERVICE + "=" + serviceId + "," + STATUS_TIMESTAMP + "=" + timeStamp + "," + STATUS_STAT + "=" + stat + "," + STATUS_STATUS + "=" + status + "," + STATUS_VALUE + "=" + value + "," + STATUS_PERCENT + "=" + percent;
		
    	Cursor cursor = this.dbr.query(STATUSES_TABLE, null, WHERE, null, null, null, null);
    	
    	if (cursor.getCount() > 0) {
    		result = true;
    	}
    	
    	cursor.close();
		
		return result;
		
	}
	
	private void addWarning(int serviceId, int timeStamp, String stat, String status, Double value, Boolean percent) {
		
		SQLiteDatabase db = database.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(STATUS_SERVICE, serviceId);
		values.put(STATUS_TIMESTAMP, timeStamp);
		values.put(STATUS_STAT, stat);
		values.put(STATUS_STATUS, status);
		values.put(STATUS_VALUE, value);
		values.put(STATUS_PERCENT, percent);
		
		db.insertOrThrow(STATUSES_TABLE, null, values);
		database.close();
		
	}

	@Override
	public void onDestroy() {
		this.dbr.close();
		this.database.close();
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		
	}
	
	/*
	 * Start threading
	 */
	private void initThreading() {
	
		guiThread = new Handler();
	    requestThread = Executors.newSingleThreadExecutor();

	    // This task perform request
	    requestTask = new Runnable() { 
	    	
	    	public void run() {

	    		// Cancel previous action if there was one
	    		if (requestPending != null) requestPending.cancel(true);

	    		try {
	    			APIRequestTask requestTask = new APIRequestTask(ServersStatusService.this, ServersStatusService.this.currentAccountAPIKey, ACTION_WARNINGS); 
	    			requestPending = requestThread.submit(requestTask); 
	    		} catch (RejectedExecutionException e) {
	    			
	    		}
	    	}
	    };
	}
	
}