package com.syrinxoon.mtaccountcenter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import greendroid.app.GDActivity;

public class StatsActivity extends GDActivity {
	
	//private static final String DEBUG_TAG = "StatsActivity";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String serviceName = getIntent().getStringExtra("serviceName");
        String label = (String) getResources().getText(R.string.stats_label_1);
    	getActionBar().setTitle(label + " " + serviceName);
        
        JSONObject stats = null;
        
		try {
			stats = new JSONObject(getIntent().getStringExtra("stats"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		String[] titles = { getResources().getText(R.string.cpu).toString(), getResources().getText(R.string.memory).toString() };
		
		JSONArray items = stats.optJSONArray("stats");
		
		int length = items.length();
		
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
			/*
			calendar.setTimeInMillis((item.optLong("timeStamp") * 1000));
			hour = calendar.get(Calendar.HOUR_OF_DAY);
			minute = calendar.get(Calendar.MINUTE);
			*/
			double itemCPU = item.optDouble("cpu");
			double itemMemory = item.optDouble("memory");
			
			dates[i] =  item.optLong("timeStamp") * 1000;
			cpu[i] = itemCPU;
			memory[i] = itemMemory;
			
		}
		
		IChart chart = new StatsChart(titles, dates, cpu, memory, length);
		View view = (View) chart.execute(this); 
		
		setActionBarContentView(view);
		
    }
	
}
