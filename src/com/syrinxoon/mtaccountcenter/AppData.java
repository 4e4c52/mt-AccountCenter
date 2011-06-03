package com.syrinxoon.mtaccountcenter;

import static android.provider.BaseColumns._ID;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNTS_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNT_API_KEY;
import static com.syrinxoon.mtaccountcenter.Constants.ACCOUNT_NAME;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICES_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_ACCOUNT;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_NAME;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_TYPE;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_ID;
import static com.syrinxoon.mtaccountcenter.Constants.SERVICE_PRIMARY_DOMAIN;
import static com.syrinxoon.mtaccountcenter.Constants.STATUSES_TABLE;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_TIMESTAMP; 
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_SERVICE;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_STAT;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_STATUS;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_VALUE;
import static com.syrinxoon.mtaccountcenter.Constants.STATUS_PERCENT;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppData extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "accountcenter.db";
	private static final int DATABASE_VERSION = 1;

	/* Create a helper object for the database */
	public AppData(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	    if (!db.isReadOnly()) {
	      // Enable foreign key constraints
	      db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}

	/*
	 * Create the tables in the database
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		/* Accounts table */
		db.execSQL("CREATE TABLE " + ACCOUNTS_TABLE + " (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + ACCOUNT_NAME
	            + " TEXT NOT NULL," + ACCOUNT_API_KEY + " TEXT NOT NULL);");
		
		/* Services table */
		db.execSQL("CREATE TABLE " + SERVICES_TABLE + " (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + SERVICE_ID
				+ " INTEGER, " + SERVICE_NAME
	            + " TEXT NOT NULL, " + SERVICE_ACCOUNT 
	            + " INTEGER," + SERVICE_PRIMARY_DOMAIN
	            + " TEXT NOT NULL, " + SERVICE_TYPE 
	            + " INTEGER, FOREIGN KEY (" + SERVICE_ACCOUNT 
	            + ") REFERENCES " + ACCOUNTS_TABLE + " (" + _ID + ") ON DELETE CASCADE);");
		
		/* Notifications history table */
		db.execSQL("CREATE TABLE " + STATUSES_TABLE + " (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + STATUS_TIMESTAMP
				+ " INTEGER, " + STATUS_SERVICE
				+ " INTEGER, " + STATUS_STAT
				+ " TEXT NOT NULL, " + STATUS_STATUS
				+ " TEXT NOT NULL, " + STATUS_VALUE
				+ " REAL, " + STATUS_PERCENT
				+ " INTEGER, FOREIGN KEY (" + STATUS_SERVICE
				+ ") REFERENCES " + SERVICES_TABLE + " (" + _ID + ") ON DELETE CASCADE);");		
	}

	   @Override
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		   db.execSQL("DROP TABLE IF EXISTS " + STATUSES_TABLE);
		   db.execSQL("DROP TABLE IF EXISTS " + SERVICES_TABLE);
		   db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE);
		   onCreate(db);
	   }
	
}