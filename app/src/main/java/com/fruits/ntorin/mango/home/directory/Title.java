package com.fruits.ntorin.mango.home.directory;

import android.graphics.Bitmap;

/**
 * Created by Ntori on 6/24/2016.
 */
public class Title{

    String title;
    String href;
    Bitmap cover;
    String genres;
    String author;
    String artist;
    boolean status;
    double rank;

    public Title(String title, String href, Bitmap cover, String genres, String author, String artist, boolean status, double rank) {
        this.title = title;
        this.href = href;
        this.cover = cover;
        this.genres = genres;
        this.author = author;
        this.artist = artist;
        this.status = status;
        this.rank = rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

}
