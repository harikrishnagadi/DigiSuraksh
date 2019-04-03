package com.example.digidriver;

import android.app.Application;
import android.widget.Switch;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by harikrishna on 23-10-2017.
 */

public class DriversDetails extends Application {
    public Map<String,String> AmbUsernames=new HashMap<>();
    public Map<String,String> AmbPhonenos=new HashMap<>();
    public Map<String,String> FireUsernames=new HashMap<>();
    public Map<String,String> FirePhonenos=new HashMap<>();
    public Map<String,String> PoliceUsernames=new HashMap<>();
    public Map<String,String> PolicePhonenos=new HashMap<>();
    public int ambcount=0;
    public int firecount=0;
    public int policecount=0;

    String user=null;
    public void  setAmbdriverDetails(Map<String,String> usernames,Map<String,String> phonenos){

       // Toast.makeText(this,"entered this1",Toast.LENGTH_LONG).show();
        this.AmbUsernames= usernames;
        this.AmbPhonenos=phonenos;


        for(Map.Entry<String,String> entry :AmbPhonenos.entrySet()){

            ambcount++;
        }


    }
    public void  setFiredriverDetails(Map<String,String> usernames,Map<String,String> phonenos){

        //Toast.makeText(this,"entered this2",Toast.LENGTH_LONG).show();
        this.FireUsernames= usernames;
        this.FirePhonenos=phonenos;


        for(Map.Entry<String,String> entry :FirePhonenos.entrySet()){

            firecount++;
        }


    }
    public void  setPolicedriverDetails(Map<String,String> usernames,Map<String,String> phonenos){

       // Toast.makeText(this,"entered this3",Toast.LENGTH_LONG).show();
        this.PoliceUsernames= usernames;
        this.PolicePhonenos=phonenos;


        for(Map.Entry<String,String> entry :FirePhonenos.entrySet()){

            policecount++;
        }


    }
    public String isphonenumreg(String Phonenum){
        String result;
         if(AmbPhonenos.containsValue(Phonenum)){
             result="RegAmb";
         }
         else if(FirePhonenos.containsValue(Phonenum)){
             result="RegFire";
         }
         else if(PolicePhonenos.containsValue(Phonenum)){
             result="RegPolice";
         }
         else {
             result="NotReg";
         }
         return result;

    }
    public boolean isusernamereg(String username){
        boolean result;
        if(AmbUsernames.containsValue(username)){
            result=true;
        }
        else if(FireUsernames.containsValue(username)){
            result=true;
        }
        else if(PoliceUsernames.containsValue(username)){
            result= true;
        }
        else {
            result=false;
        }
        return result;

    }
    public String getusername(String phonenum){
       String source= isphonenumreg(phonenum);
        String username=null;
        switch (source){
            case "RegAmb":
                    String ambdriver;
                for(Map.Entry entry: AmbPhonenos.entrySet()) {
                    if (entry.getValue().equals(phonenum)) {
                        ambdriver = (String) entry.getKey();
                        username = AmbUsernames.get(ambdriver);
                        break;

                    }
                }
                break;
            case "RegFire":
                String firedriver;
                for(Map.Entry entry: AmbPhonenos.entrySet()) {
                    if (entry.getValue().equals(phonenum)) {
                        firedriver = (String) entry.getKey();
                        username = AmbUsernames.get(firedriver);
                        break;

                    }
                }
                break;
            case "RegPolice":
                String policedriver;
                for(Map.Entry entry: AmbPhonenos.entrySet()) {
                    if (entry.getValue().equals(phonenum)) {
                        policedriver = (String) entry.getKey();
                        username = AmbUsernames.get(policedriver);
                        break;

                    }
                }
        }
        return username;
    }
}
