/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.fruits.ntorin.dstore;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.taskqueue.TaskQueuePb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Named;

/** An endpoint class we are exposing */
@Api(
  name = "myApi",
  version = "v1",
  namespace = @ApiNamespace(
    ownerDomain = "dstore.ntorin.fruits.com",
    ownerName = "dstore.ntorin.fruits.com",
    packagePath=""
  )
)
public class MyEndpoint {

    /** A simple endpoint method that takes a name and says Hi back */
    @ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) {
        MyBean response = new MyBean();

        IndexSpec indexSpec = IndexSpec.newBuilder().setName("MangaHere").build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
        String searchQuery = "";
        ArrayList<String> licensedids = new ArrayList<>();
        File f = new File("WEB-INF/licensed.txt");
        f.getName();
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while((line = br.readLine()) != null){
                String replaced = line.replaceAll(",|=|[(]|[)]|\"|:", " ");
                if(replaced.trim().equals("")){
                    continue;
                }
                searchQuery = "title = " + replaced;
                Query query = Query.newBuilder().build(searchQuery);
                Results<ScoredDocument> results = index.search(query);
                for(ScoredDocument r : results){
                    licensedids.add(r.getId());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        index.delete(licensedids);

        response.setData("Hi, " + name);

        return response;
    }

}
