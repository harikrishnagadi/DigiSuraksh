package com.example.digidriver;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.doctoror.particlesdrawable.ParticlesDrawable;
import com.github.florent37.materialtextfield.MaterialTextField;
import com.github.javiersantos.bottomdialogs.BottomDialog;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.irozon.sneaker.Sneaker;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.shawnlin.numberpicker.NumberPicker;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.ghyeok.stickyswitch.widget.StickySwitch;

/**
 * Created by harikrishna on 25-11-2017.
 */

public class SigninPolice extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private static final String TAG = "phone activity";
    private boolean mVerificationInProgress = false;
    private static String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private EditText phone, otp;
    private MaterialTextField phoneparent;
    private FloatingActionButton fab,otpfab;
    private String phoneNumber;
    private Handler mhandler;
    private Button resendotp;
    private boolean codesent=false;
    boolean result;
    boolean flag;
    int countofusers;
    Map<String,String> Usernames = new HashMap<>();
    Map<String,String> emails = new HashMap<>();
    Map<String,String> Phonenos = new HashMap<>();
    Map<String,String> ages= new HashMap<>();
    Map<String,String> genders = new HashMap<>();
    private  ParticlesDrawable mdrawable = new ParticlesDrawable();
    private String name;
    private int age;
    private String usergender;
    private FloatingActionButton regfab;
    private LottieAnimationView animationView;
    private ImageView userimage;
    private EditText username;
    private TextView textView;
    private Pattern pattern;
    private EditText email;
    private String Emailid;
    private FloatingActionButton regemail;
    private NumberPicker numberPicker;
    private TextView ageview;
    private FloatingActionButton regage;
    private FloatingActionButton regender;
    private DatabaseReference reference;
    private ChildEventListener childEventListener;
    private StickySwitch gender;
    StateProgressBar pageindicator;
    String[] reqpermissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};

    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Toast.makeText(this, currentUser.getUid(), Toast.LENGTH_SHORT).show();
            Log.w(currentUser.getUid(), "is the uid");
            Intent loc = new Intent(SigninPolice.this, MapActivityAmb.class);
            startActivity(loc);
        }

        setContentView(R.layout.signinamb);
        findViewById(R.id.backview).setBackground(getDrawable(R.color.black));

        //ASSIGN VIEWS
        phone = findViewById(R.id.editTextphone);
        phoneparent = findViewById(R.id.materialTextField);
        fab = findViewById(R.id.floatingActionButton);
        otp = findViewById(R.id.editTextotp);
        username=findViewById(R.id.username);
        email=findViewById(R.id.email);
        numberPicker=findViewById(R.id.number_picker);
        regage=findViewById(R.id.regage);
        ageview=findViewById(R.id.agetextview);
        regemail=findViewById(R.id.regemail);
        regender=findViewById(R.id.regender);
        gender=findViewById(R.id.sticky_switch);
        otpfab = findViewById(R.id.floatingActionButtonotp);
        regfab=findViewById(R.id.regfloatingActionButton);
        resendotp = findViewById(R.id.resendotp);
        animationView = findViewById(R.id.useranim);
        pageindicator=findViewById(R.id.your_state_progress_bar_id);
        textView=findViewById(R.id.textView);
        //set listener for buttons
        fab.setOnClickListener(this);
        otpfab.setOnClickListener(this);
        resendotp.setOnClickListener(this);
        gender.setTypeFace(Typeface.DEFAULT_BOLD);
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

        pattern= Pattern.compile(regex);
        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
            }
        });


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
                    Toast.makeText(SigninPolice.this, "Invalid phone credentails", Toast.LENGTH_LONG).show();// todo replace with sneaker

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    //the sms quote of the project has been exceeded
                    // display error of quote exceeded
                    Toast.makeText(SigninPolice.this, "Quota exceeded", Toast.LENGTH_LONG).show();// todo replace with sneaker
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
                    mhandler.postDelayed(delayintent, 1000);
                    if(Phonenos.containsValue(phoneNumber)) {
                        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(phoneparent);
                        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(fab);
                    }
                    codesent=true;
                    //Update ui
                    // updateUI(STATE_CODE_SENT);

                }
                Sneaker.with(SigninPolice.this)
                        .setTitle("OTP sent!")
                        .setHeight(175)
                        .setMessage("OTP successfully sent to +91"+phoneNumber)
                        .sneakSuccess();
            }
        };

    }


    @Override
    public void onResume() {
        super.onResume();

        reference = FirebaseDatabase.getInstance().getReference("DriversReg").child("Police");
        reference.addChildEventListener(childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot!=null)
                {
                    countofusers++;
                    String email = dataSnapshot.child("Email").getValue().toString();
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String phoneno = dataSnapshot.child("Phonenum").getValue().toString();
                    String userage = dataSnapshot.child("Age").getValue().toString();
                    String gender = dataSnapshot.child("Gender").getValue().toString();
                    Usernames.put("user" + countofusers, username);
                    emails.put("user" + countofusers, email);
                    Phonenos.put("user" + countofusers, phoneno);
                    ages.put("user" + countofusers, userage);
                    genders.put("user" + countofusers, gender);
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
                SigninPolice.this,  //activity for callbacks
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
                SigninPolice.this,  //activity for callbacks
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
                    mhandler.postDelayed(delayintent1, 2500);
                    userimage=findViewById(R.id.user);
                    userimage.setVisibility(View.GONE);
                    animationView.playAnimation();
                    if(!Phonenos.containsValue(phoneNumber)) {
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                        final SharedPreferences.Editor sharedprefeditor = sharedPreferences.edit();
                        sharedprefeditor.putString("Username", name);
                        sharedprefeditor.putString("Phonenum", phoneNumber);
                        sharedprefeditor.putInt("Age",age);
                        sharedprefeditor.putString("Email",Emailid);
                        sharedprefeditor.putString("Gender",usergender);
                        sharedprefeditor.apply();
                        if(mAuth.getCurrentUser()!=null) {
                            String uid = mAuth.getCurrentUser().getUid();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DriversReg").child("Police");
                            reference.child(""+uid).child("Username").setValue(name);
                            reference.child(""+uid).child("Phonenum").setValue(phoneNumber);
                            reference.child(""+uid).child("Email").setValue(Emailid);
                            reference.child(""+uid).child("Age").setValue(age);
                            reference.child(""+uid).child("Gender").setValue(usergender);
                            reference.child(""+uid).child("Userid").setValue(uid);
                        }
                    }
                    else {
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getResources().getString(R.string.Shared_prefernce_filekey1), MODE_PRIVATE);
                        final SharedPreferences.Editor sharedprefeditor = sharedPreferences.edit();
                        sharedprefeditor.putString("Username", name);
                        sharedprefeditor.putString("Phonenum", phoneNumber);
                        sharedprefeditor.putInt("Age",age);
                        sharedprefeditor.putString("Email",Emailid);
                        sharedprefeditor.putString("Gender",usergender);
                        sharedprefeditor.apply();
                    }
                    YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(otp);
                    YoYo.with(Techniques.SlideOutLeft).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(resendotp);
                    YoYo.with(Techniques.SlideOutLeft).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(otpfab);
                    checkloc();
                    checklocset();

                } else {
                    //openact failed display a message and update the ui
                    Log.w(TAG, "siginInWithCredentail:failure", task.getException());
                    Log.w(TAG, "sent otp:" + "otp by user:");
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        //the code entered was invalid
                        //display the error message
                        YoYo.with(Techniques.Shake).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(otp);
                        Sneaker.with(SigninPolice.this)
                                .setTitle("Error!")
                                .setHeight(175)
                                .setMessage("Invalid OTP")
                                .sneakWarning();
                    }
                    //update ui
                    // updateUI(STATE_SIGNIN_FAILED);

                }


            }
        });

    }


    private boolean validatePhoneNumber() {
        phoneNumber = phone.getText().toString();

        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() != 10) {
            YoYo.with(Techniques.Shake).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(phone);
            Sneaker.with(this)
                    .setTitle("Error!")
                    .setHeight(175)
                    .setMessage("Check your Phone number")
                    .sneakError();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.floatingActionButton:
                reference.removeEventListener(childEventListener);
                phoneNumber = phone.getText().toString().trim();
                if (!validatePhoneNumber()) {
                    return;
                }
                if (!isdeviceconnected(SigninPolice.this)) {
                    connecttonetwork();
                }
                checknumber();
                break;
            case R.id.regfloatingActionButton:
                name =  username.getText().toString();
                if(TextUtils.isEmpty(name)){
                    YoYo.with(Techniques.Shake).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(username);
                    Sneaker.with(this)
                            .setTitle("Error!")
                            .setHeight(175)
                            .setMessage("Username cannot be blank")
                            .sneakError();
                }
                else {
                    checkusername();
                }

                break;
            case R.id.regemail:
                Emailid= email.getText().toString().trim();
                if(!pattern.matcher(Emailid).matches()){
                    YoYo.with(Techniques.Shake).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(email);
                    Sneaker.with(this)
                            .setTitle("Error!")
                            .setHeight(175)
                            .setMessage(" Check your Email id")
                            .sneakError();

                }else {
                    checkemail();
                }
                break;
            case R.id.regage:
                age=numberPicker.getValue();
                nextview();
                break;
            case R.id.regender:
                usergender=  gender.getText();
                formalities();


                break;

            case R.id.floatingActionButtonotp:
                String code = otp.getText().toString().trim();
                if(TextUtils.isEmpty(code)|| code.length()!=6){
                    YoYo.with(Techniques.Shake).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(otp);
                    Sneaker.with(this)
                            .setTitle("Error!")
                            .setHeight(175)
                            .setMessage("Invalid OTP format")
                            .sneakError();
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
    public void checknumber(){


        if(Phonenos.containsValue(phoneNumber)){
            String userid=null;
            for (Map.Entry<String, String> entry : Phonenos.entrySet())
            {
                if(entry.getValue().equals(phoneNumber)) {
                    userid = entry.getKey();
                }
            }
            if(userid!=null)
            {
                name=Usernames.get(userid);
                age=Integer.parseInt(ages.get(userid));
                usergender=genders.get(userid);
                Emailid=emails.get(userid);
            }
            startPhoneNumberVerification(phoneNumber);

        }
        else {

            new BottomDialog.Builder(this)
                    .setTitle("Register!")
                    .setContent("seems like you are new here We couldnt find you in the database.Please, Register with us to continue")
                    .setPositiveText("Register")
                    .setPositiveBackgroundColorResource(R.color.grey)
                    //.setPositiveBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary)
                    .setPositiveTextColorResource(android.R.color.white)
                    //.setPositiveTextColor(ContextCompat.getColor(this, android.R.color.colorPrimary)
                    .onPositive(new BottomDialog.ButtonCallback() {
                        @Override
                        public void onClick(BottomDialog dialog) {
                            textView.setText("Register!");
                            YoYo.with(Techniques.SlideOutRight).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(phoneparent);
                            YoYo.with(Techniques.SlideOutRight).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(fab);
                            regfab.setVisibility(View.VISIBLE);
                            username.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.SlideInLeft).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(regfab);
                            YoYo.with(Techniques.SlideInLeft).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(username);
                            pageindicator.setVisibility(View.VISIBLE);
                            pageindicator.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);

                        }
                    }).show();

        }
    }
    public void formalities(){
        YoYo.with(Techniques.SlideOutRight).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(regender);
        gender.setVisibility(View.GONE);
        startPhoneNumberVerification(phoneNumber);
        pageindicator.setVisibility(View.GONE);




    }
    public void nextview(){
        numberPicker.setVisibility(View.GONE);
        ageview.setVisibility(View.GONE);
        YoYo.with(Techniques.SlideOutRight).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(regage);
        pageindicator.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
        gender.setVisibility(View.VISIBLE);
        gender.setOnClickListener(new View.OnClickListener() {
            boolean flag=false;
            @Override
            public void onClick(View view) {
                if (!flag) {
                    flag=true;
                    StickySwitch.Direction direction = gender.getDirection();
                    Log.w("invoked with", "&" + direction.name());
                    if (direction == StickySwitch.Direction.LEFT) {
                        gender.setDirection(StickySwitch.Direction.RIGHT, true, true);
                        flag=false;
                    } else {
                        gender.setDirection(StickySwitch.Direction.LEFT, true, false);
                        flag=false;
                    }
                }
            }
        });
        regender.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.SlideInLeft).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(regender);



    }

    public void connecttonetwork() {

        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public boolean isdeviceconnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return (network != null && (network.isConnectedOrConnecting()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mdrawable.stop();
    }
    public  void checkemail(){
        String emailid = email.getText().toString().trim();
        if(emails.containsValue(emailid)){
            YoYo.with(Techniques.Shake).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(email);
            Sneaker.with(this)
                    .setTitle("Alert!")
                    .setHeight(175)
                    .setMessage("Already taken chooose another")
                    .sneakWarning();

        }
        else {

            pageindicator.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
            pageindicator.animate();
            YoYo.with(Techniques.SlideOutRight).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(email);
            YoYo.with(Techniques.SlideOutRight).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(regemail);
            numberPicker.setVisibility(View.VISIBLE);
            ageview.setVisibility(View.VISIBLE);
            regage.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInLeft).duration(1000).interpolate(new AccelerateDecelerateInterpolator()).playOn(regage);
        }
    }
    public void checkusername(){
        String name = username.getText().toString().trim();
        if (Usernames.containsValue(name)) {
            YoYo.with(Techniques.Shake).duration(800).interpolate(new AccelerateDecelerateInterpolator()).playOn(username);
            Sneaker.with(this)
                    .setTitle("Alert!")
                    .setHeight(175)
                    .setMessage("Already taken chooose another")
                    .sneakWarning();

        }
        else {
            pageindicator.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
            pageindicator.animate();
            YoYo.with(Techniques.SlideOutRight).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(username);
            YoYo.with(Techniques.SlideOutRight).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(regfab);
            email.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInLeft).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(email);
            regemail.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInLeft).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(regemail);

        }
    }
    private Runnable delayintent = new Runnable() {
        @Override
        public void run() {


            resendotp.setVisibility(View.VISIBLE);
            otpfab.setVisibility(View.VISIBLE);
            otp.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInRight).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(otp);
            YoYo.with(Techniques.SlideInLeft).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(resendotp);
            YoYo.with(Techniques.SlideInLeft).duration(2000).interpolate(new AccelerateDecelerateInterpolator()).playOn(otpfab);


        }
    };
    private  Runnable delayintent1 = new Runnable() {
        @Override
        public void run() {

            Intent loc = new Intent(SigninPolice.this, MapActivityPolice.class);
            startActivity(loc);

        }
    };
    public void checkloc(){
        boolean fineperm = ContextCompat.checkSelfPermission(this,reqpermissions[0])== PackageManager.PERMISSION_GRANTED;
        boolean coarseperm = ContextCompat.checkSelfPermission(this,reqpermissions[1])== PackageManager.PERMISSION_GRANTED;
        // check if the permissions are not given and req the user for permissions
        Log.e("FINE",""+fineperm);
        Log.e("coarse",""+coarseperm);
        // get the status of our permissions
        if(!(fineperm&&coarseperm))
        {
            ActivityCompat.requestPermissions(this, reqpermissions, 2);
            Log.e("permission req", "onStatusChanged: ");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mdrawable.start();
    }

    public void checklocset(){
        LocationManager Locmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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
                    finish(); //todo handle this
                }
            });
            dialog.create().show();
        }
    }
}
