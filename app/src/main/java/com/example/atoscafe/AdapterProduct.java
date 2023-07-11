package com.example.atoscafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.HolderProduct> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProduct filter;

    public AdapterProduct(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }


    @NonNull
    @Override
    public HolderProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_products,parent,false);
        return new HolderProduct(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProduct holder, int position) {
        //Get Data
        ModelProduct modelProduct = productsList.get(position);
        String productId = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String productTitle = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();

        String Price = modelProduct.getPrice();
//        String decrementBtn = modelProduct.

        //SET DATA
        holder.titleTv.setText(productTitle);
//        holder.quantityTv.setText(quantity);
        holder.priceTv.setText("₹ "+Price);

        try {
            Picasso.get().load(icon).placeholder(R.drawable.addproduct).into(holder.productIconIv);
        }
        catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.addproduct);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Add Product to Cart
                showQuantityDialog(modelProduct);

            }
        });

    }

    private double cost =0, finalCost=0;
    private int productquantity =0;
    private void showQuantityDialog(ModelProduct modelProduct) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);

        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView costTv = view.findViewById(R.id.priceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        MaterialButton AddToCartBtn = view.findViewById(R.id.addToCartTv);

        //Get Data from Model
        String productId = modelProduct.getProductId();
        String image = modelProduct.getProductIcon();
        String productQuantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String price = modelProduct.getPrice();


        cost= Double.parseDouble(price.replaceAll("₹ ",""));
        finalCost= Double.parseDouble(price.replaceAll("₹ ",""));
        productquantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        try {
            Picasso.get().load(image).placeholder(R.drawable.addproduct).into(productIconIv);
        }
        catch (Exception e){
            productIconIv.setImageResource(R.drawable.addproduct);
        }

        titleTv.setText(""+title);
        quantityTv.setText(""+productquantity);
        costTv.setText("₹ "+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalCost = finalCost + cost;
                productquantity++;

                costTv.setText("₹ "+finalCost);
                quantityTv.setText(""+productquantity);
            }
        });

        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(productquantity>1){
                    finalCost = finalCost - cost;
                    productquantity--;
                    costTv.setText("₹ "+finalCost);
                    quantityTv.setText(""+productquantity);
                }
            }
        });

        AddToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalPrice = costTv.getText().toString().trim().replace("₹ ","");
                String quantity = quantityTv.getText().toString().trim();

                //Add to DB (Sqlite)
                addToCart(productId,title,priceEach,totalPrice,quantity);
                dialog.dismiss();
//              Toast.makeText(context,"Added to Cart",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private int itemId =1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quanity",new String[]{"text","not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id",itemId)
                .addData("Item_PID",productId)
                .addData("Item_Name",title)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quanity",quantity)
                .doneDataAdding();

        Toast.makeText(context,"Added to Cart",Toast.LENGTH_SHORT).show();

        //Update cart count
        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterProduct(this,filterList);
        }
        return filter;
    }

    class HolderProduct extends RecyclerView.ViewHolder{

        //ui views
        private ImageView productIconIv;
        private TextView titleTv,priceTv,addToCartTv,quantityTv;

        public HolderProduct(@NonNull View itemView) {
            super(itemView);

            //Init ui views
            productIconIv = itemView.findViewById(R.id.productIconIv);
            titleTv = itemView.findViewById(R.id.titleTv);
            priceTv = itemView.findViewById(R.id.priceTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);

        }
    }
}
