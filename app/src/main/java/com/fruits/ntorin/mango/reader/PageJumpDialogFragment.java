package com.fruits.ntorin.mango.reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.title.Chapter;

import org.jsoup.Connection;

import java.util.HashMap;

public class PageJumpDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private PageJumpDialogListener mListener;

    public PageJumpDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ReaderTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_page_jump_dialog, null);
        builder.setTitle("Page Jump");

        ImageAdapter pagesAdapter = new ImageAdapter((Uri[]) getArguments().getSerializable("bitmapuris"));

        GridView pagesGrid = (GridView) v.findViewById(R.id.pages_grid);
        pagesGrid.setAdapter(pagesAdapter);

        pagesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onPageItemClick(PageJumpDialogFragment.this, position);
            }
        });

        ChapterAdapter chapterAdapter = new ChapterAdapter(getActivity().getBaseContext(),
                (HashMap) getArguments().getSerializable("chlist"));

        GridView chaptersGrid = (GridView) v.findViewById(R.id.chapters_grid);
        chaptersGrid.setAdapter(chapterAdapter);

        chaptersGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onChapterItemClick(PageJumpDialogFragment.this, position + 1);
            }
        });

        builder.setView(v);

        return builder.create();
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
        Uri[] bitmapUris;
        public ImageAdapter(Uri[] bitmapUris){
            this.bitmapUris = bitmapUris;
            Log.d("ImageAdapter", "" + bitmapUris[0]);
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

            if (v == null) {
                v = getActivity().getLayoutInflater().inflate(R.layout.gridview_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }


            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);
            //Item item = (Item)getItem(i);
            Uri uri = null;

            picture.setImageURI(bitmapUris[position]);
            name.setText("Page " + (position + 1));

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

            return textView;
        }
    }
}
