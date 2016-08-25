package com.fruits.ntorin.mango.sourcefns;

import android.content.Context;
import android.database.Cursor;

import com.fruits.ntorin.mango.packages.ChangeChaptersPackage;
import com.fruits.ntorin.mango.packages.TitlePackage;
import com.fruits.ntorin.mango.title.Chapter;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Map;

/**
 * Created by Ntori on 6/27/2016.
 */
public class SourceFunctions {
    public static void Setup() {

    }

    public void GetDetailedInfo(String name, String href) {

    }

    public TitlePackage TitleSetup(String href, Map<String, Chapter> chMap) {
        return null;
    }

    public ChangeChaptersPackage ChangeChapters(String itemcontent, Elements pages, String[] pageURLs) {
        return null;
    }

    public String FetchPage(Document document) {
        return null;
    }

    public void UpdateFavorites(String href, Cursor selectQuery, int position, Context context) {

    }
}
