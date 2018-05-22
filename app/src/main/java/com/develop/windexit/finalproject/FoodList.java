package com.develop.windexit.finalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Database.Database;
import com.develop.windexit.finalproject.Interface.ItemCliclListener;
import com.develop.windexit.finalproject.Model.Favorites;
import com.develop.windexit.finalproject.Model.Food;
import com.develop.windexit.finalproject.Model.Order;
import com.develop.windexit.finalproject.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference foodList;

    RecyclerView recycler_food;

    RecyclerView.LayoutManager layoutManager;
    String categoryId = "";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDB;


    SwipeRefreshLayout swipeRefreshLayout;



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

        setContentView(R.layout.activity_food_list);

        /* toolbar =  findViewById(R.id.sylBack);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setTitle("Product");
        setTitleColor(Color.WHITE);
        // toolbar.setTitleTextColor(Color.WHITE);

        //setTitleColor();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Get Intent here
        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");

        //load menu
        recycler_food = findViewById(R.id.recycler_food);
        // recycler_food.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_food.getContext(), R.anim.layout_from_right);
        recycler_food.setLayoutAnimation(controller);

        //view
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!categoryId.isEmpty() && categoryId != null) {

                    if (Common.isConnectedToINternet(getBaseContext())) {
                        loadListFood(categoryId);
                    } else {
                        Toast.makeText(FoodList.this, "please check your internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            }
        });

        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (!categoryId.isEmpty() && categoryId != null) {

                    if (Common.isConnectedToINternet(getBaseContext())) {
                        loadListFood(categoryId);
                    } else {
                        Toast.makeText(FoodList.this, "please check your internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }


                //Because search
                //Search
                materialSearchBar = findViewById(R.id.searchBar);
                materialSearchBar.setHint("Enter your item");
                //materialSerch.setSpeechMode(false); no need , bcz we already define it at XML
                loadSuggest();
                materialSearchBar.setLastSuggestions(suggestList);
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // when user type text , we will change suggest list
                        List<String> suggest = new ArrayList<String>();
                        for (String search : suggestList) {
                            if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //When search bar is close
                        //Restore Orginal adapter
                        if (!enabled)
                            recycler_food.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        //When search finish
                        //Show result of search adapter
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });


        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        //Local DB
        localDB = new Database(this);



       /* //load menu
        recyclerView = findViewById(R.id.recycler_search);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(), R.anim.layout_from_left);
        recyclerView.setLayoutAnimation(controller);*/

       /* recycler_food.setLayoutManager(new GridLayoutManager(this, 2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_food.getContext(),
                R.anim.layout_fall_down);
        recycler_food.setLayoutAnimation(controller);*/
    }


    private void startSearch(CharSequence text) {
        //Query searchByName =  foodList.orderByChild("name").equalTo(text.toString());
        //create option
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class, R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("name").equalTo(text.toString())) {
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.food_image);

                final Food local = model;

                viewHolder.setItemCliclListener(new ItemCliclListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(FoodList.this,""+local.getName(),Toast.LENGTH_SHORT).show();
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);

                    }
                });
            }

        };
        recycler_food.setAdapter(searchAdapter);
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName());//Add name of food to suggest list

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class, R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.available.setText(Common.convertCodeToAvailAbility(model.getAvailable()));
                viewHolder.food_price.setText(String.format("৳ %s", model.getPrice().toString()));
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.food_image);

                //Quick cart
                viewHolder.btn_quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(model.getAvailable().equals("0")) {

                            boolean isExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(),
                                    Common.currentUser.getPhone());
                            if (!isExists) {

                                new Database(getBaseContext()).addToCart(new Order(
                                        Common.currentUser.getPhone(),
                                        adapter.getRef(position).getKey(),
                                        model.getName(),
                                        "1",
                                        model.getPrice(),
                                        model.getDiscount(),
                                        model.getImage()
                                ));
                            } else {
                                new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(position).getKey());
                            }
                            Toast.makeText(FoodList.this, "Added to cart", Toast.LENGTH_LONG).show();


                        }else {
                            Toast.makeText(FoodList.this, "This product is not available!", Toast.LENGTH_LONG).show();
                        }



                    }
                });


                //Add Favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

               /* //Click to share
                viewHolder.btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getBaseContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });*/
                // Click to change state of Favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef(position).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodPrice(model.getPrice());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());
                        favorites.setAvailable(model.getAvailable());
                        favorites.setUserPhone(Common.currentUser.getPhone());


                        if (!localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                            localDB.addToFavorites(favorites);
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, "" + model.getName() + " was added to Favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, "" + model.getName() + " was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Food local = model;

                viewHolder.setItemCliclListener(new ItemCliclListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(FoodList.this,""+local.getName(),Toast.LENGTH_SHORT).show();
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(foodDetail);

                    }
                });

            }

        };
        recycler_food.setAdapter(adapter);
        recycler_food.getAdapter().notifyDataSetChanged();
        recycler_food.scheduleLayoutAnimation();
        swipeRefreshLayout.setRefreshing(false);

    }


}









/*
 Query query =  foodList.orderByChild("menuId").equalTo(categoryId);
        //create option
        FirebaseRecyclerOptions <Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query, Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FavoritesAdapter>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FavoritesAdapter viewHolder, final int position, @NonNull final Food model) {



* */