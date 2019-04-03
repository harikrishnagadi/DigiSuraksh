package com.example.digidriver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by harikrishna on 21-10-2017.
 */

public class OpenAct extends AppCompatActivity {
    FirebaseAuth mAuth;
    Map<String, String> AmbUsernames = new HashMap<>();
    Map<String, String> AmbPhonenos = new HashMap<>();
    Map<String, String> FireUsernames = new HashMap<>();
    Map<String, String> FirePhonenos = new HashMap<>();
    Map<String, String> PoliceUsernames = new HashMap<>();
    Map<String, String> PolicePhonenos = new HashMap<>();
    String service;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
        service = pref.getString("service", null);
        if (service != null) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                {
                    Log.w(currentUser.getUid(), "is the uid");
                    switch (service) {
                        case "ambulance":
                            Intent loc = new Intent(OpenAct.this, MapActivityAmb.class);
                            startActivity(loc);
                            break;
                        case "fire":
                            Intent locfire = new Intent(OpenAct.this, MapActivityFire.class);
                            startActivity(locfire);
                            break;
                        case "police":
                            Intent locpolice = new Intent(OpenAct.this, MapActivityPolice.class);
                            startActivity(locpolice);
                            break;

                    }
                }

            }


        }

        setContentView(R.layout.openact);

        BoomMenuButton bmb = findViewById(R.id.bmb);
        // build ambulance button
        HamButton.Builder builder1 = new HamButton.Builder()
                .normalColorRes(R.color.black)
                .normalText("Ambulance")
                .normalImageRes(R.drawable.ic_people)
                .highlightedColorRes(R.color.green)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("service", "ambulance");
                        service = "ambulance";
                        editor.apply();
                        Handler mhandler = new Handler();
                        mhandler.postDelayed(delayintent, 1000);

                    }
                });
        bmb.addBuilder(builder1);

        //build fire button

        HamButton.Builder builder2 = new HamButton.Builder()
                .normalColorRes(R.color.black)
                .normalText("Fire")
                .normalImageRes(R.drawable.ic_firefighter_svgrepo_com)
                .highlightedColorRes(R.color.red)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("service", "fire");
                        service = "fire";
                        editor.apply();
                        Handler mhandler = new Handler();
                        mhandler.postDelayed(delayintent, 1000);
                    }
                });
        bmb.addBuilder(builder2);


        //build police button
        HamButton.Builder builder3 = new HamButton.Builder()
                .normalColorRes(R.color.black)
                .normalText("Police")
                .normalImageRes(R.drawable.ic_police)
                .highlightedColorRes(R.color.blue)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("service", "police");
                        service = "police";
                        editor.apply();
                        Handler mhandler = new Handler();
                        mhandler.postDelayed(delayintent, 1000);
                    }
                });
        bmb.addBuilder(builder3);

        //getDriversRegistered

    }

    public void getDriversRegistered() {

        DatabaseReference AmbdatabaseReference = FirebaseDatabase.getInstance().getReference();
        String key = AmbdatabaseReference.child("driver1").child("username").toString();
        AmbdatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String username = dataSnapshot.child("Username").getValue().toString();
                String phoneno = dataSnapshot.child("Phone").getValue().toString();
                AmbUsernames.put(dataSnapshot.getKey(), username);
                AmbPhonenos.put(dataSnapshot.getKey(), phoneno);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final DatabaseReference FiredatabaseReference = FirebaseDatabase.getInstance().getReference("DriversReg").child("Fire");
        FiredatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String username = dataSnapshot.child("Username").getValue().toString();
                String phoneno = dataSnapshot.child("Phone").getValue().toString();
                FireUsernames.put(dataSnapshot.getKey(), username);
                FirePhonenos.put(dataSnapshot.getKey(), phoneno);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final DatabaseReference PolicedatabaseReference = FirebaseDatabase.getInstance().getReference("DriversReg").child("Police");
        PolicedatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String username = dataSnapshot.child("Username").getValue().toString();
                String phoneno = dataSnapshot.child("Phone").getValue().toString();
                PoliceUsernames.put(dataSnapshot.getKey(), username);
                PolicePhonenos.put(dataSnapshot.getKey(), phoneno);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void onResume() {
        super.onResume();
        ((DriversDetails) getApplication()).setAmbdriverDetails(AmbUsernames, AmbPhonenos);
        ((DriversDetails) getApplication()).setFiredriverDetails(FireUsernames, FirePhonenos);
        ((DriversDetails) getApplication()).setPolicedriverDetails(PoliceUsernames, PoliceUsernames);
    }

    private Runnable delayintent = new Runnable() {
        @Override
        public void run() {

            switch (service) {
                case "ambulance":
                    Intent loc = new Intent(OpenAct.this, SigninAmb.class);
                    startActivity(loc);
                    break;
                case "fire":
                    Intent locfire = new Intent(OpenAct.this, SigninFire.class);
                    startActivity(locfire);
                    break;
                case "police":
                    Intent locpolice = new Intent(OpenAct.this, SigninPolice.class);
                    startActivity(locpolice);
                    break;
            }
        }


    };

}

