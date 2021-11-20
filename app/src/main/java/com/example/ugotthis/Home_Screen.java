//Default homescreen of the uGotThis app
package com.example.ugotthis;
//Java imports
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Home_Screen extends AppCompatActivity implements View.OnClickListener,MyRecyclerViewAdapter.ItemClickListener{
    Button task, newtask, logout;
    Intent intent;
    TextView welcome;
    String greetings , user = FirebaseAuth.getInstance().getCurrentUser().getUid(); //Gets the ID of the current logged in user
    Boolean mSwiping, left;
    float mDownX, mDownY, touch;
    MyRecyclerViewAdapter adapter;
    ProgressBar bar;
    List<Task> taskList = new ArrayList<>(); //Array List to fill Recycler View
    DatabaseReference database = FirebaseDatabase.getInstance().getReference(); //Gets a reference for a new instance of the Firebase Database
    DatabaseReference ref, myRef = database.child("username").child(user); //Sets the path Firebase references
    View view;
    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home__screen);
        view = findViewById(R.id.HomePage);
        bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);
        view.setOnTouchListener(new StandardGestures(this)); //Enable pinch gesture zoom in/out

        // Reads information from the database
        myRef.addValueEventListener(new ValueEventListener() { //Listens for a single event
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //Firebase data has changed
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot == null)
                {
                    Toast.makeText(getApplicationContext(), "An error has occured in the database", Toast.LENGTH_LONG).show();
                }
                else
                {
                    greetings = dataSnapshot.child("user").getValue().toString(); //Gets the username from Firebase of the current user
                    welcome = findViewById(R.id.textView);
                    welcome.setText("Welcome, \n" +greetings); //Sets the welcome text to welcome the user
                    welcome.setWidth(20);
                    if(!dataSnapshot.hasChild("tasks")) //Checks if the user has any tasks, and creates an alert to inform the user if they do not
                    {
                        bar.setVisibility(View.GONE);
                        AlertDialog.Builder builder = new AlertDialog.Builder(Home_Screen.this);
                        builder.setTitle("Editing");
                        builder.setMessage("You have no tasks! \nDo you want to make one?");
                        builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                bar.setVisibility(View.VISIBLE);
                                Intent intent = new Intent(Home_Screen.this, NewTask.class);
                                intent.putExtra("ID", user);
                                startActivity(intent);//Sends the user to the NewTask activity
                                bar.setVisibility(View.GONE);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else //The user has at least one task
                    {
                        ref = database.child("username/" + user + "/tasks" ); //Sets the path Firebase references
                        ref.addListenerForSingleValueEvent(new ValueEventListener() { //Listens for a single event
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) { //Data is changed in Firebase
                                taskList.clear(); //Clears the task list to avoid possible duplication of items
                                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) { //Required to handle multiple results
                                    Task taskL = eventSnapshot.getValue(Task.class); //Reads an item from Firebase as an object
                                    taskList.add(taskL); //Adds the object to the task list
                                }

                                RecyclerView recyclerView = findViewById(R.id.Recycling);
                                recyclerView.setLayoutManager(new LinearLayoutManager(Home_Screen.this));
                                adapter = new MyRecyclerViewAdapter(Home_Screen.this, taskList);
                                adapter.setIsChecked(""); //Sets the adapter checked value to empty for visual purposes
                                //Sorts the recyclerview to show uncomplete tasks first
                                Collections.sort(taskList, new Comparator<Task>() {
                                    @Override
                                    public int compare(Task lhs, Task rhs) {
                                        return Boolean.compare(lhs.getComp(), rhs.getComp());
                                    }
                                });
                                adapter.notifyDataSetChanged();

                                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext());
                                recyclerView.addItemDecoration(dividerItemDecoration); //Adds divider decoration to the Recyclerview

                                recyclerView.setAdapter(adapter);
                                bar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { //Query is cancelled
                                Log.e("TAG", "onCancelled", databaseError.toException());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { //Query has been cancelled
                // Failed to read value
                throw error.toException();
            }
        });


       // Tasks db = new Tasks(this); Unused SQLite code, saved for posteriy
        //List<Task> taskList = db.getAllTasks(identity); Unused SQLite code, saved for posterity

        task = findViewById(R.id.Task_List);
        task.setOnClickListener(this);
        newtask = findViewById(R.id.New_Task);
        newtask.setOnClickListener(this);
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(this);

        ViewConfiguration vc = ViewConfiguration.get(view.getContext());
        touch = vc.getScaledTouchSlop(); //Get slop for accurate position determination


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
                        intent = new Intent(this, TaskList.class);
                        intent.putExtra("ID", user);
                        startActivity(intent); //Sends the user to the Task List activity
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                    else //Swipe direction was to the right
                    {
                        intent = new Intent(this, NewTask.class);
                        intent.putExtra("ID", user);
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

    // Pythagorean Theorem distance maker method

    public static float makeDistance(float x1, float y1, float x2, float y2) {
        float delta1 = (x2 - x1) * (x2 - x1);
        float delta2 = (y2 - y1) * (y2 - y1);
        float distance = (float) Math.sqrt(delta1 + delta2);
        return distance;
    }

    //Function to handle onclick events
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            //"Task List" button is clicked
            case R.id.Task_List: intent = new Intent(this, TaskList.class);
                bar.setVisibility(View.VISIBLE);
                intent.putExtra("ID", user);
                startActivity(intent); //Sends the user to the Task List activity
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                bar.setVisibility(View.GONE);
                break;
            //"New Task" button is clicked
            case R.id.New_Task: intent = new Intent(this, NewTask.class);
                bar.setVisibility(View.VISIBLE);
                intent.putExtra("ID", user);
                startActivity(intent); //Sends the user to the New Task activity
                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                bar.setVisibility(View.GONE);
                break;
            //"Log Out" button is clicked
            case R.id.logout:
                bar.setVisibility(View.VISIBLE);
                FirebaseAuth.getInstance().signOut(); //Signs the current user out of Firebase and the app
                finish(); //Ends the activity
                startActivity(new Intent(Home_Screen.this, MainActivity.class)); //Sends the user to the login page
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                bar.setVisibility(View.GONE);
                break;
        }
    }

    //Handles onclick event for the RecyclerView
    @Override
    public void onItemClick(View view, int position) {
        intent = new Intent(this, TaskList.class);
        intent.putExtra("ID", user);
        bar.setVisibility(View.GONE);
        startActivity(intent); //Sends the user to the Task List activity
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


