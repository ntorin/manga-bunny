package com.fruits.ntorin.mango.home.favorites;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.fruits.ntorin.mango.utils.BitmapFunctions;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.utils.RetainFragment;
import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.utils.UpdatesBootReceiver;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.sourcefns.Sources;
import com.fruits.ntorin.mango.title.DescriptionChapters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


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

    SimpleCursorAdapter favoritesListAdapter;
    FavoritesGridAdapter favoritesGridAdapter;
    DirectoryDbHelper ddbHelper;
    private View mView;
    private LayoutInflater mInflater;
    //private EditText mFilterText;
    private SearchView mSearchText;
    private int mFirstVisibleItem;
    private LruCache<String, Bitmap> mMemoryCache;
    private Set<SoftReference<Bitmap>> mReusableBitmaps;

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
        //Log.d("FavoritesFragment", "onCreate started");
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

        ddbHelper = new DirectoryDbHelper(this.getContext());

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        setHasOptionsMenu(true);
        //Log.d("FavoritesFragment", "onCreate run Async ");
        ////Log.d("FavoritesFragment", getActivity().getIntent().getAction());

        boolean alarmUp = (PendingIntent.getBroadcast(getContext(), 0,
                new Intent("com.fruits.ntorin.mango.UPDATE_FAVORITES"),
                PendingIntent.FLAG_NO_CREATE) != null);
        if(!alarmUp) {
            //Log.d("UpdatesBootReceiver", "starting from FavoritesFragment");
            new UpdatesBootReceiver().onReceive(getContext(), new Intent("com.fruits.ntorin.mango.UPDATE_FAVORITES"));
        }else{
            //Log.d("FavoritesFragment", "already updating");
        }
        //Log.d("FavoritesFragment", "onCreate ended");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //inflater.inflate(R.menu.menu_home, menu);
        //Log.d("FavoritesFragment", "onCreateOptionsMenu started");
        mSearchText = (SearchView) menu.findItem(R.id.action_search).getActionView();
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchText.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("searchtext", "clicked");
            }
        });
        MenuItem views = menu.findItem(R.id.list_view);
        if(absListView instanceof ListView){
            views.setIcon(R.drawable.ic_view_module_white_24dp);
        }
        mSearchText.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //Log.d("textclose", "closed");

                searchItem.collapseActionView();

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return false;
            }
        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        MenuItem listitem = menu.findItem(R.id.list_view);
        if (listitem.getIcon().getConstantState().equals(ResourcesCompat.getDrawable(
                getResources(), R.drawable.ic_view_list_white_24dp, null).getConstantState()) && sharedPreferences.getString(Settings.PREF_FAVORITES_LISTINGS, "1").equals("1")) {
            ToggleViews(listitem);
        }
        new AsyncFetchDirectory(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME);

        //Log.d("FavoritesFragment", "onCreateOptionsMenu ended");
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
                if(mSearchText != null) { // TODO: 7/21/2016 maybe has errors
                    mSearchText.setIconified(false);
                }
                return true;

            case R.id.list_view:
                ToggleViews(item);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ToggleViews(MenuItem item) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        if(item.getIcon().getConstantState().equals(ResourcesCompat.getDrawable(
                getResources(), R.drawable.ic_view_list_white_24dp, null).getConstantState())){
            item.setIcon(R.drawable.ic_view_module_white_24dp);
            mView.findViewById(R.id.favorites_grid).setVisibility(View.GONE);
            absListView = (AbsListView) mView.findViewById(R.id.favorites_list);
            if (absListView.getOnItemClickListener() == null) {
                setListener();
            }
            absListView.setAdapter(favoritesListAdapter);
            mView.findViewById(R.id.favorites_list).setVisibility(View.VISIBLE);
            editor.putString(Settings.PREF_FAVORITES_LISTINGS, "1");
            editor.apply();
            //Log.d("tolist", "request list");
        }else{
            item.setIcon(R.drawable.ic_view_list_white_24dp);
            mView.findViewById(R.id.favorites_list).setVisibility(View.GONE);
            absListView = (AbsListView) mView.findViewById(R.id.favorites_grid);
            if (absListView.getOnItemClickListener() == null) {
                setListener();
            }
            absListView.setAdapter(favoritesGridAdapter);
            mView.findViewById(R.id.favorites_grid).setVisibility(View.VISIBLE);
            editor.putString(Settings.PREF_FAVORITES_LISTINGS, "0");
            editor.apply();
            //Log.d("togrid", "request grid");
        }
        absListView.setSelection(mFirstVisibleItem);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mInflater = inflater;
        mView = inflater.inflate(R.layout.fragment_favorites, container, false);
        absListView = (AbsListView) mView.findViewById(R.id.favorites_grid);
        //mFilterText = (EditText) mView.findViewById(R.id.editText);

        TextView t = (TextView) mView.findViewById(R.id.empty_display);


        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");

        t.setTypeface(tf);

        setListener();

        return mView;
    }


    public void setListener(){
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                Intent intent = new Intent(FavoritesFragment.this.getContext(), DescriptionChapters.class);
                Bundle bundle = new Bundle();
                Cursor cursor = favoritesListAdapter.getCursor();
                cursor.moveToPosition(position);
                String title = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
                String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));

                if(cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED)) == 1){
                    //Log.d("updated", "item: " + title + " listed as updated == 1");
                    SQLiteDatabase db = ddbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED, 0);
                    db.update(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME, values,
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);

                    String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
                    int[] to = {R.id.site_search_content};
                    String query = "SELECT * FROM " +
                            DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME;
                    if(mSearchText.getQuery().toString() != null){
                        String verifiedString = mSearchText.getQuery().toString().replace("'", "''");
                        query += " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                                + " LIKE '%" + verifiedString + "%'";
                    }
                     query += " ORDER BY "+ DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED + " DESC, "
                             + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                             + " ASC";
                    Cursor selectQuery = db.rawQuery(query, null);



                    favoritesGridAdapter = new FavoritesGridAdapter(getContext(), R.layout.site_item, selectQuery, from, to, 0);
                    favoritesListAdapter = new FavoritesListAdapter(getContext(), R.layout.site_item, selectQuery, from, to, 0);
                    favoritesListAdapter.notifyDataSetChanged();
                    favoritesGridAdapter.notifyDataSetChanged();

                    if (absListView instanceof ListView){
                        //Log.d("instanceof", "ListView");
                        absListView.setAdapter(favoritesListAdapter);
                    }else if(absListView instanceof GridView){
                        //Log.d("instanceof", "GridView");
                        absListView.setAdapter(favoritesGridAdapter);
                    }

                }

                String coveruri = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
                Bitmap cover;
                Uri uri = null;
                if(coveruri != null) {
                     uri = Uri.parse(coveruri);
                }
                byte[] coverbytes = new byte[0];
                try {
                    cover = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    cover.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    coverbytes = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String author = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR));
                String artist = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST));
                String genres = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES));
                int statusint = cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS));
                boolean status;
                if (statusint == 1) {
                    status = true;
                } else {
                    status = false;
                }
                int src = cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE));
                double rank = cursor.getDouble(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_RANK));
                //test.close();
                bundle.putString("title", title);
                bundle.putString("href", href);
                bundle.putByteArray("cover", coverbytes);
                bundle.putString("author", author);
                bundle.putString("artist", artist);
                bundle.putString("genres", genres);
                bundle.putBoolean("status", status);
                bundle.putDouble("rank", rank);
                bundle.putInt("src", src);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        absListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //Log.d("onScrollStateChanged", " " + scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                ////Log.d("onScrollStateChanged", " " + visibleItemCount + " first: " + firstVisibleItem);
                mFirstVisibleItem = firstVisibleItem;
            }
        });
        absListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Cursor cursor = favoritesListAdapter.getCursor();
                cursor.moveToPosition(position);

                final String[] choices = {"Remove from favories"};
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.text_item, choices) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View v = mInflater.inflate(R.layout.chlist_item, parent, false);
                        TextView t = (TextView) v.findViewById(R.id.text);
                        t.setText(getItem(position));
                        return v;
                    }
                };

                AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                        .setTitle("Favorites Options")
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d("FavoritesLongClick", "" + adapter.getItem(which));
                                SQLiteDatabase db = ddbHelper.getWritableDatabase();
                                db.delete(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME,
                                        DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" +
                                                cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF)) +
                                                "\'", null);


                                String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
                                int[] to = {R.id.site_search_content};
                                String query = "SELECT * FROM " +
                                        DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME;
                                if(mSearchText.getQuery().toString() != null){
                                    String verifiedString = mSearchText.getQuery().toString().replace("'", "''");
                                    query += " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                                            + " LIKE '%" + verifiedString + "%'";
                                }
                                 query += " ORDER BY "+ DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED + " DESC, "
                                         + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                                         + " ASC";
                                Cursor selectQuery = db.rawQuery(query, null);

                                favoritesGridAdapter = new FavoritesGridAdapter(getContext(), R.layout.site_item, selectQuery, from, to, 0);
                                favoritesListAdapter = new FavoritesListAdapter(getContext(), R.layout.site_item, selectQuery, from, to, 0);
                                favoritesListAdapter.notifyDataSetChanged();
                                favoritesGridAdapter.notifyDataSetChanged();
                                absListView.invalidateViews();

                                if (absListView instanceof ListView){
                                    //Log.d("instanceof", "ListView");
                                    absListView.setAdapter(favoritesListAdapter);
                                }else if(absListView instanceof GridView){
                                    //Log.d("instanceof", "GridView");
                                    absListView.setAdapter(favoritesGridAdapter);
                                }
                            }
                        })
                        .create();
                dialog.show();
                return true;
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

    private class AsyncFetchDirectory extends AsyncTask<String, Cursor, Void> {

        ContentValues values = new ContentValues();
        SQLiteDatabase db = ddbHelper.getWritableDatabase();
        Fragment fragment;

        public AsyncFetchDirectory(Fragment a){
            fragment = a;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mFirstVisibleItem = 0;
            String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
            int[] to = {R.id.site_search_content};
            Cursor selectQuery = db.rawQuery("SELECT * FROM " +
                    params[0] + " ORDER BY "+ DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED + " DESC, "
                    + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                    + " ASC", null);
            favoritesGridAdapter = new FavoritesGridAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            favoritesListAdapter = new FavoritesListAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);

            FilterQueryProvider filterQueryProvider = new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    String verifiedString = constraint.toString().replace("'", "''");
                    return db.rawQuery("SELECT * FROM " +
                            params[0] + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                            + " LIKE '%" + verifiedString + "%' ORDER BY "+ DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED + " DESC, "
                            + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                            + " ASC", null);
                }
            };
            favoritesGridAdapter.setFilterQueryProvider(filterQueryProvider);
            favoritesListAdapter.setFilterQueryProvider(filterQueryProvider);
            publishProgress(selectQuery);
            return null;
        }

        @Override
        protected void onProgressUpdate(final Cursor... cursors) {
            ToggleResults(cursors[0]);
            mSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    //Log.d("textsubmit", " " + mSearchText.getQuery());
                    mSearchText.clearFocus();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mFirstVisibleItem = 0;
                    //Log.d("textchange", "" + mSearchText.getQuery());
                    favoritesListAdapter.getFilter().filter(newText);
                    favoritesGridAdapter.getFilter().filter(newText);
                    favoritesListAdapter.notifyDataSetChanged();
                    favoritesGridAdapter.notifyDataSetChanged();
                    return false;
                }
            });

            if(absListView.equals(mView.findViewById(R.id.favorites_grid))) {
                absListView.setAdapter(favoritesGridAdapter);
            }else{
                absListView.setAdapter(favoritesListAdapter);
            }
            //Log.d("c", "approached notify");
        }
    }

    private void ToggleResults(Cursor cursor) {
        if(cursor.getCount() > 0){
            mView.findViewById(R.id.empty_display).setVisibility(View.GONE);
        }else{
            mView.findViewById(R.id.empty_display).setVisibility(View.VISIBLE);
        }
    }

    public void UpdateFavorites(Context context){
        //new AsyncUpdateFavorites().execute(context);
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



    class FavoritesGridAdapter extends SimpleCursorAdapter{
        int counter = 0;
        public FavoritesGridAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            //Log.d("counter++", "" + counter++);
            ////Log.d("favoritesGridAdapter", "getView started");
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
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
            name.setTypeface(tf);
            cursor = (Cursor) getItem(i);

            ImageView status = (ImageView) v.findViewById(R.id.completion_status);
            if (cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS)) == 1) {
                status.setImageResource(R.drawable.ic_check_box_white_24dp);
            } else {
                status.setImageResource(R.drawable.ic_indeterminate_check_box_white_24dp);
            }

            TextView source = (TextView) v.findViewById(R.id.authortext);
            String sourcename = Sources.getSourceString(cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE)));
            source.setText(sourcename);
            source.setTypeface(tf);

            //Item item = (Item)getItem(i);

            if(cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED)) == 1){
                Toolbar toolbar = (Toolbar) v.findViewById(R.id.gridview_toolbar);
                toolbar.setBackgroundColor(Color.argb(225, 0, 47, 64));
            }else{
                Toolbar toolbar = (Toolbar) v.findViewById(R.id.gridview_toolbar);
                toolbar.setBackgroundColor(Color.argb(187, 0, 0, 0));
            }
            String coverURI = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            if(coverURI != null) {
                Uri uri = Uri.parse(coverURI); //// FIXME: 4/11/2016  android.database.StaleDataException: Attempting to access a closed CursorWindow.Most probable cause: cursor is deactivated prior to calling this method.
                if(getActivity() != null) {
                    BitmapFunctions.loadBitmap(uri, picture, getResources(), mMemoryCache, mReusableBitmaps, 1);
                }
            }
            //name.setText(item.name);
            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }

    }

    class FavoritesListAdapter extends SimpleCursorAdapter{

        public FavoritesListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            //Log.d("favoritesListAdapter", "getView started");
            View v = view;
            ImageView picture;
            TextView name;
            Cursor cursor;

            if(v == null)
            {
                v = mInflater.inflate(R.layout.listview_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }

            picture = (ImageView)v.getTag(R.id.picture);
            name = (TextView)v.getTag(R.id.text);
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
            name.setTypeface(tf);
            cursor = (Cursor) getItem(i);
            cursor.moveToPosition(i);

            TextView author = (TextView) v.findViewById(R.id.authortext);
            author.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR)));
            author.setTypeface(tf);

            String artistString = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST));

            TextView artist = (TextView) v.findViewById(R.id.artisttext);
            ImageView artistimg = (ImageView) v.findViewById(R.id.artistimg);
            if(artistString != null && !artistString.equals("")){
                artistimg.setVisibility(View.VISIBLE);
                artist.setText(artistString);
                artist.setTypeface(tf);
            }else{
                artist.setText("");
                artistimg.setVisibility(View.INVISIBLE);
            }

            TextView genres = (TextView) v.findViewById(R.id.genretext);
            genres.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES)));
            genres.setTypeface(tf);

            ////Log.d("getView", "" + getItem(position).getStatus());
            ImageView status = (ImageView) v.findViewById(R.id.completion_status);
            if (cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS)) == 1) {
                status.setImageResource(R.drawable.ic_check_box_white_24dp);
            } else {
                status.setImageResource(R.drawable.ic_indeterminate_check_box_white_24dp);
            }

            TextView source = (TextView) v.findViewById(R.id.sourcetext);
            String sourcename = Sources.getSourceString(cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE)));
            source.setText(sourcename);
            source.setTypeface(tf);

            //Item item = (Item)getItem(i);

            if(cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED)) == 1){
                LinearLayout layout = (LinearLayout) v.findViewById(R.id.listview_layout);
                layout.setBackgroundColor(Color.argb(225, 0, 47, 64));
            }else{
                LinearLayout layout = (LinearLayout) v.findViewById(R.id.listview_layout);
                layout.setBackgroundColor(0);
            }

            String coverURI = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            if(coverURI != null) {
                Uri uri = Uri.parse(coverURI); //// FIXME: 4/11/2016  android.database.StaleDataException: Attempting to access a closed CursorWindow.Most probable cause: cursor is deactivated prior to calling this method.
                if(getActivity() != null) {
                    BitmapFunctions.loadBitmap(uri, picture, getResources(), mMemoryCache, mReusableBitmaps, 1);
                }
            }
            //picture.setImageURI(uri);
            //name.setText(item.name);
            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            //Log.d("favoritesListAdapter", "getView ended");
            return v;
        }

    }

}


