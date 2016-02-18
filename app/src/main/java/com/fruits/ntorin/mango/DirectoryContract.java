package com.fruits.ntorin.mango;

import android.provider.BaseColumns;

/**
 * Created by Ntori on 2/7/2016.
 */
public final class DirectoryContract {

    public DirectoryContract(){}

    public static abstract class DirectoryEntry implements BaseColumns {
        public static final String MANGAFOX_TABLE_NAME = "Mangafox";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_HREF = "href";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DirectoryEntry.MANGAFOX_TABLE_NAME + " (" +
            DirectoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DirectoryEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    DirectoryEntry.COLUMN_NAME_HREF + TEXT_TYPE + " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DirectoryEntry.MANGAFOX_TABLE_NAME;


}
