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

package com.dtz;

import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.*;

public class DTZMobility extends CordovaActivity 
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	 super.onCreate(savedInstanceState);
         super.init();
         
         String page=Config.getStartUrl();
         Bundle bundle =getIntent().getExtras();
         String action =getIntent().getAction();
         
         if(action!=null && action.equals("com.dtz.cordova.VIEW_WORK_ORDER")) {
         	page=Config.getStartUrl();     	
         	String workOrderId = "";
 			if(bundle!=null){
         	 workOrderId =bundle.getString("WORK_ORDER_NO");
         	}
         	
         	page=page+"?wo="+workOrderId;
         }
         Log.d(TAG, "page : "+page);
         // Set by <content src="index.html" /> in config.xml
         super.loadUrl(page);
         //super.loadUrl("file:///android_asset/www/index.html");
    }
}

