package com.example.jumpjudge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    //Global Variables
    Button[] btn = new Button[13];
    EditText userInput;
    ConstraintLayout relativeLayout;

    private Spinner divisionSpinner;
    private String division;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private boolean permissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
