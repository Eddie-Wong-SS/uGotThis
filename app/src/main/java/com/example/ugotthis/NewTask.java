//Allows a user to add a new task, which will be stored in Firebase under their record
package com.example.ugotthis;
//Java imports
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.IOException;
import java.util.UUID;

public class NewTask extends AppCompatActivity implements View.OnClickListener {
    EditText taskName, taskDesc;
    ImageView image;
    Button Submit;
    Intent intent;
    String uid, photo="", url = "";
    Boolean mSwiping, left;
    float mDownX, mDownY, touch;
    View view;

    ProgressBar bar;
    Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;
    final int REQUEST_CODE = 1; //Used to check if the returned image was called from the app
    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        //Assigns elements from the activity for coding purposes
        intent = getIntent();
        uid = intent.getStringExtra("ID"); //Gets the ID of the current logged in user
        Submit = findViewById(R.id.button);
        Submit.setOnClickListener(this);
        bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);

        image = findViewById(R.id.upload);
        image.setOnClickListener(this);

        storage = FirebaseStorage.getInstance(); //Gets an instance of Firebase Storage
        storageReference = storage.getReference();

        view = findViewById(R.id.NewTask);
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

        bar.setVisibility(View.GONE);
    }

    //Function to handle various onclick events
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            //If "Create" button was selected
            case R.id.button :
                taskName = findViewById(R.id.taskName);
                taskDesc = findViewById(R.id.taskDesc);
                //Checks if all required fields are filled when the "Create" button is called
                if(taskName.getText().toString().trim().length() == 0 || taskDesc.getText().toString().trim().length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_LONG).show();
                }
                else
                {
                    //Tasks db = new Tasks(this); Unused SQLite code, saved for posterity
                    bar.setVisibility(View.VISIBLE);
                    if(filePath != null) //User has selected an image
                    {
                        final ProgressDialog progressDialog = new ProgressDialog(this); //Assure user that the app is running correctly
                        progressDialog.setTitle("Uploading...");
                        progressDialog.show();
                        photo = "images/" + UUID.randomUUID().toString(); //Sets the image location and name
                        final StorageReference ref = storageReference.child(photo);
                        ref.putFile(filePath) //Uploads the image to Firebase storage
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //Image is successfully uploaded
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.d("TAG", "onSuccess: uri= "+ uri.toString());
                                                url = uri.toString(); //Gets the download URL of the newly uploaded image
                                                Task check = new Task(taskName.getText().toString().trim(), taskDesc.getText().toString().trim(),url,photo, false);
                                                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                                                database.child("username").child(uid).child("tasks").push().setValue(check); //Saves the task as a new record in Firebase
                                                bar.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) { //Image failed to upload
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Failed "+e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) { //Uploading of image in progress
                                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                                .getTotalByteCount());
                                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                                    }
                                });
                    }
                    else //Sets a default image should the user not choose an image
                    {
                        //Default image location
                        photo = "images/taskview.jpg";
                        //Default image download URL
                        url = "https://firebasestorage.googleapis.com/v0/b/ugotthis-96012.appspot.com/o/images%2Ftaskview.jpg?alt=media&token=c5d16be1-a03c-4e3f-b50c-73ebb6470aa3";
                        Task check = new Task(taskName.getText().toString().trim(), taskDesc.getText().toString().trim(),url, photo, false);
                        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                        database.child("username").child(uid).child("tasks").push().setValue(check); //Adds the task to Firebase
                        bar.setVisibility(View.GONE);
                    }

                    //db.addTask(check); Unused SQLite code, saved for posterity
                    //Handler to delay the subsequent code commands to ensure Firebase has sufficient time to make the new task
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "New Task Successfully Made", Toast.LENGTH_LONG).show();
                            bar.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(NewTask.this, Home_Screen.class);
                            startActivity(intent); //Returns user to the homescreen
                        }
                    }, 2000);
                }
                break;
            case R.id.upload : //User has chosen to change the image
                pickImage();
                break;
        }
    }
    //Opens a chooser to allow the user to select the photo gallery to obtain an image from
    private void pickImage()
    {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, REQUEST_CODE); //REQUEST_CODE = 1
    }

    //Function that handles the returned image
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //Checks the returned image to ensure it is the right image, and is not empty
        // Result code is RESULT_OK only if the user selects an Image
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null )
        {
            filePath = data.getData(); //Gets the image filepath
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                image.setImageBitmap(bitmap); //Sets the new image as the task image
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
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
                        intent = new Intent(this, Home_Screen.class);
                        startActivity(intent); //Sends the user to the Task List activity
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                    else //Swipe direction was to the right
                    {
                        intent = new Intent(this, TaskList.class);
                        intent.putExtra("ID", uid);
                        startActivity(intent); //Sends the user to the Task List activity
                        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE: //Position of touch has changed without leaving the screen
                float x = ev.getX(); //Final x-axis position
                float y = ev.getY(); //Final y-axis position
                float xDelta = Math.abs(x - mDownX); //Distance travelled on x-axis
                float yDelta = Math.abs(y - mDownY); //Distance travelled on y-axis
                final float SWIPE_THRESHOLD = 250;

                if (xDelta > touch && xDelta / 2 > yDelta + 50 && xDelta > SWIPE_THRESHOLD) { //Horizontal swipe gesture detected
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
