package com.syrinxoon.mtaccountcenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.DescriptionItem;
import greendroid.widget.item.Item;
import greendroid.widget.item.SeparatorItem;
import greendroid.widget.item.TextItem;

public class ActionsActivity extends GDListActivity {
	
	//private final static String DEBUG_TAG = "ActionsActivity";
	
	private String serviceName;
	private String data;
	private int accountId;
	private int serviceType;
	private int serviceId;
	private String apiKey;
	
	private Handler guiThread;
	private ExecutorService requestThread;
	private Runnable requestTask;
	private Future<?> requestPending;
	
	private final static int ACTION_REBOOT = 1;
	private final static int ACTION_HD_SPACE = 2;
	private final static int ACTION_FLUSH = 3;
	private final static int ACTION_STATS_15 = 4;
	private final static int ACTION_STATS_60 = 5;
	private final static int ACTION_ROOT_PASSWORD = 6;
	private final static int ACTION_PLESK_PASSWORD = 7;
	
	private int actionType = 0;
	
	private Dialog dialog;
	private int currentDialog;
	private Handler manageDialogs = new Handler();
	
	private final static int REQUEST_ERROR = 0;
	private final static int REBOOT_DIALOG = 1;
	private final static int HD_SPACE_DIALOG = 2;
	private final static int FLUSH_DIALOG = 3;
	private final static int ROOT_PASSWORD_DIALOG = 4;
	private final static int SET_ROOT_PASSWORD_DIALOG = 5;
	private final static int PLESK_PASSWORD_DIALOG = 6;
	private final static int SET_PLESK_PASSWORD_DIALOG = 7;
	private final static int STATS_15_DIALOG = 8;
	private final static int STATS_60_DIALOG = 9;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	
    	this.accountId = getIntent().getIntExtra("accountId", 0);
    	this.serviceName = getIntent().getStringExtra("serviceName");
    	this.serviceType = getIntent().getIntExtra("serviceType", 0);
    	this.serviceId = getIntent().getIntExtra("serviceId", 0);
    	this.apiKey = getIntent().getStringExtra("apiKey");
    	
    	String label = (String) getResources().getText(R.string.actions_label);
    	getActionBar().setTitle(label + " " + this.serviceName);
    	
    	List<Item> list = new ArrayList<Item>();
    	
    	list.add(new DescriptionItem(getResources().getText(R.string.actions_description).toString())); 
    	
    	list.add(new SeparatorItem(getResources().getText(R.string.separator_utilities).toString()));
    	
    	list.add(createTextItem(R.string.action_reboot, ACTION_REBOOT));
    	
    	if ( ! is_dv_20_server(this.serviceType)) {
    		// Actions not available for (dv) 2.0 servers
    		list.add(createTextItem(R.string.action_hd_space, ACTION_HD_SPACE));
    		list.add(createTextItem(R.string.action_flush, ACTION_FLUSH));
    	}
    	
    	if (is_ve_server(this.serviceType) || is_dv_40_server(this.serviceType)) {
    		// Actions available only for (ve) and (dv) 4.0 servers
    		list.add(new SeparatorItem(getResources().getText(R.string.separator_stats).toString()));
    		
    		list.add(createTextItem(R.string.action_stats_15, ACTION_STATS_15));
        	list.add(createTextItem(R.string.action_stats_60, ACTION_STATS_60));
    	}
    	
    	list.add(new SeparatorItem(getResources().getText(R.string.separator_passwords).toString()));
    	
    	list.add(createTextItem(R.string.action_root_password, ACTION_ROOT_PASSWORD));
    	list.add(createTextItem(R.string.action_plesk_password, ACTION_PLESK_PASSWORD));
    	
    	final ItemAdapter adapter = new ItemAdapter(this, list);
        setListAdapter(adapter);
        
        // Any error message?
        String errorMessage = getIntent().getStringExtra("errorMessage");
    	if (errorMessage != null) Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        initThreading();
    	
	}
	
	private TextItem createTextItem(int text, int identifier) {
		final TextItem textItem = new TextItem(getResources().getText(text).toString());
		textItem.setTag(1, identifier);
		return textItem;
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	final TextItem textItem = (TextItem) l.getAdapter().getItem(position);
    	int identifier = new Integer(textItem.getTag(1).toString());
    	this.actionType = identifier;
    	if (identifier == ACTION_ROOT_PASSWORD) {
    		showDialog(SET_ROOT_PASSWORD_DIALOG);
    	}
    	else if (identifier == ACTION_PLESK_PASSWORD) {
    		showDialog(SET_PLESK_PASSWORD_DIALOG);
    	}
    	else {
    		performRequest(identifier, 0);
    	}
    }
	
	public void manageRequestResult(JSONObject stats) {
		
		Boolean error = false;
		error = stats.optBoolean("error", false);
		
		if (error == true) {
			dismissDialog(this.currentDialog);
			
			this.manageDialogs.postDelayed(new Runnable() {
			    @Override
			    public void run() {
			        showDialog(REQUEST_ERROR);
			    }
			}, 100);
		}
		else {
			
			Intent intent = null;
			
			switch (ActionsActivity.this.actionType) {
			
    		case ACTION_STATS_15:
    			dismissDialog(STATS_15_DIALOG);
    			intent = new Intent(ActionsActivity.this, StatsActivity.class);
    			intent.putExtra("accountId", this.accountId);
    			intent.putExtra("serviceName", this.serviceName);
    			intent.putExtra("apiKey", this.apiKey);
    			intent.putExtra("serviceType", this.serviceType);
    			intent.putExtra("serviceId", this.serviceId);
    			intent.putExtra("stats", stats.toString());
    			startActivity(intent);
    			break;
    		case ACTION_STATS_60:
    			dismissDialog(STATS_60_DIALOG);
    			intent = new Intent(ActionsActivity.this, StatsActivity.class);
    			intent.putExtra("accountId", this.accountId);
    			intent.putExtra("serviceName", this.serviceName);
    			intent.putExtra("apiKey", this.apiKey);
    			intent.putExtra("serviceType", this.serviceType);
    			intent.putExtra("serviceId", this.serviceId);
    			intent.putExtra("stats", stats.toString());
    			startActivity(intent);
    			break;
    			
			}
			
		}
		
	}
	
	public void manageRequestResult(JSONArray result) {
		
		Boolean error = false;
		try {
			error = result.getBoolean(1);
		} catch (JSONException e) {
			error = false;
		}
		
		if (error == true) {
			
			dismissDialog(this.currentDialog);
			
			this.manageDialogs.postDelayed(new Runnable() {
			    @Override
			    public void run() {
			        showDialog(REQUEST_ERROR);
			    }
			}, 100);
		}
		else {
			
			switch (ActionsActivity.this.actionType) {
    		
    		case ACTION_REBOOT:
    			dismissDialog(REBOOT_DIALOG);
    			this.manageDialogs.post(new Runnable() {
    			    @Override
    			    public void run() {
    			    	Toast.makeText(ActionsActivity.this, getResources().getText(R.string.server_rebooted), Toast.LENGTH_LONG).show();
    			    }
    			});		
    			break;
    		case ACTION_HD_SPACE:
    			dismissDialog(HD_SPACE_DIALOG);
    			this.manageDialogs.post(new Runnable() {
    			    @Override
    			    public void run() {
    			    	Toast.makeText(ActionsActivity.this, getResources().getText(R.string.hd_space_added), Toast.LENGTH_LONG).show();
    			    }
    			});		
    			break;
    		case ACTION_FLUSH:
    			dismissDialog(FLUSH_DIALOG);
    			this.manageDialogs.post(new Runnable() {
    			    @Override
    			    public void run() {
    			    	Toast.makeText(ActionsActivity.this, getResources().getText(R.string.firewall_flushed), Toast.LENGTH_LONG).show();
    			    }
    			});		
    			break;
    		case ACTION_ROOT_PASSWORD:
    			dismissDialog(ROOT_PASSWORD_DIALOG);
    			this.manageDialogs.post(new Runnable() {
    			    @Override
    			    public void run() {
    			    	Toast.makeText(ActionsActivity.this, getResources().getText(R.string.root_password_saved), Toast.LENGTH_LONG).show();
    			    }
    			});		
    			break;
    		case ACTION_PLESK_PASSWORD:
    			dismissDialog(PLESK_PASSWORD_DIALOG);
    			this.manageDialogs.post(new Runnable() {
    			    @Override
    			    public void run() {
    			    	Toast.makeText(ActionsActivity.this, getResources().getText(R.string.plesk_password_saved), Toast.LENGTH_LONG).show();
    			    }
    			});		
    			break;
    		
    		}
			
		}
		
	}
	
	private void performRequest(int identifier, int action) {
		if (action == ACTION_ROOT_PASSWORD) {
			showDialog(SET_ROOT_PASSWORD_DIALOG);
		}
		else if(action == ACTION_PLESK_PASSWORD) {
			showDialog(SET_PLESK_PASSWORD_DIALOG);
		}
		else {
			// Cancel previous request if it hasn't started yet
		    guiThread.removeCallbacks(requestTask);
		    // Start a request
		    guiThread.post(requestTask);	
		}
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

	    		// Let user know we're doing something
	    		switch (ActionsActivity.this.actionType) {
	    		
	    		case ACTION_REBOOT:
	    			showDialog(REBOOT_DIALOG);
	    			break;
	    		case ACTION_HD_SPACE:
	    			showDialog(HD_SPACE_DIALOG);
	    			break;
	    		case ACTION_FLUSH:
	    			showDialog(FLUSH_DIALOG);
	    			break;
	    		case ACTION_ROOT_PASSWORD:
	    			showDialog(ROOT_PASSWORD_DIALOG);
	    			break;
	    		case ACTION_PLESK_PASSWORD:
	    			showDialog(PLESK_PASSWORD_DIALOG);
	    			break;
	    		case ACTION_STATS_15:
	    			showDialog(STATS_15_DIALOG);
	    			break;
	    		case ACTION_STATS_60:
	    			showDialog(STATS_60_DIALOG);
	    			break;
	    		}

	    		try {
	    			APIRequestTask requestTask = new APIRequestTask(ActionsActivity.this, ActionsActivity.this.apiKey, ActionsActivity.this.serviceId, ActionsActivity.this.actionType, ActionsActivity.this.data); 
	    			requestPending = requestThread.submit(requestTask); 
	    		} catch (RejectedExecutionException e) {
	    			// Unable to start new task
	    			dismissDialog(ActionsActivity.this.currentDialog);
	    		}
	    	}
	    };
	}
	
	protected Dialog onCreateDialog(int id) {
	    AlertDialog.Builder builder;
	    switch(id) {
	    
	    case REBOOT_DIALOG:
	    	this.currentDialog = REBOOT_DIALOG;
	    	dialog = ProgressDialog.show(ActionsActivity.this, "", getResources().getText(R.string.rebooting_server), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                	if (requestPending != null) requestPending.cancel(true); 
                }
            });
	        break;
	    case HD_SPACE_DIALOG:
	    	this.currentDialog = HD_SPACE_DIALOG;
	    	dialog = ProgressDialog.show(ActionsActivity.this, "", getResources().getText(R.string.adding_hd_space), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                	if (requestPending != null) requestPending.cancel(true); 
                }
            });
	        break;
	    case FLUSH_DIALOG:
	    	this.currentDialog = FLUSH_DIALOG;
	    	dialog = ProgressDialog.show(ActionsActivity.this, "", getResources().getText(R.string.flushing_firewall), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                	if (requestPending != null) requestPending.cancel(true); 
                }
            });
	        break;
	    case STATS_15_DIALOG:
	    	this.currentDialog = STATS_15_DIALOG;
	    	dialog = ProgressDialog.show(ActionsActivity.this, "", getResources().getText(R.string.retrieving_stats), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                	if (requestPending != null) requestPending.cancel(true); 
                }
            });
	        break;
	    case STATS_60_DIALOG:
	    	this.currentDialog = STATS_60_DIALOG;
	    	dialog = ProgressDialog.show(ActionsActivity.this, "", getResources().getText(R.string.retrieving_stats), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                	if (requestPending != null) requestPending.cancel(true); 
                }
            });
	        break;
	    case ROOT_PASSWORD_DIALOG:
	    	this.currentDialog = ROOT_PASSWORD_DIALOG;
	    	dialog = ProgressDialog.show(ActionsActivity.this, "", getResources().getText(R.string.saving_root_password), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                	if (requestPending != null) requestPending.cancel(true); 
                }
            });
	    	break;
	    case SET_ROOT_PASSWORD_DIALOG:
	    	LayoutInflater factoryRoot = LayoutInflater.from(this);
            final View textEntryViewRoot = factoryRoot.inflate(R.layout.set_root_password, null);
            return new AlertDialog.Builder(ActionsActivity.this)
                .setTitle(R.string.set_root_password_title)
                .setView(textEntryViewRoot)
                .setPositiveButton(R.string.set_password_button, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {

                		// Getting item data
                		EditText passwordInput = (EditText) textEntryViewRoot.findViewById(R.id.root_new_password);
                		ActionsActivity.this.data = passwordInput.getText().toString();
                		performRequest(ACTION_ROOT_PASSWORD, 0);                		
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                		/* Do nothing */
                    }
                }).create();
	    case PLESK_PASSWORD_DIALOG:
	    	this.currentDialog = PLESK_PASSWORD_DIALOG;
	    	dialog = ProgressDialog.show(ActionsActivity.this, "", getResources().getText(R.string.saving_plesk_password), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                	if (requestPending != null) requestPending.cancel(true); 
                }
            });
	    	break;
	    case SET_PLESK_PASSWORD_DIALOG:
	    	LayoutInflater factoryPlesk = LayoutInflater.from(this);
            final View textEntryViewPlesk = factoryPlesk.inflate(R.layout.set_plesk_password, null);
            return new AlertDialog.Builder(ActionsActivity.this)
                .setTitle(R.string.set_plesk_password_title)
                .setView(textEntryViewPlesk)
                .setPositiveButton(R.string.set_password_button, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {

                		// Getting item data
                		EditText passwordInput = (EditText) textEntryViewPlesk.findViewById(R.id.plesk_new_password);
                		ActionsActivity.this.data = passwordInput.getText().toString();
                		performRequest(ACTION_PLESK_PASSWORD, 0);                		
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                		/* Do nothing */
                    }
                }).create();
		case REQUEST_ERROR:
	    	builder = new AlertDialog.Builder(ActionsActivity.this);
			builder.setMessage(getResources().getText(R.string.request_error))
			       .setCancelable(false)
			       .setPositiveButton(getResources().getText(R.string.close_button), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
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
	 * Check if the serviceType matches a (ve) server
	 */
	private Boolean is_ve_server(int serviceType) {
		
		if (serviceType >= 668 && serviceType <= 723) return true;
		else return false;
		
	}
	
	/*
	 * Check if the serviceType matches a (dv) 4.0 server
	 */
	private Boolean is_dv_40_server(int serviceType) {
	
		if (serviceType >= 725 && serviceType <= 737) return true;
		else return false;
		
	}
	
	/*
	 * Check if the serviceType matches a (dv) 3.5 server
	 */
	@SuppressWarnings("unused")
	private Boolean is_dv_35_server(int serviceType) {
		
		if (serviceType >= 605 && serviceType <= 610) return true;
		else return false;
		
	}
	
	/*
	 * Check if the serviceType matches a (dv) 3.0 server
	 */
	@SuppressWarnings("unused")
	private Boolean is_dv_30_server(int serviceType) {
		
		if (serviceType >= 525 && serviceType <= 535) return true;
		else return false;
		
	}
	
	/*
	 * Check if the serviceType matches a (dv) 2.0 server
	 */
	private Boolean is_dv_20_server(int serviceType) {
		
		if (serviceType >= 208 && serviceType <= 263) return true;
		else return false;
		
	}
	
}
