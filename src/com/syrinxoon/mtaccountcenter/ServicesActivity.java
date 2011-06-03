package com.syrinxoon.mtaccountcenter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;

import static android.provider.BaseColumns._ID;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICES_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_ID;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_NAME;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_ACCOUNT;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_TYPE;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_PRIMARY_DOMAIN;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.TextItem;
import greendroid.widget.item.ThumbnailItem;
import greendroid.widget.item.Item;
import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

public class ServicesActivity extends GDListActivity {
	
	//private static final String DEBUG_TAG = "ServicesActivity";

	/* Vars */
	
	private AppData services;	
	private static String[] FROM = { _ID, SERVICE_ID, SERVICE_NAME, SERVICE_PRIMARY_DOMAIN, SERVICE_TYPE, };
	private static String ORDER_BY = _ID + " ASC";
	private String WHERE;
	private String apiKey;
	private int accountId;
	private String accountName;
	private QuickActionWidget mBar;
	private int selectedItemPosition;
	private ItemAdapter viewAdapter;
	private AppData database;
	
	// Dialogs
	private static final int RENAME_DIALOG = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	
    	// Retrieve the intent extras
    	this.accountId = getIntent().getIntExtra("accountId", 0);
    	this.apiKey = getIntent().getStringExtra("apiKey");
    	this.accountName = getIntent().getStringExtra("accountName");
    	
    	// Set the ActionBar title
    	String label = (String) getResources().getText(R.string.services_label);
    	getActionBar().setTitle(label + " " + accountName);
    	
    	// TODO: Add a service and refresh for the 1.1 or 1.2 version
    	/*
    	addActionBarItem(getActionBar()
                .newActionBarItem(NormalActionBarItem.class)
                .setDrawable(new ActionBarDrawable(getResources(), R.drawable.gd_action_bar_refresh)), R.id.action_bar_refresh);
    	
    	addActionBarItem(getActionBar()
                .newActionBarItem(NormalActionBarItem.class)
                .setDrawable(new ActionBarDrawable(getResources(), R.drawable.gd_action_bar_add)), R.id.action_bar_add);
        */
    	
    	// Load the database
    	services = new AppData(this);
    	
    	// Retrieve services for the given account
    	try {
    		Cursor cursor = getServices(accountId);
    		this.viewAdapter = showServices(cursor, apiKey);
    		prepareQuickActionBar();
    	} finally {
    		services.close();
    	}
    	
    	// Handle long click on the list view items
    	ListView lv = getListView();
    	lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    		@Override
    	    public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
    			onListItemLongClick(a, v, position, id);
    	        return true;
    	    }
    	}); 
        
    }
    
    /*
     * Manage dialogs
     */
    protected Dialog onCreateDialog(int id) {
    	
    	switch (id) {
    	
    	case RENAME_DIALOG:
    		LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.rename_service, null);
            return new AlertDialog.Builder(ServicesActivity.this)
                .setTitle(R.string.rename_service_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.rename_button, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {

                		// Getting item data
                		EditText nameInput = (EditText) textEntryView.findViewById(R.id.service_new_name);
                		String serviceName = nameInput.getText().toString();
                		ListAdapter adapter = ServicesActivity.this.getListAdapter();
                		ThumbnailItem service = (ThumbnailItem) adapter.getItem(ServicesActivity.this.selectedItemPosition);
                		String serviceSubtitle = (String) service.subtitle;
                		int serviceIcon = service.drawableId;
                		int serviceType = new Integer(service.getTag(3).toString());
                		int serviceId = new Integer(service.getTag(4).toString());
                		int serviceIdentifier = new Integer(service.getTag(5).toString());
                		
                		// Saving change in the database
                		ServicesActivity.this.database = new AppData(ServicesActivity.this);
                		SQLiteDatabase db = ServicesActivity.this.database.getWritableDatabase();
                		ContentValues values = new ContentValues();
                		values.put(SERVICE_NAME, serviceName);
                		String WHERE = _ID + "=" + serviceIdentifier;
                		
                		db.update(SERVICES_TABLE, values, WHERE, null);
                		ServicesActivity.this.database.close();
                		
                		// Updating the view
                		ServicesActivity.this.viewAdapter.remove(service);
                		ThumbnailItem item = new ThumbnailItem(serviceName, serviceSubtitle, serviceIcon);
                		item.setTag(1, ActionsActivity.class);
        	        	item.setTag(2, serviceName);
        	        	item.setTag(3, serviceType);
        	        	item.setTag(4, serviceId);
        	        	item.setTag(5, serviceIdentifier);
                		ServicesActivity.this.viewAdapter.insert(item, ServicesActivity.this.selectedItemPosition);
                		ServicesActivity.this.viewAdapter.notifyDataSetChanged();
                		Toast.makeText(ServicesActivity.this, getResources().getText(R.string.service_renamed), Toast.LENGTH_SHORT).show();
                		
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                		/* Do nothing */
                    }
                })
                .create();
    	
    	}
    	
    	return null;
    	
    }   
    
    /*
     * Handle the clicks on the list items
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	final TextItem textItem = (TextItem) l.getAdapter().getItem(position);
    	String serviceName = textItem.getTag(2).toString();
    	int serviceType = new Integer(textItem.getTag(3).toString());
    	int serviceId = new Integer(textItem.getTag(4).toString());
    	Intent intent = new Intent(ServicesActivity.this, (Class<?>) textItem.getTag(1));
    	
    	intent.putExtra("accountId", this.accountId);
    	intent.putExtra("serviceName", serviceName);
    	intent.putExtra("serviceType", serviceType);
    	intent.putExtra("serviceId", serviceId);
    	intent.putExtra("apiKey", this.apiKey);
    	
    	startActivity(intent);
    }
    
    /*
     * Handle the long click on the list items
     */
    protected void onListItemLongClick(AdapterView<?> a, View v, int position, long id) {
    	ServicesActivity.this.selectedItemPosition = position;
        mBar.show(v);
    }
    
    /* QuickActions management */
    
    /*
     * Create the QuickActionBars for the services
     */
    private void prepareQuickActionBar() {
        mBar = new QuickActionBar(this);
        mBar.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_edit, R.string.rename_quickaction));
        mBar.setOnQuickActionClickListener(mActionListener);
    }
    
    /*
     * Handle QuickActionsBar clicks
     */
    private OnQuickActionClickListener mActionListener = new OnQuickActionClickListener() {
        public void onQuickActionClicked(QuickActionWidget widget, int position) {
            switch (position) {
            case 0:
            	showDialog(RENAME_DIALOG);
            	break;
            }
        }
    };
    
    /*
     * QuickAction customization
     */
    private static class MyQuickAction extends QuickAction {
        
        private static final ColorFilter BLACK_CF = new LightingColorFilter(Color.BLACK, Color.BLACK);

        public MyQuickAction(Context ctx, int drawableId, int titleId) {
            super(ctx, buildDrawable(ctx, drawableId), titleId);
        }
        
        private static Drawable buildDrawable(Context ctx, int drawableId) {
            Drawable d = ctx.getResources().getDrawable(drawableId);
            d.setColorFilter(BLACK_CF);
            return d;
        }
        
    }
    
    /* Utility functions */
    
    /*
	 * Check if the serviceType matches a (ve) server
	 */
	private Boolean is_ve_server(int serviceType) {
		
		if (serviceType >= 668 && serviceType <= 723) return true;
		else return false;
		
	}
	
	/*
	 * Check if the serviceType matches a (dv) server
	 */
	private Boolean is_dv_server(int serviceType) {
	
		if (serviceType >= 208 && serviceType <= 737) return true;
		else return false;
		
	}
	
	/* Services management */
	
	/*
     * Get the services associated to the account
     */
    private Cursor getServices(int accountId) {
    	
    	// Perform a managed query
    	// The Activity will handle closing and re-querying the cursor when needed
    	
    	WHERE = SERVICE_ACCOUNT + "=" + accountId;
    	
    	SQLiteDatabase db = services.getReadableDatabase();
    	Cursor cursor = db.query(SERVICES_TABLE, FROM, WHERE, null, null, null, ORDER_BY);
    	startManagingCursor(cursor);
    	return cursor;
    	
    }
    
    /*
     * Show the services associated to the account
     */
    private ItemAdapter showServices(Cursor cursor, String apiKey) {
    	
    	int rows = cursor.getCount();
    	
    	if (rows == 0) {
    		setActionBarContentView(R.layout.no_services);
    	}
    	else {
    		
    		List<Item> items = new ArrayList<Item>();
    		
    		while (cursor.moveToNext()) { 
    	    
    			int serviceIdentifier = cursor.getInt(0);
    			int serviceId = cursor.getInt(1);
    	        String serviceName = cursor.getString(2);
    	        String servicePrimaryDomain = cursor.getString(3);
    	        int serviceType = cursor.getInt(4);
    	        
    	        ThumbnailItem item;
    	        
    	        if (is_ve_server(serviceType)) {
    	        	item = new ThumbnailItem(serviceName, servicePrimaryDomain, R.drawable.ve_server);
    	        	item.setTag(1, ActionsActivity.class);
    	        	item.setTag(2, serviceName);
    	        	item.setTag(3, serviceType);
    	        	item.setTag(4, serviceId);
    	        	item.setTag(5, serviceIdentifier);
        	        items.add(item);
    	        }
    	        else if (is_dv_server(serviceType)) {
    	        	item = new ThumbnailItem(serviceName, servicePrimaryDomain, R.drawable.dv_server);
    	        	item.setTag(1, ActionsActivity.class);
    	        	item.setTag(2, serviceName);
    	        	item.setTag(3, serviceType);
    	        	item.setTag(4, serviceId);
    	        	item.setTag(5, serviceIdentifier);
        	        items.add(item);
    	        }
    	     
    		}
    		
    		final ItemAdapter adapter = new ItemAdapter(this, items);
            setListAdapter(adapter);
            
            return adapter;
    		
    	}
    	
    	return null;
    	
    }
	
}
