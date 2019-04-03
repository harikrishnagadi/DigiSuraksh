package com.example.harikrishna.digisuraksh.fragments;





import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.irozon.sneaker.Sneaker;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.goncalves.pugnotification.notification.PugNotification;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by harikrishna on 18-10-2017.
 */

public class PoliceFrag extends Fragment {
    private final WrapAroundMode fullmap = WrapAroundMode.ENABLE_WHEN_SUPPORTED;
    boolean isLivetrackingreq=false;
    LocationDisplay Currentloc;
    boolean requestsent;
    boolean tracking_flag=false;
    GraphicsOverlay graphicsOverlay;
    SimpleLineSymbol simpleLineSymbol;
    Polyline polyline;
    PolylineBuilder polylineBuilder;
    int count=0;
    PointCollection points;
    DatabaseReference reference;
    Graphic graphic;
    ChildEventListener policelistener,policeengagedlistener;
    HashMap<String,String> policeavailable = new HashMap<>();
    boolean existedpoliceresult;
    double policelat;
    double policelongi;
    int policeonline;
    HashMap<String,Point> policeloc =new HashMap<>();
    Map<String,String> facilitiesdata= new HashMap<>();
    Point userloc;
    ClosestFacilityParameters closestFacilityParameters;
    int closestindex;
    boolean addedtodb=false;
    ClosestFacilityResult closestResult;
    DatabaseReference referforrealtime;
    MapView mapView;
    Button locationtrackbut;
    private String name;
    private String userCity;
    private String phonenum;
    private String policecity;
    private ImageView logo;
    FloatingActionButton locbutton;
    private LottieAnimationView animationView,bottomanimview;
    private String assignedpolicename;
    private String assignedpolicephonenum;
    LocationDisplay.LocationChangedListener loclistener;
    TextView bottomlayouttextview;
    ConstraintLayout bottomlayout;
    TextView trackview;
    Context context;
    private TextView time,distance,policename,policephonenum;
    double Distance,arrivaltime;
    private  DatabaseReference policeRefer,policeEngagedrefer;
    private ImageView policelogo;
    private FrameLayout bottomframelayout;
    private SlideUp slideUp;
    private View slideupview;
    private BitmapDrawable ambdraw;
    private PictureMarkerSymbol startmarker;
    private Drawable polislog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.policefrag,container,false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        context=getActivity();

        int i = 0;
        Log.w("times:", "" + i++);
        View v = getView();
        polislog= getResources().getDrawable(R.drawable.ic_police);
        // get the map form the layout
        mapView = getActivity().findViewById(R.id.map);
        //create a map  layer and add to map view
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey), MODE_PRIVATE);
        userCity = pref.getString("Usercity", null);
        name = pref.getString("Username", null);
        phonenum = pref.getString("Phonenum", null);
        Currentloc = mapView.getLocationDisplay();
        Currentloc.startAsync();
        graphicsOverlay = new GraphicsOverlay();
        bottomanimview = v.findViewById(R.id.bottomanim);
        bottomlayout = v.findViewById(R.id.bottomlayout);
        bottomlayouttextview = v.findViewById(R.id.bottomlayouttextview);
        bottomframelayout = v.findViewById(R.id.driverdetailsbottomlayout);
        mapView.getGraphicsOverlays().add(graphicsOverlay);
        points = new PointCollection(SpatialReferences.getWgs84());
        simpleLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 5);
        reference = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Police");
        locationtrackbut = v.findViewById(R.id.locationtrack);
        locbutton = v.findViewById(R.id.floatloc);
        logo = v.findViewById(R.id.logo);
        time = v.findViewById(R.id.clock);
        distance = v.findViewById(R.id.distance);
        policename = v.findViewById(R.id.nameview);
        policephonenum = v.findViewById(R.id.phonenum);
        policelogo = v.findViewById(R.id.driverimage);
        trackview = v.findViewById(R.id.track);
        slideupview = v.findViewById(R.id.slideview);
        animationView = v.findViewById(R.id.lottieAnimationView);
        ambdraw = (BitmapDrawable) ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.marker);
        try {

            startmarker = PictureMarkerSymbol.createAsync(ambdraw).get();
            startmarker.setHeight(20);
            startmarker.setWidth(20);
            startmarker.loadAsync();

        } catch (Exception e) {
            getActivity().finish();
            e.printStackTrace();
        }
            locbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isLivetrackingreq) {
                        Currentloc.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                        Currentloc.startAsync();
                    } else if (isLivetrackingreq) {
                        Log.w("entering this", "block");
                        slideUp.show();
                    }
                }
            });
            loclistener = new LocationDisplay.LocationChangedListener() {
                @Override
                public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
                    if (isLivetrackingreq) {
                        if (!tracking_flag) {
                            tracking_flag = true;
                            displayroute();


                            userloc = Currentloc.getLocation().getPosition();
                            double lat = userloc.getX();
                            double longi = userloc.getY();
                            policeEngagedrefer.child("request").child("l").child("0").setValue(longi);
                            policeEngagedrefer.child("request").child("l").child("1").setValue(lat);

                            tracking_flag = false;

                        }
                    }
                }
            };


            slideUp = new SlideUpBuilder(slideupview)
                    .withListeners(new SlideUp.Listener.Events() {
                        @Override
                        public void onSlide(float percent) {

                        }

                        @Override
                        public void onVisibilityChanged(int visibility) {
                            if (visibility == View.GONE) {
                                locbutton.setVisibility(View.VISIBLE);
                                if (isLivetrackingreq) {
                                    bottomlayout.setVisibility(View.VISIBLE);
                                }
                            } else {
                                locbutton.setVisibility(View.GONE);
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
                    if (isLivetrackingreq) {
                        slideUp.show();
                    }
                }
            });


            locationtrackbut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Drawable icon = getResources().getDrawable(R.drawable.ic_police);
                    new LovelyStandardDialog(getActivity())
                            .setTopColorRes(R.color.blue)
                            .setButtonsColorRes(R.color.black)
                            .setIcon(icon)
                            .setTitle("Confirm!")
                            .setMessage("Are you sure want to confirm?")
                            .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    animationView.setVisibility(View.VISIBLE);
                                    locationtrackbut.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                    locationtrackbut.setVisibility(View.GONE);
                                    locbutton.setVisibility(View.VISIBLE);
                                    locationtrackbut.setEnabled(false);
                                    animationView.setVisibility(View.VISIBLE);
                                    animationView.loop(true);
                                    animationView.playAnimation();
                                    if (policeavailable.isEmpty()) {
                                        locationtrackbut.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                        Sneaker.with(getActivity())
                                                .setTitle("Alert!")
                                                .setHeight(175)
                                                .setMessage("No Nearby Police Available")
                                                .sneakWarning();

                                    }
                                    else {
                                        //wait  for response from police
                                        addtodb();
                                        reference.removeEventListener(policelistener);
                                        Currentloc.addLocationChangedListener(loclistener);
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    locationtrackbut.setEnabled(true);
                                }
                            })
                            .show();


                }
            });

        }


    public void displayroute(){
        count++;
        if(count==1) {
            com.esri.arcgisruntime.geometry.Point point = Currentloc.getLocation().getPosition();
            points.add(point);
            polylineBuilder = new PolylineBuilder(points, SpatialReferences.getWgs84());
            polyline = polylineBuilder.toGeometry();
            graphic= new Graphic(polyline,simpleLineSymbol);
            Graphic pointer=new Graphic(point,startmarker);
            graphicsOverlay.getGraphics().add(graphic);
            graphicsOverlay.getGraphics().add(pointer);

        }
        else {
            com.esri.arcgisruntime.geometry.Point point = Currentloc.getLocation().getPosition();
            points.add(point);
            polylineBuilder.addPart(points);
            polyline = polylineBuilder.toGeometry();

        }
        graphic.setGeometry(polyline);


    }
    public void  addtodb(){


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
                    Log.w("started adding","to db");
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
                    for (Map.Entry<String, Point> entry : policeloc.entrySet()) {
                        String uid = entry.getKey();
                        facilitiesdata.put("facility" + i, uid);
                        facilities.add(new Facility(entry.getValue()));
                        Log.w("added the facilities", "succesfully" + uid);
                        Log.w(""+String.valueOf(entry.getValue().getY()),""+String.valueOf(entry.getValue().getX()));
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
                                    ClosestFacilityRoute closestroute = closestResult.getRoute(closestindex, 0);
                                    if (closestroute != null) {

                                        Log.w("got the  route", "hurray");
                                        //add the route to mapview
                                       final double Lat = userloc.getY();
                                       final   double lng = userloc.getX();
                                        for (Map.Entry<String, String> entry : facilitiesdata.entrySet()) {
                                            if ((entry.getKey().equals("facility" + closestindex))) {
                                               final  String driverid = entry.getValue();
                                                 Point driverloc=policeloc.get(driverid);
                                                 final double driverlat=driverloc.getY();
                                                 final double driverlng=driverloc.getX();
                                                Distance = Math.round((closestroute.getTotalLength() / 1000) * 10d);
                                                Distance = Distance / 10d;
                                                arrivaltime = Math.ceil(closestroute.getTravelTime());
                                                 referforrealtime = FirebaseDatabase.getInstance().getReference("DriversAvailable").child("Police").child(driverid);
                                                Log.w("added ","to"+referforrealtime);
                                                 referforrealtime.addChildEventListener(new ChildEventListener() {
                                                     boolean requested=false;
                                                     int childcount=0;
                                                     @Override
                                                     public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                             referforrealtime.getRef().orderByChild("RequestStatus");
                                                             if (!requested) {
                                                                 if (dataSnapshot.getKey().equals("RequestStatus")) {

                                                                     Sneaker.with(getActivity())
                                                                             .setTitle("Error!")
                                                                             .setHeight(175)
                                                                             .setMessage("Requested police is engaged, Please try again later")
                                                                             .sneakWarning();
                                                                     referforrealtime.removeEventListener(this);
                                                                     Fragment ambfag = new PoliceFrag();
                                                                     FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                                                                     transaction.replace(R.id.fragplaceholder, ambfag);
                                                                     transaction.commit();
                                                                     requested = true;

                                                                 } else if (!requested) {
                                                                     Log.w("in the confirm process", "" + dataSnapshot.getKey() + "is the ref");
                                                                     if (childcount == 1) {
                                                                         referforrealtime.child("RequestStatus").child("status").setValue("0");
                                                                         referforrealtime.child("RequestStatus").child("username").setValue("" + name); // todo add with shared prefs
                                                                         referforrealtime.child("RequestStatus").child("Phonenum").setValue("" + phonenum);//todo add with shared prefs
                                                                         requested = true;

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
                                                                         .setTitle("Success!")
                                                                         .setHeight(155)
                                                                         .setMessage("Nearest police officer is tracking your location")
                                                                         .sneakSuccess();
                                                                 reference.removeEventListener(policelistener);
                                                                 referforrealtime.removeEventListener(this);
                                                                 policeEngagedrefer = FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Police").child(driverid);
                                                                 DatabaseReference reference1 = referforrealtime.getParent().getParent().getParent().child("DriversEngaged").child("Police").child(driverid);
                                                                 reference1.child("status").setValue("tracking");
                                                                 GeoFire geoFire = new GeoFire(reference1);
                                                                 geoFire.setLocation("request", new GeoLocation(Lat, lng));
                                                                 geoFire.setLocation("policeloctrack",new GeoLocation(driverlat,driverlng));
                                                                 reference1.child("request").child("UserPhonenum").setValue(""+phonenum);
                                                                 referforrealtime.removeValue();
                                                                 //update ui
                                                                 animationView.setVisibility(View.GONE);
                                                                 logo.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(2000);
                                                                 // todo add trackview showing that you are being tracked
                                                                 trackview.setVisibility(View.VISIBLE);
                                                                 YoYo.with(Techniques.SlideInDown).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(trackview);
                                                                 locbutton.animate().setInterpolator(new AccelerateDecelerateInterpolator()).translationY(40).setDuration(1000);
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
                                                                         .setMessage("Requested police is engaged.Please try again later")
                                                                         .sneakWarning();
                                                                 referforrealtime.removeEventListener(this);
                                                                 reference.removeEventListener(policelistener);
                                                                 referforrealtime.child("RequestStatus").removeValue();
                                                                 Fragment ambfag = new PoliceFrag();
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
                                                tracking_flag=false;
                                                Log.w(referforrealtime.toString(),"is the reference to db");


                                            }
                                        }



                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                referforrealtime.child("RequestStatus").removeValue();
                                Sneaker.with(getActivity())
                                        .setTitle("Alert!")
                                        .setHeight(175)
                                        .setMessage("Requested police is engaged.Please try again later")
                                        .sneakWarning();

                            }
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    referforrealtime.child("RequestStatus").removeValue();
                    Sneaker.with(getActivity())
                            .setTitle("Alert!")
                            .setHeight(175)
                            .setMessage("Requested police is engaged.Please try again later")
                            .sneakWarning();
                }

            }
        });






    }
  
   


    public void onStop(){
        mapView.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleY(1f).translationY(0).setDuration(3000);
        mapView.getGraphicsOverlays().clear();
        Currentloc.removeLocationChangedListener(loclistener);
        if(policeRefer!=null) {
            policeRefer.removeEventListener(policeengagedlistener);
        }
        super.onStop();
    }
    public  void onStart(){
        super.onStart();


         reference.addChildEventListener(policelistener=new ChildEventListener() {
             @Override
             public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                 Log.w("trying to add","here");
                 policeonline++;
                   String userid = dataSnapshot.getKey();
                   if(!existedpolice(userid)) {
                       policeavailable.put("policeonline" + policeonline, userid);
                       try {
                           policecity=dataSnapshot.child("City").getValue().toString();
                           if(policecity.equals(userCity)){
                               policelat = Double.parseDouble(dataSnapshot.child("driverloc").child("l").child("0").getValue().toString());
                               policelongi = Double.parseDouble(dataSnapshot.child("driverloc").child("l").child("1").getValue().toString());
                               Point point = new Point(policelongi, policelat, SpatialReferences.getWgs84());
                               policeloc.put(dataSnapshot.getKey(),point);
                               Log.w("police act",userid+"is added to"+point.getX()+"&"+point.getY());
                           }


                       } catch (Exception e) {
                           e.printStackTrace();
                       }


                      if(policeloc.size()==1){

                          locationtrackbut.setScaleX(0f);
                          locationtrackbut.setScaleY(0f);
                          locationtrackbut.setVisibility(View.VISIBLE);
                          locationtrackbut.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(2000);
                      }
                      if(policeloc.size()==0){
                          Sneaker.with(getActivity())
                                  .setTitle("Alert!")
                                  .setHeight(175)
                                  .setMessage("There are no Police Nearby")
                                  .sneakWarning();

                      }
                   }
             }

             @Override
             public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                 DatabaseReference refer=dataSnapshot.child("driverloc").child("l").getRef();
                 refer.addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot) {
                         String userid=dataSnapshot.getRef().getParent().getParent().getKey();
                         try{
                             double lat=Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                             double longi=Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                             Point point=new Point(longi,lat,SpatialReferences.getWgs84());
                             policeloc.put(userid,point);
                         }catch (Exception e){
                             e.printStackTrace();
                         }
                     }

                     @Override
                     public void onCancelled(DatabaseError databaseError) {

                     }
                 });
             }

             @Override
             public void onChildRemoved(DataSnapshot dataSnapshot) {
                    //todo check for assignment
                   String policeid=dataSnapshot.getKey();
                   policeloc.remove(policeid);
                   String lolcalkey=getpolicekey(policeid);
                   if(lolcalkey!=null) {
                       policeavailable.remove(lolcalkey);
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

    @Override
    public void onPause() {
        super.onPause();

    }

    public void getdriverdetails(final String driverid){

        policeRefer= FirebaseDatabase.getInstance().getReference("DriversReg").child("Police");

        policeRefer.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.w(""+dataSnapshot,"is the refernece");

                Log.w("started getting","details");
                String id = dataSnapshot.child("Userid").getValue().toString();
                if (id.equals(driverid)) {
                    policeRefer.removeEventListener(this);
                    assignedpolicename = dataSnapshot.child("Username").getValue().toString();
                    assignedpolicephonenum = dataSnapshot.child("Phonenum").getValue().toString();
                    if(assignedpolicephonenum!=null && assignedpolicename!=null){

                        policeRefer.removeEventListener(this);
                        isLivetrackingreq=true;
                        displaydpolicedetails();
                        policename.setText("Officer  "+assignedpolicename);
                        policephonenum.setText("     +91"+assignedpolicephonenum);
                        distance.setText("     "+Distance+" km");
                        time.setText("     "+(int)arrivaltime/60+"h "+(int)arrivaltime%60+"m");
                        bottomlayouttextview.setText("Officer  "+assignedpolicename);
                        Log.w("name",""+assignedpolicename);
                        Log.w("phonenum",""+assignedpolicephonenum);
                        Log.w("arrival time",""+(int)arrivaltime/60+"h"+(int)arrivaltime%60+"m");
                        Log.w("total distance",""+Distance+"km");
                        PugNotification.with(context)
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
        DatabaseReference trackrefer;
        trackrefer=FirebaseDatabase.getInstance().getReference("DriversEngaged").child("Police").child(driverid);
        trackrefer.addChildEventListener(policeengagedlistener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Log.w("in tracking",""+dataSnapshot.child("l").child("0").getValue()+"is the ref");
                if(dataSnapshot.getKey().equals("status")){

                    Log.w(""+dataSnapshot.getValue(),"is the child");
                    if(dataSnapshot.getValue().toString().equals("stop")){
                        policeEngagedrefer.removeEventListener(this);
                        PugNotification.with(getActivity())
                                .load()
                                .title("DIGI SURAKSH")
                                .message("Ambulance has arrived")
                                .smallIcon(R.drawable.icon)
                                .largeIcon(R.drawable.icon)
                                .flags(Notification.DEFAULT_ALL)
                                .simple()
                                .build();


                        dataSnapshot.getRef().removeEventListener(this);
                        dataSnapshot.getRef().removeValue();
                        Fragment ambfag = new PoliceFrag();
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
    public void displaydpolicedetails(){
        Handler mhandler = new Handler();
        mhandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    policelogo.setImageDrawable(polislog);
                    bottomanimview.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0f).scaleY(0f).setDuration(1000);
                    bottomframelayout.setScaleX(0f);
                    bottomframelayout.setScaleY(0f);
                    bottomframelayout.setVisibility(View.VISIBLE);
                    bottomframelayout.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1f).scaleY(1f).setDuration(1000);
                    slideUp.show();


            }
        },4000);
    }
    public boolean existedpolice(String driverid) {

        for (String key : policeavailable.keySet()) {
            String value = policeavailable.get(key);
            if (value.equals(driverid))
            {
                existedpoliceresult= true;
                Log.w("duplicate user added","but rejected");
                break;
            } else {
                existedpoliceresult= false;

            }


        }
        return existedpoliceresult;

    }
    public  String getpolicekey(String policeid){
        String result=null;
        for(Map.Entry<String,String> entry:policeavailable.entrySet()){
            if(entry.getValue().equals(policeid)){
                result=entry.getKey();
            }



        }


        return result;
    }
}

