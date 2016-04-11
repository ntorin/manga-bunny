package com.fruits.ntorin.mango.database;

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
        public static final String FAVORITES_TABLE_NAME = "Favorites";
        public static final String HISTORY_TABLE_NAME = "History";

        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_HREF = "href";
        public static final String COLUMN_NAME_COVER = "cover";
        public static final String COLUMN_NAME_GENRES = "genres";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_ARTIST = "artist";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_RANK = "rank";
        public static final String COLUMN_NAME_DATE = "date";
    }

    public static final String SQL_CREATE_MANGAFOX_TABLE =
            "CREATE TABLE " + DirectoryEntry.MANGAFOX_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_COVER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_GENRES + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_ARTIST + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_STATUS + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_RANK + " TEXT" + " )";

    public static final String SQL_CREATE_MANGAHERE_TABLE =
            "CREATE TABLE " + DirectoryEntry.MANGAHERE_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_COVER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_GENRES + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_ARTIST + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_STATUS + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_RANK + " TEXT" + " )";

    public static final String SQL_CREATE_BATOTO_TABLE =
            "CREATE TABLE " + DirectoryEntry.BATOTO_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_COVER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_GENRES + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_ARTIST + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_STATUS + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_RANK + " TEXT" + " )";

    public static final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + DirectoryEntry.FAVORITES_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_COVER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_GENRES + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_ARTIST + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_STATUS + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_RANK + " TEXT" + " )";

    public static final String SQL_CREATE_HISTORY_TABLE =
            "CREATE TABLE " + DirectoryEntry.HISTORY_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_COVER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_GENRES + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_ARTIST + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_STATUS + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_RANK + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_DATE + " TEXT" + " )";




    public static final String SQL_DELETE_MANGAFOX_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.MANGAFOX_TABLE_NAME;

    public static final String SQL_DELETE_MANGAHERE_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.MANGAHERE_TABLE_NAME;

    public static final String SQL_DELETE_BATOTO_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.BATOTO_TABLE_NAME;

    public static final String SQL_DELETE_HISTORY_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.HISTORY_TABLE_NAME;

    public static final String SQL_DELETE_FAVORITES_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.FAVORITES_TABLE_NAME;

}
