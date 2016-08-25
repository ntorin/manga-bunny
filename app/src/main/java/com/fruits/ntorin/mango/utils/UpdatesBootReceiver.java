package com.fruits.ntorin.mango.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.Settings;
import com.fruits.ntorin.mango.database.DirectoryContract;
import com.fruits.ntorin.mango.database.DirectoryDbHelper;
import com.fruits.ntorin.mango.home.AppHome;
import com.fruits.ntorin.mango.packages.ChangeChaptersPackage;
import com.fruits.ntorin.mango.sourcefns.GoodMangaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaFoxFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaHereFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaInnFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaPandaFunctions;
import com.fruits.ntorin.mango.sourcefns.MangaReaderFunctions;
import com.fruits.ntorin.mango.sourcefns.Sources;
import com.fruits.ntorin.mango.title.Chapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static com.fruits.ntorin.mango.utils.BitmapFunctions.getBitmapFromURL;

/**
 * Created by Ntori on 7/9/2016.
 */
public class UpdatesBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("UpdatesBootReceiver", "recieved, " + intent.getAction());
        if (intent.getAction() != null) {
            if (intent.getAction().equals("com.fruits.ntorin.mango.UPDATE_FAVORITES")) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (sharedPreferences.getBoolean(Settings.PREF_CHECK_UPDATES, true)) {

                    //Log.d("UpdatesBootReceiver", "starting, " + intent.getAction());
                    new AsyncUpdateFavorites(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
                    if (sharedPreferences.getBoolean(Settings.PREF_AUTODOWNLOAD_WIFI_ONLY, false)) {
                        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        if (!wifi.isWifiEnabled()) {
                            //Log.d("UpdatesBootReceiver", "not on wifi, ending");
                            return;
                        }
                        //Log.d("UpdatesBootReceiver", "on wifi, starting");
                        new AsyncUpdateFavorites(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
                    } else {
                        //Log.d("UpdatesBootReceiver", "on wifi disabled, starting");
                        new AsyncUpdateFavorites(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
                    }
                } else {
                    //Log.d("UpdatesBootReceiver", "updates have been disabled");
                }
            } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.MAIN")) {
                //Log.d("UpdatesBootReceiver", "starting, " + intent.getAction());

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (sharedPreferences.getBoolean(Settings.PREF_AUTO_CHECK_UPDATES, true)) {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    String frequency = sharedPreferences.getString(Settings.PREF_TITLE_UPDATE_FREQUENCY, "30");
                    long interval = AlarmManager.INTERVAL_HALF_HOUR;
                    switch (frequency) {
                        case "15":
                            interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                            break;
                        case "60":
                            interval = AlarmManager.INTERVAL_HOUR;
                            break;
                        case "120":
                            interval = AlarmManager.INTERVAL_HOUR * 2;
                            break;
                        case "180":
                            interval = AlarmManager.INTERVAL_HOUR * 2;
                            break;
                        case "360":
                            interval = AlarmManager.INTERVAL_HOUR * 6;
                            break;
                        case "720":
                            interval = AlarmManager.INTERVAL_HALF_DAY;
                            break;
                        case "1440":
                            interval = AlarmManager.INTERVAL_DAY;
                            break;
                    }
                    //Log.d("UpdatesBootReceiver", "frequency: " + frequency);
                    Intent updateIntent = new Intent("com.fruits.ntorin.mango.UPDATE_FAVORITES", null, context, UpdatesBootReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                            1000,
                            interval, alarmIntent);
                    //Log.d("UpdatesBootReceiver", "ended");
                } else {
                    //Log.d("UpdatesBootReceiver", "auto check updates disabled");
                }


            } else {

                //Log.d("UpdatesBootReceiver", "wrong intent?");
            }
        } else {
            //Log.d("UpdatesBootReceiver", "getaction is null");
        }
    }

    private class AsyncUpdateFavorites extends AsyncTask<Context, Context, Void> {

        NotificationManager notificationManager;
        NotificationCompat.Builder builder;
        String tableName;

        public AsyncUpdateFavorites(String tableName) {
            this.tableName = tableName;
        }


        @Override
        protected Void doInBackground(Context... params) {
            //Log.d("UpdateFavorites", "starting");
            DirectoryDbHelper ddbHelper = new DirectoryDbHelper(params[0]);
            SQLiteDatabase db = ddbHelper.getWritableDatabase();

            String[] hrefs;
            Cursor selectQuery;
            if (tableName.equals(DirectoryContract.DirectoryEntry.FAVORITES_TABLE_NAME)) {
                selectQuery = db.rawQuery("SELECT * FROM " +
                        tableName, null);
                hrefs = new String[selectQuery.getCount()];
                for (int i = 0; i < selectQuery.getCount(); i++) { // FIXME: 6/27/2016 testing
                    selectQuery.moveToPosition(i);
                    String href = selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                    hrefs[i] = href;
                    Log.d("UpdateFavorites", "" + hrefs[i]);
                }
            } else {
                selectQuery = db.rawQuery("SELECT * FROM " +
                        tableName + " WHERE " + DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATE + "=" + 1, null);
                hrefs = new String[selectQuery.getCount()];
                for (int i = 0; i < selectQuery.getCount(); i++) { // FIXME: 6/27/2016 testing
                    selectQuery.moveToPosition(i);
                    String href = selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF));
                    hrefs[i] = href;
                    //Log.d("UpdateFavorites", "" + hrefs[i]);
                }

            }

            builder = new NotificationCompat.Builder(params[0]);
            String contentText = "";
            for (int i = 0; i < hrefs.length; i++) {
                ArrayList<Chapter> newChapters = null;
                selectQuery.moveToPosition(i);
                int source = selectQuery.getInt(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_SOURCE));
                switch (source) {
                    case Sources.MANGAHERE:
                        newChapters = MangaHereFunctions.UpdateFavorites(hrefs[i]);
                        break;
                    case Sources.GOODMANGA:
                        newChapters = GoodMangaFunctions.UpdateFavorites(hrefs[i]);
                        break;
                    case Sources.MANGAFOX:
                        newChapters = MangaFoxFunctions.UpdateFavorites(hrefs[i]);
                        break;
                    case Sources.MANGAREADER:
                        newChapters = MangaReaderFunctions.UpdateFavorites(hrefs[i]);
                        break;
                    case Sources.MANGAPANDA:
                        newChapters = MangaPandaFunctions.UpdateFavorites(hrefs[i]);
                        break;
                    case Sources.MANGAINN:
                        newChapters = MangaInnFunctions.UpdateFavorites(hrefs[i]);
                        break;
                }
                if (newChapters == null || newChapters.size() == 0) {
                    continue;
                }
                contentText = CheckForUpdates(selectQuery, i, params[0], contentText, newChapters, db, hrefs[i], source);
            }
            if (contentText.contains("\n")) {
                int totalUpdates = contentText.split("\n").length;
                if (totalUpdates > 0) {
                    Intent intent = new Intent(params[0], AppHome.class);
                    Bundle b = new Bundle();
                    b.putInt("tab", 1);
                    intent.putExtras(b);
                    String contentTitle = totalUpdates + " new update";
                    if (totalUpdates > 1) {
                        contentTitle += "s";
                    }
                    PendingIntent pendingIntent = PendingIntent.getActivity(params[0], 0, intent, 0);
                    //
                    builder.setContentTitle(contentTitle)
                            .setContentText(contentText)
                            .setColor(ContextCompat.getColor(params[0], R.color.colorPrimaryDark))
                            .setSmallIcon(R.drawable.notif)
                            .setContentIntent(pendingIntent);

                    int requestCode = ("update" + System.currentTimeMillis()).hashCode();
                    Notification n = builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText)).build();
                    notificationManager = (NotificationManager) params[0].getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(requestCode, n);
                    //Log.d("UpdateNotification", " " + contentText);
                }
            } else {
                //Log.d("contentText", "contentText: " + contentText);
            }
            //ArrayList<Chapter>[] chapters = new ArrayList<Chapter>[selectQuery.getCount()];
            //selectQuery.
            return null;
        }

        private String CheckForUpdates(Cursor selectQuery, int position, Context context, String contentText, ArrayList<Chapter> newChapters, SQLiteDatabase db, String href, int source) {
            selectQuery.moveToPosition(position);
            String oldChaptersURI = selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_CHAPTERS));

            if (oldChaptersURI != null) {
                Log.d("oldChaptersURI", oldChaptersURI);
                Uri uri = Uri.parse(oldChaptersURI);
                File file = new File(uri.getPath());
                FileInputStream fis = null;
                ArrayList<Chapter> updateList = new ArrayList<>();
                //File[] f = file.listFiles();
                //for(File fi : f){
                //    //Log.d("teset", fi.getName());
                //}
                try {
                    fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    ArrayList<Chapter> oldChapters = new ArrayList<>();
                    try {
                        oldChapters = (ArrayList<Chapter>) ois.readObject();
                    } catch (InvalidClassException e) {
                        e.printStackTrace();
                    }
                    //Log.d("UpdateFavorites", "oldChapters size: " + oldChapters.size());
                    //if(oldChapters.size() > 0) {
                    boolean updated = false;
                    if (newChapters != null) {
                        for (Chapter newChapter : newChapters) {
                            boolean match = false;
                            for (Chapter oldChapter : oldChapters) {
                                if (oldChapter.content.equals(newChapter.content)) {
                                    match = true;
                                }
                                if (match) {
                                    //Log.d("UpdateFavorites", "match found");
                                    break;
                                }
                            }
                            if (!match) {
                                if (oldChapters.size() > 0) {
                                    updated = true;
                                    //Log.d("UpdateFavorites", "no matches found; alert update for " + newChapter.content);
                                    contentText += newChapter.id + "\n";
                                    updateList.add(newChapter);
                                    ContentValues values = new ContentValues();
                                    values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_UPDATED, 1);
                                    db.update(tableName, values,
                                            DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF + "=\'" + href + "\'", null);
                                }
                            }
                        }

                        if (tableName.equals(DirectoryContract.DirectoryEntry.DOWNLOADS_TABLE_NAME)) {
                            new DownloadUpdatedTitle(selectQuery.getString(selectQuery.getColumnIndex(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE)), updateList, context, source).doInBackground();
                        }
                        if (updated || oldChapters.size() == 0) {
                            String filename = file.getName();
                            file.delete();


                            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(newChapters);

                            oos.close();
                            fos.close();
                            ois.close();
                            fis.close();
                        }
                    }
                    //}
                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
            return contentText;
        }

    }

    public class DownloadUpdatedTitle {

        String title;
        ArrayList<Chapter> map;
        NotificationManager notificationManager;
        NotificationCompat.Builder builder;
        Context context;
        int source;

        public DownloadUpdatedTitle(String title, ArrayList<Chapter> map, Context context, int source) {
            this.title = title;
            this.map = map;
            this.context = context;
            this.source = source;
        }

        public void doInBackground() {
            if (map == null) {
                return;
            }

            Chapter[] chapters = new Chapter[map.size()];

            int n = 0;

            if (map.size() == 0) {
                //Log.d("AsyncDownloadTitle", "no chapters");
                return;
            }
            for (Chapter c : map) {
                chapters[n] = c;
                //Log.d("AsyncDownloadTitle", chapters[n].id + " " + chapters[n].content);
                n++;
            }
            //for (int i = map.size(); i > 0; i--, n++) {
            //    chapters[n] = map.get(Integer.toString(i));
            //    //Log.d("AsyncDownloadTitle", chapters[n].id + " " + chapters[n].content);
            //}


            //File coredownloads = new File(Environment.DIRECTORY_PICTURES + "/coredownloads");

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                String rootDirectory = Environment.getExternalStorageDirectory().toString();
                /*File myDir = new File(rootDirectory + "/NewDirectory");
                if (myDir.mkdir()) {
                    //Log.d("AsyncDownloadTitle", "created folder");
                }*/

                File coredownloads = new File(Environment.getExternalStoragePublicDirectory(""), "coredownloads");
                if (coredownloads.mkdirs()) {
                    //Log.d("AsyncDownloadTitle", "created folder " + coredownloads.getName());
                }
                //File coredownloadsfile = new File(coredownloads, "test.png");
                //File coredownloads = new File(Environment.DIRECTORY_PICTURES + "/coredownloads");

                File titledir = new File(coredownloads, title);
                if (titledir.mkdirs()) {
                    //Log.d("AsyncDownloadTitle", "created folder " + titledir.getName());
                }

                String chaptersURI = null;
                try {
                    FileOutputStream fos = context.openFileOutput(title + "chapters", Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    ArrayList<Chapter> chapterArrayList = new ArrayList<>();
                    chapterArrayList.add(chapters[0]);
                    chapterArrayList.add(chapters[1]);
                    //Collections.addAll(chapterArrayList, chapters);
                    oos.writeObject(chapterArrayList);
                    chaptersURI = context.getFileStreamPath(title + "chapters").getPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent cancelIntent = new Intent();
                cancelIntent.setAction("com.fruits.ntorin.mango.STOP_DOWNLOAD");
                PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, 0);
                NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(R.drawable.ic_cancel_black_24dp, "Cancel", pendingCancelIntent)
                        .build();

                int requestCode = ("update" + System.currentTimeMillis()).hashCode();

                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                builder = new NotificationCompat.Builder(context);
                builder.setContentTitle(title)
                        .setContentText("Downloading ...")
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                        .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                        .setOngoing(true)
                        .addAction(cancelAction);

                String contentText = "";

                int i = 0;
                builder.setProgress(chapters.length, i, false);
                notificationManager.notify(requestCode, builder.build());
                for (Chapter ch : chapters) {
                    if (!CancelDownloadReceiver.isCancelled) {
                        File chdir = new File(titledir, ch.id);
                        if (chdir.mkdirs()) {
                            //Log.d("AsyncDownloadTitle", "created folder " + chdir.getName());
                        }
                        String[] pages = GetPages(ch.content, source);
                        DownloadPages(chdir, pages, i++, source);
                        builder.setProgress(chapters.length, i, false);
                        notificationManager.notify(requestCode, builder.build());
                    } else {
                        CancelDownloadReceiver.isCancelled = false;
                        contentText = "Download canceled.";
                        break;
                    }
                }
                if (contentText.equals("")) {
                    contentText = "Download completed.";
                }

                Intent intent = new Intent(context, AppHome.class);
                Bundle b = new Bundle();
                b.putInt("tab", 3);
                intent.putExtras(b);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                //NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_book_white_24dp, "Read Now", pendingIntent)
                //        .build();
                builder = new NotificationCompat.Builder(context);
                builder.setContentTitle(title)
                        .setContentText(contentText)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                        .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                        .setProgress(0, 0, false);
                notificationManager.cancel(requestCode);
                if (contentText.equals("Download completed.")) {
                    builder.setContentIntent(pendingIntent);
                }
                notificationManager.notify(requestCode, builder.build());
            } else {
                /*if (ContextCompat.checkSelfPermission(DescriptionChapters.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Log.d("AsyncDownloadTitle", "permission granted, going again");
                    //doInBackground();
                }*/
            }
        }

        private String[] GetPages(String href, int source) {

            Elements pages = null;
            String[] pageURLs = new String[0];


            ChangeChaptersPackage c = null;
            if (!CancelDownloadReceiver.isCancelled) {
                switch (source) {
                    case Sources.MANGAHERE:
                        c = MangaHereFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.GOODMANGA:
                        c = GoodMangaFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.MANGAFOX:
                        c = MangaFoxFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.MANGAREADER:
                        c = MangaReaderFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.MANGAPANDA:
                        c = MangaPandaFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                    case Sources.MANGAINN:
                        c = MangaInnFunctions.ChangeChapters(href, pages, pageURLs);
                        break;
                }
                pageURLs = c.getPageURLs();
            }
            return pageURLs;
        }

        private void DownloadPages(File folderpath, String[] hrefs, int chno, int source) {

            //Log.d("DownloadPages", "" + hrefs.length);
            for (int i = 0; i < hrefs.length; i++) {
                if (!CancelDownloadReceiver.isCancelled) {
                    int tries = 0;
                    boolean success = false;
                    Document document = null;
                    Bitmap bitmap = null;
                    while (!success) {
                        try {
                            ////Log.d("checkurl", params[0]);
                            //Log.d("DownloadPages", "connecting to " + hrefs[i]);
                            document = Jsoup.connect(hrefs[i]).get();

                            //Log.d("DownloadPages", "connected to " + hrefs[i]);
                            success = true;

                        } catch (IOException e) {
                            tries++;
                            if (tries > 50) {
                                return;
                            }
                            e.printStackTrace();
                            //this.cancel(true);
                        }


                    }
                    //Element li = document.getElementById("image");
                    //String bmpURL = li.attr("src");
                    String bmpURL = null;
                    switch (source) {
                        case Sources.MANGAHERE:
                            bmpURL = MangaHereFunctions.FetchPage(document);
                            break;
                        case Sources.GOODMANGA:
                            bmpURL = GoodMangaFunctions.FetchPage(document);
                            break;
                        case Sources.MANGAFOX:
                            bmpURL = MangaFoxFunctions.FetchPage(document);
                            break;
                        case Sources.MANGAREADER:
                            bmpURL = MangaReaderFunctions.FetchPage(document);
                            break;
                        case Sources.MANGAPANDA:
                            bmpURL = MangaPandaFunctions.FetchPage(document);
                            break;
                        case Sources.MANGAINN:
                            bmpURL = MangaInnFunctions.FetchPage(document);
                            break;
                    }
                    //Log.d("DownloadPages", bmpURL);
                    ////Log.d("test", "" + li.);
                    tries = 0;
                    success = false;
                    while (!success) {
                        try {
                            if (tries++ > 50) {
                                break;
                            }
                            ////Log.d("AsyncFetchPage", "getBitmapFromURL starting");
                            bitmap = getBitmapFromURL(bmpURL);
                            ////Log.d("AsyncFetchPage", "getBitmapFromURL done");
                            success = true;
                            //Log.d("DownloadPages", "download from " + bmpURL + " successful");
                        } catch (IOException e) {
                            //cancel(true);
                            e.printStackTrace();
                        }
                    }

                    if (bitmap != null) {
                        File f = new File(folderpath, "" + chno + "_" + i + ".jpg");

                        try {
                            FileOutputStream fos = new FileOutputStream(f);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            if (!f.exists()) {
                                if (f.createNewFile()) {
                                    //Log.d("DownloadPages", "" + chno + i + " created");
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        }


    }

}
