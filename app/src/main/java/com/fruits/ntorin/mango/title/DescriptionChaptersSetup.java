package com.fruits.ntorin.mango.title;

import android.graphics.Bitmap;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.fruits.ntorin.mango.BitmapFunctions.getBitmapFromURL;

/**
 * Created by Ntori on 4/3/2016.
 */
public class DescriptionChaptersSetup {
    public static TitlePackage MangafoxTitleSetup(String href, Map<String, Chapter> chMap) {
        Elements summary = null;
        try {
            Document document = Jsoup.connect(href).get();
            Log.d("c", "connected to " + href);
            summary = document.getElementsByClass("summary");
            Elements chapters = document.getElementsByClass("tips"); // a class that's similar to DummyItem, but stores chapter info
            int ch = 1;
            chMap = new HashMap<String, Chapter>();
            for (Element element : chapters) {
                Log.d("DescriptionChaptersSet", element.attr("href"));
                chMap.put(String.valueOf(ch), new Chapter(element.text(), element.attr("href")));
                ch++;
            }
            Log.d("DescriptionChaptersSet", "setting text");
            //descriptionFragment.setText(element.text());
            //publishProgress(new ProgressUpdate(summary.first().text(), chMap));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitlePackage(summary, chMap, null); //// TODO: 4/5/2016 get the image
    }

    public static TitlePackage MangahereTitleSetup(String href, Map<String, Chapter> chMap) {
        Elements summary = new Elements();
        Bitmap cover = null;
        try {
            Document document = Jsoup.connect(href).get();
            Log.d("c", "connected to " + href);
            summary.add(document.getElementById("show"));
            Elements chapters = document.getElementsByClass("detail_list").first().getElementsByClass("left");
            int ch = 1;
            chMap = new HashMap<String, Chapter>();
            for (Element element : chapters) {
                Element e = element.children().first();
                Log.d("t", e.attr("href"));
                chMap.put(String.valueOf(ch), new Chapter(e.ownText(), e.attr("href"))); //// FIXME: 4/9/2016  "Show less" appears in the description.
                ch++;
            }
            String coverURL = document.getElementsByClass("manga_detail_top").first().getElementsByClass("img").first().attr("src");
            cover = getBitmapFromURL(coverURL);
            Log.d("s", "setting text");
            //descriptionFragment.setText(element.text());
            //publishProgress(new ProgressUpdate(summary.first().text(), chMap));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitlePackage(summary, chMap, cover);
    }
}

