package com.develop.windexit.finalproject.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Interface.ItemCliclListener;
import com.develop.windexit.finalproject.R;

/**
 * Created by WINDEX IT on 16-Feb-18.
 */

class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener{

    public TextView food_name,food_price,available;

    public ImageView food_image,btn_quick_cart;

    private ItemCliclListener itemCliclListener;

    public FavoritesViewHolder(View itemView) {
        super(itemView);

        food_name =  itemView.findViewById(R.id.food_name);
        food_image =  itemView.findViewById(R.id.food_image);
        food_price = itemView.findViewById(R.id.food_price);
        available = itemView.findViewById(R.id.available);
        btn_quick_cart = itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }


    public void setItemCliclListener(ItemCliclListener itemCliclListener) {
        this.itemCliclListener = itemCliclListener;
    }

    @Override
    public void onClick(View v) {
        itemCliclListener.onClick(v, getAdapterPosition(), false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select action");
        menu.add(0, 0, getAdapterPosition(), Common.DELETE);
    }
}
