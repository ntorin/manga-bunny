package com.example.ntori.myapplication.backend.entities;


import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Entity
public class MangaHere {

    @Id Long id;
    String title;
    String href;
    String genres;
    String author;
    String artist;
    String status;
    int rank;

    private MangaHere(){}

    public MangaHere(String title, String href, String genres, String author, String artist,
                     String status, int rank){
        this.title = title;
        this.href = href;
        this.genres = genres;
        this.author = author;
        this.artist = artist;
        this.status = status;
        this.rank = rank;
    }

}
