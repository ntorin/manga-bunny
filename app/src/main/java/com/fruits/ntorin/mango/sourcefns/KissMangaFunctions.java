package com.fruits.ntorin.mango.sourcefns;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.fruits.ntorin.dstore.kissmangaApi.KissmangaApi;
import com.fruits.ntorin.dstore.kissmangaApi.model.Kissmanga;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.fruits.ntorin.mango.utils.BitmapFunctions.getBitmapFromURL;

/**
 * Created by Ntori on 6/27/2016.
 */
public class KissmangaFunctions {
    static KissmangaApi api;

    public static void Setup(){
        //Log.d("KissmangaSetup", "executing setup");
        Document document = null;
        boolean success = false;
        while(!success){
            try{
                int i = 1;
                while(true){
                    //Log.d("KissmangaSetup", "connecting");
                    document = Jsoup.connect("http://kissmanga.com/MangaList?page=" + i++)
                            .userAgent("Mozilla")
                            .validateTLSCertificates(false)
                            .timeout(4000).followRedirects(false).maxBodySize(0).get();
                    if(document.getElementsByTag("tbody").size() == 0){
                        return;
                    }
                    Element tbody = document.getElementsByTag("tbody").first();
                    Elements trs = tbody.children();
                    for(int n = 2; i < trs.size(); n++){
                        Element tr = trs.get(n);
                        Element a = tr.getElementsByTag("a").first();
                        String href = a.absUrl("href");
                        String title = a.text();
                        GetDetailedInfo(title, href);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void GetDetailedInfo(String name, String href){
        //Log.d("KissmangaSetup", "titlePage " + href + " connecting");
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

        String author;
        String genres;
        boolean isComplete = false;
        int views;
        String[] viewstring = new String[0];
        String viewsunparsed = null;
        byte[] coverbytes = new byte[0];
        String coverURL;
        Bitmap cover = null;

        Elements barcontent = titlePage.getElementsByClass("barContent");
        Element divcontent = barcontent.get(1);
        Elements ps = divcontent.getElementsByTag("p");
        String genrestext = ps.get(1).text();

        genres = genrestext.split("Genres: ")[1];
        author = ps.get(2).text().split("Author: ")[1];

        String[] statusviews = ps.get(3).text().split(" ");
        int n = 0;
        for(String s : statusviews){
            n++;
            if(s.equals("Status")){
                if(statusviews[n+1].equals("Completed")){
                    isComplete = true;
                }
                if(s.equals("Views")){
                    viewstring = statusviews[n+1].split(",");
                }
            }
        }

        for(String s : viewstring){
            viewsunparsed += s;
        }

        views = Integer.parseInt(viewsunparsed);

        coverURL = barcontent.get(5).children().get(2).children().first().attr("src");
        success = false;
        tries = 0;
        while(!success){
            try {
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
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Kissmanga kissmanga = new Kissmanga();
        kissmanga.setTitle(name);
        kissmanga.setHref(href);
        String s = Base64.encodeToString(coverbytes, Base64.DEFAULT);
        kissmanga.setCover(s);
        kissmanga.setGenres(genres);
        kissmanga.setAuthor(author);
        kissmanga.setStatus(isComplete);
        kissmanga.setRank(views);

        insertKissmanga(kissmanga);

        //Log.d("KissmangaSetup", "titlePage " + href + " connected");
    }

    public static void insertKissmanga(Kissmanga kissmanga){
        KissmangaApi.Builder builder = new KissmangaApi.Builder(
                AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setRootUrl("https://core-1590.appspot.com/_ah/api/");
        builder.setApplicationName("Mango");
        KissmangaApi api = builder.build();

        try {
            api.insert(kissmanga).execute();
            //CollectionResponseMangaHere list = api.list().execute();
            //Log.d("insertMangaHere", kissmanga.getTitle() + " inserted to datastore");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
