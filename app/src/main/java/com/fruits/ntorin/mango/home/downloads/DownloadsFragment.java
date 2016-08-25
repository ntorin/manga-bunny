package com.fruits.ntorin.mango.home.downloads;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import com.fruits.ntorin.mango.Help;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.utils.RetainFragment;
import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DownloadsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DownloadsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int FILE_CODE = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private DirectoryDbHelper ddbHelper;
    private DownloadsGridAdapter downloadsGridAdapter;
    private DownloadsListAdapter downloadsListAdapter;
    private LayoutInflater mInflater;
    private View mView;
    //private EditText mFilterText;
    private SearchView mSearchText;
    private AbsListView absListView;
    private int mFirstVisibleItem;
    private LruCache<String, Bitmap> mMemoryCache;
    private Set<SoftReference<Bitmap>> mReusableBitmaps;

    public DownloadsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DownloadsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DownloadsFragment newInstance(String param1, String param2) {
        DownloadsFragment fragment = new DownloadsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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

        ddbHelper = new DirectoryDbHelper(getContext());
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_downloads, menu);
        mSearchText = (SearchView) menu.findItem(R.id.action_search).getActionView();
        final MenuItem searchItem = menu.findItem(R.id.action_search);
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
                getResources(), R.drawable.ic_view_list_white_24dp, null).getConstantState()) && sharedPreferences.getString(Settings.PREF_DOWNLOADS_LISTINGS, "1").equals("1")) {
            ToggleViews(listitem);
        }
        new AsyncFetchDirectory(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.list_view:
                ToggleViews(item);
                return true;
            case R.id.action_upload:
                UploadFolder();
                return true;
            case R.id.action_search:
                if(mSearchText != null) { // TODO: 7/21/2016 maybe has errors
                    mSearchText.setIconified(false);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void UploadFolder() {
        Intent i = new Intent(getContext(), FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, FILE_CODE);
    }

    private void ToggleViews(MenuItem item) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        if(item.getIcon().getConstantState().equals(ResourcesCompat.getDrawable(
                getResources(), R.drawable.ic_view_list_white_24dp, null).getConstantState())){
            item.setIcon(R.drawable.ic_view_module_white_24dp);
            mView.findViewById(R.id.downloads_grid).setVisibility(View.GONE);
            absListView = (AbsListView) mView.findViewById(R.id.downloads_list);
            if (absListView.getOnItemClickListener() == null) {
                setListener();
            }
            absListView.setAdapter(downloadsListAdapter);
            mView.findViewById(R.id.downloads_list).setVisibility(View.VISIBLE);
            editor.putString(Settings.PREF_DOWNLOADS_LISTINGS, "1");
            editor.apply();
            //Log.d("tolist", "request list");
        }else{
            item.setIcon(R.drawable.ic_view_list_white_24dp);
            mView.findViewById(R.id.downloads_list).setVisibility(View.GONE);
            absListView = (AbsListView) mView.findViewById(R.id.downloads_grid);
            if (absListView.getOnItemClickListener() == null) {
                setListener();
            }
            absListView.setAdapter(downloadsGridAdapter);
            mView.findViewById(R.id.downloads_grid).setVisibility(View.VISIBLE);
            editor.putString(Settings.PREF_DOWNLOADS_LISTINGS, "0");
            editor.apply();
            //Log.d("togrid", "request grid");
        }
        absListView.setSelection(mFirstVisibleItem);

    }

    public void setListener() {
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                Intent intent = new Intent(DownloadsFragment.this.getContext(), DownloadsChapters.class);
                Bundle bundle = new Bundle();
                Cursor cursor = downloadsListAdapter.getCursor();
                cursor.moveToPosition(position);

                String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                String title = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
                String dir = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_DIR));
                String cover = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
                File f = new File(dir);

                if(cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED)) == 1){
                    //Log.d("updated?", "yes");
                    //Log.d("checking", "href: " + href + " ");
                    SQLiteDatabase db = ddbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED, 0);
                    db.update(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME, values,
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);

                    UpdateViews();
                }

                if(f.listFiles() != null) {

                    bundle.putString("title", title);
                    bundle.putString("dir", dir);
                    bundle.putString("cover", cover);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else{
                    AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                            .setMessage("The selected folder could not be found; it has either been " +
                                    "moved or deleted.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create();
                    dialog.show();
                }
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
                String toggleUpdates = "";
                final Cursor cursor = downloadsListAdapter.getCursor();
                cursor.moveToPosition(position);
                boolean isupload = false;


                final int update = cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE));
                switch (update) {
                    case 0:
                        toggleUpdates = "Enable automatic updates";
                        break;
                    case 1:
                        toggleUpdates = "Disable automatic updates";
                        break;
                    case 2:
                        isupload = true;
                        break;
                }

                final String[] choices = {toggleUpdates, "Remove title from list"/*, "Delete title from device"*/};

                final String[] uploadchoices = {"Remove title from list"};
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.text_item, choices) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View v = mInflater.inflate(R.layout.chlist_item, parent, false);
                        TextView t = (TextView) v.findViewById(R.id.text);
                        t.setText(getItem(position));
                        return v;
                    }
                };
                final ArrayAdapter<String> uploadadapter = new ArrayAdapter<String>(getContext(), R.layout.text_item, uploadchoices){

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View v = mInflater.inflate(R.layout.chlist_item, parent, false);
                        TextView t = (TextView) v.findViewById(R.id.text);
                        t.setText(getItem(position));
                        return v;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                        .setTitle("Downloads Options");
                        if(!isupload) {
                            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Log.d("DownloadsLongClick", "" + adapter.getItem(which));
                                    if (adapter.getItem(which).equals(choices[0])) {
                                        if (update != 3) {
                                            SQLiteDatabase db = ddbHelper.getWritableDatabase();
                                            ContentValues values = new ContentValues();
                                            if (update == 0) {
                                                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE, 1);
                                            } else if (update == 1) {
                                                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE, 0);
                                            }
                                            String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                                            db.update(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME, values,
                                                    DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);
                                            UpdateViews();
                                        }
                                    }

                                    if (adapter.getItem(which).equals(choices[1])) {
                                        AlertDialog removeDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                                                .setTitle("Remove Title")
                                                .setMessage("Are you sure you want to remove this " +
                                                        "title? It will not be deleted from your device, " +
                                                        "but it will not be able to be updated.")
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        SQLiteDatabase db = ddbHelper.getWritableDatabase();
                                                        String dir = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_DIR));
                                                        db.delete(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME,
                                                                DirectoryContract.DirectoryEntry.COLUMN_NAME_DIR + "=\'" + dir + "\'", null);
                                                        UpdateViews();
                                                    }
                                                }).create();
                                        removeDialog.show();
                                    }

                                /*if(adapter.getItem(which).equals(choices[2])){
                                    AlertDialog deleteDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                                            .setTitle("Delete Title")
                                            .setMessage("Are you sure you want to delete this " +
                                                    "title? (WARNING: It WILL be deleted from your device!)")
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    SQLiteDatabase db = ddbHelper.getWritableDatabase();
                                                    String uriString = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_DIR));
                                                    Uri uri = Uri.parse(uriString);
                                                    File f = new File(uri.getPath());
                                                    //Log.d("DeleteDirectory", "" + f.getName() + ", " + f.getAbsolutePath());
                                                    //if(f.exists()) {
                                                        f.delete();
                                                    //}
                                                    String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                                                    db.delete(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME,
                                                            DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);
                                                    UpdateViews();
                                                }
                                            }).create();
                                    deleteDialog.show();
                                }*/
                                }
                            });
                        }else{
                            builder.setAdapter(uploadadapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (adapter.getItem(which).equals(choices[0])) {
                                        AlertDialog removeDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                                                .setTitle("Remove Title")
                                                .setMessage("Are you sure you want to remove this " +
                                                        "title? It will not be deleted from your device, " +
                                                        "but it will not be able to be updated.")
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        SQLiteDatabase db = ddbHelper.getWritableDatabase();
                                                        String dir = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_DIR));
                                                        db.delete(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME,
                                                                DirectoryContract.DirectoryEntry.COLUMN_NAME_DIR + "=\'" + dir + "\'", null);
                                                        UpdateViews();
                                                    }
                                                }).create();
                                        removeDialog.show();
                                    }
                                }
                            });
                        }
                        AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
    }

    public void UpdateViews(){
        String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
        int[] to = {R.id.site_search_content};
        String query = "SELECT * FROM " +
                DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME;
        if(mSearchText.getQuery().toString() != null){
            String verifiedString = mSearchText.getQuery().toString().replace("'", "''");
            query += " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                    + " LIKE '%" + verifiedString + "%'";
        }
        SQLiteDatabase db = ddbHelper.getWritableDatabase();
        Cursor selectQuery = db.rawQuery(query, null);

        downloadsGridAdapter = new DownloadsGridAdapter(getContext(), R.layout.site_item, selectQuery, from, to, 0);
        downloadsListAdapter = new DownloadsListAdapter(getContext(), R.layout.site_item, selectQuery, from, to, 0);
        downloadsListAdapter.notifyDataSetChanged();
        downloadsGridAdapter.notifyDataSetChanged();

        if (absListView instanceof ListView){
            //Log.d("instanceof", "ListView");
            absListView.setAdapter(downloadsListAdapter);
        }else if(absListView instanceof GridView){
            //Log.d("instanceof", "GridView");
            absListView.setAdapter(downloadsGridAdapter);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mInflater = inflater;
        mView = inflater.inflate(R.layout.fragment_downloads, container, false);
        absListView = (AbsListView) mView.findViewById(R.id.downloads_grid);
        //mFilterText = (EditText) mView.findViewById(R.id.editText);

        TextView t = (TextView) mView.findViewById(R.id.empty_display);


        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");

        t.setTypeface(tf);

        setListener();

        //Log.d("DownloadsFragment", "view created");
        return mView;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            AddFileFolder(uri);
                            // Do something with the URI
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path: paths) {
                            Uri uri = Uri.parse(path);
                            AddFileFolder(uri);
                            // Do something with the URI
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                AddFileFolder(uri);
                // Do something with the URI
            }
        }
    }

    private void AddFileFolder(Uri uri) {
        //Log.d("onActivityResult", uri.getPath());
        File f = new File(uri.getPath());

        DirectoryDbHelper dbHelper = new DirectoryDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        //Log.d("AddFileFolder", f.getName());
        //Log.d("AddFileFolder", f.getPath());
        //Log.d("AddFileFolder", f.toURI().toString());
        File[] fi;
        File cover;
        if (f.listFiles() != null) {
            fi = f.listFiles();
            if(fi[0].listFiles() != null) {
                File[] fil = fi[0].listFiles();
                if(fil.length > 0) {
                    cover = fil[0];
                }else{
                    ShowBadUpload();
                    return;
                }
            }else{
                ShowBadUpload();
                return;
            }
        } else {
            ShowBadUpload();
            return;
        }
        //Log.d("AddFileFolder", fi[0].getPath());
        //Log.d("AddFileFolder", cover.getPath());

        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, f.getName());
        values.putNull(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF);
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER, cover.getPath());
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_DIR, f.getPath());
        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE, 2);
        values.putNull(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERS);

        db.insert(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME, null, values);

        db.close();
        UpdateViews();
    }

    private void ShowBadUpload() {
        //Log.d("ShowBadUpload", "bad upload");
        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                .setMessage("The selected folder is not organized in a way suitable " +
                        "for a file upload. For the correct organization, please check " +
                        "the \"Help\" page.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton("Help", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent helpIntent = new Intent(getContext(), Help.class);
                        startActivity(helpIntent);
                    }
                })
                .create();
        dialog.show();
    }

    private class AsyncFetchDirectory extends AsyncTask<String, String, Void> {

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
                    params[0] + " ORDER BY " + DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED + " DESC, "
                    + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + " ASC", null);


            downloadsGridAdapter = new DownloadsGridAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            downloadsListAdapter = new DownloadsListAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            FilterQueryProvider filterQueryProvider = new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    String verifiedString = constraint.toString().replace("'", "''");
                    return db.rawQuery("SELECT * FROM " +
                            params[0] + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                            + " LIKE '%" + verifiedString + "%' ORDER BY " + DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED + " DESC, "
                            + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + " ASC", null);
                }
            };
            downloadsGridAdapter.setFilterQueryProvider(filterQueryProvider);
            downloadsListAdapter.setFilterQueryProvider(filterQueryProvider);
            publishProgress(params[0]);
            return null;
        }

        @Override
        protected void onProgressUpdate(final String... tableName) {
            //Log.d("DownloadsFragment", "progress update starting");
            mSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    //Log.d("textsubmit", "" + mSearchText.getQuery());
                    mSearchText.clearFocus();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mFirstVisibleItem = 0;
                    //Log.d("textchange", "" + mSearchText.getQuery());
                    downloadsListAdapter.getFilter().filter(newText);
                    downloadsGridAdapter.getFilter().filter(newText);
                    downloadsListAdapter.notifyDataSetChanged();
                    downloadsGridAdapter.notifyDataSetChanged();
                    return false;
                }
            });

            if(absListView.equals(mView.findViewById(R.id.downloads_grid))) {
                //Log.d("FetchDownloads", "grid");
                absListView.setAdapter(downloadsGridAdapter);
            }else{
                //Log.d("FetchDownloads", "list");
                absListView.setAdapter(downloadsListAdapter);
            }
            //Log.d("DownloadsFragment", "approached notify");
        }

    }

    class DownloadsGridAdapter extends SimpleCursorAdapter{

        public DownloadsGridAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            //Log.d("downloadsGridAdapter", "getView started");
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

            TextView updatestatus = (TextView) v.findViewById(R.id.authortext);
            updatestatus.setTypeface(tf);

            ImageView status = (ImageView) v.findViewById(R.id.completion_status);
            int update = cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE));
            switch (update){
                case 0:
                    status.setImageResource(R.drawable.ic_file_download_white_24dp);
                    updatestatus.setText("Automatic updates disabled");
                    break;
                case 1:
                    status.setImageResource(R.drawable.ic_file_download_white_24dp);
                    updatestatus.setText("Automatic updates enabled");
                    break;
                case 2:
                    status.setImageResource(R.drawable.ic_file_upload_white_24dp);
                    updatestatus.setText("File upload");
                    break;
            }

            //Item item = (Item)getItem(i);

            //Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER))); //// FIXME: 4/11/2016  android.database.StaleDataException: Attempting to access a closed CursorWindow.Most probable cause: cursor is deactivated prior to calling this method.

            //picture.setImageURI(uri);
            //name.setText(item.name);

            String coverstring = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            Uri uri = null;
            if (coverstring != null) {
                uri = Uri.parse(coverstring);
                if(getActivity() != null) {
                    BitmapFunctions.loadBitmap(uri, picture, getResources(), mMemoryCache, mReusableBitmaps, 1);
                }
            }

            if(cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED)) == 1){
                Toolbar toolbar = (Toolbar) v.findViewById(R.id.gridview_toolbar);
                toolbar.setBackgroundColor(Color.argb(225, 0, 47, 64));
            }else{
                Toolbar toolbar = (Toolbar) v.findViewById(R.id.gridview_toolbar);
                toolbar.setBackgroundColor(Color.argb(187, 0, 0, 0));
            }

            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }

    }

    class DownloadsListAdapter extends SimpleCursorAdapter{

        public DownloadsListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            //Log.d("downloadsListAdapter", "getView started");
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

            ImageView authorimg = (ImageView) v.findViewById(R.id.authorimg);
            authorimg.setVisibility(View.INVISIBLE);
            ImageView artistimg = (ImageView) v.findViewById(R.id.artistimg);
            artistimg.setVisibility(View.INVISIBLE);


            picture = (ImageView)v.getTag(R.id.picture);
            name = (TextView)v.getTag(R.id.text);
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
            name.setTypeface(tf);
            cursor = (Cursor) getItem(i);

            TextView updatestatus = (TextView) v.findViewById(R.id.authortext);
            updatestatus.setTypeface(tf);

            ImageView status = (ImageView) v.findViewById(R.id.completion_status);
            int update = cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE));
            switch (update){
                case 0:
                    status.setImageResource(R.drawable.ic_file_download_white_24dp);
                    updatestatus.setText("Automatic updates disabled");
                    break;
                case 1:
                    status.setImageResource(R.drawable.ic_file_download_white_24dp);
                    updatestatus.setText("Automatic updates enabled");
                    break;
                case 2:
                    status.setImageResource(R.drawable.ic_file_upload_white_24dp);
                    updatestatus.setText("File upload");
                    break;
            }

            //Item item = (Item)getItem(i);

            //Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER))); //// FIXME: 4/11/2016  android.database.StaleDataException: Attempting to access a closed CursorWindow.Most probable cause: cursor is deactivated prior to calling this method.

            //picture.setImageURI(uri);
            //name.setText(item.name);

            String coverstring = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            Uri uri = null;
            if (coverstring != null) {
                uri = Uri.parse(coverstring);
                if(getActivity() != null) {
                    BitmapFunctions.loadBitmap(uri, picture, getResources(), mMemoryCache, mReusableBitmaps, 1);
                }

            }

            if(cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED)) == 1){
                LinearLayout layout = (LinearLayout) v.findViewById(R.id.listview_layout);
                layout.setBackgroundColor(Color.argb(225, 0, 47, 64));
            }else{
                LinearLayout layout = (LinearLayout) v.findViewById(R.id.listview_layout);
                layout.setBackgroundColor(0);
            }

            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
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
}
