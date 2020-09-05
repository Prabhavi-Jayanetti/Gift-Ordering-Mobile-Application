package com.example.user.giftandroidapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.user.giftandroidapp.Common.Common;
import com.example.user.giftandroidapp.Database.Database;
import com.example.user.giftandroidapp.Interface.ItemClickListener;
import com.example.user.giftandroidapp.Model.Category;
import com.example.user.giftandroidapp.Model.Gift;
import com.example.user.giftandroidapp.ViewHolder.GiftViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class GiftList extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference giftList;

    String categoryId="";

    FirebaseRecyclerAdapter<Gift,GiftViewHolder> adapter;


    //Search Functionality
    FirebaseRecyclerAdapter<Gift,GiftViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //favorites
    Database localDB;

    //facebook share
    CallbackManager callbackManager;
    ShareDialog shareDialog;


    //create target from Picasso
    Target target=new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //create photo from bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

            if(ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content=new SharePhotoContent.Builder()
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_list);

        //Init facebook
        callbackManager= CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);


        //firebase
        database = FirebaseDatabase.getInstance();
        giftList = database.getReference("Gift");

        //local DB
        localDB = new Database(this);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_gift);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Get Intent here
            if(getIntent() != null)
                categoryId = getIntent().getStringExtra("CategoryId");
            if(!categoryId.isEmpty() && categoryId != null)
            {
                if (Common.isConnectedToInternet(getBaseContext()))
                     loadListGift(categoryId);
                else
                {

                    Toast.makeText(GiftList.this,"Please check your internet connection !!!",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            //Search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your gift");
        //materialSearchBar.setSpeechMode(false); No need, bcz already defined in xml.
        loadSuggest();//write function to load suggest from Firebase
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //When user type suggest list, we will change suggest list
                List<String> suggest = new ArrayList<String>();
                for(String search:suggestList)
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
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
                //when Search bar is close
                //Restore original adapter
                if(!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when search finished
                //show results of search adapter
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearch(CharSequence text) {
        searchAdapter = new FirebaseRecyclerAdapter<Gift, GiftViewHolder>(
                Gift.class,
                R.layout.gift_item,
                GiftViewHolder.class,
                giftList.orderByChild("name").equalTo(text.toString())//compare name

        ) {
            @Override
            protected void populateViewHolder(GiftViewHolder viewHolder, Gift model, int position) {
                viewHolder.gift_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.gift_image);

                final Gift local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //start new activity
                        Intent giftDetail = new Intent(GiftList.this,GiftDetail.class);
                        giftDetail.putExtra("GiftId",searchAdapter.getRef(position).getKey());//send Gift Id to new activity
                        startActivity(giftDetail);
                    }
                });

            }
        };

        recyclerView.setAdapter(searchAdapter); //Set adapter for Recycler view is search result


    }

    private void loadSuggest() {
        giftList.orderByChild("giftCategoryId").equalTo(categoryId).
        addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Gift item = postSnapshot.getValue(Gift.class);
                    suggestList.add(item.getName()); //Add name of gift to suggest list
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadListGift(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Gift, GiftViewHolder>(Gift.class,
                R.layout.gift_item,
                GiftViewHolder.class,
                giftList.orderByChild("giftCategoryId").equalTo(categoryId)//like :select * from gifts where GiftCategoryId =

                ) {
            @Override
            protected void populateViewHolder(final GiftViewHolder viewHolder, final Gift model, final int position) {
                viewHolder.gift_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.gift_image);

                //Add favorites
                if (localDB.isFavorite(adapter.getRef(position).getKey()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);


                //click to share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                //click to change status of favourites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!localDB.isFavorite(adapter.getRef(position).getKey()))
                        {
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(GiftList.this,""+model.getName()+"was added to favourites",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(GiftList.this,""+model.getName()+"was removed from  favourites",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                final Gift local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                       //start new activity
                        Intent giftDetail = new Intent(GiftList.this,GiftDetail.class);
                        giftDetail.putExtra("GiftId",adapter.getRef(position).getKey());//send Gift Id to new activity
                        startActivity(giftDetail);
                    }
                });

            }
        };
        //set adapter
        //Log.d("Tag",""+adapter.getItemCount());
        recyclerView.setAdapter(adapter);
    }
}
