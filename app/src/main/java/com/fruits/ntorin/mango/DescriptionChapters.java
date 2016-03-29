package com.fruits.ntorin.mango;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fruits.ntorin.mango.dummy.DummyContent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DescriptionChapters extends AppCompatActivity
        implements DescriptionFragment.OnFragmentInteractionListener,
        ChaptersFragment.OnListFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    DescriptionFragment descriptionFragment;
    ChaptersFragment chaptersFragment;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_chapters);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);



        Intent intent = this.getIntent();
        setTitle(intent.getStringExtra("title"));
        new AsyncFetchTitle(intent.getStringExtra("href")).execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_description_chapters, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class AsyncFetchTitle extends AsyncTask<Void, ProgressUpdate, Void>{


        String href;
        String description;
        Map<String, Chapter> chMap;

        public AsyncFetchTitle(String href){
            super();
            this.href = href;
        }

        @Override
        protected Void doInBackground(Void... params) { //// FIXME: 3/15/2016 
            try{
                Document document = Jsoup.connect(href).get();
                Log.d("c", "connected to " + href);
                Elements summary = document.getElementsByClass("summary");
                Elements chapters = document.getElementsByClass("tips"); // a class that's similar to DummyItem, but stores chapter info
                int ch = 1;
                chMap = new HashMap<String, Chapter>();
                for (Element element : chapters){
                    Log.d("t", element.attr("href"));
                    chMap.put(String.valueOf(ch), new Chapter(element.text(), element.attr("href")));
                    ch++;
                }
                Log.d("s", "setting text");
                    //descriptionFragment.setText(element.text());
                    publishProgress(new ProgressUpdate(summary.first().text(), chMap));

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final ProgressUpdate... progress){
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    descriptionFragment.setText(progress[0].description);
                    Log.d("s", "set text");
                    chaptersFragment.setAdapter(progress[0].map);

                }
            });
        }

        @Override
        protected void onPostExecute(Void result){

        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(Chapter item) {
        Intent intent = new Intent(this, ChapterReader.class);
        Bundle bundle = new Bundle();
        bundle.putString("href", item.content);
        intent.putExtras(bundle);
        Log.d("toChapterReader", item.content);
        startActivity(intent);
    }

    class ProgressUpdate{
        public final String description;
        public final Map<String, Chapter> map;

        public ProgressUpdate(String description, Map<String, Chapter> map){
            this.description = description;
            this.map = map;
        }

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
            switch(position){
                case 0:
                    descriptionFragment = DescriptionFragment.newInstance(position + 1);
                    return descriptionFragment;
                case 1:
                    chaptersFragment = ChaptersFragment.newInstance(position + 1);
                    return chaptersFragment;
            }

            return DescriptionFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show x total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "DESCRIPTION";
                case 1:
                    return "CHAPTERS";
                case 2:
                    return "RECOMMENDED";
            }
            return null;
        }
    }
}
