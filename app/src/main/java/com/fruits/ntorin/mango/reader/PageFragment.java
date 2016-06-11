package com.fruits.ntorin.mango.reader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.title.Chapter;
import com.soundcloud.android.crop.Crop;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.fruits.ntorin.mango.BitmapFunctions.getBitmapFromURL;


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
    //private static final String ARG_PARAM1 = "http://mangafox.me/manga/virgin_love/v01/c002/1.html";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ImageView mImageView;
    private String mBitmapURI;
    private Bitmap bitmap;
    private String mFileName;
    private int mActivePointerId;
    private Activity mScaleDetector;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mPosX;
    private float mPosY;
    private FragmentActivity mFragmentActivity;
    private File mFile;
    private ChapterReader chapterReader;
    //private static Toolbar mBottomToolbar;


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
    // TODO: Rename and change types and number of parameters
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
        fragment.setArguments(args);
        fragment.chapterReader = chapterReader;
        //fragment.mBottomToolbar = bottomToolbar;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mFragmentActivity = getActivity();


        setHasOptionsMenu(true);
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        //mBottomToolbar.inflateMenu(R.menu.menu_page);
        //inflater.inflate(R.menu.menu_page, mBottomToolbar.getMenu());
        inflater.inflate(R.menu.menu_page, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_bookmark:
                BookmarkPage();
                return true;
            case R.id.action_crop:
                if(mBitmapURI != null){
                    Uri uri = Uri.parse(mBitmapURI);
                    Crop.of(uri, uri).withAspect(50, 50).start(getActivity());

                }
                return true;
            case R.id.action_share_page:
                CopyPageURL();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //@Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                Log.d("ChapterReaderMotion", "ACTION_DONW");
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Remember where we started (for dragging)
                mLastTouchX = x;
                mLastTouchY = y;
                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                Log.d("ChapterReaderMotion", "ACTION_MOVE");
                // Find the index of the active pointer and fetch its position
                final int pointerIndex =
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Calculate the distance moved
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mPosX += dx;
                mPosY += dy;

                mImageView.invalidate();

                // Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                Log.d("ChapterReaderMotion", "ACTION_UP");
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                Log.d("ChapterReaderMotion", "ACTION_CANCEL");
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                Log.d("ChapterReaderMotion", "ACTION_POINTER_UP");

                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
                    mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }
        return true;
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
        Log.d("mFileName", "" + mFileName);
        if(mFile != null) {
            mFile.delete();
        }
        super.onDestroy();
        //Log.d("pagefragment", "PageFragment destroyed");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d("pagefragment", "PageFragment paused");
    }


    @Override
    public void onStart() {
        super.onStart();
        if(bitmap == null){
            //bitmap = mBitmap;
        }
        //Log.d("pagefragment", "PageFragment started");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(bitmap == null){
            //bitmap = mBitmap;
        }
        //Log.d("pagefragment", "PageFragment view destroyed");
    }

    private class AsyncFetchPage extends AsyncTask<String, Void, Void>{

        String href;
        Bitmap bitmap;
        PageFragment pageFragment;
        ImageView imageView;

        public AsyncFetchPage(){
            super();
            //this.href = href;
            //this.pageFragment = pageFragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ajax_loader); // FIXME: 5/7/2016 java.lang.NullPointerException: Attempt to invoke virtual method 'android.graphics.drawable.Drawable android.content.Context.getDrawable(int)' on a null object reference

            mImageView.setImageDrawable(drawable);
        }

        @Override
        protected Void doInBackground(String... params) {
            //Log.d("pagefragment", "doInBackground");
            href = params[0];
            Document document = null;
            try {
                Log.d("checkurl", params[0]);
                document = Jsoup.connect(params[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                this.cancel(true);
            }
            Element li = document.getElementById("image");
            String bmpURL = li.attr("src");
            //Log.d("test", "" + li.);
            try {
                bitmap = getBitmapFromURL(bmpURL);
            } catch (IOException e) {
                cancel(true);
                //e.printStackTrace();
            }
            mBitmapURI = null;


            mFileName = document.title();
            Log.d("pagetitle", mFileName);
            mFile = null;

            if(bitmap != null) {
                try {
                    FileOutputStream fos = null;
                    if (getActivity() != null) {
                        fos = getActivity().openFileOutput(mFileName, Context.MODE_PRIVATE);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        mFile = getActivity().getFileStreamPath(mFileName);
                        mBitmapURI = mFile.toURI().toString();
                        Log.d("filepathURI", "" + mBitmapURI);
                        int position = getArguments().getInt("pgno") - 1;
                        chapterReader.mBitmapURIs[position]  = Uri.parse(mBitmapURI);
                        fos.close();
                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //Log.d("pagefragment", "doInBackground done");
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
           // Log.d("pagefragment", "onPostExecute");
            if(mBitmapURI != null) {
                setImage(mBitmapURI);
                Log.d("onPostExecute setimg", "setting image" + mBitmapURI);
            }else{
                this.cancel(true);
            }
            //bitmap = mBitmap;
            //Log.d("pagefragment", "onPostExecute finished");
        }

        @Override
        protected void onCancelled() {
            new AsyncFetchPage().execute(href);
            super.onCancelled();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            saveCroppedImage(Uri.parse(mBitmapURI));
        }
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

        Log.d("BookmarkPage", "" + this.getArguments());
        Log.d("BookmarkPage", bkmkData.getString("title"));
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, bkmkData.getString("title"));

        Log.d("BookmarkPage", bkmkData.getString("chaptertitle"));
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERID, bkmkData.getString("chaptertitle"));

        Log.d("BookmarkPage", bkmkData.getString("chapterhref"));
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERCONTENT, bkmkData.getString("chapterhref"));

        Log.d("BookmarkPage", "" + bkmkData.getInt("pgno"));
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM, bkmkData.getInt("pgno"));

        Log.d("BookmarkPage", "" + bkmkData.getInt("chno"));
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERNUM, bkmkData.getInt("chno"));

        //Log.d("BookmarkPage", );
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGEIMG, mBitmapURI);
        db.insert(DirectoryContract.DirectoryEntry.BOOKMARKS_TABLE_NAME, null, values);

        Toast toast = Toast.makeText(getContext(), "Added page to Bookmarks", Toast.LENGTH_SHORT);
        toast.show();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_page, container, false);


        mImageView = (ImageView) view.findViewById(R.id.reader_img);
        //if(bitmap != null){
            Log.d("onCreateView resetimg", "setting image" + mBitmapURI);
        if(mBitmapURI != null) {
            setImage(mBitmapURI);
        }
        //}else{
            //new AsyncFetchPage().execute(ARG_PARAM1);
        //}
        Log.d("pagefragment", "view created");
        String href = getArguments().getString("href");
        new AsyncFetchPage().execute(href);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    public void setImage(String bitmapURI){
        Uri uri = Uri.parse(bitmapURI);
        mImageView.setImageURI(uri);
        mImageView.postInvalidate();
        //bitmap = mBitmap;
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
    }
}
