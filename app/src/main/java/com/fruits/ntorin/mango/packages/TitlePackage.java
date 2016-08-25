package com.fruits.ntorin.mango.packages;

import android.graphics.Bitmap;

import com.fruits.ntorin.mango.title.Chapter;

import org.jsoup.select.Elements;

import java.util.Map;

/**
 * Created by Ntori on 4/4/2016.
 */
public class TitlePackage{ // TODO: 6/27/2016 maybe needs more refactoring
    Elements elements;
    Map<String, Chapter> chapterMap;
    Bitmap bitmap;

    public Elements getElements() {
        return elements;
    }

    public void setElements(Elements elements) {
        this.elements = elements;
    }

    public Map<String, Chapter> getChapterMap() {
        return chapterMap;
    }

    public void setChapterMap(Map<String, Chapter> chapterMap) {
        this.chapterMap = chapterMap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public TitlePackage(Elements elements,Map<String, Chapter> chapterMap, Bitmap bitmap){
        this.elements = elements;
        this.chapterMap = chapterMap;
        this.bitmap = bitmap;
    }
}
