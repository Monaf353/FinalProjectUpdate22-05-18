package com.develop.windexit.finalproject.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Database.Database;
import com.develop.windexit.finalproject.FoodDetail;
import com.develop.windexit.finalproject.Interface.ItemCliclListener;
import com.develop.windexit.finalproject.Model.Favorites;
import com.develop.windexit.finalproject.Model.Order;
import com.develop.windexit.finalproject.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FovoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {

    private Context context;
    private List<Favorites> favoritesList = new ArrayList<>();


    public FovoritesAdapter(Context context, List<Favorites> favoritesList) {
        this.context = context;
        this.favoritesList = favoritesList;
    }

    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.favotites_item,parent,false);
        return new FavoritesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FavoritesViewHolder viewHolder, final int position) {
        viewHolder.food_name.setText(favoritesList.get(position).getFoodName());
        viewHolder.available.setText(Common.convertCodeToAvailAbility(favoritesList.get(position).getAvailable()));
        viewHolder.food_price.setText(String.format("৳ %s", favoritesList.get(position).getFoodPrice()));
        Picasso.with(context)
                .load(favoritesList.get(position).getFoodImage())
                .into(viewHolder.food_image);

        //Quick cart
        viewHolder.btn_quick_cart.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v){

               if(favoritesList.get(position).getAvailable().equals("0"))
               {
                   boolean isExists = new Database(context)
                           .checkFoodExists(favoritesList.get(position).getFoodId(), Common.currentUser.getPhone());
                   if (!isExists) {
                       new Database(context).addToCart(new Order(
                               Common.currentUser.getPhone(),
                               favoritesList.get(position).getFoodId(),
                               favoritesList.get(position).getFoodName(),
                               "1",
                               favoritesList.get(position).getFoodPrice(),
                               favoritesList.get(position).getFoodDiscount(),
                               favoritesList.get(position).getFoodImage()

                       ));
                   } else {
                       new Database(context).increaseCart(Common.currentUser.getPhone(),
                               favoritesList.get(position).getFoodId());
                   }
                   Toast.makeText(context, "Added to cart", Toast.LENGTH_LONG).show();

               } else {
                   Toast.makeText(context, "Sorry right now this product is not available!", Toast.LENGTH_LONG).show();
               }




            }


        });

        final Favorites local = favoritesList.get(position);
        viewHolder.setItemCliclListener(new ItemCliclListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                //Toast.makeText(FoodList.this,""+local.getName(),Toast.LENGTH_SHORT).show();
                Intent foodDetail = new Intent(context, FoodDetail.class);
                foodDetail.putExtra("FoodId", favoritesList.get(position).getFoodId());
                context.startActivity(foodDetail);

            }
        });
    }


    @Override
    public int getItemCount() {
        return favoritesList.size();
    }



}
