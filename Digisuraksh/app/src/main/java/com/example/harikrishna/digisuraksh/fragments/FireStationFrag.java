package com.example.harikrishna.digisuraksh.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.airbnb.lottie.LottieAnimationView;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedEvent;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
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
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.irozon.sneaker.Sneaker;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import br.com.goncalves.pugnotification.notification.PugNotification;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by harikrishna on 18-10-2017.
 */

public class FireStationFrag extends Fragment {
    private final WrapAroundMode fullmap = WrapAroundMode.ENABLE_WHEN_SUPPORTED;
    LocationDisplay Currentloc;
    FirebaseAuth mAuth;
    PictureMarkerSymbol firemarker;
    Point stationloc;
    ClosestFacilityParameters closestFacilityParameters;
    DatabaseReference reference;
    Graphic[] stagraphic =new Graphic[100];
    double  firelat;
    double  firelong;
    int idresult;
    FrameLayout driverdetailslayout;
    GraphicsOverlay mgraphicoverlays;
    HashMap<String,Point> stationids=new HashMap<>();
    HashMap<String,Graphic> stationgraphics=new HashMap<>();
    HashMap<String,String> localfireids=new HashMap<>();
    HashMap<String,String> facilitiesdata = new HashMap<>();
    int scount=0;
    ClosestFacilityResult closestResult;
    boolean requestsent;
    Point userloc;
    SimpleLineSymbol RouteSymbol;
    int closestindex;
    FloatingActionButton locationbut;
    MapView mapView;
    MapScaleChangedListener mapScaleChangedListener;
    ChildEventListener firstlistener;
    Button sendreq;
    LottieAnimationView bottomanimview;
    String Stationcity;
    ImageView logo;
    TextView bottomlayouttextview;
    ImageView driverimage;
    TextView drivername;
    TextView Driverphonenum;
    TextView time;
    String userCity;
    String phonenum;
    String name;
    TextView distance;
    FrameLayout mapbaselayout;
    DatabaseReference driverRefer;
    String assigneddrivername;
    boolean driverassigned=false;
    String assigneddriverphonenum;
    ConstraintLayout bottomlayout;
    SlideUp slideUp;
    RelativeLayout slideupview;
    double Distance;
    double arrivaltime;
    LottieAnimationView progressanim;
    String requestedstationuid;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.firefrag,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        View v = getView();
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey), MODE_PRIVATE);
        userCity = pref.getString("Usercity", null);
        name=pref.getString("Username",null);
        phonenum=pref.getString("Phonenum",null);
        // get the map form the layout
        mapView = getActivity().findViewById(R.id.map);
        //create a map  layer and add to map view
        // adding the arcgis map
        sendreq = v.findViewById(R.id.locationtrack);
        locationbut=v.findViewById(R.id.floatloc);
        bottomanimview = v.findViewById(R.id.bottomanim);
        logo = v.findViewById(R.id.logo);
        slideupview=v.findViewById(R.id.slideview);
        driverdetailslayout=v.findViewById(R.id.driverdetailsbottomlayout);
        driverimage = v.findViewById(R.id.driverimage);
        drivername = v.findViewById(R.id.nameview);
        Driverphonenum = v.findViewById(R.id.phonenum);
        bottomlayout=v.findViewById(R.id.bottomlayout);
        bottomlayouttextview=v.findViewById(R.id.bottomlayouttextview);
        time = v.findViewById(R.id.clock);
        distance = v.findViewById(R.id.distance);
        mapbaselayout = getActivity().findViewById(R.id.fragplaceholder);
        progressanim=v.findViewById(R.id.lottieAnimationView);
        Currentloc = mapView.getLocationDisplay();
        Currentloc.startAsync();
        RouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK,5);
        BitmapDrawable fireicon = (BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.firestation);
        reference = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Fire");
        mgraphicoverlays = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(mgraphicoverlays);



        try {

            firemarker = PictureMarkerSymbol.createAsync(fireicon).get();
            firemarker.setWidth(30);
            firemarker.setHeight(30);
            firemarker.loadAsync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        mapScaleChangedListener = new MapScaleChangedListener() {
            @Override
            public void mapScaleChanged(MapScaleChangedEvent mapScaleChangedEvent) {
                if (mapView.getMapScale() > 350000) {
                    sendreq.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(1000);
                } else if (mapView.getMapScale() > 80000) {
                    mgraphicoverlays.setVisible(true);
                       firemarker.setWidth(25);
                       firemarker.setHeight(20);
                        if (!requestsent)
                            sendreq.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);



                } else if ((mapView.getMapScale() < 80000) && (mapView.getMapScale() > 50000)) {
                    mgraphicoverlays.setVisible(true);


                       firemarker.setWidth(20);
                       firemarker.setHeight(20);
                    if (!requestsent)
                        sendreq.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                } else if (mapView.getMapScale() < 50000) {
                   mgraphicoverlays.setVisible(true);


                       firemarker.setHeight(15);
                       firemarker.setWidth(15);
                    if (!requestsent)
                        sendreq.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                }
            }
        };
       // mapView.addMapScaleChangedListener(mapScaleChangedListener);
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
        driverdetailslayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(driverassigned){
                    slideUp.show();
                }
            }
        });

        View.OnClickListener buttonlistener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Drawable icon = getResources().getDrawable(R.drawable.ic_fire);
                new LovelyStandardDialog(getActivity())
                        .setTopColorRes(R.color.red)
                        .setButtonsColorRes(R.color.black)
                        .setIcon(icon)
                        .setTitle("Confirm!")
                        .setMessage("Are you sure want to confirm?")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendreq.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                progressanim.setVisibility(View.VISIBLE);
                                progressanim.playAnimation();
                                progressanim.loop(true);
                                sendreq.setEnabled(false);
                                // todo start request process
                                onconfirmed();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendreq.setEnabled(true);
                            }
                        })
                        .show();





            }
        };
        sendreq.setOnClickListener(buttonlistener);
        //get location updates
                Currentloc = mapView.getLocationDisplay();
        Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        locationbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(driverassigned){
                    Fragment firefag = new FireStationFrag();
                    FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragplaceholder, firefag);
                    transaction.commit();
                }
                else if(!driverassigned){
                    Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);

                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        mapView.getGraphicsOverlays().clear();
        sendreq.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
        mapView.animate().setInterpolator(new AccelerateDecelerateInterpolator()).translationY(0).setDuration(500);
        driverdetailslayout.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(1000);
        mapbaselayout.setBackgroundColor(getResources().getColor(R.color.grey));

    }

    public void displaydriverdetails(){
        Handler mhandler = new Handler();
        mhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                driverimage.setImageDrawable(getResources().getDrawable(R.drawable.ic_firefighter_svgrepo_com));
                bottomanimview.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(1000);
                driverdetailslayout.setScaleX(0f);
                driverdetailslayout.setScaleY(0f);
                driverdetailslayout.setVisibility(View.VISIBLE);
                driverdetailslayout.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                slideUp.show();

            }
        },4000);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!requestsent) {
            reference.addChildEventListener(firstlistener=new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    Log.w(s+"is the id ", "added");

                    try {
                        firelat = Double.parseDouble(dataSnapshot.child("stationloc").child("l").child("0").getValue().toString());
                        firelong = Double.parseDouble(dataSnapshot.child("stationloc").child("l").child("1").getValue().toString());
                        Stationcity=dataSnapshot.child("City").getValue().toString();
                        if(Stationcity.equals(userCity))
                        {
                            scount++;
                            String stationid = dataSnapshot.getKey();
                            localfireids.put("station" + scount, stationid);
                            stationloc = new Point(firelong, firelat, SpatialReferences.getWgs84());
                            stagraphic[scount] = new Graphic(stationloc, firemarker);
                            mgraphicoverlays.getGraphics().add(stagraphic[scount]);
                            stationids.put(stationid, stationloc);
                            stationgraphics.put(stationid, stagraphic[scount]);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    if(localfireids.size()==1){
                        sendreq.setScaleX(0f);
                        sendreq.setScaleY(0f);
                        sendreq.setVisibility(View.VISIBLE);
                        sendreq.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(2000);
                    }


                }


                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    String stationidtoberemoved = dataSnapshot.getKey();
                    int count = getstationcount(stationidtoberemoved);
                    mgraphicoverlays.getGraphics().remove(stagraphic[count]);
                    String keytoberemoved = "station" + count;
                    localfireids.remove(keytoberemoved);
                    stationids.remove(stationidtoberemoved);
                    stationgraphics.remove(stationidtoberemoved);


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

    public  int getstationcount(String key){

        for(Map.Entry<String,String> entry:localfireids.entrySet()){

             if(entry.getValue().equals(key)){

               idresult= Integer.parseInt(entry.getKey().substring(7));


            }

        }

        return idresult;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onStop() {
        super.onStop();
        mgraphicoverlays.getGraphics().clear();
    }
    public void removestationmarker(int closestindex){
        for (Map.Entry<String,String> entry:facilitiesdata.entrySet()){
            if(!(entry.getKey().equals("facility"+closestindex))) {
                String driverid = entry.getValue();
                int i=getstationcount(driverid);
                stationids.remove(driverid);
                stationgraphics.remove("Graphic" + i);
                mgraphicoverlays.getGraphics().remove(stagraphic[i]);
                Log.w("remvoed driver with "+entry.getKey(), "" + driverid + "&"+"facility"+closestindex);
            }
        }
    }
    public void onconfirmed(){
        if (localfireids.isEmpty()) {


            Sneaker.with(getActivity())
                    .setTitle("Alert!")
                    .setHeight(175)
                    .setMessage("No Nearby Fire Stations Available")
                    .sneakWarning();

        } else {


            // send the request to the fire station
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

                            Incident incidentpoint = new Incident(userloc);


                            incidents.add(incidentpoint);
                            closestFacilityParameters.setIncidents(incidents);
                            Log.w("added incident", "succesfully");
                        } else {
                            Log.w("failed to add", "incidents");
                        }
                        List<Facility> facilities = new ArrayList<>();
                        int i = 0;
                        for (Map.Entry<String, Point> entry : stationids.entrySet()) {
                            String uid = entry.getKey();
                            facilitiesdata.put("facility" + i, uid);
                            facilities.add(new Facility(entry.getValue()));
                            Log.w("added the facilities", entry.getValue().getY()+ "is lat&"+entry.getValue().getX()+"is long" );
                            i++;
                        }
                        closestFacilityParameters.setFacilities(facilities);
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
                                            // add request to the databse
                                            Distance = Math.round((closestroute.getTotalLength() / 1000) * 10d);
                                            Distance = Distance / 10d;
                                            arrivaltime = Math.ceil(closestroute.getTravelTime());
                                            for(Map.Entry<String,String> entry :facilitiesdata.entrySet()){
                                                if(entry.getKey().equals("facility"+closestindex)){
                                                     requestedstationuid=entry.getValue();
                                                    final double Lat = userloc.getY();
                                                    final double lng = userloc.getX();

                                                    final Thread requestthread= new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            final DatabaseReference refer = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Fire").child(requestedstationuid);
                                                            refer.addChildEventListener(new ChildEventListener() {
                                                                boolean flag=false;
                                                                int childcount=0;
                                                                @Override
                                                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                                    if(dataSnapshot.getKey().equals("RequestStatus")){
                                                                        if(!flag) {
                                                                        Sneaker.with(getActivity())
                                                                                .setTitle("Error!")
                                                                                .setHeight(175)
                                                                                .setMessage("Requested driver is engaged, Please try again later")
                                                                                .sneakWarning();
                                                                        refer.removeEventListener(this);
                                                                        Fragment firefag = new FireStationFrag();
                                                                        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                                                                        transaction.replace(R.id.fragplaceholder, firefag);
                                                                        transaction.commit();
                                                                       }

                                                                    }
                                                                    else if(!flag){

                                                                        refer.child("RequestStatus").child("status").setValue("0");
                                                                        refer.child("RequestStatus").child("username").setValue("" + name); // todo add with shared prefs
                                                                        refer.child("RequestStatus").child("Phonenum").setValue("" + phonenum);//todo add with shared prefs
                                                                        flag=true;
                                                                    }
                                                                    childcount++;
                                                                }

                                                                @Override
                                                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {


                                                                    if(dataSnapshot.getKey().equals("RequestStatus")){
                                                                        if(dataSnapshot.child("status").getValue().toString().equals("1")){
                                                                            Sneaker.with(getActivity())
                                                                                    .setTitle("Sent Successfully!")
                                                                                    .setHeight(155)
                                                                                    .setMessage("Request successfully sent to the nearest fire station")
                                                                                    .sneakSuccess();
                                                                            driverassigned=true;
                                                                            refer.removeEventListener(this);
                                                                            reference.removeEventListener(firstlistener);
                                                                            DatabaseReference reference1 = refer.getParent().getParent().getParent().child("DriversEngaged").child("Fire").child(requestedstationuid);
                                                                            reference1.child("status").setValue("tracking");
                                                                            GeoFire geoFire = new GeoFire(reference1);
                                                                            geoFire.setLocation("request", new GeoLocation(Lat, lng));
                                                                            reference1.child("request").child("UserPhonenum").setValue(""+phonenum);
                                                                            refer.child("RequestStatus").removeValue();
                                                                            //update ui
                                                                            progressanim.clearAnimation();
                                                                            progressanim.setVisibility(View.GONE);
                                                                            logo.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                                                            Graphic routegraphic = new Graphic(closestroute.getRouteGeometry(), RouteSymbol);
                                                                            mgraphicoverlays.getGraphics().add(routegraphic);
                                                                            bottomlayout.setVisibility(View.VISIBLE);
                                                                            bottomanimview.setScaleX(0f);
                                                                            bottomanimview.setScaleY(0f);
                                                                            bottomanimview.setVisibility(View.VISIBLE);
                                                                            bottomanimview.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                                                                            bottomanimview.playAnimation();
                                                                            bottomanimview.loop(true);
                                                                            getstationdetails(requestedstationuid);
                                                                            locationbut.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0.75f).scaleY(0.75f).translationY(-25).setDuration(2000);





                                                                        }
                                                                        if(dataSnapshot.child("status").getValue().toString().equals("2")){

                                                                            Sneaker.with(getActivity())
                                                                                    .setTitle("Alert!")
                                                                                    .setHeight(175)
                                                                                    .setMessage("Please try again later")
                                                                                    .sneakWarning();
                                                                            refer.removeEventListener(this);
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
                                                        public  void  getstationdetails(final String station){
                                                            driverRefer= FirebaseDatabase.getInstance().getReference("DriversReg").child("Fire");

                                                            driverRefer.addChildEventListener(new ChildEventListener() {
                                                                @Override
                                                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                                    Log.w(""+dataSnapshot,"is the refernece");

                                                                    Log.w("started getting","details");
                                                                    String id = dataSnapshot.child("Userid").getValue().toString();
                                                                    if (id.equals(station)) {
                                                                        driverRefer.removeEventListener(this);
                                                                        assigneddrivername = dataSnapshot.child("Username").getValue().toString();
                                                                        assigneddriverphonenum = dataSnapshot.child("Phonenum").getValue().toString();
                                                                        if(assigneddriverphonenum!=null && assigneddrivername!=null){

                                                                            driverRefer.removeEventListener(this);
                                                                            displaydriverdetails();
                                                                            drivername.setText(assigneddrivername);
                                                                            Driverphonenum.setText("     +91"+assigneddriverphonenum);
                                                                            distance.setText("     "+Distance+"km");
                                                                            time.setText("     "+(int)arrivaltime/60+"h "+(int)arrivaltime%60+"m");
                                                                            bottomlayouttextview.setText(assigneddrivername);
                                                                            Log.w("name",""+assigneddrivername);
                                                                            Log.w("phonenum",""+assigneddriverphonenum);
                                                                            Log.w("arrival time",""+(int)arrivaltime/60+"h"+(int)arrivaltime%60+"m");
                                                                            Log.w("total distance",""+Distance+"km");
                                                                            PugNotification.with(getActivity())
                                                                                    .load()
                                                                                    .title("DIGI SURAKSH")
                                                                                    .message("Fire Engine is on the way")
                                                                                    .smallIcon(R.drawable.icon)
                                                                                    .largeIcon(R.drawable.icon)
                                                                                    .flags(Notification.DEFAULT_ALL)
                                                                                    .simple()
                                                                                    .build();


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

                                                    });
                                                    requestthread.start();
                                                }
                                            }
                                            removestationmarker(closestindex);

                                        }

                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });


        }

    }

}
