package com.fruits.ntorin.mango.title;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
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
import android.widget.Button;
import android.widget.TextView;

import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.home.history.HistoryFragment;
import com.fruits.ntorin.mango.reader.ChapterReader;
import com.fruits.ntorin.mango.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
    private Map<String, Chapter> mMap;

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
            Elements summary;
            chMap = new HashMap<String, Chapter>();
            Uri cover;
            TitlePackage titlePackage = DescriptionChaptersSetup.MangahereTitleSetup(href, chMap);

            summary = titlePackage.elements;
            chMap = titlePackage.chapterMap;
            Log.d("check", "" + getIntent().getStringExtra("cover"));
            cover = Uri.parse(getIntent().getStringExtra("cover"));
            String summaryText = "";
            if (summary != null) {
                summaryText = summary.first().text();
            }
            publishProgress(new ProgressUpdate(summaryText, chMap, cover));

            return null;
        }


        @Override
        protected void onProgressUpdate(final ProgressUpdate... progress){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DirectoryDbHelper dbHelper = new DirectoryDbHelper(DescriptionChapters.this);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();

                    Cursor cursor = db.rawQuery("SELECT * FROM " + DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME
                            + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + " =\'"
                            + getIntent().getStringExtra("title") + "\'", null);
                    cursor.moveToFirst();
                    String titleInfo = "";
                    Log.d("count", "" + cursor.getCount());
                    titleInfo += "Author(s): ";
                    titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR)) + "\n\n";
                    titleInfo += "Artist(s): ";
                    titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST)) + "\n\n";
                    titleInfo += "Genre(s): ";
                    titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES)) + "\n\n";
                    titleInfo += "Status: ";
                    titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS)) + "\n\n";
                    titleInfo += "Ranking: ";
                    titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_RANK)) + "\n";



                    descriptionFragment.setText(progress[0].description,  (TextView) findViewById(R.id.description_title_description));
                    descriptionFragment.setText(titleInfo, (TextView) findViewById(R.id.description_title_info));
                    descriptionFragment.setCover(progress[0].cover);
                    Log.d("s", "set text");
                    chaptersFragment.setAdapter(progress[0].map);
                    mMap = progress[0].map;

                }
            });
        }

        @Override
        protected void onPostExecute(Void result){

        }
    }

    private class AsyncGetPages extends AsyncTask<Void, Void, Void>{

        Chapter item;

        public AsyncGetPages(Chapter item){
            super();
            this.item = item;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Elements pages = null;
            String[] pageURLs = new String[0];
            try {
                Document document = Jsoup.connect(item.content).get();
                Elements li = document.getElementsByAttributeValue("onchange", "change_page(this)");
                pages = li.first().children();
                Log.d("#pages", "" + (pages.size()));
                //li = document.getElementsByAttributeValue("class", "btn next_page");
                Log.d("nextpageurl", "" + li.first().attr("href"));
                pageURLs = new String[pages.size()];
                int i = 0;
                for(Element option : pages){
                    pageURLs[i] = option.attr("value");
                    Log.d("getpages", option.attr("value"));
                    i++;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }


            ContentValues values = new ContentValues();
            DirectoryDbHelper dbHelper = new DirectoryDbHelper(getBaseContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, item.id);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, item.content);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_DATE, getDateTime());
            db.delete(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME,
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + item.content + "\'", null);
            db.insert(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME, null, values);
            Log.d("DescriptionChapters", "history time " + getDateTime());

            Intent intent = new Intent(DescriptionChapters.this, ChapterReader.class);
            Bundle bundle = new Bundle();
            bundle.putString("href", item.content);
            bundle.putStringArray("pageURLs", pageURLs);
            bundle.putInt("pages", pages.size()); //// FIXME: 4/2/2016 possible null issues here
            intent.putExtras(bundle);

            Log.d("toChapterReader", item.content);
            startActivity(intent);


            return null;
        }

        /*@Override
        protected void onPostExecute(Void aVoid) {
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    Intent intent = new Intent(DescriptionChapters.this, ChapterReader.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("href", href);
                    intent.putExtras(bundle);

                    Log.d("toChapterReader", href);
                    startActivity(intent);

                }
            });
        }*/
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(Chapter item) {

        /*Intent intent = new Intent(this, ChapterReader.class);
        Bundle bundle = new Bundle();
        bundle.putString("href", item.content);
        intent.putExtras(bundle);*/
        Log.d("toChapterReader", "chapter pressed");
        Log.d("toChapterReader", item.content + " " + item.id);
        new AsyncGetPages(item).execute();

        /*startActivity(intent);*/
    }

    class ProgressUpdate{
        public final String description;
        public final Map<String, Chapter> map;
        public Uri cover;

        public ProgressUpdate(String description, Map<String, Chapter> map, Uri cover){ //// TODO: 4/5/2016 can be changed to a TitlePackage
            this.description = description;
            this.map = map;
            this.cover = cover;
        }

    }

    public void AddToFavorites(View view) {
        Log.d("DescriptionChapters", "AddToFavorites");
        Intent intent = this.getIntent();
        DirectoryDbHelper dbHelper = new DirectoryDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String title = intent.getStringExtra("title");
        String href = intent.getStringExtra("href");

        db.execSQL("INSERT INTO " + DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME +
                " SELECT *, NULL FROM " + DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME +
                " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'");
        ArrayList<Chapter> chapters = new ArrayList<Chapter>();
        int n = 0;
        for (Chapter chapter : mMap.values()) {
            chapters.add(n, chapter);
            n++;
        }
        String chaptersURI = null;
        try {
            FileOutputStream fos = openFileOutput(title + "en", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(chapters);
            chaptersURI = getBaseContext().getFileStreamPath(title + "en").toURI().toString();
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERS, chaptersURI);
        db.update(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME, values,
                DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);
        //db.insert(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME, null, values);
    }

    public void RemoveFromFavorites(View view){
        Log.d("DescriptionChapters", "RemoveFromFavorites");
        Intent intent = this.getIntent();
        DirectoryDbHelper dbHelper = new DirectoryDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String href = intent.getStringExtra("href");

        db.delete(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME,
                DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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
