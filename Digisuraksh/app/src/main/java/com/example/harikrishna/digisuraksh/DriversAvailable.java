package com.example.harikrishna.digisuraksh;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityResult;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityRoute;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Facility;
import com.esri.arcgisruntime.tasks.networkanalysis.Incident;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by harikrishna on 05-09-2017.
 */

public class DriversAvailable extends ContextWrapper{


    Context contextof;
    ClosestFacilityTask closestFacilityTask;
    ClosestFacilityRoute closestroute;


    DriversAvailable(Context context) {
        super(context);
        contextof = context;

    }


    public ClosestFacilityRoute getroutetonearestdriver(String routeserv, final Point userloc, final Map<String,Point> driversloc){
         Log.w("invoked this method","");
        //create new closest facility task
        closestFacilityTask=new ClosestFacilityTask(contextof,routeserv);
        //get default parameters
        final ListenableFuture<ClosestFacilityParameters> paramsFuture = closestFacilityTask.createDefaultParametersAsync();
        paramsFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ClosestFacilityParameters closestFacilityParameters= paramsFuture.get();
                    //create incident and facilities
                    List<Incident> incidents = new ArrayList<>();
                    Incident incidentpoint = new Incident(userloc);
                    incidents.add(incidentpoint);
                    closestFacilityParameters.setIncidents(incidents);
                    Log.w("added incident","succesfully");



                    List<Facility> facilities = new ArrayList<>();
                    for(Map.Entry<String,Point> entry:driversloc.entrySet()) {
                          facilities.add(new Facility(entry.getValue()));
                        Log.w("added the facilities","succesfully");
                    }
                    closestFacilityParameters.setFacilities(facilities);





                   //now solve the closest route
                    final ListenableFuture<ClosestFacilityResult> resultfuture =closestFacilityTask.solveClosestFacilityAsync(closestFacilityParameters);
                    resultfuture.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                ClosestFacilityResult closestResult = resultfuture.get();
                                closestroute=closestResult.getRoute(0,0);
                                if(closestroute!=null){

                                Log.w("got the  route","hurray");
                                }
                                else
                                {
                                    Log.w("didnt get the root", "   :(");
                                }

                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });



                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        return closestroute;







    }
}