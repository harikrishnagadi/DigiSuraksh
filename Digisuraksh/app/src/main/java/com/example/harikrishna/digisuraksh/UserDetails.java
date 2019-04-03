package com.example.harikrishna.digisuraksh;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by harikrishna on 05-10-2017.
 */

public class UserDetails extends Application {
    public Map<String,String> Usernames=new HashMap<>();
    public Map<String,String> Phonenos=new HashMap<>();
    public Map<String,String> Passwords=new HashMap<>();
       String user=null;


    public void  setUserDetails(Map<String,String> usernames,Map<String,String> phonenos,Map<String,String> passwords){

        this.Usernames= usernames;
        this.Phonenos=phonenos;
        this.Passwords=passwords;

        for(Map.Entry<String,String> entry :Phonenos.entrySet()){


        }


    }

    public boolean isphoneregistered(String phoneno)
    {
        return Phonenos.containsValue(phoneno);

    }
    public boolean isusernameregistered(String username){


        return Usernames.containsValue(username);

    }
    public String ispasswordmatching(String phoneno,String password){

        String result=null;
        for(Map.Entry entry: Phonenos.entrySet()){

            if(entry.getValue().equals(phoneno)){

                user = (String) entry.getKey();

                break;

            }


        }
        if(user==null){
            result= "Number not found";
        }
        else if(password.equals(Passwords.get(user))){
            result= "password matching";
        }
        else if(!(password.equals(Passwords.get(user))))
        {

            result = "wrong password";
        }

      return  result;
    }


}
