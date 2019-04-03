package com.example.digidriver;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
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
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;
import com.esri.arcgisruntime.util.ListenableList;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
/**
 * Created by harikrishna on 21-10-2017.
 */

public class MapActivityAmb extends  AppCompatActivity{

    private final WrapAroundMode fullmap = WrapAroundMode.ENABLE_WHEN_SUPPORTED;
    private LocationDisplay Currentloc;
   private GraphicsOverlay mGraphicsOverlay;
    private DatabaseReference reference;
    private PictureMarkerSymbol ambmarker;
    private Point userloc;
    private String userid;
    private DatabaseReference targetrefer;
    private  LocationDisplay.LocationChangedListener Loclistener;
    public RouteTask mRouteTask;
    public Point mSourcePoint;
    public Point mDestinationPoint;
    public Route mRoute;
    public SimpleLineSymbol mRouteSymbol;
    private  boolean assigned=false;
    String address="";
    private String city="";
    Button button;
    private String[] reqpermissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    private int requestCode=2;
    private  String useraddress;
    private Point previousloc;
    private double bearing;
    private double previousbearing;
    private Drawable icon;
    private String reqname;
    private  String reqphonenum;
    private DatabaseReference trackrefer;
    private  Graphic routeGraphic;
    private Context context1;
    private ConstraintLayout gpslayout;
    private MaterialStyledDialog.Builder dialog;
    private boolean ready=false;
    private FloatingToolbar mtoolbar;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_amb);
        fab=findViewById(R.id.fab);
        mtoolbar=findViewById(R.id.floatingToolbar);
        mtoolbar.attachFab(fab);
        context1=MapActivityAmb.this;
        button=findViewById(R.id.reached);
        // get the map form the layout
        MapView mapView = findViewById(R.id.mapview);
        gpslayout=findViewById(R.id.gpslayout);
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
        try{
        userid=auth.getCurrentUser().getUid();
        }catch (Exception e){
            e.printStackTrace();
        }
        targetrefer = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Ambulance").child(userid);
        trackrefer= FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Ambulance").child(userid);
        // create a new graphics overlay and add it to the mapview
        mGraphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(mGraphicsOverlay);
        icon=getResources().getDrawable(R.drawable.ic_placeholder);

        //CHECK IF LOCATION IS BEING DISPLAYED AND SHOW THE BUTTONS
        checklocset();

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
                    Intent intent = new Intent(MapActivityAmb.this,OpenAct.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(MenuItem item) {

            }
        });


        //initialize database
        reference = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Ambulance");
        Log.w(""+reference.getKey(),"is the refernce");


        mRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 5);
        BitmapDrawable ambdraw = (BitmapDrawable) ContextCompat.getDrawable(this,R.drawable.marker); //todo fire
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

    public void onResume(){
        super.onResume();
        //Listen to changes in the loctaion data source
        Currentloc.addLocationChangedListener(Loclistener =new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
                previousloc=userloc;
                if(bearing!=0.00) {
                    previousbearing = bearing;
                }
                userloc=Currentloc.getLocation().getPosition();
                    String urlgeo = getResources().getString(R.string.geocode_services);
                    final LocatorTask geocodetask = new LocatorTask(urlgeo);

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
                                    city = citybuilder.toString();
                                    if(city!=null) {
                                        ready=true;
                                        gpslayout.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(1000);
                                        targetrefer.addChildEventListener(dialoglistener);
                                        fab.setVisibility(View.VISIBLE);
                                    }
                                    double Lat = userloc.getY();
                                    double lng = userloc.getX();
                                    if(userloc!=null&&previousloc!=null) {
                                        bearing = bearingBetweenLocations(previousloc, userloc);
                                    }
                                    if(ready) {
                                        final DatabaseReference refer = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Ambulance").child(userid);
                                        //Toast.makeText(MapActivityAmb.this,refer+"is the reference",Toast.LENGTH_SHORT).show();
                                        if (!assigned) {
                                            GeoFire geoFire = new GeoFire(refer);
                                            geoFire.setLocation("driverloc", new GeoLocation(Lat, lng));
                                            refer.child("City").setValue("" + city);
                                            if (bearing != 0.00) {
                                                refer.child("driverloc").child("l").child("Bearing").setValue(bearing);
                                            } else {

                                                refer.child("driverloc").child("l").child("Bearing").setValue(previousbearing);
                                            }

                                        } else if (assigned) {
                                            trackrefer = FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Ambulance").child(userid);
                                            GeoFire geoFire = new GeoFire(trackrefer);
                                            geoFire.setLocation("driverloctrack", new GeoLocation(Lat, lng));
                                            trackrefer.child("driverloctrack").child("l").child("Bearing").setValue("" + bearing);


                                        }
                                    }


                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                 Thread firstthread = new Thread(new Runnable() {
                     @Override
                     public void run() {

                     }
                     });
                 firstthread.start();
                 }

        });









    }
    public void onStart(){
        super.onStart();
        if (!isdeviceconnected(this)) {
            connecttonetwork();
        }
        checkloc();
    }
    public boolean isdeviceconnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return (network != null && (network.isConnectedOrConnecting()));
    }
    public void connecttonetwork(){

        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public String getuseraddress(Point point){


            return useraddress;

    }
    public void onStop(){
        super.onStop();
        Currentloc.removeLocationChangedListener(Loclistener);
        FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Ambulance").child(userid).removeValue();


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
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapActivityAmb.this);
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
                       Log.w("is the total length",""+mRoute.getTotalLength()) ;
                        routeGraphic = new Graphic(mRoute.getRouteGeometry(), mRouteSymbol);
                        mGraphicsOverlay.getGraphics().add(routeGraphic);
                        String urlgeo = getResources().getString(R.string.geocode_services);
                        final LocatorTask geocodetask = new LocatorTask(urlgeo);

                        final ListenableFuture<List<GeocodeResult>> geocodeResultListenableFuture =geocodetask.reverseGeocodeAsync(mDestinationPoint);
                        geocodeResultListenableFuture.addDoneListener(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    List<GeocodeResult> geocodeResults=geocodeResultListenableFuture.get();
                                    if(!(geocodeResults.isEmpty())){
                                        geocodeResultListenableFuture.removeDoneListener(this);
                                        StringBuilder adressbuilder = new StringBuilder();
                                        for(GeocodeResult temp:geocodeResults){


                                            // adressbuilder = adressbuilder.append(temp.getAttributes());
                                            //Log.w(""+temp.getLabel(),"is inside string");
                                            Map stringMap = temp.getAttributes();

                                            adressbuilder.append(String.format("%s\n%s, %s ",
                                                    stringMap.get("Address"), stringMap.get("City"),
                                                    stringMap.get("Region") ));


                                        }
                                        useraddress=adressbuilder.toString();
                                        Log.w(""+useraddress,"is the addres of user");


                                    }


                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                TextSymbol textSymbol = new TextSymbol(15,useraddress,R.color.black,TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.TOP);
                                textSymbol.setFontWeight(TextSymbol.FontWeight.BOLD);
                                textSymbol.setFontStyle(TextSymbol.FontStyle.ITALIC);
                                textSymbol.setOffsetY(50);

                                Graphic markergraphic = new Graphic(mDestinationPoint,ambmarker);
                                Graphic addgraphic=new Graphic(mDestinationPoint,textSymbol);
                                // add mRouteSymbol graphic to the map
                                mGraphicsOverlay.getGraphics().add(markergraphic);
                                mGraphicsOverlay.getGraphics().add(addgraphic);
                            }
                        });


                        //assigned=true;
                        button.setVisibility(View.VISIBLE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DatabaseReference refer1=FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Ambulance").child(userid).child("status");
                                refer1.setValue("stop");
                                button.setVisibility(View.GONE);
                                Intent intent = new Intent(MapActivityAmb.this,MapActivityAmb.class);
                                startActivity(intent);
                            }
                        });

                    }



                } catch (Exception e) {
                    e.printStackTrace();

                    e.getMessage();

                }
            }
        });


    }
    private double bearingBetweenLocations(Point latLng1,Point latLng2) {
        double brng;

        double PI = 3.14159;
        double lat1 = latLng1.getY() * PI / 180;
        double long1 = latLng1.getX() * PI / 180;
        double lat2 = latLng2.getY() * PI / 180;
        double long2 = latLng2.getX() * PI / 180;
        double dLon = (long2 - long1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);
        brng = Math.atan2(y, x);
        brng = Math.toDegrees(brng);
        return brng;
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
    ChildEventListener dialoglistener=new ChildEventListener() {
        boolean flag=false;
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.w(""+dataSnapshot.getKey(),"is the first ref");
            if(!flag){
            if(dataSnapshot.hasChild("status")) {
                if ((dataSnapshot.child("status").getValue().toString().equals("0"))) {
                    try {
                        flag=true;
                        Log.w("" + dataSnapshot.child("status").getValue().toString(), "is the ref");
                        targetrefer.removeEventListener(this);
                        reqname = dataSnapshot.child("username").getValue().toString();
                        reqphonenum = dataSnapshot.child("Phonenum").getValue().toString();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Build a dialog box
                    dialog=new MaterialStyledDialog.Builder(context1)
                            .setTitle("Emergency!!!")
                            .setDescription("In Coming Request Are you sure want to confirm?")
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
                                    trackrefer.addChildEventListener(tracklistener);
                                    assigned=true;


                                }

                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    //do nothing to do

                                    targetrefer.child("RequestStatus").child("status").setValue("2");
                                    reqname = null;
                                    reqphonenum = null;
                                    Intent loc = new Intent(MapActivityAmb.this, MapActivityAmb.class);
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
    ChildEventListener tracklistener =new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.w("CHILD","ADDED");
            if(dataSnapshot.getKey().equals("request")) {
                    trackrefer.removeEventListener(this);
                    try {
                        double lat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                        double longi = Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                        mDestinationPoint = new Point(longi, lat, SpatialReferences.getWgs84());
                        trackrefer.removeEventListener(tracklistener);
                        useraddress = getuseraddress(mDestinationPoint);
                        if(mDestinationPoint!=null){
                            routefun();
                        }
                        double Lat = userloc.getY();
                        double lng = userloc.getX();
                        if(userloc!=null&&previousloc!=null) {
                            bearing = bearingBetweenLocations(previousloc, userloc);
                        }
                        trackrefer= FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Ambulance").child(userid);
                        GeoFire geoFire = new GeoFire(trackrefer);
                        geoFire.setLocation("driverloctrack", new GeoLocation(Lat, lng));
                        trackrefer.child("driverloctrack").child("l").child("Bearing").setValue(""+bearing);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }



                Log.w("CHILD","got");
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


}
