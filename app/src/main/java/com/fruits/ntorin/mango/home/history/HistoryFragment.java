package com.fruits.ntorin.mango.home.history;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.packages.ChangeChaptersPackage;
import com.fruits.ntorin.mango.packages.TitlePackage;
import com.fruits.ntorin.mango.reader.ChapterReader;
import com.fruits.ntorin.mango.sourcefns.GoodMangaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaFoxFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaHereFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaInnFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaPandaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaReaderFunctions;
import com.fruits.ntorin.mango.sourcefns.Sources;
import com.fruits.ntorin.mango.title.Chapter;
import com.fruits.ntorin.mango.utils.BitmapFunctions;
import com.fruits.ntorin.mango.utils.RetainFragment;

import org.jsoup.select.Elements;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


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
    AbsListView absListView;
    HistoryListAdapter historyListAdapter;
    HistoryGridAdapter historyGridAdapter;
    DirectoryDbHelper ddbHelper;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private View mView;
    private LayoutInflater mInflater;
    //private EditText mFilterText;
    private SearchView mSearchText;
    private int mFirstVisibleItem;
    private LruCache<String, Bitmap> mMemoryCache;
    private Set<SoftReference<Bitmap>> mReusableBitmaps;

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

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(getActivity().getFragmentManager());
        mMemoryCache = retainFragment.mRetainedCache;
        if (mMemoryCache == null) {
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

        if (mReusableBitmaps == null) {
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //inflater.inflate(R.menu.menu_home, menu);
        mSearchText = (SearchView) menu.findItem(R.id.action_search).getActionView();
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        //Log.d("HistoryFragment", "onCreateOptionsMenu run Async ");
        MenuItem views = menu.findItem(R.id.list_view);
        if (absListView instanceof ListView) {
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
                getResources(), R.drawable.ic_view_list_white_24dp, null).getConstantState()) && sharedPreferences.getString(Settings.PREF_HISTORY_LISTINGS, "1").equals("1")) {
            ToggleViews(listitem);
        }

        new AsyncFetchHistory(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME);
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
                if (mSearchText != null) { // TODO: 7/21/2016 maybe has errors
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
        if (item.getIcon().getConstantState().equals(ResourcesCompat.getDrawable(
                getResources(), R.drawable.ic_view_list_white_24dp, null).getConstantState())) {
            item.setIcon(R.drawable.ic_view_module_white_24dp);
            mView.findViewById(R.id.history_grid).setVisibility(View.GONE);
            absListView = (AbsListView) mView.findViewById(R.id.history_list);
            if (absListView.getOnItemClickListener() == null) {
                setListener();
            }
            absListView.setAdapter(historyListAdapter);
            mView.findViewById(R.id.history_list).setVisibility(View.VISIBLE);
            editor.putString(Settings.PREF_HISTORY_LISTINGS, "1");
            editor.apply();
            //Log.d("tolist", "request list");
        } else {
            item.setIcon(R.drawable.ic_view_list_white_24dp);
            mView.findViewById(R.id.history_list).setVisibility(View.GONE);
            absListView = (AbsListView) mView.findViewById(R.id.history_grid);
            if (absListView.getOnItemClickListener() == null) {
                setListener();
            }
            absListView.setAdapter(historyGridAdapter);
            mView.findViewById(R.id.history_grid).setVisibility(View.VISIBLE);
            editor.putString(Settings.PREF_HISTORY_LISTINGS, "0");
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
        mView = inflater.inflate(R.layout.fragment_history, container, false);
        absListView = (AbsListView) mView.findViewById(R.id.history_grid);
        //mFilterText = (EditText) mView.findViewById(R.id.editText);
        //TextView t = (TextView) mView.findViewById(R.id.empty_display);


        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");

        //t.setTypeface(tf);

        setListener();

        return mView;
    }


    public void setListener() {
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                //Intent intent = new Intent(HistoryFragment.this.getContext(), DescriptionChapters.class);
                Bundle bundle = new Bundle();
                Cursor cursor = (Cursor) historyListAdapter.getCursor();
                cursor.moveToPosition(position);
                String title = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
                String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                String titlehref = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLEHREF));
                String mangatitle = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_MANGATITLE));
                int sourceid = cursor.getInt(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE));
                Sources.setSelectedSource(sourceid);
                //test.close();

                String[] text = title.split(" ");
                Chapter chapter = new Chapter(title, href, text[text.length - 1]);
                new AsyncGetPagesFromHistory(chapter, titlehref, mangatitle).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                bundle.putString("title", title);
                bundle.putString("href", href);
                //intent.putExtras(bundle);
                //startActivity(intent);

                /*
                Intent intent = new Intent(HistoryFragment.this.getContext(), ChapterReader.class);
                Bundle bundle = new Bundle();
                Cursor cursor = (Cursor) historyListAdapter.getCursor();
                cursor.moveToPosition(position);
                String title = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
                String href = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                bundle.putString("href", href);
                bundle.putStringArray("pageURLs", pageURLs);
                bundle.putInt("pages", pages.size()); //// FIXME: 4/2/2016 possible null issues here
                intent.putExtras(bundle);

                //Log.d("toChapterReader", href);
                startActivity(intent);*/
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
                final Cursor cursor = historyListAdapter.getCursor();
                cursor.moveToPosition(position);

                final String[] choices = {"Remove from history"};
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
                        .setTitle("History Options")
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d("HistoryLongClick", "" + adapter.getItem(which));
                                SQLiteDatabase db = ddbHelper.getWritableDatabase();
                                db.delete(DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME,
                                        DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" +
                                                cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF)) +
                                                "\'", null);

                                String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
                                int[] to = {R.id.site_search_content};
                                String query = "SELECT * FROM " +
                                        DirectoryContract.DirectoryEntry.HISTORY_TABLE_NAME;
                                if (mSearchText.getQuery().toString() != null) {
                                    String verifiedString = mSearchText.getQuery().toString().replace("'", "''");
                                    query += " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                                            + " LIKE '%" + verifiedString + "%'";
                                }
                                Cursor selectQuery = db.rawQuery(query, null);

                                historyGridAdapter = new HistoryGridAdapter(getContext(), R.layout.site_item, selectQuery, from, to, 0);
                                historyListAdapter = new HistoryListAdapter(getContext(), R.layout.site_item, selectQuery, from, to, 0);
                                historyListAdapter.notifyDataSetChanged();
                                historyGridAdapter.notifyDataSetChanged();
                                absListView.invalidateViews();

                                if (absListView instanceof ListView) {
                                    //Log.d("instanceof", "ListView");
                                    absListView.setAdapter(historyListAdapter);
                                } else if (absListView instanceof GridView) {
                                    //Log.d("instanceof", "GridView");
                                    absListView.setAdapter(historyGridAdapter);
                                }
                            }
                        })
                        .create();
                dialog.show();
                return true;
            }
        });
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

    private class AsyncGetPagesFromHistory extends AsyncTask<Void, Void, Void> {

        Chapter item;
        String titlehref;
        String mangatitle;
        ProgressDialog pdialog;

        public AsyncGetPagesFromHistory(Chapter item, String titlehref, String mangatitle) {
            super();
            this.item = item;
            this.titlehref = titlehref;
            this.mangatitle = mangatitle;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast t = Toast.makeText(getContext(), "Unable to connect to pages; check network and try again", Toast.LENGTH_SHORT);
            t.show();
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
        protected Void doInBackground(Void... params) {
            Elements pages = null;
            String[] pageURLs = new String[0];
            //Log.d("GetPagesFromHistory", " " + item.content);
            Map<String, Chapter> chMap = new HashMap<>();
            ChangeChaptersPackage c = null;
            TitlePackage t = null;
            switch (Sources.getSelectedSource()) {
                case Sources.MANGAHERE:
                    t = MangaHereFunctions.TitleSetup(titlehref, chMap);
                    c = MangaHereFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.GOODMANGA:
                    t = GoodMangaFunctions.TitleSetup(titlehref, chMap);
                    c = GoodMangaFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAFOX:
                    t = MangaFoxFunctions.TitleSetup(titlehref, chMap);
                    c = MangaFoxFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAREADER:
                    t = MangaReaderFunctions.TitleSetup(titlehref, chMap);
                    c = MangaReaderFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAPANDA:
                    t = MangaPandaFunctions.TitleSetup(titlehref, chMap);
                    c = MangaPandaFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
                case Sources.MANGAINN:
                    t = MangaInnFunctions.TitleSetup(titlehref, chMap);
                    c = MangaPandaFunctions.ChangeChapters(item.content, pages, pageURLs);
                    break;
            }
            chMap = t.getChapterMap();
            pages = c.getPages();
            pageURLs = c.getPageURLs();
            String chno;
            /*for(String s : chMap.keySet()){
                if(item.content.equals(chMap.get(s).content)){
                    chno = s;
                }
            }*/
            /*try {
                Document document = Jsoup.connect(item.content).get();
                Elements li = document.getElementsByAttributeValue("onchange", "change_page(this)");
                pages = li.first().children();
                //Log.d("#pages", "" + (pages.size()));
                //li = document.getElementsByAttributeValue("class", "btn next_page");
                //Log.d("nextpageurl", "" + li.first().attr("href"));
                pageURLs = new String[pages.size()];
                int i = 0;
                for(Element option : pages){
                    pageURLs[i] = option.attr("value");
                    //Log.d("getpages", option.attr("value"));
                    i++;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }*/
            if (pages != null) {
                Intent intent = new Intent(HistoryFragment.this.getContext(), ChapterReader.class);
                Bundle bundle = new Bundle();
                bundle.putString("href", item.content);
                bundle.putString("title", item.id);
                bundle.putString("mangatitle", mangatitle);
                bundle.putStringArray("pageURLs", pageURLs);
                bundle.putSerializable("chlist", (HashMap) chMap);
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

                //Log.d("toChapterReader", item.content);
                startActivity(intent);
            } else {
                publishProgress();
            }


            return null;
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

        /*@Override
        protected void onPostExecute(Void aVoid) {
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    Intent intent = new Intent(DescriptionChapters.this, ChapterReader.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("href", href);
                    intent.putExtras(bundle);

                    //Log.d("toChapterReader", href);
                    startActivity(intent);

                }
            });
        }*/
    }

    private class AsyncFetchHistory extends AsyncTask<String, String, Void> {

        SQLiteDatabase db = ddbHelper.getWritableDatabase();
        Fragment fragment;

        public AsyncFetchHistory(Fragment a) {
            fragment = a;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mFirstVisibleItem = 0;
            String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
            int[] to = {R.id.site_search_content};
            Cursor selectQuery = db.rawQuery("SELECT * FROM " +
                    params[0] + " ORDER BY " + DirectoryContract.DirectoryEntry.COLUMN_NAME_DATE + " DESC", null);


            historyGridAdapter = new HistoryGridAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            historyListAdapter = new HistoryListAdapter(fragment.getContext(), R.layout.site_item, selectQuery, from, to, 0);
            FilterQueryProvider filterQueryProvider = new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    String verifiedString = constraint.toString().replace("'", "''");
                    return db.rawQuery("SELECT * FROM " +
                            params[0] + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                            + " LIKE '%" + verifiedString + "%' ORDER BY " + DirectoryContract.DirectoryEntry.COLUMN_NAME_DATE + " DESC", null);
                }
            };
            historyGridAdapter.setFilterQueryProvider(filterQueryProvider);
            historyListAdapter.setFilterQueryProvider(filterQueryProvider);
            publishProgress(params[0]);
            return null;
        }

        @Override
        protected void onProgressUpdate(final String... tableName) {
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
                    historyListAdapter.getFilter().filter(newText);
                    historyGridAdapter.getFilter().filter(newText);
                    historyListAdapter.notifyDataSetChanged();
                    historyGridAdapter.notifyDataSetChanged();
                    return false;
                }
            });

            if (absListView.equals(mView.findViewById(R.id.history_grid))) {
                absListView.setAdapter(historyGridAdapter);
            } else {
                absListView.setAdapter(historyListAdapter);
            }
        }
    }

    class HistoryGridAdapter extends SimpleCursorAdapter {

        public HistoryGridAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ////Log.d("historyGridAdapter", "getView started");
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
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
            name.setTypeface(tf);
            cursor = (Cursor) getItem(i);

            TextView timestamp = (TextView) v.findViewById(R.id.authortext);
            String time = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_DATE));
            timestamp.setText(time);
            timestamp.setTypeface(tf);

            //Item item = (Item)getItem(i);
            String coverstring = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            Uri uri = null;
            if (coverstring != null) {
                uri = Uri.parse(coverstring);
                if (getActivity() != null) {
                    BitmapFunctions.loadBitmap(uri, picture, getResources(), mMemoryCache, mReusableBitmaps, 1);
                }
            }
            //name.setText(item.name);
            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }

    }

    class HistoryListAdapter extends SimpleCursorAdapter {

        public HistoryListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ////Log.d("historyGridAdapter", "getView started");
            View v = view;
            ImageView picture;
            TextView name;
            Cursor cursor;

            if (v == null) {
                v = mInflater.inflate(R.layout.listview_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }


            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
            name.setTypeface(tf);
            cursor = (Cursor) getItem(i);

            TextView author = (TextView) v.findViewById(R.id.authortext);
            author.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR)));
            author.setTypeface(tf);

            String artistString = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST));

            TextView artist = (TextView) v.findViewById(R.id.artisttext);
            ImageView artistimg = (ImageView) v.findViewById(R.id.artistimg);
            if (artistString != null && !artistString.equals("")) {
                artistimg.setVisibility(View.VISIBLE);
                artist.setText(artistString);
                artist.setTypeface(tf);
            } else {
                artist.setText("");
                artistimg.setVisibility(View.INVISIBLE);
            }

            TextView genres = (TextView) v.findViewById(R.id.genretext);
            genres.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES)));
            genres.setTypeface(tf);

            TextView timestamp = (TextView) v.findViewById(R.id.sourcetext);
            String time = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_DATE));
            timestamp.setText(time);
            timestamp.setTypeface(tf);

            String coverstring = cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER));
            Uri uri = null;
            if (coverstring != null) {
                uri = Uri.parse(coverstring);
                if (getActivity() != null) {
                    BitmapFunctions.loadBitmap(uri, picture, getResources(), mMemoryCache, mReusableBitmaps, 1);
                }
            }

            //Item item = (Item)getItem(i);

            //picture.setImageResource(item.drawableId);
            //name.setText(item.name);
            name.setText(cursor.getString(cursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));

            return v;
        }

    }

}


