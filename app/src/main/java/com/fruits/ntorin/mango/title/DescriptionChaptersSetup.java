package com.fruits.ntorin.mango.title;

import com.fruits.ntorin.mango.packages.TitlePackage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ntori on 4/3/2016.
 */
public class DescriptionChaptersSetup {
    public static TitlePackage MangafoxTitleSetup(String href, Map<String, Chapter> chMap) {
        Elements summary = null;
        try {
            Document document = Jsoup.connect(href).get();
            //Log.d("c", "connected to " + href);
            summary = document.getElementsByClass("summary");
            Elements chapters = document.getElementsByClass("tips");
            int ch = 1;
            chMap = new HashMap<String, Chapter>();
            for (Element element : chapters) {
                //Log.d("DescriptionChaptersSet", element.attr("href"));
                //chMap.put(String.valueOf(ch), new Chapter(element.text(), element.attr("href"))); // FIXME: 6/11/2016 Chapter constructor changed
                ch++;
            }
            //Log.d("DescriptionChaptersSet", "setting text");
            //descriptionFragment.setText(element.text());
            //publishProgress(new ProgressUpdate(summary.first().text(), chMap));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TitlePackage(summary, chMap, null); //// TODO: 4/5/2016 get the image
    }


}

