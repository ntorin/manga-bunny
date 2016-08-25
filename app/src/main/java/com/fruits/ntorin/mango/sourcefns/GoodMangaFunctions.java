package com.fruits.ntorin.mango.sourcefns;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.fruits.ntorin.dstore.goodMangaApi.GoodMangaApi;
import com.fruits.ntorin.dstore.goodMangaApi.model.CollectionResponseGoodManga;
import com.fruits.ntorin.dstore.goodMangaApi.model.GoodManga;
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
public class GoodMangaFunctions {
    static GoodMangaApi api;

    public static void Setup(){
        //Log.d("GoodMangaSetup", "executing setup");

        Document document = null;
        boolean success = false;
        while(!success){
            try{
                //Log.d("GoodMangaSetup", "connecting");
                document = Jsoup.connect("http://www.goodmanga.net/manga-list").maxBodySize(0).get();
                //Log.d("GoodMangaSetup", "connected");
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Elements tables = document.getElementsByClass("series_index");
        for(Element e : tables){
            Elements trs = e.children().first().children();
            for(Element tds : trs){
                Elements tdss = tds.children();
                for(Element td : tdss){
                    if(td.children() == null){
                        continue;
                    }else {
                        Element a = td.children().first();
                        if (a == null) {
                            continue;
                        }
                        String title = a.text();
                        String href = a.attr("href");
                        GetDetailedInfo(title, href);
                    }
                }
            }
        }
    }

    public static void GetDetailedInfo(String name, String href){
        //Log.d("GoodMangaSetup", "titlePage " + href + " connecting");
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

        Element seriesinfo = titlePage.getElementById("series_info");
        Elements seriesdetails = titlePage.getElementById("series_details").children();
        int offset = 0;
        if(seriesdetails.size() == 7){
            offset++;
        }
        String author = seriesdetails.get(0 + offset).ownText();
        //Log.d("GoodMangaSetup", author);
        boolean isComplete = false;
        //Log.d("GoodMangaSetup", seriesdetails.get(2+offset).text());
        if(seriesdetails.get(2+offset).text().contains("Completed")){
            isComplete = true;
            //Log.d("GoodMangaSetup", "completed manga");
        }
        //Log.d("GoodMangaSetup", seriesdetails.get(5+offset).text().split("Genres: ")[0]);
        String genres = seriesdetails.get(5+offset).text().split("Genres:")[1];
        double rating = Double.parseDouble(titlePage.getElementById("rating_num").text());
        //Log.d("GoodMangaSetup", " " + rating);
        String coverURL = seriesinfo.children().first().children().first().attr("src");
        byte[] coverbytes = new byte[0];
        Bitmap cover = null;
        success = false;
        tries = 0;
        while(!success){
            try{
                if(tries++ > 50){
                    break;
                }
                cover = getBitmapFromURL(coverURL);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(cover != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            cover.compress(Bitmap.CompressFormat.PNG, 100, baos);
            coverbytes = baos.toByteArray();
            try{
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        GoodManga goodManga = new GoodManga();
        goodManga.setTitle(name);
        goodManga.setHref(href);
        String s = Base64.encodeToString(coverbytes, Base64.DEFAULT);
        goodManga.setCover(s);
        goodManga.setGenres(genres);
        goodManga.setAuthor(author);
        goodManga.setStatus(isComplete);
        goodManga.setRank(rating);

        insertGoodManga(goodManga);

    }

    public static void insertGoodManga(GoodManga goodManga){
        GoodMangaApi.Builder builder = new GoodMangaApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        GoodMangaApi api = builder.build();

        try {
            api.insert(goodManga).execute();
            //CollectionResponseMangaHere list = api.list().execute();
            //Log.d("insertMangaHere", goodManga.getTitle() + " inserted to datastore");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TitleResponsePackage GetDirectory(String[] params){
        if (api == null) {
            GoodMangaApi.Builder builder = new GoodMangaApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
            builder.setApplicationName("Mango");
            api = builder.build();
        }
        String nextPageToken = null;
        ArrayList<Title> titleList = null;
        try {

            GoodMangaApi.Search searchfn = api.search();
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
            CollectionResponseGoodManga c;

            c = searchfn.execute();
            //Log.d("AsyncGetDirectory", "search complete");
            //Log.d("nextPageToken", "" + c.getNextPageToken());
            nextPageToken = c.getNextPageToken();

            BitmapFactory.Options options = new BitmapFactory.Options();
            int n = 0;


            titleList = new ArrayList<>();
            if(c != null) {
                if (c.getItems() != null) {
                    for (GoodManga goodManga : c.getItems()) {
                        //Log.d("AsyncGetDirectory", goodManga.getTitle());
                        String coverencoded = goodManga.getCover();
                        byte[] coverdecoded = Base64.decode(coverencoded, Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(coverdecoded, 0, coverdecoded.length, options);

                        titleList.add(n++, new Title(goodManga.getTitle(), goodManga.getHref(), b, goodManga.getGenres(),
                                goodManga.getAuthor(), goodManga.getArtist(), goodManga.getStatus(), goodManga.getRank()));

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
            //Log.d("c", "connected to " + href);
            Elements seriesdetails = document.getElementById("series_details").children();
            int offset = 0;
            if(seriesdetails.size() == 7){
                offset++;
            }
            Element sum = seriesdetails.get(1 + offset).children().get(1);
            if(sum.children().size() == 0) {
                summary.add(sum);
            }else{
                summary.add(sum.children().get(1));
            }
            //Log.d("test", "texT: " + summary.first().ownText());
            boolean isComplete = false;
            int ch = 1;
            int pgno = 1;
            chMap = new HashMap<>();
            while (!isComplete) {
                Document chdoc = Jsoup.connect(href + "?page=" + pgno++).get();
                Elements chapters = chdoc.getElementById("chapters").getElementsByTag("li");
                for (Element element : chapters) {
                    Element e = element.getElementsByTag("a").first();
                    String[] text = e.ownText().split(" ");
                    if(chMap.get("1") != null) {
                        //Log.d("GoodMangaTP", "" + chMap.get("1").content + ", compared to " + e.attr("href"));
                        if (chMap.get("1").content.equals(e.attr("href"))) {
                            isComplete = true;
                            break;
                        }
                    }
                    chMap.put(String.valueOf(ch), new Chapter(e.ownText(), e.attr("href"), text[text.length - 1]));
                    ch++;
                }
            }
            Element seriesinfo = document.getElementById("series_info");
            String coverURL = seriesinfo.children().first().children().first().attr("src");
            cover = getBitmapFromURL(coverURL);
            //summary.add();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitlePackage(summary, chMap, cover);
    }

    public static ChangeChaptersPackage ChangeChapters(String itemcontent, Elements pages, String[] pageURLs) {
        //Log.d("doc", itemcontent + " connected");
        try {
            Document document = Jsoup.connect(itemcontent).maxBodySize(0).get();
            Element li = document.getElementById("asset_2");
            if(li != null){
                pages = li.select("select.page_select > option");
                //Log.d("asa", "a: " + li.html());
                pageURLs = new String[pages.size()];
                int i = 0;
                for(Element option : pages){

                    pageURLs[i] = option.attr("value");
                    //Log.d("GoodMangaCC", "url: " + pageURLs[i]);
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
        return document.getElementById("manga_viewer").children().get(1).getElementsByTag("img").first().attr("src");
    }

    public static ArrayList<Chapter> UpdateFavorites(String href){
        ArrayList<Chapter> newChapters = new ArrayList<>();

        boolean isComplete = false;
        int pgno = 1;
        while(!isComplete) {
            try {
                Document chdoc = Jsoup.connect(href + "?page=" + pgno++).get();
                Elements chapters = chdoc.getElementById("chapters").getElementsByTag("li");
                for (Element element : chapters) {
                    Element e = element.getElementsByTag("a").first();
                    String[] text = e.ownText().split(" ");
                    if(newChapters.size() > 0) {
                        if (newChapters.get(0) != null) {
                            if (newChapters.get(0).content.equals(e.attr("href"))) {
                                isComplete = true;
                                break;
                            }
                        }
                    }
                    newChapters.add(new Chapter(e.ownText(), e.attr("href"), text[text.length - 1]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newChapters;
    }

    public static void searchDeleteGoodManga(String s){
        GoodMangaApi.Builder builder = new GoodMangaApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        GoodMangaApi api = builder.build();

        try {
            GoodMangaApi.SearchDelete searchDelete = api.searchDelete();
            searchDelete.setKeyword(s);
            searchDelete.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
