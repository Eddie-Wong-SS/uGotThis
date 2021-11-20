//Java file for the registration screen, which allows a new user to make a new account for the app
//Firebase email and password authentication is used for the making of a new account

//Required imports
package com.example.ugotthis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class Register extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    private EditText user;
    private EditText pass;
    private EditText conpass;
    private Button reg;
    private ColorStateList colors;
    private EditText email;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    Boolean mSwiping, left;
    float mDownX, mDownY, touch;
    Intent intent;
    View view;
    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    int color[] = new int[]
            {
                    Color.RED,
                    Color.GREEN
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Gets and assigns elements of the activity for coding purposes
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        email = findViewById(R.id.Email);
        email.setOnFocusChangeListener(this);

        user = findViewById(R.id.Username);
        user.setOnFocusChangeListener(this);

        pass = findViewById(R.id.Password);
        pass.setOnFocusChangeListener(this);

        conpass = findViewById(R.id.ConPass);
        conpass.setOnFocusChangeListener(this);

        reg = findViewById(R.id.Button_reg);
        reg.setOnClickListener(this);

        auth = FirebaseAuth.getInstance(); //Gets a new instance of Firebase Authentication

        view = findViewById(R.id.Register);
        ViewConfiguration vc = ViewConfiguration.get(view.getContext());
        touch = vc.getScaledTouchSlop(); //Get slop for accurate position determination
        view.setOnTouchListener(new StandardGestures(this)); //Enable pinch gesture zoom in/out

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() { //Runs event when the phone is shaken
            @Override
            public void onShake(int count) { //count refers to number of times the phone is shaken
                reLoad(count);
            }
        });

        progressBar.setVisibility(View.GONE);
    }

    //Code to check password length in the password or password confirm fields after the user has left the field focus
    @Override
    public void onFocusChange(View view, boolean b) {
        if(!b)
        {
            if (view == pass) {
                if (pass.getText().toString().trim().length() < 8) {
                    Toast.makeText(getApplicationContext(), "Your password length is too short!", Toast.LENGTH_SHORT).show();
                    colors = ColorStateList.valueOf(color[0]);
                    pass.setBackgroundTintList(colors);
                } else {
                    colors = ColorStateList.valueOf(color[1]);
                    pass.setBackgroundTintList(colors);
                }
            } else if (view == conpass) {
                if (!conpass.getText().toString().trim().equals(pass.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "Your passwords do not match!", Toast.LENGTH_SHORT).show();
                    colors = ColorStateList.valueOf(color[0]);
                    pass.setBackgroundTintList(colors);
                    conpass.setBackgroundTintList(colors);
                } else {
                    colors = ColorStateList.valueOf(color[1]);
                    pass.setBackgroundTintList(colors);
                    conpass.setBackgroundTintList(colors);
                }
            } else if (view == user) {
                if (user.getText().toString().trim().length() != 0) {
                    colors = ColorStateList.valueOf(color[1]);
                    user.setBackgroundTintList(colors);
                }
            }
            else if(view == email)
            {
                if (user.getText().toString().trim().length() != 0) {
                    colors = ColorStateList.valueOf(color[1]);
                    user.setBackgroundTintList(colors);
                }
            }
        }
    }

    //Function that handles events after the "Register New" button is clicked
    @Override
    public void onClick(View view) {
        //Code to check if various requirements have been met with corresponding error messages if not
        if(pass.getText().toString().trim().length() == 0 || conpass.getText().toString().trim().length() == 0
                || user.getText().toString().trim().length() == 0 || email.getText().toString().trim().length() == 0)
        {
            Toast.makeText(getApplicationContext(), "All fields need to be filled!", Toast.LENGTH_LONG).show();
            colors = ColorStateList.valueOf(color[0]);
            pass.setBackgroundTintList(colors);
            conpass.setBackgroundTintList(colors);
            user.setBackgroundTintList(colors);
            email.setBackgroundTintList(colors);
        }
        else if(user.getText().toString().trim().length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Username field is empty!", Toast.LENGTH_LONG).show();
            colors = ColorStateList.valueOf(color[0]);
            user.setBackgroundTintList(colors);
        }
        else if(email.getText().toString().trim().length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Email field is empty!", Toast.LENGTH_LONG).show();
            colors = ColorStateList.valueOf(color[0]);
            email.setBackgroundTintList(colors);
        }
        else if(pass.getText().toString().trim().length() < 8)
        {
            Toast.makeText(getApplicationContext(), "Password must not be less than 8 characters!", Toast.LENGTH_LONG).show();
            colors = ColorStateList.valueOf(color[0]);
            pass.setBackgroundTintList(colors);
        }
        else if(!conpass.getText().toString().trim().equals(pass.getText().toString().trim()))
        {
            Toast.makeText(getApplicationContext(), "Your passwords do not match!", Toast.LENGTH_SHORT).show();
            colors = ColorStateList.valueOf(color[0]);
            pass.setBackgroundTintList(colors);
            conpass.setBackgroundTintList(colors);
        }
        else //All requirements are met
        {
            progressBar.setVisibility(View.VISIBLE);
            //create user
            String mail = email.getText().toString();
            String password = pass.getText().toString();
            auth.createUserWithEmailAndPassword(mail, password) //Creates a new Firebase authentication acc with email and password
                    .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                            Toast.makeText(getApplicationContext(), "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(), "Authentication failed." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                addUserNameToUser(task.getResult().getUser(), user.getText().toString());
                                startActivity(new Intent(Register.this,Home_Screen.class));
                                finish();
                            }
                        }
                    });
            //Users db = new Users(this); Unused SQLite code, saved for posterity

            //db.addUser(use); Unused SQLite code, saved for posterity
        }
    }

    //Function to add the user's username into a new record in Firebase Database
    private void addUserNameToUser(FirebaseUser user, String newname) {
        String username = newname;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("username/" + user.getUid() + "/user"); //Creates a new record based on the given Unique ID
        myRef.setValue(username);
        Toast.makeText(getApplicationContext(), "You have been registered!", Toast.LENGTH_LONG).show();
    }

    /**
     *dispatchTouchEvents fire before any other event listeners
     *Used to ensure certain events have priority in being executed
     *Horizontal swipe detection and event firing is done here
     **/
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: //Touch is detected on screen
                mDownX = ev.getX(); //Initial x-axis position
                mDownY = ev.getY(); //Initial y-axis position
                mSwiping = false; //Boolean to flag swipe occurred
                left = false; //Boolean to flag direction
                break;
            case MotionEvent.ACTION_CANCEL: break;
            case MotionEvent.ACTION_UP: //Touch has left the screen
                if(mSwiping) { //Swipe occurred
                    if(left) //Swipe direction was to the left
                    {
                        intent = new Intent(this, MainActivity.class);
                        startActivity(intent); //Sends the user to the New Task activity
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE: //Position of touch has changed without leaving the screen
                float x = ev.getX(); //Final x-axis position
                float y = ev.getY(); //Final y-axis position
                float xDelta = Math.abs(x - mDownX); //Distance travelled on x-axis
                float yDelta = Math.abs(y - mDownY); //Distance travelled on y-axis

                if (xDelta > touch && xDelta / 2 > yDelta + 50) { //Horizontal swipe gesture detected
                    mSwiping = true;
                    if(x > mDownX)
                    {
                        left = false;
                    }
                    else if(x < mDownX)
                    {
                        left = true;
                    }
                    return true; //Click event is consumed here
                }
                break;
        }
        return super.dispatchTouchEvent(ev); //No swipe detected, click event proceed to remaining listeners
    }

    //Function to reload the activity
    public  void reLoad(int count)
    {
        finish(); //Ends the current activity
        overridePendingTransition(0,0);//Default Android animation is removed for visual purposes
        startActivity(getIntent()); //Restart the Task List activity to reload the task list
        overridePendingTransition(0,0);//Default Android animation is removed for visual purposes
    }

    //Function to handle resumption of the activity
    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        //IMPORTANT to avoid draining too much battery power
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    //Function to handle pausing of the activity
    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        //IMPORTANT to avoid draining too much battery power
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
}

