package com.example.atoscafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView registertxt;
    private MaterialButton loginbtn;

    SharedPreferences sharedPreferences;
    public static final String fileName = "login";
    public static final String Username = "username";
    public static final String Password = "password";

    //Firebase
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etusername);
        etPassword = findViewById(R.id.etpassword);
        loginbtn = findViewById(R.id.loginbtn);
        registertxt = findViewById(R.id.registertxt);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Username)){
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }


        registertxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this,RegisterUser.class);
                startActivity(i);
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              loginUser();
     }
        });
    }
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(LoginActivity.this,"Invalid Mail Id",Toast.LENGTH_SHORT).show();
            return;
            //don't proceed further
        }

        if(password.length()<6){
            Toast.makeText(LoginActivity.this,"Enter Password",Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }

        progressDialog.setTitle("Logging In...");
        progressDialog.show();


        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Username,email);
                        editor.putString(Password,password);
                        editor.commit();
                        Intent i = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(i);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
//                if(username.equals("user@gmail.com")&& password.equals("user123")){
//                    //Toast.makeText(MainActivity.this, "LOGIN SUCCESS!!!", Toast.LENGTH_SHORT).show();
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString(Username,username);
//                    editor.putString(Password,password);
//                    editor.commit();
//                    Intent i = new Intent(LoginActivity.this,MainActivity.class);
//                    startActivity(i);
//                }
//                else{
//                    Toast.makeText(getApplicationContext(), "LOGIN FAILED", Toast.LENGTH_SHORT).show();
//                    etUsername.setText("");
//                    etUsername.requestFocus();
//                    etPassword.setText("");
//                }
    }

}
