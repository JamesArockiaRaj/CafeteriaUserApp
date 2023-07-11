

package com.example.atoscafe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity implements PaymentResultListener {
    private static final String TAG = ShopDetailsActivity.class.getSimpleName();
    //Declare UI views
    private ImageView shopIv;
    private TextView shopNameTv, shopDesc, filteredProductsTv, cartCountTv;
    private ImageButton backBtn,cartBtn, filterProductBtn, reviewsBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;

    private String shopUid;
    private String shopName, shopDescription,profileImage;
    public String gst = "0";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productsList;
    private AdapterProduct adapterProduct;

    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;

    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        Checkout.preload(getApplicationContext());

        //Init Ui views\
        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        shopDesc = findViewById(R.id.shopDesc);
        backBtn = findViewById(R.id.backBtn);
        cartBtn = findViewById(R.id.cartBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        cartCountTv = findViewById(R.id.cartCountTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewsBtn = findViewById(R.id.reviewsBtn);

        //Init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        //Get uid of clicked shop from intent
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();


        easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quanity",new String[]{"text","not null"}))
                .doneTableColumn();

        deleteCartData();

        cartCount();
        //Search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                try {
                    adapterProduct.getFilter().filter(charSequence);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Filter
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategoriesfilter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //Get Selected Item
                                String selected = Constants.productCategoriesfilter[which];
                                filteredProductsTv.setText("Showing "+selected);
                                if(selected.equals("All")){
                                    //Load All
                                    loadShopProducts();
                                }else{
                                    //Filtered Load
                                    adapterProduct.getFilter().filter(selected);
                                }
                            }
                        }).show();
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShopDetailsActivity.this,ShopReviewsActivity.class);
                intent.putExtra("shopUid",shopUid);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //View cart
                    showCartDialog();
            }
        });

    }

    public void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_Cv9y1EiEX8Kgxu");

        checkout.setImage(R.drawable.atoslogo);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", "Atos Cafe");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg");
            //from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", Double.toString(TotalPrice * 100));//pass amount in currency subunits
            System.out.println(TotalPrice);
            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        submitOrder();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(ShopDetailsActivity.this,"Payment Failed!",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ShopDetailsActivity.this,ShopDetailsActivity.class);
        intent.putExtra("shopUid",shopUid);
        startActivity(intent);
    }

    private float ratingSum = 0;
    private void loadReviews() {
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

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void deleteCartData() {

        easyDB.deleteAllDataFromTable(); //Delete all records from cart
    }

    public void cartCount(){
        //Get cart count
        int count = easyDB.getAllData().getCount();
        if(count<=0){
            //If no items in cart, hide cart count
            cartCountTv.setVisibility(View.GONE);
        }else{
            //return count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);
        }
    }

    public double TotalPrice = 0.00;
    //Need to access these views in adapter so making public
    public TextView sTotalTv,gstTv,TotalPriceTv;

    private void showCartDialog() {
        // Initialize list
        cartItemList = new ArrayList<>();

        // Inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);

        // Initialize views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        gstTv = view.findViewById(R.id.gstTv);
        TotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quanity", new String[]{"text", "not null"}))
                .doneTableColumn();

        // Get all records from DB
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            TotalPrice = TotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    "" + id,
                    "" + pId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity
            );
            cartItemList.add(modelCartItem);
        }

        // Set up adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        // Set to recyclerview
        cartItemsRv.setAdapter(adapterCartItem);

        gstTv.setText("₹ " + gst);
        sTotalTv.setText("₹ " + Double.parseDouble(String.valueOf(TotalPrice)));
        TotalPriceTv.setText("₹" + (TotalPrice + Double.parseDouble(gst.replace("₹", ""))));

        if(cartItemList != null && cartItemList.size() > 0) {
            dialog = builder.create();
            dialog.show();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    TotalPrice = 0.00;
                }
            });

        }else{
            Toast.makeText(ShopDetailsActivity.this,"Cart is empty...",Toast.LENGTH_SHORT).show();
        }



        // Place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartItemList.size() == 0) {
                    // Cart is empty
                    Toast.makeText(ShopDetailsActivity.this, "No items in cart", Toast.LENGTH_SHORT).show();
                    return; // Don't proceed further
                }
                startPayment();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    String orderID;
    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        //for order id and order time
        String timestamp = ""+System.currentTimeMillis();

        String cost = TotalPriceTv.getText().toString().trim().replace("₹",""); //remove ₹ if contains

        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress"); // In progress, completed, cancelled
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        orderID = timestamp;

        //Add to DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Sellers").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items
                        if(cartItemList != null && cartItemList.size() > 0) {

                            for (int i = 0; i < cartItemList.size(); i++) {
                                String pId = cartItemList.get(i).getpId();
                                String id = cartItemList.get(i).getId();
                                String cost = cartItemList.get(i).getCost();
                                String name = cartItemList.get(i).getName();
                                String price = cartItemList.get(i).getPrice();
                                String quantity = cartItemList.get(i).getQuantity();

                                HashMap<String, String> hashMap1 = new HashMap<>();
                                hashMap1.put("pId", pId);
                                hashMap1.put("name", name);
                                hashMap1.put("cost", cost);
                                hashMap1.put("price", price);
                                hashMap1.put("quantity", quantity);

                                ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                            }

                            progressDialog.dismiss();
                            Toast.makeText(ShopDetailsActivity.this,"Order Placed Successfully...",Toast.LENGTH_SHORT).show();
                            //Send notification to shop once order is placed
                            prepareNotificationMessage(timestamp);

                            dismissCartDialog();
                            deleteCartData();

                        }else{
                            Toast.makeText(ShopDetailsActivity.this,"Cart is empty...",Toast.LENGTH_SHORT).show();

                        }}
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Order Placing Failed
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void dismissCartDialog() {
        // Get the root layout of the dialog_cart.xml layout
        Intent intent = new Intent(ShopDetailsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            //Set User data

//                            loadAllShops();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopProducts() {
        productsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Clear List before adding items
                        productsList.clear();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            ModelProduct modelProduct = dataSnapshot.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        //Setup Adapter
                        adapterProduct = new AdapterProduct(ShopDetailsActivity.this,productsList);
                        productsRv.setAdapter(adapterProduct);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Get shop Data
                shopName= "" + snapshot.child("shopName").getValue();
                shopDescription = "" + snapshot.child("Description").getValue();
                profileImage =  "" + snapshot.child("shopIcon").getValue();

                //Set shop data
                shopNameTv.setText(shopName);
                shopDesc.setText(shopDescription);

                try{
                    Picasso.get().load(profileImage).into(shopIv);
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void prepareNotificationMessage(String orderID){
        String NOTIFICATION_TOPIC = "/topics/" +Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order "+orderID;
        String NOTIFICATION_MESSAGE = "Congrats! You've a new order";
        String NOTIFICATION_TYPE = "NewOrder";

        //Create json what and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try{
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderID",orderID);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);
        } catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo,orderID);
    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderID) {
        //Send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending notification, start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this,OrderDetailsActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderID);
                startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if notification sending failed, start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this,OrderDetailsActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderID);
                startActivity(intent);
                Toast.makeText(ShopDetailsActivity.this,"Notification not sent",Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //Required headers
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key=" + Constants.FCM_KEY);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}