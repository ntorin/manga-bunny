package com.fruits.ntorin.mango;

import android.provider.BaseColumns;

/**
 * Created by Ntori on 2/7/2016.
 */
public final class DirectoryContract {

    public DirectoryContract(){}

    public static abstract class DirectoryEntry implements BaseColumns {
        public static final String MANGAFOX_TABLE_NAME = "Mangafox";
        public static final String MANGAHERE_TABLE_NAME = "MangaHere";
        public static final String BATOTO_TABLE_NAME = "Batoto";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_HREF = "href";
    }

    public static final String SQL_CREATE_MANGAFOX_TABLE =
            "CREATE TABLE " + DirectoryEntry.MANGAFOX_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT" + " )";

    public static final String SQL_CREATE_MANGAHERE_TABLE =
            "CREATE TABLE " + DirectoryEntry.MANGAHERE_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT" + " )";

    public static final String SQL_CREATE_BATOTO_TABLE =
            "CREATE TABLE " + DirectoryEntry.BATOTO_TABLE_NAME + " (" +
                DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                DirectoryEntry.COLUMN_NAME_HREF + " TEXT" + " )";


    public static final String SQL_DELETE_MANGAFOX_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.MANGAFOX_TABLE_NAME;

    public static final String SQL_DELETE_MANGAHERE_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.MANGAHERE_TABLE_NAME;

    public static final String SQL_DELETE_BATOTO_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.BATOTO_TABLE_NAME;


}
