package com.develop.windexit.finalproject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Database.Database;
import com.develop.windexit.finalproject.Model.Favorites;
import com.develop.windexit.finalproject.ViewHolder.FovoritesAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rey.material.widget.RelativeLayout;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FavoritesActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference foodList;

    RecyclerView recycler_favorites;

    RecyclerView.LayoutManager layoutManager;

    FovoritesAdapter adapter;
    RelativeLayout rootLayout;
    private List<Favorites> favoritesList;

    //Favorites
    Database localDB;

    //Roboto_Regular.ttf

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto_Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());


        setContentView(R.layout.activity_favorites);


        getSupportActionBar().setTitle("Favorites");
        setTitleColor(Color.WHITE);
        // toolbar.setTitleTextColor(Color.WHITE);

        //setTitleColor();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //load menu
        recycler_favorites = findViewById(R.id.recycler_favorites);
        layoutManager = new LinearLayoutManager(this);
        recycler_favorites.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_favorites.getContext(), R.anim.layout_from_left);
        recycler_favorites.setLayoutAnimation(controller);

        loadAllFavorites();
    }

    private void loadAllFavorites() {
        favoritesList  = new Database(this).getAllFavorites(Common.currentUser.getPhone());
        adapter = new FovoritesAdapter(this,new Database(this)
                .getAllFavorites(Common.currentUser.getPhone()));
        recycler_favorites.setAdapter(adapter);
        recycler_favorites.getAdapter().notifyDataSetChanged();
        recycler_favorites.scheduleLayoutAnimation();
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteFav(item.getOrder());
        return super.onContextItemSelected(item);
    }

    private void deleteFav(int position) {
        favoritesList.remove(position);
        new Database(this).cleanFavorites(Common.currentUser.getPhone());

        for (Favorites item : favoritesList)
            new Database(this).addToFavorites(item);
        loadAllFavorites();
    }

}
