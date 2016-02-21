/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;


public class SignInActivity extends ActionBarActivity {

    EditText usernameText;
    EditText passwordText;
    Button buttonSignin;
    Button buttonSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/Gotham Book.ttf");
        usernameText = (EditText) findViewById(R.id.username_signin);
        passwordText = (EditText) findViewById(R.id.password_signin);
        usernameText.setTypeface(face1);
        passwordText.setTypeface(face1);

        buttonSignin = (Button) findViewById(R.id.signin_button);
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/GOTHAM-BOLD.TTF");
        buttonSignin.setTypeface(face2);
        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(usernameText.getText().toString(), passwordText.getText().toString());
            }
        });

        buttonSignup = (Button) findViewById(R.id.signup_linkButton);
        buttonSignup.setTypeface(face2);
        buttonSignup.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(SignInActivity.this, SignUpActivity.class);
                        startActivity(i);
                    }
                });

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        usernameText.setText(savedInstanceState.getString(UserKey.USERNAME_KEY));
        passwordText.setText(savedInstanceState.getString(UserKey.PASSWORD_KEY));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UserKey.USERNAME_KEY, usernameText.getText().toString());
        outState.putString(UserKey.PASSWORD_KEY, passwordText.getText().toString());
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

    @Override
    public void onBackPressed() {
        // Used to go to the Home screen and not to the DispatchActivity.
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Sign in method.
    private void signIn(String name, String password) {

        ParseUser.logInInBackground(name, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {

                if (user != null) {
                    // Log in ok.
                    Intent intent = new Intent(SignInActivity.this, DispatchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    // Log in not ok.
                    Toast.makeText(SignInActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
