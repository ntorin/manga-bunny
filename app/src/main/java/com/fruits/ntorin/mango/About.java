package com.fruits.ntorin.mango;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("About Manga Bunny");
        TextView aboutText = (TextView) findViewById(R.id.about_text);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Muli.ttf");
        aboutText.setTypeface(tf);

        ImageButton twitterButton = (ImageButton) findViewById(R.id.twitter_button);
        twitterButton.setScaleType(ImageView.ScaleType.FIT_XY);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent twIntent = newTwitterIntent(getPackageManager());
                startActivity(twIntent);
            }
        });

        ImageButton facebookButton = (ImageButton) findViewById(R.id.facebook_button);
        facebookButton.setScaleType(ImageView.ScaleType.FIT_XY);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fbIntent = newFacebookIntent(getPackageManager(), "https://www.facebook.com/Manga-Bunny-571274283054755/");
                startActivity(fbIntent);
            }
        });
    }

    /**
     * <p>Intent to open the official Facebook app. If the Facebook app is not installed then the
     * default web browser will be used.</p>
     *
     * <p>Example usage:</p>
     *
     * {@code newFacebookIntent(ctx.getPackageManager(), "https://www.facebook.com/JRummyApps");}
     *
     * @param pm
     *     The {@link PackageManager}. You can find this class through .
     * @param url
     *     The full URL to the Facebook page or profile.
     * @return An intent that will open the Facebook page/profile.
     */
    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent newTwitterIntent(PackageManager pm){
        // Create intent using ACTION_VIEW and a normal Twitter url:
        String tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s",
                urlEncode("@irisinami #BunnyReader "));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));

        // Narrow down to official Twitter app, if available:
        List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
                intent.setPackage(info.activityInfo.packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        }
        return intent;
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.wtf("urlEncode", "UTF-8 should always be supported", e);
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }
}
