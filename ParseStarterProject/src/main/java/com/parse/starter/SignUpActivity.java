package com.parse.starter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    EditText usernameText;
    EditText emailText;
    EditText passwordText;
    Button buttonSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameText = (EditText) findViewById(R.id.username_signup);
        emailText = (EditText) findViewById(R.id.email_signup);
        passwordText = (EditText) findViewById(R.id.password_signup);

        buttonSignup = (Button) findViewById(R.id.signup_button);
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               signUp(usernameText.getText().toString(), emailText.getText().toString(), passwordText.getText().toString());
            }
        });
    }

    private void signUp(String username, String email, String password) {

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Signup succeed.
                    Intent i = new Intent(SignUpActivity.this, Main2Activity.class);
                    startActivity(i);
                } else {
                    // Signup didn't succeed.
                    new AlertDialog.Builder(SignUpActivity.this)
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
