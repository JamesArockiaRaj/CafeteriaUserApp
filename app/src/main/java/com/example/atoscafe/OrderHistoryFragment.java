package com.example.atoscafe;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class OrderHistoryFragment extends Fragment {
    private RelativeLayout ordersRL;
    private RecyclerView ordersRv;
    private ArrayList<ModelOrders> ordersList;
    private AdapterOrders adapterOrders;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history,container,false);

        ordersRL = (RelativeLayout) root.findViewById(R.id.ordersRL);
        ordersRv = (RecyclerView) root.findViewById(R.id.ordersRv);

        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();

        loadAllOrders();


        return root;
    }
    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user==null){
            Intent intent = new Intent(getActivity(),LoginActivity.class);
            startActivity(intent);
        } else {
            loadmyInfo();
        }
    }

    private void loadmyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("Username").getValue();

                            //Set User data

//                            loadAllShops();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

//    private void loadAllOrders() {
//        //init order list
//        ordersList = new ArrayList<>();
//
//        //get shop UIDs
//        DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference("Sellers");
//        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    String shopUid = ds.getKey();
////                    System.out.println(shopUid);
//                    loadOrdersForShop(shopUid);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                //handle error
//            }
//        });
//    }
//
//
//    private void loadOrdersForShop(String shopUid) {
//        //init order list
//        ordersList = new ArrayList<>();
//
//        //get orders
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//        reference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ordersList.clear();
//                for (DataSnapshot ds: snapshot.getChildren()){
//                    String uid = ""+ds.getRef().getKey();
//                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers").child(shopUid).child("Orders");
//                    reference.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
//                            .addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
////                                    System.out.println(firebaseAuth.getUid());
//                                    if (snapshot.exists()){
//                                        for (DataSnapshot ds: snapshot.getChildren()){
//                                            ModelOrders modelOrders =ds.getValue(ModelOrders.class);
//                                            //add to list
//                                            ordersList.add(modelOrders);
//                                        }
//                                        //setup adapter
//                                        adapterOrders = new AdapterOrders(getActivity(),ordersList);
//                                        //set to recycler view
//                                        ordersRv.setAdapter(adapterOrders);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void loadOrdersForShop(String shopUid) {
        //init order list
        ordersList = new ArrayList<>();

        //get orders
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers").child(shopUid).child("Orders");
        reference.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot ds: snapshot.getChildren()){
                                ModelOrders modelOrders =ds.getValue(ModelOrders.class);
                                //add to list
                                ordersList.add(modelOrders);
                            }
                            //setup adapter
                            adapterOrders = new AdapterOrders(getActivity(),ordersList);
                            //set to recycler view
                            ordersRv.setAdapter(adapterOrders);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllOrders() {
        //get shop UIDs
        DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference("Sellers");
        shopRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String shopUid = ds.getKey();
                    loadOrdersForShop(shopUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle error
            }
        });
    }

}