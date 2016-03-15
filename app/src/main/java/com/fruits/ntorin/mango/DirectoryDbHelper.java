package com.fruits.ntorin.mango;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Ntori on 2/7/2016.
 */
public class DirectoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "Directory.db";




    public DirectoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DirectoryContract.SQL_DELETE_ENTRIES);
        db.execSQL(DirectoryContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DirectoryContract.SQL_DELETE_ENTRIES);
        db.execSQL(DirectoryContract.SQL_CREATE_ENTRIES);
    }
}
