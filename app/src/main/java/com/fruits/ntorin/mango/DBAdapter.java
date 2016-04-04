package com.fruits.ntorin.mango;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Ntori on 3/27/2016.
 */
public class DBAdapter extends CursorAdapter {

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
