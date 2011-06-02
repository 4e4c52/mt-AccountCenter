package com.syrinxoon.mtaccountcenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class APIRequestTask implements Runnable {
	
   private static final String DEBUG_TAG = "APIRequestTask";
   private static final String apiServicesAddress = "https://api.mediatemple.net/api/v1/services/";
   private static final String apiStatsAddress = "https://api.mediatemple.net/api/v1/stats/";
   private static final String userAgentVersion = "1.0";
   private ActionsActivity actions;
   private ServersStatusService statuses;
   private final String apiKey;
   private String data;
   private final int action;
   private int serviceId;
   
   private final static int ACTION_REBOOT = 1;
   private final static int ACTION_HD_SPACE = 2;
   private final static int ACTION_FLUSH = 3;
   private final static int ACTION_STATS_15 = 4;
   private final static int ACTION_STATS_60 = 5;
   private final static int ACTION_ROOT_PASSWORD = 6;
   private final static int ACTION_PLESK_PASSWORD = 7;
   private final static int ACTION_WARNINGS = 8;

   APIRequestTask(ActionsActivity actions, String apiKey, int serviceId, int action, String data) {
      this.actions = actions;
      this.apiKey = apiKey;
      this.serviceId = serviceId;
      this.action = action;
      this.data = data;
   }
   
   APIRequestTask(ServersStatusService statuses, String apiKey, int action) {
	  this.statuses = statuses;
	  this.apiKey = apiKey;
	  this.action = action;
   }
   
   public void run() {
	   
	   JSONArray result = null;
	   JSONObject stats = null;
	   JSONObject warnings = null;
	   
	   switch (this.action) {
	   
	   case ACTION_REBOOT:
		   result = rebootServer();
		   break;
	   case ACTION_HD_SPACE:
		   result = addHDSpace();
		   break;
	   case ACTION_FLUSH:
		   result = flushFirewall();
		   break;
	   case ACTION_ROOT_PASSWORD:
		   result = setRootPassword(this.data);
		   break;
	   case ACTION_PLESK_PASSWORD:
		   result = setPleskPassword(this.data);
		   break;
	   case ACTION_STATS_15:
		   stats = getStats15();
		   break;
	   case ACTION_STATS_60:
		   stats = getStats60();
		   break;
	   case ACTION_WARNINGS:
		   warnings = getWarnings();
		   break;
	   
	   }
	   
	   if (result != null) actions.manageRequestResult(result);
	   if (stats != null) actions.manageRequestResult(stats);
	   if (warnings != null) statuses.manageRequestResult(warnings);
	   
   }
   
   private JSONArray rebootServer() {
	
	   JSONArray json = null;
	   
	   try {

		   if (Thread.interrupted()) throw new InterruptedException();
		
			// Building RESTfull query		   
			String key = URLEncoder.encode(this.apiKey, "UTF-8");
			URL url = new URL(APIRequestTask.apiServicesAddress + this.serviceId + "/reboot.json" + "?apikey=" + key);
			json = performPostRequest(url);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return json;
	   
   }
   
   private JSONArray addHDSpace() {
		
	   JSONArray json = null;
	   
	   try {

		   if (Thread.interrupted()) throw new InterruptedException();
		
			// Building RESTfull query		   
			String key = URLEncoder.encode(this.apiKey, "UTF-8");
			URL url = new URL(APIRequestTask.apiServicesAddress + this.serviceId + "/disk/temp.json" + "?apikey=" + key);
			json = performPostRequest(url);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return json;
	   
   }
   
   private JSONArray flushFirewall() {
		
	   JSONArray json = null;
	   
	   try {

		   if (Thread.interrupted()) throw new InterruptedException();
		
			// Building RESTfull query		   
			String key = URLEncoder.encode(this.apiKey, "UTF-8");
			URL url = new URL(APIRequestTask.apiServicesAddress + this.serviceId + "/firewall/flush.json" + "?apikey=" + key);
			json = performPostRequest(url);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return json;
	   
   }
   
   private JSONObject getStats15() {
		
	   JSONObject json = null;
	   
	   try {

		   if (Thread.interrupted()) throw new InterruptedException();
		   
		   long start = ((System.currentTimeMillis() / 1000) - (15 * 60));
		   long end = (System.currentTimeMillis() / 1000);
		
			// Building RESTfull query		   
			String key = URLEncoder.encode(this.apiKey, "UTF-8");
			URL url = new URL(APIRequestTask.apiStatsAddress + this.serviceId + ".json?start=" + start + "&end=" + end + "&resolution=60&apikey=" + key + "&wrapRoot=false");
			json = performGetRequest(url);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return json;
	   
   }
   
   private JSONObject getStats60() {
		
	   JSONObject json = null;
	   
	   try {

		   if (Thread.interrupted()) throw new InterruptedException();
		
		   long start = ((System.currentTimeMillis() / 1000) - (60 * 60));
		   long end = (System.currentTimeMillis() / 1000);
		
			// Building RESTfull query		   
			String key = URLEncoder.encode(this.apiKey, "UTF-8");
			URL url = new URL(APIRequestTask.apiStatsAddress + this.serviceId + ".json?start=" + start + "&end=" + end + "&resolution=60&apikey=" + key + "&wrapRoot=false");
			json = performGetRequest(url);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return json;
	   
   }
   
   private JSONArray setRootPassword(String data) {
		
	   JSONArray json = null;
	   
	   try {

		   if (Thread.interrupted()) throw new InterruptedException();
		
			// Building RESTfull query		   
			String key = URLEncoder.encode(this.apiKey, "UTF-8");
			URL url = new URL(APIRequestTask.apiServicesAddress + this.serviceId + "/rootPassword.json" + "?apikey=" + key);
			JSONObject password = new JSONObject();
			password.put("password", data);
			json = performPutRequest(url, password);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	   
   }
   
   private JSONArray setPleskPassword(String data) {
		
	   JSONArray json = null;
	   
	   try {

		   if (Thread.interrupted()) throw new InterruptedException();
		
			// Building RESTfull query		   
			String key = URLEncoder.encode(this.apiKey, "UTF-8");
			URL url = new URL(APIRequestTask.apiServicesAddress + this.serviceId + "/pleskPassword.json" + "?apikey=" + key);
			JSONObject password = new JSONObject();
			password.put("password", data);
			json = performPutRequest(url, password);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	   
   }
   
   private JSONObject getWarnings() {
		
	   JSONObject json = null;
	   
	   try {

		   if (Thread.interrupted()) throw new InterruptedException();
		
			// Building RESTfull query		   
			String key = URLEncoder.encode(this.apiKey, "UTF-8");
			URL url = new URL(APIRequestTask.apiStatsAddress + "warnings.json?apikey=" + key + "&wrapRoot=false");
			json = performGetRequest(url);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return json;
	   
   }
   
   private JSONObject performGetRequest(URL url) {
	   
	   JSONObject json = null;
	   
	   try {
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

		   try {
			   json = new JSONObject(payload);	
			   return json;
		   } catch(JSONException e) {
			   json = new JSONObject();
			   json.put("error", true);
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
	   }
	   
	   return json;
	   
   }
   
   private JSONArray performPostRequest(URL url) {
	   
	   JSONArray json = null;
	   
	   try {
		   HttpPost httpRequest = null;

		   httpRequest = new HttpPost(url.toURI());
		   httpRequest.addHeader("Content-type", "application/json");
		   httpRequest.addHeader("Accept", "application/json");
		   httpRequest.addHeader("Authorization", "MediaTemple " + apiKey);
		   httpRequest.addHeader("Referer", "http://android.syrinxoon.net/p/mt-account-center.html");
		   httpRequest.addHeader("User-Agent", "MtAccountCenter-Android/" + userAgentVersion);

		   HttpClient httpClient = new DefaultHttpClient();
		   HttpResponse response = (HttpResponse) httpClient.execute(httpRequest);

		   int statusCode = response.getStatusLine().getStatusCode();
		   
		   if (statusCode == 202) {
			   json = new JSONArray();
			   json.put(1, false);
			   return json; 
		   }
		   else {
			   json = new JSONArray();
			   json.put(1, true);
			   return json;
		   }       
	   
	   } catch (IOException e) {
		   Log.e(DEBUG_TAG, "IOException", e);
	   } catch (JSONException e) {
		   Log.e(DEBUG_TAG, "JSONException", e);
	   } catch (URISyntaxException e) {
		   Log.e(DEBUG_TAG, "URISyntaxException", e);
	   }
	   
	   return json;
	   
   }
   
private JSONArray performPutRequest(URL url, JSONObject data) {
	   
	   JSONArray json = null;
	   
	   try {
		   HttpPut httpRequest = null;

		   httpRequest = new HttpPut(url.toURI());
		   httpRequest.addHeader("Content-type", "application/json");
		   httpRequest.addHeader("Accept", "application/json");
		   httpRequest.addHeader("Authorization", "MediaTemple " + apiKey);
		   httpRequest.addHeader("Referer", "http://android.syrinxoon.net/p/mt-account-center.html");
		   httpRequest.addHeader("User-Agent", "MtAccountCenter-Android/" + userAgentVersion);
		   
		   HttpEntity entity = new StringEntity(data.toString());
		   httpRequest.setEntity(entity);

		   HttpClient httpClient = new DefaultHttpClient();
		   HttpResponse response = (HttpResponse) httpClient.execute(httpRequest);

		   int statusCode = response.getStatusLine().getStatusCode();
		   
		   if (statusCode == 202) {
			   json = new JSONArray();
			   json.put(1, false);
			   return json; 
		   }
		   else {
			   json = new JSONArray();
			   json.put(1, true);
			   return json;
		   }       
	   
	   } catch (IOException e) {
		   Log.e(DEBUG_TAG, "IOException", e);
	   } catch (JSONException e) {
		   Log.e(DEBUG_TAG, "JSONException", e);
	   } catch (URISyntaxException e) {
		   Log.e(DEBUG_TAG, "URISyntaxException", e);
	   }
	   
	   return json;
	   
   }
   
}

