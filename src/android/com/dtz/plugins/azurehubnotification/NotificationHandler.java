package com.dtz.plugins.azurehubnotification;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.dtz.R; //for dev testing
//import com.dtz.au.R; //for au prod
//import com.dtz.sg.R; //for sg prod

import com.microsoft.windowsazure.notifications.NotificationsHandler;

public class NotificationHandler extends NotificationsHandler {
	public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "NotificationHandler";
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context ctx;

	@Override
	public void onReceive(Context context, Bundle bundle) {
		ctx = context;
		/*
		 * { "WorkOrderNumber":"WO91911", "Status":"Active",
		 * "Message":"Successful", "IsNewWorkOrder": true, "IsCompleted": false,
		 * "TrafficLight": "red" }
		 */
		String message = bundle.getString("msg");
		Log.d(TAG, "Receive new notification message from GCM/Azure HUB : " + message);
		JSONObject notifiactionJsonObject = null;
		try {
			notifiactionJsonObject = new JSONObject(message);
			String workOrderNumber = notifiactionJsonObject.getString("WorkOrderNumber");
			String status = notifiactionJsonObject.getString("Status");
			String messageText = notifiactionJsonObject.getString("Message");
			boolean isNewWorkOrder = notifiactionJsonObject.getBoolean("IsNewWorkOrder");
			boolean isCompleted = notifiactionJsonObject.getBoolean("IsCompleted");
			String trafficLight = notifiactionJsonObject.getString("TrafficLight");
			sendNotification(workOrderNumber, status, messageText, isNewWorkOrder, isCompleted, trafficLight);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void sendNotification(String workOrderNumber, String status, String messageText, boolean isNewWorkOrder, boolean isCompleted, String trafficLight) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		long timeInMillis = Calendar.getInstance().getTimeInMillis();
		Intent resultIntent = new Intent();
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		resultIntent.setAction("com.dtz.cordova.VIEW_WORK_ORDER");
		resultIntent.putExtra("WORK_ORDER_NO", workOrderNumber);
	
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, (int)timeInMillis, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		String contentText = "";

		// large icon for notification,normally use App icon
		Bitmap largeIcon=BitmapFactory.decodeResource(ctx.getResources(), R.drawable.icon);
		
		if (messageText.equalsIgnoreCase("failed")) {
			contentText = "Failed to update the WorkOrder" + workOrderNumber;
			largeIcon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_failed);			
		} else if (!messageText.equalsIgnoreCase("successful") && messageText.length() > 0) {
			contentText = "Failed to update the WorkOrder: " + workOrderNumber + " " + messageText;
			largeIcon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_failed);			
		} else {
			if (isNewWorkOrder) {
				contentText = "A new WorkOrder is assigned to you";
			} else if (isCompleted) {
				contentText = "WorkOrder is successfully closed";
			} else {
				contentText = "WorkOrder is successfully updated to new status: " + status;
			}
			largeIcon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_success);			
		}
		
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
		.setWhen(timeInMillis)
		.setSmallIcon(R.drawable.icon)
		.setLargeIcon(largeIcon)
		.setTicker(contentText)
		.setContentTitle(workOrderNumber)
		.setAutoCancel(true)
		.setContentText(contentText)
		.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
		.setContentIntent(contentIntent);
		
		
		//this will apply to API levels that are later than API LEVEL 16
		NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();  
        bigText.bigText(contentText);  
        bigText.setBigContentTitle(workOrderNumber);  
        bigText.setSummaryText("From : DTZ ");  
        mBuilder.setStyle(bigText);  
        
      
				
		Notification notification = mBuilder.build();
		mNotificationManager.notify((int)timeInMillis, notification);
	}
}
