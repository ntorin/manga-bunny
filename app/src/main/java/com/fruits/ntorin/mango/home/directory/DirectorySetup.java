package com.fruits.ntorin.mango.home.directory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import com.fruits.ntorin.mango.database.DirectoryContract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Iterator;

import static com.fruits.ntorin.mango.BitmapFunctions.getBitmapFromURL;

/**
 * Created by Ntori on 3/19/2016.
 */
public class DirectorySetup {

    static ContentValues values = new ContentValues();

    public static void MangafoxSetup(SQLiteDatabase db){
        ContentValues values = new ContentValues();

        Log.d("mfoxsetup", "executing setup");

        try {
            Log.d("mfoxsetup", "connecting");
            Document document = Jsoup.connect("http://mangafox.me/manga/").maxBodySize(0).get();
            Log.d("mfoxsetup", "connected");
            Elements li = document.getElementById("page").getElementsByClass("series_preview");
            Iterator i = li.iterator();
            int c = 0;
            Log.d("mfoxsetup", "filling db");
            for(Element element : li){
                Element title = element;
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, title.text());
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, title.attr("href"));
                //db.delete(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME, null, null);
                db.insert(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME, null, values);
                /*c++;
                if(c > 50){ //// FIXME testing purposes
                    break;
                }*/
            }
            Log.d("mfoxsetup", "filling db done");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void MangaHereSetup(SQLiteDatabase db, Context context) {
        ContentValues values = new ContentValues();

        Log.d("mheresetup", "executing setup");

        try {
            Log.d("mheresetup", "connecting");
            Document document = Jsoup.connect("http://mangahere.co/mangalist/").maxBodySize(0).get();
            Log.d("mheresetup", "connected");
            Elements li = document.getElementsByClass("manga_info");
            Iterator i = li.iterator();
            int c = 0;
            Log.d("mheresetup", "filling db");
            db.delete(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME, null, null); //// FIXME: 4/5/2016 potential issues here if connection is lost?
            for(Element element : li) {
                Element title = element;
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, title.attr("rel"));
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, title.attr("href"));
                MangaHereGetDetailedInfo(values, title.attr("href"), c, db, context);
                c++;
                if(c > 10){ //// FIXME testing purposes
                    return;
                }
            }
            Log.d("mheresetup", "filling db done");

        } catch (SocketTimeoutException e){
            MangaHereSetup(db, context);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Document document = Jsoup.connect("").get(); //// FIXME: 3/19/2016

    }

    public static void MangaHereGetDetailedInfo(ContentValues values, String href, int coverCounter, SQLiteDatabase db, Context context){
        Log.d("mheresetup", "titlePage " + href + " connecting");
        Document titlePage = null;
        try {
            titlePage = Jsoup.connect(href).get();
        }catch(SocketTimeoutException e){
            Log.d("mheresetup", "first timeout");
            try {
                titlePage = Jsoup.connect(href).get();
            }catch(SocketTimeoutException ee){
                Log.d("mheresetup", "second timeout continue");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(titlePage.getElementsByClass("detail_topText").first() != null) {
            Elements titleInfo = titlePage.getElementsByClass("detail_topText").first().children();

            String genres = titleInfo.get(3).ownText();
            Log.d("mheresetup", "genres: " + genres);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES, genres);

            String author = "Unknown";
            if (titleInfo.get(4).getElementsByTag("a").first() != null) {
                author = titleInfo.get(4).getElementsByTag("a").first().text();
            }
            Log.d("mheresetup", "author: " + author);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR, author);

            String artist = "Unknown";
            if (titleInfo.get(5).getElementsByTag("a").first() != null) {
                artist = titleInfo.get(5).getElementsByTag("a").first().text();
            }
            Log.d("mheresetup", "artist: " + artist);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST, artist);

            String status = titleInfo.get(6).ownText();
            Log.d("mheresetup", "status: " + status);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS, status);

            String rank = titleInfo.get(7).ownText();
            Log.d("mheresetup", "rank: " + rank);
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_RANK, rank);

            Element imgElement = titlePage.getElementsByClass("img").first();
            String bmpURL = imgElement.attr("src");
            Bitmap cover = getBitmapFromURL(bmpURL);
            String coverURI = null;
            if(cover != null) {
                FileOutputStream fos = null;
                try {
                    fos = context.openFileOutput("cover" + coverCounter, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                cover.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                coverURI = context.getFileStreamPath("cover" + coverCounter).toURI().toString();
                Log.d("filepathURI", "" + coverURI);
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_COVER, coverURI);

            Log.d("mheresetup", "titlePage " + href + " connected");
            db.insert(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME, null, values);
        }
    }

    public static void BatotoSetup(ContentValues values, SQLiteDatabase db) {
        Log.d("WIP", "Work in progress. Please turn back for now!");
       // Document document = Jsoup.connect("").get(); //// FIXME: 3/19/2016
    }

    public static void UpdateDirectory(String tableName, String siteDir, SQLiteDatabase db, Context context){
        ContentValues values = new ContentValues();
        Log.d("UpdateDirectory", "executing update");
        db.execSQL(DirectoryContract.SQL_CREATE_UPDATE_TABLE);

        try {
            Log.d("UpdateDirectory", "connecting");
            Document document = Jsoup.connect(siteDir).maxBodySize(0).get();
            Log.d("UpdateDirectory", "connected");
            Elements li = document.getElementsByClass("manga_info");
            int c = 0;
            Log.d("UpdateDirectory", "filling db");
            for(Element element : li) {
                Element title = element;
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, title.text());
                values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, title.attr("href"));
                //db.delete(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME, null, null);
                db.insert(DirectoryContract.DirectoryEntry.UPDATE_TABLE_NAME, null, values);
                c++;
                if (c > 50) { //// FIXME testing purposes
                    break;
                }
            }
                Cursor directoryCursor =  db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                        DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + " FROM " +
                        tableName, null);
                int i = directoryCursor.getCount();

                Cursor updateCursor = db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE + ", " +
                        DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + " FROM " +
                        DirectoryContract.DirectoryEntry.UPDATE_TABLE_NAME, null);

                for(int n = 0; n < updateCursor.getCount(); n++){
                    updateCursor.moveToPosition(n);
                    Cursor check = db.rawQuery("SELECT " + DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE
                             + " FROM " + tableName + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF
                            + "=\'" + updateCursor.getString(updateCursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF))
                            + "\'", null);
                    if(check.getCount() > 0) {
                        Log.d("UpdateDirectory", "match found");
                    }else{
                        Log.d("UpdateDirectory", "no matches; get detailed info");
                        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, updateCursor.getString(updateCursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)));
                        values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, updateCursor.getString(updateCursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF)));
                        MangaHereGetDetailedInfo(values, updateCursor.getString(updateCursor.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF)), i, db, context);
                        i++;
                        Log.d("UpdateDirectory", "done getting detailed info");
                    }
                }

            Log.d("UpdateDirectory", "db update done");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            db.execSQL(DirectoryContract.SQL_DELETE_UPDATE_TABLE);
        }
    }
}
