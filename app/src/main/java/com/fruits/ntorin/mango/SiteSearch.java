package com.fruits.ntorin.mango;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Iterator;

public class SiteSearch extends ListActivity{

    DirectoryDbHelper ddbHelper = new DirectoryDbHelper(this);
    SimpleCursorAdapter simpleCursorAdapter;
    private static final int LOADER_ID = 42;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_site_search);

        new AsyncFetchDirectory(this).execute();
        Log.d("d", "cursor adapter set");


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

            DirectorySetup.MangafoxSetup(title, values, db);

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
                    c++;
                    if(c > 50){ //// FIXME testing purposes
                        break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            publishProgress();

            return null;

        }

        @Override
        protected void onProgressUpdate(String... progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] from = {DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE};
                    int[] to = {R.id.site_search_content};
                    Cursor selectQuery = db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry._ID + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                            DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + " FROM " +
                            DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME, null);
                    simpleCursorAdapter = new SimpleCursorAdapter(activity, R.layout.site_item, selectQuery, from, to, 0);
                    activity.setListAdapter(simpleCursorAdapter);
                    Log.d("c", "approached notify");
                }
            });

        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, DescriptionChapters.class);
        Bundle bundle = new Bundle();
        SQLiteCursor test = (SQLiteCursor) simpleCursorAdapter.getCursor();
        test.moveToPosition(position);
        String title = test.getString(test.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
        String href = test.getString(test.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
        test.close();
        bundle.putString("title", title);
        bundle.putString("href", href);
        intent.putExtras(bundle);
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
        Log.d("v", "new view called");
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.site_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.site_search_content);
        String body = cursor.getString(cursor.getColumnIndexOrThrow(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE));
        textView.setText(body);
        Log.d("v", "bind view called");
    }
}
