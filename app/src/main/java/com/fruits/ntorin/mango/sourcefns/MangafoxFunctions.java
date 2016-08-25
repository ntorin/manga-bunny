package com.fruits.ntorin.mango.sourcefns;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.fruits.ntorin.dstore.mangaFoxApi.MangaFoxApi;
import com.fruits.ntorin.dstore.mangaFoxApi.model.CollectionResponseMangaFox;
import com.fruits.ntorin.dstore.mangaFoxApi.model.MangaFox;
import com.fruits.ntorin.dstore.mangaHereApi.MangaHereApi;
import com.fruits.ntorin.mango.home.directory.Title;
import com.fruits.ntorin.mango.packages.ChangeChaptersPackage;
import com.fruits.ntorin.mango.packages.TitlePackage;
import com.fruits.ntorin.mango.packages.TitleResponsePackage;
import com.fruits.ntorin.mango.title.Chapter;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
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
public class MangaFoxFunctions {
    static MangaFoxApi api;
    public static void Setup(){
        //Log.d("MangaFoxSetup", "executing setup");
        Document document = null;
        boolean success = false;
        int tries = 0;
        while (!success) {
            try {
                if(tries++ > 50){
                    return;
                }
                //Log.d("MangaFoxSetup", "connecting");
                document = Jsoup.connect("http://mangafox.me/manga").maxBodySize(0).get();
                //Log.d("MangaFoxSetup", "connected");
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Elements li = document.getElementById("page").getElementsByClass("series_preview");
        int c = 0;
        //Log.d("MangaFoxSetup", "filling db");
        for(Element element : li){
            GetDetailedInfo(element.text(), element.attr("href"));
            //c++;
            //if(c > 2){
            //    break;
            //}
        }
        //Log.d("MangaFoxSetup", "filling db done");
    }

    public static void GetDetailedInfo(String name, String href){
        //Log.d("MangaFoxInfo", "titlePage " + href + " connecting");
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

        if(titlePage != null){
            Elements titleInfo = titlePage.getElementsByTag("tbody").first().children().get(1).children();

            String genres = titleInfo.get(3).text();
            //Log.d("MangaFoxInfo", "genres: " + genres);

            String author = "Unknown";
            author = titleInfo.get(1).text();
            //Log.d("MangaFoxInfo", "author: " + author);

            String artist = "Unknown";
            artist = titleInfo.get(2).text();
            //Log.d("MangaFoxInfo", "artist: " + artist);

            boolean isComplete = false;

            Elements titleData = titlePage.getElementsByClass("data");

            String status = titleData.get(0).getElementsByTag("span").get(0).text();
            //Log.d("MangaFoxInfo", status);
            if(status.split(" ")[0].contains("Completed")){
                isComplete = true;
            }else{
                isComplete = false;
            }

            String rank = titleData.get(1).getElementsByTag("span").get(0).text();
            //Log.d("MangaFoxInfo", rank);
            String unparsedrank = rank.split(" ")[0];
            int rankint = 66535;
            if(StringUtil.isNumeric(rank.substring(0, (unparsedrank.length() - 3)))){
                rankint = Integer.parseInt(rank.substring(0, (unparsedrank.length() - 3)));
            }
            //Log.d("MangaFoxInfo", "rankint: " + rankint);

            Element imgElement = titlePage.getElementsByClass("cover").first().getElementsByTag("img").first();
            String bmpURL = imgElement.attr("src");

            Bitmap cover = null;
            success = false;
            tries = 0;
            while(!success){
                try{
                    tries++;
                    if(tries > 50){
                        break;
                    }
                    cover = getBitmapFromURL(bmpURL);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(cover != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                cover.compress(Bitmap.CompressFormat.PNG, 100, baos);
                coverbytes = baos.toByteArray();

                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            MangaFox mangaFox = new MangaFox();
            mangaFox.setTitle(name);
            mangaFox.setHref(href);
            String s = Base64.encodeToString(coverbytes, Base64.DEFAULT);
            ////Log.d("DirectorySetup", s);
            mangaFox.setCover(s);
            mangaFox.setGenres(genres);
            mangaFox.setAuthor(author);
            mangaFox.setArtist(artist);
            mangaFox.setStatus(isComplete);
            mangaFox.setRank(rankint);

            insertMangaFox(mangaFox);
        }
    }

    public static void insertMangaFox(MangaFox mangaFox) {
        MangaFoxApi.Builder builder = new MangaFoxApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        MangaFoxApi api = builder.build();

        try {
            api.insert(mangaFox).execute();
            //CollectionResponseMangaHere list = api.list().execute();
            //Log.d("insertMangaHere", mangaFox.getTitle() + " inserted to datastore");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TitleResponsePackage GetDirectory(String[] params){
        if (api == null) {
            MangaFoxApi.Builder builder = new MangaFoxApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
            builder.setApplicationName("Mango");
            api = builder.build();
        }
        String nextPageToken = null;
        ArrayList<Title> titleList = null;
        try {

            MangaFoxApi.Search searchfn = api.search();
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
            CollectionResponseMangaFox c;

            c = searchfn.execute();
            //Log.d("AsyncGetDirectory", "search complete");
            //Log.d("nextPageToken", "" + c.getNextPageToken());
            nextPageToken = c.getNextPageToken();

            BitmapFactory.Options options = new BitmapFactory.Options();
            int n = 0;


            titleList = new ArrayList<>();
            if(c != null) {
                if (c.getItems() != null) {
                    for (MangaFox mangaFox : c.getItems()) {
                        //Log.d("AsyncGetDirectory", mangaFox.getTitle());
                        String coverencoded = mangaFox.getCover();
                        byte[] coverdecoded = Base64.decode(coverencoded, Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(coverdecoded, 0, coverdecoded.length, options);

                        titleList.add(n++, new Title(mangaFox.getTitle(), mangaFox.getHref(), b, mangaFox.getGenres(),
                                mangaFox.getAuthor(), mangaFox.getArtist(), mangaFox.getStatus(), mangaFox.getRank()));

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
            //Log.d("MangaFoxTitleSetup", "connected to " + href);
            Element sum = document.getElementsByClass("summary").first();
            if(sum == null){
                //e = document.getelementsbyclass
            }
            summary.add(sum);
            Elements chapters = document.getElementsByClass("chlist");
            int ch = 1;
            chMap = new HashMap<String, Chapter>();
            for(Element elem : chapters) {
                //Log.d("MangaFoxTitleSetup", "size: " + chapters.size());
                for (Element element : elem.getElementsByTag("li")) {
                    Element el = element.getElementsByTag("h3").first();
                    if (el == null) {
                        el = element.getElementsByTag("h4").first();
                    }
                    Element e = el.getElementsByTag("a").first();
                    //Log.d("MangaFoxTitleSetup", e.attr("href") + " owntext: " + e.ownText());
                    String[] text = e.ownText().split(" ");
                    chMap.put(String.valueOf(ch), new Chapter(e.ownText(), e.attr("href"), text[text.length - 1]));
                    //Log.d("MangaFoxTitleSetup", text[text.length - 1]);
                    ch++;
                }
            }
            Elements coverel = document.getElementsByClass("cover");
            if(coverel.size() > 0) {
                String coverURL = document.getElementsByClass("cover").first().getElementsByTag("img").first().attr("src");
                //Log.d("MangaFoxTitleSetup", " " + coverURL);
                cover = getBitmapFromURL(coverURL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitlePackage(summary, chMap, cover);
    }

    public static ChangeChaptersPackage ChangeChapters(String itemcontent, Elements pages, String[] pageURLs){
        try {
            Document document = Jsoup.connect(itemcontent).maxBodySize(0).get();
            Elements li = document.getElementsByAttributeValue("onchange", "change_page(this)");
            ////Log.d("documentli", document.html());
            if(li.first() != null){
                pages = li.first().children();
                pages.remove(pages.size() - 1);
                pageURLs = new String[pages.size()];
                int i = 0;
                for(Element option : pages){
                    pageURLs[i] = itemcontent.replace("1.html", "" + ++i + ".html");
                    if(pageURLs[i - 1].equals(itemcontent)){
                        pageURLs[i - 1] = itemcontent + "" + i + ".html";
                    }
                    //Log.d("MangaFoxCC", "put " + pageURLs[i - 1]);

                    if(i == pages.size()){
                        break;
                    }
                }
            }else{
                ////Log.d("crashing", null);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ChangeChaptersPackage(pages, pageURLs);
    }

    public static String FetchPage(Document document){
        Element e = document.getElementsByClass("read_img").first();
            return e.children().first().children().first().attr("src");

    }

    public static ArrayList<Chapter> UpdateFavorites(String href){
        ArrayList<Chapter> newChapters = new ArrayList<>();
        try {
            Document document = Jsoup.connect(href).get();
            if(document == null){
                return null;
            }
            //Log.d("MangaFoxTitleSetup", "connected to " + href);
            Elements chapters = document.getElementsByClass("chlist");
            for(Element elem : chapters) {
                //Log.d("MangaFoxTitleSetup", "size: " + chapters.size());
                for (Element element : elem.getElementsByTag("li")) {
                    Element el = element.getElementsByTag("h3").first();
                    if (el == null) {
                        el = element.getElementsByTag("h4").first();
                    }
                    Element e = el.getElementsByTag("a").first();
                    //Log.d("MangaFoxTitleSetup", e.attr("href") + " owntext: " + e.ownText());
                    String[] text = e.ownText().split(" ");
                    newChapters.add(new Chapter(e.ownText(), e.attr("href"), text[text.length - 1]));
                    //Log.d("MangaFoxTitleSetup", text[text.length - 1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newChapters;
    }

    public static void searchDeleteMangaFox(String s){
        MangaFoxApi.Builder builder = new MangaFoxApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        MangaFoxApi api = builder.build();

        try {
            MangaFoxApi.SearchDelete searchDelete = api.searchDelete();
            searchDelete.setKeyword(s);
            searchDelete.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
