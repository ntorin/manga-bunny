package com.fruits.ntorin.mango.sourcefns;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.fruits.ntorin.dstore.mangaFoxApi.MangaFoxApi;
import com.fruits.ntorin.dstore.mangaInnApi.MangaInnApi;
import com.fruits.ntorin.dstore.mangaInnApi.model.CollectionResponseMangaInn;
import com.fruits.ntorin.dstore.mangaInnApi.model.MangaInn;
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
public class MangaInnFunctions {
    static MangaInnApi api;

    public static void Setup(){
        //Log.d("MangaInnSetup", "executing setup");

        Document document = null;
        boolean success = false;
        while(!success){
            try{
                //Log.d("MangaInnSetup", "connecting");
                document = Jsoup.connect("http://www.mangainn.me/MangaList").maxBodySize(0).get();
                //Log.d("MangaInnSetup", "connected");
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Elements li = document.getElementsByClass("mangalistItems");
        for(Element e : li){
            Element a = e.getElementsByTag("a").first();
            GetDetailedInfo(a.ownText(), a.attr("href"));
        }
    }

    public static void GetDetailedInfo(String name, String href){
        //Log.d("MangaInnSetup", "titlePage " + href + " connecting");
        Document titlePage = null;
        boolean success = false;
        int tries = 0;
        while (!success){
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


        Elements tds = titlePage.getElementsByClass("BlackLabel14");

        String status = tds.get(2).text();
        //Log.d("MangaInnSetup", " " + status);
        boolean isComplete = false;
        if(status.equals("Completed")){
            //Log.d("MangaInnSetup", "completed manga");
            isComplete = true;
        }
        String author = tds.get(3).text();
        //Log.d("MangaInnSetup", " " + author);
        String artist = tds.get(4).text();
        //Log.d("MangaInnSetup", " " + artist);
        String genres = tds.get(5).text();
        //Log.d("MangaInnSetup", " " + genres);

        String viewstring = tds.get(7).text().split(" ")[1];
        int views = 0;
        //Log.d("MangaInnSetup", " " + viewstring);
        if(StringUtil.isNumeric(viewstring)){
            views = Integer.parseInt(viewstring);
        }


        String coverurl = titlePage.getElementsByAttributeValueContaining("href", href).first()
                .children().first().attr("src");

        Bitmap cover = null;
        byte[] coverbytes = new byte[0];
        success = false;
        tries = 0;
        while(!success){
            try{
                if(tries++ > 50){
                    break;
                }
                cover = getBitmapFromURL(coverurl);
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

        MangaInn mangaInn = new MangaInn();
        mangaInn.setTitle(name);
        mangaInn.setHref(href);
        String s = Base64.encodeToString(coverbytes, Base64.DEFAULT);
        mangaInn.setCover(s);
        mangaInn.setGenres(genres);
        mangaInn.setAuthor(author);
        mangaInn.setArtist(artist);
        mangaInn.setStatus(isComplete);
        mangaInn.setRank(views);

        insertMangaInn(mangaInn);

        //Log.d("MangaInnSetup", "titlePage " + href + " connected");
    }

    public static void insertMangaInn(MangaInn mangaInn){
        MangaInnApi.Builder builder = new MangaInnApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        MangaInnApi api = builder.build();

        try {
            api.insert(mangaInn).execute();
            //CollectionResponseMangaHere list = api.list().execute();
            //Log.d("insertMangaInn", mangaInn.getTitle() + " inserted to datastore");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TitleResponsePackage GetDirectory(String[] params){
        if (api == null) {
            MangaInnApi.Builder builder = new MangaInnApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
            builder.setApplicationName("Mango");
            api = builder.build();
        }
        String nextPageToken = null;
        ArrayList<Title> titleList = null;
        try {

            MangaInnApi.Search searchfn = api.search();
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
            CollectionResponseMangaInn c;

            c = searchfn.execute();
            //Log.d("AsyncGetDirectory", "search complete");
            //Log.d("nextPageToken", "" + c.getNextPageToken());
            nextPageToken = c.getNextPageToken();

            BitmapFactory.Options options = new BitmapFactory.Options();
            int n = 0;


            titleList = new ArrayList<>();
            if(c != null) {
                if (c.getItems() != null) {
                    for (MangaInn mangaInn : c.getItems()) {
                        //Log.d("AsyncGetDirectory", mangaInn.getTitle());
                        String coverencoded = mangaInn.getCover();
                        byte[] coverdecoded = Base64.decode(coverencoded, Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(coverdecoded, 0, coverdecoded.length, options);

                        titleList.add(n++, new Title(mangaInn.getTitle(), mangaInn.getHref(), b, mangaInn.getGenres(),
                                mangaInn.getAuthor(), mangaInn.getArtist(), mangaInn.getStatus(), mangaInn.getRank()));

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
            //Log.d("MangaInnTS", "connected to " + href);
            summary.add(document.getElementsByAttributeValue("itemprop", "description").first());
            Elements tbody = document.getElementsByTag("tbody");
            Elements chapters = tbody.get(tbody.size() - 1).children();
            //Log.d("MangaInnTS", chapters.html());
            chMap = new HashMap<>();
            int ch = chapters.size();
            for(Element e : chapters){
                //Log.d("MangaInnTS", e.html());
                Element a = e.getElementsByTag("a").first();
                if(a == null){
                    break;
                }
                String strongtext = a.text();
                String title = strongtext.split(":")[0];
                String[] text = title.split(" ");
                chMap.put(String.valueOf(ch), new Chapter(title, a.attr("href"), text[text.length - 1]));
                ch--;
            }
            String coverURL = document.getElementsByAttributeValueContaining("href", href).first()
                    .children().first().attr("src");
            cover = getBitmapFromURL(coverURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitlePackage(summary, chMap, cover);
    }

    public static ChangeChaptersPackage ChangeChapters(String itemcontent, Elements pages, String[] pageURLs){
        try{
            Document document = Jsoup.connect(itemcontent).get();
            Elements li = document.getElementById("cmbpages").children();
            if(li.first() != null){
                pages = li;
                pageURLs = new String[pages.size()];
                int i = 0;
                for(Element option : pages){
                    pageURLs[i] = itemcontent + "/page_" + ++i;
                    //Log.d("MangaInnCC", "put " + pageURLs[i - 1]);

                    if(i == pages.size()){
                        break;
                    }
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
        return document.getElementById("imgPage").attr("src");
    }

    public static ArrayList<Chapter> UpdateFavorites(String href){
        ArrayList<Chapter> newChapters = new ArrayList<>();
        try{
            Document document = Jsoup.connect(href).get();
            //Log.d("MangaInnUF", "connected to " + href);
            Elements tbody = document.getElementsByTag("tbody");
            Elements chapters = tbody.get(tbody.size() - 1).children();
            for(Element e : chapters){
                //Log.d("MangaInnTS", e.html());
                Element a = e.getElementsByTag("a").first();
                if(a == null){
                    break;
                }
                String strongtext = a.text();
                String title = strongtext.split(":")[0];
                String[] text = title.split(" ");
                newChapters.add(new Chapter(title, a.attr("href"), text[text.length - 1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newChapters;
    }

    public static void searchDeleteMangaInn(String s){
        MangaInnApi.Builder builder = new MangaInnApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        MangaInnApi api = builder.build();

        try {
            MangaInnApi.SearchDelete searchDelete = api.searchDelete();
            searchDelete.setKeyword(s);
            searchDelete.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
