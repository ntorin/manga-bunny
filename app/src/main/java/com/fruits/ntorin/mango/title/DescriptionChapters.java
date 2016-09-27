package com.fruits.ntorin.mango.title;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fruits.ntorin.mango.utils.CancelDownloadReceiver;
import com.fruits.ntorin.mango.utils.CenteredToolbar;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.home.AppHome;
import com.fruits.ntorin.mango.packages.ChangeChaptersPackage;
import com.fruits.ntorin.mango.packages.TitlePackage;
import com.fruits.ntorin.mango.reader.ChapterReader;
import com.fruits.ntorin.mango.sourcefns.GoodMangaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaFoxFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaHereFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaInnFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaPandaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaReaderFunctions;
import com.fruits.ntorin.mango.sourcefns.Sources;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.fruits.ntorin.mango.utils.BitmapFunctions.getBitmapFromURL;

public class DescriptionChapters extends AppCompatActivity
        implements DescriptionFragment.OnFragmentInteractionListener,
        ChaptersFragment.OnListFragmentInteractionListener {

    DescriptionFragment descriptionFragment;
    ChaptersFragment chaptersFragment;
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
    private Map<String, Chapter> mMap;
    private ImageView mLoader;
    private TextView mErrorText;
    private RelativeLayout mLoadingLayout;
    //Title title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //title = (Title) getIntent().getSerializableExtra("titlepack");
        setContentView(R.layout.activity_description_chapters);
        mLoader = (ImageView) findViewById(R.id.loader);
        mErrorText = (TextView) findViewById(R.id.error_text);
        mLoadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //Log.d("adblock", "started");
         /* MobileAds.initialize(this, getString(R.string.firebase_app_id));

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                /* .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("2031648F3AF51C33AE4F2672E64F95F5") *
                .build();
        adView.loadAd(request); */

        //Log.d("adblock", "ended");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        CenteredToolbar bottomToolbar = (CenteredToolbar) findViewById(R.id.toolbar_title_menu);
        bottomToolbar.inflateMenu(R.menu.menu_description_chapters);


        DirectoryDbHelper dbHelper = new DirectoryDbHelper(getBaseContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String verifiedString = getIntent().getStringExtra("title").replace("'", "''");
        Cursor cursor = db.rawQuery("SELECT * FROM " + DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME
                + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + "=\'"
                + verifiedString + "\' AND " + DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE + " = " + Sources.getSelectedSource(), null);
        if (cursor.getCount() > 0) {
            Menu menu = bottomToolbar.getMenu();
            MenuItem item = menu.findItem(R.id.favorite_title);
            item.setIcon(R.drawable.ic_favorite_white_24dp);
        }

        cursor.close();
        db.close();

        bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();

                //noinspection SimplifiableIfStatement
                switch (id) {
                    case R.id.favorite_title:
                        ToggleFavorites(item);
                        return true;
                    case R.id.read_title:
                        ReadFromCheckpoint();
                        return true;
                    case R.id.download_title:
                        if (ContextCompat.checkSelfPermission(DescriptionChapters.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            int i = 0;
                            //Log.d("AsyncDownloadTitle", "need permission");
                            ActivityCompat.requestPermissions(DescriptionChapters.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, i);

                            //Log.d("AsyncDownloadTitle", "asking permission");
                        } else {
                            CreateDownloadDialog();
                        }
                        //Log.d("DownloadTitle", "clicked");
                        return true;
                    case R.id.share_title:
                        CopyURL();
                        return true;
                }

                return false;
            }
        });

        Intent intent = this.getIntent();
        TextView textView = (TextView) findViewById(R.id.toolbar_text);


        //setTitle(intent.getStringExtra("title"));

        new AsyncFetchTitle(intent.getStringExtra("href")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.d("onRequestResult", "starting");
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            CreateDownloadDialog();
        }
    }

    private void CreateDownloadDialog() {
        //Log.d("CreateDownloadDialog", "starting");
        if (ContextCompat.checkSelfPermission(DescriptionChapters.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            final String[] choices = {"Download all chapters", "Download individual chapters"};
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.text_item, choices){

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = getLayoutInflater().inflate(R.layout.chlist_item, parent, false);
                    TextView t = (TextView) v.findViewById(R.id.text);
                    t.setText(getItem(position));
                    return v;
                }
            };

            AlertDialog dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setTitle("Download Options")
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d("CreateDownloadDialog", "" + adapter.getItem(which));
                            if(adapter.getItem(which).equals(choices[0])){
                                ShowDownloadAllDialog();
                            }

                            if(adapter.getItem(which).equals(choices[1])){
                                ShowDownloadIndividualDialog();
                            }
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    private void ShowDownloadIndividualDialog() {
        if(mMap != null) {
            final Chapter[] chapters = new Chapter[mMap.size()];
            int i = 0;
            for (String s : mMap.keySet()) {
                chapters[i++] = mMap.get(String.valueOf(i));
            }
            i = 0;
            final String[] chaptertitles = new String[chapters.length];
            final boolean[] toDownload = new boolean[chapters.length];
            for (Chapter c : chapters) {
                chaptertitles[i] = chapters[i].id;
                i++;
            }
            final ArrayAdapter<Chapter> adapter = new ArrayAdapter<Chapter>(getBaseContext(), R.layout.text_item, chapters) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = getLayoutInflater().inflate(R.layout.chlist_item, parent, false);
                    TextView textView = (TextView) v.findViewById(R.id.text);
                    //Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
                    //textView.setTypeface(tf);
                    textView.setText(getItem(position).id);
                    return v;
                }
            };

            final ArrayList<Chapter> selectedChapters = new ArrayList<>();
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setMultiChoiceItems(chaptertitles, null, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            //Log.d("onMultiChoiceClick", "item clicked at " + which + ", corresponds to " + chapters[which].content);
                            toDownload[which] = isChecked;
                            if (toDownload[which]) {
                                selectedChapters.add(chapters[which]);
                            } else {
                                selectedChapters.remove(chapters[which]);
                            }
                        }
                    })
                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int n = 0;
                            //for(String s : mMap.keySet()){
                            //    if(toDownload[n++]){
                            //        selectedChapters.add(mMap.get(s));
                            //    }
                            //}
                            new AsyncDownloadTitle(getIntent().getStringExtra("title"), selectedChapters, getIntent().getStringExtra("href"), false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create();
            dialog.show();
        }else{
            Toast t = Toast.makeText(getBaseContext(), "No chapters found; check internet connection", Toast.LENGTH_SHORT);
            t.show();
        }
    }


    private void ShowDownloadAllDialog() {
        final CharSequence[] item = {"Automatically download latest chapters when available"};
        final boolean[] update = {true};
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle("Download Title?")
                        //.setTitle("Would you like to download all chapters from \"" + getIntent().getStringExtra("title") + "\" to your device?")
                .setMultiChoiceItems(item, update, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        update[0] = isChecked;
                        //Log.d("CreateDownloadDialog", "" + update[0]);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadTitle(update[0]);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        dialog.show();

    }

    private void DownloadTitle(boolean autodownload) {
        //Log.d("DownloadTitle", "starting");
        ArrayList<Chapter> list = new ArrayList<>();
        for(String s : mMap.keySet()){
            list.add(mMap.get(s));
        }
        new AsyncDownloadTitle(getIntent().getStringExtra("title"), list, getIntent().getStringExtra("href"), autodownload).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void ToggleFavorites(MenuItem item) {
        if (item.getIcon().getConstantState().equals(ResourcesCompat.getDrawable(
                getResources(), R.drawable.ic_favorite_border_white_24dp, null).getConstantState())) {
            item.setIcon(R.drawable.ic_favorite_white_24dp);
            AddToFavorites();
        } else {
            item.setIcon(R.drawable.ic_favorite_border_white_24dp);
            RemoveFromFavorites();
        }
    }

    private void ReadFromCheckpoint() {
        //Log.d("ReadFromCheckpoint", getFilesDir().getPath());
        File f = new File(getFilesDir().getPath() + "/" + getIntent().getStringExtra("title") + "checkpoint");
        Chapter ch = null;
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            int[] checkpoint = (int[]) ois.readObject();
            if (mMap != null) {
                int maplocation = mMap.size() - checkpoint[0] + 1;

                ch = mMap.get(Integer.toString(maplocation));
                if (ch != null) {
                    //Log.d("ReadFromCheckpoint", ch.content);
                    new AsyncGetPages(ch, Integer.toString(maplocation), checkpoint[1]).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else{

                }
            } else {
                Toast toast = Toast.makeText(getBaseContext(), "No pages found; check internet connection", Toast.LENGTH_SHORT);
                toast.show();
            }

        } catch (IOException | ClassNotFoundException e) {
            if (mMap != null) {
                int size = mMap.size();
                new AsyncGetPages(mMap.get(Integer.toString(mMap.size())), Integer.toString(size), 1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                //Log.d("ReadFromCheckpoint", "from exception");
                Toast toast = Toast.makeText(getBaseContext(), "No pages found; check internet connection", Toast.LENGTH_SHORT);
                toast.show();
            }
            e.printStackTrace();
        }
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
        //Log.d("toChapterReader", "chapter pressed");
        //Log.d("toChapterReader", item.content + " " + item.id);
        String chno = "";
        for (String key : mMap.keySet()) {
            Chapter ch = mMap.get(key);
            if (ch.content.equals(item.content)) {
                chno = key;
                //Log.d("DescriptionChapters", "chno " + chno + " keyset key " + key + " itemcontent " + item.content + " chcontent " + ch.content);
            }
        }
        new AsyncGetPages(item, chno).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /*startActivity(intent);*/
    }

    public void AddToFavorites() {
        //Log.d("DescriptionChapters", "AddToFavorites");
        Intent intent = this.getIntent();
        DirectoryDbHelper dbHelper = new DirectoryDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues favoriteValues = new ContentValues();
        ContentValues values = new ContentValues();

        String title = intent.getStringExtra("title");
        String href = intent.getStringExtra("href");
        String genres = intent.getStringExtra("genres");
        String author = intent.getStringExtra("author");
        String artist = intent.getStringExtra("artist");
        //Log.d("AddToFavorites", title + href + genres + author + artist);
        boolean status = intent.getBooleanExtra("status", false);
        double rank = intent.getDoubleExtra("rank", 0);
        Bitmap cover = null;
        String coverURI = ConvertCoverToUri();

        /*Cursor cursor = db.rawQuery("SELECT * FROM " +
                DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME + " WHERE " +
                DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);*/

        //if (cursor.moveToFirst()) {
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, title);
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, href);
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER, coverURI);
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES, genres);
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR, author);
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST, artist);
        if (status) {
            favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS, 1);
        } else {
            favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS, 0);
        }
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_RANK, rank);
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE, Sources.getSelectedSource());
        //Log.d("selsource", "source: " + Sources.getSelectedSource());
        favoriteValues.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED, 0);

        //}


        db.insert(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME, null, favoriteValues);
        /*db.execSQL("INSERT INTO " + DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME +
                " SELECT *, NULL FROM " + DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME +
                " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'");*/
        ArrayList<Chapter> chapters = new ArrayList<Chapter>();
        int n = 0;
        if (mMap != null) {
            for (Chapter chapter : mMap.values()) {
                chapters.add(n, chapter);
                /*n++;
                if (n > 2) { // FIXME: 7/9/2016 testing purposes
                    break;
                } */
            }
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

        //Snackbar.make(findViewById(R.id.main_content), "Added to favorites", Snackbar.LENGTH_SHORT).show();
        Toast toast = Toast.makeText(getBaseContext(), "Added to Favorites", Toast.LENGTH_SHORT);
        toast.show();
    }

    private String ConvertCoverToUri() {
        Bitmap cover = null;
        String title = getIntent().getStringExtra("title");
        if (getIntent().getByteArrayExtra("cover") != null) {
            byte[] coverbytes = getIntent().getByteArrayExtra("cover");
            BitmapFactory.Options options = new BitmapFactory.Options();
            cover = BitmapFactory.decodeByteArray(coverbytes, 0, coverbytes.length, options);
            //Log.d("AddToFavorites", "converted cover bitmap successfully");
        }else{
            cover = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
        }

        String coverURI = null;
        try {
            FileOutputStream fos = openFileOutput(title + "_cover", Context.MODE_PRIVATE);
            if(cover == null){
                cover = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
            }
            cover.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            //ObjectOutputStream oos = new ObjectOutputStream(fos);
            //oos.writeObject(cover);
            coverURI = getBaseContext().getFileStreamPath(title + "_cover").toURI().toString();
            //oos.close();
            fos.close();
            //Log.d("AddToFavorites", "converted cover to file: " + coverURI);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coverURI;
    }

    public void RemoveFromFavorites() {
        //Log.d("DescriptionChapters", "RemoveFromFavorites");
        Intent intent = this.getIntent();
        DirectoryDbHelper dbHelper = new DirectoryDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String href = intent.getStringExtra("href");

        db.delete(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME,
                DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);

        Toast toast = Toast.makeText(getBaseContext(), "Removed from Favorites", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void CopyURL() {
        String href = getIntent().getStringExtra("href");

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("pageURL", href);
        clipboardManager.setPrimaryClip(clipData);

        Toast toast = Toast.makeText(getBaseContext(), "Copied link to clipboard", Toast.LENGTH_SHORT);
        toast.show();

    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private class AsyncFetchTitle extends AsyncTask<Void, ProgressUpdate, Void> {


        String href;
        String description;
        Map<String, Chapter> chMap;

        public AsyncFetchTitle(String href) {
            super();
            this.href = href;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoader.setImageResource(R.drawable.loading_animation_home);
            mErrorText.setVisibility(View.INVISIBLE);
            AnimationDrawable d = (AnimationDrawable) mLoader.getDrawable();
            d.start();
        }

        @Override
        protected Void doInBackground(Void... params) { //// FIXME: 3/15/2016
            Elements summary;
            chMap = new HashMap<String, Chapter>();
            Bitmap cover = null;

            TitlePackage titlePackage = null;
            int src = getIntent().getIntExtra("src", -1);
            if(src == -1){
                src = Sources.getSelectedSource();
            }else{
                Sources.setSelectedSource(src);
            }
            switch(src) {
                case Sources.MANGAHERE:
                    titlePackage = MangaHereFunctions.TitleSetup(href, chMap);
                    break;
                case Sources.GOODMANGA:
                    titlePackage = GoodMangaFunctions.TitleSetup(href, chMap);
                    break;
                case Sources.MANGAFOX:
                    titlePackage = MangaFoxFunctions.TitleSetup(href, chMap);
                    break;
                case Sources.MANGAREADER:
                    titlePackage = MangaReaderFunctions.TitleSetup(href, chMap);
                    break;
                case Sources.MANGAPANDA:
                    titlePackage = MangaPandaFunctions.TitleSetup(href, chMap);
                    break;
                case Sources.MANGAINN:
                    titlePackage = MangaInnFunctions.TitleSetup(href, chMap);
                    break;
            }

            summary = titlePackage.getElements();
            if (titlePackage.getChapterMap() != null) {
                chMap = titlePackage.getChapterMap();
            } else {
                return null;
            }
            ////Log.d("check", "" + getIntent().getStringExtra("cover"));
            if (getIntent().getByteArrayExtra("cover") != null) {
                byte[] coverbytes = getIntent().getByteArrayExtra("cover");
                BitmapFactory.Options options = new BitmapFactory.Options();
                cover = BitmapFactory.decodeByteArray(coverbytes, 0, coverbytes.length, options);
            }
            String summaryText = "";
            if (summary.first() != null) {
                summaryText = summary.first().ownText(); // FIXME: 6/13/2016 socketexception causes this to crash
            }
            publishProgress(new ProgressUpdate(summaryText, chMap, cover));

            return null;
        }

        @Override
        protected void onProgressUpdate(final ProgressUpdate... progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DirectoryDbHelper dbHelper = new DirectoryDbHelper(DescriptionChapters.this);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                            //descriptionFragment = DescriptionFragment.newInstance(1); // FIXME: 7/11/2016 this causes issues with crashing

                            //chaptersFragment = ChaptersFragment.newInstance(2, getIntent().getStringExtra("title"));


                    /*Cursor cursor = db.rawQuery("SELECT * FROM " + DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME
                            + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + " =\'"
                            + getIntent().getStringExtra("title") + "\'", null);
                    cursor.moveToFirst();*/
                    String titleInfo = "";
                    ////Log.d("count", "" + cursor.getCount());
                    titleInfo += "Author(s): ";
                    titleInfo += getIntent().getStringExtra("author") + "\n\n";
                    String author = getIntent().getStringExtra("author") + "";
                    //titleInfo += getIntent()
                    //titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR)) + "\n\n";
                    titleInfo += "Artist(s): ";
                    titleInfo += getIntent().getStringExtra("artist") + "\n\n";
                    String artist = getIntent().getStringExtra("artist");
                    //titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST)) + "\n\n";
                    titleInfo += "Genre(s): ";
                    titleInfo += getIntent().getStringExtra("genres") + "\n\n";
                    String genres = getIntent().getStringExtra("genres") + "";
                    //titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES)) + "\n\n";
                    titleInfo += "Status: ";
                    String status;
                    if (getIntent().getBooleanExtra("status", false)) {
                        titleInfo += "Completed";
                        status = "Completed" + "";
                    } else {
                        titleInfo += "Ongoing";
                        status = "Ongoing" + "";
                    }
                    titleInfo += "\n\n";
                    //titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS)) + "\n\n";
                    switch (Sources.getSelectedSource()){
                        case Sources.MANGAHERE:
                            titleInfo += "Ranking: ";
                            break;
                        case Sources.GOODMANGA:
                            titleInfo += "Rating: ";
                        default:
                            titleInfo += "Ranking: ";
                    }
                    //titleInfo += getIntent().getIntExtra("rank", 0);
                    double rankd = getIntent().getDoubleExtra("rank", 0);
                    String rank = "";
                    int ranki;
                    switch (Sources.getSelectedSource()){
                        case Sources.MANGAHERE:
                            ranki = (int) rankd;
                            rank += "Ranked " + ranki;
                            break;
                        case Sources.GOODMANGA:
                            rank += "Rated " + rankd;
                            break;
                        case Sources.MANGAFOX:
                            ranki = (int) rankd;
                            rank += "Ranked " + ranki;
                            break;
                        case Sources.MANGAINN:
                            ranki = (int) rankd;
                            rank += "" + ranki + " views";

                    }
                    //String rank = getIntent().getDoubleExtra("rank", 0) + "";
                    //titleInfo += cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_RANK)) + "\n";


                    if (descriptionFragment != null) {
                        if (progress[0].description != null) {
                            descriptionFragment.setText(progress[0].description,
                                    (TextView) findViewById(R.id.description_title_description));
                        }
                        descriptionFragment.setText(author, (TextView) findViewById(R.id.description_title_author));
                        if(artist != null) {
                            descriptionFragment.setText(artist, (TextView) findViewById(R.id.description_title_artist));
                        }else{
                            ImageView i = (ImageView) findViewById(R.id.artistimg);
                            if(i != null) {
                                i.setVisibility(View.INVISIBLE);
                            }
                        }
                        descriptionFragment.setText(genres, (TextView) findViewById(R.id.description_title_genres));
                        descriptionFragment.setText(status, (TextView) findViewById(R.id.description_title_status));
                        descriptionFragment.setText(rank, (TextView) findViewById(R.id.description_title_rank));
                        if (progress[0].cover != null) {
                            descriptionFragment.setCover(progress[0].cover);
                        }
                    }
                    //Log.d("s", "set text");
                    if (chaptersFragment != null) {
                        chaptersFragment.setAdapter(progress[0].map);
                    }
                    mMap = progress[0].map;

                }
            });
        }

        @Override
        protected void onPostExecute(Void result) {
            //Log.d("onPostExecFetchTitle", "post exec started");
            mLoadingLayout.setVisibility(View.GONE);
            if(descriptionFragment != null) {
                descriptionFragment.showView(true);
            }
            if(chaptersFragment != null) {
                chaptersFragment.showErrorView(false);
            }
            //mLoader.setVisibility(View.INVISIBLE);
            //mErrorText.setVisibility(View.INVISIBLE);

            //mLoader.setImageResource(0);
            ////Log.d("onPostExecute setimg", "setting image" + mBitmapURI);

            if (chMap.size() == 0) {
                //descriptionFragment.showView(false);
                if(chaptersFragment != null) {
                    chaptersFragment.showErrorView(true);
                }
                //mLoadingLayout.setVisibility(View.VISIBLE);
                //mLoader.setImageResource(R.drawable.ic_error_outline_white_48dp);
                //mErrorText.setVisibility(View.VISIBLE);
                //this.cancel(true);
            }
        }
    }

    public class AsyncDownloadChapters extends AsyncTask<Void, Void, Void>{

        String title;
        String titlehref;
        Map<String, Chapter> map;
        NotificationManager notificationManager;
        NotificationCompat.Builder builder;

        public AsyncDownloadChapters(String title, String titlehref, Map<String, Chapter> map){
            this.title = title;
            this.titlehref = titlehref;
            this.map = map;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getBaseContext(), "No chapters to download", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(map == null){
                publishProgress();
                return  null;
            }
            return null;
        }
    }

    public class AsyncDownloadTitle extends AsyncTask<Void, Void, Void> {

        String title;
        String titlehref;
        ArrayList<Chapter> map;
        boolean autodownload;
        NotificationManager notificationManager;
        NotificationCompat.Builder builder;
        int source;

        public AsyncDownloadTitle(String title, ArrayList<Chapter> map, String titlehref, boolean autodownload) {
            this.title = title;
            this.map = map;
            this.titlehref = titlehref;
            this.autodownload = autodownload;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getBaseContext(), "No chapters to download; check internet connection", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(map == null){
                publishProgress();
                return  null;
            }

            Chapter[] chapters = new Chapter[map.size()];

            int n = 0;

            if (map.size() == 0) {
                //Log.d("AsyncDownloadTitle", "no chapters");
                publishProgress();
                return null;
            }
            for(Chapter c : map){
                chapters[n] = c;
                //Log.d("AsyncDownloadTitle", chapters[n].id + " " + chapters[n].content);
                n++;
            }
            //for (int i = map.size(); i > 0; i--, n++) {
            //    chapters[n] = map.get(Integer.toString(i));
            //    //Log.d("AsyncDownloadTitle", chapters[n].id + " " + chapters[n].content);
            //}


            //File coredownloads = new File(Environment.DIRECTORY_PICTURES + "/coredownloads");

            if (ContextCompat.checkSelfPermission(DescriptionChapters.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                String rootDirectory = Environment.getExternalStorageDirectory().toString();
                /*File myDir = new File(rootDirectory + "/NewDirectory");
                if (myDir.mkdir()) {
                    //Log.d("AsyncDownloadTitle", "created folder");
                }*/

                File coredownloads = new File(Environment.getExternalStoragePublicDirectory(""), "MangaBunnyDownloads");
                if (coredownloads.mkdirs()) {
                    //Log.d("AsyncDownloadTitle", "created folder " + coredownloads.getName());
                }
                //File coredownloadsfile = new File(coredownloads, "test.png");
                //File coredownloads = new File(Environment.DIRECTORY_PICTURES + "/coredownloads");

                File titledir = new File(coredownloads, title);
                if (titledir.mkdirs()) {
                    //Log.d("AsyncDownloadTitle", "created folder " + titledir.getName());
                }

                String chaptersURI = null;
                try {
                    FileOutputStream fos = openFileOutput(title + "_dlchapters", Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    ArrayList<Chapter> chapterArrayList = new ArrayList<>();
                    //chapterArrayList.add(chapters[0]);
                    Collections.addAll(chapterArrayList, chapters);
                    oos.writeObject(chapterArrayList);
                    chaptersURI = getBaseContext().getFileStreamPath(title + "_dlchapters").getPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                DirectoryDbHelper dbHelper = new DirectoryDbHelper(DescriptionChapters.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();

                String coverURI = ConvertCoverToUri();

                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, title);
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, titlehref);
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER, coverURI);
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_DIR, titledir.getPath());
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE, Sources.getSelectedSource());
                source = Sources.getSelectedSource();
                if (autodownload) {
                    //Log.d("autodownload", "Yes");
                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE, 1);
                } else {
                    //Log.d("autodownload", "no");
                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE, 0);
                }
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERS, chaptersURI);

                Intent cancelIntent = new Intent();
                cancelIntent.setAction("com.fruits.ntorin.mango.STOP_DOWNLOAD");
                PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(DescriptionChapters.this, 0, cancelIntent, 0);
                NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(R.drawable.ic_cancel_black_24dp, "Cancel", pendingCancelIntent)
                        .build();

                int requestCode = ("update" + System.currentTimeMillis()).hashCode();

                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                builder = new NotificationCompat.Builder(DescriptionChapters.this);
                builder.setContentTitle(title)
                        .setContentText("Downloading ...")
                        .setColor(ContextCompat.getColor(DescriptionChapters.this, R.color.colorPrimaryDark))
                        .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                        .setOngoing(true)
                        .addAction(cancelAction);

                String contentText = "";

                int i = 0;
                builder.setProgress(chapters.length, i, false);
                notificationManager.notify(requestCode, builder.build());
                for (Chapter ch : chapters) {
                    if(!CancelDownloadReceiver.isCancelled) {
                        File chdir = new File(titledir, ch.id);
                        if (chdir.mkdirs()) {
                            //Log.d("AsyncDownloadTitle", "created folder " + chdir.getName());
                        }
                        String[] pages = GetPages(ch.content, source);
                        DownloadPages(chdir, pages, i++, source);
                        builder.setProgress(chapters.length, i, false);
                        notificationManager.notify(requestCode, builder.build());
                    }else{
                        CancelDownloadReceiver.isCancelled = false;
                        contentText = "Download canceled.";
                        break;
                    }
                }
                db.delete(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME,
                        DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + titlehref + "\'",
                        null);
                db.insert(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME, null, values);

                db.close();
                if(contentText.equals("")) {
                    contentText = "Download completed.";
                }

                Intent intent = new Intent(DescriptionChapters.this, AppHome.class);
                Bundle b = new Bundle();
                b.putInt("tab", 3);
                intent.putExtras(b);
                PendingIntent pendingIntent = PendingIntent.getActivity(DescriptionChapters.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT  );
                //NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_book_white_24dp, "Read Now", pendingIntent)
                //        .build();
                builder = new NotificationCompat.Builder(DescriptionChapters.this);
                builder.setContentTitle(title)
                        .setContentText(contentText)
                        .setColor(ContextCompat.getColor(DescriptionChapters.this, R.color.colorPrimaryDark))
                        .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                        .setProgress(0, 0, false);
                notificationManager.cancel(requestCode);
                if(contentText.equals("Download completed.")) {
                    builder.setContentIntent(pendingIntent);
                }
                notificationManager.notify(requestCode, builder.build());
            } else {
                /*if (ContextCompat.checkSelfPermission(DescriptionChapters.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Log.d("AsyncDownloadTitle", "permission granted, going again");
                    //doInBackground();
                }*/
            }


            return null;
        }

        private String[] GetPages(String href, int source) {

            Elements pages = null;
            String[] pageURLs = new String[0];


            ChangeChaptersPackage c = null;
            if(!CancelDownloadReceiver.isCancelled) {
                switch (source) {
                    case Sources.MANGAHERE:
                        c = MangaHereFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.GOODMANGA:
                        c = GoodMangaFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.MANGAFOX:
                        c = MangaFoxFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.MANGAREADER:
                        c = MangaReaderFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.MANGAPANDA:
                        c = MangaPandaFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.MANGAINN:
                        c = MangaInnFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                }
                pageURLs = c.getPageURLs();
            }

            /* try {
                Document document = Jsoup.connect(href).get();
                Elements li = document.getElementsByAttributeValue("onchange", "change_page(this)");
                pages = li.first().children();
                //Log.d("#pages", "" + (pages.size()));
                //li = document.getElementsByAttributeValue("class", "btn next_page");
                //Log.d("nextpageurl", "" + li.first().attr("href"));
                pageURLs = new String[pages.size()];
                int i = 0;
                for (Element option : pages) {
                    if(!CancelDownloadReceiver.isCancelled) {
                        pageURLs[i] = option.attr("value");
                        //Log.d("getpages", option.attr("value"));
                        i++;
                    }else{
                        return pageURLs;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } */
            return pageURLs;
        }

        private void DownloadPages(File folderpath, String[] hrefs, int chno, int source) {

            //Log.d("DownloadPages", "" + hrefs.length);
            for (int i = 0; i < hrefs.length; i++) {
                if(!CancelDownloadReceiver.isCancelled) {
                    int tries = 0;
                    boolean success = false;
                    Document document = null;
                    Bitmap bitmap = null;
                    while (!success) {
                        try {
                            ////Log.d("checkurl", params[0]);
                            //Log.d("DownloadPages", "connecting to " + hrefs[i]);
                            document = Jsoup.connect(hrefs[i]).get();

                            //Log.d("DownloadPages", "connected to " + hrefs[i]);
                            success = true;

                        } catch (IOException e) {
                            tries++;
                            if (tries > 50) {
                                cancel(true);
                            }
                            e.printStackTrace();
                            //this.cancel(true);
                        }


                    }
                    //Element li = document.getElementById("image");
                    //String bmpURL = li.attr("src");
                    String bmpURL = null;
                    switch (source){
                        case Sources.MANGAHERE:
                            bmpURL = MangaHereFunctions.FetchPage(document);
                            break;
                        case Sources.GOODMANGA:
                            bmpURL = GoodMangaFunctions.FetchPage(document);
                            break;
                        case Sources.MANGAFOX:
                            bmpURL = MangaFoxFunctions.FetchPage(document);
                            break;
                        case Sources.MANGAREADER:
                            bmpURL = MangaReaderFunctions.FetchPage(document);
                            break;
                        case Sources.MANGAPANDA:
                            bmpURL = MangaPandaFunctions.FetchPage(document);
                            break;
                        case Sources.MANGAINN:
                            bmpURL = MangaInnFunctions.FetchPage(document);
                            break;
                    }
                    //Log.d("DownloadPages", bmpURL);
                    ////Log.d("test", "" + li.);
                    tries = 0;
                    success = false;
                    while (!success) {
                        try {
                            if(tries++ > 50){
                                break;
                            }
                            ////Log.d("AsyncFetchPage", "getBitmapFromURL starting");
                            bitmap = getBitmapFromURL(bmpURL);
                            ////Log.d("AsyncFetchPage", "getBitmapFromURL done");
                            success = true;
                            //Log.d("DownloadPages", "download from " + bmpURL + " successful");
                        } catch (IOException e) {
                            //cancel(true);
                            e.printStackTrace();
                        }
                    }

                    if (bitmap != null) {
                        File f = new File(folderpath, "" + chno + "_" + i + ".jpg");

                        try {
                            FileOutputStream fos = new FileOutputStream(f);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            if (!f.exists()) {
                                if (f.createNewFile()) {
                                    //Log.d("DownloadPages", "" + chno + i + " created");
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        }


    }


    public class AsyncGetPages extends AsyncTask<Void, Void, Void> {

        Chapter item;
        String chno;
        int pgno;
        ProgressDialog pdialog;

        public AsyncGetPages(Chapter item, String chno) {
            super();
            this.item = item;
            this.chno = chno;
        }

        public AsyncGetPages(Chapter item, String chno, int pgno) {
            super();
            this.item = item;
            this.chno = chno;
            this.pgno = pgno;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdialog = new ProgressDialog(DescriptionChapters.this, R.style.AlertDialogStyle);
            pdialog.setMessage("Loading ...");
            pdialog.setCancelable(false);
            pdialog.setCanceledOnTouchOutside(false);
            pdialog.show();

        }


        @Override
        protected void onProgressUpdate(Void... values) {
            Toast t = Toast.makeText(getBaseContext(), "No chapters found; the chapter was likely removed from the site.", Toast.LENGTH_LONG);
            t.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Elements pages = null;
            String[] pageURLs = new String[0];
            ChangeChaptersPackage c = null;
            switch(Sources.getSelectedSource()){
                case Sources.MANGAHERE:
                    c = MangaHereFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.GOODMANGA:
                    c = GoodMangaFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAFOX:
                    c = MangaFoxFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAREADER:
                    c = MangaReaderFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAPANDA:
                    c = MangaPandaFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAINN:
                    c = MangaInnFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
            }

            if(c == null){
                //Log.d("toChapterReader", "no chapters");
                publishProgress();
                return null;
            }

            pages = c.getPages();
            pageURLs = c.getPageURLs();
            /*try {
                Document document = Jsoup.connect(item.content).get();
                Elements li = document.getElementsByAttributeValue("onchange", "change_page(this)");
                if (li.first() != null) { // TODO: 7/11/2016 put error message here if null
                    pages = li.first().children();
                    //Log.d("#pages", "" + (pages.size()));
                    //li = document.getElementsByAttributeValue("class", "btn next_page");
                    //Log.d("nextpageurl", "" + li.first().attr("href"));
                    pageURLs = new String[pages.size()];
                    int i = 0;
                    for (Element option : pages) {
                        pageURLs[i] = option.attr("value");
                        //Log.d("getpages", option.attr("value"));
                        i++;
                    }
                } else {
                    //Log.d("toChapterReader", "no chapters");
                    publishProgress();
                    return null;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }*/

            if (pageURLs.length == 0) {
                //Log.d("AsyncGetPages", "no pages, going back");
                return null;
            }

            ContentValues values = new ContentValues();
            DirectoryDbHelper dbHelper = new DirectoryDbHelper(getBaseContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String coverURI = ConvertCoverToUri();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if(sharedPreferences.getBoolean(Settings.PREF_TRACK_HISTORY, true)) {
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, item.id);
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, item.content);
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_MANGATITLE, getIntent().getStringExtra("title"));
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLEHREF, getIntent().getStringExtra("href"));
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR, getIntent().getStringExtra("author"));
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST, getIntent().getStringExtra("artist"));
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES, getIntent().getStringExtra("genres"));
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_RANK, getIntent().getDoubleExtra("rank", 0));
                if (getIntent().getBooleanExtra("status", false)) {
                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS, 1);
                } else {
                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS, 0);
                }
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_DATE, getDateTime());
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER, coverURI);
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE, Sources.getSelectedSource());
                db.delete(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME,
                        DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + item.content + "\'", null);
                db.insert(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME, null, values);
                //Log.d("DescriptionChapters", "history time " + getDateTime());
            }else{
                //Log.d("DescriptionChapters", "history tracking disabled");
            }

            Intent intent = new Intent(DescriptionChapters.this, ChapterReader.class);
            Bundle bundle = new Bundle();
            bundle.putString("href", item.content);
            bundle.putString("title", item.id);
            bundle.putString("mangatitle", String.valueOf(getSupportActionBar().getTitle()));
            bundle.putStringArray("pageURLs", pageURLs);
            if (pages != null) {
                bundle.putInt("pages", pages.size());
            }
            bundle.putInt("chno", Integer.parseInt(chno));
            bundle.putSerializable("chlist", (HashMap) mMap);
            if (pgno != 0) {
                bundle.putInt("pgno", pgno);
            }
            intent.putExtras(bundle);

            //Log.d("toChapterReader", item.content);
            startActivity(intent);


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

                    //Log.d("toChapterReader", href);
                    startActivity(intent);

                }
            });
        }*/
    }

    class ProgressUpdate {
        public final String description;
        public final Map<String, Chapter> map;
        public Bitmap cover;

        public ProgressUpdate(String description, Map<String, Chapter> map, Bitmap cover) { //// TODO: 4/5/2016 can be changed to a TitlePackage
            this.description = description;
            this.map = map;
            this.cover = cover;
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
            switch (position) {
                case 0:
                    descriptionFragment = DescriptionFragment.newInstance(position + 1); // FIXME: 7/11/2016 this causes issues with crashing
                    return descriptionFragment;
                case 1:
                    chaptersFragment = ChaptersFragment.newInstance(position + 1, getIntent().getStringExtra("title"));
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
