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
        name = "mangaInnApi",
        version = "v1",
        resource = "mangaInn",
        namespace = @ApiNamespace(
                ownerDomain = "dstore.ntorin.fruits.com",
                ownerName = "dstore.ntorin.fruits.com",
                packagePath = ""
        )
)
public class MangaInnEndpoint {

    private static final Logger logger = Logger.getLogger(MangaInnEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(MangaInn.class);
    }

    /**
     * Returns the {@link MangaInn} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code MangaInn} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "mangaInn/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public MangaInn get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting MangaInn with ID: " + id);
        MangaInn mangaInn = ofy().load().type(MangaInn.class).id(id).now();
        if (mangaInn == null) {
            throw new NotFoundException("Could not find MangaInn with ID: " + id);
        }
        return mangaInn;
    }

    /**
     * Inserts a new {@code MangaInn}.
     */
    @ApiMethod(
            name = "insert",
            path = "mangaInn",
            httpMethod = ApiMethod.HttpMethod.POST)
    public MangaInn insert(MangaInn mangaInn) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that mangaInn.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        //ofy().save().entity(mangaInn).now();

        IndexSpec indexSpec = IndexSpec.newBuilder().setName("MangaInn").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        Blob b = mangaInn.getCover();
        String cover = Base64.encodeBase64String(b.getBytes());
        String status;
        boolean isComplete = mangaInn.getStatus();
        if(isComplete){
            status = "Completed";
        }else{
            status = "Ongoing";
        }
        Document document = Document.newBuilder()
                .addField(Field.newBuilder().setName("title").setText(mangaInn.getTitle()))
                .addField(Field.newBuilder().setName("href").setText(mangaInn.getHref()))
                .addField(Field.newBuilder().setName("cover").setText(cover))
                .addField(Field.newBuilder().setName("genres").setText(mangaInn.getGenres()))
                .addField(Field.newBuilder().setName("author").setText(mangaInn.getAuthor()))
                .addField(Field.newBuilder().setName("artist").setText(mangaInn.getArtist()))
                .addField(Field.newBuilder().setName("status").setAtom(status))
                .addField(Field.newBuilder().setName("rank").setNumber(mangaInn.getRank())).build();
        index.put(document);

        logger.info("Created MangaInn with ID: " + mangaInn.getId());

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

        return  mangaInn;
        //return ofy().load().entity(mangaInn).now();
    }

    /**
     * Updates an existing {@code MangaInn}.
     *
     * @param id       the ID of the entity to be updated
     * @param mangaInn the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code MangaInn}
     */
    @ApiMethod(
            name = "update",
            path = "mangaInn/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public MangaInn update(@Named("id") Long id, MangaInn mangaInn) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(mangaInn).now();
        logger.info("Updated MangaInn: " + mangaInn);
        return ofy().load().entity(mangaInn).now();
    }

    /**
     * Deletes the specified {@code MangaInn}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code MangaInn}
     */
    @ApiMethod(
            name = "remove",
            path = "mangaInn/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(MangaInn.class).id(id).now();
        logger.info("Deleted MangaInn with ID: " + id);
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
            path = "mangaInn",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<MangaInn> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<MangaInn> query = ofy().load().type(MangaInn.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<MangaInn> queryIterator = query.iterator();
        List<MangaInn> mangaInnList = new ArrayList<MangaInn>(limit);
        while (queryIterator.hasNext()) {
            mangaInnList.add(queryIterator.next());
        }
        return CollectionResponse.<MangaInn>builder().setItems(mangaInnList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "search",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<MangaInn> search(@Nullable @Named("searchString") String searchString,
                                                @Nullable @Named("genrelist") String genrelist,
                                                @Nullable @Named("sort") String sort,
                                                @Nullable @Named("cursor") String cursor,
                                                @Nullable @Named("completion") String completion){

        IndexSpec indexSpec = IndexSpec.newBuilder().setName("MangaInn").build();
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
            options = QueryOptions.newBuilder().setSortOptions(sortOptions).setCursor(com.google.appengine.api.search.Cursor.newBuilder().setPerResult(true)).setLimit(16).build();
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
        ArrayList<MangaInn> mangaInns = new ArrayList<>();

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


            MangaInn mangaInn = new MangaInn();
            mangaInn.setTitle(title);
            mangaInn.setHref(href);
            mangaInn.setCover(cover);
            mangaInn.setGenres(genres);
            mangaInn.setAuthor(author);
            mangaInn.setArtist(artist);
            mangaInn.setStatus(isComplete);
            mangaInn.setRank(rank);
            mangaInns.add(i++, mangaInn);

            nextPageToken = r.getCursor().toWebSafeString();
        }

        return CollectionResponse.<MangaInn>builder().setItems(mangaInns).setNextPageToken(nextPageToken).build();
    }

    @ApiMethod(
            name = "searchDelete",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void searchDelete(@Nullable @Named("keyword") String keyword) {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName("MangaInn").build();
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
            ofy().load().type(MangaInn.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find MangaInn with ID: " + id);
        }
    }
}