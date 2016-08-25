package com.fruits.ntorin.dstore;

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
        name = "kissmangaApi",
        version = "v1",
        resource = "kissmanga",
        namespace = @ApiNamespace(
                ownerDomain = "dstore.ntorin.fruits.com",
                ownerName = "dstore.ntorin.fruits.com",
                packagePath = ""
        )
)
public class KissmangaEndpoint {

    private static final Logger logger = Logger.getLogger(KissmangaEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Kissmanga.class);
    }

    /**
     * Returns the {@link Kissmanga} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Kissmanga} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "kissmanga/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Kissmanga get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting Kissmanga with ID: " + id);
        Kissmanga kissmanga = ofy().load().type(Kissmanga.class).id(id).now();
        if (kissmanga == null) {
            throw new NotFoundException("Could not find Kissmanga with ID: " + id);
        }
        return kissmanga;
    }

    /**
     * Inserts a new {@code Kissmanga}.
     */
    @ApiMethod(
            name = "insert",
            path = "kissmanga",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Kissmanga insert(Kissmanga kissmanga) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that kissmanga.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(kissmanga).now();
        logger.info("Created Kissmanga with ID: " + kissmanga.getId());

        return ofy().load().entity(kissmanga).now();
    }

    /**
     * Updates an existing {@code Kissmanga}.
     *
     * @param id        the ID of the entity to be updated
     * @param kissmanga the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Kissmanga}
     */
    @ApiMethod(
            name = "update",
            path = "kissmanga/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Kissmanga update(@Named("id") Long id, Kissmanga kissmanga) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(kissmanga).now();
        logger.info("Updated Kissmanga: " + kissmanga);
        return ofy().load().entity(kissmanga).now();
    }

    /**
     * Deletes the specified {@code Kissmanga}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Kissmanga}
     */
    @ApiMethod(
            name = "remove",
            path = "kissmanga/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Kissmanga.class).id(id).now();
        logger.info("Deleted Kissmanga with ID: " + id);
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
            path = "kissmanga",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Kissmanga> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Kissmanga> query = ofy().load().type(Kissmanga.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Kissmanga> queryIterator = query.iterator();
        List<Kissmanga> kissmangaList = new ArrayList<Kissmanga>(limit);
        while (queryIterator.hasNext()) {
            kissmangaList.add(queryIterator.next());
        }
        return CollectionResponse.<Kissmanga>builder().setItems(kissmangaList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(Kissmanga.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Kissmanga with ID: " + id);
        }
    }
}