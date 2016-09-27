package com.fruits.ntorin.mango.reader;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.fruits.ntorin.mango.utils.BitmapFunctions;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.utils.RetainFragment;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.sourcefns.GoodMangaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaFoxFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaHereFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaInnFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaPandaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaReaderFunctions;
import com.fruits.ntorin.mango.sourcefns.Sources;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.fruits.ntorin.mango.utils.BitmapFunctions.getBitmapFromURL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private OnFragmentInteractionListener mListener;
    private SubsamplingScaleImageView mImageView;
    //private String mFileName;
    private ImageView mLoader;
    private TextView mErrorText;
    private LruCache<String, Bitmap> mMemoryCache;
    private Set<SoftReference<Bitmap>> mReusableBitmaps;
    private String mPath;

    public PageFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param href Page image URL
     * @return A new instance of fragment PageFragment.
     */
    public static PageFragment newInstance(String href, int chno, int pgno, String title,
                                           String chapterTitle, String chapterHref, ChapterReader chapterReader) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putString("href", href);
        args.putInt("chno", chno);
        args.putInt("pgno", pgno);
        args.putString("title", title);
        args.putString("chaptertitle", chapterTitle);
        args.putString("chapterhref", chapterHref);
        args.putSerializable("bitmapURIs", chapterReader.mBitmapURIs);
        args.putSerializable("pageloaders", chapterReader.pageLoaders);
        fragment.setArguments(args);
        //fragment.chapterReader = chapterReader;
        //fragment.mBottomToolbar = bottomToolbar;
        return fragment;
    }

    public static PageFragment newInstance(File page, int chno, int pgno, String title,
                                           String chapterTitle, ChapterReader chapterReader){
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putSerializable("page", page);
        args.putInt("chno", chno);
        args.putInt("pgno", pgno);
        args.putString("title", title);
        args.putString("chaptertitle", chapterTitle);
        args.putBoolean("offline", true);
        args.putSerializable("bitmapURIs", chapterReader.mBitmapURIs);
        args.putSerializable("pageloaders", chapterReader.pageLoaders);
        fragment.setArguments(args);
        //fragment.chapterReader = chapterReader;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(getActivity().getFragmentManager());
        mMemoryCache = retainFragment.mRetainedCache;
        if(mMemoryCache == null) {
            //Log.d("PageJumpDialogFragment", "memcache is null");
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
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

        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        //mBottomToolbar.inflateMenu(R.menu.menu_page);
        //inflater.inflate(R.menu.menu_page, mBottomToolbar.getMenu());
        inflater.inflate(R.menu.menu_page, menu);

        DirectoryDbHelper dbHelper = new DirectoryDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String verifiedString = getArguments().getString("title").replace("'", "''");
        Cursor cursor = null;
        if(getArguments().getBoolean("offline")){
            cursor = db.rawQuery("SELECT * FROM " + DirectoryContract.DirectoryEntry.BOOKMARKS_TABLE_NAME
                    + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                    + "=\'" + verifiedString +  "_OFFLINE\' AND " + DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERNUM + "="
                    + getArguments().getInt("chno") + " AND " + DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM
                    + "=" + (getArguments().getInt("pgno") + 1), null);
        }else {
            cursor = db.rawQuery("SELECT * FROM " + DirectoryContract.DirectoryEntry.BOOKMARKS_TABLE_NAME
                    + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                    + "=\'" + verifiedString + "\' AND " + DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERNUM + "="
                    + getArguments().getInt("chno") + " AND " + DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM
                    + "=" + getArguments().getInt("pgno"), null);
        }
        if(cursor.getCount() > 0){
            MenuItem item = menu.findItem(R.id.action_bookmark);
            item.setIcon(R.drawable.ic_bookmark_white_24dp);
        }
        if(getArguments().getBoolean("offline")){
            menu.removeItem(R.id.action_share_page);
        }

        cursor.close();
        db.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_bookmark:
                ToggleBookmark(item);
                return true;
            case R.id.action_crop:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    int i = 0;
                    //Log.d("CropPage", "need permission");
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, i);
                    //Log.d("CropPage", "asking permission");
                } else {
                    CropPage();
                }
                return true;
            case R.id.action_share_page:
                if(!getArguments().getBoolean("offline")) {
                    CopyPageURL();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.d("onRequestResult", "starting");
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            CropPage();
        }
    }

    private void CropPage() {
        Uri[] bitmapURIs = (Uri[]) getArguments().getSerializable("bitmapURIs");
        if(getArguments().getInt("pgno") - 1 < bitmapURIs.length) {
            Uri bitmapURI = bitmapURIs[getArguments().getInt("pgno") - 1];
            if (bitmapURI != null && !getArguments().getBoolean("offline")) {
                Uri uri = bitmapURI;
                CropImage.activity(uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(getActivity());
                //Crop.of(uri, uri).withAspect(50, 50).start(getActivity());
            }
            if (getArguments().getBoolean("offline")) {
                File f = (File) getArguments().getSerializable("page");
                Uri uri = Uri.parse(f.toURI().toString());
                CropImage.activity(uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(getActivity());
                //Crop.of(uri, uri).withAspect(50, 50).start(getActivity());
            }
        }
    }

    private void ToggleBookmark(MenuItem item) {
        if(item.getIcon().getConstantState().equals(ResourcesCompat.getDrawable(
                getResources(), R.drawable.ic_bookmark_border_white_24dp, null).getConstantState())){
            item.setIcon(R.drawable.ic_bookmark_white_24dp);
            BookmarkPage();
        }else{
            item.setIcon(R.drawable.ic_bookmark_border_white_24dp);
            RemoveBookmarkPage();
        }

    }

    private void CopyPageURL() {
        String href = getArguments().getString("href");
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("pageURL", href);
        clipboardManager.setPrimaryClip(clipData);

        Toast toast = Toast.makeText(getContext(), "Copied page link to clipboard", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onDestroy() {
        /* //Log.d("mFileName", "" + mFileName);

        if (mFileName != null) {
            //File f = getActivity().getFileStreamPath(mFileName);
            //f.delete();
        } */
        //Log.d("pgfragdestroy", "ondestroy");
        super.onDestroy();
        ////Log.d("pagefragment", "PageFragment destroyed");
    }

    @Override
    public void onPause() {
        super.onPause();
        ////Log.d("pagefragment", "PageFragment paused");
    }


    @Override
    public void onStart() {
        super.onStart();
        ////Log.d("pagefragment", "PageFragment started");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //removeImage();
        //mImageView.recycle();
        mImageView.setImage(ImageSource.resource(0));
        //Bitmap b = mMemoryCache.get(mPath);
        //mMemoryCache.remove(mPath);
        //Log.d("PageFragment", "view destroyed, page " + getArguments().getInt("pgno"));
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void onStop() {
        //Log.d("PageFragment", "stopped, page " + getArguments().getInt("pgno"));
        super.onStop();
    }

    private void saveCroppedImage(Uri resultUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MangoCrop");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void BookmarkPage() {
        DirectoryDbHelper dbHelper = new DirectoryDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Bundle bkmkData = this.getArguments();

        if(getArguments().getBoolean("offline")){
            //Log.d("BookmarkPage", bkmkData.getString("title"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, bkmkData.getString("title") + "_OFFLINE");

            //Log.d("BookmarkPage", bkmkData.getString("chaptertitle"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERID, bkmkData.getString("chaptertitle"));

            values.putNull(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERCONTENT);

            //Log.d("BookmarkPage", "" + bkmkData.getInt("pgno"));

            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM, bkmkData.getInt("pgno") + 1);

            //Log.d("BookmarkPage", "" + bkmkData.getInt("chno"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERNUM, bkmkData.getInt("chno"));

            values.putNull(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTER);
            ////Log.d("BookmarkPage", );
            File page = (File) getArguments().getSerializable("page");
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGEIMG, page.toURI().toString());

        }else {

            //Log.d("BookmarkPage", "" + this.getArguments());
            //Log.d("BookmarkPage", bkmkData.getString("title"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, bkmkData.getString("title"));

            //Log.d("BookmarkPage", bkmkData.getString("chaptertitle"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERID, bkmkData.getString("chaptertitle"));

            ////Log.d("BookmarkPage", bkmkData.getString("chapterhref"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERCONTENT, bkmkData.getString("chapterhref"));

            //Log.d("BookmarkPage", "" + bkmkData.getInt("pgno"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM, bkmkData.getInt("pgno"));

            //Log.d("BookmarkPage", "" + bkmkData.getInt("chno"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERNUM, bkmkData.getInt("chno"));

            String[] split = bkmkData.getString("chaptertitle").split(" ");
            //Log.d("BookmarkPage", "" + split[split.length - 1]);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTER, split[split.length - 1]);

            ////Log.d("BookmarkPage", );
            Uri[] bitmapURIs = (Uri[]) getArguments().getSerializable("bitmapURIs");
            Bitmap bitmap = BitmapFactory.decodeFile(bitmapURIs[getArguments().getInt("pgno") - 1].getPath());
            String fileName = "imgbkmk" + bkmkData.getString("chaptertitle") + "_" + bkmkData.getInt("pgno");
            File f = new File(fileName);
            if (bitmap != null) {
                try {
                    FileOutputStream fos;
                    if (getActivity() != null && getContext() != null) {
                        fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        f = getContext().getFileStreamPath(fileName);
                        fos.close();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGEIMG, f.toURI().toString());
        }
        db.insert(DirectoryContract.DirectoryEntry.BOOKMARKS_TABLE_NAME, null, values);

        Toast toast = Toast.makeText(getContext(), "Added page to Bookmarks", Toast.LENGTH_SHORT);
        toast.show();

    }

    private void RemoveBookmarkPage() {
        DirectoryDbHelper dbHelper = new DirectoryDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Bundle bkmkData = this.getArguments();

        String whereClause = null;
        if (getArguments().getBoolean("offline")) {
            whereClause = DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERID + "=\'" +
                    bkmkData.getString("chaptertitle") + "\' AND " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM + "=\'" +
                    bkmkData.getInt("pgno") + "\' AND " + DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERCONTENT + " IS NULL";

        } else {

            //Log.d("BookmarkPage", "" + this.getArguments());
            //Log.d("BookmarkPage", bkmkData.getString("title"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, bkmkData.getString("title"));

            //Log.d("BookmarkPage", bkmkData.getString("chaptertitle"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERID, bkmkData.getString("chaptertitle"));

            //Log.d("BookmarkPage", bkmkData.getString("chapterhref"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERCONTENT, bkmkData.getString("chapterhref"));

            //Log.d("BookmarkPage", "" + bkmkData.getInt("pgno"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM, bkmkData.getInt("pgno"));

            //Log.d("BookmarkPage", "" + bkmkData.getInt("chno"));
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERNUM, bkmkData.getInt("chno"));


            String fileName = "imgbkmk" + bkmkData.getString("chaptertitle");
            File f = new File(getContext().getFileStreamPath(fileName).getName());
            f.delete();
            ////Log.d("BookmarkPage", );
            String verifiedString = bkmkData.getString("chaptertitle").replace("'", "''");
            Uri[] bitmapURIs = (Uri[]) getArguments().getSerializable("bitmapURIs");
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGEIMG, bitmapURIs[getArguments().getInt("pgno") - 1].toString());
            whereClause = DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERID + "=\'" +
                    verifiedString + "\' AND " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM + "=\'" +
                    bkmkData.getInt("pgno") + "\'";
        }
        Toast toast;

        if (db.delete(DirectoryContract.DirectoryEntry.BOOKMARKS_TABLE_NAME, whereClause, null) > 0) {
            toast = Toast.makeText(getContext(), "Removed page from Bookmarks", Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(getContext(), "This page isn't bookmarked!", Toast.LENGTH_SHORT);
        }
        toast.show();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_page, container, false);


        mLoader = (ImageView) view.findViewById(R.id.loader);
        mErrorText = (TextView) view.findViewById(R.id.error_text);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
        mErrorText.setTypeface(tf);
        mImageView = (SubsamplingScaleImageView) view.findViewById(R.id.reader_img);
        //mImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        RelativeLayout loadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        loadingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("mLoaderOnClick", "clicked");
                if(mLoader.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                        getResources(), R.drawable.ic_error_outline_white_48dp, null).getConstantState())){
                    //Log.d("mLoaderOnClick", "clicked and error");
                    String href = getArguments().getString("href");
                    AsyncFetchPage fetchPage = new AsyncFetchPage(getContext());
                    fetchPage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, href);
                }
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("onClick", "clicked");
                    mListener.onToggleUI();

            }
        });

        Uri bitmapURI = null;
        if (!getArguments().getBoolean("offline")) {
            if(getArguments().getSerializable("bitmapURIs") != null) {
                Uri[] bitmapURIs = (Uri[]) getArguments().getSerializable("bitmapURIs");
                bitmapURI = bitmapURIs[getArguments().getInt("pgno") - 1];
                //Log.d("onCreateView resetimg", "setting image" + bitmapURI);
                if (bitmapURI != null) {
                    setImage(bitmapURI);
                }
            }
        }

        if (bitmapURI == null && !getArguments().getBoolean("offline")) {
            String href = getArguments().getString("href");
            AsyncFetchPage fetchPage = new AsyncFetchPage(getContext());
            fetchPage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, href);
            ArrayList<AsyncTask> pageLoaders = (ArrayList<AsyncTask>) getArguments().getSerializable("pageloaders");
            if (pageLoaders == null) {
                pageLoaders = new ArrayList<>();
            }
            pageLoaders.add(fetchPage);
        }

        if (getArguments().getBoolean("offline")) {
            File page = (File) getArguments().getSerializable("page");
            setImage(Uri.parse(page.toURI().toString()));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setImage(Uri bitmapURI) {
        if (mImageView != null) {
            if(isAdded() && getActivity() != null) {
                BitmapFunctions.loadBitmap(bitmapURI, mImageView, getResources(), mMemoryCache, mReusableBitmaps);
                mPath = bitmapURI.getPath();
                //mImageView.setImage(ImageSource.uri(bitmapURI));
                mImageView.postInvalidate();
            }
            //mImageView.setImage(ImageSource.resource(0));
            /*ListView linearLayout = (ListView) getActivity().findViewById(R.id.vertical_container);
            SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(getContext());
            imageView.setImage(ImageSource.uri(bitmapURI));
            imageView.setTag("page" + getArguments().getInt("pgno"));
            if(linearLayout.findViewWithTag("page" + getArguments().getInt("pgno")) == null) {
                //linearLayout.addView(imageView, stripCounter++);
            } */
            ////Log.d("setImage", "linearlayout size: " + linearLayout.);
        }
        //bitmap = mBitmap;
    }

    public void removeImage() {
        if (mImageView != null) {
            //mImageView.setImage(ImageSource.resource(0));
            //mImageView.postInvalidate();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onChapterCalled(int direction);

        void onToggleUI();
        void onPageUpdate();
    }

    private class AsyncFetchPage extends AsyncTask<String, Void, Void> {

        String href;
        //Bitmap bitmap;
        //PageFragment pageFragment;
        //ImageView imageView;
        Context context;

        public AsyncFetchPage(Context context) {
            super();
            this.context = context;
            //this.href = href;
            //this.pageFragment = pageFragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ajax_loader); // FIXME: 5/7/2016 java.lang.NullPointerException: Attempt to invoke virtual method 'android.graphics.drawable.Drawable android.content.Context.getDrawable(int)' on a null object reference

            mLoader.setImageResource(R.drawable.loading_animation);
            mErrorText.setVisibility(View.INVISIBLE);
            AnimationDrawable d = (AnimationDrawable) mLoader.getDrawable();
            d.start();
            //mImageView.setImageDrawable(drawable);
        }

        @Override
        protected Void doInBackground(String... params) {
            ////Log.d("pagefragment", "doInBackground");
            String href = params[0];
            Document document = null;
            Bitmap bitmap = null;
            int tries = 0;
            boolean success = false;
            while (!success) {
                try {
                    if (tries++ > 50) {
                        return null;
                    }
                    ////Log.d("checkurl", params[0]);
                    //Log.d("AsyncFetchPage", "connecting to " + params[0]);
                    document = Jsoup.connect(params[0]).get();

                    //Log.d("AsyncFetchPage", "connected to " + params[0]);
                    success = true;

                } catch (IOException e) {
                    e.printStackTrace();
                    //this.cancel(true);
                }


            }


            //Element li = document.getElementById("image");
            String bmpURL = null;
            switch (Sources.getSelectedSource()){
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
            ////Log.d("test", "" + li.);
            tries = 0;
            success = false;
            while (!success) {
                try {
                    //Log.d("AsyncFetchPage", "getBitmapFromURL starting: " + params[0]);
                    bitmap = getBitmapFromURL(bmpURL);
                    //Log.d("AsyncFetchPage", "getBitmapFromURL done: " + params[0]);
                    success = true;
                } catch (IOException e) {
                    //cancel(true);
                    e.printStackTrace();
                }
            }
            //mBitmapURI = null;


            String fileName = "imgcache" + document.title();
            ////Log.d("pagetitle", mFileName);
            File f;

            if (bitmap != null) {
                try {
                    FileOutputStream fos;
                    if (getActivity() != null) {
                        fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        f = context.getFileStreamPath(fileName);
                        Uri bitmapURI = Uri.parse(f.toURI().toString());
                        ////Log.d("filepathURI", "" + mBitmapURI);
                        int position = getArguments().getInt("pgno") - 1;
                        Uri[] bitmapURIs = (Uri[]) getArguments().getSerializable("bitmapURIs");
                        bitmapURIs[position] = bitmapURI;
                        //mListener.onPageUpdate();
                        fos.close();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ////Log.d("pagefragment", "doInBackground done");
            //Log.d("AsyncFetchPage", params[0] + " done");
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            ////Log.d("AsyncFetchPage", "onPostExecute starting");
            Uri[] bitmapURIs = (Uri[]) getArguments().getSerializable("bitmapURIs");
            Uri bitmapURI = bitmapURIs[getArguments().getInt("pgno") - 1];
            if (bitmapURI != null && mImageView != null) {
                mImageView.setVisibility(View.VISIBLE);
                setImage(bitmapURI);
                //mLoader.setImageResource(0);
                ////Log.d("onPostExecute setimg", "setting image" + mBitmapURI);
            } else {
                if(mImageView != null) {
                    mImageView.setVisibility(View.GONE);
                }
                mLoader.setImageResource(R.drawable.ic_error_outline_white_48dp);
                mErrorText.setVisibility(View.VISIBLE);
                //this.cancel(true);
            }
            //bitmap = mBitmap;
            ////Log.d("AsyncFetchPage", "onPostExecute done");
        }

        @Override
        protected void onCancelled() {
            //Log.d("AsyncFetchPage", "task cancelled");
            //new AsyncFetchPage().execute(href);
            super.onCancelled();
        }


    }
}
