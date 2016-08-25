package com.fruits.ntorin.mango.packages;

import org.jsoup.select.Elements;

/**
 * Created by Ntori on 6/27/2016.
 */
public class ChangeChaptersPackage {
    Elements pages;

    public String[] getPageURLs() {
        return pageURLs;
    }

    public void setPageURLs(String[] pageURLs) {
        this.pageURLs = pageURLs;
    }

    public Elements getPages() {
        return pages;
    }

    public void setPages(Elements pages) {
        this.pages = pages;
    }

    String[] pageURLs;

    public ChangeChaptersPackage(Elements pages, String[] pageURLs) {
        this.pages = pages;
        this.pageURLs = pageURLs;
    }
}
