package com.fruits.ntorin.mango.packages;

/**
 * Created by Ntori on 6/27/2016.
 */
public class FetchPagePackage {
    String bmpURL;
    String title;

    public String getBmpURL() {
        return bmpURL;
    }

    public void setBmpURL(String bmpURL) {
        this.bmpURL = bmpURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FetchPagePackage(String bmpURL, String title) {
        this.bmpURL = bmpURL;
        this.title = title;
    }
}
