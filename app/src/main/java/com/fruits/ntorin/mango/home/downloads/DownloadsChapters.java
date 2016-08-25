package com.fruits.ntorin.mango.home.downloads;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import com.fruits.ntorin.mango.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class DownloadsChapters extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_chapters);
        setTitle(getIntent().getStringExtra("title"));
        //Log.d("adblock", "started");
         MobileAds.initialize(this, getString(R.string.firebase_app_id));

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                /* .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("2031648F3AF51C33AE4F2672E64F95F5") */
                .build();
        adView.loadAd(request);

        //Log.d("adblock", "ended");

        if(savedInstanceState == null){
            Fragment fragment = FileListFragment.newInstance(getIntent().getStringExtra("dir"), getIntent().getStringExtra("title"));
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.file_list_container, fragment, null).commit();
        }
    }


    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {

        return super.onCreateView(name, context, attrs);
    }
}
