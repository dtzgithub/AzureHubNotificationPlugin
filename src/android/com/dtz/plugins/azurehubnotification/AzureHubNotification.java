/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */
package com.dtz.plugins.azurehubnotification;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.notifications.NotificationsManager;

public class AzureHubNotification extends CordovaPlugin {
	protected static final String TAG = "Azure Hub Notification Plugin";
	private GoogleCloudMessaging gcm;
	private NotificationHub hub;
	public static final String ACTION_AZURE_HUB_NOTIFICATION_REGISTER = "ACTION_AZURE_HUB_NOTIFICATION_REGISTER";
	public static final String ACTION_AZURE_HUB_NOTIFICATION_UN_REGISTER = "ACTION_AZURE_HUB_NOTIFICATION_UN_REGISTER";
	
	
	public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    
    private String regid;

	public AzureHubNotification() {
	}

	@Override
	public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
		Log.i(TAG, "Inside the Android Execute method...");
		try {
			if (ACTION_AZURE_HUB_NOTIFICATION_REGISTER.equalsIgnoreCase(action)) {
				Log.i(TAG, "Action : ACTION_AZURE_HUB_NOTIFICATION_REGISTER , Data : " + data);
				String handleTag = data.getString(0);
				registerForAzureNotificationHub(handleTag, callbackContext);
				return true;
			}else if(ACTION_AZURE_HUB_NOTIFICATION_UN_REGISTER.equalsIgnoreCase(action)) {
				Log.i(TAG, "Action : ACTION_AZURE_HUB_NOTIFICATION_UN_REGISTER");
				unRegisterFromAzureNotificationHub(callbackContext);
				return true;
			}
			callbackContext.error("Invalid action");
			return false;
		} catch (Exception ex) {
			Log.e(TAG, "Exception: " + ex.getMessage());
			return false;
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void unRegisterFromAzureNotificationHub(final CallbackContext callbackContext) {
		final String senderId = AzureConfig.getSenderId(this.cordova.getActivity());
		NotificationsManager.handleNotifications(this.cordova.getActivity(), senderId, NotificationHandler.class);
		gcm = GoogleCloudMessaging.getInstance(this.cordova.getActivity());
		String connectionString = AzureConfig.getEndPoint(this.cordova.getActivity());
		String notificationHubPath = AzureConfig.getNotificationHubPath(this.cordova.getActivity());
		
		Log.i(TAG,"Configuration data  :senderId : "+senderId+" ,  notificationHubPath :"+notificationHubPath+" ,  connectionString : "+connectionString);
		
		hub = new NotificationHub(notificationHubPath, connectionString, this.cordova.getActivity());
		new AsyncTask() {
			@Override
			protected Object doInBackground(Object... params) {
				try {
					regid = getRegistrationId(AzureHubNotification.this.cordova.getActivity());
					if(!regid.isEmpty()) {
						gcm.unregister();
						hub.unregisterAll(regid);
						Log.i(TAG,"Unregister for GCM and Azure hub using REG_ID : "+regid);
					}else {
						Log.i(TAG,"You can unregister before register");
						
					}
				} catch (Exception e) {
					Log.e(TAG, "Error in Unregistering Handle : " + e.getMessage());
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Object result) {
				Log.i(TAG, "Handle unregistered");
				super.onPostExecute(result);
				sendNotificationCallback(callbackContext, "Handle unregistered with Server", PluginResult.Status.OK);
			}
		}.execute(null, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void registerForAzureNotificationHub(final String handle, final CallbackContext callbackContext) {
		
		
		final String senderId = AzureConfig.getSenderId(this.cordova.getActivity());
		NotificationsManager.handleNotifications(this.cordova.getActivity(), senderId, NotificationHandler.class);
		gcm = GoogleCloudMessaging.getInstance(this.cordova.getActivity());
		String connectionString = AzureConfig.getEndPoint(this.cordova.getActivity());
		String notificationHubPath = AzureConfig.getNotificationHubPath(this.cordova.getActivity());
		
		Log.i(TAG,"Configuration data  :senderId : "+senderId+" ,  notificationHubPath :"+notificationHubPath+" ,  connectionString : "+connectionString);
		
		hub = new NotificationHub(notificationHubPath, connectionString, this.cordova.getActivity());
		new AsyncTask() {
			@Override
			protected Object doInBackground(Object... params) {
				try {
					regid = getRegistrationId(AzureHubNotification.this.cordova.getActivity());
					if(regid.isEmpty()) {
						hub.unregister();
						gcm.unregister();
						regid = gcm.register(senderId);						
						hub.register(regid, handle);
						Log.i(TAG,"Register for GCM and Azure hub using REG_ID : "+regid);
					}else {
						Log.i(TAG,"Device is already registered for GCM and Azure hub using REG_ID : "+regid);
					}
					storeRegistrationId(AzureHubNotification.this.cordova.getActivity(), regid);
				} catch (Exception e) {
					Log.e(TAG, "Error in Registering Handle : " + e.getMessage());
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Object result) {
				Log.i(TAG, "Handle Registered");
				super.onPostExecute(result);
				sendNotificationCallback(callbackContext, "Handle Registered with Server", PluginResult.Status.OK);
			}
		}.execute(null, null, null);
	}

	// Send Notification callback to java script object
	protected void sendNotificationCallback(CallbackContext callbackContext, String result, Status status) {
		PluginResult progressResult = new PluginResult(status, result);
		progressResult.setKeepCallback(true);
		callbackContext.sendPluginResult(progressResult);
	}
	
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return context.getSharedPreferences("DTZ_SharedPreferences",
	            Context.MODE_PRIVATE);
	}
	
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
}
