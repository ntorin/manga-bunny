package com.fruits.ntorin.mango.home.history;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.fruits.ntorin.mango.home.directory.DirectorySetup;
import com.fruits.ntorin.mango.reader.ChapterReader;
import com.fruits.ntorin.mango.title.Chapter;
import com.fruits.ntorin.mango.title.DescriptionChapters;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    AbsListView absListView;

    SimpleCursorAdapter simpleCursorAdapter;
    historyAdapter historyAdapter;
    DirectoryDbHelper ddbHelper;
    private View mView;
    private LayoutInflater mInflater;
    private EditText mFilterText;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ddbHelper = new DirectoryDbHelper(this.getContext());

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        setHasOptionsMenu(true);
        Log.d("HistoryFragment", "onCreate run Async ");
        new AsyncFetchHistory(this).execute(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;

            case R.id.action_search:




                return true;

            case R.id.list_view:
                mView.findViewById(R.id.directory_grid).setVisibility(View.GONE);
                absListView = (AbsListView) mView.findViewById(R.id.directory_list);
                if(absListView.getOnItemClickListener() == null){
                    setListener();
                }
                absListView.setAdapter(simpleCursorAdapter);
                mView.findViewById(R.id.directory_list).setVisibility(View.VISIBLE);
                Log.d("tolist", "request list");
                return true;

            case R.id.catalog_view:
                mView.findViewById(R.id.directory_list).setVisibility(View.GONE);
                absListView = (AbsListView) mView.findViewById(R.id.directory_grid);
                if(absListView.getOnItemClickListener() == null){
                    setListener();
                }
                absListView.setAdapter(historyAdapter);
                mView.findViewById(R.id.directory_grid).setVisibility(View.VISIBLE);
                Log.d("togrid", "request grid");
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mInflater = inflater;
        mView = inflater.inflate(R.layout.fragment_directory, container, false);
        absListView = (AbsListView) mView.findViewById(R.id.directory_grid);
        mFilterText = (EditText) mView.findViewById(R.id.editText);

        setListener();

        return mView;
    }


    public void setListener(){
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                //Intent intent = new Intent(HistoryFragment.this.getContext(), DescriptionChapters.class);
                Bundle bundle = new Bundle();
                Cursor cursor = (Cursor) simpleCursorAdapter.getCursor();
                cursor.moveToPosition(position);
                String title = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
                String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                //test.close();

                String[] text = title.split(" ");
                Chapter chapter = new Chapter(title, href, text[text.length - 1]);
                new AsyncGetPagesFromHistory(chapter).execute();
                bundle.putString("title", title);
                bundle.putString("href", href);
                //intent.putExtras(bundle);
                //startActivity(intent);

                /*
                Intent intent = new Intent(HistoryFragment.this.getContext(), ChapterReader.class);
                Bundle bundle = new Bundle();
                Cursor cursor = (Cursor) simpleCursorAdapter.getCursor();
                cursor.moveToPosition(position);
                String title = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
                String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                bundle.putString("href", href);
                bundle.putStringArray("pageURLs", pageURLs);
                bundle.putInt("pages", pages.size()); //// FIXME: 4/2/2016 possible null issues here
                intent.putExtras(bundle);

                Log.d("toChapterReader", href);
                startActivity(intent);*/
            }
        });
    }

    private class AsyncGetPagesFromHistory extends AsyncTask<Void, Void, Void>{

        Chapter item;

        public AsyncGetPagesFromHistory(Chapter item){
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

            Intent intent = new Intent(HistoryFragment.this.getContext(), ChapterReader.class);
            Bundle bundle = new Bundle();
            bundle.putString("href", item.content);
            bundle.putStringArray("pageURLs", pageURLs);
            bundle.putInt("pages", pages.size()); //// FIXME: 4/2/2016 possible null issues here
            intent.putExtras(bundle);

            SQLiteDatabase db = ddbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("date", getDateTime());
            db.update(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME, values,
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF
                            + "=\'" + item.content + "\'", null);
            /*db.execSQL("UPDATE " + DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME +
                    " SET " + DirectoryContract.DirectoryEntry.COLUMN_NAME_DATE + " = " + getDateTime()
                     + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + item.content + "\'");*/

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

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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

    private class AsyncFetchHistory extends AsyncTask<String, String, Void> {

        ContentValues values = new ContentValues();
        SQLiteDatabase db = ddbHelper.getWritableDatabase();
        Fragment fragment;

        public AsyncFetchHistory(Fragment a){
            fragment = a;
        }

        @Override
        protected Void doInBackground(String... params) {
            publishProgress(params[0]);

            /*if(params[0].equals(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME)){
                DirectorySetup.MangafoxSetup(db);
            }else if(params[0].equals(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME)){
                //DirectorySetup.MangaHereSetup(values, db);
            }else if(params[0].equals(DirectoryContract.DirectoryEntry.BATOTO_TABLE_NAME)){
                DirectorySetup.BatotoSetup(values, db);
            }

            publishProgress(params[0]);*/
            return null;
        }

        @Override
        protected void onProgressUpdate(final String... tableName) {
            String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
            int[] to = {R.id.site_search_content};
            Cursor selectQuery = db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry._ID + ", " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + " FROM " +
                    tableName[0], null);


            historyAdapter = new historyAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            simpleCursorAdapter = new SimpleCursorAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            FilterQueryProvider filterQueryProvider = new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    return db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry._ID + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + " FROM " +
                            tableName[0] + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                            + " LIKE '%" + constraint.toString() + "%'", null);
                }
            };
            historyAdapter.setFilterQueryProvider(filterQueryProvider);
            simpleCursorAdapter.setFilterQueryProvider(filterQueryProvider);
            mFilterText.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    simpleCursorAdapter.getFilter().filter(s);
                    historyAdapter.getFilter().filter(s);
                    simpleCursorAdapter.notifyDataSetChanged();
                    historyAdapter.notifyDataSetChanged();
                }
            });

            if(absListView.equals(mView.findViewById(R.id.directory_grid))) {
                absListView.setAdapter(historyAdapter);
            }else{
                absListView.setAdapter(simpleCursorAdapter);
            }
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
    }



    class historyAdapter extends SimpleCursorAdapter{

        public historyAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            //Log.d("historyAdapter", "getView started");
            View v = view;
            ImageView picture;
            TextView name;
            Cursor cursor;

            if(v == null)
            {
                v = mInflater.inflate(R.layout.gridview_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }


            picture = (ImageView)v.getTag(R.id.picture);
            name = (TextView)v.getTag(R.id.text);
            cursor = (Cursor) getItem(i);

            //Item item = (Item)getItem(i);

            //picture.setImageResource(item.drawableId);
            //name.setText(item.name);
            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }

    }

}


