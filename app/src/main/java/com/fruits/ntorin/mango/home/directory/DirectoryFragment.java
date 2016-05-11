package com.fruits.ntorin.mango.home.directory;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.RadioButton;
import android.widget.TextView;

import com.fruits.ntorin.mango.SettingsDialogFragment;
import com.fruits.ntorin.mango.home.AppHome;
import com.fruits.ntorin.mango.title.DescriptionChapters;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;

import java.util.ArrayList;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DirectoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectoryFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private AbsListView absListView;

    private SimpleCursorAdapter simpleCursorAdapter;
    private DirectoryAdapter directoryAdapter;
    private DirectoryDbHelper ddbHelper;
    private View mView;
    private LayoutInflater mInflater;
    private EditText mFilterText;
    private String searchString;
    private String pickSource = DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME;
    private String sortBy = DirectoryContract.DirectoryEntry.BATOTO_TABLE_NAME;
    private ArrayList<String> genres;

    public int getPickSourceRadioID() {
        return pickSourceRadioID;
    }

    public void setPickSourceRadioID(int pickSourceRadioID) {
        this.pickSourceRadioID = pickSourceRadioID;
    }

    public int getSortByRadioID() {
        return sortByRadioID;
    }

    public void setSortByRadioID(int sortByRadioID) {
        this.sortByRadioID = sortByRadioID;
    }

    private int pickSourceRadioID;
    private int sortByRadioID;



    public DirectoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DirectoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DirectoryFragment newInstance() {
        DirectoryFragment fragment = new DirectoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ddbHelper = new DirectoryDbHelper(this.getContext());

        setHasOptionsMenu(true);
        Log.d("DirectoryFragment", "onCreate run Async ");
        new AsyncFetchDirectory(this).execute(pickSource);
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
               // new AsyncUpdateDirectory().execute();
                ConfigureSearch();
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
                absListView.setAdapter(directoryAdapter);
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
                Intent intent = new Intent(DirectoryFragment.this.getContext(), DescriptionChapters.class);
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


    private class AsyncUpdateDirectory extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            DirectorySetup.UpdateDirectory(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME,
                    "http://www.mangahere.co/mangalist/", ddbHelper.getWritableDatabase(), getContext());
            return null;
        }
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

            if(params[0].equals(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME)){
                DirectorySetup.MangafoxSetup(db);
            }else if(params[0].equals(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME)){
                DirectorySetup.MangaHereSetup(db, getContext());
            }else if(params[0].equals(DirectoryContract.DirectoryEntry.BATOTO_TABLE_NAME)){
                DirectorySetup.BatotoSetup(values, db);
            }

            publishProgress(params[0]);
            return null;
        }

        @Override
        protected void onProgressUpdate(final String... tableName) {
            pickSource = tableName[0];
            String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
            final int[] to = {R.id.site_search_content};
            Cursor selectQuery = db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry._ID + ", " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + ", " +
                    DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER + " FROM " +
                    pickSource, null);


            directoryAdapter = new DirectoryAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            simpleCursorAdapter = new SimpleCursorAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            FilterQueryProvider filterQueryProvider = new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    searchString = constraint.toString();
                    return db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry._ID + ", " + // TODO: 5/11/2016 does not take into account genre filtering. fix; make query a separate string, and foreach the genres in
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER + " FROM " +
                            pickSource + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                            + " LIKE '%" + searchString + "%' ORDER BY " +
                            sortBy + " ASC", null);
                }
            };
            directoryAdapter.setFilterQueryProvider(filterQueryProvider);
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
                    directoryAdapter.getFilter().filter(s);
                    simpleCursorAdapter.notifyDataSetChanged();
                    directoryAdapter.notifyDataSetChanged();
                }
            });

            if(absListView.equals(mView.findViewById(R.id.directory_grid))) {
                absListView.setAdapter(directoryAdapter);
            }else{
                absListView.setAdapter(simpleCursorAdapter);
            }
            Log.d("c", "approached notify");
        }
    }

    public void ConfigureSearch(){
        Bundle bundle = new Bundle();
        bundle.putInt("sortByID", getSortByRadioID());
        bundle.putInt("pickSourceID", getPickSourceRadioID());
        DialogFragment dialog = new SettingsDialogFragment();
        dialog.setArguments(bundle);
        AppHome appHome = (AppHome) getActivity();
        appHome.setFragmentCalled(this);
        //dialog.set(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        dialog.show(getActivity().getFragmentManager(), "test");
    }

    public void requeryFromConfigure(String sortBy, String pickSource, ArrayList<String> genres){
        this.pickSource = pickSource;
        this.sortBy = sortBy;
        this.genres = genres;
        SQLiteDatabase db = ddbHelper.getWritableDatabase();

        String requeryString = "SELECT " + DirectoryContract.DirectoryEntry._ID + ", " +
                DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + ", " +
                DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER + " FROM " +
                pickSource;

        if(searchString != null) {
            requeryString += " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                    + " LIKE '%" + searchString + "%'";
        }

        if(!genres.isEmpty()) {
            if (searchString == null) {
                requeryString += " WHERE ";
            } else {
                requeryString += " AND ";
            }
        }

        for(String genre : genres){

            requeryString += DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES
                    + " LIKE '%" + genre + "%'";

            if(genres.indexOf(genre) != genres.size() - 1){
                requeryString += " AND ";
            }
        }

        requeryString += " ORDER BY " +
                sortBy + " ASC";
        Log.d("requeryFromConfigure", requeryString);
        Cursor requery = db.rawQuery(requeryString, null);

        directoryAdapter.changeCursor(requery);
        simpleCursorAdapter.changeCursor(requery);
        simpleCursorAdapter.notifyDataSetChanged();
        directoryAdapter.notifyDataSetChanged();

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



    class DirectoryAdapter extends SimpleCursorAdapter{

        public DirectoryAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            //Log.d("DirectoryAdapter", "getView started");
            View v = view;
            ImageView picture;
            TextView name;
            Cursor cursor;

            if (v == null) {
                v = mInflater.inflate(R.layout.gridview_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }


            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);
            cursor = (Cursor) getItem(i);

            String uriString = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            Log.d("coverURI", "testcover" + uriString);
            //Item item = (Item)getItem(i);
            Uri uri = null;
            if (uriString != null) {
                uri = Uri.parse(uriString);
            }

            picture.setImageURI(uri);
            //name.setText(item.name);
            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }

    }

}


