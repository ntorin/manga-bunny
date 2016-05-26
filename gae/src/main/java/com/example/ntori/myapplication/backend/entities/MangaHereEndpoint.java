package com.example.ntori.myapplication.backend.entities;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "mangaHereApi",
        version = "v1",
        resource = "mangaHere",
        namespace = @ApiNamespace(
                ownerDomain = "entities.backend.myapplication.ntori.example.com",
                ownerName = "entities.backend.myapplication.ntori.example.com",
                packagePath = ""
        )
)
public class MangaHereEndpoint {

    private static final Logger logger = Logger.getLogger(MangaHereEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(MangaHere.class);
    }

    /**
     * Returns the {@link MangaHere} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code MangaHere} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "mangaHere/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public MangaHere get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting MangaHere with ID: " + id);
        MangaHere mangaHere = ofy().load().type(MangaHere.class).id(id).now();
        if (mangaHere == null) {
            throw new NotFoundException("Could not find MangaHere with ID: " + id);
        }
        return mangaHere;
    }

    /**
     * Inserts a new {@code MangaHere}.
     */
    @ApiMethod(
            name = "insert",
            path = "mangaHere",
            httpMethod = ApiMethod.HttpMethod.POST)
    public MangaHere insert(MangaHere mangaHere) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that mangaHere.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(mangaHere).now();
        logger.info("Created MangaHere.");

        return ofy().load().entity(mangaHere).now();
    }

    /**
     * Updates an existing {@code MangaHere}.
     *
     * @param id        the ID of the entity to be updated
     * @param mangaHere the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code MangaHere}
     */
    @ApiMethod(
            name = "update",
            path = "mangaHere/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public MangaHere update(@Named("id") Long id, MangaHere mangaHere) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(mangaHere).now();
        logger.info("Updated MangaHere: " + mangaHere);
        return ofy().load().entity(mangaHere).now();
    }

    /**
     * Deletes the specified {@code MangaHere}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code MangaHere}
     */
    @ApiMethod(
            name = "remove",
            path = "mangaHere/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(MangaHere.class).id(id).now();
        logger.info("Deleted MangaHere with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "mangaHere",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<MangaHere> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<MangaHere> query = ofy().load().type(MangaHere.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<MangaHere> queryIterator = query.iterator();
        List<MangaHere> mangaHereList = new ArrayList<MangaHere>(limit);
        while (queryIterator.hasNext()) {
            mangaHereList.add(queryIterator.next());
        }
        return CollectionResponse.<MangaHere>builder().setItems(mangaHereList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(MangaHere.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find MangaHere with ID: " + id);
        }
    }
}