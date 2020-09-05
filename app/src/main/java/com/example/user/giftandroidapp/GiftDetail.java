package com.example.user.giftandroidapp;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.user.giftandroidapp.Common.Common;
import com.example.user.giftandroidapp.Database.Database;
import com.example.user.giftandroidapp.Model.Gift;
import com.example.user.giftandroidapp.Model.Order;
import com.example.user.giftandroidapp.Model.Rating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

public class GiftDetail extends AppCompatActivity implements RatingDialogListener{
    TextView gift_name,gift_price,gift_description;
    ImageView gift_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart,btnRating;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    String giftId="";

    FirebaseDatabase database;
    DatabaseReference gifts;
    DatabaseReference ratingTbl;

    Gift currentGift;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_detail);

        //Firebase
        database = FirebaseDatabase.getInstance();
        gifts = database.getReference("Gift");
        ratingTbl = database.getReference("Rating");

        //Init view
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (FloatingActionButton)findViewById(R.id.btnCart);
        btnRating = (FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        giftId,
                        currentGift.getName(),
                        numberButton.getNumber(),
                        currentGift.getPrice(),
                        currentGift.getDiscount()
                ));
                Toast.makeText(GiftDetail.this,"Added to Cart",Toast.LENGTH_SHORT).show();
            }
        });

        gift_description = (TextView)findViewById(R.id.gift_description);
        gift_name = (TextView)findViewById(R.id.gift_name);
        gift_price = (TextView)findViewById(R.id.gift_price);
        gift_image = (ImageView)findViewById(R.id.img_gift);

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);
        
        //Get Gift Id from Intent
        if(getIntent() != null)
            giftId = getIntent().getStringExtra("GiftId");
        if(!giftId.isEmpty()){
            if (Common.isConnectedToInternet(getBaseContext()))
            {
                getDetailGift(giftId);
                getRatingGift(giftId);
            }

            else
            {

                Toast.makeText(GiftDetail.this,"Please check your internet connection !!!",Toast.LENGTH_SHORT).show();
                return;
            }


        }

    }

    private void getRatingGift(String giftId) {
        com.google.firebase.database.Query giftRating = ratingTbl.orderByChild("giftId").equalTo(giftId);

        giftRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if(count !=0)
                {
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
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this gift")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(GiftDetail.this)
                .show();



    }

    private void getDetailGift(String giftId) {
        gifts.child(giftId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentGift = dataSnapshot.getValue(Gift.class);

                //Set Image
                Picasso.with(getBaseContext()).load( currentGift.getImage())
                        .into(gift_image);

                collapsingToolbarLayout.setTitle( currentGift.getName());

                gift_price.setText( currentGift.getPrice());

                gift_name.setText( currentGift.getName());

                gift_description.setText( currentGift.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        //Get Rating and upload to firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                giftId,
                String.valueOf(value),
                comments);

        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    //remove old value
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                else
                {
                    //update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(GiftDetail.this,"Thank you for submit rating !!!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
