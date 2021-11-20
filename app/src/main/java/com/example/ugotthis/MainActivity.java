//Allows the user to log into the app or go to register a new account
package com.example.ugotthis;
//Java imports
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    EditText password;
    EditText email;
    View view;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance(); //Gets an instance of Firebase authentication

        if (auth.getCurrentUser() != null) { //Checks if the user has previously logged in the app and has not signed out
            //startActivity(new Intent(MainActivity.this, Home_Screen.class)); //Sends the user to the home screen
            //finish(); //Ends the current activity
        }
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.Email);

        view = findViewById(R.id.LogIn);
        view.setOnTouchListener(new StandardGestures(this)); //Enable pinch gesture zoom in/out
    }

    //Handles events when "Login" button is clicked
    public void loginCheck(View v)
    {
        email = findViewById(R.id.Email);
        password = findViewById(R.id.Password);
        //Checks if all required fields have been filled
        if(email.getText().toString().trim().length() == 0 || password.getText().toString().length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_LONG).show();
        }
        else //All required fields are filled
        {
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            String mail = email.getText().toString();
            String pass = password.getText().toString();
            //authenticate user
            auth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) { //Authentication complete
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            progressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                // there was an error
                                Toast.makeText(getApplicationContext(), "There was an error with your email or password", Toast.LENGTH_LONG).show();
                            } else {
                                Intent intent = new Intent(MainActivity.this,Home_Screen.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

            //Unused code for SQLite data storage, kept for posterity
           /* Users db = new Users(MainActivity.this);
            User login = db.getUser(logname.getText().toString().trim(), password.getText().toString());
            if (login.getUser().equals("")) {
                Toast.makeText(getApplicationContext(), "Login failed, try again", Toast.LENGTH_LONG).show();
            } else {
                intent = new Intent(MainActivity.this, Home_Screen.class);
                intent.putExtra("USERNAME", login.getUser());
                intent.putExtra("ID", login.getId());
                startActivity(intent);
            }*/
        }
    }

    //Handles events when "Register New" button is clicked
    public void signUp(View v)
    {
        intent = new Intent(MainActivity.this, Register.class);
        startActivity(intent);//Sends the user to the Register activity
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
    }
}

