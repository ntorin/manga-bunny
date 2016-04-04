package com.fruits.ntorin.mango.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ntori on 2/7/2016.
 */
public class DirectoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 10;
    private static final String DATABASE_NAME = "MangoDirectory.db";




    public DirectoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DirectoryContract.SQL_DELETE_MANGAFOX_TABLE);
        db.execSQL(DirectoryContract.SQL_DELETE_MANGAHERE_TABLE);
        db.execSQL(DirectoryContract.SQL_DELETE_BATOTO_TABLE);

        db.execSQL(DirectoryContract.SQL_CREATE_MANGAFOX_TABLE);
        db.execSQL(DirectoryContract.SQL_CREATE_MANGAHERE_TABLE);
        db.execSQL(DirectoryContract.SQL_CREATE_BATOTO_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DirectoryContract.SQL_DELETE_MANGAFOX_TABLE);
        db.execSQL(DirectoryContract.SQL_DELETE_MANGAHERE_TABLE);
        db.execSQL(DirectoryContract.SQL_DELETE_BATOTO_TABLE);
    }
}
