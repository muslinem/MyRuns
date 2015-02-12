package com.example.ericmusliner.myruns;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
    public static final int REQUEST_CODE_CROP_PHOTO = 2;
    public static final String PREFS_NAME = "PrefsFile";

    private static final String IMAGE_UNSPECIFIED = "image/*";
    private Uri mImageCaptureUri;
    private boolean isTakenFromCamera;
    private ImageView mImageView;
    private EditText nameEntry;
    private EditText emailEntry;
    private EditText phoneEntry;
    private RadioGroup genderEntry;
    private RadioButton maleRadio;
    private RadioButton femaleRadio;
    private EditText classYearEntry;
    private EditText majorEntry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);

        //Instantiating elements in UI
        mImageView = (ImageView) findViewById(R.id.photoEntry);
        nameEntry = (EditText) findViewById(R.id.nameEntry);
        emailEntry = (EditText) findViewById(R.id.emailEntry);
        phoneEntry = (EditText) findViewById(R.id.phoneEntry);
        genderEntry = (RadioGroup)findViewById(R.id.genderGroup);
        maleRadio = (RadioButton) genderEntry.getChildAt(1);
        femaleRadio = (RadioButton) genderEntry.getChildAt(0);
        classYearEntry = (EditText) findViewById(R.id.classYearEntry);
        majorEntry = (EditText) findViewById(R.id.majorEntry);

        //Loading saved entries from Shared Preference File
        nameEntry.setText(pref.getString("Name", null));
        emailEntry.setText(pref.getString("Email", null));
        phoneEntry.setText(pref.getString("Phone", null));
        maleRadio.setChecked(pref.getBoolean("MaleRadio", false));
        femaleRadio.setChecked(pref.getBoolean("FemaleRadio", false));
        classYearEntry.setText(pref.getString("ClassYear", null));
        majorEntry.setText(pref.getString("Major", null));

        // Load profile photo from internal storage
        try
        {
            FileInputStream fis = openFileInput(getString(R.string.profile_photo_name));
            Bitmap bmap = BitmapFactory.decodeStream(fis);
            mImageView.setImageBitmap(bmap);
            fis.close();
        } catch (IOException e) {
            // Default profile photo if no photo saved before.
            mImageView.setImageResource(R.drawable.default_profile);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("PrefsFile", 0);

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

    public void onSaveClicked(View v) {
        // Commit all the changes into preference file
        // Save profile image into internal storage.

        //Saving fields to Preferences file
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Name", nameEntry.getText().toString());
        editor.putString("Email", emailEntry.getText().toString());
        editor.putString("Phone", phoneEntry.getText().toString());
        editor.putBoolean("FemaleRadio", femaleRadio.isChecked());
        editor.putBoolean("MaleRadio", maleRadio.isChecked());
        editor.putString("ClassYear", classYearEntry.getText().toString());
        editor.putString("Major", majorEntry.getText().toString());
        editor.commit();

        //Saving Profile picture to file
        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(
                    getString(R.string.profile_photo_name), MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        Toast.makeText(getApplicationContext(),
                    "Saved!", Toast.LENGTH_SHORT).show();

        super.onBackPressed();
    }

    public void onCancelClicked(View v) {
        Toast.makeText(getApplicationContext(),
                "Cancelled!", Toast.LENGTH_SHORT).show();

        super.onBackPressed();
    }

    public void onChangeClicked(View v) {
        Intent intent;

        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Construct temporary image path and name to save the taken
        // photo
        mImageCaptureUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), "tmp_"
                + String.valueOf(System.currentTimeMillis()) + ".jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                mImageCaptureUri);
        intent.putExtra("return-data", true);
        try {
            // Start a camera capturing activity
            // REQUEST_CODE_TAKE_FROM_CAMERA is an integer tag you
            // defined to identify the activity in onActivityResult()
            // when it returns
            startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        isTakenFromCamera = true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CODE_TAKE_FROM_CAMERA:
                // Send image taken from camera for cropping
                cropImage();
                break;

            case REQUEST_CODE_CROP_PHOTO:
                // Update image view after image crop
                Bundle extras = data.getExtras();
                // Set the picture image in UI
                if (extras != null) {
                    mImageView.setImageBitmap((Bitmap) extras.getParcelable("data"));
                }

                // Delete temporary image taken by camera after crop.
                if (isTakenFromCamera) {
                    File f = new File(mImageCaptureUri.getPath());
                    if (f.exists())
                        f.delete();
                }

                break;
        }
    }

    // Crop and resize the image for profile
    private void cropImage() {
        // Use existing crop activity.
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

        // Specify image size
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);

        // Specify aspect ratio, 1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        // REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
        // identify the activity in onActivityResult() when it returns
        startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
    }



}
