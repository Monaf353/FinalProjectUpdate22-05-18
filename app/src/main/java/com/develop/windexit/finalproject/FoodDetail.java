package com.develop.windexit.finalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Database.Database;
import com.develop.windexit.finalproject.Model.Food;
import com.develop.windexit.finalproject.Model.Order;
import com.develop.windexit.finalproject.Model.Rating;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shaishavgandhi.loginbuttons.FacebookButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name_detail, food_price_detail, food_description_detail, food_available;
    ImageView food_imgae_detail;

    FloatingActionButton btnRating;
    CounterFab btnCart;

    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    FacebookButton btnShare;

    String foodId = "";
    FirebaseDatabase database;
    DatabaseReference foods;

    DatabaseReference ratingTabl;
    Food currentFood;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    //Create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo from bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)) {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

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


        setContentView(R.layout.activity_food_detail);


        //Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingTabl = database.getReference("Rating");

        //init view
        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btnShare = findViewById(R.id.btnShare);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picasso.with(getBaseContext())
                        .load(currentFood.getImage())
                        .into(target);
            }
        });
        btnRating = (FloatingActionButton) findViewById(R.id.btnRating);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentFood.getAvailable().equals("0")) {

                /*
                boolean isExists = new Database(getBaseContext()).checkFoodExists(foodId,Common.currentUser.getPhone());
                if(!isExists) {*/

                    new Database(getBaseContext()).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            foodId,
                            currentFood.getName(),
                            numberButton.getNumber(),
                            currentFood.getPrice(),
                            currentFood.getDiscount(),
                            currentFood.getImage()

                    ));

                /*}else {
                    new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(),foodId);
                }*/

                    Toast.makeText(FoodDetail.this, "Added to cart", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(FoodDetail.this, "Sorry right now this product is not available!", Toast.LENGTH_LONG).show();
                }

            }
        });
        btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        food_description_detail = findViewById(R.id.food_description_detail);
        food_name_detail = findViewById(R.id.food_name_detail);
        food_imgae_detail = findViewById(R.id.img_food);
        food_price_detail = findViewById(R.id.food_price_detail);
        food_available = findViewById(R.id.food_available);

       /* collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);*/


        //Get Food Id from Intent
        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("FoodId");
        }
        if (!foodId.isEmpty() && foodId != null) {

            if (Common.isConnectedToINternet(getBaseContext())) {
                getDetailFood(foodId);
                getRatingFood(foodId);
            } else {
                Toast.makeText(FoodDetail.this, "please check your internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
    }

    private void getRatingFood(String foodId) {

        Query foodRating = ratingTabl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Rating item = postSnapshot.getValue(Rating.class);
                    if (item != null) {
                        sum += Integer.parseInt(item.getRateValue());
                    }
                    count++;
                }

                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not Good", "Quite Ok", "Very Good", "Excellent"))
                .setDefaultRating(1)  // .setDefaultComment("This product is pretty cool !")
                .setTitle("Rate this Product")
                .setStarColor(R.color.colorAccent)
                .setNoteDescriptionTextColor(R.color.colorPrimaryDark)
                .setDescription("Please select stars and give your feedback")
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.black)
                .setHint("Please write your comment here....")
                .setHintTextColor(R.color.white)
                .setCommentTextColor(R.color.white)
                .setCommentBackgroundColor(R.color.colorAccent) //R.color.colorPrimary
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(final String foodId) {

        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);
                //Set Image
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_imgae_detail);

                // collapsingToolbarLayout.setTitle(currentFood.getName());
                food_name_detail.setText(currentFood.getName());
                food_price_detail.setText("৳ " + currentFood.getPrice());
                food_description_detail.setText("Description : " + currentFood.getDescription());
                food_available.setText(Common.convertCodeToAvailAbility(currentFood.getAvailable()));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        //Get Rating and upload to Firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);

        ratingTabl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()) {

                    //remove old value  ( you can delete or useless function
                    ratingTabl.child(Common.currentUser.getPhone()).removeValue();
                    //Update new value
                    ratingTabl.child(Common.currentUser.getPhone()).setValue(rating);
                } else {
                    ratingTabl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(FoodDetail.this, "Thank you for submit rating !!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }
}
