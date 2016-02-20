package com.parse.starter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    private ParseUser newUser;

    EditText usernameText;
    EditText emailText;
    EditText passwordText;
    EditText passwordAgainText;
    Button buttonSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        newUser = new ParseUser();

        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/Gotham Book.ttf");
        usernameText = (EditText) findViewById(R.id.username_signup);
        emailText = (EditText) findViewById(R.id.email_signup);
        passwordText = (EditText) findViewById(R.id.password_signup);
        passwordAgainText = (EditText) findViewById(R.id.passwordAgain_signup);
        usernameText.setTypeface(face1);
        emailText.setTypeface(face1);
        passwordText.setTypeface(face1);
        passwordAgainText.setTypeface(face1);

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/GOTHAM-BOLD.TTF");
        buttonSignup = (Button) findViewById(R.id.signup_button);
        buttonSignup.setTypeface(face);
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(usernameText.getText().toString(), emailText.getText().toString(), passwordText.getText().toString(), passwordAgainText.getText().toString());
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        usernameText.setText(savedInstanceState.getString(UserKey.USERNAME_KEY));
        passwordText.setText(savedInstanceState.getString(UserKey.PASSWORD_KEY));
        passwordAgainText.setText(savedInstanceState.getString(UserKey.CONFIRMPASSWORD_KEY));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UserKey.USERNAME_KEY, usernameText.getText().toString());
        outState.putString(UserKey.PASSWORD_KEY, passwordText.getText().toString());
        outState.putString(UserKey.CONFIRMPASSWORD_KEY, passwordAgainText.getText().toString());
    }

    private void signUp(String username, String email, String password, String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Your password and confirmation password do not match.",
                    Toast.LENGTH_LONG).show();
        } else {
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.put(UserKey.GROUP_KEY, false);

            newUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        // Signup succeed.
                        // Start an intent for the dispatch activity.
                        Intent intent = new Intent(SignUpActivity.this, DispatchActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        // Signup didn't succeed.
                        Toast.makeText(SignUpActivity.this, e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
