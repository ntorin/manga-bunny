package com.fruits.ntorin.mango.reader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.title.Chapter;
import com.fruits.ntorin.mango.title.DescriptionChapters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChapterReader extends AppCompatActivity implements PageFragment.OnFragmentInteractionListener{

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
    private View mView;
    private int mPages;
    private String mHref;
    private String[] mPageURLs;
    private int mActivePointerId = MotionEvent.INVALID_POINTER_ID;
    private Activity mScaleDetector;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mPosY;
    private float mPosX;
    private float mTouchX;
    private float mDragX;
    private Map<String, Chapter> mMap;
    private boolean backFlag = false;

    private int nextCh;
    private int prevCh = 0;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_chapter_reader);


        Intent intent = this.getIntent();

        mPages = intent.getIntExtra("pages", 1);
        mHref = intent.getStringExtra("href");
        mPageURLs = intent.getStringArrayExtra("pageURLs");

        nextCh = mPages - 1;

        Drawable actionBarBackground = new ColorDrawable(Color.parseColor("#FF0000"));
        actionBarBackground.setAlpha(65);


        Toolbar bottomToolbar = (Toolbar) findViewById(R.id.toolbar_reader_bottom);
        bottomToolbar.inflateMenu(R.menu.menu_chapter_reader);
        bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch(id){
                    case R.id.action_left_to_right:
                        return true;
                    case R.id.action_right_to_left:
                        return true;
                    case R.id.action_top_to_bottom:
                        return true;
                    case R.id.action_page_jump:
                        return true;
                    case R.id.action_more_options:
                        return true;
                    default:
                        return false;
                }
            }
        });

        Toolbar topToolbar = (Toolbar) findViewById(R.id.toolbar_reader_top);
        topToolbar.setTitle(intent.getStringExtra("title"));
        //topToolbar.inflateMenu(R.menu.menu_chapter_reader);

        Toolbar actionBar = (Toolbar) findViewById(R.id.toolbar);
        //actionBar.setTitle(intent.getStringExtra("title"));
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setBackgroundDrawable(actionBarBackground);
        //getSupportActionBar().setStackedBackgroundDrawable(actionBarBackground);
        //getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if(Build.VERSION.SDK_INT >= 23){
                //window.setStatusBarColor(ContextCompat.getColor(getBaseContext(),android.R.color.transparent));
                //window.setNavigationBarColor(ContextCompat.getColor(getBaseContext(), android.R.color.transparent));
            }else {
                //window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
                //window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
            }
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        if(getIntent().getBooleanExtra("backflag", false)){
            mViewPager.setCurrentItem(mPages - 1);
        }
        //mViewPager.setOffscreenPageLimit(mPages);
        Log.d("ChapterReader", "onCreate finished");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_chapter_reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_left_to_right:
                return true;
            case R.id.action_right_to_left:
                return true;
            case R.id.action_top_to_bottom:
                return true;
            case R.id.action_page_jump:
                return true;
            case R.id.action_more_options:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

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
            View rootView = inflater.inflate(R.layout.fragment_chapter_reader, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        if(action == MotionEvent.ACTION_DOWN) {
                if (getSupportActionBar().isShowing()) {
                    getSupportActionBar().hide();
                } else {
                    getSupportActionBar().show();
                }
        }

        if(mViewPager.getCurrentItem() == prevCh) {
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    Log.d("ChapterReaderMotion", "ACTION_DOWN");
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    mTouchX = MotionEventCompat.getX(event, pointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                    Log.d("ChapterReaderMotion", "" + mTouchX);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    Log.d("ChapterReaderMotion", "ACTION_MOVE");
                    final int pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                    mDragX = MotionEventCompat.getX(event, pointerIndex);
                    Log.d("ChapterReaderMotion", "" + mDragX);
                    if (mTouchX - mDragX < -300) {
                        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                    } else {
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.readerBackground));
                    }
                    break;

                }
                case MotionEvent.ACTION_UP: {
                    Log.d("ChapterReaderMotion", "ACTION_UP");
                    if (mTouchX - mDragX < -300) {
                        Log.d("ChapterReader", "back one chapter");
                        HashMap<String, Chapter> map = (HashMap) getIntent().getSerializableExtra("chlist");
                        Log.d("ChapterReaderMotion", "" + map);
                        int intkey = getIntent().getIntExtra("chno", 1);
                        String key = String.valueOf(++intkey);
                        Chapter next = map.get(key);
                        Log.d("ChapterReaderMotion", "" + next + " " + key);
                        if(next == null){
                            Toast toast = Toast.makeText(getBaseContext(), "This is the first chapter", Toast.LENGTH_SHORT);
                            toast.show();
                        }else {
                            backFlag = true;
                            new AsyncGetPages(next, intkey).execute();
                        }
                    }
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.readerBackground));
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    mTouchX = 0;
                    mDragX = 0;
                    break;
                }
                case MotionEvent.ACTION_CANCEL: {
                    Log.d("ChapterReaderMotion", "ACTION_CANCEL");
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    break;
                }
            }
        }

        if(mViewPager.getCurrentItem() == nextCh) {
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    Log.d("ChapterReaderMotion", "ACTION_DOWN");
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    mTouchX = MotionEventCompat.getX(event, pointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                    Log.d("ChapterReaderMotion", "" + mTouchX);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    Log.d("ChapterReaderMotion", "ACTION_MOVE");
                    final int pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                    mDragX = MotionEventCompat.getX(event, pointerIndex);
                    Log.d("ChapterReaderMotion", "" + mDragX);
                    if (mTouchX - mDragX > 300) {
                        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                    } else {
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.readerBackground));
                    }
                    break;

                }
                case MotionEvent.ACTION_UP: {
                    Log.d("ChapterReaderMotion", "ACTION_UP");
                    if (mTouchX - mDragX > 300) {
                        Log.d("ChapterReader", "forward one chapter");
                        HashMap<String, Chapter> map = (HashMap) getIntent().getSerializableExtra("chlist");
                        Log.d("ChapterReaderMotion", "" + map);
                        int intkey = getIntent().getIntExtra("chno", 1);
                        String key = String.valueOf(--intkey);
                        Chapter next = map.get(key);
                        Log.d("ChapterReaderMotion", "" + next + " " + key);
                        if(next == null){
                            Toast toast = Toast.makeText(getBaseContext(), "This is the last chapter", Toast.LENGTH_SHORT);
                            toast.show();
                        }else {
                            new AsyncGetPages(next, intkey).execute();
                        }
                    }
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.readerBackground));
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    mTouchX = 0;
                    mDragX = 0;
                    break;
                }
                case MotionEvent.ACTION_CANCEL: {
                    Log.d("ChapterReaderMotion", "ACTION_CANCEL");
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    mTouchX = 0;
                    mDragX = 0;
                    break;
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public int getChapterNumber(){
        int intkey = getIntent().getIntExtra("chno", 1);
        HashMap map = (HashMap) getIntent().getSerializableExtra("chlist");
        int totalChapters = map.keySet().size();
        Log.d("getChapterNumber", "returned " + (totalChapters - intkey + 1));
        return totalChapters - intkey + 1;
    }

    public class AsyncGetPages extends AsyncTask<Void, Void, Void> {

        Chapter item;
        int chno;

        public AsyncGetPages(Chapter item, int chno){
            super();
            this.item = item;
            this.chno = chno;
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


            /*ContentValues values = new ContentValues();
            DirectoryDbHelper dbHelper = new DirectoryDbHelper(getBaseContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, item.id);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, item.content);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_DATE, getDateTime());
            db.delete(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME,
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + item.content + "\'", null);
            db.insert(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME, null, values);
            Log.d("DescriptionChapters", "history time " + getDateTime());*/

            Intent intent = new Intent(ChapterReader.this, ChapterReader.class);
            Bundle bundle = new Bundle();
            bundle.putString("href", item.content);
            bundle.putString("title", item.id);
            bundle.putStringArray("pageURLs", pageURLs);
            bundle.putInt("pages", pages.size()); //// FIXME: 4/2/2016 possible null issues here
            bundle.putInt("chno", chno);
            bundle.putBoolean("backflag", backFlag);
            bundle.putSerializable("chlist", (HashMap) getIntent().getSerializableExtra("chlist"));
            intent.putExtras(bundle);

            Log.d("toChapterReader", item.content);
            startActivity(intent);
            finish();


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
            //Log.d("ChapterReader", "pager position " + position);
            if(position + 1 >= mPages){
                //mViewPager.setOffscreenPageLimit(1);
            }else{
                //Log.d("PageFragment getItem", "not yet," + position + " " + mPages);
            }
            return PageFragment.newInstance(mPageURLs[position], getChapterNumber(), (Toolbar) findViewById(R.id.toolbar_reader_bottom));
        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return mPages;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
