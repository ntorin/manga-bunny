package com.fruits.ntorin.mango.reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fruits.ntorin.mango.utils.BitmapFunctions;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.utils.RetainFragment;
import com.fruits.ntorin.mango.home.downloads.FileArrayAdapter;
import com.fruits.ntorin.mango.title.Chapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PageJumpDialogFragment extends DialogFragment {

    private PageJumpDialogListener mListener;
    private LruCache<String, Bitmap> mMemoryCache;
    private Set<SoftReference<Bitmap>> mReusableBitmaps;

    public PageJumpDialogFragment() {
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
        View v = inflater.inflate(R.layout.fragment_page_jump_dialog, null);
        builder.setTitle("Page Jump");

        ImageAdapter pagesAdapter;
        if (getArguments().getBoolean("offline", false)) {
            File[] pages = (File[]) getArguments().getSerializable("pagelist");
            Uri[] pageuris = new Uri[pages.length];
            for(int i = 0; i < pages.length; i++){
                pageuris[i] = Uri.parse(pages[i].toURI().toString());
            }
            pagesAdapter = new ImageAdapter(pageuris);
        } else {
            pagesAdapter = new ImageAdapter((Uri[]) getArguments().getSerializable("bitmapuris"));
        }

        GridView pagesGrid = (GridView) v.findViewById(R.id.pages_grid);
        pagesGrid.setAdapter(pagesAdapter);
        pagesGrid.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                //view = null;
                //Log.d("recycled", "view recycled");
                ImageView i = (ImageView) view.findViewById(R.id.picture);
                i.setImageURI(null);
            }
        });

        pagesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onPageItemClick(PageJumpDialogFragment.this, position);
            }
        });

        GridView chaptersGrid = (GridView) v.findViewById(R.id.chapters_grid);
        ListView chaptersList = (ListView) v.findViewById(R.id.chapters_list);
        chaptersList.setVisibility(View.GONE);
        if(getArguments().getBoolean("offline", false)){
            chaptersGrid.setVisibility(View.GONE);
            chaptersList.setVisibility(View.VISIBLE);
            chaptersList = (ListView) v.findViewById(R.id.chapters_list);
            FileArrayAdapter fileChapterAdapter = new FileArrayAdapter<>(getActivity().getBaseContext(), R.layout.file_item, (File[]) getArguments().getSerializable("chlist"));
            chaptersList.setAdapter(fileChapterAdapter);
            chaptersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    File[] chlist = (File[]) getArguments().getSerializable("chlist");
                    mListener.onChapterItemClick(PageJumpDialogFragment.this, position + 1);
                }
            });
        }else{
            ChapterAdapter chapterAdapter = new ChapterAdapter(getActivity().getBaseContext(),
                    (HashMap) getArguments().getSerializable("chlist"));

            chaptersGrid.setAdapter(chapterAdapter);
            chaptersGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap map = (HashMap) getArguments().getSerializable("chlist");
                    map.size();
                    mListener.onChapterItemClick(PageJumpDialogFragment.this, (map.size() - position));
                }
            });
        }






        builder.setView(v);

        return builder.create();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            mListener = (PageJumpDialogListener) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement PageJumpDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface PageJumpDialogListener {
        void onPageItemClick(PageJumpDialogFragment dialog, int pgno);

        void onChapterItemClick(PageJumpDialogFragment dialog, int chno);
    }

    public class ImageAdapter extends BaseAdapter {
        int counter = 0;
        Uri[] bitmapUris;
        public ImageAdapter(Uri[] bitmapUris){
            this.bitmapUris = bitmapUris;
            //Log.d("ImageAdapter", "" + bitmapUris[0]);
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
        public View getView(int position, View view, ViewGroup viewGroup) {
            View v = view;
            ImageView picture;
            TextView name;
            //Log.d("counter++", "" + counter++ + ", uri: " + bitmapUris[position]);

            if (v == null) {
                v = getActivity().getLayoutInflater().inflate(R.layout.gridview_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }

            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Muli.ttf");

            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);
            //Item item = (Item)getItem(i);
            if(bitmapUris[position] != null) {
                Uri uri = bitmapUris[position];
                if(getActivity() != null) {
                    BitmapFunctions.loadBitmap(uri, picture, getResources(), mMemoryCache, mReusableBitmaps, 4);
                }
            }else{
                picture.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.noimage));
            }
            name.setText("Page " + (position + 1));
            name.setTypeface(tf);

            return v;
        }
    }

    public class ChapterAdapter extends BaseAdapter{
        Context context;
        HashMap<String, Chapter> chlist;

        public ChapterAdapter(Context context, HashMap chlist) {
            this.context = context;
            this.chlist = chlist;
        }

        @Override
        public int getCount() {
            return chlist.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = new TextView(context);
            textView.setText("" + (chlist.get(String.valueOf(position + 1)).chno));
            textView.setTextSize(24);
            textView.setGravity(Gravity.CENTER);
            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Muli.ttf");
            textView.setTypeface(tf);

            return textView;
        }
    }

    public class FileChapterAdapter extends BaseAdapter{
        Context context;
        File[] chlist;

        public FileChapterAdapter(Context context, File[] chlist) {
            this.context = context;
            this.chlist = chlist;
        }

        @Override
        public int getCount() {
            return chlist.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = new TextView(context);
            textView.setText("" + (position + 1));
            textView.setTextSize(24);
            textView.setGravity(Gravity.CENTER);
            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Muli.ttf");
            textView.setTypeface(tf);

            return textView;
        }
    }
}
