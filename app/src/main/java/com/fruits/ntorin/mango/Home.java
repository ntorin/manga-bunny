package com.fruits.ntorin.mango;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

/*
Starting Date: January 29, 2016
 */
public class Home extends AppCompatActivity /*implements FavoritesList.OnFragmentInteractionListener*/ {

    //SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //db = new DirectoryDbHelper(this).getWritableDatabase();

        //DirectorySetup.MangafoxSetup(db);

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            //FavoritesList favoritesList = new FavoritesList();
            //favoritesList.setArguments(getIntent().getExtras());

            //getSupportFragmentManager().beginTransaction()
            //        .add(R.id.fragment_container, favoritesList).commit();


        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AppHome.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;

            case R.id.action_search_settings:
                intent = new Intent(this, DescriptionChapters.class);
                startActivity(intent);
                return true;

            case R.id.list_view:
                //FavoritesList favoritesList = new FavoritesList();
                android.support.v4.app.FragmentTransaction transaction =
                        getSupportFragmentManager().beginTransaction();
                //transaction.replace(R.id.fragment_container, favoritesList);
                return true;

            case R.id.catalog_view:
                intent = new Intent(this, ChapterReader.class);
                startActivity(intent);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //@Override
    public void onFragmentInteraction(Uri uri) {

    }
}
