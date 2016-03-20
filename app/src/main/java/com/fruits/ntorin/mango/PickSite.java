package com.fruits.ntorin.mango;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class PickSite extends ListActivity {

    ArrayList<String> siteList;
    ArrayAdapter<String> aAdapter;
    HashMap<String, String>siteLinks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_site);

        siteList = new ArrayList<String>();
        siteList.add(DirectoryContract.DirectoryEntry.MANGAFOX_TABLE_NAME);
        siteList.add(DirectoryContract.DirectoryEntry.MANGAHERE_TABLE_NAME);
        siteList.add(DirectoryContract.DirectoryEntry.BATOTO_TABLE_NAME);




        aAdapter = new ArrayAdapter<String>(this, R.layout.activity_pick_site, R.id.site_list_content, siteList);

        setListAdapter(aAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, SiteSearch.class);
        Bundle bundle = new Bundle();
        bundle.putString("site", (String) l.getItemAtPosition(position));
        intent.putExtras(bundle);
        Log.d("t", (String) l.getItemAtPosition(position));
        startActivity(intent);
    }
}
