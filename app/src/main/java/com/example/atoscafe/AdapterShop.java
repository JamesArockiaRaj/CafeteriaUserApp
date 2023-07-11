package com.example.atoscafe;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop>{

    private Context context;
    public ArrayList<ModelShop> shopList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopList) {
        this.context = context;
        this.shopList = shopList;

    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate layout row_shop.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //Get Data
        ModelShop modelShop = shopList.get(position);
        String uid = modelShop.getUid();
        String email = modelShop.getEmail();
        String shopName = modelShop.getShopName();
        String location = modelShop.getLocation();
        String description = modelShop.getDescription();
        String shopIcon = modelShop.getShopIcon();
        String timestamp = modelShop.getTimestamp();

        loadReviews(modelShop,holder);


        //Set Data
        holder.shopname.setText(shopName);
        holder.shopdesc.setText(description);
        holder.location.setText(location);
        try{
            Picasso.get().load(shopIcon).placeholder(R.drawable.ic_baseline_store_24).into(holder.shopIv);
        } catch (Exception e){
            holder.shopIv.setImageResource(R.drawable.ic_baseline_store_24);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });

    }


    private float ratingSum = 0;
    private void loadReviews(ModelShop modelShop, HolderShop holder) {

        String shopUid = modelShop.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ratingSum = 0;
                        for (DataSnapshot ds:snapshot.getChildren()) {
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum+rating; //Average rating


                        }


                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }



    //View Holder
    class HolderShop extends RecyclerView.ViewHolder{

        //Ui views of row_shop.xml
        private TextView shopname,shopdesc,location;
        private ImageView shopIv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //Init ui views
            shopname = itemView.findViewById(R.id.shoptitle);
            shopdesc = itemView.findViewById(R.id.shopdes);
            shopIv = itemView.findViewById(R.id.shopIv);
            location = itemView.findViewById(R.id.shopLocation);
            ratingBar = itemView.findViewById(R.id.ratingBar);

        }
    }
    public void updateList(ArrayList<ModelShop> filteredList) {
        shopList = filteredList;
        notifyDataSetChanged();
    }
}
