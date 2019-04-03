package com.example.harikrishna.digisuraksh.fragments;

import android.net.ConnectivityManager;
import android.net.Network;

/**
 * Created by harikrishna on 02-11-2017.
 */

public class NetworkListener extends ConnectivityManager.NetworkCallback {
   public  boolean networkstatus;

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        networkstatus=true;
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        networkstatus=false;

    }
    public  boolean  getstatus(){
        return networkstatus;
    }


}

