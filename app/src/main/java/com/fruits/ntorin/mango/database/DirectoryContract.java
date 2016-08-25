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
        public static final String UPDATE_TABLE_NAME = "Updates";
        public static final String BOOKMARKS_TABLE_NAME = "Bookmarks";
        public static final String DOWNLOADS_TABLE_NAME = "Downloads";

        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_HREF = "href";
        public static final String COLUMN_NAME_COVER = "cover";
        public static final String COLUMN_NAME_GENRES = "genres";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_ARTIST = "artist";
        public static final String COLUMN_NAME_STATUS = "status"; // 0 = ONGOING, 1 = COMPLETED
        public static final String COLUMN_NAME_RANK = "rank";
        public static final String COLUMN_NAME_CHAPTERS = "chapters";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_CHAPTER = "chapter";
        public static final String COLUMN_NAME_PAGENUM = "pageNumber";
        public static final String COLUMN_NAME_CHAPTERNUM = "chapterNumber";
        public static final String COLUMN_NAME_PAGEIMG = "pageImage";
        public static final String COLUMN_NAME_CHAPTERID = "chapterTitle";
        public static final String COLUMN_NAME_CHAPTERCONTENT = "chapterHref";
        public static final String COLUMN_NAME_DIR = "directory";
        public static final String COLUMN_NAME_UPDATE = "autoupdate"; // NO UPDATE = 0, AUTOUPDATE = 1, FILE UPLOAD = 2
        public static final String COLUMN_NAME_TITLEHREF = "titleHref";
        public static final String COLUMN_NAME_MANGATITLE = "mangaTitle";
        public static final String COLUMN_NAME_SOURCE = "source";
        public static final String COLUMN_NAME_UPDATED = "updated"; // NO = 0, YES = 1
    }

    public static final String SQL_CREATE_UPDATE_TABLE =
            "CREATE TABLE " + DirectoryEntry.UPDATE_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT" + " )";

    public static final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + DirectoryEntry.FAVORITES_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_COVER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_GENRES + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_ARTIST + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_STATUS + " INTEGER," +
                    DirectoryEntry.COLUMN_NAME_RANK + " DOUBLE," +
                    DirectoryEntry.COLUMN_NAME_SOURCE + " INTEGER," +
                    DirectoryEntry.COLUMN_NAME_UPDATED + " INTEGER," +
                    DirectoryEntry.COLUMN_NAME_CHAPTERS + " TEXT" + " )";

    public static final String SQL_CREATE_HISTORY_TABLE =
            "CREATE TABLE " + DirectoryEntry.HISTORY_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_TITLEHREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_MANGATITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_COVER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_GENRES + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_ARTIST + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_STATUS + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_RANK + " DOUBLE," +
                    DirectoryEntry.COLUMN_NAME_SOURCE + " INTEGER," +
                    DirectoryEntry.COLUMN_NAME_DATE + " TEXT" + " )";

    public static final String SQL_CREATE_BOOKMARKS_TABLE =
            "CREATE TABLE " + DirectoryEntry.BOOKMARKS_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_CHAPTERID + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_CHAPTERCONTENT + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_PAGENUM + " INTEGER," +
                    DirectoryEntry.COLUMN_NAME_CHAPTERNUM + " INTEGER," +
                    DirectoryEntry.COLUMN_NAME_CHAPTER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_PAGEIMG + " BLOB" + " )";

    public static final String SQL_CREATE_DOWNLOADS_TABLE =
            "CREATE TABLE " + DirectoryEntry.DOWNLOADS_TABLE_NAME + " (" +
                    DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_HREF + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_COVER + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_DIR + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_SOURCE + " INTEGER," +
                    DirectoryEntry.COLUMN_NAME_CHAPTERS + " TEXT," +
                    DirectoryEntry.COLUMN_NAME_UPDATED + " INTEGER," +
                    DirectoryEntry.COLUMN_NAME_UPDATE + " INTEGER" +" )";


    public static final String SQL_DELETE_MANGAFOX_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.MANGAFOX_TABLE_NAME;

    public static final String SQL_DELETE_MANGAHERE_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.MANGAHERE_TABLE_NAME;

    public static final String SQL_DELETE_BATOTO_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.BATOTO_TABLE_NAME;

    public static final String SQL_DELETE_UPDATE_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.UPDATE_TABLE_NAME;

    public static final String SQL_DELETE_HISTORY_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.HISTORY_TABLE_NAME;

    public static final String SQL_DELETE_FAVORITES_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.FAVORITES_TABLE_NAME;

    public static final String SQL_DELETE_BOOKMARKS_TABLE =
            "DROP TABLE IF EXISTS " + DirectoryEntry.BOOKMARKS_TABLE_NAME;

}
