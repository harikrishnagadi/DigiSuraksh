package com.example.harikrishna.digisuraksh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.harikrishna.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by harikrishna on 19-08-2017.
 */

public class OpenAct extends AppCompatActivity  {

    private FirebaseAuth mAuth;
    Map<String,String> Usernames = new HashMap<>();
    Map<String,String> Passwords = new HashMap<>();
    Map<String,String> Phonenos = new HashMap<>();
    int countofusers=0;
    private FloatingActionButton fab;
    private Handler mhandler;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.loginact);
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Log.w(currentUser.getUid(), "is the uid");
            Intent loc = new Intent(OpenAct.this, BaseNavigationDrawer.class);
            startActivity(loc);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            // TODO: 05-10-2017
             // open the app
        }
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.openact);

        //Restore Instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey),MODE_PRIVATE);
        final SharedPreferences.Editor sharedprefeditor = sharedPreferences.edit();
        getusersregistered();

        fab= findViewById(R.id.fab);
        final LottieAnimationView buttonanim =   findViewById(R.id.button_anim);
        final CountryCodePicker ccp = findViewById(R.id.ccp);
        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.animate().scaleX(1f).scaleY(1f).setDuration(1000).start();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ccp.getSelectedCountryCodeAsInt()==ccp.getDefaultCountryCodeAsInt())
                {
                    sharedprefeditor.putString("countrycode",ccp.getSelectedCountryCode());
                    fab.animate().scaleX(0f).scaleY(0f).setDuration(1000).start();
                    buttonanim.setVisibility(View.VISIBLE);
                    buttonanim.setAnimation("simple_check.json");
                    buttonanim.addColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC));
                    buttonanim.playAnimation();


                      // set an exit transition


                    getWindow().setExitTransition(new Fade());
                    mhandler = new Handler();
                    mhandler.postDelayed(delayintent,2000);






                }
                else{
                    Toast.makeText(view.getContext(),"sorry not available in your country",Toast.LENGTH_LONG).show();
                }
            }
        });



    }
    public void  getusersregistered()
    {
        /*

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UsersReg");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                countofusers++;
                String password= dataSnapshot.child("Pass").getValue().toString();
                String username = dataSnapshot.child("Username").getValue().toString();
                String phoneno = dataSnapshot.child("Phone").getValue().toString();
                Usernames.put("user"+countofusers,username);
                Passwords.put("user"+countofusers,password);
                Phonenos.put("user"+countofusers,phoneno);

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

 */
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.w("entering this","sucessfuly");
        ((UserDetails) getApplication()).setUserDetails(Usernames,Phonenos,Passwords);
    }
    private Runnable delayintent = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(OpenAct.this,SignInAct.class);
            startActivity(intent);

        }
    };
}



