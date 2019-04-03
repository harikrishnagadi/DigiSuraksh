package com.example.digidriver;

/**
 * Created by harikrishna on 19-10-2017.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by harikrishna on 08-10-2017.
 */

public class SignInAct extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private static final String TAG = "phone activity";
    private boolean mVerificationInProgress = false;
    private static String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private EditText phone, otp,usernameedittext;
    private MaterialTextField phoneparent;
    private FloatingActionButton fab,otpfab;
    private String phoneNumber;
    private Handler mhandler;
    private Button resendotp;
    private boolean codesent=false;
    private LottieAnimationView animationView;
    private ImageView userimage;
    private String service;
    private CoordinatorLayout snackbarview;
    private String username;
    private MaterialTextField usernamefield;
    private FloatingActionButton userfab;
    private String usernametext;
    private boolean newuser;
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        service = pref.getString("service", null);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("DriversReg");
        setContentView(R.layout.signinact);

        //ASSIGN VIEWS
        phone = findViewById(R.id.editTextphone);
        fab = findViewById(R.id.floatingActionButton);
        otp = findViewById(R.id.editTextotp);
        otpfab = findViewById(R.id.floatingActionButtonotp);
        resendotp = findViewById(R.id.resendotp);
        animationView = findViewById(R.id.useranim);
        phoneparent=findViewById(R.id.materialTextField);
        //set listener for buttons
        fab.setOnClickListener(this);
        otpfab.setOnClickListener(this);
        resendotp.setOnClickListener(this);


        //callbacks
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential Credential) {
                //this call back will be invoked in two situations
                //1-Instant verification.In some cases the phone number can be instantly verified without
                //needing to send the verification code or enter it.
                //2-auto retrieval on some devices google play services can automatically detect the incoming
                //verification code and perform activation without user's action.
                Log.w(TAG, "onverification completed:" + Credential);
                //change verification progress state
                mVerificationInProgress = false;
                //Update ui
                //updateUI(STATE_SIGNIN_SUCCESS,Credential);
                //openact with phone auth credential
                //signInWithPhoneAuthCredential(Credential);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                //THIS CALLBACK is invoked in an invalid request for verification is made
                //for instance phone number is not valid.
                Log.w(TAG, "onVerificationFailed:", e);
                //change the status of process
                mVerificationInProgress = false;
                //showing eroor messages
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    //show invalid phone number
                    Toast.makeText(SignInAct.this, "Invalid phone credentails", Toast.LENGTH_LONG).show();

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    //the sms quote of the project has been exceeded
                    // display error of quote exceeded
                    Toast.makeText(SignInAct.this, "Quota exceeded", Toast.LENGTH_LONG).show();
                }
                //update the ui accordingly
                //updateUI(STATE_VERIFY_FAILED);


            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                //The SMS VERIFICATION HAS BEEN SENT TO THE PROVIDED PHONE NUMBER, WE

                //NOW NEED TO ASK THE USER TO ENTER THE CODE AND THEN CONSTRUCT A CREDENTIAL
                //BY COMBINING THE COE WITH THE VERIFICATION ID
                Log.w(TAG, "onCodeSent: " + verificationId);

                // show toast to user indicating the sent of code
                if(!codesent) {
                    //SAVE verification ID and resending token so we can use them later
                    phoneparent = findViewById(R.id.materialTextField);
                    mVerificationId = verificationId;
                    mResendToken = token;
                    mhandler = new Handler();
                    mhandler.postDelayed(delayintent, 1200);
                    YoYo.with(Techniques.SlideOutRight).duration(300).interpolate(new AccelerateDecelerateInterpolator()).playOn(phoneparent);
                    YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(fab);
                    codesent=true;
                    Toast.makeText(SignInAct.this,"OTP sent sucessfully",Toast.LENGTH_LONG).show();
                    //Update ui
                    // updateUI(STATE_CODE_SENT);

                }

            }
        };

    }


    @Override
    public void onResume() {
        super.onResume();
        // check if user if signed in (non-null) and update ui accordingly.
        FirebaseUser currentuser = mAuth.getCurrentUser();

        // updateUI(currentuser);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    public void startPhoneNumberVerification(String PhoneNum) {
        Log.w(TAG, "started phoner verification");

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + PhoneNum,  // PHONE NUMBER
                60,        // time out time
                java.util.concurrent.TimeUnit.SECONDS, // unit of time
                SignInAct.this,  //activity for callbacks
                mCallbacks);
        //flag verification status
        mVerificationInProgress = true;

    }

    private void verifyPhoneNumberWithCode(String verificationid, String code) {
        // trigger this funtion on the button of verify by the user
        //start verify with code
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationid, code);
        // end verify with code
        signInWithPhoneAuthCredential(credential);

    }

    // resend verification
    public void resendVerifictionCode(String PhoneNumber, PhoneAuthProvider.ForceResendingToken token) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + PhoneNumber,  // PHONE NUMBER
                60,        // time out time
                java.util.concurrent.TimeUnit.SECONDS, // unit of time
                SignInAct.this,  //activity for callbacks
                mCallbacks, token);
    }


    public void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //sign in success, update ui with new activity
                    Log.w(TAG, "SigninwithCredential:success");
                    FirebaseUser user = task.getResult().getUser();
                    //update ui
                    //updateUI(STATE_SIGNIN_SUCCESS,user);
                    mhandler = new Handler();
                    mhandler.postDelayed(delayintent1, 1200);
                    userimage=findViewById(R.id.user);
                    userimage.setVisibility(View.GONE);
                    animationView.playAnimation();
                    YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(otp);
                    YoYo.with(Techniques.SlideOutLeft).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(resendotp);
                    YoYo.with(Techniques.SlideOutLeft).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(otpfab);
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor =pref.edit();
                    editor.putString("Phonenum",phoneNumber);
                    editor.apply();
                    if(newuser){
                        //registerondb();
                    }


                } else {
                    //openact failed display a message and update the ui
                    Log.w(TAG, "siginInWithCredentail:failure", task.getException());
                    Log.w(TAG, "sent otp:" + "otp by user:");
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        //the code entered was invalid
                        //display the error message
                        Toast.makeText(SignInAct.this,"Invalid Otp",Toast.LENGTH_LONG).show();
                    }
                    //update ui
                    // updateUI(STATE_SIGNIN_FAILED);

                }


            }
        });

    }
   /* private boolean validateusername(){
        boolean result1=true;
        boolean result2=true;
        if(usernametext.contains(" ")||usernametext.length()<6){
            Snackbar.make(snackbarview, "Username should be atleast 6 characters and must not contain spaces", Snackbar.LENGTH_SHORT).show();
            result1=false;
        }
        if (((DriversDetails) getApplication()).isusernamereg(usernametext)){
            Snackbar.make(snackbarview, "Username Exists", Snackbar.LENGTH_SHORT).show();
            result2=false;
        }

        return (result1&&result2);
    }  */

    private boolean validatePhoneNumber() {
        phoneNumber = phone.getText().toString();

        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() != 10) {
            phone.setError("Check your Phone Number");
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.floatingActionButton:
                phoneNumber = phone.getText().toString().trim();
               // Toast.makeText(getBaseContext(), "started fab", Toast.LENGTH_LONG).show();
                if (!validatePhoneNumber()) {
                    return;
                }
                if (!isdeviceconnected(SignInAct.this)) {
                    connecttonetwork();
                }
                if(isdeviceconnected(SignInAct.this)) {
                    Log.w(phoneNumber, "is the number");

                        startPhoneNumberVerification(phoneNumber);

                }
                break;
           /* case R.id.floatingActionButtonusername:
                newuser=true;
                usernametext=usernameedittext.getText().toString().trim();
                if(validateusername()) {
                    username=usernametext;
                    startPhoneNumberVerification(phoneNumber);
                } */
            case R.id.floatingActionButtonotp:
                String code = otp.getText().toString().trim();
                if(TextUtils.isEmpty(code)|| code.length()!=6){
                    otp.setError("Invalid otp");
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId,code);
                break;
            case R.id.resendotp:
                resendVerifictionCode(phoneNumber,mResendToken);
                findViewById(R.id.resendotp).setEnabled(false);
                break;


        }
    }

    public void connecttonetwork() {
       // Snackbar.make(snackbarview, "BAD REQUEST TRY AGAIN LATER", Snackbar.LENGTH_SHORT).show();

    }

    public boolean isdeviceconnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return (network != null && (network.isConnectedOrConnecting()));
    }
    public boolean verifyphone(String phoneNumber){
             String fromresult = ((DriversDetails)getApplication()).isphonenumreg(phoneNumber);
             boolean toresult=true;

        switch (service) {
            case "ambulance":
               // Toast.makeText(SignInAct.this,fromresult +"is the service",Toast.LENGTH_LONG).show();
                switch (fromresult) {
                    case "RegAmb":
                        Toast.makeText(SignInAct.this,"entered this",Toast.LENGTH_LONG).show();
                        username=((DriversDetails) getApplication()).getusername(phoneNumber);
                        toresult = true;
                        break;
                    case "RegFire":
                        Snackbar.make(snackbarview, "Number Already Registered", Snackbar.LENGTH_SHORT).show();
                        toresult = false;
                        break;
                    case "RegPolice":
                        Snackbar.make(snackbarview, "Number Already Registered", Snackbar.LENGTH_SHORT).show();
                        toresult = false;
                        break;
                    case "NotReg":
                        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(phoneparent);
                        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(fab);
                        usernamefield.setVisibility(View.VISIBLE);
                        userfab.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(usernamefield);
                        YoYo.with(Techniques.SlideInRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(userfab);
                        Snackbar.make(snackbarview, "You are new here choose username", Snackbar.LENGTH_SHORT).show();
                        toresult = false;
                        break;
                }
                break;
            case "fire":
                switch (fromresult) {
                    case "RegAmb":
                        toresult = false;
                        Snackbar.make(snackbarview, "Number Already Registered", Snackbar.LENGTH_SHORT).show();
                        break;
                    case "RegFire":
                        username=((DriversDetails) getApplication()).getusername(phoneNumber);
                        toresult = true;
                        break;
                    case "RegPolice":
                        Snackbar.make(snackbarview, "Number Already Registered", Snackbar.LENGTH_SHORT).show();
                        toresult = false;
                        break;
                    case "NotReg":
                        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(phoneparent);
                        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(fab);
                        usernamefield.setVisibility(View.VISIBLE);
                        userfab.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(usernamefield);
                        YoYo.with(Techniques.SlideInRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(userfab);
                        Snackbar.make(snackbarview, "You are new here choose username", Snackbar.LENGTH_SHORT).show();
                        toresult = false;
                        break;
                }
                break;
            case "police":
                switch (fromresult) {
                    case "RegAmb":
                        toresult = false;
                        Snackbar.make(snackbarview, "Number Already Registered", Snackbar.LENGTH_SHORT).show();
                        break;
                    case "RegFire":
                        Snackbar.make(snackbarview, "Number Already Registered", Snackbar.LENGTH_SHORT).show();
                        toresult = false;
                        break;
                    case "RegPolice":
                        username=((DriversDetails) getApplication()).getusername(phoneNumber);
                        toresult = true;
                        break;
                    case "NotReg":
                        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(phoneparent);
                        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(fab);
                        usernamefield.setVisibility(View.VISIBLE);
                        userfab.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(usernamefield);
                        YoYo.with(Techniques.SlideInRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(userfab);
                        Snackbar.make(snackbarview, "You are new here choose username", Snackbar.LENGTH_SHORT).show();
                        toresult = false;
                        break;
                }
                break;


        }

        Toast.makeText(SignInAct.this,Boolean.toString(toresult),Toast.LENGTH_LONG).show();
        return toresult;
    }
     /*
    private void registerondb(){ //// TODO: 23-10-2017  update data and add new
        switch (service){


            case "ambulance":
                DatabaseReference AmbdatabaseReference = FirebaseDatabase.getInstance().getReference("DriversReg").child("Ambulance");
                int ambdrivercount = ((DriversDetails)getApplication()).ambcount;
                AmbdatabaseReference.child("driver"+ambdrivercount+1).child("Username").setValue(username);
                AmbdatabaseReference.child("driver"+ambdrivercount+1).child("Phone").setValue(phoneNumber);
                break;
            case "fire":
                DatabaseReference firedatabaseReference = FirebaseDatabase.getInstance().getReference("DriversReg").child("Fire");
                int firedrivercount = ((DriversDetails)getApplication()).firecount;
                firedatabaseReference.child("driver"+firedrivercount+1).child("Username").setValue(username);
                firedatabaseReference.child("driver"+firedrivercount+1).child("Phone").setValue(phoneNumber);

            case "police":
                DatabaseReference policedatabaseReference = FirebaseDatabase.getInstance().getReference("DriversReg").child("Fire");
                int policedrivercount = ((DriversDetails)getApplication()).policecount;
                policedatabaseReference.child("driver"+policedrivercount+1).child("Username").setValue(username);
                policedatabaseReference.child("driver"+policedrivercount+1).child("Phone").setValue(phoneNumber);
                break;





        }








    } */

    private Runnable delayintent = new Runnable() {
        @Override
        public void run() {


            resendotp.setVisibility(View.VISIBLE);
            otpfab.setVisibility(View.VISIBLE);
            otp.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInRight).duration(10000).interpolate(new AccelerateDecelerateInterpolator()).playOn(otp);
            YoYo.with(Techniques.SlideInLeft).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(resendotp);
            YoYo.with(Techniques.SlideInLeft).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(otpfab);


        }
    };
    private  Runnable delayintent1 = new Runnable() {
        @Override
        public void run() {
            switch (service) {
                case "ambulance":
                    Intent loc = new Intent(SignInAct.this, MapActivityAmb.class);
                    startActivity(loc);
                    break;
                case "fire":
                    Intent locfire = new Intent(SignInAct.this, MapActivityFire.class);
                    startActivity(locfire);
                    break;
                case "police":
                    Intent locpolice = new Intent(SignInAct.this, MapActivityPolice.class);
                    startActivity(locpolice);
                    break;

            }

        }
    };

}


