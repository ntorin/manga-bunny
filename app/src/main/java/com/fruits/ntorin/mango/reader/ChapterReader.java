package com.fruits.ntorin.mango.reader;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.fruits.ntorin.mango.utils.CenteredToolbar;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.utils.RetainFragment;
import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.packages.ChangeChaptersPackage;
import com.fruits.ntorin.mango.sourcefns.GoodMangaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaFoxFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaHereFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaInnFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaPandaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaReaderFunctions;
import com.fruits.ntorin.mango.sourcefns.Sources;
import com.fruits.ntorin.mango.title.Chapter;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChapterReader extends AppCompatActivity implements
        PageFragment.OnFragmentInteractionListener,
        BookmarksDialogFragment.BookmarksDialogListener,
        PageJumpDialogFragment.PageJumpDialogListener {

    public Uri[] mBitmapURIs;
    public ArrayList<AsyncTask> pageLoaders;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private int mPages;
    private String mHref;
    private String[] mPageURLs;
    private boolean backFlag = false;
    private int nextCh;
    private int prevCh = 0;
    private int pgfrags;
    private int pgno = -1;
    private ReversedSectionsPagerAdapter mReversedSectionsPagerAdapter;
    private ViewPager mReversedViewPager;
    private LruCache<String, Bitmap> mMemoryCache;
    private Set<SoftReference<Bitmap>> mReusableBitmaps;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_reader);
        //final RelativeLayout maincontent = (RelativeLayout) findViewById(R.id.main_content);
        //maincontent.setLayerType(View.LAYER_TYPE_SOFTWARE, null); //for disabling hardware acceleration

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;



        RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(getFragmentManager());
        mMemoryCache = retainFragment.mRetainedCache;
        mReusableBitmaps = retainFragment.mReusableBitmaps;
        if (mMemoryCache == null) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

                @Override
                protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                    //Log.d("ChapterReaderLRU", "entry removed: " + key);
                    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                    ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    activityManager.getMemoryInfo(mi);
                    int usedMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
                    //oldValue.recycle();
                    //oldValue = null;
                    mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue));
                    /*Log.d("ChapterReaderLRU", "cache specs; maxsize: " + mMemoryCache.maxSize() + "currsize: "
                            + mMemoryCache.size() + "freemem: " +
                             usedMemory + "totalmem: " + maxMemory); */
                    if(usedMemory >= maxMemory * 0.55){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                    //System.gc();
                }

                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
            retainFragment.mRetainedCache = mMemoryCache;

        }

        if (mReusableBitmaps == null){
            mReusableBitmaps =
                    Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
            retainFragment.mReusableBitmaps = mReusableBitmaps;
        }



        Intent intent = this.getIntent();
        mPages = intent.getIntExtra("pages", 20);
        //mPages += 1;
        mHref = intent.getStringExtra("href");
        mPageURLs = intent.getStringArrayExtra("pageURLs");
        mBitmapURIs = new Uri[mPages];
        //Log.d("ChapterReader", "" + mPages);

        nextCh = mPages - 1;

        Drawable actionBarBackground = new ColorDrawable(Color.parseColor("#FF0000"));
        actionBarBackground.setAlpha(65);


        Toolbar bottomToolbar = (Toolbar) findViewById(R.id.toolbar_reader_bottom);
        bottomToolbar.inflateMenu(R.menu.menu_chapter_reader);
        bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_all_bookmarks:
                        ShowAllBookmarks();
                        return true;
                    case R.id.action_page_jump:
                        //toggleUI();
                        PageJump();
                        return true;
                    default:
                        return false;
                }
            }
        });


        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Muli.ttf");
        Toolbar topToolbar = (Toolbar) findViewById(R.id.toolbar_reader_top);
        TextView titleText = (TextView) findViewById(R.id.title_text);
        titleText.setText(intent.getStringExtra("title"));
        //titleText.setTypeface(tf);
        //topToolbar.setTitle(intent.getStringExtra("title"));

        //topToolbar.inflateMenu(R.menu.menu_chapter_reader);

        //AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        //appBarLayout.bringToFront();
        CenteredToolbar actionBar = (CenteredToolbar) findViewById(R.id.toolbar);
        actionBar.bringToFront();
        //actionBar.setTitle(intent.getStringExtra("title"));
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setBackgroundDrawable(actionBarBackground);
        //getSupportActionBar().setStackedBackgroundDrawable(actionBarBackground);
        //getSupportActionBar().hide();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(Settings.PREF_SCREEN_TIMEOUT, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        /*int orientation = Integer.parseInt(sharedPreferences.getString(Settings.PREF_READER_ORIENTATION, "0"));
        switch (orientation) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (Build.VERSION.SDK_INT >= 23) {
                //window.setStatusBarColor(ContextCompat.getColor(getBaseContext(),android.R.color.transparent));
                //window.setNavigationBarColor(ContextCompat.getColor(getBaseContext(), android.R.color.transparent));
            } else {
                //window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
                //window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
            }
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment f : fragmentList) {
                //Log.d("ListFragments", "" + f.toString());

            }
        }
        //mListView = (ListView) findViewById(R.id.vertical_container);
        //mListView.setAdapter(new StripAdapter(mBitmapURIs));
        /*mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int firstvisibleitem;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //Log.d("scrollstate", "scrolling");
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(this.firstvisibleitem != firstVisibleItem){
                    this.firstvisibleitem = firstVisibleItem;
                    if(mViewPager.getAdapter() != null){
                        mViewPager.setCurrentItem(firstVisibleItem);
                    }else if(mReversedViewPager.getAdapter() != null){
                        mReversedViewPager.setCurrentItem(mPages - firstVisibleItem - 1);
                    }
                }
                //Log.d("onscroll", "firstvisibleitem: " + firstVisibleItem + ", visibleitemcount: " + visibleItemCount + ", totalitemcount: " + totalItemCount);
            }
        }); */
        //mListView.setAdapter(mSectionsPagerAdapter);

        mReversedSectionsPagerAdapter = new ReversedSectionsPagerAdapter(getSupportFragmentManager());
        mReversedViewPager = (ViewPager) findViewById(R.id.reverse_container);
        mReversedViewPager.setAdapter(mReversedSectionsPagerAdapter);
        mReversedViewPager.setCurrentItem(nextCh);
        mReversedViewPager.setVisibility(View.GONE);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //mViewPager.setAdapter(null);
        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

            int state;
            int indicator;
            boolean chflag = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffsetPixels == 0 && state != 2) {
                    indicator++;
                } else {
                    if (indicator > 0) {
                        indicator--;
                    }
                }

                if (indicator > 2) {
                    if (indicator <= 115) {
                        getWindow().getDecorView().setBackgroundColor(Color.argb(255, 0, (int) (indicator * 0.73), indicator));
                    } else {
                        getWindow().getDecorView().setBackgroundColor(Color.argb(255, 0, (int) (115 * 0.73), 115));
                    }
                } else {
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(ChapterReader.this, R.color.readerBackground));
                }
                //Log.d("onPageScrolled", "" + indicator);
                ////Log.d("onPageScrolled", "position: " + position + " positionOffset: " + positionOffset + " positionOffsetPixels: " + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                //Log.d("onPageSelected", "position: " + position);
                TextView currentPage = (TextView) findViewById(R.id.page_number);
                if (mViewPager.getAdapter() != null) {
                    currentPage.setText("" + (mViewPager.getCurrentItem() + 1) + "/" + mPages);
                } else if (mReversedViewPager.getAdapter() != null) {
                    currentPage.setText("" + (mPages - mReversedViewPager.getCurrentItem()) + "/" + mPages);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                this.state = state;
                if (state == 0) {
                    if (indicator > 2) {
                        chflag = true;
                    }
                    indicator = 0;
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(ChapterReader.this, R.color.readerBackground));
                }
                //Log.d("onPageScrollState", "state: " + state);
                if (chflag) {
                    chflag = false;
                    if (mViewPager.getCurrentItem() == prevCh && mViewPager.getAdapter() != null) {
                        //Log.d("prevch", "mviewpager adapter is not null");
                        toPrevChapter();
                    }

                    if (mReversedViewPager.getCurrentItem() == nextCh && mReversedViewPager.getAdapter() != null) {
                        //Log.d("prevch", "mreversedviewpager adapter is not null");
                        toPrevChapter();
                    }

                    if (mViewPager.getCurrentItem() == nextCh && mViewPager.getAdapter() != null) {
                        //Log.d("nextch", "mviewpager adapter is not null");
                        toNextChapter();
                    }

                    if (mReversedViewPager.getCurrentItem() == prevCh && mReversedViewPager.getAdapter() != null) {
                        //Log.d("nextch", "mreversedviewpager adapter is not null");
                        toNextChapter();
                    }
                }
            }
        };
        mViewPager.addOnPageChangeListener(pageChangeListener);
        mReversedViewPager.addOnPageChangeListener(pageChangeListener);
        //mViewPager.setOffscreenPageLimit(3);
        if (getIntent().getBooleanExtra("backflag", false)) {
            mViewPager.setCurrentItem(prevCh);
            mReversedViewPager.setCurrentItem(0);
        }
        if(!(getIntent().getBooleanExtra("offline", false))){
            mViewPager.setOffscreenPageLimit(3);
            mReversedViewPager.setOffscreenPageLimit(3);
        }
        //Log.d("ChapterReader", "onCreate finished");

        TextView currentPage = (TextView) findViewById(R.id.page_number);
        if (mViewPager.getAdapter() != null) {
            currentPage.setText("" + (mViewPager.getCurrentItem() + 1) + "/" + mPages);
        } else {
            currentPage.setText("" + (mPages - mReversedViewPager.getCurrentItem()) + "/" + mPages);
        }
        currentPage.setTypeface(tf);


        ViewPager.OnPageChangeListener alterPageNumber = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                TextView currentPage = (TextView) findViewById(R.id.page_number);
                if (mViewPager.getAdapter() != null) {
                    currentPage.setText("" + (mViewPager.getCurrentItem() + 1) + "/" + mPages);
                } else {
                    //currentPage.setText("" + mReversedViewPager.getCurrentItem() + 1 + "/" + mPages);
                    currentPage.setText("" + (mPages - mReversedViewPager.getCurrentItem()) + "/" + mPages);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        mViewPager.addOnPageChangeListener(alterPageNumber);
        mReversedViewPager.addOnPageChangeListener(alterPageNumber);

        int readingDirection = Integer.parseInt(sharedPreferences.getString(Settings.PREF_READER_DIRECTION, "0"));
        switch (readingDirection) {
            case 0:
                ReadWestern();
                break;
            case 1:
                ReadEastern();
                break;
            case 2:
                ReadVerticalStrip();
                break;
        }


        int pgno = getIntent().getIntExtra("pgno", -1);
        //Log.d("ChapterReader", "pageno " + pgno);
        if (pgno != -1) {
            mViewPager.setCurrentItem(pgno - 1);
            mReversedViewPager.setCurrentItem(mPages - pgno - 1);
            //Log.d("ChapterReader", "page changed to" + pgno);
        }

        Toolbar navToolbar = (Toolbar) findViewById(R.id.toolbar_navigation);
        navToolbar.inflateMenu(R.menu.menu_reader_navigation);
        navToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_prev_chapter:
                        if (mViewPager.getAdapter() != null) {
                            //Log.d("prevch", "mviewpager adapter is not null");
                            toPrevChapter();
                        } else {
                            //Log.d("nextch", "mviewpager adapter is null");
                            toNextChapter();
                        }
                        break;
                    case R.id.action_next_chapter:
                        if (mViewPager.getAdapter() != null) {
                            //Log.d("nextch", "mviewpager adapter is not null");
                            toNextChapter();
                        } else {
                            //Log.d("prevch", "mviewpager adapter is null");
                            toPrevChapter();
                        }
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });
    }

    @Override
    public void onPageUpdate() {
        /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StripAdapter stripAdapter = new StripAdapter(mBitmapURIs);
                mListView.setAdapter(stripAdapter);
            }
        }); */
    }


    @Override //TODO: enable once options are configurable
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (sharedPreferences.getBoolean(Settings.PREF_VOLUME_NAVIGATION, false)) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (mViewPager.getAdapter() != null) {
                        if (mViewPager.getCurrentItem() == prevCh) {
                            toPrevChapter();
                        } else {
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                        }
                    } else {
                        if (mReversedViewPager.getCurrentItem() == nextCh) {
                            toPrevChapter();
                        } else {
                            mReversedViewPager.setCurrentItem(mReversedViewPager.getCurrentItem() + 1);
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (mViewPager.getAdapter() != null) {
                        if (mViewPager.getCurrentItem() == nextCh) {
                            toNextChapter();
                        } else {
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        }
                    } else {
                        if (mReversedViewPager.getCurrentItem() == prevCh) {
                            toNextChapter();
                        } else {
                            mReversedViewPager.setCurrentItem(mReversedViewPager.getCurrentItem() - 1);
                        }
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        //Log.d("ChapterReader", "destroyed");
        EndLoaders();
        SetCheckpoint();

        super.onDestroy();
    }

    private void SetCheckpoint() {
        int[] checkpoint = new int[2];
        checkpoint[0] = getChapterNumber();
        checkpoint[1] = mViewPager.getCurrentItem() + 1;
        //Log.d("SetCheckpoint", "chapter " + checkpoint[0] + " page " + checkpoint[1]);

        try {
            FileOutputStream fos;
            if (getIntent().getBooleanExtra("offline", false)) {
                fos = openFileOutput(getIntent().getStringExtra("mangatitle") + "checkpoint_OFFLINE", Context.MODE_PRIVATE);
            } else {
                fos = openFileOutput(getIntent().getStringExtra("mangatitle") + "checkpoint", Context.MODE_PRIVATE);
            }
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(checkpoint);

            fos.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PageJump() {
        DialogFragment dialog = new PageJumpDialogFragment();

        Bundle bundle;
        if (getIntent().getBooleanExtra("offline", false)) {
            bundle = new Bundle();
            bundle.putSerializable("pagelist", getIntent().getSerializableExtra("pagelist"));
            bundle.putSerializable("chlist", getIntent().getSerializableExtra("chlist"));
            bundle.putBoolean("offline", true);
        } else {
            bundle = new Bundle();
            bundle.putSerializable("bitmapuris", mBitmapURIs);
            bundle.putSerializable("chlist", getIntent().getSerializableExtra("chlist"));
            //Log.d("PageJump", "" + getIntent().getSerializableExtra("chlist"));
        }

        dialog.setArguments(bundle);
        dialog.show(this.getFragmentManager(), "PageJumpDialog");
    }

    private void ShowAllBookmarks() {

        DialogFragment dialog = new BookmarksDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString("mangatitle", getIntent().getStringExtra("mangatitle"));
        bundle.putInt("chno", getChapterNumber());
        if (getIntent().getBooleanExtra("offline", false)) {
            bundle.putBoolean("offline", true);
        }

        dialog.setArguments(bundle);
        dialog.show(this.getFragmentManager(), "BookmarksDialog");
        ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chapter_reader_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (id) {
            case R.id.action_left_to_right:
                editor.putString(Settings.PREF_READER_DIRECTION, "0");
                editor.apply();
                ReadWestern();
                return true;
            case R.id.action_right_to_left:
                editor.putString(Settings.PREF_READER_DIRECTION, "1");
                editor.apply();
                ReadEastern();
                return true;
            /* case R.id.action_top_to_bottom:
                editor.putString(Settings.PREF_READER_DIRECTION, "2");
                editor.apply();
                ReadVerticalStrip();
                return true; */
            case R.id.action_volnav_on:
                editor.putBoolean(Settings.PREF_VOLUME_NAVIGATION, true);
                editor.apply();
                return true;
            case R.id.action_volnav_off:
                editor.putBoolean(Settings.PREF_VOLUME_NAVIGATION, false);
                editor.apply();
                return true;
            case R.id.action_timeout_on:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                editor.putBoolean(Settings.PREF_SCREEN_TIMEOUT, true);
                editor.apply();
                return true;
            case R.id.action_timeout_off:
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                editor.putBoolean(Settings.PREF_SCREEN_TIMEOUT, false);
                editor.apply();
                return true;

            /*case R.id.action_default_orientation:
                editor.putString(Settings.PREF_READER_ORIENTATION, "0");
                editor.apply();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                return true;
            case R.id.action_lock_portrait:
                editor.putString(Settings.PREF_READER_ORIENTATION, "1");
                editor.apply();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            case R.id.action_lock_landscape:
                editor.putString(Settings.PREF_READER_ORIENTATION, "2");
                editor.apply();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ReadWestern() {
        //Log.d("ReadWestern", "changing");
        mReversedViewPager.setAdapter(null);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        if (getIntent().getBooleanExtra("backflag", false)) {
            mViewPager.setCurrentItem(nextCh);
        } else {
            mViewPager.setCurrentItem(0);
        }
        mReversedViewPager.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
        //mListView.setVisibility(View.GONE);
        TextView currentPage = (TextView) findViewById(R.id.page_number);
        currentPage.setText("" + (mViewPager.getCurrentItem() + 1) + "/" + mPages);

    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d("onActivityResult", "starting");
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                File cropDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MangaBunnyCrops");
                if (cropDir.mkdirs()) {
                    //Log.d("onActivityResult", "created folder " + cropDir.getName());
                }
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri uri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    File crop = new File(cropDir, getIntent().getStringExtra("title") + "_" + getDateTime() + ".png");
                    FileOutputStream fos = new FileOutputStream(crop);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    if (!crop.exists()) {
                        if (crop.createNewFile()) {
                            //Log.d("onActivityResult", "new crop created: " + crop.getName());
                        }
                    }
                    Toast t = Toast.makeText(this, "Crop successful", Toast.LENGTH_SHORT);
                    t.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            //CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Exception e = result.getError();
            //e.printStackTrace();
        }
    }

    private void ReadEastern() {
        //Log.d("ReadEastern", "changing");
        mReversedViewPager.setAdapter(mReversedSectionsPagerAdapter);
        if (getIntent().getBooleanExtra("backflag", false)) {
            mReversedViewPager.setCurrentItem(0);
        } else {
            mReversedViewPager.setCurrentItem(nextCh);
        }
        mViewPager.setAdapter(null);
        mReversedViewPager.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.GONE);
        //mListView.setVisibility(View.GONE);

        TextView currentPage = (TextView) findViewById(R.id.page_number);
        currentPage.setText("" + (mPages - mReversedViewPager.getCurrentItem()) + "/" + mPages);
        //currentPage.setText("" + (mReversedViewPager.getCurrentItem() - mPages + 1) + "/" + mPages);
    }

    private void ReadVerticalStrip() {
        /*if(mViewPager.getAdapter() == null){
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }
        mViewPager.setVisibility(View.INVISIBLE);
        mReversedViewPager.setVisibility(View.INVISIBLE);
        mListView.setVisibility(View.VISIBLE); */
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onChapterCalled(int direction) {
        //Log.d("onChapterCalled", "starting");
        if (mViewPager.getCurrentItem() == prevCh && direction == 0 && mViewPager.getAdapter() != null) {
            toPrevChapter();
        } else if (mViewPager.getCurrentItem() == nextCh && direction == 1 && mViewPager.getAdapter() != null) {
            toNextChapter();
        }

        if (mReversedViewPager.getCurrentItem() == nextCh && direction == 0 && mReversedViewPager.getAdapter() != null) {
            toNextChapter();
        } else if (mReversedViewPager.getCurrentItem() == prevCh && direction == 1 && mReversedViewPager.getAdapter() != null) {
            toPrevChapter();
        }
    }

    @Override
    public void onToggleUI() {
        toggleUI();
    }

    @Override
    public void onItemClick(BookmarksDialogFragment dialog, int chno, int pgno) {
        if (getIntent().getBooleanExtra("offline", false)) {
            if (chno == getIntent().getIntExtra("chno", 0)) {
                mViewPager.setCurrentItem(pgno);
            } else {
                this.pgno = pgno;
                OfflineChangeChapters(-getIntent().getIntExtra("chno", 0) + chno);
            }
        } else {
            HashMap<String, Chapter> chlist = (HashMap) getIntent().getSerializableExtra("chlist");
            int totalChapters = chlist.keySet().size();
            if (chno == getChapterNumber()) {
                mViewPager.setCurrentItem(pgno);
            } else {
                Chapter chapter = chlist.get(Integer.toString(totalChapters - chno + 1));

                //Log.d("PageJumpDialogFragment", "" + (totalChapters - chno + 1));

                this.pgno = pgno;

                ChangeChapters(chapter, chno);


            }
        }
        //Log.d("BookmarksDialogFragment", "item clicked " + chno + " chapternumber() " + getChapterNumber() + " " + pgno);
        dialog.dismiss();
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN) {

            //toggleUI();
        }

        if (mViewPager.getCurrentItem() == prevCh) {
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    //Log.d("ChapterReaderMotion", "ACTION_DOWN");
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    mTouchX = MotionEventCompat.getX(event, pointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                    //Log.d("ChapterReaderMotion", "" + mTouchX);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    //Log.d("ChapterReaderMotion", "ACTION_MOVE");
                    /*
                    final int pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                    mDragX = MotionEventCompat.getX(event, pointerIndex);
                    //Log.d("ChapterReaderMotion", "" + mDragX);
                    if (mTouchX - mDragX < -300) {
                        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                    } else {
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.readerBackground));
                    }
                    *
                    break;

                }
                case MotionEvent.ACTION_UP: {
                    //Log.d("ChapterReaderMotion", "ACTION_UP");
                    if (mTouchX - mDragX < -300) {
                        toPrevChapter();
                    }
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.readerBackground));
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    mTouchX = 0;
                    mDragX = 0;

                    break;
                }
                case MotionEvent.ACTION_CANCEL: {
                    //Log.d("ChapterReaderMotion", "ACTION_CANCEL");
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    break;
                }
            }
        }

        if (mViewPager.getCurrentItem() == nextCh) {
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    //Log.d("ChapterReaderMotion", "ACTION_DOWN");
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    mTouchX = MotionEventCompat.getX(event, pointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                    //Log.d("ChapterReaderMotion", "" + mTouchX);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    //Log.d("ChapterReaderMotion", "ACTION_MOVE");
                    final int pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                    mDragX = MotionEventCompat.getX(event, pointerIndex);
                    //Log.d("ChapterReaderMotion", "" + mDragX);
                    if (mTouchX - mDragX > 300) {
                        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                    } else {
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.readerBackground));
                    }
                    break;

                }
                case MotionEvent.ACTION_UP: {
                    //Log.d("ChapterReaderMotion", "ACTION_UP");
                    if (mTouchX - mDragX > 300) {
                        toNextChapter();
                    }
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.readerBackground));
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    mTouchX = 0;
                    mDragX = 0;
                    break;
                }
                case MotionEvent.ACTION_CANCEL: {
                    //Log.d("ChapterReaderMotion", "ACTION_CANCEL");
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    mTouchX = 0;
                    mDragX = 0;
                    break;
                }
            }
        }

        return super.onTouchEvent(event);
    }*/

    private void toggleUI() {

        if (getSupportActionBar().isShowing()) {
            getSupportActionBar().hide();
            findViewById(R.id.toolbar_reader_bottom).setVisibility(View.INVISIBLE);
            findViewById(R.id.toolbar_reader_top).setVisibility(View.INVISIBLE);
            findViewById(R.id.toolbar_navigation).setVisibility(View.INVISIBLE);
        } else {
            getSupportActionBar().show();
            findViewById(R.id.toolbar_reader_bottom).setVisibility(View.VISIBLE);
            findViewById(R.id.toolbar_reader_top).setVisibility(View.VISIBLE);
            findViewById(R.id.toolbar_navigation).setVisibility(View.VISIBLE);
        }
    }

    private void toNextChapter() {
        if (getIntent().getBooleanExtra("offline", false)) {
            OfflineChangeChapters(1);
        } else {
            //Log.d("ChapterReader", "forward one chapter");
            HashMap<String, Chapter> map = (HashMap) getIntent().getSerializableExtra("chlist");
            //Log.d("ChapterReaderMotion", "" + map);
            int intkey = getIntent().getIntExtra("chno", -1);
            //Log.d("intkey", "" + intkey);
            String key = "1";
            if (intkey == -1) {
                for (String s : map.keySet()) {
                    //Log.d("search key", "compared " + mHref + " to " + map.get(s).content + " at " + s);
                    if (mHref.equals(map.get(s).content)) {
                        //Log.d("found key", "compared " + mHref + " to " + map.get(s).content + " at " + s);
                        int toparse = Integer.parseInt(s);
                        toparse--;
                        key = Integer.toString(toparse);

                        break;
                    }
                }
            } else {
                key = String.valueOf(--intkey);
            }
            //Log.d("key", " " + key);
            Chapter next = map.get(key);
            //Log.d("ChapterReaderMotion", "" + next + " " + key);
            if (next == null) {
                Toast toast = Toast.makeText(getBaseContext(), "This is the last chapter", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                ChangeChapters(next, intkey);
            }
        }
    }

    private void ChangeChapters(Chapter next, int intkey) {
        EndLoaders();
        new AsyncChangeChapters(next, intkey).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void EndLoaders() {
        if (pageLoaders != null) {
            AsyncTask[] loaders = new AsyncTask[pageLoaders.size()];
            loaders = pageLoaders.toArray(loaders);
            for (int i = 0; i < loaders.length; i++) {
                //Log.d("ChangeChapters", "cancelling " + loaders[i]);
                loaders[i].cancel(true);
            }
        }
    }

    private void toPrevChapter() {
        if (getIntent().getBooleanExtra("offline", false)) {
            backFlag = true;
            OfflineChangeChapters(-1);
        } else {
            //Log.d("ChapterReader", "back one chapter");
            HashMap<String, Chapter> map = (HashMap) getIntent().getSerializableExtra("chlist");
            //Log.d("ChapterReaderMotion", "" + map);
            int intkey = getIntent().getIntExtra("chno", -1);
            String key = "1";
            if (intkey == -1) {
                for (String s : map.keySet()) {
                    //Log.d("search key", "compared " + mHref + " to " + map.get(s).content + " at " + s);
                    if (mHref.equals(map.get(s).content)) {
                        //Log.d("found key", "compared " + mHref + " to " + map.get(s).content + " at " + s);
                        int toparse = Integer.parseInt(s);
                        toparse++;
                        key = Integer.toString(toparse);
                        break;
                    }
                }
            } else {
                key = String.valueOf(++intkey);
            }
            //String key = String.valueOf(++intkey);
            //Log.d("key", " " + key);
            Chapter next = map.get(key);
            //Log.d("ChapterReaderMotion", "" + next + " " + key);
            if (next == null) {
                Toast toast = Toast.makeText(getBaseContext(), "This is the first chapter", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                backFlag = true;
                ChangeChapters(next, intkey);
            }
        }
    }

    private void OfflineChangeChapters(int direction) {
        Intent intent = new Intent(this, ChapterReader.class);
        Bundle bundle = new Bundle();

        File[] chlist = (File[]) getIntent().getSerializableExtra("chlist");
        int chno = getIntent().getIntExtra("chno", 0) - 1;
        if (chno + direction < 0) {
            Toast toast = Toast.makeText(getBaseContext(), "This is the first chapter", Toast.LENGTH_SHORT);
            toast.show();
        } else if (chno + direction > chlist.length - 1) {
            Toast toast = Toast.makeText(getBaseContext(), "This is the last chapter", Toast.LENGTH_SHORT);
            toast.show();
        } else {

            File[] nextch = chlist[chno + direction].listFiles();

            bundle.putSerializable("pagelist", nextch);
            bundle.putSerializable("chlist", getIntent().getSerializableExtra("chlist"));
            bundle.putInt("chno", getIntent().getIntExtra("chno", 0) + direction);
            bundle.putInt("pages", nextch.length);
            bundle.putString("mangatitle", getIntent().getStringExtra("mangatitle"));
            bundle.putString("title", chlist[chno + direction].getName());
            bundle.putBoolean("offline", true);
            bundle.putBoolean("backflag", backFlag);
            if (pgno != -1) {
                bundle.putInt("pgno", pgno + 1);
            }

            intent.putExtras(bundle);

            startActivity(intent);
            new AsyncDeletePages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            finish();
        }
    }

    public int getChapterNumber() { // TODO: 6/15/2016 doesn't work with history
        if (getIntent().getBooleanExtra("offline", false)) {
            return getIntent().getIntExtra("chno", 0);
        } else {
            int intkey = getIntent().getIntExtra("chno", 1);
            HashMap map = (HashMap) getIntent().getSerializableExtra("chlist");
            int totalChapters = map.keySet().size();
            //Log.d("getChapterNumber", "returned " + (totalChapters - intkey + 1));
            return totalChapters - intkey + 1;
        }
    }

    /*@Override
    public int onBookmarkPage(int pgno) {
        if(mViewPager.getAdapter() != null){
            //Log.d("mviewpager", "" + pgno);
            return pgno + 1;
        }else{
            //Log.d("mviewpager", "" + (mPages - pgno) + ", " + pgno);
            return mPages - pgno;
        }
    } */

    @Override
    public void onPageItemClick(PageJumpDialogFragment dialog, int pgno) {
        //Log.d("PageJumpDialogFragment", "page clicked");
        mViewPager.setCurrentItem(pgno);
        mReversedViewPager.setCurrentItem(mPages - pgno - 1);
        dialog.dismiss();
    }

    @Override
    public void onChapterItemClick(PageJumpDialogFragment dialog, int chno) {
        if (getIntent().getBooleanExtra("offline", false)) {
            OfflineChangeChapters(-getIntent().getIntExtra("chno", 0) + chno);
        } else {
            //Log.d("PageJumpDialogFragment", "chapter clicked, chno " + chno + "current ch "
            //        + getChapterNumber() + " chno string " + Integer.toString(chno));

            if (chno != getChapterNumber()) {
                HashMap<String, Chapter> chlist = (HashMap) getIntent().getSerializableExtra("chlist");
                int totalChapters = chlist.keySet().size();
                Chapter chapter = chlist.get(Integer.toString(totalChapters - chno + 1));

                //Log.d("PageJumpDialogFragment", "" + (totalChapters - chno + 1));

                ChangeChapters(chapter, totalChapters - chno + 1);
            }
        }

        dialog.dismiss();
    }

    public class AsyncDeletePages extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            //Log.d("AsyncDeletePages", "starting");
            File[] files = getFilesDir().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith("imgcache");
                }
            });
            for (File f : files) {
                //Log.d("AsyncDeletePages", "deleting " + f.getName());
                f.delete();
            }
            //Log.d("AsyncDeletePages", "ended");
            return null;
        }
    }

    public class AsyncChangeChapters extends AsyncTask<Void, Void, Void> {

        Chapter item;
        int chno;
        ProgressDialog pdialog;

        public AsyncChangeChapters(Chapter item, int chno) {
            super();
            this.item = item;
            this.chno = chno;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdialog = new ProgressDialog(ChapterReader.this);
            pdialog.setMessage("Loading ...");
            pdialog.setCancelable(false);
            pdialog.setCanceledOnTouchOutside(false);
            pdialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast t = Toast.makeText(getBaseContext(), "Unable to connect to pages; check network and try again", Toast.LENGTH_SHORT);
            t.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Elements pages = null;
            String[] pageURLs = new String[0];
            ChangeChaptersPackage p = null;
            switch (Sources.getSelectedSource()) {
                case Sources.MANGAHERE:
                    p = MangaHereFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.GOODMANGA:
                    p = GoodMangaFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAFOX:
                    p = MangaFoxFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAREADER:
                    p = MangaReaderFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAPANDA:
                    p = MangaPandaFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAINN:
                    p = MangaInnFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;

            }
            pages = p.getPages();
            pageURLs = p.getPageURLs();

            /*try {
                Document document = Jsoup.connect(item.content).get();
                Elements li = document.getElementsByAttributeValue("onchange", "change_page(this)");
                pages = li.first().children();
                //Log.d("#pages", "" + (pages.size()));
                //Log.d("nextpageurl", "" + li.first().attr("href"));
                pageURLs = new String[pages.size()];
                int i = 0;
                for (Element option : pages) {
                    pageURLs[i] = option.attr("value");
                    //Log.d("getpages", option.attr("value"));
                    i++;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }*/
            if (pages != null) {
                Intent intent = new Intent(ChapterReader.this, ChapterReader.class);
                Bundle bundle = new Bundle();
                bundle.putString("href", item.content);
                bundle.putString("title", item.id);
                bundle.putStringArray("pageURLs", pageURLs);
                bundle.putInt("pages", pages.size()); //// FIXME: 4/2/2016 possible null issues here
                bundle.putInt("chno", chno);
                bundle.putBoolean("backflag", backFlag);
                bundle.putString("mangatitle", getIntent().getStringExtra("mangatitle"));
                bundle.putSerializable("chlist", getIntent().getSerializableExtra("chlist"));

                if (pgno != -1) {
                    bundle.putInt("pgno", pgno + 1);
                    //Log.d("toChapterReader", "pgno put as" + pgno);
                }

                intent.putExtras(bundle);


                //Log.d("toChapterReader", "title " + item.id + " mangatitle " + getIntent().getStringExtra("mangatitle"));
                //Log.d("toChapterReader", item.content);
                startActivity(intent);
                finish();
            } else {
                publishProgress();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if ((this.pdialog != null) && this.pdialog.isShowing()) {
                    this.pdialog.dismiss();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                this.pdialog = null;
            }
            new AsyncDeletePages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public class AsyncLoadPages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mPageURLs.length; i++) {
                try {
                    Document document = Jsoup.connect(mPageURLs[i]).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
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
            pgfrags++;
            //Log.d("pgfrags mpages", "pgfrags: " + pgfrags + " mpages: " + mPages);
            if (pgfrags >= mPages - 1) {
                //mViewPager.setOffscreenPageLimit(1);
                //Log.d("getItem", "setting page limit");
            }

            if (getIntent().getBooleanExtra("offline", false)) {
                File[] pages = (File[]) getIntent().getSerializableExtra("pagelist");
                //Log.d("ChapterReader", "" + pages[0].getName());
                Fragment f = PageFragment.newInstance(pages[position], getIntent().getIntExtra("chno", 0),
                        position, getIntent().getStringExtra("mangatitle"), getIntent().getStringExtra("title"),
                        ChapterReader.this);
                //mScrollView.addView(f.getView(), position);
                return f;
            } else {
                Fragment f = PageFragment.newInstance(mPageURLs[position], getChapterNumber(), position + 1,
                        getIntent().getStringExtra("mangatitle"), getIntent().getStringExtra("title"),
                        getIntent().getStringExtra("href"), ChapterReader.this);
                //FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                //fragmentTransaction.add(R.id.vertical_container, f);
                //fragmentTransaction.commit();
                //mLinearLayout.addView(f.getView(), position);
                return f;
            }
        }

        @Override
        public int getCount() {
            // Show mPages total pages.
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

    public class ReversedSectionsPagerAdapter extends FragmentPagerAdapter {

        public ReversedSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //pgfrags++;
            ////Log.d("pgfrags mpages", "pgfrags: " + pgfrags + " mpages: " + mPages);
            if (pgfrags >= mPages - 1) {
                //mViewPager.setOffscreenPageLimit(1);
                //Log.d("getItem", "setting page limit");
            }

            if (getIntent().getBooleanExtra("offline", false)) {
                File[] pages = (File[]) getIntent().getSerializableExtra("pagelist");
                //Log.d("ChapterReader", "" + pages[0].getName());
                Fragment f = PageFragment.newInstance(pages[pages.length - position - 1], getIntent().getIntExtra("chno", 0),
                        position, getIntent().getStringExtra("mangatitle"), getIntent().getStringExtra("title"),
                        ChapterReader.this);
                return f;
            } else {
                Fragment f = PageFragment.newInstance(mPageURLs[mPageURLs.length - position - 1], getChapterNumber(), mPages - position,
                        getIntent().getStringExtra("mangatitle"), getIntent().getStringExtra("title"),
                        getIntent().getStringExtra("href"), ChapterReader.this);
                return f;
            }
        }

        @Override
        public int getCount() {
            // Show mPages total pages.
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

    public class StripAdapter extends BaseAdapter {

        Uri[] bitmapUris;

        public StripAdapter(Uri[] bitmapUris) {
            this.bitmapUris = bitmapUris;
        }

        @Override
        public int getCount() {
            return bitmapUris.length;
        }

        @Override
        public Object getItem(int position) {
            return bitmapUris[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(getBaseContext());
            //imageView.sca(ImageView.ScaleType.FIT_CENTER);
            if (bitmapUris[position] != null) {
                imageView.setImage(ImageSource.uri(bitmapUris[position]));
            } else {
                imageView.setImage(ImageSource.resource(R.drawable.loading_animation));
                //AnimationDrawable d = (AnimationDrawable) imageView.get();
                //d.start();
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleUI();
                }
            });
            return imageView;
        }
    }
}
