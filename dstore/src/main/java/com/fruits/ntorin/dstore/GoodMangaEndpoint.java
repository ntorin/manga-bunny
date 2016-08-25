package com.fruits.ntorin.dstore;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
        name = "goodMangaApi",
        version = "v1",
        resource = "goodManga",
        namespace = @ApiNamespace(
                ownerDomain = "dstore.ntorin.fruits.com",
                ownerName = "dstore.ntorin.fruits.com",
                packagePath = ""
        )
)
public class GoodMangaEndpoint {

    private static final Logger logger = Logger.getLogger(GoodMangaEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(GoodManga.class);
    }

    /**
     * Returns the {@link GoodManga} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code GoodManga} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "goodManga/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public GoodManga get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting GoodManga with ID: " + id);
        GoodManga goodManga = ofy().load().type(GoodManga.class).id(id).now();
        if (goodManga == null) {
            throw new NotFoundException("Could not find GoodManga with ID: " + id);
        }
        return goodManga;
    }

    /**
     * Inserts a new {@code GoodManga}.
     */
    @ApiMethod(
            name = "insert",
            path = "goodManga",
            httpMethod = ApiMethod.HttpMethod.POST)
    public GoodManga insert(GoodManga goodManga) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that goodManga.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(goodManga).now();

        IndexSpec indexSpec = IndexSpec.newBuilder().setName("GoodManga").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        Blob b = goodManga.getCover();
        String cover = Base64.encodeBase64String(b.getBytes());
        String status;
        boolean isComplete = goodManga.getStatus();
        if(isComplete){
            status = "Completed";
        }else{
            status = "Ongoing";
        }
        Document document = Document.newBuilder()
                .addField(Field.newBuilder().setName("title").setText(goodManga.getTitle()))
                .addField(Field.newBuilder().setName("href").setText(goodManga.getHref()))
                .addField(Field.newBuilder().setName("cover").setText(cover))
                .addField(Field.newBuilder().setName("genres").setText(goodManga.getGenres()))
                .addField(Field.newBuilder().setName("author").setText(goodManga.getAuthor()))
                .addField(Field.newBuilder().setName("artist").setText(goodManga.getArtist()))
                .addField(Field.newBuilder().setName("status").setAtom(status))
                .addField(Field.newBuilder().setName("rank").setNumber(goodManga.getRank())).build();
        index.put(document);

        logger.info("Created GoodManga with ID: " + goodManga.getId());

        /* while (true) {
            List<String> docIds = new ArrayList<>();
            // Return a set of doc_ids.
            GetRequest request = GetRequest.newBuilder().setReturningIdsOnly(true).build();
            GetResponse<Document> response = index.getRange(request);
            if (response.getResults().isEmpty()) {
                break;
            }
            for (Document doc : response) {
                docIds.add(doc.getId());
            }
            index.delete(docIds);
        } */

        return goodManga;
        //return ofy().load().entity(goodManga).now();
    }

    /**
     * Updates an existing {@code GoodManga}.
     *
     * @param id        the ID of the entity to be updated
     * @param goodManga the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code GoodManga}
     */
    @ApiMethod(
            name = "update",
            path = "goodManga/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public GoodManga update(@Named("id") Long id, GoodManga goodManga) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(goodManga).now();
        logger.info("Updated GoodManga: " + goodManga);
        return ofy().load().entity(goodManga).now();
    }

    /**
     * Deletes the specified {@code GoodManga}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code GoodManga}
     */
    @ApiMethod(
            name = "remove",
            path = "goodManga/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(GoodManga.class).id(id).now();
        logger.info("Deleted GoodManga with ID: " + id);
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
            path = "goodManga",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<GoodManga> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<GoodManga> query = ofy().load().type(GoodManga.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<GoodManga> queryIterator = query.iterator();
        List<GoodManga> goodMangaList = new ArrayList<GoodManga>(limit);
        while (queryIterator.hasNext()) {
            goodMangaList.add(queryIterator.next());
        }
        return CollectionResponse.<GoodManga>builder().setItems(goodMangaList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "search",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<GoodManga> search(@Nullable @Named("searchString") String searchString,
                                                @Nullable @Named("genrelist") String genrelist,
                                                @Nullable @Named("sort") String sort,
                                                @Nullable @Named("cursor") String cursor,
                                                @Nullable @Named("completion") String completion){

        IndexSpec indexSpec = IndexSpec.newBuilder().setName("GoodManga").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
        String searchQuery = "";

        if(searchString != null && !searchString.trim().equals("")){
            searchQuery += "title = " + searchString;
        }

        if(genrelist != null && !genrelist.equals("")){
            String[] genres = genrelist.split(" ");
            for(String s : genres){
                if(!searchQuery.equals("")){
                    searchQuery += " AND ";
                }
                searchQuery += "genres = " + s;
            }
        }

        if(completion != null) {
            if (completion.equals("completed")) {
                if (!searchQuery.equals("")) {
                    searchQuery += " AND ";
                }
                searchQuery += "status = Completed";
            }

            if (completion.equals("ongoing")) {
                if (!searchQuery.equals("")) {
                    searchQuery += " AND ";
                }
                searchQuery += "status = Ongoing";
            }
        }

        SortOptions sortOptions = SortOptions.newBuilder()
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression("rank")
                        .setDirection(SortExpression.SortDirection.DESCENDING))
                .build();
        if(sort != null && !sort.equals("")){
            if(sort.equals("title")) {
                sortOptions = SortOptions.newBuilder()
                        .addSortExpression(SortExpression.newBuilder()
                                .setExpression("title")
                                .setDirection(SortExpression.SortDirection.ASCENDING))
                        .build();
            }

            if(sort.equals("rank")){
            }
        }
        QueryOptions options;

        if(cursor != null && !cursor.equals("")){
            options = QueryOptions.newBuilder().setSortOptions(sortOptions).setCursor(com.google.appengine.api.search.Cursor.newBuilder().setPerResult(true).build(cursor)).setLimit(20).build();
        }else {
            options = QueryOptions.newBuilder().setSortOptions(sortOptions).setCursor(com.google.appengine.api.search.Cursor.newBuilder().setPerResult(true)).setLimit(20).build();
        }
        com.google.appengine.api.search.Query query = com.google.appengine.api.search.Query.newBuilder().setOptions(options).build(searchQuery);
        Future<Results<ScoredDocument>> resultsFuture = index.searchAsync(query);
        Results<ScoredDocument> results = null;
        try {
            results = resultsFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        String nextPageToken = null;
        int i = 0;
        ArrayList<GoodManga> goodMangas = new ArrayList<>();

        for(ScoredDocument r : results){
            String title = r.getOnlyField("title").getText();
            String href = r.getOnlyField("href").getText();

            String coverstring = r.getOnlyField("cover").getText();
            byte[] cover = Base64.decodeBase64(coverstring);

            String genres = r.getOnlyField("genres").getText();
            String author = r.getOnlyField("author").getText();
            String artist = r.getOnlyField("artist").getText();

            String status = r.getOnlyField("status").getAtom();
            boolean isComplete;
            if(status.equals("Completed")){
                isComplete = true;
            }else{
                isComplete = false;
            }

            int rank = r.getOnlyField("rank").getNumber().intValue();


            GoodManga goodManga = new GoodManga();
            goodManga.setTitle(title);
            goodManga.setHref(href);
            goodManga.setCover(cover);
            goodManga.setGenres(genres);
            goodManga.setAuthor(author);
            goodManga.setArtist(artist);
            goodManga.setStatus(isComplete);
            goodManga.setRank(rank);
            goodMangas.add(i++, goodManga);

            nextPageToken = r.getCursor().toWebSafeString();
        }

        return CollectionResponse.<GoodManga>builder().setItems(goodMangas).setNextPageToken(nextPageToken).build();
    }

    @ApiMethod(
            name = "searchDelete",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void searchDelete(@Nullable @Named("keyword") String keyword) {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName("GoodManga").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        String searchQuery = "";
        ArrayList<String> licensedids = new ArrayList<>();
        String replaced = keyword.replaceAll(",|=|[(]|[)]|\"|:", " ");
        if (replaced.trim().equals("")) {
            return;
        }
        searchQuery = "title = " + replaced;
        com.google.appengine.api.search.Query query = com.google.appengine.api.search.Query.newBuilder().build(searchQuery);
        Results<ScoredDocument> results = index.search(query);
        for (ScoredDocument r : results) {
            String title = r.getOnlyField("title").getText();
            if(title.startsWith(keyword)) {
                licensedids.add(r.getId());
            }
        }
        index.delete(licensedids);
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(GoodManga.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find GoodManga with ID: " + id);
        }
    }
}