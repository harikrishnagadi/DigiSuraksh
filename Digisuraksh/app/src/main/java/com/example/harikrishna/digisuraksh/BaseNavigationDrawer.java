package com.example.harikrishna.digisuraksh;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.doctoror.particlesdrawable.ParticlesDrawable;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.example.harikrishna.digisuraksh.fragments.FireStationFrag;
import com.example.harikrishna.digisuraksh.fragments.LocActivity;
import com.example.harikrishna.R;
import com.example.harikrishna.digisuraksh.fragments.PoliceFrag;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.List;
import java.util.Map;

/**
 * Created by harikrishna on 11-10-2017.
 */

public class BaseNavigationDrawer extends AppCompatActivity {
    final WrapAroundMode fullmap = WrapAroundMode.ENABLE_WHEN_SUPPORTED;
    LocationDisplay Currentloc;
    MapView mapView;
    ArcGISMap map;
    Point userloc;
    String userCity;
    String address;
    boolean startflag=false;
    ConstraintLayout gpslayout;
    ConstraintLayout parentlayout;
    ImageView hamanage;
    Drawer result;
    ParticlesDrawable mdrawable;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.basenav);
        gpslayout=findViewById(R.id.gpslayout);
        if (!isdeviceconnected(this)) {
            connecttonetwork();
        }
        mapView = findViewById(R.id.map);
        mapView.setWrapAroundMode(WrapAroundMode.ENABLE_WHEN_SUPPORTED);
       map = new ArcGISMap(Basemap.createOpenStreetMap());
       mapView.setMap(map);
        parentlayout=findViewById(R.id.parentlayout);
        mdrawable=new ParticlesDrawable();
        mdrawable.setDotColor(getResources().getColor(R.color.black));
        mdrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.DARKEN);
        mdrawable.setLineColor(getResources().getColor(R.color.black));
        hamanage=findViewById(R.id.hambut);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey), MODE_PRIVATE);
       String username= sharedPreferences.getString("Username","user");



        //if you want to update the items at a later time it is recommended to keep it in a variable
        final PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(2).withName("Ambulance").withIcon((getResources().getDrawable(R.drawable.ic_people)));
        final PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(4).withName("Fire Station").withIcon(getResources().getDrawable(R.drawable.ic_firefighter_svgrepo_com));
        final PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(5).withName("Police").withIcon(getResources().getDrawable(R.drawable.ic_police));
        final PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(3).withName("SIGN OUT").withSelectedColor((getResources().getColor(R.color.white))).withIcon(getResources().getDrawable(R.drawable.ic_logout_sketch));
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(mdrawable)
                .addProfiles(
                        new ProfileDrawerItem().withName(username).withIcon(getResources().getDrawable(R.drawable.ic_round_account_button_with_user_inside))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();
        mdrawable.start();


//create the drawer and remember the `Drawer` result object
        final Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withTranslucentNavigationBar(false)
                .withActionBarDrawerToggle(false)
                .addDrawerItems(
                        item3,
                        item5,
                        item6,
                        item4

                )
                .withSliderBackgroundColor(getResources().getColor(R.color.grey))
                .withSelectedItem(-1)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        fragmentManager=getFragmentManager();
                        if(item3.isSelected() ){
                            if(fragmentManager.findFragmentByTag("Ambulance")!=null)
                            {
                                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Ambulance"));
                            }
                            else{
                                Fragment ambfag = new LocActivity();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.fragplaceholder,ambfag,"Ambulance");
                                transaction.commit();
                            }
                        }
                        if(item5.isSelected() ){
                            if(fragmentManager.findFragmentByTag("Fire")!=null)
                            {
                                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Fire"));
                            }
                            else{
                                Fragment ambfag = new FireStationFrag();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.fragplaceholder,ambfag,"Fire");
                                transaction.commit();
                            }


                        }
                        if(item6.isSelected() ){
                            if(fragmentManager.findFragmentByTag("Police")!=null)
                            {
                                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Police"));
                            }
                            else{

                                Fragment ambfag = new PoliceFrag();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.fragplaceholder,ambfag,"Police");
                                transaction.commit();
                            }

                        }
                        if(item4.isSelected() ){
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(BaseNavigationDrawer.this,SignInAct.class);
                            startActivity(intent);


                        }
                        return false;
                    }
                }).build();
         this.result=result;
          hamanage.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  if(result.isDrawerOpen()){

                      result.closeDrawer();
                  }
                  else{

                      result.openDrawer();
                  }
              }
          });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Currentloc = mapView.getLocationDisplay();
        Currentloc.startAsync();
        Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        userloc = Currentloc.getLocation().getPosition();
        String urlgeo = getResources().getString(R.string.geocode_services);
        final LocatorTask geocodetask = new LocatorTask(urlgeo);
        if (userloc != null) {

            final ListenableFuture<List<GeocodeResult>> geocodeResultListenableFuture = geocodetask.reverseGeocodeAsync(userloc);
            geocodeResultListenableFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {

                        List<GeocodeResult> geocodeResults = geocodeResultListenableFuture.get();
                        if (!(geocodeResults.isEmpty())) {
                            geocodeResultListenableFuture.removeDoneListener(this);
                            StringBuilder adressbuilder = new StringBuilder();
                            StringBuilder citybuilder = new StringBuilder();
                            for (GeocodeResult temp : geocodeResults) {


                                // adressbuilder = adressbuilder.append(temp.getAttributes());
                                //Log.w(""+temp.getLabel(),"is inside string");
                                Map stringMap = temp.getAttributes();

                                adressbuilder.append(String.format("%s\n%s, %s ",
                                        stringMap.get("Address"), stringMap.get("City"),
                                        stringMap.get("Region")));
                                citybuilder.append(String.format("%s", stringMap.get("City")));


                            }
                            address = adressbuilder.toString();
                            userCity = citybuilder.toString();
                            Log.w("BasenAV", userCity + "is the user's city");
                            if (userCity != null && (!startflag)) {
                                startflag = true;
                                SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey), MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("Usercity", "" + userCity);
                                editor.apply();
                                gpslayout.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                onStart();

                            }

                        }


                    } catch (Exception e) {
                        //Bad REQUEST todo please try again later
                        e.printStackTrace();
                    }

                }
            });

        }else{

        }

    }
    public boolean isdeviceconnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return (network != null && (network.isConnectedOrConnecting()));
    }

    public void connecttonetwork(){

        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }


}
