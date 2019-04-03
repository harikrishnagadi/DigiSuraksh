package com.example.harikrishna.digisuraksh.fragments;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedEvent;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.symbology.MarkerSymbol;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityResult;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityRoute;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Facility;
import com.esri.arcgisruntime.tasks.networkanalysis.Incident;
import com.example.harikrishna.R;
import com.example.harikrishna.digisuraksh.BaseNavigationDrawer;
import com.example.harikrishna.digisuraksh.SignInAct;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.irozon.sneaker.Sneaker;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import br.com.goncalves.pugnotification.notification.PugNotification;

import static android.content.Context.MODE_PRIVATE;


public class LocActivity extends Fragment {
    private final WrapAroundMode fullmap = WrapAroundMode.ENABLE_WHEN_SUPPORTED;
    private int requestCode =2;
    LocationDisplay Currentloc;
    GraphicsOverlay mGraphicsOverlay;
    double amblat;
    double amblong;
    Map<String,Graphic> ambgraphics= new HashMap<>();
    Map<String,String> Driversonline = new HashMap<>();
    DatabaseReference reference;
    int driversOnline=0;
    Graphic[] graphics = new Graphic[100];
    PictureMarkerSymbol[] ambmarker = new PictureMarkerSymbol[110];
    String ID;
    boolean existeddriverresult;
    FloatingActionButton locationbut;
    Button getDriver;
    String[] reqpermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
    Map<String,Point> driversloc = new HashMap<>();
    Point userloc;
    SimpleLineSymbol RouteSymbol;
    ClosestFacilityParameters closestFacilityParameters;
    ClosestFacilityResult closestResult;
    Map<String,String> facilitiesdata= new HashMap<>();
    int closestindex;
    boolean driverassigned=false;
    boolean onstartstarted=false;
    MapView mapView;
    ImageView logo;
    ChildEventListener childlistener;
    BitmapDrawable ambdraw;
    LottieAnimationView animationView,bottomanimview;
    String assigneddrivername;
    String assigneddriverphonenum;
    double Distance;
    double arrivaltime;
    ConstraintLayout bottomlayout;
    FrameLayout driverdetailslayout;
    ImageView driverimage;
    TextView drivername;
    FrameLayout mapbaselayout;
    TextView Driverphonenum;
    TextView time;
    TextView distance;
    ValueEventListener ambloclistener;
    DatabaseReference amblocRefer;
    MapScaleChangedListener mapScaleChangedListener;
    ChildEventListener trackinglistener;
    DatabaseReference trackrefer;
    DatabaseReference   driverRefer;
    double bearing;
    String userCity;
    double finalbearing;
    String drivercity;
    View slideupview;
    SlideUp slideUp;
    TextView bottomlayouttextview;
    String name;
    String phonenum;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_loc, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        super.onCreate(savedInstanceState);
        View v = getView();
        // get the map form the layout

        mapView = getActivity().findViewById(R.id.map);
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey), MODE_PRIVATE);
        userCity = pref.getString("Usercity", null);
        name=pref.getString("Username",null);
        phonenum=pref.getString("Phonenum",null);
        Log.w("LocAct", "user city is" + userCity);
        Currentloc = mapView.getLocationDisplay();
        Currentloc.startAsync();
        animationView = v.findViewById(R.id.lottieAnimationView);
        bottomanimview = v.findViewById(R.id.bottomanim);
        logo = v.findViewById(R.id.logo);
        bottomlayout = v.findViewById(R.id.bottomlayout);
        driverdetailslayout=v.findViewById(R.id.driverdetailsbottomlayout);
        bottomlayouttextview=v.findViewById(R.id.bottomlayouttextview);
        driverimage = v.findViewById(R.id.driverimage);
        drivername = v.findViewById(R.id.nameview);
        Driverphonenum = v.findViewById(R.id.phonenum);
        slideupview=v.findViewById(R.id.slideview);
        time = v.findViewById(R.id.clock);
        distance = v.findViewById(R.id.distance);
        mapbaselayout = getActivity().findViewById(R.id.fragplaceholder);
        // create a new graphics overlay and add it to the mapview
        mGraphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(mGraphicsOverlay);
        //CHECK IF LOCATION IS BEING DISPLAYED AND SHOW THE BUTTONS
        locationbut = v.findViewById(R.id.floatloc);
        getDriver = v.findViewById(R.id.getambulance);
        checklocset();
        locationbut = v.findViewById(R.id.floatloc);
        locationbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!driverassigned) {
                    Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                    Currentloc.startAsync();
                }
                else if(driverassigned){
                    Log.w("entering this","block");
                    slideUp.show();
                }


            }
        });
        slideUp=new SlideUpBuilder(slideupview)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {

                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE) {
                            locationbut.setVisibility(View.VISIBLE);
                            if(driverassigned) {
                                bottomlayout.setVisibility(View.VISIBLE);
                            }
                        }
                        else {
                            locationbut.setVisibility(View.GONE);
                            bottomlayout.setVisibility(View.GONE);
                        }
                    }
                })
                .withStartState(SlideUp.State.HIDDEN)
                .withLoggingEnabled(true)
                .withGesturesEnabled(true)
                .withStartGravity(Gravity.BOTTOM)
                .build();
        bottomlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(driverassigned){
                    slideUp.show();
                }
            }
        });
        mapScaleChangedListener = new MapScaleChangedListener() {
            @Override
            public void mapScaleChanged(MapScaleChangedEvent mapScaleChangedEvent) {
                if (mapView.getMapScale() > 350000) {
                    getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(1000);
                } else if (mapView.getMapScale() > 80000) {
                    mGraphicsOverlay.setVisible(true);
                    for (int i = 1; i <= Driversonline.size(); i++) {
                        Log.w("ambmarker is", "" + i);
                        ambmarker[i].setHeight(20);
                        ambmarker[i].setWidth(10);
                    }
                    if (!driverassigned)
                        getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);

                } else if ((mapView.getMapScale() < 80000) && (mapView.getMapScale() > 20000)) {
                    mGraphicsOverlay.setVisible(true);


                    for (int i = 1; i <= Driversonline.size(); i++) {
                        ambmarker[i].setHeight(30);
                        ambmarker[i].setWidth(15);

                    }
                    if (!driverassigned)
                        getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                } else if (mapView.getMapScale() < 20000) {
                    mGraphicsOverlay.setVisible(true);


                    for (int i = 1; i <= Driversonline.size(); i++) {
                        ambmarker[i].setHeight(40);
                        ambmarker[i].setWidth(20);
                    }
                    if (!driverassigned)
                        getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                }
            }
        };
        mapView.addMapScaleChangedListener(mapScaleChangedListener);


        getDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ask for confirmation

                Drawable icon = getResources().getDrawable(R.drawable.ic_ambulance_side_view);
                new LovelyStandardDialog(getActivity())
                        .setTopColorRes(R.color.green)
                        .setButtonsColorRes(R.color.black)
                        .setIcon(icon)
                        .setTitle("Confirm!")
                        .setMessage("Are you sure want to confirm?")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getDriver.setEnabled(false);
                                onconfirmed();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getDriver.setEnabled(true);
                            }
                        })
                        .show();


            }

        });



        //get location updates
        Currentloc = mapView.getLocationDisplay();
        Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        //initialize database
        reference = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Ambulance");
        Log.w("" + reference.getKey(), "is the refernce");
        //CREATE A MARKER
        // create a picture marker and simple graphics
        RouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 5);
        ambdraw = (BitmapDrawable) ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.amb);

        Currentloc.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {

                userloc = Currentloc.getLocation().getPosition();
            }
        });


    }






    public void getdriverdetails(final String driverid){

     driverRefer= FirebaseDatabase.getInstance().getReference("DriversReg").child("Ambulance");

     driverRefer.addChildEventListener(new ChildEventListener() {
         @Override
         public void onChildAdded(DataSnapshot dataSnapshot, String s) {
             Log.w(""+dataSnapshot,"is the refernece");

                 Log.w("started getting","details");
                 String id = dataSnapshot.child("Userid").getValue().toString();
                 if (id.equals(driverid)) {
                     driverRefer.removeEventListener(this);
                     assigneddrivername = dataSnapshot.child("Username").getValue().toString();
                     assigneddriverphonenum = dataSnapshot.child("Phonenum").getValue().toString();
                     if(assigneddriverphonenum!=null && assigneddrivername!=null){

                         driverRefer.removeEventListener(this);
                         driverassigned=true;
                         displaydriverdetails();
                         drivername.setText(assigneddrivername);
                         Driverphonenum.setText("     +91"+assigneddriverphonenum);
                         distance.setText("     "+Distance+" km");
                         time.setText("     "+(int)arrivaltime/60+"h "+(int)arrivaltime%60+"m");
                         bottomlayouttextview.setText(assigneddrivername);
                         Log.w("name",""+assigneddrivername);
                         Log.w("phonenum",""+assigneddriverphonenum);
                         Log.w("arrival time",""+(int)arrivaltime/60+"h"+(int)arrivaltime%60+"m");
                         Log.w("total distance",""+Distance+"km");
                         PugNotification.with(getActivity())
                                 .load()
                                 .title("DIGI SURAKSH")
                                 .message("Ambulance is on the way")
                                 .smallIcon(R.drawable.icon)
                                 .largeIcon(R.drawable.icon)
                                 .flags(Notification.DEFAULT_ALL)
                                 .simple()
                                 .build();
                         Handler mhandler=new Handler();
                         mhandler.postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                 addlistenertotrack(driverid);
                             }
                         },3000);

                 }
             }
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
    public void addlistenertotrack(final String driverid){
        trackrefer=FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Ambulance").child(driverid);
        trackrefer.addChildEventListener(trackinglistener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                         double trackbear;
                         float angle;
                        Log.w("in tracking",""+dataSnapshot.child("l").child("0").getValue()+"is the ref");
                         if(dataSnapshot.getKey().equals("driverloctrack")){
                             //track the amb

                             try {
                                 double lat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                                 double longi = Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                                 Point point = new Point(longi, lat, SpatialReferences.getWgs84());
                                 String driver = getdriverid(driverid);
                                     String i = driver.substring(8);
                                     Log.w("driver id caught is ", driver);
                                     Log.w("grahic we trynna get is", "Graphic" + i);
                                     if (ambgraphics.get("Graphic" + i) != null) {
                                         Point p1 = (Point) ambgraphics.get("Graphic" + i).getGeometry();
                                         boolean samepoint = issamepoint(p1, point);
                                         if (!samepoint) {

                                             ambgraphics.get("Graphic" + i).setGeometry(point);
                                             MarkerSymbol marker = (MarkerSymbol) ambgraphics.get("Graphic" + i).getSymbol();
                                             marker.setAngleAlignment(MarkerSymbol.AngleAlignment.MAP);
                                             if(dataSnapshot.child("l").child("Bearing").getValue()!=null) {

                                                 trackbear = Double.parseDouble(dataSnapshot.child("l").child("Bearing").getValue().toString());
                                                 Log.w("lat,long,bear is",""+lat+longi+trackbear);
                                                 angle= (float)trackbear;
                                                 rotateMarker(marker,  angle);
                                             }

                                         }
                                         else {
                                             MarkerSymbol marker = (MarkerSymbol) ambgraphics.get("Graphic" + i).getSymbol();
                                             marker.setAngleAlignment(MarkerSymbol.AngleAlignment.MAP);
                                             if (dataSnapshot.child("l").child("Bearing").getValue() != null) {

                                                 trackbear = Double.parseDouble(dataSnapshot.child("l").child("Bearing").getValue().toString());
                                                 Log.w("lat,long,bear is", "else" + lat + longi + trackbear);
                                                 angle = (float) trackbear;
                                                 rotateMarker(marker, angle);
                                             }
                                         }
                                 }
                             }catch (Exception e){
                                 e.printStackTrace();
                             }

                         }
                          if(dataSnapshot.getKey().equals("status")){

                             Log.w(""+dataSnapshot.getValue(),"is the child");
                             if(dataSnapshot.getValue().toString().equals("stop")){
                                 trackrefer.removeEventListener(this);
                                 PugNotification.with(getActivity())
                                         .load()
                                         .title("DIGI SURAKSH")
                                         .message("Ambulance has arrived")
                                         .smallIcon(R.drawable.icon)
                                         .largeIcon(R.drawable.icon)
                                         .flags(Notification.DEFAULT_ALL)
                                         .simple()
                                         .build();

                                 mapView.removeMapScaleChangedListener(mapScaleChangedListener);
                                 dataSnapshot.getRef().removeEventListener(this);
                                 dataSnapshot.getRef().removeValue();
                                 Fragment ambfag = new LocActivity();
                                 FragmentTransaction transaction =getActivity().getFragmentManager().beginTransaction();
                                 transaction.replace(R.id.fragplaceholder,ambfag);
                                 transaction.commit();


                             }
                         }
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
    public void displaydriverdetails(){
        Handler mhandler = new Handler();
        mhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                driverimage.setImageDrawable(getResources().getDrawable(R.drawable.ic_taxi_driver));
                bottomanimview.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(1000);
                driverdetailslayout.setScaleX(0f);
                driverdetailslayout.setScaleY(0f);
                driverdetailslayout.setVisibility(View.VISIBLE);
                driverdetailslayout.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                slideUp.show();

            }
        },4000);
    }
    public boolean isdeviceconnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return (network != null && (network.isConnectedOrConnecting()));
    }
    public void checkloc(){
        boolean fineperm = ContextCompat.checkSelfPermission(getActivity(),reqpermissions[0])== PackageManager.PERMISSION_GRANTED;
        boolean coarseperm = ContextCompat.checkSelfPermission(getActivity(),reqpermissions[1])== PackageManager.PERMISSION_GRANTED;
        // check if the permissions are not given and req the user for permissions
        Log.e("FINE",""+fineperm);
        Log.e("coarse",""+coarseperm);
        // get the status of our permissions
        if(!(fineperm&&coarseperm))
        {

            ActivityCompat.requestPermissions(getActivity(), reqpermissions, requestCode);
            Log.e("permission req", "onStatusChanged: ");
        }

    }
    public void connecttonetwork(){

        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.getGraphicsOverlays().clear();
        getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
        if(childlistener!=null) {
            reference.removeEventListener(childlistener);
        }
    }

    @Override
    public  void onResume(){
        super.onResume();


        if(!onstartstarted){
            onStart();
        }
        if(driverassigned){
            trackrefer.addChildEventListener(trackinglistener);// todo trouble here adding this listener
        }



    }


    @Override
    public void onStop() {

        if(driverassigned&&trackinglistener!=null){
            trackrefer.removeEventListener(trackinglistener);

        }
        super.onStop();


    }

    @Override

    public void onStart() {

        super.onStart();
        if (userCity != null) {
            Log.w("this is ","invoked you can start showing ambulances");
            if (!driverassigned) {
                if (!isdeviceconnected(getActivity())) {
                    connecttonetwork();
                }
                checkloc();
                if (reference != null) {

                    reference.addChildEventListener(childlistener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            String userid = dataSnapshot.getKey();
                            if (!existeddriver(userid)) {
                                if (!driverassigned) {
                                    driversOnline++;
                                    Log.w("driver", "is invoked" + dataSnapshot.getKey());
                                    try {

                                        amblat = Double.parseDouble(dataSnapshot.child("driverloc").child("l").child("0").getValue().toString());
                                        amblong = Double.parseDouble(dataSnapshot.child("driverloc").child("l").child("1").getValue().toString());
                                        bearing = Double.parseDouble(dataSnapshot.child("driverloc").child("l").child("Bearing").getValue().toString());
                                        drivercity = dataSnapshot.child("City").getValue().toString();

                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                    if(drivercity!=null) {
                                        if (drivercity.equals(userCity)) {
                                            Point point = new Point(amblong, amblat, SpatialReferences.getWgs84());
                                            userloc = Currentloc.getLocation().getPosition();

                                            try {

                                                ambmarker[driversOnline] = PictureMarkerSymbol.createAsync(ambdraw).get();
                                                ambmarker[driversOnline].setWidth(20);
                                                ambmarker[driversOnline].setHeight(40);
                                                ambmarker[driversOnline].loadAsync();

                                            } catch (InterruptedException e) {
                                                getActivity().finish();
                                                e.printStackTrace();
                                            } catch (ExecutionException e) {
                                                getActivity().finish();
                                                e.printStackTrace();
                                            }
                                            float bear = (float) bearing;
                                            Driversonline.put("driverid" + driversOnline, dataSnapshot.getKey());
                                            graphics[driversOnline] = new Graphic(point, ambmarker[driversOnline]);
                                            mGraphicsOverlay.getGraphics().add(graphics[driversOnline]);
                                            ambmarker[driversOnline].setAngleAlignment(MarkerSymbol.AngleAlignment.MAP);
                                            rotateMarker(ambmarker[driversOnline], bear);
                                            ambgraphics.put("Graphic" + driversOnline, graphics[driversOnline]);
                                            Log.w("driver available is", "" + userid + "& ambmarker" + driversOnline + "is graphic");
                                            driversloc.put(dataSnapshot.getKey(), point);
                                            finalbearing = bear;
                                            if (Driversonline.size() == 1) {
                                                getDriver.setScaleX(0f);
                                                getDriver.setScaleY(0f);
                                                getDriver.setVisibility(View.VISIBLE);
                                                getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(2000);
                                            }
                                        } else {


                                        }
                                    }
                                }

                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            String drivercity1;
                            Log.w("locact","inovked childchanged");
                            amblocRefer = dataSnapshot.child("driverloc").child("l").getRef();
                            drivercity1=dataSnapshot.child("City").getValue().toString();
                            if(drivercity1.equals(userCity)) {
                                amblocRefer.addValueEventListener(ambloclistener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!driverassigned) {
                                            Log.w("" + dataSnapshot.getChildren(), " is the ref");
                                            String driverid = dataSnapshot.getRef().getParent().getParent().getKey();
                                            if ((dataSnapshot.child("0").getValue() != null) && (dataSnapshot.child("1").getValue() != null)) {
                                                double lat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                                                double lng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                                                double bearing=0;
                                                if(dataSnapshot.child("Bearing").getValue()!=null) {
                                                    bearing = Double.parseDouble(dataSnapshot.child("Bearing").getValue().toString());
                                                }

                                                    Log.w("on location change ", "" + lat + "&" + lng + "&" + bearing);
                                                    Point point = new Point(lng, lat, SpatialReferences.getWgs84());
                                                    userloc = Currentloc.getLocation().getPosition();
                                                    String driver = getdriverid(driverid);
                                                    if (driver != null) {
                                                        String i = driver.substring(8);
                                                        Log.w("driver id caught is ", driver);
                                                        Log.w("grahic we trynna get is", "Graphic" + i);
                                                        if (ambgraphics.get("Graphic" + i) != null) {

                                                            Point p1 = (Point) ambgraphics.get("Graphic" + i).getGeometry();
                                                            if(bearing!=0) {
                                                            boolean samepoint = issamepoint(p1, point);
                                                            if (!samepoint) {
                                                                driversloc.put(driverid, point);
                                                                ambgraphics.get("Graphic" + i).setGeometry(point);
                                                                MarkerSymbol marker = (MarkerSymbol) ambgraphics.get("Graphic" + i).getSymbol();
                                                                marker.setAngleAlignment(MarkerSymbol.AngleAlignment.MAP);
                                                                rotateMarker(marker, (float) bearing);
                                                                finalbearing = bearing;
                                                            } else {
                                                                MarkerSymbol marker = (MarkerSymbol) ambgraphics.get("Graphic" + i).getSymbol();
                                                                marker.setAngleAlignment(MarkerSymbol.AngleAlignment.MAP);
                                                                finalbearing = bearing;
                                                                rotateMarker(marker, (float) bearing);
                                                            }

                                                        }

                                                    } else {
                                                        driversOnline++;
                                                        try {

                                                            ambmarker[driversOnline] = PictureMarkerSymbol.createAsync(ambdraw).get();
                                                            ambmarker[driversOnline].setWidth(20);
                                                            ambmarker[driversOnline].setHeight(40);
                                                            ambmarker[driversOnline].loadAsync();

                                                        } catch (InterruptedException e) {
                                                            getActivity().finish();
                                                            e.printStackTrace();
                                                        } catch (ExecutionException e) {
                                                            getActivity().finish();
                                                            e.printStackTrace();
                                                        }
                                                        float bear = (float) bearing;
                                                        Driversonline.put("driverid" + driversOnline, dataSnapshot.getKey());
                                                        graphics[driversOnline] = new Graphic(point, ambmarker[driversOnline]);
                                                        mGraphicsOverlay.getGraphics().add(graphics[driversOnline]);
                                                        ambmarker[driversOnline].setAngleAlignment(MarkerSymbol.AngleAlignment.MAP);
                                                        rotateMarker(ambmarker[driversOnline], bear);
                                                        ambgraphics.put("Graphic" + driversOnline, graphics[driversOnline]);
                                                        driversloc.put(dataSnapshot.getKey(), point);


                                                        if (Driversonline.size() == 1) {


                                                            getDriver.setScaleX(0f);
                                                            getDriver.setScaleY(0f);
                                                            getDriver.setVisibility(View.VISIBLE);
                                                            getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(2000);


                                                        }


                                                    }
                                                }


                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }

                                });
                            }

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            if (!driverassigned) {

                                String driverid = dataSnapshot.getKey();
                                String driverkey = getdriverid(driverid);
                                String i = driverkey.substring(8);
                                Driversonline.remove(driverkey);
                                ambgraphics.remove("Graphic" + i);
                                mGraphicsOverlay.getGraphics().remove(graphics[Integer.parseInt(i)]);
                                for (String key : Driversonline.keySet()) {
                                    Log.w("after removal drivers", Driversonline.get(key));

                                }
                                Log.w("count of graphics", ambgraphics.size() + "");
                                Log.w("after removal count", "" + Driversonline.size());
                                Log.w("after removal amb count", "" + mGraphicsOverlay.getGraphics().size());
                                if (!driverassigned) {
                                    if (Driversonline.isEmpty()) {
                                        getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);

                                    }
                                }
                            }


                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                onstartstarted = true;
                onResume();
            }
        }
    }
   public String getdriverid(String driverid){

       for(String key:Driversonline.keySet()){
           String value= Driversonline.get(key);
           if(value.equals(driverid))
           {
               ID=key;
               Log.w("driver id got is:",key);
               break;
           }
           else {
               ID= null;
           }
       }
      return ID;
   }
   public boolean existeddriver(String driverid) {

       for (String key : Driversonline.keySet()) {
           String value = Driversonline.get(key);
           if (value.equals(driverid))
           {
               existeddriverresult= true;
               Log.w("duplicate user added","but rejected");
               break;
           } else {
               existeddriverresult= false;

           }


       }
       return existeddriverresult;

   }

   public  void removeambulances(int index){
       for (Map.Entry<String,String> entry:facilitiesdata.entrySet()){
           if(!(entry.getKey().equals("facility"+index))) {
               String driverid = entry.getValue();
               String driverkey = getdriverid(driverid);
               if(driverkey!=null){
                   String i = driverkey.substring(8);
                   Driversonline.remove(driverkey);
                   ambgraphics.remove("Graphic" + i);
                   mGraphicsOverlay.getGraphics().remove(graphics[Integer.parseInt(i)]);
                   Log.w("remvoed driver with "+entry.getKey(), "" + driverid + "&"+"facility"+index);
               }

           }
       }
   }


   public void checklocset(){
       LocationManager Locmanager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
       boolean gps_enabled =false;
       boolean network_enabled = false;Locmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);



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

       if(!gps_enabled && !network_enabled) {
           // notify user  //TODO USE OUR CUSTOMIZES DIALOG
           AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
           dialog.setMessage("TO CONTINUE USING THIS APPLICATION PLEASE TURN ON YOUR LOCATION SETTINGS");
           dialog.setPositiveButton("OPEN LOCATION SETTINGS", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                   Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                   startActivity(myIntent);
                   //get gps
               }
           });
           dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

               @Override
               public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                         locationbut.setVisibility(View.GONE);
                        getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
               }
           });
           dialog.create().show();
       }
   }

    private boolean isMarkerRotating ;
    private void rotateMarker(final MarkerSymbol marker, final float toRotation) {
        if(!isMarkerRotating) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getAngle();
            final long duration = 1000;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;

                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);

                    float rot = t * toRotation + (1 - t) * startRotation;

                    marker.setAngle(-rot > 180 ? rot / 2 : rot);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating = false;
                    }
                }
            });
        }
    }


   public boolean issamepoint(Point p1,Point point){

       return (p1.equals(point));

   }
   public void onconfirmed(){
        reference.removeEventListener(childlistener);
        if (amblocRefer != null) {
            amblocRefer.removeEventListener(ambloclistener);
        }
        driverassigned = true;
        Log.w("click", "instantiated");
        getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
        animationView.setVisibility(View.VISIBLE);
        animationView.loop(true);
        animationView.playAnimation();
        if (Driversonline.isEmpty()) {
            getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
            Sneaker.with(getActivity())
                    .setTitle("Alert!")
                    .setHeight(175)
                    .setMessage("No Nearby Ambulances Available")
                    .sneakWarning();

        } else {

                  displayroute();
        }
    }
    public void displayroute(){
        //drivers=new DriversAvailable(getApplicationContext());
        String routingservice = getResources().getString(R.string.routing_service);
        //create new closest facility task
        final ClosestFacilityTask closestFacilityTask = new ClosestFacilityTask(getActivity().getApplicationContext(), routingservice);
        //get default parameters
        final ListenableFuture<ClosestFacilityParameters> paramsFuture = closestFacilityTask.createDefaultParametersAsync();
        paramsFuture.addDoneListener(new Runnable() {

            @Override
            public void run() {
                try {
                    if (paramsFuture.isDone()) {
                        closestFacilityParameters = paramsFuture.get();
                        //create incident and facilities
                        List<Incident> incidents = new ArrayList<>();
                        userloc = Currentloc.getLocation().getPosition();
                        Log.w("lat of user at :", "" + userloc.getY());
                        Log.w("long of user at:", "" + userloc.getX());

                        if (userloc != null) {

                            Incident incidentpoint = new Incident(userloc);


                            incidents.add(incidentpoint);
                            closestFacilityParameters.setIncidents(incidents);
                            Log.w("added incident", "succesfully");
                        } else {
                            Log.w("failed to add", "incidents");
                        }

                    }


                    List<Facility> facilities = new ArrayList<>();
                    int i = 0;
                    for (Map.Entry<String, Point> entry : driversloc.entrySet()) {
                        Log.w("in driverloc key" + entry.getKey(), "value" + entry.getValue());
                        String uid = entry.getKey();
                        facilitiesdata.put("facility" + i, uid);
                        facilities.add(new Facility(entry.getValue()));
                        Log.w("added the facilities", "succesfully" + uid);
                        i++;
                    }
                    closestFacilityParameters.setFacilities(facilities);
                    for (Map.Entry<String, String> entry : facilitiesdata.entrySet()) {
                        Log.w("" + entry.getKey() + "is key for", "" + entry.getValue());
                    }


                    //now solve the closest route
                    final ListenableFuture<ClosestFacilityResult> resultfuture = closestFacilityTask.solveClosestFacilityAsync(closestFacilityParameters);
                    resultfuture.addDoneListener(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                closestResult = resultfuture.get();
                                if (closestResult != null) {
                                    closestindex = closestResult.getRankedFacilityIndexes(0).get(0);
                                    Log.w("" + closestindex, "is the length of route");
                                    final ClosestFacilityRoute closestroute = closestResult.getRoute(closestindex, 0);
                                    if (closestroute != null) {

                                        Log.w("got the  route", "hurray");
                                        //add the route to mapview
                                        Distance = Math.round((closestroute.getTotalLength() / 1000) * 10d);
                                        Distance = Distance / 10d;
                                        arrivaltime = Math.ceil(closestroute.getTravelTime());
                                        reference.removeEventListener(childlistener);
                                        getDriver.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(1000);
                                        final double Lat = userloc.getY();
                                        final double lng = userloc.getX();
                                        for (final Map.Entry<String, String> entry : facilitiesdata.entrySet()) {
                                            if ((entry.getKey().equals("facility" + closestindex))) {
                                                final String driverid = entry.getValue();
                                                final double driverlat=driversloc.get(entry.getValue()).getY();
                                                final double driverlng=driversloc.get(entry.getValue()).getX();
                                                final DatabaseReference refer = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Ambulance").child(driverid);
                                                refer.addChildEventListener(new ChildEventListener() {
                                                    boolean requested=false;
                                                    int childcount=0;
                                                    @Override
                                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                        refer.getRef().orderByChild("RequestStatus");
                                                        if(!requested){
                                                            if(dataSnapshot.getKey().equals("RequestStatus")){

                                                                Sneaker.with(getActivity())
                                                                        .setTitle("Error!")
                                                                        .setHeight(175)
                                                                        .setMessage("Requested driver is engaged, Please try again later")
                                                                        .sneakWarning();
                                                                refer.removeEventListener(this);
                                                                Fragment ambfag = new LocActivity();
                                                                FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                                                                transaction.replace(R.id.fragplaceholder, ambfag);
                                                                transaction.commit();


                                                            }
                                                            else if(!requested){
                                                                Log.w("in the confirm process",""+dataSnapshot.getKey()+"is the ref");
                                                                if(childcount==1){
                                                                    refer.child("RequestStatus").child("status").setValue("0");
                                                                    refer.child("RequestStatus").child("username").setValue(""+name); // todo add with shared prefs
                                                                    refer.child("RequestStatus").child("Phonenum").setValue(""+phonenum);//todo add with shared prefs
                                                                    requested=true;

                                                                }
                                                                childcount++;
                                                            }

                                                        }
                                                    }

                                                    @Override
                                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {


                                                        if( dataSnapshot.getKey().equals("RequestStatus")){


                                                        if(dataSnapshot.child("status").getValue().toString().equals("1")) {
                                                            Sneaker.with(getActivity())
                                                                    .setTitle("Sent Successfully!")
                                                                    .setHeight(155)
                                                                    .setMessage("Request successfully sent to the nearest fire station")
                                                                    .sneakSuccess();

                                                            driverassigned = true;
                                                            refer.removeEventListener(this);
                                                            reference.removeEventListener(childlistener);
                                                            DatabaseReference reference1 = refer.getParent().getParent().getParent().child("DriversEngaged").child("Ambulance").child(driverid);
                                                            reference1.child("status").setValue("tracking");
                                                            GeoFire geoFire = new GeoFire(reference1);
                                                            geoFire.setLocation("request", new GeoLocation(Lat, lng));
                                                            geoFire.setLocation("driverloctrack",new GeoLocation(driverlat,driverlng));
                                                            reference1.child("driverloctrack").child("l").child("Bearing").setValue(""+finalbearing);
                                                            reference1.child("request").child("UserPhonenum").setValue(""+phonenum);
                                                            refer.removeValue();
                                                            //update ui
                                                            animationView.clearAnimation();
                                                            animationView.setVisibility(View.GONE);
                                                            logo.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                                            Graphic routegraphic = new Graphic(closestroute.getRouteGeometry(), RouteSymbol);
                                                            mGraphicsOverlay.getGraphics().add(routegraphic);
                                                            locationbut.animate().setInterpolator(new AccelerateDecelerateInterpolator()).translationY(-40).scaleX(0.75f).scaleY(0.75f).translationX(25).setDuration(2000);
                                                            bottomlayout.setVisibility(View.VISIBLE);
                                                            bottomanimview.setScaleX(0f);
                                                            bottomanimview.setScaleY(0f);
                                                            bottomanimview.setVisibility(View.VISIBLE);
                                                            bottomanimview.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                                                            bottomanimview.playAnimation();
                                                            bottomanimview.loop(true);
                                                            getdriverdetails(driverid);
                                                            // TODO: 12-11-2017 update ui slide from bottom view


                                                        }
                                                        if(dataSnapshot.child("status").getValue().toString().equals("2")){

                                                            Sneaker.with(getActivity())
                                                                    .setTitle("Alert!")
                                                                    .setHeight(175)
                                                                    .setMessage("Requested driver is engaged.Please try again later")
                                                                    .sneakWarning();
                                                            refer.removeEventListener(this);
                                                            trackrefer.removeEventListener(trackinglistener);
                                                            Fragment ambfag = new LocActivity();
                                                            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                                                            transaction.replace(R.id.fragplaceholder, ambfag);
                                                            transaction.commit();
                                                        }

                                                        }

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
                                        }

                                        removeambulances(closestindex);

                                    }

                                }
                            } catch (Exception e) {


                                e.printStackTrace();
                                Sneaker.with(getActivity())
                                        .setTitle("Request Failed!")
                                        .setHeight(175)
                                        .setMessage("Please try again later")
                                        .sneakWarning();
                                Fragment ambfag = new LocActivity();
                                FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragplaceholder, ambfag);
                                transaction.commit();
                            }
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    Sneaker.with(getActivity())
                            .setTitle("Request Failed!")
                            .setHeight(175)
                            .setMessage("Please try again later")
                            .sneakWarning();
                    Fragment ambfag = new LocActivity();
                    FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragplaceholder, ambfag);
                    transaction.commit();
                }
            }
        });

    }

}
