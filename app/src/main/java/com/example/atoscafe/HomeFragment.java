package com.example.atoscafe;

import static com.example.atoscafe.LoginActivity.Username;
import static com.example.atoscafe.MainActivity.fileName;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;


public class HomeFragment extends Fragment {

    int hour = Calendar.getInstance().getTime().getHours();
    private SharedPreferences sharedPreferences;
    private TextView nameTv;
    private TextView tvGreeting,location;
    private Intent intent;
    private RelativeLayout shopsRL;
    private RecyclerView shopsRv;

    private Spinner locationSpinner;

//    private ArrayList<ModelShop> shopList ;
    private ArrayList<ModelShop> filteredShops ;

    private AdapterShop adapterShop;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String selectedLocation = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = this.getActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        nameTv = (TextView) root.findViewById(R.id.nametv);
        shopsRL = (RelativeLayout) root.findViewById(R.id.shopsRL);
        shopsRv = (RecyclerView) root.findViewById(R.id.shopsRv);
        locationSpinner = (Spinner) root.findViewById(R.id.locationSpinner);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.location_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        // Set OnItemSelectedListener to locationSpinner
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected location from the spinner
                selectedLocation = parent.getItemAtPosition(position).toString();
//                System.out.println(selectedLocation);
                // Filter the shopList based on the selected location
                filteredShops = new ArrayList<>();

                //Get All Shops
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
                Query query = reference.orderByChild("location").equalTo(selectedLocation);
                    query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                filteredShops.clear();
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    ModelShop modelShop = ds.getValue(ModelShop.class);
                                    filteredShops.add(modelShop);
                                }
                                //progressDialog.dismiss();
                                //Setup adapter
                                adapterShop = new AdapterShop(getActivity(),filteredShops);
                                //Set Adapter
                                shopsRv.setAdapter(adapterShop);
                                tvGreeting = (TextView) root.findViewById(R.id.greet);

                                String Timing = null;

                                if (hour >= 12 && hour < 14) {
                                    Timing="Lunch";
                                } else if (hour >= 14 && hour < 19) {
                                    Timing="Snacks";
                                } else if (hour >= 19 && hour < 24) {
                                    Timing="Dinner";
                                }
                                else {
                                    Timing="Breakfast";
                                }
                                tvGreeting.setText("Have you not had your " +Timing+ " yet? Have it now!");
                                locationSpinner.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                            nameTv.setText("Hello "+name);
                            System.out.println("Nammeeeee"+name);

//                            loadAllShops();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

//    private void loadAllShops() {
//        shopList = new ArrayList<>();
//
//        //Get All Products
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
//        reference.orderByChild("uid")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        shopList.clear();
//                        for(DataSnapshot ds: dataSnapshot.getChildren()){
//                            ModelShop modelShop = ds.getValue(ModelShop.class);
//                            shopList.add(modelShop);
//                            progressDialog.dismiss();
//                        }
//                        //Setup adapter
//                        adapterShop = new AdapterShop(getActivity(),shopList);
//                        //Set Adapter
//                        shopsRv.setAdapter(adapterShop);
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }

}