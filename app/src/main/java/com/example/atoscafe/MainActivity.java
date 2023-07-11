package com.example.atoscafe;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

//import com.denzcoskun.imageslider.ImageSlider;
//import com.denzcoskun.imageslider.models.SlideModel;
//import com.denzcoskun.imageslider.constants.ScaleTypes;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navigationView;
    private final String HOME_FRAGMENT_TAG = "home_fragment";
    private HomeFragment homeFragment;
    private final String HISTORY_FRAGMENT_TAG = "order_fragment";
    private OrderHistoryFragment orderHistoryFragment;
    private final String PROFILE_FRAGMENT_TAG = "profile_fragment";
    private ProfileFragment profileFragment;
    public static final String fileName = "login";
    public static final String Username = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.body_container, new HomeFragment()).commit();
        navigationView.setSelectedItemId(R.id.nav_home);

//        sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        if (homeFragment == null) {
                            // HomeFragment has not been added to the FragmentManager yet
                            homeFragment = new HomeFragment();
                            getSupportFragmentManager().beginTransaction().add(R.id.body_container, homeFragment, HOME_FRAGMENT_TAG).commit();
                        } else {
                            // HomeFragment has already been added, re-attach it
                            getSupportFragmentManager().beginTransaction().attach(homeFragment).commit();
                        }
                        fragment = homeFragment;
                        break;
                    case R.id.nav_profile:
                        if (profileFragment == null) {
                            // HomeFragment has not been added to the FragmentManager yet
                            profileFragment = new ProfileFragment();
                            getSupportFragmentManager().beginTransaction().add(R.id.body_container, profileFragment, PROFILE_FRAGMENT_TAG).commit();
                        } else {
                            // HomeFragment has already been added, re-attach it
                            getSupportFragmentManager().beginTransaction().attach(profileFragment).commit();
                        }
                        fragment = profileFragment;
                        break;
//                    case R.id.nav_pay:
//                        fragment = new PayFragment();
//                        break;
                    case R.id.nav_history:
                        if (orderHistoryFragment == null) {
                            // orderHistoryFragment has not been added to the FragmentManager yet
                            orderHistoryFragment = new OrderHistoryFragment();
                            getSupportFragmentManager().beginTransaction().add(R.id.body_container, orderHistoryFragment, HISTORY_FRAGMENT_TAG).commit();
                        } else {
                            // orderHistoryFragment has already been added, re-attach it
                            getSupportFragmentManager().beginTransaction().attach(orderHistoryFragment).commit();
                        }
                        fragment = orderHistoryFragment;
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.body_container, fragment).commit();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
