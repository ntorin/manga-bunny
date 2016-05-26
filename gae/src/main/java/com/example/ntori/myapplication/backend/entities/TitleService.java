package com.example.ntori.myapplication.backend.entities;

import com.googlecode.objectify.ObjectifyService;

/**
 * Created by Ntori on 5/24/2016.
 */
public class TitleService {
    public TitleService(){
        ObjectifyService.register(MangaHere.class);
    }
}
