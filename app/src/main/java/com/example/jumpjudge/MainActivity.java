package com.example.jumpjudge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import com.opencsv.CSVWriter;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Global Variables
    Button[] btn = new Button[17];
    EditText userInput;

    private Spinner divisionSpinner;
    private String division;
    private LinkedList<String> divisionIndex;
    private Division[] divisions;

    TextView refusalTextView;

    private Spinner fenceSpinner;
    private String fence;

    private Spinner otherSpinner;
    private String other;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private boolean permissionGranted;

    private boolean timerRunning = false;
    private boolean timerHasRun = false;
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
        refusalTextView = findViewById(R.id.refusalText);
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
                clearScreenData();
                break;
            case R.id.refusal:
                refusals = refusals + 1;
                refusalTextView.setText(String.format("%d", refusals));
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
                clearScreenData();
                break;
            case R.id.buttonBack:
                goBackAChar(userInput);
                break;
        }
    }

    private void changeBtnText(String text, Button btn) {
        btn.setText(text);
    }

    private void changeOtherSelectionNone () {
        otherSpinner.setSelection(0);
    }

    private void setupDivisionSpinner() {

        String[] divisions = getDivisions();

        ArrayAdapter divisionSpinnerAdapter = new ArrayAdapter(this, R.layout.division_spinner_item, divisions);

        divisionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        divisionSpinner.setAdapter(divisionSpinnerAdapter);

        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    division = selection;
                    setupFenceSpinner();
                } else {
                    division = "Division Unknown";
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                division = "Division Unknown";
            }
        });
    }

     private String[] getDivisions () {
        Context context = getApplicationContext();
        Utils utils = new Utils();
        utils.loadJSONSetupData(context);
        divisions = utils.getDivisions();
        divisionIndex = new LinkedList<>();
        String[] divisionNames = new String[divisions.length];

        for (int i = 0; i < divisions.length; i++) {
            divisionNames[i] = divisions[i].getName();
            divisionIndex.add(divisionNames[i]);
        }

        if (division == null)
            division = divisions[0].getName();

        return divisionNames;
    }

    private void setupFenceSpinner() {
        int index = divisionIndex.indexOf(division);
        List<String> fenceList = new ArrayList<String>();

        int numFences = divisions[index].getFences();

        for (int i = 1; i < numFences + 1; i++)
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
                    if (!other.equals("None"))
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

    public void clearScreenData () {
        refusals = 0;
        refusalTextView.setText(Integer.toString(refusals));
        changeOtherSelectionNone();
        changeBtnText("Jumped Clear", btn[12]);
        timer.setBase(SystemClock.elapsedRealtime());
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
        }
    }

    private void processNumber(String input, Calendar now, long jumpTime) {
        Context context = getApplicationContext();
        showTimeNumber(input, now, refusals);
        Rider rider = saveRiderData(input, jumpTime);
        insertRider(rider);
        //TODO: Encrypt data
        MqttHelper mqttHelper = new MqttHelper(context);
        String msg = createMessageString(rider);
        mqttHelper.connect(msg);
    }

    public void showTimeNumber(String number, Calendar now, int refusals) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss:SS", Locale.getDefault());
        Date jumpTime = now.getTime();
        CharSequence text = "Rider: " + number + " Refusals: " + refusals + " Time: " + format.format(jumpTime);
        showToast(text);
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
        userInput = dialogView.findViewById(R.id.add_number);
        builder.setView(dialogView);

        builder.setTitle("Please enter the Rider Number");
        builder.setMessage("Enter Number");
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //showSoftNumPad(dialogView);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rNumber = userInput.getText().toString();
                boolean closeDialog = false;
                if(!rNumber.isEmpty()) {
                    processNumber(userInput.getText().toString(), now, jumpTime);
                    clearNumber(userInput);
                    closeDialog = true;
                }
                if(closeDialog)
                    alertDialog.dismiss();
            }
        });
    }

    public void showSoftNumPad(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void holdClicked() {
        if (timerRunning) {
            timer.stop();
            holdTime = SystemClock.elapsedRealtime() - timer.getBase();
            changeBtnText("Hold", btn[14]);
            timerHasRun = true;
            timerRunning = false;
        } else {
            timer.setBase(SystemClock.elapsedRealtime());
            timer.start();
            changeBtnText("reStart", btn[14]);
            timerHasRun = false;
            timerRunning = true;
        }
    }

    public void clearHold() {
        if (timerRunning) {
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

    private void deleteAllRiders() {
        int rowsDeleted = getContentResolver().delete(RiderContract.RiderEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from rider database");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_all_enteries:
                Intent catalogIntent = new Intent(this, CatalogActivity.class);
                startActivity(catalogIntent);
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_download_to_computer:
                showExportConfirmationDialog();
                return true;
            case R.id.action_uninstall:
                showUnistallConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllRiders();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showExportConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.export_all_msg);
        builder.setPositiveButton(R.string.export, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exportData();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void exportData() {

        RiderDbHelper dbHelper = new RiderDbHelper(getApplicationContext());

        String state = Environment.getExternalStorageState();
        String external = Environment.getExternalStorageDirectory().toString();
        String fileName = fence + RiderDbHelper.DATABASE + ".csv";

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission()) {
                    fileAsCSV(external, "CrossCountryScoring", fileName, dbHelper);
                } else {
                    requestPermission();
                }
            } else {
                fileAsCSV(external, "CrossCountryScoring", fileName, dbHelper);
            }
        }

    }

    private void fileAsCSV(String rootPath, String newFolder, String fileName, RiderDbHelper dbHelper) {

        File path = checkForDir(rootPath, newFolder);

        File csvFile = new File(path, fileName);
        if (!csvFile.exists()) {
            createCSVFile(dbHelper, csvFile);
            showToast("CSV File for " + fence + " Exported");
        } else {
            if(csvFile.lastModified() < Calendar.DATE) {
                csvFile.delete();
                createCSVFile(dbHelper, csvFile);
                showToast("CSV File for " + fence + " Exported");
            } else {
                showFileDeleteErrorDialog();
            }
        }
    }

    private void showToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private File checkForDir(String rootPath, String addPath) {
        File newPath = new File(rootPath, addPath);
        if (!newPath.exists()) {
            newPath.mkdirs();
        }
        return newPath;
    }

    private void createCSVFile (RiderDbHelper dbHelper, File file) {
        Log.d("MainActivity.file", file.toString());
        try {
            FileOutputStream output = new FileOutputStream(file);
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            writeCSVFile(dbHelper, file, csvWrite);
            output.flush();
            output.close();
            csvWrite.close();
        } catch (Exception ex) {
            Log.e("MainActivity.file", ex.getMessage(), ex);
        }
    }

    private void writeCSVFile(RiderDbHelper dbHelper, File file, CSVWriter csvWrite) {

        try {
            file.createNewFile();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "
                    + RiderContract.RiderEntry.TABLE_NAME, null);
            csvWrite.writeNext(cursor.getColumnNames());
            while (cursor.moveToNext()) {
                String[] columnArray = {cursor.getString(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6)};
                csvWrite.writeNext(columnArray);
            }
            cursor.close();
        } catch (Exception ex) {
            Log.e("MainActivity.csv", ex.getMessage(), ex);
        }
    }

    private void showUnistallConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.uninstall_msg);
        builder.setPositiveButton(R.string.uninstall, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                uninstallApp();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void uninstallApp() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:com.example.jumpjudge"));
        startActivity(intent);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                }
                break;
        }
    }

    public void showFileDeleteErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.cannot_delete_file_msg);
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (userInput != null) {
            outState.putString("number", userInput.getText().toString());
            outState.putString("other", other);
        }
        outState.putString("division", division);
        outState.putString("holdBtn", btn[14].getText().toString());
        outState.putString("fence", fence);
        outState.putBoolean("timerRunning", timerRunning);
        outState.putBoolean("timerHasRun", timerHasRun);
        outState.putLong("hold", timer.getBase());
        outState.putLong("holdTime", holdTime);
        Chronometer screenTimer = findViewById(R.id.timer);
        outState.putCharSequence("screenTime", screenTimer.getText());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        holdTime = savedInstanceState.getLong("holdTime");
        timerRunning = savedInstanceState.getBoolean("timerRunning");
        timerHasRun = savedInstanceState.getBoolean("timerHasRun");
        long time = savedInstanceState.getLong("hold");
        long sysTime = SystemClock.elapsedRealtime();
        CharSequence screenTime = savedInstanceState.getCharSequence("screenTime");
        changeBtnText(savedInstanceState.getString("holdBtn"), btn[14]);
        if (timerRunning) {
            timer.setBase(time);
            timer.start();
        } else if (timerHasRun) {
            Chronometer screenTimer = findViewById(R.id.timer);
            screenTimer.setText(screenTime);
        } else {
            timer.setBase(sysTime);
        }
    }
}
