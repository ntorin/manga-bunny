package com.fruits.ntorin.mango.home.directory;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fruits.ntorin.mango.database.DirectoryContract;

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

    static ContentValues values = new ContentValues();

    public static void MangafoxSetup(SQLiteDatabase db){

        Log.d("mfoxsetup", "executing setup");

        try {
            Log.d("mfoxsetup", "connecting");
            Document document = Jsoup.connect("http://mangafox.me/manga/").get();
            Log.d("mfoxsetup", "connected");
            Elements li = document.getElementById("page").getElementsByClass("series_preview");
            Iterator i = li.iterator();
            int c = 0;
            Log.d("mfoxsetup", "filling db");
            for(Element element : li){
                Element title = element;
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, title.text());
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, title.attr("href"));
                db.insert(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME, null, values);
                c++;
                if(c > 50){ //// FIXME testing purposes
                    break;
                }
            }
            Log.d("mfoxsetup", "filling db done");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void MangaHereSetup(ContentValues values, SQLiteDatabase db) {
        //Log.d("WIP", "Work in progress. Please turn back for now!");

        Log.d("mheresetup", "executing setup");

        try {
            Log.d("mheresetup", "connecting");
            Document document = Jsoup.connect("http://mangahere.co/mangalist/").get();
            Log.d("mheresetup", "connected");
            Elements li = document.getElementsByClass("manga_info");
            Iterator i = li.iterator();
            int c = 0;
            Log.d("mheresetup", "filling db");
            for(Element element : li){
                Element title = element;
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, title.attr("rel"));
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, title.attr("href"));
                db.insert(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME, null, values);
                c++;
                if(c > 50){ //// FIXME testing purposes
                    break;
                }
            }
            Log.d("mheresetup", "filling db done");
        } catch (IOException e) {
            e.printStackTrace();
        }

       // Document document = Jsoup.connect("").get(); //// FIXME: 3/19/2016

    }

    public static void BatotoSetup(ContentValues values, SQLiteDatabase db) {
        Log.d("WIP", "Work in progress. Please turn back for now!");
       // Document document = Jsoup.connect("").get(); //// FIXME: 3/19/2016
    }
}
