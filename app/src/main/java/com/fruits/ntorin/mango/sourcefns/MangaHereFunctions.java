package com.fruits.ntorin.mango.sourcefns;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.fruits.ntorin.dstore.mangaHereApi.MangaHereApi;
import com.fruits.ntorin.dstore.mangaHereApi.model.CollectionResponseMangaHere;
import com.fruits.ntorin.dstore.mangaHereApi.model.MangaHere;
import com.fruits.ntorin.mango.home.directory.Title;
import com.fruits.ntorin.mango.packages.ChangeChaptersPackage;
import com.fruits.ntorin.mango.packages.TitlePackage;
import com.fruits.ntorin.mango.packages.TitleResponsePackage;
import com.fruits.ntorin.mango.title.Chapter;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.fruits.ntorin.mango.utils.BitmapFunctions.getBitmapFromURL;

/**
 * Created by Ntori on 6/27/2016.
 */
public class MangaHereFunctions {
    static MangaHereApi api;

    public static void Setup() {
        //ContentValues values = new ContentValues();

        //Log.d("mheresetup", "executing setup");

        Document document = null;
        boolean success = false;
        while (!success) {
            try {
                //Log.d("mheresetup", "connecting");
                document = Jsoup.connect("http://mangahere.co/mangalist/").maxBodySize(0).get();
                //Log.d("mheresetup", "connected");
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Elements li = document.getElementsByClass("manga_info");
        //Iterator i = li.iterator();
        int c = 0;
        //Log.d("mheresetup", "filling db");
        //db.delete(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME, null, null); //// FIXME: 4/5/2016 potential issues here if connection is lost?
        for (Element element : li) {
            //values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE, element.attr("rel"));
            //values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF, element.attr("href"));
            //if (checkIfExists(dbHelper.getReadableDatabase(), DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME, title.attr("href")) == false) {
            GetDetailedInfo(element.attr("rel"), element.attr("href"));
            //} else {
            //    //Log.d("Setup", "title " + title.attr("rel") +
            //            "(" + title.attr("href") + ") already exists in db; skipping");
            //}
            //c++;
            //if (c > 5) { //// FIXME testing purposes
            //    return;
            //}
        }
        //Log.d("mheresetup", "filling db done");

    }

    public static void GetDetailedInfo(String name, String href) {
        //Log.d("mheresetup", "titlePage " + href + " connecting");
        Document titlePage = null;
        boolean success = false;
        int tries = 0;
        while (!success) {
            try {
                if(tries++ > 50){
                    return;
                }
                titlePage = Jsoup.connect(href).get();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] coverbytes = new byte[0];

        if (titlePage != null) {
            if (titlePage.getElementsByClass("detail_topText").first() != null) {
                Elements titleInfo = titlePage.getElementsByClass("detail_topText").first().children();

                String genres = titleInfo.get(3).ownText();
                //Log.d("mheresetup", "genres: " + genres);
                //values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_GENRES, genres);

                String author = "Unknown";
                if (titleInfo.get(4).getElementsByTag("a").first() != null) {
                    author = titleInfo.get(4).getElementsByTag("a").first().text();
                }
                //Log.d("mheresetup", "author: " + author);
                //values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_AUTHOR, author);

                String artist = "Unknown";
                if (titleInfo.get(5).getElementsByTag("a").first() != null) {
                    artist = titleInfo.get(5).getElementsByTag("a").first().text();
                }
                //Log.d("mheresetup", "artist: " + artist);
                //values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_ARTIST, artist);

                String status = titleInfo.get(6).ownText();
                boolean isComplete = false;
                if (status.split(" ")[0].contains("Complete")) {
                    isComplete = true;
                    //Log.d("mheresetup", "completed manga");
                } else {
                    isComplete = false;
                }
                //Log.d("mheresetup", "status: " + status);
                //values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_STATUS, status);

                String rank = titleInfo.get(7).ownText();
                int rankint = Integer.parseInt(rank.substring(0, (rank.length() - 2)));
                //Log.d("rankint", "" + rankint);
                //Log.d("mheresetup", "rank: " + rank);
                //values.put(DirectoryContract.DirectoryEntry.COLUMN_NAME_RANK, rankint);

                Element imgElement = titlePage.getElementsByClass("img").first();
                String bmpURL = imgElement.attr("src");
                Bitmap cover = null;
                success = false;
                tries = 0;
                while (!success) {
                    try {
                        tries++;
                        if (tries > 50) {
                            break;
                        }
                        cover = getBitmapFromURL(bmpURL);
                        success = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String coverURI = null;
                if (cover != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    cover.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    coverbytes = baos.toByteArray();
                    //coverURI = context.getFileStreamPath("cover" + coverCounter).toURI().toString();
                    ////Log.d("filepathURI", "" + coverURI);
                    try {
                        baos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //DatastoreFunctions.addTitle(values);

                //String name = values.getAsString(DirectoryContract.DirectoryEntry.COLUMN_NAME_TITLE);

                //String href = values.getAsString(DirectoryContract.DirectoryEntry.COLUMN_NAME_HREF);
                //byte[] coverbytes;
                //String genres;
                //String author;
                //String artist;
                //String status;
                //int intrank;

                //DatastoreFunctions.StoreTitle(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME,
                //        name, href, coverbytes, genres, author, artist, status, rankint);
                //MangaTest mangaHere = new MangaTest();
                MangaHere mangaHere = new MangaHere();
                mangaHere.setTitle(name);
                mangaHere.setHref(href);
                String s = Base64.encodeToString(coverbytes, Base64.DEFAULT);
                ////Log.d("DirectorySetup", s);
                mangaHere.setCover(s);
                mangaHere.setGenres(genres);
                mangaHere.setAuthor(author);
                mangaHere.setArtist(artist);
                mangaHere.setStatus(isComplete);
                mangaHere.setRank(rankint);
                //MangaTest mangaTest = new MangaTest();

                insertMangaHere(mangaHere);

                //Log.d("mheresetup", "titlePage " + href + " connected");
                //db.insert(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME, null, values);
            }
        }
    }

    public static void insertMangaHere(MangaHere mangaHere){
        MangaHereApi.Builder builder = new MangaHereApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        MangaHereApi api = builder.build();

        try {
            api.insert(mangaHere).execute();
            //CollectionResponseMangaHere list = api.list().execute();
            //Log.d("insertMangaHere", mangaHere.getTitle() + " inserted to datastore");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void searchDeleteMangaHere(String s){
        MangaHereApi.Builder builder = new MangaHereApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        MangaHereApi api = builder.build();

        try {
            MangaHereApi.SearchDelete searchDelete = api.searchDelete();
            searchDelete.setKeyword(s);
            searchDelete.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TitlePackage TitleSetup(String href, Map<String, Chapter> chMap) {
        Elements summary = new Elements();
        Bitmap cover = null;
        try {
            Document document = Jsoup.connect(href).get();
            //Log.d("c", "connected to " + href);
            summary.add(document.getElementById("show"));
            Elements chapters = document.getElementsByClass("detail_list").first().getElementsByClass("left");
            int ch = 1;
            chMap = new HashMap<String, Chapter>();
            for (Element element : chapters) {
                Element e = element.children().first();
                //Log.d("t", e.attr("href") + " owntext: " + e.ownText());
                String[] text = e.ownText().split(" ");
                chMap.put(String.valueOf(ch), new Chapter(e.ownText(), e.attr("href"), text[text.length - 1]));
                //chMap.put(text[text.length - 1], new Chapter(e.ownText(), e.attr("href"), text[text.length - 1]));
                //Log.d("t", text[text.length - 1]);
                ch++;
            }
            String coverURL = document.getElementsByClass("manga_detail_top").first().getElementsByClass("img").first().attr("src");
            //Log.d("DescriptionChapterSetup", " " + coverURL);
            cover = getBitmapFromURL(coverURL);
            //Log.d("s", "setting text");
            //descriptionFragment.setText(element.text());
            //publishProgress(new ProgressUpdate(summary.first().text(), chMap));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitlePackage(summary, chMap, cover);
    }

    public static ChangeChaptersPackage ChangeChapters(String itemcontent, Elements pages, String[] pageURLs) {
        try {
            Document document = Jsoup.connect(itemcontent).get();
            Elements li = document.getElementsByAttributeValue("onchange", "change_page(this)");
            if(li.first() != null) {
                pages = li.first().children();
                //Log.d("#pages", "" + (pages.size()));
                //Log.d("nextpageurl", "" + li.first().attr("href"));
                pageURLs = new String[pages.size()];
                int i = 0;
                for (Element option : pages) {
                    pageURLs[i] = option.attr("value");
                    //Log.d("getpages", option.attr("value"));
                    i++;
                }
            }else {
                return null;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ChangeChaptersPackage(pages, pageURLs);
    }

    public static String FetchPage(Document document) {
        Element li = document.getElementById("image");

        return li.attr("src");
    }

    public static ArrayList<Chapter> UpdateFavorites(String href) {
        ArrayList<Chapter> newChapters = new ArrayList<>();
        try {
            Document document = Jsoup.connect(href).get();
            Elements detaillist = document.getElementsByClass("detail_list");
            if(detaillist != null) {
                if (detaillist.size() > 0) {
                    Elements chapters = detaillist.first().getElementsByClass("left");
                    //int ch = 1;
                    for (Element element : chapters) {
                        Element e = element.children().first();
                        //Log.d("t", e.attr("href"));
                        String[] text = e.ownText().split(" ");
                        newChapters.add(new Chapter(e.ownText(), e.attr("href"), text[text.length - 1]));
                        //ch++;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return newChapters;
    }

    public static TitleResponsePackage GetDirectory(String[] params){
        if (api == null) {
            MangaHereApi.Builder builder = new MangaHereApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
            builder.setApplicationName("Mango");
            api = builder.build();
            //Log.d("MangaHereApi", "set api");
        }
        String nextPageToken = null;
        ArrayList<Title> titleList = null;
        try {
            /*MangaHereApi.List listfn = api.list();
            listfn.setLimit(20);
            if(params != null) {
                listfn.setCursor(params[0]);
            } */
            //CollectionResponseMangaHere list = listfn.execute();
            //nextPageToken = list.getNextPageToken();

            MangaHereApi.Search searchfn = api.search();
            if(params != null){
                if(params.length > 4){
                    searchfn.setCursor(params[3]);
                    //Log.d("setcursor", "cursor set as " + params[3]);
                    if(params[0] != null){
                        searchfn.setSearchString(params[0]);
                    }
                    if(params[1] != null){
                        searchfn.setGenrelist(params[1]);
                    }
                    if(params[2] != null){
                        searchfn.setSort(params[2]);
                    }
                    if(params[4] != null){
                        searchfn.setCompletion(params[4]);
                    }
                    //Log.d("searchfn", "set cursor " + params[3]);
                }else if(params.length > 1) {
                    searchfn.setSearchString(params[0]);
                    searchfn.setGenrelist(params[1]);
                    searchfn.setSort(params[2]);
                    searchfn.setCompletion(params[3]);
                }else{
                    searchfn.setSearchString(params[0]);
                }
            }
            //searchfn
            //Log.d("AsyncGetDirectory", "executing search");
            CollectionResponseMangaHere c;

            c = searchfn.execute();
            //Log.d("AsyncGetDirectory", "search complete");
            //Log.d("nextPageToken", "" + c.getNextPageToken());
            nextPageToken = c.getNextPageToken();
            //c.getItems();

            BitmapFactory.Options options = new BitmapFactory.Options();
            int n = 0;


            titleList = new ArrayList<>();
            if(c != null) {
                ////Log.d("AsyncGetDirectory", "" + c.size());
                if (c.getItems() != null) {
                    for (MangaHere mangaHere : c.getItems()) {
                        //Log.d("AsyncGetDirectory", mangaHere.getTitle());
                        String coverencoded = mangaHere.getCover();
                        byte[] coverdecoded = Base64.decode(coverencoded, Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(coverdecoded, 0, coverdecoded.length, options);

                        titleList.add(n++, new Title(mangaHere.getTitle(), mangaHere.getHref(), b, mangaHere.getGenres(),
                                mangaHere.getAuthor(), mangaHere.getArtist(), mangaHere.getStatus(), mangaHere.getRank()));

                    }
                }
            }
            // TODO: 6/25/2016 change limit from 20 to n/a
                /*list.size();
                List<MangaHere> items = list.getItems();
                MangaHere item = items.get(0);
                //DirectoryFragment.testbmp = b;
                ////Log.d("AsyncMangaHereTask", "" + items.size());



            if (items != null) {
                for (MangaHere i : items) {
                    //Log.d("AsyncMangaHereTask", i.getTitle());
                    String coverencoded = i.getCover();
                    byte[] coverdecoded = Base64.decode(coverencoded, Base64.DEFAULT);
                    Bitmap b = BitmapFactory.decodeByteArray(coverdecoded, 0, coverdecoded.length, options);

                    titleList.add(n++, new Title(i.getTitle(), i.getHref(), b, i.getGenres(),
                            i.getAuthor(), i.getArtist(), i.getStatus(), i.getRank()));
                }
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitleResponsePackage(nextPageToken, titleList);
    }

    public static class AsyncGetDirectory extends AsyncTask<String, Void, TitleResponsePackage> { //UNUSED

        ProgressDialog pdialog;
        Context context;

        public AsyncGetDirectory(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            //Log.d("AsyncGetDirectory", "starting preexec");
            super.onPreExecute();
            pdialog = new ProgressDialog(context);
            pdialog.setMessage("Loading ...");
            pdialog.show();
        }

        @Override
        protected TitleResponsePackage doInBackground(String... params) {
            if (api == null) {
                MangaHereApi.Builder builder = new MangaHereApi.Builder(
                        AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
                builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
                builder.setApplicationName("Mango");
                api = builder.build();
                //Log.d("MangaHereApi", "set api");
            }
            String nextPageToken = null;
            ArrayList<Title> titleList = null;
            try {
                MangaHereApi.List listfn = api.list();
                listfn.setLimit(20);
                if(params != null) {
                    listfn.setCursor(params[0]);
                }
                //CollectionResponseMangaHere list = listfn.execute();
                //nextPageToken = list.getNextPageToken();

                MangaHereApi.Search searchfn = api.search();
                if(params != null){
                    if(params.length > 4){
                        searchfn.setCursor(params[3]);
                        //Log.d("setcursor", "cursor set as " + params[3]);
                        if(params[0] != null){
                            searchfn.setSearchString(params[0]);
                        }
                        if(params[1] != null){
                            searchfn.setGenrelist(params[1]);
                        }
                        if(params[2] != null){
                            searchfn.setSort(params[2]);
                        }
                        if(params[4] != null){
                            searchfn.setCompletion(params[4]);
                        }
                        //Log.d("searchfn", "set cursor " + params[3]);
                    }else if(params.length > 1) {
                        searchfn.setSearchString(params[0]);
                        searchfn.setGenrelist(params[1]);
                        searchfn.setSort(params[2]);
                        searchfn.setCompletion(params[3]);
                    }else{
                        searchfn.setSearchString(params[0]);
                    }
                }
                //searchfn
                //Log.d("AsyncGetDirectory", "executing search");
                CollectionResponseMangaHere c;

                c = searchfn.execute();
                //Log.d("AsyncGetDirectory", "search complete");
                //Log.d("nextPageToken", "" + c.getNextPageToken());
                nextPageToken = c.getNextPageToken();
                //c.getItems();

                BitmapFactory.Options options = new BitmapFactory.Options();
                int n = 0;


                titleList = new ArrayList<>();
                if(c != null) {
                    ////Log.d("AsyncGetDirectory", "" + c.size());
                    if (c.getItems() != null) {
                        for (MangaHere mangaHere : c.getItems()) {
                            //Log.d("AsyncGetDirectory", mangaHere.getTitle());
                            String coverencoded = mangaHere.getCover();
                            byte[] coverdecoded = Base64.decode(coverencoded, Base64.DEFAULT);
                            Bitmap b = BitmapFactory.decodeByteArray(coverdecoded, 0, coverdecoded.length, options);

                            titleList.add(n++, new Title(mangaHere.getTitle(), mangaHere.getHref(), b, mangaHere.getGenres(),
                                    mangaHere.getAuthor(), mangaHere.getArtist(), mangaHere.getStatus(), mangaHere.getRank()));

                        }
                    }
                }
                // TODO: 6/25/2016 change limit from 20 to n/a
                /*list.size();
                List<MangaHere> items = list.getItems();
                MangaHere item = items.get(0);
                //DirectoryFragment.testbmp = b;
                ////Log.d("AsyncMangaHereTask", "" + items.size());



            if (items != null) {
                for (MangaHere i : items) {
                    //Log.d("AsyncMangaHereTask", i.getTitle());
                    String coverencoded = i.getCover();
                    byte[] coverdecoded = Base64.decode(coverencoded, Base64.DEFAULT);
                    Bitmap b = BitmapFactory.decodeByteArray(coverdecoded, 0, coverdecoded.length, options);

                    titleList.add(n++, new Title(i.getTitle(), i.getHref(), b, i.getGenres(),
                            i.getAuthor(), i.getArtist(), i.getStatus(), i.getRank()));
                }
            }*/
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new TitleResponsePackage(nextPageToken, titleList);
        }

        @Override
        protected void onPostExecute(TitleResponsePackage titleResponsePackage) {
            super.onPostExecute(titleResponsePackage);
            //Log.d("AsyncGetDirectory", "starting postexec");
            try {
                if ((this.pdialog != null) && this.pdialog.isShowing()) {
                    this.pdialog.dismiss();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                this.pdialog = null;
            }
        }
    }
}
