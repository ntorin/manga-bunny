package com.fruits.ntorin.mango.title;

import java.io.Serializable;

/**
 * Created by Ntori on 3/16/2016.
 */
public class Chapter implements Serializable{

    public final String id;
    public final String content;

    public Chapter(String id, String content) {
        this.id = id;
        this.content = content;
    }
}
