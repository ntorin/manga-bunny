package com.fruits.ntorin.mango.reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;


public class BookmarksDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BookmarksDialogListener mListener;

    public BookmarksDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ReaderTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_bookmarks_dialog, null);
        builder.setTitle("Bookmarks");

        DirectoryDbHelper dbHelper = new DirectoryDbHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
        final int[] to = {R.id.site_search_content};

        Cursor selectQuery = db.rawQuery("SELECT * FROM " +
                DirectoryContract.DirectoryEntry.BOOKMARKS_TABLE_NAME +
                " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + "=\'" +
                getArguments().getString("mangatitle") + "\'", null);

        PageAdapter adapter = new PageAdapter(getActivity().getBaseContext(), R.layout.site_item,
                selectQuery, from, to, 0);

        GridView gridView = (GridView) v.findViewById(R.id.bookmarks_grid);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int chno = getArguments().getInt("chno");
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();

                mListener.onItemClick(BookmarksDialogFragment.this, chno,
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


            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);
            cursor = (Cursor) getItem(i);

            String uriString = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGEIMG));
            Log.d("coverURI", "testcover" + uriString);
            //Item item = (Item)getItem(i);
            Uri uri = null;
            if (uriString != null) {
                uri = Uri.parse(uriString);
            }

            picture.setImageURI(uri);
            //name.setText(item.name);
            String text = "Ch."
                    + cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERNUM))
                    + " (Pg." + cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_PAGENUM))
                    + ")";

            name.setText(text);

            return v;
        }

    }
}
