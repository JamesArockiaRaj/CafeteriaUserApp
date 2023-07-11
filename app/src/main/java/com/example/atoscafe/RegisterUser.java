package com.example.atoscafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class RegisterUser extends AppCompatActivity {

    private EditText etUsername, etPassword,etmail,etphone;
    private MaterialButton registerbtn;

    SharedPreferences sharedPreferences;
    public static final String fileName = "login";


    //Firebase
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //init ui views
        etUsername = findViewById(R.id.etusername);
        etPassword = findViewById(R.id.etpassword);
        etmail = findViewById(R.id.etmail);
        etphone = findViewById(R.id.etphone);
        registerbtn = findViewById(R.id.registerbtn);


        sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Username)){
            Intent i = new Intent(RegisterUser.this, MainActivity.class);
            startActivity(i);
        }
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Register User
                inputData();
            }
        });


    }
    private String Username,Password,email,phone;
    private void inputData() {
//1) Input Data
        Username = etUsername.getText().toString().trim();
        email = etmail.getText().toString().trim();
        Password = etPassword.getText().toString().trim();
        phone = etphone.getText().toString().trim();
        //2) Validate Data

//        if(AddImage.drawable.constantState == getResources().getDrawable(R.id.add)){
//        }


        if(TextUtils.isEmpty(Username)){
            Toast.makeText(this,"Shop Name is Required",Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid Mail Id",Toast.LENGTH_SHORT).show();
            return;
            //don't proceed further
        }
        if(phone.length()!=10){
            Toast.makeText(this,"Phone number must be 10 numbers",Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }

        if(Password.length()<6){
            Toast.makeText(this,"Password must be atleast 6 characters",Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }

        createUser();
    }

    private void createUser() {
        //3) Add Data to DB
        progressDialog.setTitle("Creating Account...");
        progressDialog.show();

        //Create Account
        firebaseAuth.createUserWithEmailAndPassword(email,Password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Account created
                        saveFirebasedata();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to Account creation
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUser.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void saveFirebasedata() {
        progressDialog.setMessage("Saving Account Info...");

        //3) Add Data to DB
        progressDialog.setTitle("Adding User...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();


            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+email);
            hashMap.put("phone",""+phone);
            hashMap.put("Username",""+Username);
            hashMap.put("timestamp",""+timestamp);

            //Add to DB
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //added to DB
                            progressDialog.dismiss();
                            Toast.makeText(RegisterUser.this,"User Added Successfully!",Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(RegisterUser.this,MainActivity.class);
                            startActivity(i);
//                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //FAILED TO ADD PRODUCT
                            progressDialog.dismiss();
                            Toast.makeText(RegisterUser.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });


    }

}