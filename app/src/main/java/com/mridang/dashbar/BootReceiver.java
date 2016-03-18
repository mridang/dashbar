package com.mridang.dashbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Broadcast receiver class to help start the traffic monitoring service when the phone boots up only
 * if the service is enabled.
 */
public class BootReceiver extends BroadcastReceiver {

    /**
     * Receiver method for the phone boot that starts the traffic monitoring service
     */
    @Override
    public void onReceive(Context appContext, Intent bootIntent) {
        Log.i("BootReceiver", "Device booted. Starting service");
        appContext.startService(new Intent(appContext, NotificationService.class));
    }
}