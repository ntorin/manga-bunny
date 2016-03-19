package com.fruits.ntorin.mango;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Ntori on 3/19/2016.
 */
public class DirectorySetup {

    public static void MangafoxSetup(ContentValues values, SQLiteDatabase db){

        try {
            Document document = Jsoup.connect("http://mangafox.me/manga/").get();
            Elements li = document.getElementById("page").getElementsByClass("series_preview");
            Iterator i = li.iterator();
            int c = 0;
            for(Element element : li){
                Element title = element;
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

    }

    public static void MangaHereSetup(ContentValues values, SQLiteDatabase db) {
        Log.d("WIP", "Work in progress. Please turn back for now!");
       // Document document = Jsoup.connect("").get(); //// FIXME: 3/19/2016

    }

    public static void BatotoSetup(ContentValues values, SQLiteDatabase db) {
        Log.d("WIP", "Work in progress. Please turn back for now!");
       // Document document = Jsoup.connect("").get(); //// FIXME: 3/19/2016
    }
}
