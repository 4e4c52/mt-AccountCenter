package com.syrinxoon.mtaccountcenter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import greendroid.app.GDActivity;

public class StatsActivity extends GDActivity {
	
	//private static final String DEBUG_TAG = "StatsActivity";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Retrieve intent extras
        String serviceName = getIntent().getStringExtra("serviceName");
        String apiKey = getIntent().getStringExtra("apiKey");
        int accountId = getIntent().getIntExtra("accountId", 0);
        int serviceType = getIntent().getIntExtra("serviceType", 0);
        int serviceId = getIntent().getIntExtra("serviceId", 0);
        
        // Set the ActionBar title
        String label = (String) getResources().getText(R.string.stats_label_1);
    	getActionBar().setTitle(label + " " + serviceName);
        
        JSONObject stats = null;
        
		try {
			stats = new JSONObject(getIntent().getStringExtra("stats"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Load the titles from the resources
		String[] titles = { getResources().getText(R.string.cpu).toString(), getResources().getText(R.string.memory).toString() };
		
		JSONArray items = stats.optJSONArray("stats");
		int length = 0;
		
		// If we can't retrieve any statistics, let's go back
		try {
			length = items.length();
		} catch (NullPointerException e) {
			Intent intent = new Intent(StatsActivity.this, (Class<?>) ServicesActivity.class);
			intent.putExtra("accountId", accountId);
			intent.putExtra("serviceName", serviceName);
	    	intent.putExtra("serviceType", serviceType);
	    	intent.putExtra("serviceId", serviceId);
	    	intent.putExtra("apiKey", apiKey);
	    	intent.putExtra("errorMessage", getResources().getText(R.string.no_stats).toString());
	    	startActivity(intent);		
		}
		
		long [] dates = new long[length];
		double cpu[] = new double[length];
		double memory[] = new double[length];
		
		for (int i = 0; i < length; i++) {
			
			JSONObject item = null;
			try {
				item = items.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			double itemCPU = item.optDouble("cpu");
			double itemMemory = item.optDouble("memory");
			
			dates[i] =  item.optLong("timeStamp") * 1000;
			cpu[i] = itemCPU;
			memory[i] = itemMemory;
			
		}
		
		IChart chart = new StatsChart(titles, dates, cpu, memory, length);
		View view = (View) chart.execute(this); 
		
		// Load the chart in the view
		setActionBarContentView(view);
		
    }
	
}
