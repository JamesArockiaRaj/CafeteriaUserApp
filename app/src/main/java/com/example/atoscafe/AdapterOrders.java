package com.example.atoscafe;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrders extends RecyclerView.Adapter<AdapterOrders.HolderOrderUser>{
    private Context context;
    private ArrayList<ModelOrders> orderUserList;

    public AdapterOrders(Context context, ArrayList<ModelOrders> orderUserList) {

        this.context = context;
        this.orderUserList = orderUserList;
    }


    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Infalte layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {
        //Get Data
        ModelOrders modelOrders = orderUserList.get(position);
        String orderId = modelOrders.getOrderId();
        String orderBy = modelOrders.getOrderBy();
        String orderCost = modelOrders.getOrderCost();
        String orderStatus = modelOrders.getOrderStatus();
        String orderTime = modelOrders.getOrderTime();
        String orderTo = modelOrders.getOrderTo();
//        System.out.println("HI"+orderTo);
        //getting shop info
        loadShopInfo(modelOrders,holder);

        //set data
        holder.amountTv.setText("Amount: ₹"+orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("OrderID: "+orderId);
        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.blue));
        }else if (orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.orange));
        }  else  if (orderStatus.equals("Delivered")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.green));
        } else  if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.red));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy",calendar).toString();
        holder.dateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open OrderDetailsActivity
                Intent intent = new Intent(context,OrderDetailsActivity.class);
                intent.putExtra("orderTo",orderTo);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent);
            }
        });
    }

    private void loadShopInfo(ModelOrders modelOrders, HolderOrderUser holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Sellers");
        ref.child(modelOrders.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String shopLocation = ""+snapshot.child("location").getValue(); // Fetch shop location from DataSnapshot
                        holder.shopName.setText(shopName+", "+shopLocation);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    //view holder class
    class HolderOrderUser extends RecyclerView.ViewHolder{

        //views of layout
        private TextView orderIdTv,dateTv,shopName,amountTv,statusTv;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            //init views of layout
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            shopName = itemView.findViewById(R.id.shopName);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }
}
