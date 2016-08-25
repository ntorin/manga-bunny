package com.fruits.ntorin.mango;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.fruits.ntorin.mango.home.AppHome;

public class Splash extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); *
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); */
        //Log.d("splash", "setting splash");
        setContentView(R.layout.splashscreen);
        //Log.d("splash", "splash set");

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent(Splash.this, AppHome.class);
                startActivity(intent);
                Splash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}