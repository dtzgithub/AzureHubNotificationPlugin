package com.dtz.plugins.azurehubnotification;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.apache.cordova.LOG;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;

public class AzureConfig {
    
    private static HashMap<String, Object> azureSettings =new HashMap<String, Object>();
    private static AzureConfig azureConfig;
    private static AzureConfig getAzureConfig(Context context) {
        return azureConfig!=null ? azureConfig:new AzureConfig(context) ;       
    }
    
    private   AzureConfig(Context context) {
        if (context == null) {
            LOG.i("CordovaLog", "There is no activity. Is this on the lock screen?");
            return;
        }

        // First checking the class namespace for config_azure_hub.xml
        int id = context.getResources().getIdentifier("config_azure_hub", "xml", context.getClass().getPackage().getName());
        if (id == 0) {
            // If we couldn't find config.xml there, we'll look in the namespace from AndroidManifest.xml
            id = context.getResources().getIdentifier("config_azure_hub", "xml", context.getPackageName());
            if (id == 0) {
                LOG.i("CordovaLog", "config_azure_hub.xml missing. Ignoring...");
                return;
            }
        }

		XmlResourceParser xml = context.getResources().getXml(id);
        int eventType = -1;
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String strNode = xml.getName();

                if (strNode.equals("preference")) {
                    String name = xml.getAttributeValue(null, "name").toLowerCase(Locale.getDefault());
                    /* Java 1.6 does not support switch-based strings
                       Java 7 does, but we're using Dalvik, which is apparently not Java.
                       Since we're reading XML, this has to be an ugly if/else.
                       
                       Also, due to cast issues, each of them has to call their separate putExtra!  
                       Wheee!!! Isn't Java FUN!?!?!?
                       
                       Note: We should probably pass in the classname for the variable splash on splashscreen!
                       */
                    if(name.equalsIgnoreCase("endPoint")) {
                        String value = xml.getAttributeValue(null, "value");
                        azureSettings.put(name, value);
                        LOG.i("CordovaLog", "Found preference for %s=%s", name, value);
                    }
                    else if(name.equalsIgnoreCase("senderId")) {
                        String value = xml.getAttributeValue(null, "value");
                         azureSettings.put(name, value);
                         LOG.i("CordovaLog", "Found preference for %s=%s", name, value);
                    }
                    else if(name.equalsIgnoreCase("notificationHubPath")) {
                        String value = xml.getAttributeValue(null, "value");
                        azureSettings.put(name, value);
                        LOG.i("CordovaLog", "Found preference for %s=%s", name, value);
                    }
                    /*else
                    {
                        String value = xml.getAttributeValue(null, "value");
                        azureSettings.put(name, value);
                        LOG.i("CordovaLog", "Found preference for %s=%s", name, value);
                    }*/
                   
        
                    
                }

            }

            try {
                eventType = xml.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static String getEndPoint(Context context) {
        if(!azureSettings.containsKey("endpoint")) {getAzureConfig(context);}
        return (String) azureSettings.get("endpoint");
    }
    
    public static String getSenderId(Context context) {
        if(!azureSettings.containsKey("senderid")) {getAzureConfig(context);}
        return (String) azureSettings.get("senderid");
    }
    
    public static String getNotificationHubPath(Context context) {
        if(!azureSettings.containsKey("notificationhubpath")) {getAzureConfig(context);}
        return (String) azureSettings.get("notificationhubpath");
    }
    
    
}
