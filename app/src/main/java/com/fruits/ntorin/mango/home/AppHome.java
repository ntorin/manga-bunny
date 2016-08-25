package com.fruits.ntorin.mango.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.fruits.ntorin.mango.About;
import com.fruits.ntorin.mango.Help;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.dummy.DummyContent;
import com.fruits.ntorin.mango.home.directory.DirectoryFragment;
import com.fruits.ntorin.mango.home.downloads.DownloadsFragment;
import com.fruits.ntorin.mango.home.explore.ExploreFragment;
import com.fruits.ntorin.mango.home.favorites.FavoritesFragment;
import com.fruits.ntorin.mango.home.history.HistoryFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.acra.ACRA;

import java.util.ArrayList;

//import com.google.cloud.datastore.Datastore;
//import com.google.cloud.datastore.DatastoreOptions;

/*@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "https://ntorikn.cloudant.com/acra-app/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "veryindencredstaindevere",
        formUriBasicAuthPassword = "740fabb9fb863f7ac3d5cbd281e555363aadbe5f"
        // Your usual ACRA configuration
)*/

/*@ReportsCrashes(mailTo = "ntorikn@gmail.com",
        customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)*/
public class AppHome extends AppCompatActivity
        implements DirectoryFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener,
        ExploreFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        DownloadsFragment.OnFragmentInteractionListener,
        SettingsDialogFragment.SettingsDialogListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Fragment mFragmentCalled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ACRA.init(getApplication());
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_reader, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_home, false);
        setContentView(R.layout.activity_app_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.titlestrip);
        //toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //Log.d("adblock", "started");
        MobileAds.initialize(this, getString(R.string.firebase_app_id));

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                /* .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("2031648F3AF51C33AE4F2672E64F95F5") */
                .build();
        adView.loadAd(request);

        //Log.d("adblock", "ended");


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.container);
            if (mViewPager != null) {
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mViewPager.setOffscreenPageLimit(10);
            }



        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }




        tabLayout.getTabAt(0).setIcon(imageResId[0]);
        tabLayout.getTabAt(1).setIcon(imageResId[1]);
        tabLayout.getTabAt(2).setIcon(imageResId[3]);
        tabLayout.getTabAt(3).setIcon(imageResId[4]);
        //tabLayout.getTabAt(4).setIcon(imageResId[4]);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int startupTab = Integer.parseInt(sharedPreferences.getString(Settings.PREF_STARTUP_TAB, "0"));
        int notification = getIntent().getIntExtra("tab", -1);
        if(notification != -1){
            mViewPager.setCurrentItem(notification);
        }else {
            mViewPager.setCurrentItem(startupTab);
        }


    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, Settings.class);
                startActivity(settingsIntent);
                return true;
            case R.id.help:
                Intent helpIntent = new Intent(this, Help.class);
                startActivity(helpIntent);
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(this, About.class);
                startActivity(aboutIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //@Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onItemClick(SettingsDialogFragment dialog) {
        String sortBy = dialog.getSortBy().getText().toString();
        String pickSource = dialog.getPickSource().getText().toString();
        String completion = dialog.getCompletion().getText().toString();
        CheckBox[] genres = dialog.getGenres();
        ArrayList<String> checkedGenres = new ArrayList<>();
        for (CheckBox genre : genres) {
            if (genre.isChecked()) {
                checkedGenres.add(genre.getText().toString());
            }
        }
        DirectoryFragment directoryFragment = (DirectoryFragment) mFragmentCalled;
        directoryFragment.setPickSourceRadioID(dialog.getPickSource().getId());
        directoryFragment.setSortByRadioID(dialog.getSortBy().getId());
        directoryFragment.requeryFromConfigure(sortBy, pickSource, checkedGenres, completion);
    }

    public void setFragmentCalled(Fragment f){
        mFragmentCalled = f;
    };

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_app_home, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public void SearchSettings(View view){

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).


            switch (position) {
                case 0:
                    return DirectoryFragment.newInstance();
                case 1:
                    return FavoritesFragment.newInstance("", "");
                //case 2:
                //    return ExploreFragment.newInstance("", "");
                case 2:
                    return HistoryFragment.newInstance("", "");
                case 3:
                    return DownloadsFragment.newInstance("", "");
            }
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "";
                case 1:
                    return "";
                case 2:
                    return "";
                case 3:
                    return "";
                case 4:
                    return "";
            }
            return null;
        }

        }

    private int[] imageResId = {
            R.drawable.ic_book_white_24dp,
            R.drawable.ic_favorite_white_24dp,
            R.drawable.ic_explore_white_24dp,
            R.drawable.ic_history_white_24dp,
            R.drawable.ic_file_download_white_24dp
    };

}