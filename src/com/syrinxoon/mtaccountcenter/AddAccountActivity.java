package com.syrinxoon.mtaccountcenter;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import static android.provider.BaseColumns._ID;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNTS_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNT_NAME;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNT_API_KEY;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICES_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_ACCOUNT;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_ID;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_NAME;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_TYPE;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_PRIMARY_DOMAIN;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import greendroid.app.GDActivity;

public class AddAccountActivity extends GDActivity {
	
	//private static final String DEBUG_TAG = "AddAccountActivity";
	
	private EditText accountName;
	private EditText accountApiKey;
	private Button addAccountButton;
	
	private Handler guiThread;
	private ExecutorService addAccountThread;
	private Runnable addAccountTask;
	private Future<?> addAccountPending;
	
	private Dialog dialog;
	private Handler manageDialogs = new Handler();
	
	private AppData database;
	
	private long accountId;
	private int servicesAdded = 0;
	private static final int ADD_ACCOUNT_DIALOG = 0;
	private static final int ERROR_NAME_DIALOG = 1;
	private static final int  ERROR_API_KEY_DIALOG = 2;
	private static final int ERROR_CREDENTIALS_DIALOG = 3;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    
		super.onCreate(savedInstanceState);

        setActionBarContentView(R.layout.add_account);
        
        // Get a handle to all user interface elements
        accountName = (EditText) findViewById(R.id.account_name);
        accountApiKey = (EditText) findViewById(R.id.account_api_key);    
        addAccountButton = (Button) findViewById(R.id.add_account_button);
        
        // Setup database
        database = new AppData(this);
        
        // Setup event handlers
        addAccountButton.setOnClickListener(new OnClickListener() { 
           public void onClick(View view) {
              verifyAccount();
           }
        });
        
        // Initialize threading
        initThreading();

    }
	
	protected Dialog onCreateDialog(int id) {
	    AlertDialog.Builder builder;
	    switch(id) {
	    case ADD_ACCOUNT_DIALOG:
	    	dialog = ProgressDialog.show(AddAccountActivity.this, "", getResources().getText(R.string.verifying_api_key), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                	if (addAccountPending != null) addAccountPending.cancel(true); 
                }
            });
	        break;
	    case ERROR_NAME_DIALOG:
	    	builder = new AlertDialog.Builder(AddAccountActivity.this);
			builder.setMessage(getResources().getText(R.string.account_name_error))
			       .setCancelable(false)
			       .setPositiveButton(getResources().getText(R.string.account_error_button), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			        	   accountName.requestFocus();
			           }
			       });
			dialog = builder.create();
	        break;
	    case ERROR_API_KEY_DIALOG:
	    	builder = new AlertDialog.Builder(AddAccountActivity.this);
			builder.setMessage(getResources().getText(R.string.account_api_key_error))
			       .setCancelable(false)
			       .setPositiveButton(getResources().getText(R.string.account_error_button), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			        	   accountApiKey.requestFocus();
			           }
			       });
			dialog = builder.create();
	    	break;
	    case ERROR_CREDENTIALS_DIALOG:
	    	builder = new AlertDialog.Builder(AddAccountActivity.this);
			builder.setMessage(getResources().getText(R.string.account_credentials_error))
			       .setCancelable(false)
			       .setPositiveButton(getResources().getText(R.string.account_error_button), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			        	   accountApiKey.requestFocus();
			           }
			       });
			dialog = builder.create();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	/*
	 * Start threading
	 */
	private void initThreading() {
	
		guiThread = new Handler();
	    addAccountThread = Executors.newSingleThreadExecutor();

	    // This task checks for account validity and updates the screen
	    addAccountTask = new Runnable() { 
	    	
	    	public void run() {
	        
	    		// Get API key and account name
	    		String name = accountName.getText().toString();
	    		String apiKey = accountApiKey.getText().toString();

	            // Cancel previous validation if there was one
	            if (addAccountPending != null) addAccountPending.cancel(true); 

	            // Check for input errors
	            if (name.length() < 3) {
	    			showDialog(ERROR_NAME_DIALOG);
	    		}
	    		else if (apiKey.length() != 65) {
	    			showDialog(ERROR_API_KEY_DIALOG);
	    		}
	            else {
	            	// Let user know we're doing something
	            	showDialog(ADD_ACCOUNT_DIALOG);

	               // Begin verification
	               try {
	                  AddAccountTask addAccountTask = new AddAccountTask(AddAccountActivity.this, apiKey); 
	                  addAccountPending = addAccountThread.submit(addAccountTask); 
	               } catch (RejectedExecutionException e) {
	                  // Unable to start new task
	                  dismissDialog(ADD_ACCOUNT_DIALOG);
	               }
	            }
	    	}
	    };
	}
	
	/*
	 * Verify if an accounts API key is correct 
	 */
	private void verifyAccount() {
		// Cancel previous verification if it hasn't started yet
	    guiThread.removeCallbacks(addAccountTask);
	    // Start a verification
	    guiThread.post(addAccountTask);
	}
	
	/*
	 * Manage API key verification's result
	 * Called from an other thread
	 */
	public void manageApiKeyVerification(JSONArray result) {
		
		Boolean error = false;
		try {
			error = result.getBoolean(1);
		} catch (JSONException e) {
			error = false;
		}
		
		if (error == true) {
			dismissDialog(ADD_ACCOUNT_DIALOG);
			
			this.manageDialogs.postDelayed(new Runnable() {
			    @Override
			    public void run() {
			        showDialog(ERROR_CREDENTIALS_DIALOG);
			    }
			}, 100);
		}
		else {
			// Registering accounts
			this.manageDialogs.post(new Runnable() {
			    @Override
			    public void run() {
			    	ProgressDialog altDialog = (ProgressDialog) AddAccountActivity.this.dialog;
					altDialog.setMessage(getResources().getText(R.string.registering_account));
			    }
			});			
			String name = accountName.getText().toString();
			String apiKey = accountApiKey.getText().toString();
			this.accountId = this.addAccount(name, apiKey);
			
			// Registering services
			this.manageDialogs.post(new Runnable() {
			    @Override
			    public void run() {
			    	ProgressDialog altDialog = (ProgressDialog) AddAccountActivity.this.dialog;
					altDialog.setMessage(getResources().getText(R.string.registering_services));
			    }
			});	
			for (int i = 0; i < result.length(); i++) {
		    	   
				JSONObject service;
				
				try {
					service = result.getJSONObject(i);
					int serviceType = service.getInt("serviceType");
					
					if (serviceType >= 668) {
					
						int serviceId = service.getInt("id");
						String serviceName = service.getString("serviceTypeName");
						String servicePrimaryDomain = service.getString("primaryDomain");
						
						this.addService(accountId, serviceType, serviceId, serviceName, servicePrimaryDomain);
						this.servicesAdded++;
					
					}
					
				} catch (JSONException e) {
					dismissDialog(ADD_ACCOUNT_DIALOG);
					Toast.makeText(this, getResources().getText(R.string.unexpected_error), Toast.LENGTH_LONG).show();
				}
		    	
		     }
		}
		
		// If the account doesn't have at least a (dv) 4.0 or (ve) sever
		if (this.servicesAdded == 0 && error == false) {
			this.deleteAccount(this.accountId);
		}
		dismissDialog(ADD_ACCOUNT_DIALOG);
		
		if (error == false) {
			Intent intent = new Intent(this, AccountsActivity.class);
			String toast;
			if (this.servicesAdded == 0) toast = (String) getResources().getText(R.string.account_not_added);
			else toast = (String) getResources().getText(R.string.account_added);
			intent.putExtra("addAccount", toast);
			startActivity(intent);
		}
		
	}
	
	private void addService(long accountId, int serviceType, int serviceId, String serviceName, String servicePrimaryDomain) {
		
		int account = (int) accountId; 
		
		SQLiteDatabase db = database.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SERVICE_ACCOUNT, account);
		values.put(SERVICE_NAME, serviceName);
		values.put(SERVICE_ID, serviceId);
		values.put(SERVICE_TYPE, serviceType);
		values.put(SERVICE_PRIMARY_DOMAIN, servicePrimaryDomain);
		
		db.insertOrThrow(SERVICES_TABLE, null, values);
		database.close();
		
	}
	
	private long addAccount(String name, String apiKey) {
	      
		SQLiteDatabase db = database.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ACCOUNT_NAME, name);
		values.put(ACCOUNT_API_KEY, apiKey);
		long accountId = db.insertOrThrow(ACCOUNTS_TABLE, null, values);
		database.close();
		   
		return accountId;
		   
	}
	
	private void deleteAccount(long accountId) {
		
		String WHERE = _ID + "=" + (int) accountId;
		SQLiteDatabase db = database.getWritableDatabase();
		db.delete(ACCOUNTS_TABLE, WHERE, null);
		db.close();
		
	}
	
}
