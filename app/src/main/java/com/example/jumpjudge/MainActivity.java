package com.example.jumpjudge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    Button[] btn = new Button[16];
    EditText userInput;

    private Spinner divisionSpinner;
    private String division;

    private Spinner fenceSpinner;
    private String fence;

    private Spinner otherSpinner;
    private String other;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private boolean permissionGranted;

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
        btn[13] = findViewById(R.id.jumpedClear);
        btn[14] = findViewById(R.id.refusal);
        btn[15] = findViewById(R.id.hold);

        divisionSpinner = findViewById(R.id.spinner_division);
        fenceSpinner = findViewById(R.id.spinnerFenceNum);

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
            case R.id.jumpedClear:
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
                break;
            case R.id.refusal:
                refusals = refusals + 1;
                refusalTextView.setText(Integer.toString(refusals));
                break;
            case R.id.hold:
                break;
            case R.id.buttonClear:
                clearNumber(userInput);
                break;
            case R.id.buttonBack:
                goBackAChar(userInput);
                break;
        }
    }

    private void setupDivisionSpinner() {
        ArrayAdapter divisionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_division_options, android.R.layout.simple_spinner_item);

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

        ArrayAdapter fenceSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, fenceList);

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
                division = "Fence Unknown";
            }
        });
    }

    private void setupOtherSpinner() {
        ArrayAdapter otherSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_other_options, android.R.layout.simple_spinner_item);

        otherSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        otherSpinner.setAdapter(otherSpinnerAdapter);

        otherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    other = selection;
                } else {
                    other = "Division Unknown";
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                division = "None";
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
        long finishTime = now.getTimeInMillis();

        if (input == null) {
            showNumberErrorDialog(now, finishTime);
        } else {
            processNumber(input.getText().toString(), now, finishTime);
            clearNumber(userInput);
        }
    }

    private void processNumber(String input, Calendar now, long finishTime) {
        Context context = getApplicationContext();
        showTimeNumber(context, input, now, refusals);
        /*Rider rider = saveRiderData(input, finishTime);
        insertRider(rider);
        //TODO: Encrypt data
        MqttHelper mqttHelper = new MqttHelper(context);
        String msg = createMessageString(rider);
        mqttHelper.connect(msg); */
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

    public void showNumberErrorDialog(final Calendar now, final long finishTime) {
        /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_popup, null);
        builder.setView(dialogView);

        userInput = dialogView.findViewById(R.id.add_number);

        builder.setTitle("Please enter the Rider Number");
        builder.setMessage("Enter Number");
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                processNumber(userInput.getText().toString(), now, finishTime);
                clearNumber(userInput);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        showSoftNumPad(dialogView); */
    }
}
