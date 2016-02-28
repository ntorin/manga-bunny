package com.fruits.ntorin.mango;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class PickSite extends ListActivity {

    ArrayList<String> siteList;
    ArrayAdapter<String> aAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_site);

        siteList = new ArrayList<String>();
        siteList.add("Mangafox");
        siteList.add("MangaHere");
        siteList.add("Batoto");

        aAdapter = new ArrayAdapter<String>(this, R.layout.activity_pick_site, R.id.site_list_content, siteList);

        setListAdapter(aAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, SiteSearch.class);
        startActivity(intent);
    }
}
