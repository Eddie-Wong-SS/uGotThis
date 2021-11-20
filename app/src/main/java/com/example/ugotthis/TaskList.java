//Shows the tasks of the current user, and allows the user to do certain related functions(editing, deleting, etc)
package com.example.ugotthis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskList extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    Intent intent;
    String uid, pURL, pLoc, checked;
    MyRecyclerViewAdapter adapter;
    ImageView image, tImage;
    TextView text;
    EditText name, descp;
    Boolean mSwiping, left;
    float mDownX, mDownY, touch;
    View view;
    List<Task> taskList = new ArrayList<>(); //ArrayList to insert items into RecyclerView
    private ColorStateList colors;
    int color[] = new int[]
            {
                    Color.CYAN,
                    Color.YELLOW
            };
    FirebaseDatabase database = FirebaseDatabase.getInstance(); //Gets a new instance of the database
    DatabaseReference ref;
    FirebaseStorage storage;
    StorageReference storageReference;
    ProgressBar bar;
    final int REQUEST_CODE = 1; //Code to ensure the returned image is the requested image
    Uri filePath;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private ScaleGestureDetector mScaleGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        //Tasks db = new Tasks(this); Legacy SQLite code, saved for posterity
        storage = FirebaseStorage.getInstance(); //Gets a new instance of Firebase
        storageReference = storage.getReference();
        bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);
        intent = getIntent();
        uid = intent.getStringExtra("ID");

        taskList.clear();
        ref = database.getReference("username/" + uid + "/tasks" ); //Sets the path the DatabaseReference refers to
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskList.clear(); //Clears tasklist to prevent possible item duplication
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Task taskL = eventSnapshot.getValue(Task.class); //Gets an item from Firebase as an object
                    taskList.add(taskL); //Add the object into the tasklist
                }

                RecyclerView recyclerView = findViewById(R.id.Recycling);
                LinearLayoutManager layoutManager = new LinearLayoutManager((TaskList.this));
                recyclerView.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(),
                        recyclerView, new RecyclerItemListener.RecyclerTouchListener() {
                    @Override
                    public void onLongClickItem(View v, int position) {
                        showAlertDialogButtonClicked(v, position);
                    }
                }));
                recyclerView.setLayoutManager(layoutManager);
                adapter = new MyRecyclerViewAdapter(TaskList.this, taskList);
                adapter.setClickListener(TaskList.this);
                adapter.setIsChecked(""); //Sets the adapter checked variable value to empty for visual purposes
                //Sorts the recyclerview to show uncomplete tasks first
                Collections.sort(taskList, new Comparator<Task>() {
                    @Override
                    public int compare(Task lhs, Task rhs) {
                        return Boolean.compare(lhs.getComp(), rhs.getComp());
                    }
                });

                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext());
                recyclerView.addItemDecoration(dividerItemDecoration); //Adds divider decoration to the Recyclerview
                recyclerView.setAdapter(adapter);
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

        view = findViewById(R.id.TaskList);
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
                checkSum(count);
            }
        });
    }

    //Handles onitemclick events for individual RecyclerView items
    //Allows users to mark tasks as complete/incomplete
    @Override
    public void onItemClick(View view, int position) {
        image = view.findViewById(R.id.compImg);
        text = view.findViewById(R.id.task_view);
        String[] replace = text.getText().toString().split("\n"); //Obtains the text and description
        //Checks if the task is completed or not based on the image tag
        if("checks".equals(image.getTag())) //Task was completed
        {
            try
            {
                Query check = ref.orderByChild("name").equalTo(replace[0]); //Locates the record with the corresponding task name
                check.addListenerForSingleValueEvent(new ValueEventListener() { //Listens for a single event
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //Firebase data changed
                        for(DataSnapshot flag : dataSnapshot.getChildren()) //Required in case of multiple results
                        {
                            image.setImageResource(R.drawable.ic_unchecked); //Visually show task is incomplete
                            image.setTag("uncheck"); //Changes image tag to indicate it is incomplete
                            flag.getRef().child("comp").setValue(false); //Marks the task as incomplete in Firebase
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { //Query is cancelled
                        throw databaseError.toException();
                    }
                });
            }
            catch (Exception e) { //Error occurred
                    e.printStackTrace();
            }
        }
        else //Task was incomplete
        {
            try
            {
                Query check = ref.orderByChild("name").equalTo(replace[0]); //Locates the record with the corresponding task name
                check.addListenerForSingleValueEvent(new ValueEventListener() { //Listens for a single event
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //Firebase data changed
                        for(DataSnapshot flag : dataSnapshot.getChildren()) //Required in case of multiple results
                        {
                            image.setImageResource(R.drawable.ic_checked); //Visually show task is completed
                            image.setTag("checks"); //Changes image tag to indicate it is completed
                            flag.getRef().child("comp").setValue(true); //Marks the task as completed in Firebase

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { //Query is cancelled
                        throw databaseError.toException();
                    }
                });
            }
            catch (Exception e) { //Error occured
                e.printStackTrace();
            }
        }
    }

    //Creates an alert dialog that allows the user to edit, delete or send as SMS the selected task and its details
    public void showAlertDialogButtonClicked(View view, int position) {
        text = view.findViewById(R.id.task_view);
        pLoc = text.getTag().toString();
        //New textviews to be placed in the alertdialog
        final TextView titname = new TextView(this);
        final TextView titdescp = new TextView(this);

        String[] replace = text.getText().toString().split("\n"); //Obtains the text and description as they are originally in one single textview
        String[] tags = pLoc.split("\n");
        //New edittexts to be placed in the alertdialog
        name = new EditText(this);
        descp = new EditText(this);
        tImage = new ImageView(this);

        ImageView editIm = view.findViewById(R.id.task_img);
        pLoc = tags[0];
        pURL = tags[1];
        //Set TextView text
        titname.setText("Task Name: ");
        titdescp.setText("Task Description: ");
        //Set EditText values
        name.setText(replace[0], TextView.BufferType.EDITABLE);
        descp.setText(replace[1], TextView.BufferType.EDITABLE);
        int maxLengthofEditText = 250; //Sets the maximum length of the task description
        descp.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthofEditText)});
        descp.setHint("250 characters max");

        text = view.findViewById(R.id.task_view);
        //Set the image default srccompat and parameters
        tImage.setImageDrawable(editIm.getDrawable());
        tImage.setScaleType(ImageView.ScaleType.FIT_XY);
        tImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 10));
        tImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        }); //Allows the user to select a new image

        colors = ColorStateList.valueOf(color[0]);
        name.setBackgroundTintList(colors);
        descp.setBackgroundTintList(colors);
        colors = ColorStateList.valueOf(color[1]);
        name.setTextColor(getResources().getColor(R.color.edit_appearance));
        descp.setTextColor(getResources().getColor(R.color.edit_appearance));
        final String origin = name.getText().toString(); //Gets the original task name before possible modifications for deletion querying purposes
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this); //Defines a new layout for the alertdialog to use
        layout.setOrientation(LinearLayout.VERTICAL); //Defines the layout orientation
        builder.setTitle("Editing");
        //Adds elements to the layout
        layout.addView(tImage);
        layout.addView(titname);
        layout.addView(name);
        layout.addView(titdescp);
        layout.addView(descp);
        //Set the layout the alertdialog will use
        builder.setView(layout);

        // add the buttons
        builder.setPositiveButton("Send SMS", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendSMS(name.getText().toString(), descp.getText().toString()); //Creates an intent that will allow the user to send the image
            }
        });

        builder.setNegativeButton("Edit" , new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                bar.setVisibility(View.VISIBLE);
                if(name.getText().toString().trim().length() == 0 || descp.getText().toString().trim().length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Task names and descriptions must both not be empty!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Query edit = ref.orderByChild("name").equalTo(origin); //Gets the corresponding task based on the task name
                    edit.addListenerForSingleValueEvent(new ValueEventListener() { //Listens for a single event
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //Firebase data changed
                            for (DataSnapshot update : dataSnapshot.getChildren()) //Required in case of multiple results
                            {
                                if (filePath != null) //Checks if the image has been changed
                                {
                                    //The user chose to upload an image instead of continuing to use the default image
                                    if (pURL.equals("https://firebasestorage.googleapis.com/v0/b/ugotthis-96012.appspot.com/o/images%2Ftaskview.jpg?alt=media&token=c5d16be1-a03c-4e3f-b50c-73ebb6470aa3")) {
                                        pLoc = "images/" + UUID.randomUUID().toString();
                                        final StorageReference ref = storageReference.child(pLoc);
                                        ref.putFile(filePath) //Uploads the image to Firebase storage
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //Image is successfully uploaded
                                                        Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                                                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Log.d("TAG", "onSuccess: uri= " + uri.toString());
                                                                pURL = uri.toString();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) { //Image failed to upload
                                                        Toast.makeText(getApplicationContext(), "Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                })
                                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) { //Uploading of image in progress
                                                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                                                .getTotalByteCount());
                                                    }
                                                });
                                    } else {
                                        final StorageReference ref = storageReference.child(pLoc); //Gets the location of the corresponding photo of the task
                                        ref.putFile(filePath) //Uploads image to Firebase Storage
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //image is successfully uploaded
                                                        Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                                                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Log.d("TAG", "onSuccess: uri= " + uri.toString());
                                                                pURL = uri.toString(); //Gets the new download URL of the newly uploaded image
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) { //Image failed to upload
                                                        Toast.makeText(getApplicationContext(), "Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                })
                                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) { //Upload of image in progress
                                                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                                                .getTotalByteCount());
                                                    }
                                                });
                                    }
                                }

                                Map<String, Object> updates = new HashMap<>(); //Creates an array for updating so listener only needs to fire once to update all info
                                //Place values in the array
                                updates.put("name", name.getText().toString());
                                updates.put("descp", descp.getText().toString());
                                updates.put("comp", false);
                                updates.put("photoLoc", pLoc);
                                updates.put("photoURL", pURL);
                                update.getRef().updateChildren(updates); //Update the corresponding Firebase record
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { //Query is cancelled
                            Log.e("TAG", "onCancelled", databaseError.toException());
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Task edited successfully", Toast.LENGTH_LONG).show();
                    dialogInterface.dismiss();
                    finish(); //Ends the current activity
                    overridePendingTransition(0, 0); //Default Android animation is removed for visual purposes
                    startActivity(getIntent()); //Restart the Task List activity to reload the task list
                    overridePendingTransition(0, 0); //Default Android animation is removed for visual purposes
                }
            }
        });

        builder.setNeutralButton("Delete Task" , new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                bar.setVisibility(View.VISIBLE);
                Query deletion = ref.orderByChild("name").equalTo(origin); //Gets the corresponding task based on task name
                deletion.addListenerForSingleValueEvent(new ValueEventListener() { //Listens for a single event
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //Firebase data changed
                        for(DataSnapshot removal: dataSnapshot.getChildren()) //Required in case of multiple results
                        {
                            if(!pLoc.equals("images/taskview.jpg")) //Checks if task image is the default image to avoid deletion of default image
                            {
                                StorageReference photoRef = storage.getReferenceFromUrl(pURL); //Gets a path to the image location in Firebase Storage
                                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) { //Image deleted successfully
                                        // File deleted successfully
                                        Log.d("Deleted", "onSuccess: deleted file");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) { //Image failed to be deleted
                                        // Uh-oh, an error occurred!
                                        Log.d("Failed", "onFailure: did not delete file");
                                    }
                                });
                            }
                            removal.getRef().removeValue(); //Removes the corresponding task and details from Firebase
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { //Query is cancelled
                        Log.e("TAG", "onCancelled", databaseError.toException());
                    }
                });
                Toast.makeText(getApplicationContext(), "Task deleted successfully" , Toast.LENGTH_LONG).show();
                dialogInterface.dismiss();
                finish(); //Ends the current activity
                overridePendingTransition(0,0);//Default Android animation is removed for visual purposes
                startActivity(getIntent()); //Restart the Task List activity to reload the task list
                overridePendingTransition(0,0);//Default Android animation is removed for visual purposes
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        //set custom appearance and layout for alertdialog buttons
        Button pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pos.setBackgroundResource(R.drawable.glass_button_alert);
        int width  = getResources().getDimensionPixelSize(R.dimen.pos_width);
        pos.setWidth(width);

        Button neu = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        neu.setBackgroundResource(R.drawable.glass_button_alert);
        width  = getResources().getDimensionPixelSize(R.dimen.neu_width);
        neu.setWidth(width);

        Button neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        neg.setBackgroundResource(R.drawable.glass_button_alert);
        width  = getResources().getDimensionPixelSize(R.dimen.neg_width);
        neg.setWidth(width);

        //Sets layout parameters for elements in the layout to use
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
        layoutParams.gravity = Gravity.CENTER; //this is layout_gravity
        //Assign layout parameters to buttons
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLayoutParams(layoutParams);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setLayoutParams(layoutParams);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLayoutParams(layoutParams);
    }

    //Function to handle onclick event when the user presses the new task image(ID:imageButton)
    public void setTask(View v)
    {
        Intent intent = new Intent(TaskList.this, NewTask.class);
        intent.putExtra("ID", uid);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
    }

    //calls the phone's messaging app to send an SMS
    void sendSMS(String name, String descp)
    {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this); // In case user changed default messaging app

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra("sms_body"  , "My new task! \n Name:"+ name + "\n What will I do? \n" + descp);

            if (defaultSmsPackageName != null)// Can be null in case that there is no default, then the user would be able to choose
            // any app that support this intent.
            {
                sendIntent.setPackage(defaultSmsPackageName);
            }
            startActivity(sendIntent); //Starts the SMS app
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
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Checks the returned image to ensure it is the right image, and is not empty
        // Result code is RESULT_OK only if the user selects an Image
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null )
        {
            filePath = data.getData(); //Gets the image filepath
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(filePath));
                tImage.setImageBitmap(bitmap); //Sets the new image as the task image
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
                        intent = new Intent(this, NewTask.class);
                        intent.putExtra("ID", uid);
                        startActivity(intent); //Sends the user to the New Task activity
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                    else //Swipe direction was to the right
                    {
                        intent = new Intent(this, Home_Screen.class);
                        startActivity(intent); //Sends the user to the Home Page
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

    //Function to mark all tasks as complete/incomplete
    public void checkSum(int count)
    {
        if(count == 1)
        {
            checked = adapter.updateChecks(); //calls the RecyclerView function to visually mark all tasks as complete/incomplete
            adapter.notifyDataSetChanged(); //Notify the adapter that RecyclerView item data has been changed

            if (checked.equals("1")) //Tasks are marked complete
            {
                Query check = ref.orderByChild("comp").equalTo(false); //Queries for tasks that have been marked incomplete
                check.addListenerForSingleValueEvent(new ValueEventListener() { //Listens for a single event
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //Firebase data changed
                        for (DataSnapshot flag : dataSnapshot.getChildren()) { //Required in case of multiple results
                            flag.getRef().child("comp").setValue(true); //Sets the task completion status to complete
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { //Query has been cancelled
                        Log.e("TAG", "onCancelled", databaseError.toException());
                    }
                });
            } else if (checked.equals("2")) //Tasks are marked incomplete
            {
                Query check = ref.orderByChild("comp").equalTo(true); //Queries for tasks that are marked complete
                check.addListenerForSingleValueEvent(new ValueEventListener() { //Listens for a single value
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //firebase data changed
                        for (DataSnapshot flag : dataSnapshot.getChildren()) //Required in case of multiple results
                        {
                            flag.getRef().child("comp").setValue(false); //Sets the task completion status to incomplete
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { //Query has been cancelled
                        Log.e("TAG", "onCancelled", databaseError.toException());
                    }
                });
            }
        }
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
