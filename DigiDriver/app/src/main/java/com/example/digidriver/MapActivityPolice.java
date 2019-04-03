package com.example.digidriver;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointBuilder;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.github.rubensousa.floatingtoolbar.FloatingToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by harikrishna on 21-10-2017.
 */

public class MapActivityPolice extends AppCompatActivity {
    private final WrapAroundMode fullmap = WrapAroundMode.ENABLE_WHEN_SUPPORTED;
    LocationDisplay Currentloc;
    GraphicsOverlay mGraphicsOverlay;
    DatabaseReference reference,trackrefer;
    ChildEventListener userloctracklistener;
    PictureMarkerSymbol ambmarker;
    Point userloc;
    String driveruid;
    int requestCode=2;
    Drawable icon;
    DatabaseReference targetrefer;
    String policeCity,address;
    boolean startflag;
    PointCollection points;
    PolylineBuilder polylineBuilder;
    Polyline userpolyline;
    LocationDisplay.LocationChangedListener Loclistener;
    ConstraintLayout gpslayout;
    public RouteTask mRouteTask;
    String reqname,reqphonenum;
    public Point mSourcePoint;
    public Point mDestinationPoint;
    public Route mRoute;
    public SimpleLineSymbol mRouteSymbol;
    Graphic markergraphic;
    private FloatingToolbar mtoolbar;
    private FloatingActionButton fab;
    MaterialStyledDialog.Builder dialog;
    double userlat,userlong;
    Point point;
    SimpleLineSymbol simpleLineSymbol= new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK,5);
    Graphic polylinegraphic;
    boolean assigned=false;
    String[] reqpermissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_police);
        mtoolbar=findViewById(R.id.floatingToolbar);
        fab=findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        mtoolbar.attachFab(fab);
        points= new PointCollection(SpatialReferences.getWgs84());

        // get the map form the layout
        MapView mapView = findViewById(R.id.mapview);
        //create a map  layer and add to map view
        // adding the arcgis map
        ArcGISMap arcMap = new ArcGISMap(Basemap.createOpenStreetMap());
        mapView.setWrapAroundMode(fullmap);
        gpslayout=findViewById(R.id.gpslayout);
        mapView.setMap(arcMap);
        Currentloc = mapView.getLocationDisplay();
        checkloc();
        Currentloc.startAsync();
        Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        //get firebase user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        driveruid=auth.getCurrentUser().getUid();
        targetrefer = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Police").child(driveruid);

        // create a new graphics overlay and add it to the mapview
        mGraphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(mGraphicsOverlay);

        icon=getResources().getDrawable(R.drawable.ic_placeholder);
        //CHECK IF LOCATION IS BEING DISPLAYED AND SHOW THE BUTTONS
        checklocset();




        //initialize database
        reference = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Police");
        Log.w(""+reference.getKey(),"is the refernce");

        mtoolbar.setClickListener(new FloatingToolbar.ItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                if(item.getItemId()==R.id.faboptions_location){
                    Currentloc.startAsync();
                    Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                }
                if(item.getItemId()==R.id.faboptions_signout){
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                    preferences.edit().clear().apply();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MapActivityPolice.this,OpenAct.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(MenuItem item) {

            }
        });

        mRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 7);
        BitmapDrawable ambdraw = (BitmapDrawable) ContextCompat.getDrawable(this,R.drawable.sniper); //todo fire
        try {
            ambmarker = PictureMarkerSymbol.createAsync(ambdraw).get();
            ambmarker.setWidth(20);
            ambmarker.setHeight(20);
            ambmarker.loadAsync();

        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        catch (ExecutionException e){
            e.printStackTrace();
        }
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
                                Map stringMap = temp.getAttributes();

                                adressbuilder.append(String.format("%s\n%s, %s ",
                                        stringMap.get("Address"), stringMap.get("City"),
                                        stringMap.get("Region")));
                                citybuilder.append(String.format("%s", stringMap.get("City")));


                            }
                            address = adressbuilder.toString();
                            policeCity = citybuilder.toString();
                            Log.w("BasenAV", policeCity + "is the user's city");
                            if (policeCity != null && (!startflag)) {
                                startflag = true;
                                SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("policeCity", "" + policeCity);
                                editor.apply();
                                gpslayout.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                onStart();
                                fab.setVisibility(View.VISIBLE);

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
    public void onResume(){
        super.onResume();

        //Listen to changes in the loctaion data source
        Currentloc.addLocationChangedListener(Loclistener =new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
                if(!assigned)
                {
                userloc = Currentloc.getLocation().getPosition();
                double Lat = userloc.getY();
                double lng = userloc.getX();
                DatabaseReference refer = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Police").child(driveruid);
                GeoFire geoFire = new GeoFire(refer);
                geoFire.setLocation("driverloc", new GeoLocation(Lat, lng));
                refer.child("City").setValue(policeCity);
            }
            }
        });









    }
    public void onStart(){
        super.onStart();
        targetrefer.addChildEventListener(new ChildEventListener() {
            int dialogshown=0;
            boolean flag=false;
            boolean dialogflag=false;
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                    Log.w(""+s,"is the first ref");
                        if(!flag){
                        if(dataSnapshot.hasChild("status")) {
                            if ((dataSnapshot.child("status").getValue().toString().equals("0"))) {
                                try {
                                    if(!flag)
                                    flag=true;
                                    else if(flag)
                                        flag=false;
                                    dialogshown++;
                                    targetrefer.removeEventListener(this);
                                    Log.w("" + dataSnapshot.getKey(), "is the ref");
                                    if(dataSnapshot.hasChild("username")&& dataSnapshot.hasChild("Phonenum")) {
                                        reqname = dataSnapshot.child("username").getValue().toString();
                                        reqphonenum = dataSnapshot.child("Phonenum").getValue().toString();
                                        // todo not getting the name
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //Build a dialog box
                                dialog=new MaterialStyledDialog.Builder(MapActivityPolice.this)
                                        .setTitle("Emergency!!!")
                                        .setDescription("In Coming Request From " + reqname + " \n Are you sure want to confirm?")
                                        .setCancelable(false)
                                        .setStyle(Style.HEADER_WITH_ICON)
                                        .setHeaderColor(R.color.green)
                                        .withDarkerOverlay(true)
                                        .withDialogAnimation(true)
                                        .withIconAnimation(true)
                                        .setIcon(icon)
                                        .setPositiveText("yes")
                                        .setNegativeText("No")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                targetrefer.child("RequestStatus").child("status").setValue("1");
                                                Currentloc.removeLocationChangedListener(Loclistener);
                                                assigned=true;
                                                addlistenertotrack();
                                            }

                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                //do nothing to do

                                                targetrefer.child("RequestStatus").child("status").setValue("2");
                                                reqname = null;
                                                reqphonenum = null;
                                                Intent loc = new Intent(MapActivityPolice.this, MapActivityPolice.class);
                                                startActivity(loc);
                                                //refresh activity todo
                                            }
                                        });
                                if((!dialogflag&&dialogshown==1)&&flag) {
                                    dialog.show();
                                    dialogflag=true;
                                }

                            }
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



    public void onStop(){
        super.onStop();
        Currentloc.removeLocationChangedListener(Loclistener);

    }
    public void checklocset () {
        LocationManager Locmanager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        Locmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        try {
            gps_enabled = Locmanager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            network_enabled = Locmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapActivityPolice.this);
            dialog.setMessage("TO CONTINUE USING THIS APPLICATION PLEASE TURN ON YOUR LOCATION SETTINGS");
            dialog.setPositiveButton("OPEN LOCATION SETTINGS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {


                }
            });
            dialog.create().show();
        }

    }
     public void addlistenertotrack(){
        trackrefer=FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Police").child(driveruid);
        Thread userloctrackthread = new Thread(new Runnable() {
            @Override
            public void run() {
                trackrefer.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //todo get the user loc and update the polyline
                        Log.w("the datasnapshot is",""+dataSnapshot.getKey());
                        if(dataSnapshot.getKey().equals("request")){
                            double userlat,userlong;
                            try {
                                userlat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                                userlong= Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                                mDestinationPoint= new Point(userlong,userlat,SpatialReferences.getWgs84());
                                points.add(mDestinationPoint);
                                polylineBuilder=new PolylineBuilder(points,SpatialReferences.getWgs84());
                                userpolyline=polylineBuilder.toGeometry();
                                markergraphic = new Graphic(mDestinationPoint,ambmarker);
                                mGraphicsOverlay.getGraphics().add(markergraphic);
                                polylinegraphic=new Graphic(userpolyline,simpleLineSymbol);
                                mGraphicsOverlay.getGraphics().add(polylinegraphic);

                            }catch (Exception e){

                            }
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        userlat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                        userlong= Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                        point= new Point(userlong,userlat,SpatialReferences.getWgs84());
                        markergraphic.setGeometry(point);
                        points.add(point);
                        polylineBuilder.addPart(points);
                        userpolyline=polylineBuilder.toGeometry();
                        polylinegraphic.setGeometry(userpolyline);


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
        userloctrackthread.start();
        
        
        
        
     }

    public void checkloc(){
        boolean fineperm = ContextCompat.checkSelfPermission(this,reqpermissions[0])== PackageManager.PERMISSION_GRANTED;
        boolean coarseperm = ContextCompat.checkSelfPermission(this,reqpermissions[1])== PackageManager.PERMISSION_GRANTED;
        // check if the permissions are not given and req the user for permissions
        Log.e("FINE",""+fineperm);
        Log.e("coarse",""+coarseperm);
        // get the status of our permissions
        if(!(fineperm&&coarseperm))
        {
            ActivityCompat.requestPermissions(this, reqpermissions, requestCode);
            Log.e("permission req", "onStatusChanged: ");
        }

    }


}
