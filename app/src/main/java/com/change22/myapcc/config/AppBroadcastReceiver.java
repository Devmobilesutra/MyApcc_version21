package com.change22.myapcc.config;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.legacy.content.WakefulBroadcastReceiver;

/**
 * Created by Satish on 29-01-2016.
 */
public class AppBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyApp.log("In BroadcastReceiver");
        MyApp.log("Intent Action->"+intent.getAction().toString());
        if(intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE"))
        {
            ComponentName comp = new ComponentName(context.getPackageName(),AppService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
        if(intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION"))
        {
            if(MyApp.get_session(MyApp.SESSION_IS_GCM_REGISTRED_TO_SERVER).equalsIgnoreCase("")) {
                Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
                intent1.putExtra("Flag", "register_gcm");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent1);
                } else {
                    context.startService(intent1);
                }
            }
        }
        else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {
            ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            // Now to check if we're actually connected
            if(cm!=null)
            {
                /*if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                    Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, PalashGCM.class);
                    intent1.putExtra("Flag", "Connection");
                    intent1.putExtra("Status", "ON");
                    context.startService(intent1);
                }*/
                /*else
                {
                    Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, PalashGCM.class);
                    intent1.putExtra("Flag", "UpdateMaster");
                    intent1.putExtra("Status", "OFF");
                    context.startService(intent1);
                }*/
            }
        }

    }
}
