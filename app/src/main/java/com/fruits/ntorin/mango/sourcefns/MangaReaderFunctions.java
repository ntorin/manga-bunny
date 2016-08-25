package com.fruits.ntorin.mango.sourcefns;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.fruits.ntorin.dstore.mangaReaderApi.MangaReaderApi;
import com.fruits.ntorin.dstore.mangaReaderApi.model.CollectionResponseMangaReader;
import com.fruits.ntorin.dstore.mangaReaderApi.model.MangaReader;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.fruits.ntorin.mango.utils.BitmapFunctions.getBitmapFromURL;

/**
 * Created by Ntori on 6/27/2016.
 */
public class MangaReaderFunctions {
    static MangaReaderApi api;

    public static void Setup(){

        //Log.d("MangaPandaSetup", "executing setup");
        Document document = null;
        boolean success = false;
        while(!success){
            try{
                //Log.d("MangaPandaSetup", "connecting");
                document = Jsoup.connect("http://www.mangareader.net/alphabetical").maxBodySize(0).get();
                //Log.d("MangaPandaSetup", "connected");
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Elements seriescol = document.getElementsByClass("series_col");
        for(Element e : seriescol){
            Elements ul = e.getElementsByTag("ul");
            for(Element liss : ul){
                Elements lis = liss.children();
                for(Element li : lis){
                    Element a = li.getElementsByTag("a").first();
                    if(a != null) {
                        String title = a.text();
                        String href = a.absUrl("href");
                        GetDetailedInfo(title, href);
                    }
                }
            }
        }
    }

    public static void GetDetailedInfo(String name, String href){
        //Log.d("MangaPandaSetup", "titlePage " + href + " connecting");
        Document titlePage = null;
        boolean success = false;
        int tries = 0;
        while(!success){
            try{
                if(tries++ > 50){
                    return;
                }
                titlePage = Jsoup.connect(href).get();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Element mangaproperties = titlePage.getElementById("mangaproperties");
        Element tbody = mangaproperties.getElementsByTag("tbody").first();
        Elements trs = tbody.children();
        String author = trs.get(4).children().get(1).text();
        //Log.d("MangaPandaDetailedInfo", " " + author);
        String artist = trs.get(5).children().get(1).text();
        //Log.d("MangaPandaDetailedInfo", " " + artist);
        Elements genrea = trs.get(7).children().get(1).children();
        String[] genreslist = trs.get(7).children().get(1).text().split(" ");
        String genres = "";
        for(Element e : genrea){
            genres += e.text() + ", ";
        }
        genres = genres.substring(0, genres.length() - 2);
        //Log.d("MangaPandaDetailedInfo", " " + genres);
        String status = trs.get(3).children().get(1).text();
        //Log.d("MangaPandaDetailedInfo", " " + status);
        boolean iscomplete = false;
        if(status.equals("Completed")){
            //Log.d("MangaPandaDetailedInfo", "completed manga");
            iscomplete = true;
        }



        String coverurl = titlePage.getElementById("mangaimg").children().first().attr("src");
        Bitmap cover = null;
        byte[] coverbytes = new byte[0];
        success = false;
        tries = 0;
        while (!success) {
            try {
                tries++;
                if (tries > 50) {
                    break;
                }
                cover = getBitmapFromURL(coverurl);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

        MangaReader mangaReader = new MangaReader();
        mangaReader.setTitle(name);
        mangaReader.setHref(href);
        String s = Base64.encodeToString(coverbytes, Base64.DEFAULT);
        mangaReader.setCover(s);
        mangaReader.setGenres(genres);
        mangaReader.setAuthor(author);
        mangaReader.setArtist(artist);
        mangaReader.setStatus(iscomplete);

        insertMangaReader(mangaReader);

        //Log.d("MangaPandaSetup", "titlePage " + href + " connected");

    }

    public static void insertMangaReader(MangaReader mangaReader){
        MangaReaderApi.Builder builder = new MangaReaderApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        MangaReaderApi api = builder.build();

        try {
            api.insert(mangaReader).execute();
            //CollectionResponseMangaHere list = api.list().execute();
            //Log.d("insertMangaPanda", mangaReader.getTitle() + " inserted to datastore");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TitleResponsePackage GetDirectory(String[] params){
        if (api == null) {
            MangaReaderApi.Builder builder = new MangaReaderApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
            builder.setApplicationName("Mango");
            api = builder.build();
        }
        String nextPageToken = null;
        ArrayList<Title> titleList = null;
        try {

            MangaReaderApi.Search searchfn = api.search();
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
            //Log.d("AsyncGetDirectory", "executing search");
            CollectionResponseMangaReader c;

            c = searchfn.execute();
            //Log.d("AsyncGetDirectory", "search complete");
            //Log.d("nextPageToken", "" + c.getNextPageToken());
            nextPageToken = c.getNextPageToken();

            BitmapFactory.Options options = new BitmapFactory.Options();
            int n = 0;


            titleList = new ArrayList<>();
            if(c != null) {
                if (c.getItems() != null) {
                    for (MangaReader mangaReader : c.getItems()) {
                        //Log.d("AsyncGetDirectory", mangaReader.getTitle());
                        String coverencoded = mangaReader.getCover();
                        byte[] coverdecoded = Base64.decode(coverencoded, Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(coverdecoded, 0, coverdecoded.length, options);

                        titleList.add(n++, new Title(mangaReader.getTitle(), mangaReader.getHref(), b, mangaReader.getGenres(),
                                mangaReader.getAuthor(), mangaReader.getArtist(), mangaReader.getStatus(), mangaReader.getRank()));

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitleResponsePackage(nextPageToken, titleList);
    }

    public static TitlePackage TitleSetup(String href, Map<String, Chapter> chMap){
        Elements summary = new Elements();
        Bitmap cover = null;
        try{
            Document document = Jsoup.connect(href).get();
            //Log.d("MangaReaderTS", "connected to " + href);
            summary.add(document.getElementById("readmangasum").children().get(1));
            Elements chapters = document.getElementById("chapterlist").getElementsByTag("tr");
            chapters.remove(0);
            ArrayList<Chapter> chapterList = new ArrayList<>();
            int ch = chapters.size();
            //Log.d("MangaReaderTS", "size: " + ch);
            chMap = new HashMap<>();
            for(Element e : chapters){
                Element a = e.children().first().children().get(1);
                String[] text = a.ownText().split(" ");
                //Log.d("MangaReaderTS", "href: " + a.absUrl("href") + "");
                chMap.put(String.valueOf(ch), new Chapter(a.ownText(), a.absUrl("href"), text[text.length - 1]));
                ch--;
            }
            String coverURL = document.getElementById("mangaimg").children().first().attr("src");
            cover = getBitmapFromURL(coverURL);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitlePackage(summary, chMap, cover);
    }

    public static ChangeChaptersPackage ChangeChapters(String itemcontent, Elements pages, String[] pageURLs){
        try{
            Document document = Jsoup.connect(itemcontent).get();
            Elements li = document.getElementById("pageMenu").children();
            if(li.first() != null){
                pages = li;
                pageURLs = new String[pages.size()];
                int i = 0;
                for(Element option : pages){
                    pageURLs[i] = option.absUrl("value");
                    //Log.d("MangaReaderCC", "href: " + pageURLs[i]);
                    i++;
                }
            }else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ChangeChaptersPackage(pages, pageURLs);
    }

    public static String FetchPage(Document document){
        return document.getElementById("img").attr("src");
    }

    public static ArrayList<Chapter> UpdateFavorites(String href){
        ArrayList<Chapter> newChapters = new ArrayList<>();
        try{
            Document document = Jsoup.connect(href).get();
            //Log.d("MangaReaderTS", "connected to " + href);
            Elements chapters = document.getElementById("chapterlist").getElementsByTag("tr");
            chapters.remove(0);
            for(Element e : chapters){
                Element a = e.children().first().children().get(1);
                String[] text = a.ownText().split(" ");
                //Log.d("MangaReaderTS", "href: " + a.absUrl("href") + "");
                newChapters.add(new Chapter(a.ownText(), a.absUrl("href"), text[text.length - 1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newChapters;
    }

    public static void searchDeleteMangaReader(String s){
        MangaReaderApi.Builder builder = new MangaReaderApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        MangaReaderApi api = builder.build();

        try {
            MangaReaderApi.SearchDelete searchDelete = api.searchDelete();
            searchDelete.setKeyword(s);
            searchDelete.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
