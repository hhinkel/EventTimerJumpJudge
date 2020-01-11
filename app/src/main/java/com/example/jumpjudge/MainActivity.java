package com.example.jumpjudge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Global Variables
    Button[] btn = new Button[17];
    EditText userInput;

    private Spinner divisionSpinner;
    private String division;

    private Spinner fenceSpinner;
    private String fence;

    private Spinner otherSpinner;
    private String other;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private boolean permissionGranted;

    private boolean timerRunning = false;
    private Chronometer timer;
    private long holdTime;

    private int refusals = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refusals = 0;

        if (savedInstanceState != null) {
            String number = savedInstanceState.getString("number");
            division = savedInstanceState.getString("division");
            userInput = findViewById(R.id.numberEntered);
            userInput.setText(number);
        }

        //Register the buttons
        btn[0] = findViewById(R.id.button0);
        btn[1] = findViewById(R.id.button1);
        btn[2] = findViewById(R.id.button2);
        btn[3] = findViewById(R.id.button3);
        btn[4] = findViewById(R.id.button4);
        btn[5] = findViewById(R.id.button5);
        btn[6] = findViewById(R.id.button6);
        btn[7] = findViewById(R.id.button7);
        btn[8] = findViewById(R.id.button8);
        btn[9] = findViewById(R.id.button9);
        btn[10] = findViewById(R.id.buttonBack);
        btn[11] = findViewById(R.id.buttonClear);
        btn[12] = findViewById(R.id.buttonEnter);
        btn[13] = findViewById(R.id.refusal);
        btn[14] = findViewById(R.id.hold);
        btn[15] = findViewById(R.id.clearHold);

        divisionSpinner = findViewById(R.id.spinner_division);
        fenceSpinner = findViewById(R.id.spinnerFenceNum);
        otherSpinner = findViewById(R.id.spinnerOther);
        timer = findViewById(R.id.timer);

        for (int i = 0; i < 16; i++) {
            btn[i].setOnClickListener(this);
        }

        setupDivisionSpinner();
        setupFenceSpinner();
        setupOtherSpinner();
    }

     @Override
    public void onClick(View view) {

        TextView refusalTextView = findViewById(R.id.refusalText);

        switch (view.getId()) {
            case R.id.button0:
                addToArray("0");
                break;
            case R.id.button1:
                addToArray("1");
                break;
            case R.id.button2:
                addToArray("2");
                break;
            case R.id.button3:
                addToArray("3");
                break;
            case R.id.button4:
                addToArray("4");
                break;
            case R.id.button5:
                addToArray("5");
                break;
            case R.id.button6:
                addToArray("6");
                break;
            case R.id.button7:
                addToArray("7");
                break;
            case R.id.button8:
                addToArray("8");
                break;
            case R.id.button9:
                addToArray("9");
                break;
            case R.id.buttonEnter:
                try {
                    enterNumber(userInput);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                refusals = 0;
                refusalTextView.setText(Integer.toString(refusals));
                changeBtnText("Jumped Clear", btn[12]);
                break;
            case R.id.refusal:
                refusals = refusals + 1;
                refusalTextView.setText(String.format("%d",refusals));
                changeBtnText("Enter", btn[12]);
                break;
            case R.id.hold:
                holdClicked();
                break;
            case R.id.clearHold:
                clearHold();
                break;
            case R.id.buttonClear:
                clearNumber(userInput);
                break;
            case R.id.buttonBack:
                goBackAChar(userInput);
                break;
        }
    }

    private void changeBtnText (String text, Button btn) {
        btn.setText(text);
    }

    private void setupDivisionSpinner() {
        ArrayAdapter divisionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_division_options, R.layout.division_spinner_item);

        divisionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        divisionSpinner.setAdapter(divisionSpinnerAdapter);

        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    division = selection;
                } else {
                    division = "Division Unknown";
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                division = "Division Unknown";
            }
        });
    }

    private void setupFenceSpinner() {
        List<String> fenceList = new ArrayList<String>();

        for (int i = 1; i < 40; i++)
            fenceList.add(Integer.toString(i));

        ArrayAdapter fenceSpinnerAdapter = new ArrayAdapter(this, R.layout.fence_spinner_item, fenceList);

        fenceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        fenceSpinner.setAdapter(fenceSpinnerAdapter);

        fenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    fence = selection;
                } else {
                    fence = "Fence Unknown";
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                fence = "Fence Unknown";
            }
        });
    }

    private void setupOtherSpinner() {
        ArrayAdapter otherSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_other_options, R.layout.other_spinner_item);

        otherSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        otherSpinner.setAdapter(otherSpinnerAdapter);

        otherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    other = selection;
                    if(!other.equals("None"))
                        changeBtnText("Enter", btn[12]);
                } else {
                    other = "None";
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                other = "None";
            }
        });
    }

    public void addToArray(String number) {
        userInput = findViewById(R.id.numberEntered);
        userInput.append(number);
    }

    public void clearNumber(EditText input) {
        int sLen = input.length();

        if (sLen > 0) {
            String selection = input.getText().toString();
            String result = input.getText().toString().replace(selection, "");
            input.setText(result);
            input.setSelection(input.getText().length());
            userInput = null;
        }
    }

    public void goBackAChar(EditText input) {
        int sLen = input.length();

        if (sLen > 0) {
            String selection = input.getText().toString().substring(sLen - 1, sLen);
            String result = input.getText().toString().replace(selection, "");
            input.setText(result);
            input.setSelection(input.getText().length());
            userInput = input;
        }
    }

    public void enterNumber(EditText input) throws MqttException, UnsupportedEncodingException {
        Calendar now = Calendar.getInstance();
        long jumpTime = now.getTimeInMillis();

        if (input == null) {
            showNumberErrorDialog(now, jumpTime);
        } else {
            processNumber(input.getText().toString(), now, jumpTime);
            clearNumber(userInput);
            timer.setBase(SystemClock.elapsedRealtime());
        }
    }

    private void processNumber(String input, Calendar now, long jumpTime) {
        Context context = getApplicationContext();
        showTimeNumber(context, input, now, refusals);
        Rider rider = saveRiderData(input, jumpTime);
        insertRider(rider);
        //TODO: Encrypt data
        MqttHelper mqttHelper = new MqttHelper(context);
        String msg = createMessageString(rider);
        mqttHelper.connect(msg);
    }

    public void showTimeNumber(Context context, String number, Calendar now, int refusals) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss:SS", Locale.getDefault());
        Date jumpTime = now.getTime();
        CharSequence text = "Rider: " + number + " Refusals: " + refusals + " Time: " + format.format(jumpTime);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public Rider saveRiderData(String number, long jumpTime) {
        int num = Integer.parseInt(number);
        return new Rider(num, division, Integer.parseInt(fence), refusals, jumpTime, other, holdTime, null);
    }

    private void insertRider(Rider rider) {

        ContentValues values = new ContentValues();
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_NUM, rider.getRiderNumber());
        values.put(RiderContract.RiderEntry.COLUMN_DIVISION, rider.getDivision());
        values.put(RiderContract.RiderEntry.COLUMN_FENCE_NUM, rider.getFenceNumber());
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_REFUSALS, rider.getRefusals());
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_TIME, rider.getJumpTime());
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_OTHER, rider.getOther());
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_HOLD, rider.getHoldTime());
        values.put(RiderContract.RiderEntry.COLUMN_EDIT, rider.getEdit());

        Uri newUri = getContentResolver().insert(RiderContract.RiderEntry.CONTENT_URI, values);
        Log.v("MainActivity", newUri + " value of newUri");
    }

    private String createMessageString(Rider rider) {

        return rider.getRiderNumber() + "," + rider.getDivision() + "," + rider.getFenceNumber()
                + "," + rider.getRefusals() + "," + rider.getJumpTime() + "," + rider.getOther()
                + "," + rider.getHoldTime() + "," + rider.getEdit();
    }

    public void showNumberErrorDialog(final Calendar now, final long jumpTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_popup, null);
        builder.setView(dialogView);

        userInput = dialogView.findViewById(R.id.add_number);

        builder.setTitle("Please enter the Rider Number");
        builder.setMessage("Enter Number");
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                processNumber(userInput.getText().toString(), now, jumpTime);
                clearNumber(userInput);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        showSoftNumPad(dialogView);
    }

    public void showSoftNumPad(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void holdClicked() {
        if(timerRunning) {
            timer.stop();
            holdTime = timer.getBase();
            changeBtnText("Hold", btn[14]);
            timerRunning = false;
        }
        else {
            timer.setBase(SystemClock.elapsedRealtime());
            timer.start();
            changeBtnText("reStart", btn[14]);
            timerRunning = true;
        }
    }

    public void clearHold() {
        if(timerRunning) {
            timer.stop();
            changeBtnText("Hold", btn[14]);
        }
        timer.setBase(SystemClock.elapsedRealtime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (userInput != null) {
            outState.putString("number", userInput.getText().toString());
            outState.putString("division", division);
            outState.putString("fence", fence);
            outState.putString("other", other);
            outState.putString("hold", String.format("%d", timer.getBase()));
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        holdTime = Long.parseLong(savedInstanceState.getString("hold"));
        timer.setBase(holdTime);
    }
}
