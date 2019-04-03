package com.example.harikrishna.digisuraksh;

import android.content.Context;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.harikrishna.digisuraksh.fragments.NetworkListener;
import com.example.harikrishna.R;

/**
 * Created by harikrishna on 30-10-2017.
 */

public class LoadingAct extends AppCompatActivity {
    GnssStatus.Callback mGnssStatusCallback;
    LottieAnimationView lottieAnimationView;
    ConnectivityManager connectivityManager ;
    NetworkRequest networkRequest;
    ConstraintLayout layout;
    NetworkListener networkListener;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loadingact);
        networkListener=new NetworkListener();
         lottieAnimationView = findViewById(R.id.lottieview);
         lottieAnimationView.setAnimation("updating_map.json");
         lottieAnimationView.playAnimation();
         lottieAnimationView.loop(true);
         layout = findViewById(R.id.parentlayout);
         connectivityManager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
         networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
         connectivityManager.registerNetworkCallback(networkRequest,networkListener);
        checklocset();



    }
    public void checklocset(){
        LocationManager Locmanager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //Locmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,10,locationListener);
        boolean gps_enabled =false;
        boolean network_enabled = false;

        try{
            gps_enabled= Locmanager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try{
            network_enabled= Locmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        }
        catch (Exception e){
            e.printStackTrace();
        }

        if( !network_enabled) {
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // notify user
                    lottieAnimationView.clearAnimation();
                    layout.setBackgroundColor(getResources().getColor(R.color.white));
                    lottieAnimationView.setAnimation("ciclista_salita.json");
                    lottieAnimationView.playAnimation();
                    lottieAnimationView.loop(true);
                }
            },20000);


        }

    }


}
