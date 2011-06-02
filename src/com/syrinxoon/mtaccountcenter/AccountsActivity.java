package com.syrinxoon.mtaccountcenter;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;

import static android.provider.BaseColumns._ID;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNTS_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNT_NAME;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNT_API_KEY;

import greendroid.app.GDListActivity;
import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ItemAdapter;
import greendroid.widget.NormalActionBarItem;
import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import greendroid.widget.item.TextItem;

public class AccountsActivity extends GDListActivity {
	
	//private static final String DEBUG_TAG = "AccountsActivity";
	
	private AppData database;	
	
	private static String[] FROM = { _ID, ACCOUNT_NAME, ACCOUNT_API_KEY, };
	private static String ORDER_BY = _ID + " ASC";
	private static final int RENAME_DIALOG = 0;
	private QuickActionWidget mBar;
	private int selectedItemPosition;
	private ItemAdapter viewAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        
    	addActionBarItem(getActionBar()
                .newActionBarItem(NormalActionBarItem.class)
                .setDrawable(new ActionBarDrawable(getResources(), R.drawable.gd_action_bar_add)), R.id.action_bar_add);
    	
    	String addAccount = getIntent().getStringExtra("addAccount");
    	
    	if (addAccount != null) Toast.makeText(this, addAccount, Toast.LENGTH_LONG).show();
       
    	database = new AppData(this);
    	
    	Boolean notifications = PreferencesActivity.getNotifications(this);
    	
    	if (notifications) {
    		startService(new Intent(this, ServersStatusService.class));
    	}
    	
    	try {
    		Cursor cursor = getAccounts();
    		this.viewAdapter = showAccounts(cursor);
    		prepareQuickActionBar();
    	} finally {
    		database.close();
    	}
    	
    	ListView lv = getListView();
    	lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    		@Override
    	    public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
    			onListItemLongClick(a, v, position, id);
    	        return true;
    	    }
    	});
        
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Boolean notifications = PreferencesActivity.getNotifications(this);
    	
    	if (notifications) {
    		startService(new Intent(this, ServersStatusService.class));
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.accounts_menu, menu);
       return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
       switch (item.getItemId()) {
       case R.id.add_account:
    	   startActivity(new Intent(this, AddAccountActivity.class));
    	   return true;
       case R.id.settings:
    	   startActivity(new Intent(this, PreferencesActivity.class));
           return true;
       case R.id.about:
    	   startActivity(new Intent(this, InformationsActivity.class));
    	   return true;
       }
       
       return false;
       
    }
    
    /*
     * Get registered accounts from the database
     * 
     * @return Accounts from the database
     */
    private Cursor getAccounts() {
    	
    	// Perform a managed query
    	// The Activity will handle closing and re-querying the cursor when needed
    	SQLiteDatabase db = database.getReadableDatabase();
    	Cursor cursor = db.query(ACCOUNTS_TABLE, null, null, null, null, null, ORDER_BY);
    	startManagingCursor(cursor);
    	return cursor;
    	
    }
    
    /*
     * Show saved accounts in the view
     */
    private ItemAdapter showAccounts(Cursor cursor) {
    	
    	int rows = cursor.getCount();
    	
    	if (rows == 0) {
    		setActionBarContentView(R.layout.no_accounts);
    	}
    	else {
    		
    		ItemAdapter adapter = new ItemAdapter(this);
    		
    		while (cursor.moveToNext()) { 
    	    
    			int accountId = cursor.getInt(0);
    	        String accountName = cursor.getString(1);
    	        String apiKey = cursor.getString(2);
    	        
    	        adapter.add(createTextItem(accountName, accountId, apiKey, ServicesActivity.class));
    	     
    		}
    		
    		setListAdapter(adapter);
    		
    		return adapter;
    		
    	}
    	
    	return null;
    	
    }
    
    private TextItem createTextItem(String accountName, int accountId, String apiKey, Class<?> klass) {
        final TextItem textItem = new TextItem(accountName);
        textItem.setTag(1, klass);
        textItem.setTag(2, accountId);
        textItem.setTag(3, apiKey);
        textItem.setTag(4, accountName);
        return textItem;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final TextItem textItem = (TextItem) l.getAdapter().getItem(position);
        String accountId = textItem.getTag(2).toString();
        String apiKey = textItem.getTag(3).toString();
        String accountName = textItem.getTag(4).toString();
        
        Intent intent = new Intent(AccountsActivity.this, (Class<?>) textItem.getTag(1));
        
        intent.putExtra("accountId", accountId);
        intent.putExtra("apiKey", apiKey);
        intent.putExtra("accountName", accountName);
        
        startActivity(intent);
    }
    
    protected void onListItemLongClick(AdapterView<?> a, View v, int position, long id) {
    	AccountsActivity.this.selectedItemPosition = position;
        mBar.show(v);
    }    
    
    /*
     * Handle clicks on the action bar
     */
    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        switch (item.getItemId()) {
            case R.id.action_bar_add:
                startActivity(new Intent(this, AddAccountActivity.class));
                return true;

            default:
                return super.onHandleActionBarItemClick(item, position);
        }
    }
    
    /*
     * Manage dialogs
     */
    protected Dialog onCreateDialog(int id) {
    	
    	switch (id) {
    	
    	case RENAME_DIALOG:
    		LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.rename_account, null);
            return new AlertDialog.Builder(AccountsActivity.this)
                .setTitle(R.string.rename_account_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.rename_button, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {

                		// Getting item data
                		EditText nameInput = (EditText) textEntryView.findViewById(R.id.account_new_name);
                		String accountName = nameInput.getText().toString();
                		ListAdapter adapter = AccountsActivity.this.getListAdapter();
                		TextItem account = (TextItem) adapter.getItem(AccountsActivity.this.selectedItemPosition);
                		int accountId = new Integer(account.getTag(2).toString());
                		String apiKey = account.getTag(3).toString();
                		
                		// Saving change in the database
                		AccountsActivity.this.database = new AppData(AccountsActivity.this);
                		SQLiteDatabase db = AccountsActivity.this.database.getWritableDatabase();
                		ContentValues values = new ContentValues();
                		values.put(ACCOUNT_NAME, accountName);
                		String WHERE = _ID + "=" + accountId;
                		
                		db.update(ACCOUNTS_TABLE, values, WHERE, null);
                		AccountsActivity.this.database.close();
                		
                		// Updating the view
                		AccountsActivity.this.viewAdapter.remove(account);
                		AccountsActivity.this.viewAdapter.insert(AccountsActivity.this.createTextItem(accountName, accountId, apiKey, ServicesActivity.class), AccountsActivity.this.selectedItemPosition);
                		AccountsActivity.this.viewAdapter.notifyDataSetChanged();
                		Toast.makeText(AccountsActivity.this, getResources().getText(R.string.account_renamed), Toast.LENGTH_SHORT).show();
                		
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
    
    private void deleteAccount(int accountId) {
		
		String WHERE = _ID + "=" + accountId;
		SQLiteDatabase db = database.getWritableDatabase();
		db.delete(ACCOUNTS_TABLE, WHERE, null);
		db.close();
		
	}
    
    /*
     * Create the QuickActionBars for the services
     */
    private void prepareQuickActionBar() {
        mBar = new QuickActionBar(this);
        mBar.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_edit, R.string.rename_quickaction));
        mBar.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_remove, R.string.remove_quickaction));
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
            case 1:
            	ListAdapter adapter = AccountsActivity.this.getListAdapter();
            	TextItem account = (TextItem) adapter.getItem(AccountsActivity.this.selectedItemPosition);
        		int accountId = new Integer(account.getTag(2).toString());
        		Toast.makeText(AccountsActivity.this, getResources().getText(R.string.removing_account), Toast.LENGTH_SHORT).show();
            	deleteAccount(accountId);
            	AccountsActivity.this.viewAdapter.remove(account);
            	AccountsActivity.this.viewAdapter.notifyDataSetChanged();
            	Toast.makeText(AccountsActivity.this, getResources().getText(R.string.account_removed), Toast.LENGTH_LONG).show();
            	break;
            }
        }
    };
    
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
    
}