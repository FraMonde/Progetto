package com.parse.starter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.parse.ParseSession;
import com.parse.ParseUser;

public class DispatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);

        // Check if there is current user info. Bug with Parse logout, after that just the username is null.
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getUsername() != null) {
            // Start an intent for the logged in activity
            startActivity(new Intent(this, Main2Activity.class));
        } else {
            // Start and intent for the logged out activity
            startActivity(new Intent(this, SignInActivity.class));
        }
    }

}

