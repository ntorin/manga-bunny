package com.fruits.ntorin.dstore;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.googlecode.objectify.ObjectifyService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

//import com.googlecode.objectify.cmd.Query;

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
                ownerDomain = "dstore.ntorin.fruits.com",
                ownerName = "dstore.ntorin.fruits.com",
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
        //ofy().save().entity(mangaHere).now();

        IndexSpec indexSpec = IndexSpec.newBuilder().setName("MangaHere").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        Blob b = mangaHere.getCover();
        String cover = Base64.encodeBase64String(b.getBytes());
        String status;
        boolean isComplete = mangaHere.getStatus();
        if (isComplete) {
            status = "Completed";
        } else {
            status = "Ongoing";
        }
        Document document = Document.newBuilder()
                .addField(Field.newBuilder().setName("title").setText(mangaHere.getTitle()))
                .addField(Field.newBuilder().setName("href").setText(mangaHere.getHref()))
                .addField(Field.newBuilder().setName("cover").setText(cover))
                .addField(Field.newBuilder().setName("genres").setText(mangaHere.getGenres()))
                .addField(Field.newBuilder().setName("author").setText(mangaHere.getAuthor()))
                .addField(Field.newBuilder().setName("artist").setText(mangaHere.getArtist()))
                .addField(Field.newBuilder().setName("status").setAtom(status))
                .addField(Field.newBuilder().setName("rank").setNumber(mangaHere.getRank())).build();
            index.put(document);

        logger.info("Created MangaHere with ID: " + mangaHere.getId());

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


        return mangaHere;
        //return ofy().load().entity(mangaHere).now();
    }

    @ApiMethod(
            name = "searchDelete",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void searchDelete(@Nullable @Named("keyword") String keyword) {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName("MangaHere").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        String searchQuery = "";
        ArrayList<String> licensedids = new ArrayList<>();
        String replaced = keyword.replaceAll(",|=|[(]|[)]|\"|:", " ");
        if (replaced.trim().equals("")) {
            return;
        }
        searchQuery = "title = " + replaced;
        Query query = Query.newBuilder().build(searchQuery);
        Results<ScoredDocument> results = index.search(query);
        for (ScoredDocument r : results) {
            String title = r.getOnlyField("title").getText();
            if(title.startsWith(keyword)) {
                licensedids.add(r.getId());
            }
        }
        index.delete(licensedids);
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
        /*DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("MangaHere").setKeysOnly();
        List<Entity> results = datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());
        for(Entity e : results){
            try {
                Entity mangaHere = datastoreService.get(e.getKey());
            } catch (EntityNotFoundException e1) {
                e1.printStackTrace();
            }
        }*/
        //return results;
        // TODO: 6/28/2016 set Query filters/sorts here
        com.googlecode.objectify.cmd.Query<MangaHere> query = ofy().load().type(MangaHere.class).limit(limit).order("title");
        if (cursor != null) {
            query = query.startAt(com.google.appengine.api.datastore.Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<MangaHere> queryIterator = query.iterator();
        List<MangaHere> mangaHereList = new ArrayList<MangaHere>(limit);
        while (queryIterator.hasNext()) {
            mangaHereList.add(queryIterator.next());
        }
        //return null;
        return CollectionResponse.<MangaHere>builder().setItems(mangaHereList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }


    @ApiMethod(
            name = "search",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<MangaHere> search(@Nullable @Named("searchString") String searchString, @Nullable @Named("genrelist") String genrelist, @Nullable @Named("sort") String sort, @Nullable @Named("cursor") String cursor, @Nullable @Named("completion") String completion) {
        final Logger log = Logger.getLogger(MangaHereEndpoint.class.getName());

        IndexSpec indexSpec = IndexSpec.newBuilder().setName("MangaHere").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
        String searchQuery = "";

        if (searchString != null && !searchString.trim().equals("")) {
            searchQuery += "title = " + searchString + " OR author = " + searchString + " OR artist = " + searchString;
        }

        if (genrelist != null && !genrelist.equals("")) {
            String[] genres = genrelist.split(" ");
            for (String s : genres) {
                if (!searchQuery.equals("")) {
                    searchQuery += " AND ";
                }
                searchQuery += "genres = " + s;
            }
        }

        if (completion != null) {
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
                        .setDirection(SortExpression.SortDirection.ASCENDING))
                .build();
        if (sort != null && !sort.equals("")) {
            if (sort.equals("title")) {
                sortOptions = SortOptions.newBuilder()
                        .addSortExpression(SortExpression.newBuilder()
                                .setExpression("title")
                                .setDirection(SortExpression.SortDirection.ASCENDING))
                        .build();
            }

            if (sort.equals("rank")) {
            }
        }
        QueryOptions options;

        if (cursor != null && !cursor.equals("")) {
            options = QueryOptions.newBuilder().setSortOptions(sortOptions).setCursor(Cursor.newBuilder().setPerResult(true).build(cursor)).setLimit(20).build();
        } else {
            options = QueryOptions.newBuilder().setSortOptions(sortOptions).setCursor(Cursor.newBuilder().setPerResult(true)).setLimit(16).build();
        }
        Query query = Query.newBuilder().setOptions(options).build(searchQuery);
        Future<Results<ScoredDocument>> resultsFuture = index.searchAsync(query);
        Results<ScoredDocument> results = null;
        try {
            results = resultsFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        /*do {
            try {
                results = resultsFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }while(!resultsFuture.isDone());*/
        String nextPageToken = null;
        int i = 0;
        ArrayList<MangaHere> mangaHeres = new ArrayList<>();

        for (ScoredDocument r : results) {
            String title = r.getOnlyField("title").getText();
            String href = r.getOnlyField("href").getText();

            String coverstring = r.getOnlyField("cover").getText();
            byte[] cover = Base64.decodeBase64(coverstring);

            String genres = r.getOnlyField("genres").getText();
            String author = r.getOnlyField("author").getText();
            String artist = r.getOnlyField("artist").getText();

            String status = r.getOnlyField("status").getAtom();
            boolean isComplete;
            if (status.equals("Completed")) {
                isComplete = true;
            } else {
                isComplete = false;
            }

            int rank = r.getOnlyField("rank").getNumber().intValue();


            MangaHere mangaHere = new MangaHere();
            mangaHere.setTitle(title);
            mangaHere.setHref(href);
            mangaHere.setCover(cover);
            mangaHere.setGenres(genres);
            mangaHere.setAuthor(author);
            mangaHere.setArtist(artist);
            mangaHere.setStatus(isComplete);
            mangaHere.setRank(rank);
            mangaHeres.add(i++, mangaHere);

            nextPageToken = r.getCursor().toWebSafeString();
            //log.info("" + mangaHere.toString());
        }

        return CollectionResponse.<MangaHere>builder().setItems(mangaHeres).setNextPageToken(nextPageToken).build();

        //return mangaHeres.toArray(new MangaHere[mangaHeres.size()]);

        /*IndexSpec indexSpec = IndexSpec.newBuilder().setName("MangaHere").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
        String searchQuery = "";

        if(searchString != null && !searchString.equals("")){
            searchQuery += "title = " + searchString;
        }

        if(genrelist != null && !genrelist.equals("")){
            String[] genres = genrelist.split(" ");
            searchQuery += "genres = " + genres[0];
        }

        SortOptions sortOptions = SortOptions.newBuilder()
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression("rank")
                        .setDirection(SortExpression.SortDirection.ASCENDING))
                .build();

        if(sort != null && !sort.equals("")){
            if(sort.equals("title")) {
                 sortOptions = SortOptions.newBuilder()
                        .addSortExpression(SortExpression.newBuilder()
                                .setExpression("title")
                                .setDirection(SortExpression.SortDirection.ASCENDING))
                        .build();
            }

            if(sort.equals("rank")) {
            }
        }
        QueryOptions options;
        if(cursor != null && cursor.equals("")){
            options = QueryOptions.newBuilder().setSortOptions(sortOptions).build();
            //options = QueryOptions.newBuilder().setCursor(com.google.appengine.api.search.Cursor.newBuilder().build(cursor)).build();
        }else {
            options = QueryOptions.newBuilder().setSortOptions(sortOptions).build();
        }
        Query query = Query.newBuilder().setOptions(options).build(searchQuery);
        Results<ScoredDocument> results = index.search(query);
        //Results<ScoredDocument> results = null;
        /*do {
            try {
                results = resultsFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }while(!resultsFuture.isDone());*

        //String nextPageToken = null;
        int i = 0;
        ArrayList<MangaHere> mangaHeres = new ArrayList<>();
        for(ScoredDocument r : results){

            String title = r.getOnlyField("title").getText();
            String href = r.getOnlyField("href").getText();

            String coverstring = r.getOnlyField("cover").getText();
            byte[] cover = Base64.decodeBase64(coverstring);

            String genres = r.getOnlyField("genres").getText();
            String author = r.getOnlyField("author").getText();
            String artist = r.getOnlyField("artist").getText();

            String status = r.getOnlyField("status").getAtom();
            r.getCursor().toWebSafeString();
            boolean isComplete;
            if(status.equals("Completed")){
                isComplete = true;
            }else{
                isComplete = false;
            }

            int rank = r.getOnlyField("rank").getNumber().intValue();


            MangaHere mangaHere = new MangaHere();
            mangaHere.setTitle(title);
            mangaHere.setHref(href);
            mangaHere.setCover(cover);
            mangaHere.setGenres(genres);
            mangaHere.setAuthor(author);
            mangaHere.setArtist(artist);
            mangaHere.setStatus(isComplete);
            mangaHere.setRank(rank);
            mangaHeres.add(i++, mangaHere);
            //if(i == results.getResults().size()){
            //    nextPageToken = r.getCursor().toWebSafeString();
            //}
        }

        CollectionResponse<MangaHere> mangaHereCollection = CollectionResponse.<MangaHere>builder().setItems(mangaHeres)/*.setNextPageToken(nextPageToken)*.build();
        //return  mangaHereCollection;
         return mangaHeres.toArray(new MangaHere[mangaHeres.size()]);*/
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(MangaHere.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find MangaHere with ID: " + id);
        }
    }

    private Index getIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName("search").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
        return index;
    }

}