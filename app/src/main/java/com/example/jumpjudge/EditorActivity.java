package com.example.jumpjudge;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.Arrays;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_RIDER_LOADER = 0;
    private Uri mCurrentRiderUri;
    private EditText mNumberEditText;
    private String mNumber;
    private String mOldNumber;
    private String mDivisionString;
    RadioButton[] mDivision;
    String[] mDivisionArray;
    RadioGroup mDivisionGroup;
    private int mFenceNum;
    private int mRefusals;
    private long mJumpTime;
    private String mOther;
    private long mHoldTime;
    //private long mFinishTime;
    private boolean mRiderHasChanged = false;
    Context mContext;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mRiderHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_item);

        mContext = getApplicationContext();

        Intent intent = getIntent();
        mCurrentRiderUri = intent.getData();

        setTitle(getString(R.string.editor_activity_title_edit_rider));
        getSupportLoaderManager().initLoader(EXISTING_RIDER_LOADER, null, this);

        final LinearLayout layout = findViewById(R.id.item_layout);
        mNumberEditText = findViewById(R.id.edit_rider_number);

        mNumberEditText.setOnTouchListener(mTouchListener);

        createRadioGroup(mContext, layout);

    }

    private void createRadioGroup(Context context, LinearLayout layout) {
        mDivisionGroup = new RadioGroup(context);
        mDivisionGroup.setOrientation(RadioGroup.VERTICAL);

        //TODO: Get array from json file.
        Utils utils = new Utils();
        mDivisionArray = utils.getDivisionNames(mContext, null);
        //mDivisionArray = getResources().getStringArray(R.array.array_division_options);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        mDivisionGroup.setLayoutParams(layoutParams);
        mDivision = new RadioButton[mDivisionArray.length];

        for(int i = 0; i < mDivisionArray.length; i++){
            mDivision[i] = new RadioButton(context);
            mDivision[i].setText(mDivisionArray[i]);
            mDivision[i].setTextColor(Color.BLACK);
            mDivisionGroup.addView(mDivision[i]);
        }

        layout.addView(mDivisionGroup);
    }

    private void saveRider() {
        mNumber = mNumberEditText.getText().toString().trim();
        int index = mDivisionGroup.getCheckedRadioButtonId();
        mDivisionString = mDivisionArray[index];

        if (mCurrentRiderUri == null &&
                TextUtils.isEmpty(mNumber)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_NUM, mNumber);
        values.put(RiderContract.RiderEntry.COLUMN_DIVISION, mDivisionString);
        values.put(RiderContract.RiderEntry.COLUMN_EDIT, mOldNumber);

        if (mCurrentRiderUri == null) {
            Uri newUri = getContentResolver().insert(RiderContract.RiderEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_rider_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_rider_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentRiderUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_rider_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_rider_success), Toast.LENGTH_SHORT).show();
            }
        }
        MqttHelper mqttHelper = new MqttHelper(mContext);
        Rider rider = new Rider(Integer.parseInt(mNumber), mDivisionString, mFenceNum, mRefusals, mJumpTime, mOther, mHoldTime, mOldNumber);
        String msg = createMessageString(rider);
        mqttHelper.connect(msg);
    }

    private String createMessageString(Rider rider) {

        return rider.getRiderNumber() + "," + rider.getDivision() + "," + rider.getFenceNumber()
                + "," + rider.getRefusals() + "," + rider.getJumpTime() + "," + rider.getOther()
                + "," + rider.getHoldTime() + "," + rider.getEdit();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentRiderUri == null) {
            MenuItem menuitem = menu.findItem(R.id.action_delete);
            menuitem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveRider();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if(!mRiderHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mRiderHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                RiderContract.RiderEntry._ID,
                RiderContract.RiderEntry.COLUMN_RIDER_NUM,
                RiderContract.RiderEntry.COLUMN_DIVISION,
                RiderContract.RiderEntry.COLUMN_FENCE_NUM,
                RiderContract.RiderEntry.COLUMN_RIDER_REFUSALS,
                RiderContract.RiderEntry.COLUMN_RIDER_TIME,
                RiderContract.RiderEntry.COLUMN_RIDER_OTHER,
                RiderContract.RiderEntry.COLUMN_RIDER_HOLD,
                RiderContract.RiderEntry.COLUMN_EDIT };

        return new CursorLoader(this,
                mCurrentRiderUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int riderColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_NUM);
            int divisionColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_DIVISION);
            int fenceColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_FENCE_NUM);
            int refusalColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_REFUSALS);
            int timeColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_TIME);
            int otherColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_OTHER);
            int holdColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_HOLD);
            int editColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_EDIT);

            mNumber = cursor.getString(riderColumnIndex);
            String division = cursor.getString(divisionColumnIndex);
            mFenceNum = cursor.getInt(fenceColumnIndex);
            mRefusals = cursor.getInt(refusalColumnIndex);
            mJumpTime = cursor.getLong(timeColumnIndex);
            mOther = cursor.getString(otherColumnIndex);
            mHoldTime = cursor.getLong(holdColumnIndex);
            String oldNumber = cursor.getString(editColumnIndex);

            //Check the division that is currently associated with the rider.
            mNumberEditText.setText(mNumber);
            int index = Arrays.asList(mDivisionArray).indexOf(division);
            mDivision[index].setChecked(true);

            //This sets up the old number in case we change the number the server can find the edit
            //and make the appropriate change.
            if(oldNumber != null)
                mOldNumber = oldNumber;
            else
                mOldNumber = mNumber;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNumberEditText.setText("");
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                if(dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteRider();
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

    private void deleteRider() {
        if (mCurrentRiderUri != null) {

            Context context = getApplicationContext();
            MqttHelper mqttHelper = new MqttHelper(context);
            Rider rider = new Rider(Integer.parseInt(mNumber), mDivisionString, mFenceNum, mRefusals, mJumpTime, mOther, mHoldTime, "D");
            String msg = createMessageString(rider);
            mqttHelper.connect(msg);

            int rowsDeleted = getContentResolver().delete(mCurrentRiderUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_rider_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_rider_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
