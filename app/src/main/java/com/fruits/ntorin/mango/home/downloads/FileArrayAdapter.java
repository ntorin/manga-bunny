package com.fruits.ntorin.mango.home.downloads;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.File;

/**
 * Created by Ntori on 6/19/2016.
 */
public class FileArrayAdapter<T> extends ArrayAdapter<T> {

    File[] chlist;

    public FileArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
        this.chlist = (File[]) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public T getItem(int position) {
        return super.getItem(position);
    }
}
