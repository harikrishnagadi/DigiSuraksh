package com.example.digidriver;

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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * Created by harikrishna on 20-10-2017.
 */




    public class MapActivityFire extends AppCompatActivity {
        private final WrapAroundMode fullmap = WrapAroundMode.ENABLE_WHEN_SUPPORTED;
        LocationDisplay Currentloc;
        GraphicsOverlay mGraphicsOverlay;
        DatabaseReference reference;
        PictureMarkerSymbol ambmarker;
        Point userloc;
        String userid;
        DatabaseReference targetrefer,realtracking;
        LocationDisplay.LocationChangedListener Loclistener;
        TextView stationsetuptextview;
        ConstraintLayout gpslayout,dummygpslayout;
        private String fireCity,address;
        private boolean startflag;
        public RouteTask mRouteTask;
        public Point mSourcePoint;
        public Point mDestinationPoint;
        public Route mRoute;
        ChildEventListener dialoglistener;
        private FloatingActionButton fab;
        private FloatingToolbar mtoolbar;
        public SimpleLineSymbol mRouteSymbol;
        private MaterialStyledDialog.Builder dialog;
        String reqname,reqphonenum;
        Drawable icon;
        public int count=1;
        private boolean newdriver;
    String[] reqpermissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    int requestCode=2;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
            newdriver=preferences.getBoolean("newdriver",false);
            Log.w("the answer for new user",""+newdriver);
            setContentView(R.layout.activity_map_fire);
            mtoolbar=findViewById(R.id.floatingToolbar);
            fab=findViewById(R.id.fab);
            gpslayout=findViewById(R.id.gpslayout);
            mtoolbar.attachFab(fab);
            // get the map form the layout
            MapView mapView = findViewById(R.id.mapview);
            //create a map  layer and add to map view
            // adding the arcgis map
            ArcGISMap arcMap = new ArcGISMap(Basemap.createOpenStreetMap());
            mapView.setWrapAroundMode(fullmap);

            mapView.setMap(arcMap);
            Currentloc = mapView.getLocationDisplay();
            checkloc();
            Currentloc.startAsync();
            Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
            //get firebase user
            FirebaseAuth auth = FirebaseAuth.getInstance();
            userid=auth.getCurrentUser().getUid();
            targetrefer = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Fire").child(userid);
            realtracking=FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Fire").child(userid);

            // create a new graphics overlay and add it to the mapview
            mGraphicsOverlay = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(mGraphicsOverlay);
            icon=getResources().getDrawable(R.drawable.ic_placeholder);


            //CHECK IF LOCATION IS BEING DISPLAYED AND SHOW THE BUTTONS
            checklocset();
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
                                fireCity = citybuilder.toString();
                                Log.w("BasenAV", fireCity + "is the user's city");
                                if (fireCity != null && (!startflag)) {
                                    startflag = true;
                                    SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("policeCity", "" +fireCity);
                                    editor.apply();
                                    gpslayout.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                    fab.setVisibility(View.VISIBLE);
                                    Startstationloc();

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




            //initialize database
            reference = FirebaseDatabase.getInstance().getReference("DriversAvailable");
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
                        Intent intent = new Intent(MapActivityFire.this,OpenAct.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onItemLongClick(MenuItem item) {

                }
            });


            mRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 7);
            BitmapDrawable ambdraw = (BitmapDrawable) ContextCompat.getDrawable(this,R.drawable.tinder); //todo fire
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





        }
        public void Startstationloc(){

            if(newdriver)
            {
                //Listen to changes in the loctaion data source
                Currentloc.addLocationChangedListener(Loclistener =new LocationDisplay.LocationChangedListener() {
                    @Override
                    public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
                        if (count == 1) {
                            Log.w("new user ","is invked");
                            userloc = Currentloc.getLocation().getPosition();
                            double Lat = userloc.getY();
                            double lng = userloc.getX();
                            DatabaseReference userref = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Fire").child(userid);
                            userref.child("City").setValue(fireCity);
                            DatabaseReference refer = FirebaseDatabase.getInstance().getReference("DriversReg").child("Fire").child(userid);
                            GeoFire geoFire = new GeoFire(refer);
                            geoFire.setLocation("stationloc", new GeoLocation(Lat, lng));
                            GeoFire geoFire1 = new GeoFire(userref);
                            geoFire1.setLocation("stationloc",new GeoLocation(Lat,lng));
                            refer.child("City").setValue(fireCity);
                            targetrefer.addChildEventListener(dialoglistener);
                            Log.w("added new user ","to database");
                            count++;
                            SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean("newdriver", false);
                            editor.apply();
                        }

                    }
                });


            }
            else{

                DatabaseReference stationlocrefer = FirebaseDatabase.getInstance().getReference("DriversReg").child("Fire").child(userid);
                stationlocrefer.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.getKey().equals("stationloc")) {
                            Log.w("old user ","is invked");
                            try {

                                double userlat, userlong;
                                userlat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                                userlong = Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                                DatabaseReference userref = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Fire").child(userid);
                                GeoFire geoFire = new GeoFire(userref);
                                userref.child("City").setValue(fireCity);
                                geoFire.setLocation("stationloc", new GeoLocation(userlat, userlong));
                                targetrefer.addChildEventListener(dialoglistener);


                            } catch (Exception e) {

                            }
                        }
                        else if(dataSnapshot.getKey().equals("City")){
                            String stationcity;
                            try {
                                stationcity = dataSnapshot.getValue().toString();
                                DatabaseReference userref = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Fire").child(userid);
                                userref.child("City").setValue(stationcity);

                            }catch (Exception e){
                                e.printStackTrace();
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



        }
        public void onResume(){
            super.onResume();
            dialoglistener=new ChildEventListener() {
                boolean flag=false;
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.w(""+dataSnapshot.getKey(),"is the first ref");
                    if(!flag){
                        if(dataSnapshot.hasChild("status")) {
                            if ((dataSnapshot.child("status").getValue().toString().equals("0"))) {
                                try {
                                    targetrefer.removeEventListener(this);
                                    flag=true;
                                    Log.w("" + dataSnapshot.child("status").getValue().toString(), "is the ref");
                                    if(dataSnapshot.child("username").getValue()!=null&& dataSnapshot.child("Phonenum").getValue()!=null) {
                                         reqname = dataSnapshot.child("username").getValue().toString();
                                        reqphonenum = dataSnapshot.child("Phonenum").getValue().toString();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                dialog=new MaterialStyledDialog.Builder(MapActivityFire.this)
                                        .setTitle("Emergency!!!")
                                        .setDescription("In Coming Request From " + reqname + " \n Are you sure want to confirm?")
                                        .setStyle(Style.HEADER_WITH_ICON)
                                        .setHeaderColor(R.color.green)
                                        .withDarkerOverlay(true)
                                        .withDialogAnimation(true)
                                        .withIconAnimation(true)
                                        .setIcon(icon)
                                        .setCancelable(false)
                                        .setPositiveText("yes")
                                        .setNegativeText("No")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                targetrefer.child("RequestStatus").child("status").setValue("1");
                                                realtracking.addChildEventListener(reallocationlistener);



                                            }

                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                //do nothing to do

                                                targetrefer.child("RequestStatus").child("status").setValue("2");
                                                reqname = null;
                                                reqphonenum = null;
                                                Intent loc = new Intent(MapActivityFire.this, MapActivityFire.class);
                                                startActivity(loc);
                                                //refresh activity todo


                                            }
                                        });



                                dialog.show();


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
            };


        }
        public void onStart(){
            super.onStart();
           // targetrefer.addChildEventListener(dialoglistener); todo


        }


        public void onStop(){
            super.onStop();
            if(Loclistener!=null) {
                Currentloc.removeLocationChangedListener(Loclistener);
            }

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
                AlertDialog.Builder dialog = new AlertDialog.Builder(MapActivityFire.this);
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


       ChildEventListener reallocationlistener=new ChildEventListener() {
           @Override
           public void onChildAdded(DataSnapshot dataSnapshot, String s) {


               //Toast.makeText(MapActivityFire.this,dataSnapshot.getKey()+"is the request key",Toast.LENGTH_LONG).show();
               if(dataSnapshot.getKey().equals("request")) {
                   realtracking.removeEventListener(this);

                   double lat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                   double longi = Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                   mDestinationPoint = new Point(longi, lat, SpatialReferences.getWgs84());
                   routefun();
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
       };

        public void routefun() {

            getResources().getString(R.string.routing_service);
            // create RouteTask instance
            mRouteTask = new RouteTask(getApplicationContext()  ,getString(R.string.routing_service));
            mRouteTask.loadAsync();
            mRouteTask.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {

                    try
                    {

                        RouteParameters mRouteParams =  mRouteTask.createDefaultParametersAsync().get();
                        // create stops

                        mSourcePoint=Currentloc.getLocation().getPosition();
                        Stop stop1 = new Stop(mSourcePoint);
                        Stop stop2 = new Stop(mDestinationPoint);
                        ArrayList<Stop> routeStops = new ArrayList<>();
                        // add stops
                        routeStops.add(stop1);
                        routeStops.add(stop2);
                        mRouteParams.setStops(routeStops);
                        mRouteParams.setReturnDirections(false);

                        // solve

                        RouteResult routeResult = (mRouteTask.solveRouteAsync(mRouteParams).get());
                        List LocalList = routeResult.getRoutes();
                        if(LocalList!=null){
                            mRoute=(Route)LocalList.get(0);

                            Graphic routeGraphic = new Graphic(mRoute.getRouteGeometry(), mRouteSymbol);
                            Graphic markergraphic = new Graphic(mDestinationPoint,ambmarker);
                            // add mRouteSymbol graphic to the map
                            mGraphicsOverlay.getGraphics().add(routeGraphic);
                            mGraphicsOverlay.getGraphics().add(markergraphic);

                        }



                    } catch (Exception e) {
                        e.printStackTrace();

                        e.getMessage();

                    }
                }
            });


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



