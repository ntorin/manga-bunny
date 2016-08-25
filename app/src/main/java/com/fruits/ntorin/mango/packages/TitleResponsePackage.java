package com.fruits.ntorin.mango.packages;

import com.fruits.ntorin.mango.home.directory.Title;

import java.util.ArrayList;

/**
 * Created by Ntori on 6/29/2016.
 */
public class TitleResponsePackage {
    String nextPageToken;
    ArrayList<Title> items;

    public TitleResponsePackage(String nextPageToken, ArrayList<Title> items) {
        this.nextPageToken = nextPageToken;
        this.items = items;
    }

    public TitleResponsePackage() {

    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public ArrayList<Title> getItems() {
        return items;
    }

    public void setItems(ArrayList<Title> items) {
        this.items = items;
    }
}
