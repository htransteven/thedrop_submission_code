package com.example.thedrop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;


public class LoginScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        SharedPreferences myPrefs;
        myPrefs = getSharedPreferences("venmoUsername", Context.MODE_PRIVATE);
        String data = myPrefs.getString("v_un","...");

        boolean flag_change_venmo = false;
        if(getIntent().hasExtra("changing_venmo")) {
            if (getIntent().getExtras().getString("changing_venmo").equals("true")) {
                flag_change_venmo = true;
            }
        }

        if (!flag_change_venmo && !data.equals("...")){
            Intent mapsActivity = new Intent(LoginScreen.this, MapsActivity.class);
            mapsActivity.putExtra("username", data);
            startActivity(mapsActivity);
            finish();
        }
    }

    public void saveVenmoUsername(View v){

        EditText username = (EditText)findViewById(R.id.venmo_username);

        SharedPreferences myPrefs;
        myPrefs = getSharedPreferences("venmoUsername", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putString("v_un", username.getText().toString());
        editor.apply();


        Intent mapsActivity = new Intent(LoginScreen.this, MapsActivity.class);
        mapsActivity.putExtra("username", username.getText().toString());
        startActivity(mapsActivity);
        finish();
    }

}
