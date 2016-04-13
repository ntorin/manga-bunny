package com.fruits.ntorin.mango.home.favorites;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.fruits.ntorin.mango.title.Chapter;
import com.fruits.ntorin.mango.title.DescriptionChapters;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FavoritesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {
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
    FavoritesAdapter favoritesAdapter;
    DirectoryDbHelper ddbHelper;
    private View mView;
    private LayoutInflater mInflater;
    private EditText mFilterText;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoritesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
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
        Log.d("FavoritesFragment", "onCreate run Async ");
        new AsyncFetchDirectory(this).execute(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME);
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
            case R.id.action_search_settings:
                UpdateFavorites(getContext());
                return true;

            case R.id.action_search:
                return true;

            case R.id.list_view:
                mView.findViewById(R.id.favorites_grid).setVisibility(View.GONE);
                absListView = (AbsListView) mView.findViewById(R.id.favorites_list);
                if(absListView.getOnItemClickListener() == null){
                    setListener();
                }
                absListView.setAdapter(simpleCursorAdapter);
                mView.findViewById(R.id.favorites_list).setVisibility(View.VISIBLE);
                Log.d("tolist", "request list");
                return true;

            case R.id.catalog_view:
                mView.findViewById(R.id.favorites_list).setVisibility(View.GONE);
                absListView = (AbsListView) mView.findViewById(R.id.favorites_grid);
                if(absListView.getOnItemClickListener() == null){
                    setListener();
                }
                absListView.setAdapter(favoritesAdapter);
                mView.findViewById(R.id.favorites_grid).setVisibility(View.VISIBLE);
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
        mView = inflater.inflate(R.layout.fragment_favorites, container, false);
        absListView = (AbsListView) mView.findViewById(R.id.favorites_grid);
        mFilterText = (EditText) mView.findViewById(R.id.editText);

        setListener();

        return mView;
    }


    public void setListener(){
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                Intent intent = new Intent(FavoritesFragment.this.getContext(), DescriptionChapters.class);
                Bundle bundle = new Bundle();
                Cursor cursor = (Cursor) simpleCursorAdapter.getCursor();
                cursor.moveToPosition(position);
                String title = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
                String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                String cover = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
                //test.close();
                bundle.putString("title", title);
                bundle.putString("href", href);
                bundle.putString("cover", cover);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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

    private class AsyncFetchDirectory extends AsyncTask<String, String, Void> {

        ContentValues values = new ContentValues();
        SQLiteDatabase db = ddbHelper.getWritableDatabase();
        Fragment fragment;

        public AsyncFetchDirectory(Fragment a){
            fragment = a;
        }

        @Override
        protected Void doInBackground(String... params) {
            publishProgress(params[0]);
            return null;
        }

        @Override
        protected void onProgressUpdate(final String... tableName) {
            String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
            int[] to = {R.id.site_search_content};
            Cursor selectQuery = db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry._ID + ", " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + ", " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER + " FROM " +
                    tableName[0], null);


            favoritesAdapter = new FavoritesAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            simpleCursorAdapter = new SimpleCursorAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            FilterQueryProvider filterQueryProvider = new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    return db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry._ID + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER + " FROM " +
                            tableName[0] + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                            + " LIKE '%" + constraint.toString() + "%'", null);
                }
            };
            favoritesAdapter.setFilterQueryProvider(filterQueryProvider);
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
                    favoritesAdapter.getFilter().filter(s);
                    simpleCursorAdapter.notifyDataSetChanged();
                    favoritesAdapter.notifyDataSetChanged();
                }
            });

            if(absListView.equals(mView.findViewById(R.id.favorites_grid))) {
                absListView.setAdapter(favoritesAdapter);
            }else{
                absListView.setAdapter(simpleCursorAdapter);
            }
            Log.d("c", "approached notify");
        }
    }

    private class AsyncUpdateFavorites extends AsyncTask<Context, Void, Void>{

        @Override
        protected Void doInBackground(Context... params) {
            Log.d("UpdateFavorites", "starting");
            SQLiteDatabase db = ddbHelper.getWritableDatabase();
            Cursor selectQuery = db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                    + ", " + DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF
                    + ", " + DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERS + " FROM " +
                    DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME, null);
            String[] hrefs = new String[selectQuery.getCount()];
            for(int i = 0; i < selectQuery.getCount(); i++){
                selectQuery.moveToPosition(i);
                hrefs[i] = selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                Log.d("UpdateFavorites", hrefs[i]);
            }

            for(int i = 0; i < hrefs.length; i++){
                try {
                    Document document = Jsoup.connect(hrefs[i]).get();
                    Elements chapters = document.getElementsByClass("detail_list").first().getElementsByClass("left");
                    //int ch = 1;
                    ArrayList<Chapter> newChapters = new ArrayList<Chapter>();
                    for (Element element : chapters) {
                        Element e = element.children().first();
                        Log.d("t", e.attr("href"));
                        newChapters.add(new Chapter(e.ownText(), e.attr("href")));
                        //ch++;
                    }
                    selectQuery.moveToPosition(i);
                    String oldChaptersURI = selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERS));
                    Uri uri = Uri.parse(oldChaptersURI);
                    File file = new File(uri.getPath());
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    ArrayList<Chapter> oldChapters = (ArrayList<Chapter>) ois.readObject();
                    for(Chapter newChapter : newChapters){
                        boolean match = false;
                        for(Chapter oldChapter : oldChapters){
                            if(oldChapter.content.equals(newChapter.content)){
                                match = true;
                            }
                            if(match){
                                Log.d("UpdateFavorites", "match found");
                                break;
                            }
                        }
                        if(!match){
                            Log.d("UpdateFavorites", "no matches found; alert update for " + newChapter.content);
                        }
                    }
                    String filename = file.getName();
                    file.delete();

                    FileOutputStream fos = params[0].openFileOutput(filename, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(newChapters);

                    oos.close();
                    fos.close();
                    ois.close();
                    fis.close();

                    //selectQuery.moveToPosition(i);

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //ArrayList<Chapter>[] chapters = new ArrayList<Chapter>[selectQuery.getCount()];
            //selectQuery.
            return null;
        }
    }

    public void UpdateFavorites(Context context){
        new AsyncUpdateFavorites().execute(context);
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



    class FavoritesAdapter extends SimpleCursorAdapter{

        public FavoritesAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            //Log.d("favoritesAdapter", "getView started");
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

            //Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER))); //// FIXME: 4/11/2016  android.database.StaleDataException: Attempting to access a closed CursorWindow.Most probable cause: cursor is deactivated prior to calling this method.

            //picture.setImageURI(uri);
            //name.setText(item.name);
            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }

    }

}


