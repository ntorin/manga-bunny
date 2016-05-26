package com.fruits.ntorin.mango.datastore;


import android.content.ContentValues;

/*import com.example.ntori.myapplication.backend.entities.mangaHereApi.MangaHereApi;
import com.example.ntori.myapplication.backend.entities.mangaHereApi.model.MangaHere;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.util.Key;
import com.google.api.client.util.store.DataStore;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import static com.googlecode.objectify.ObjectifyService.ofy;*/


public class DatastoreFunctions {
    /**
     * Adds a task entity to the Datastore.
     *
     * @param values The data for the title
     * @return The {@link Key} of the entity
     */
    public static void addTitle(ContentValues values) {
        /*MangaHere title = new MangaHere();
        MangaHereApi.Builder builder = new MangaHereApi.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null).setRootUrl();
        title.set("title", values.get("title"));
        title.set("href", values.get("href"));
        title.set("genres", values.get("genres"));
        title.set("author", values.get("author"));
        title.set("artist", values.get("artist"));
        title.set("status", values.get("status"));
        title.set("rank", values.get("rank"));
        ofy().save().entities(title).now();*/
    }
}
