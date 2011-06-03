package com.syrinxoon.mtaccountcenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class AddAccountTask implements Runnable {
	
	/* Vars */
	
	private static final String DEBUG_TAG = "AddAccountTask";
	private static final String apiAddress = "https://api.mediatemple.net/api/v1/services.json";
	private static final String userAgentVersion = "1.0";
	private final AddAccountActivity addAccount;
	private final String apiKey;
	private JSONArray result;

	AddAccountTask(AddAccountActivity addAccount, String apiKey) {
		this.addAccount = addAccount;
		this.apiKey = apiKey;
	}
   
	
	/* Launch the verification */
	public void run() {
	   
	   result = verifyAccount(apiKey);
	   addAccount.manageApiKeyVerification(result);
	   
   }
   
   /*
    * Call the API and return the list of services
    * associated with the account provided
    */
	@SuppressWarnings("finally")
	private JSONArray verifyAccount(String apiKey) {
	   
		JSONArray json = null;

		try {
		   
			// Check if task has been interrupted
			if (Thread.interrupted()) throw new InterruptedException();
		   
			// Building RESTfull query		   
			String key = URLEncoder.encode(apiKey, "UTF-8");
			URL url = new URL(apiAddress + "?apikey=" + key + "&wrapRoot=false");
			HttpGet httpRequest = null;

			httpRequest = new HttpGet(url.toURI());
			httpRequest.addHeader("Content-type", "application/json");
			httpRequest.addHeader("Accept", "application/json");
			httpRequest.addHeader("Authorization", "MediaTemple " + apiKey);
			httpRequest.addHeader("Referer", "http://android.syrinxoon.net/p/mt-account-center.html");
			httpRequest.addHeader("User-Agent", "MtAccountCenter-Android/" + userAgentVersion);

			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) httpClient.execute(httpRequest);

			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			InputStream input = bufHttpEntity.getContent();
	       
			// Check if task has been interrupted
			if (Thread.interrupted()) throw new InterruptedException();
	       
			// Read results from the query
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			String payload = reader.readLine();
			reader.close();
			input.close();

			// Parse the result, an exception is threw if the API key is wrong
			try {
				json = new JSONArray(payload);	
				return json;
			} catch(JSONException e) {
				json = new JSONArray();
				json.put(1, true);
				return json;
			}
	       
		   
		} catch (InterruptedException e) {
			Log.e(DEBUG_TAG, "InterruptedException", e);
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "IOException", e);
		} catch (JSONException e) {
			Log.e(DEBUG_TAG, "JSONException", e);
		} catch (URISyntaxException e) {
			Log.e(DEBUG_TAG, "URISyntaxException", e);
		} finally {
			return json;
		}
	   
   }
   
}
