package com.fruits.ntorin.mango.utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Ntori on 7/13/2016.
 */
public class CancelDownloadReceiver extends BroadcastReceiver{


    public static boolean isCancelled;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("CancelDownloadReceiver", "starting");
        String action = intent.getAction();
        if(action.equals("com.fruits.ntorin.mango.STOP_DOWNLOAD")){
            //Log.d("CancelDownloadReceiver", "cancellation sent");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
            isCancelled = true;
        }

    }

}
