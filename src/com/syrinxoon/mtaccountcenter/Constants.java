package com.syrinxoon.mtaccountcenter;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {

	// Accounts table
	public final static String ACCOUNTS_TABLE = "accounts";
	public final static String ACCOUNT_NAME = "name";
	public final static String ACCOUNT_API_KEY = "api_key";
	
	// Services table
	public final static String SERVICES_TABLE = "services";
	public final static String SERVICE_NAME = "name";
	public final static String SERVICE_ACCOUNT = "account";
	public final static String SERVICE_TYPE = "type";
	public final static String SERVICE_ID = "service_id";
	public final static String SERVICE_PRIMARY_DOMAIN = "primary_domain";
	
	// Servers statuses history table
	public final static String STATUSES_TABLE = "statuses";
	public final static String STATUS_TIMESTAMP = "timestamp"; 
	public final static String STATUS_SERVICE = "service";
	public final static String STATUS_STAT = "stat";
	public final static String STATUS_STATUS = "status";
	public final static String STATUS_VALUE = "value";
	public final static String STATUS_PERCENT = "percentage";
	
}