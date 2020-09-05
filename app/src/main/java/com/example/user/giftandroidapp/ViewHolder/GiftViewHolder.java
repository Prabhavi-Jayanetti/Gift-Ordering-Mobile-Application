package com.example.user.giftandroidapp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.giftandroidapp.Interface.ItemClickListener;
import com.example.user.giftandroidapp.R;

/**
 * Created by User on 7/23/2018.
 */

public class GiftViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView gift_name;
    public ImageView gift_image,fav_image,share_image;



    private ItemClickListener itemClickListener;


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public GiftViewHolder(View itemView) {
        super(itemView);

        gift_name = (TextView)itemView.findViewById(R.id.gift_name);
        gift_image = (ImageView)itemView.findViewById(R.id.gift_image);
        fav_image = (ImageView)itemView.findViewById(R.id.fav);
        share_image=(ImageView)itemView.findViewById(R.id.btnShare);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
}
