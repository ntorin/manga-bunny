package com.fruits.ntorin.mango.reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fruits.ntorin.mango.utils.BitmapFunctions;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.utils.RetainFragment;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class BookmarksDialogFragment extends DialogFragment {

    private BookmarksDialogListener mListener;
    private LruCache<String, Bitmap> mMemoryCache;
    private Set<SoftReference<Bitmap>> mReusableBitmaps;

    public BookmarksDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ReaderTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_bookmarks_dialog, null);
        builder.setTitle("Bookmarks");

        DirectoryDbHelper dbHelper = new DirectoryDbHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
        final int[] to = {R.id.site_search_content};

        String verifiedString = getArguments().getString("mangatitle").replace("'", "''");
        String query = "SELECT * FROM " +
                DirectoryContract.DirectoryEntry.BOOKMARKS_TABLE_NAME +
                " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + "=\'" +
                verifiedString;
        if(getArguments().getBoolean("offline")){
            query += "_OFFLINE";
        }
        query += "\'";

        Cursor selectQuery = db.rawQuery(query, null);

        PageAdapter adapter = new PageAdapter(getActivity().getBaseContext(), R.layout.site_item,
                selectQuery, from, to, 0);

        GridView gridView = (GridView) v.findViewById(R.id.bookmarks_grid);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //int chno = getArguments().getInt("chno");
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();

                mListener.onItemClick(BookmarksDialogFragment.this, cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERNUM)),
                        cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM)) - 1);

            }
        });

        builder.setView(v);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (BookmarksDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement BookmarksDialogListener");
        }
    }

    public interface BookmarksDialogListener {
        void onItemClick(BookmarksDialogFragment dialog, int chno, int pgno);
    }

    public class PageAdapter extends SimpleCursorAdapter {

        public PageAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            ImageView picture;
            TextView name;
            Cursor cursor;

            if (v == null) {
                v = getActivity().getLayoutInflater().inflate(R.layout.gridview_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }

            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Muli.ttf");

            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);
            cursor = (Cursor) getItem(i);

            String uriString = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGEIMG));
            //Log.d("coverURI", "testcover" + uriString);

            Uri uri;
            if (uriString != null) {
                uri = Uri.parse(uriString);
                if(getActivity() != null) {
                    BitmapFunctions.loadBitmap(uri, picture, getResources(), mMemoryCache, mReusableBitmaps, 4);
                }
            }else{
                picture.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.noimage));
            }

            String ch = "Ch. ";
            String chname = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTER));

            if(chname == null){
                chname = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERID));
                ch = chname;
            }else{
                ch += chname;
            }

            String text = ch
                    + " (Pg." + cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM))
                    + ")";

            name.setText(text);
            name.setTypeface(tf);

            return v;
        }

    }
}
