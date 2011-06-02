package com.syrinxoon.mtaccountcenter;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity {
	
   // Option names and default values
   private static final String OPT_NOTIFICATIONS = "server_status_notifications";
   private static final boolean OPT_NOTIFICATIONS_DEF = false;
   private static final String OPT_NOTIFICATIONS_INTERVAL = "servers_notifications_interval";
   private static final int OPT_NOTIFICATIONS_INTERVAL_DEF = 15;
   private static final String OPT_NOTIFICATIONS_LEVEL = "servers_notifications_level";
   private static final int OPT_NOTIFICATIONS_LEVEL_DEF = 1;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
   }

   /** Get the current value of the notifications option */
   
   public static boolean getNotifications(Context context) {
      return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_NOTIFICATIONS, OPT_NOTIFICATIONS_DEF);
   }
   
   /** Get the current value of the notifications interval option */
   
   public static int getNotificationsInterval(Context context) {
      return PreferenceManager.getDefaultSharedPreferences(context).getInt(OPT_NOTIFICATIONS_INTERVAL, OPT_NOTIFICATIONS_INTERVAL_DEF);
   }
   
   /** Get the current value of the notifications level option */
   
   public static int getNotificationsLevel(Context context) {
      return PreferenceManager.getDefaultSharedPreferences(context).getInt(OPT_NOTIFICATIONS_LEVEL, OPT_NOTIFICATIONS_LEVEL_DEF);
   }
   
}
