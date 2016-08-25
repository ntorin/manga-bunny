package com.fruits.ntorin.mango.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.sourcefns.Sources;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;


@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "https://ntorikn.cloudant.com/acra-app/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "veryindencredstaindevere",
        formUriBasicAuthPassword = "740fabb9fb863f7ac3d5cbd281e555363aadbe5f"
        // Your usual ACRA configuration
)
public class LaunchActivity extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(base);
        int defaultSourcePref = Integer.parseInt(sharedPreferences.getString(Settings.PREF_DEFAULT_SOURCE, "4"));
        Sources.setSelectedSource(defaultSourcePref);
        //Log.d("LaunchActivity", "init source completed");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("LaunchActivity", "onCreate started");
    }
}
