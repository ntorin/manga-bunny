package com.fruits.ntorin.mango.title;

import android.graphics.Bitmap;

import org.jsoup.select.Elements;

import java.util.Map;

/**
 * Created by Ntori on 4/4/2016.
 */
public class TitlePackage{
    Elements elements;
    Map<String, Chapter> chapterMap;
    Bitmap bitmap;

    public TitlePackage(Elements elements,Map<String, Chapter> chapterMap, Bitmap bitmap){
        this.elements = elements;
        this.chapterMap = chapterMap;
        this.bitmap = bitmap;
    }
}
