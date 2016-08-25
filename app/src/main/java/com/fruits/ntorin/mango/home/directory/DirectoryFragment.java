package com.fruits.ntorin.mango.home.directory;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.fruits.ntorin.dstore.mangaHereApi.model.MangaHere;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.utils.RetainFragment;
import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.home.AppHome;
import com.fruits.ntorin.mango.home.SettingsDialogFragment;
import com.fruits.ntorin.mango.packages.TitleResponsePackage;
import com.fruits.ntorin.mango.sourcefns.GoodMangaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaFoxFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaHereFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaInnFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaPandaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaReaderFunctions;
import com.fruits.ntorin.mango.sourcefns.Sources;
import com.fruits.ntorin.mango.title.DescriptionChapters;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import com.fruits.ntorin.mango.datastore.AsyncEndpointsTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DirectoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectoryFragment extends Fragment {

    public static Bitmap testbmp;
    private OnFragmentInteractionListener mListener;
    private AbsListView absListView;

    private TitleListAdapter titleListAdapter;
    private TitleGridAdapter titleGridAdapter;
    private DirectoryDbHelper ddbHelper;
    private View mView;
    private LayoutInflater mInflater;
    private SearchView mSearchText;
    private String pickSource = DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME;
    private int source = Sources.MANGAFOX;
    private String sortBy = DirectoryContract.DirectoryEntry.COLUMN_NAME_RANK;
    private ArrayList<String> genres;
    private int pickSourceRadioID;
    private int sortByRadioID;
    private Cursor selectQuery;
    private String mToken;
    private ArrayList<Title> mTitles;
    private String mReplaceQuery;

    private String searchString;
    private String genrelist;
    private String sort;
    private boolean mAllLoaded = false;
    private int mFirstVisibleItem;
    private String completion;
    private LruCache<String, Bitmap> mMemoryCache;
    private Set<SoftReference<Bitmap>> mReusableBitmaps;

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d("DirectoryFragment", "destroyed");
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        setHasOptionsMenu(true);
        ddbHelper = new DirectoryDbHelper(this.getContext());
        if(savedInstanceState != null){
            //Log.d("onCreate", "loading instance");
            String replaceQuery = savedInstanceState.getString("replaceQuery");
            mReplaceQuery = replaceQuery;
            genrelist = savedInstanceState.getString("genrelist");
            sort = savedInstanceState.getString("sort");
            completion = savedInstanceState.getString("completion");
            //new AsyncFetchDirectory("" + replaceQuery, genrelist, sort, completion).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }else {


        //Log.d("DirectoryFragment", "onCreate run Async " + pickSource);
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(sharedPreferences.getBoolean(Settings.PREF_AUTOLOAD_DIRECTORY, true)) {
                new AsyncFetchDirectory(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }else{
            //Log.d("DirectoryFragment", "autoload directory disabled");
        }
        //new AsyncEndpointsTask().execute(new Pair<Context, String>(getContext(), ""));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Log.d("onSaveInstanceState", "saving");
        String replaceQuery = "";
        if (mSearchText != null) {
            replaceQuery = mSearchText.getQuery().toString().replaceAll(",|=|[(]|[)]|\"", " ");
        }
        outState.putString("replaceQuery", replaceQuery);
        outState.putString("genrelist", genrelist);
        outState.putString("sort", sort);
        outState.putString("completion", completion);
        //outState.putSerializable("titles", mTitles);
        //outState.putString("token", mToken);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_home, menu);
        //mFilterText = (EditText) menu.findItem(R.id.action_search).getActionView();
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        /* MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //Log.d("searchCollapse", "search collapsed and closed");
                new AsyncFetchDirectory("", "", "", "").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                MenuItemCompat.setOnActionExpandListener(item, null);
                return false;
            }
        }); */
        mSearchText = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if(mReplaceQuery != null){
            mSearchText.setQuery(mReplaceQuery, false);
        }
        MenuItem views = menu.findItem(R.id.list_view);
        if (absListView instanceof ListView) {
            views.setIcon(R.drawable.ic_view_module_white_24dp);
        }
        //mSearchText.requestFocusFromTouch();

        /*MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                //Log.d("a", "expanding?");
                item.expandActionView();
                mSearchText.setIconified(false);
                mSearchText.requestFocus();
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return false;
            }
        });*/

        mSearchText.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("searchclick", "clicked");
                //mSearchText.setFocusable(true);
                //mSearchText.setIconified(false);
                //mSearchText.requestFocusFromTouch();
                //menuItem.setVisible(true);
                //mSearchText.setIconifiedByDefault(false);
                //mSearchText.setIconifiedByDefault(false);
                //mSearchText.requestFocusFromTouch();
                //InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        mSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchText.clearFocus();
                //Log.d("textsubmit", "" + mSearchText.getQuery());
                String replaceQuery = mSearchText.getQuery().toString().replaceAll(",|=|[(]|[)]|\"", " ");
                //Log.d("replacedquery", "" + replaceQuery);
                new AsyncFetchDirectory("" + replaceQuery, genrelist, sort, completion).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.d("textchange", "" + mSearchText.getQuery());
                return false;
            }
        });

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
        //Log.d("testingpref", sharedPreferences.getString(Settings.PREF_DIRECTORY_LISTINGS, "1") + "");
        if (listitem.getIcon().getConstantState().equals(ResourcesCompat.getDrawable(
                getResources(), R.drawable.ic_view_list_white_24dp, null).getConstantState()) && sharedPreferences.getString(Settings.PREF_DIRECTORY_LISTINGS, "1").equals("1")) {
            ToggleViews(listitem);
        }

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

            //case R.id.fillDB:
            //new AsyncFetchDirectory(this).execute(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME);
            //    return true;

            case R.id.action_search:
                //Log.d("search", "clicked");
                //item.expandActionView();
                if(mSearchText != null) { // TODO: 7/21/2016 maybe has errors
                    mSearchText.setIconified(false);
                }

                //mSearchText.requestFocus();
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
        if (item.getIcon().getConstantState().equals(ResourcesCompat.getDrawable(
                getResources(), R.drawable.ic_view_list_white_24dp, null).getConstantState())) {
            item.setIcon(R.drawable.ic_view_module_white_24dp);
            mView.findViewById(R.id.directory_grid).setVisibility(View.GONE);
            absListView = (AbsListView) mView.findViewById(R.id.directory_list);
            if (absListView.getOnItemClickListener() == null) {
                setListener();
            }
            absListView.setAdapter(titleListAdapter);
            mView.findViewById(R.id.directory_list).setVisibility(View.VISIBLE);
            editor.putString(Settings.PREF_DIRECTORY_LISTINGS, "1");
            editor.apply();
            //Log.d("tolist", "request list");
        } else {
            item.setIcon(R.drawable.ic_view_list_white_24dp);
            mView.findViewById(R.id.directory_list).setVisibility(View.GONE);
            absListView = (AbsListView) mView.findViewById(R.id.directory_grid);
            if (absListView.getOnItemClickListener() == null) {
                setListener();
            }
            absListView.setAdapter(titleGridAdapter);
            mView.findViewById(R.id.directory_grid).setVisibility(View.VISIBLE);
            editor.putString(Settings.PREF_DIRECTORY_LISTINGS, "0");
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
        mView = inflater.inflate(R.layout.fragment_directory, container, false);
        absListView = (AbsListView) mView.findViewById(R.id.directory_grid);
        TextView t = (TextView) mView.findViewById(R.id.empty_display);


        /*if(savedInstanceState != null){
            titleGridAdapter = new TitleGridAdapter(getContext(), R.layout.site_item, mTitles);
            titleListAdapter = new TitleListAdapter(getContext(), R.layout.site_item, mTitles);
            if (absListView.equals(mView.findViewById(R.id.directory_grid))) {
                if(titleGridAdapter != null) {
                    absListView.setAdapter(titleGridAdapter);
                }
            } else {
                if(titleListAdapter != null) {
                    absListView.setAdapter(titleListAdapter);
                }
            }
        }*/


        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");

        t.setTypeface(tf);

        setListener();

        return mView;
    }


    public void setListener() {
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                //Log.d("abslistviewclick", "clicked");
                Intent intent = new Intent(DirectoryFragment.this.getContext(), DescriptionChapters.class);
                Bundle bundle = new Bundle();
                Title t = (Title) absListView.getAdapter().getItem(position);
                //Cursor cursor = (Cursor) titleListAdapter.getCursor();
                //cursor.moveToPosition(position);
                //String title = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
                //String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                //String cover = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
                //test.close();
                String title = t.getTitle();
                String href = t.getHref();
                Bitmap cover = t.getCover();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (cover != null) {
                    cover.compress(Bitmap.CompressFormat.PNG, 100, baos);
                }
                byte[] coverbytes = baos.toByteArray();

                //bundle.putSerializable("titlepack", t);
                bundle.putString("title", title);
                bundle.putString("href", href);

                bundle.putString("author", t.getAuthor());
                bundle.putString("artist", t.getArtist());
                bundle.putString("genres", t.getGenres());
                bundle.putBoolean("status", t.getStatus());
                bundle.putDouble("rank", t.getRank());

                bundle.putByteArray("cover", coverbytes);

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

    public void ConfigureSearch() {
        Bundle bundle = new Bundle();
        bundle.putInt("sortByID", getSortByRadioID());
        bundle.putInt("pickSourceID", getPickSourceRadioID());
        bundle.putString("genreselected", genrelist);
        DialogFragment dialog = new SettingsDialogFragment();
        dialog.setArguments(bundle);
        AppHome appHome = (AppHome) getActivity();
        appHome.setFragmentCalled(this);
        //dialog.set(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        dialog.show(getActivity().getFragmentManager(), "SettingsDialog");
    }

    public void requeryFromConfigure(String sortBy, String pickSource, ArrayList<String> genres, String completion) {
        this.pickSource = pickSource;
        this.sortBy = sortBy;
        this.genres = genres;
        this.completion = completion.toLowerCase();
        this.sort = sortBy.toLowerCase();

        if (genres != null) {
            genrelist = "";

            for (String genre : genres) {
                genrelist += genre + " ";
            }
        } else {
            genrelist = null;
        }

        //Log.d("requeryFromConfigure", "genres: " + genrelist + " sort: " + sort + " search: " + mSearchText.getQuery().toString() + "completion: " + this.completion);

        new AsyncFetchDirectory(mSearchText.getQuery().toString(), genrelist, sort, this.completion).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if(titleListAdapter != null && titleGridAdapter != null) {
            titleListAdapter.notifyDataSetChanged();
            titleGridAdapter.notifyDataSetChanged();
        }

        //SQLiteDatabase db = ddbHelper.getWritableDatabase();

        /*String requeryString = "SELECT " + DirectoryContract.DirectoryEntry._ID + ", " +
                DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + ", " +
                DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER + " FROM " +
                pickSource;

        if (searchString != null) {
            requeryString += " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                    + " LIKE '%" + searchString + "%'";
        }

        if (!genres.isEmpty()) {
            if (searchString == null) {
                requeryString += " WHERE ";
            } else {
                requeryString += " AND ";
            }
        }

        for (String genre : genres) {
            genrelist += genre + " ";

            requeryString += DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES
                    + " LIKE '%" + genre + "%'";

            if (genres.indexOf(genre) != genres.size() - 1) {
                requeryString += " AND ";
            }
        }

        requeryString += " ORDER BY " +
                sortBy + " ASC";
        //Log.d("requeryFromConfigure", requeryString);
        Cursor requery = db.rawQuery(requeryString, null); */

        //titleGridAdapter.changeCursor(requery);
        //titleListAdapter.changeCursor(requery);

    }

    class AsyncAddTitles extends AsyncTask<Void, TitleResponsePackage, Void>{

        ProgressDialog pdialog;
        String searchText;
        public AsyncAddTitles(String searchText){
            this.searchText = searchText;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdialog = new ProgressDialog(getContext());
            pdialog.setMessage("Loading ...");
            pdialog.setCancelable(false);
            pdialog.setCanceledOnTouchOutside(false);
            pdialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if ((this.pdialog != null) && this.pdialog.isShowing()) {
                    this.pdialog.dismiss();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                this.pdialog = null;
            }
        }

        @Override
        protected void onProgressUpdate(TitleResponsePackage... titleResponsePackage) {
            super.onProgressUpdate(titleResponsePackage);
            if (titleResponsePackage[0].getItems() != null && mTitles != null) {
                mTitles.addAll(titleResponsePackage[0].getItems());
                titleListAdapter.notifyDataSetChanged();
                titleGridAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!mAllLoaded) {

                TitleResponsePackage titleResponsePackage = null;
                switch (Sources.getSelectedSource()) {
                    case Sources.MANGAHERE:
                        //Log.d("AsyncFetchDirectory", "starting AsyncGetDirectory");
                        titleResponsePackage = MangaHereFunctions.GetDirectory(new String[]{searchText, genrelist, sort, mToken, completion});
                        break;
                    case Sources.MANGAFOX:
                        titleResponsePackage = MangaFoxFunctions.GetDirectory(new String[]{searchText, genrelist, sort, mToken, completion});
                        break;
                    case Sources.MANGAREADER:
                        titleResponsePackage = MangaReaderFunctions.GetDirectory(new String[]{searchText, genrelist, sort, mToken, completion});
                        break;
                    case Sources.GOODMANGA:
                        titleResponsePackage = GoodMangaFunctions.GetDirectory(new String[]{searchText, genrelist, sort, mToken, completion});
                        break;
                    case Sources.MANGAPANDA:
                        titleResponsePackage = MangaPandaFunctions.GetDirectory(new String[]{searchText, genrelist, sort, mToken, completion});
                        break;
                    case Sources.MANGAINN:
                        titleResponsePackage = MangaInnFunctions.GetDirectory(new String[]{searchText, genrelist, sort, mToken, completion});
                        break;
                }
                //Log.d("addTitles", "starting AsyncGetDirectory");
                //titleResponsePackage =  MangaHereFunctions.GetDirectory(new String[]{searchText, genrelist, sort, mToken, completion});
                publishProgress(titleResponsePackage);
                mToken = titleResponsePackage.getNextPageToken();
                //Log.d("mTokenExtra", "" + mToken);
                if (mToken == null) {
                    mAllLoaded = true;
                }
            }
            return null;
        }
    }

    /* private void addTitles() {
        if (!mAllLoaded) {
            TitleResponsePackage titleResponsePackage = null;
            try {
                //Log.d("addTitles", "starting AsyncGetDirectory");
                titleResponsePackage = new MangaHereFunctions.AsyncGetDirectory(getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mSearchText.getQuery().toString(), genrelist, sort, mToken, completion).get();
                if (titleResponsePackage.getItems() != null) {
                    mTitles.addAll(titleResponsePackage.getItems());
                    titleListAdapter.notifyDataSetChanged();
                    titleGridAdapter.notifyDataSetChanged();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            mToken = titleResponsePackage.getNextPageToken();
            //Log.d("mTokenExtra", "" + mToken);
            if (mToken == null) {
                mAllLoaded = true;
            }
        }
    } */

    /*private class AsyncUpdateDirectory extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            DirectorySetup.UpdateDirectory(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME,
                    "http://www.mangahere.co/mangalist/", ddbHelper.getWritableDatabase(), getContext());
            return null;
        }
    }*/

    private void ToggleResults() {
        if (mTitles != null) {
            //Log.d("ToggleResults", "mtitles size: " + mTitles.size());
            if (mTitles.size() > 0) {
                mView.findViewById(R.id.empty_display).setVisibility(View.GONE);
                ImageView img = (ImageView) mView.findViewById(R.id.bg_img);
                img.setImageResource(R.drawable.book_bg);
            } else {
                mView.findViewById(R.id.empty_display).setVisibility(View.VISIBLE);
                ImageView img = (ImageView) mView.findViewById(R.id.bg_img);
                img.setImageResource(R.drawable.help_bg);
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

    private class AsyncFetchDirectory extends AsyncTask<Void, TitleResponsePackage, Void> {

        String completion;
        ContentValues values = new ContentValues();
        SQLiteDatabase db = ddbHelper.getWritableDatabase();
        Fragment fragment;
        String genrelist;
        String sort;
        String searchString;
        ProgressDialog pdialog;

        public AsyncFetchDirectory(Fragment a) {
            fragment = a;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if ((this.pdialog != null) && this.pdialog.isShowing()) {
                    this.pdialog.dismiss();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                this.pdialog = null;
            }
            mAllLoaded = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdialog = new ProgressDialog(getContext());
            pdialog.setMessage("Loading ...");
            pdialog.setCancelable(false);
            pdialog.setCanceledOnTouchOutside(false);
            pdialog.show();
        }

        public AsyncFetchDirectory(String searchString) {
            this.searchString = searchString;
        }

        public AsyncFetchDirectory(String searchString, String genrelist, String sort, String completion) {
            this.searchString = searchString;
            this.genrelist = genrelist;
            this.sort = sort;
            this.completion = completion;
        }

        @Override
        protected Void doInBackground(Void... params) {
             /*List<String> mLines = new ArrayList<>();

            AssetManager am = getContext().getAssets();

            try {
                InputStream is = am.open("licensed.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = reader.readLine()) != null) {
                    mLines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(String line : mLines){
                if(line.trim().equals("")){
                    Log.d("setup", "skip blank line");
                    continue;
                }
                Log.d("setup", "search deleting " + line);
                MangaFoxFunctions.searchDeleteMangaFox(line);
                GoodMangaFunctions.searchDeleteGoodManga(line);
                MangaPandaFunctions.searchDeleteMangaPanda(line);
                MangaInnFunctions.searchDeleteMangaInn(line);
            } */
            /*Log.d("setup", "mfox starting");
            MangaFoxFunctions.Setup();
            Log.d("setup", "mfox done");
            Log.d("setup", "mhere starting");
            MangaHereFunctions.Setup();
            Log.d("setup", "mhere done");
            Log.d("setup", "gmanga starting");
            GoodMangaFunctions.Setup();
            Log.d("setup", "gmanga done");
            Log.d("setup", "minn starting");
            MangaInnFunctions.Setup();
            Log.d("setup", "minn done");
            Log.d("setup", "mreader starting");
            MangaReaderFunctions.Setup();
            Log.d("setup", "mreader done");
            Log.d("setup", "mpanda starting");
            MangaPandaFunctions.Setup();
            Log.d("setup", "mpanda done"); */

            switch (Sources.getSelectedSource()) {
                case Sources.MANGAFOX:
                    break;
                case Sources.MANGAHERE:
                    break;
                case Sources.KISSMANGA:
                    break;
                case Sources.GOODMANGA:
                    break;
                case Sources.MANGAPANDA:
                    break;
                case Sources.MANGAREADER:
                    break;
                case Sources.MANGAINN:
                    break;
            }

            TitleResponsePackage titleResponsePackage = null;
            switch (Sources.getSelectedSource()) {
                case Sources.MANGAHERE:
                    titleResponsePackage = MangaHereFunctions.GetDirectory(new String[]{searchString, genrelist, sort, completion});

                    break;
                case Sources.MANGAPANDA:
                    titleResponsePackage = MangaPandaFunctions.GetDirectory(new String[]{searchString, genrelist, sort, completion});
                    break;
                case Sources.MANGAINN:
                    titleResponsePackage = MangaInnFunctions.GetDirectory(new String[]{searchString, genrelist, sort, completion});
                    break;
                case Sources.MANGAFOX:
                    titleResponsePackage = MangaFoxFunctions.GetDirectory(new String[]{searchString, genrelist, sort, completion});
                    break;
                case Sources.MANGAREADER:
                    titleResponsePackage = MangaReaderFunctions.GetDirectory(new String[]{searchString, genrelist, sort, completion});
                    break;
                case Sources.GOODMANGA:
                    titleResponsePackage = GoodMangaFunctions.GetDirectory(new String[]{searchString, genrelist, sort, completion});
            }
            mToken = titleResponsePackage.getNextPageToken();
            //Log.d("mToken", " " + mToken);
            mTitles = titleResponsePackage.getItems();
            mFirstVisibleItem = 0;
            //titleResponsePackage = new AsyncGetDirectory().execute(mToken).get();

            publishProgress(titleResponsePackage);
            return null;
        }

        @Override
        protected void onProgressUpdate(final TitleResponsePackage... titleResponsePackage) {

            ToggleResults();
            if (titleResponsePackage != null && titleResponsePackage[0].getItems() != null && mTitles != null && getContext() != null) {
                titleGridAdapter = new TitleGridAdapter(getContext(), R.layout.site_item, mTitles);
                titleListAdapter = new TitleListAdapter(getContext(), R.layout.site_item, mTitles);
            }

            if (absListView.equals(mView.findViewById(R.id.directory_grid))) {
                if (titleGridAdapter != null) {
                    absListView.setAdapter(titleGridAdapter);
                }
            } else {
                if (titleListAdapter != null) {
                    absListView.setAdapter(titleListAdapter);
                }
            }
            //Log.d("c", "approached notify");
        }
    }

    class DirectoryAdapter extends SimpleCursorAdapter {

        public DirectoryAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ////Log.d("DirectoryAdapter", "getView started");
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
            //Log.d("coverURI", "testcover" + uriString);
            //Item item = (Item)getItem(i);
            Uri uri = null;
            if (uriString != null) {
                uri = Uri.parse(uriString);
            }

            //picture.setImageURI(uri);
            //if (testbmp != null) {
            //picture.setImageBitmap(testbmp);
            //}
            //name.setText(item.name);
            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }

    }

    class TitleGridAdapter extends ArrayAdapter<Title> {


        public TitleGridAdapter(Context context, int resource, List<Title> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ////Log.d("DirectoryAdapter", "getView started");
            if(mTitles != null) {
                if (position == mTitles.size() - 1) {
                    //Log.d("OnScroll", "Last item loaded");
                    //addTitles();
                    new AsyncAddTitles(mSearchText.getQuery().toString()).execute();
                }
            }
            View v = convertView;
            ImageView picture;
            TextView name;
            //Cursor cursor;

            if (v == null) {
                v = mInflater.inflate(R.layout.gridview_item, parent, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }


            Toolbar titleBar = (Toolbar) v.findViewById(R.id.gridview_toolbar);
            titleBar.setClickable(false);
            titleBar.setOnClickListener(null);
            titleBar.setEnabled(false);
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");

            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);
            name.setTypeface(tf);
            //cursor = (Cursor) getItem(i);

            Title title = getItem(position);

            if (title.getCover() != null) {
                picture.setImageBitmap(title.getCover());
            } else {
                //Log.d("getView", "cover is null");
                picture.setImageResource(R.drawable.noimage);
                FrameLayout f = (FrameLayout) v.findViewById(R.id.frame);
                f.setBackgroundColor(Color.argb(225, 0, 0, 0));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    picture.setImageAlpha(255);
                } else {
                    picture.setAlpha(255);
                }
                //picture.setImageResource(R.drawable.ic_help_white_48dp);
            }
            name.setText(title.getTitle());

            TextView t = (TextView) v.findViewById(R.id.authortext);
            t.setText(title.getAuthor());
            t.setTypeface(tf);

            ////Log.d("getView", "" + getItem(position).getStatus());
            ImageView i = (ImageView) v.findViewById(R.id.completion_status);
            if (getItem(position).getStatus()) {
                i.setImageResource(R.drawable.ic_check_box_white_24dp);
            } else {
                i.setImageResource(R.drawable.ic_indeterminate_check_box_white_24dp);
            }

            //String uriString = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            // //Log.d("coverURI", "testcover" + uriString);
            //Item item = (Item)getItem(i);
            //Uri uri = null;
            //if (uriString != null) {
            //    uri = Uri.parse(uriString);
            //}

            //picture.setImageURI(uri);
            //if(testbmp != null) {
            //    picture.setImageBitmap(testbmp);
            //}
            //name.setText(item.name);
            //name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }
    }

    class TitleListAdapter extends ArrayAdapter<Title> {

        public TitleListAdapter(Context context, int resource, List<Title> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ////Log.d("DirectoryAdapter", "getView started");
            ////Log.d("loadmore?", "position: " + position + " mtitle size: " + (mTitles.size() - 1));
            if(mTitles != null) {
                if (position == mTitles.size() - 1) {
                    //Log.d("OnScroll", "Last item loaded");
                    //addTitles();
                    new AsyncAddTitles(mSearchText.getQuery().toString()).execute();
                }
            }
            View v = convertView;
            ImageView picture;
            TextView name;
            //Cursor cursor;

            if (v == null) {
                v = mInflater.inflate(R.layout.listview_item, parent, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }


            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);
            //cursor = (Cursor) getItem(i);

            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");

            Title title = getItem(position);

            if(title.getCover() != null) {
                picture.setImageBitmap(title.getCover());
            }
            name.setText(title.getTitle());
            name.setTypeface(tf);

            TextView author = (TextView) v.findViewById(R.id.authortext);
            author.setText(title.getAuthor());
            author.setTypeface(tf);


            TextView artist = (TextView) v.findViewById(R.id.artisttext);
            ImageView artistimg = (ImageView) v.findViewById(R.id.artistimg);
            if(title.getArtist() != null && !title.getArtist().equals("")){
                artistimg.setVisibility(View.VISIBLE);
                artist.setText(title.getArtist());
                artist.setTypeface(tf);
            }else{
                artist.setText("");
                artistimg.setVisibility(View.INVISIBLE);
            }


            TextView genres = (TextView) v.findViewById(R.id.genretext);
            genres.setText(title.getGenres());
            genres.setTypeface(tf);

            ////Log.d("getView", "" + getItem(position).getStatus());
            ImageView i = (ImageView) v.findViewById(R.id.completion_status);
            if (getItem(position).getStatus()) {
                i.setImageResource(R.drawable.ic_check_box_white_24dp);
            } else {
                i.setImageResource(R.drawable.ic_indeterminate_check_box_white_24dp);
            }

            //String uriString = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            // //Log.d("coverURI", "testcover" + uriString);
            //Item item = (Item)getItem(i);
            //Uri uri = null;
            //if (uriString != null) {
            //    uri = Uri.parse(uriString);
            //}

            //picture.setImageURI(uri);
            //if(testbmp != null) {
            //    picture.setImageBitmap(testbmp);
            //}
            //name.setText(item.name);
            //name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }
    }
}


