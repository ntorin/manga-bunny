package com.fruits.ntorin.mango;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SiteSearch extends ListActivity{

    private ArrayList<String> siteList;
    private ArrayAdapter<String> aAdapter;
    private HashMap<String, String> chLinks = new HashMap<String, String>();
    DirectoryDbHelper ddbHelper = new DirectoryDbHelper(this);
    DBAdapter cursorAdapter;
    SimpleCursorAdapter simpleCursorAdapter;
    Cursor selectQuery;
    private static final int LOADER_ID = 42;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_site_search);


        //ListView listView = (ListView) findViewById(R.id.search_list);
       // RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_list_directory);
        //RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        //recyclerView.setLayoutManager(layoutManager);
        //cursorAdapter = new DBAdapter(this, selectQuery, 0);
        //String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
        //int[] to = {R.id.site_search_content};
        //simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.site_item, selectQuery, from, to, 0);

        new AsyncFetchDirectory(this).execute();

        siteList = new ArrayList<String>();
        siteList.add("New Game!");
        siteList.add("Amari Mawari");
        siteList.add("A Channel");

        //aAdapter = new ArrayAdapter<String>(this, R.layout.activity_site_search, R.id.site_search_content, siteList);
        //listView.setAdapter(cursorAdapter);
       // this.setListAdapter(simpleCursorAdapter);
        Log.d("d", "cursor adapter set");
        //setAdapter(aAdapter);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    private class AsyncFetchDirectory extends AsyncTask<Void, String, Void>{

        Element title;
        ContentValues values = new ContentValues();
        SQLiteDatabase db = ddbHelper.getWritableDatabase();
        ListActivity activity;

        public AsyncFetchDirectory(ListActivity a){
            activity = a;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.d("b", "test");
            try {
                Document document = Jsoup.connect("http://mangafox.me/manga/").get();
                Elements li = document.getElementById("page").getElementsByClass("series_preview");
                Iterator i = li.iterator();
                int c = 0;
                for(Element element : li){
                    title = element;
                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, title.text());
                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, title.attr("href"));
                    db.insert(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME, null, values);
                    Log.d("z", "value put and inserted");
                    //publishProgress();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Log.d("cz", "" + selectQuery.toString());
            /*if(selectQuery.moveToFirst()){
                do{
                    //siteList.add(selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));
                    Log.d("a",selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));
                } while(selectQuery.moveToNext());
            }*/
            publishProgress();

            return null;

        }

        @Override
        protected void onProgressUpdate(String... progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //siteList.add(title.text());
                    //aAdapter.notifyDataSetChanged();
                    //cursorAdapter.changeCursor(selectQuery);
                    //cursorAdapter.notifyDataSetChanged();
                    String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
                    int[] to = {R.id.site_search_content};
                    //Cursor selectQuery = db.query(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME, from, null, null, null, null, null);
                    Cursor selectQuery = db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry._ID + ", " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + " FROM " + DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME, null);
                    /*if(selectQuery.moveToFirst()){
                        do{
                            //siteList.add(selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));
                            //Log.d("a",selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));
                            //Log.d("a",selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry._ID)));

                        } while(selectQuery.moveToNext());
                    }*/
                    simpleCursorAdapter = new SimpleCursorAdapter(activity, R.layout.site_item, selectQuery, from, to, 0);
                    activity.setListAdapter(simpleCursorAdapter);
                    //simpleCursorAdapter.notifyDataSetChanged();
                    Log.d("c", "approached notify");

                }
            });

        }
    }
/**
    public static class DirectoryAdapter extends RecyclerView.Adapter{

        ArrayList<String> listDirectory;
        public static class ViewHolder extends RecyclerView.ViewHolder{

            public TextView textView;
            public ViewHolder(TextView itemView) {
                super(itemView);
                textView = itemView;
            }
        }

        public DirectoryAdapter(ArrayList<String> directory){
            listDirectory = directory;
        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_site_search, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            holder.
        }

        @Override
        public int getItemCount() {
            return siteList.size();
        }
    }
        */

@Override
public void onListItemClick(ListView l, View v, int position, long id) {
    Intent intent = new Intent(this, DescriptionChapters.class);
    startActivity(intent);
}
}

class DBAdapter extends CursorAdapter{

    public DBAdapter(Context context, Cursor c, int flags) {
        super(context, c, 0);
        Log.d("g", "dbadapter set");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d("e", "new view called");
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.site_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.site_search_content);
        String body = cursor.getString(cursor.getColumnIndexOrThrow(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
        textView.setText(body);
        Log.d("f", "bind view called");
    }
}
