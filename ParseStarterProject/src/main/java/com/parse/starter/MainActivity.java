/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import bolts.Task;


public class MainActivity extends ActionBarActivity {

    EditText usernameText;
    EditText passwordText;
    Button buttonSignin;
    Button buttonSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        // If there's a session open skip the sign in phase.
        ParseUser currentUser = ParseUser.getCurrentUser();
        Task<ParseSession> session = ParseSession.getCurrentSessionInBackground();
        /*if(currentUser != null && currentUser.getUsername() != null) {
            Intent i = new Intent(this, Main2Activity.class);
            startActivity(i);
        } */

        usernameText = (EditText) findViewById(R.id.username_signin);
        passwordText = (EditText) findViewById(R.id.password_signin);

        buttonSignin = (Button) findViewById(R.id.signin_button);
        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(usernameText.getText().toString(), passwordText.getText().toString());
            }
        });

        buttonSignup = (Button) findViewById(R.id.signup_linkButton);
        buttonSignup.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Sign in method.
    private void signIn(String name, String password) {
        ParseUser.logInInBackground(name, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(user != null) {
                    // Log in ok.
                    Intent i = new Intent(MainActivity.this, Main2Activity.class);
                    startActivity(i);
                } else {
                   // Log in not ok.
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
    }
}
