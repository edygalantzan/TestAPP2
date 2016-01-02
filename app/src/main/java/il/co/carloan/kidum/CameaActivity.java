package il.co.carloan.kidum;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class CameaActivity extends AppCompatActivity implements CameraPermissionDialog.CameraPermissionDialogListener{

    private PhotoSendTask sendTask;
    private static final String TAG = "Camera";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
    private static final int MY_PERMISSIONS_CAMERA=0;
    private Uri fileUri = null;

    private View mProgressView;

    private String mEmail;
    private String mPassword;
    private void dispatchTakePictureIntent() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = Uri.fromFile(getOutputPhotoFile());
        i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ );
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camea);
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        mEmail = settings.getString("Username", "");
        mPassword = settings.getString("Password", "");
        cameraPermission();
        mProgressView = findViewById(R.id.photoSend);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == RESULT_OK) {
                Uri photoUri;
                if (data == null) {
                    // A known bug here! The image should have saved in fileUri
                    Toast.makeText(this, "Image saved successfully",
                            Toast.LENGTH_LONG).show();
                    photoUri = fileUri;
                } else {
                    photoUri = data.getData();
                    Toast.makeText(this, "Image saved successfully in: " + data.getData(),
                            Toast.LENGTH_LONG).show();
                }
                sendPhoto(photoUri);
            } else if (resultCode == RESULT_CANCELED) {
                cansle();
            } else {
                cansle();
            }
        }
    }
    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                //Log.e(TAG, "Failed to create storage directory.");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
    }
    private void sendPhoto(Uri photoUri) {
        File imageFile = new File(photoUri.getPath());
        if (imageFile.exists()){
            showProgress(true);
            sendTask = new PhotoSendTask(Uri.parse(imageFile.getAbsolutePath()),this);
            sendTask.execute();
        }
    }


    private void cameraPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                        showDialog();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else{
            dispatchTakePictureIntent();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchTakePictureIntent();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showDialog();
                }
            }
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_CAMERA);
    }
    private void showDialog(){
        DialogFragment dialog = new CameraPermissionDialog();
        dialog.show(getFragmentManager(), "Attention!");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void finish(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Toast",getString(R.string.camera_sent));
        startActivity(intent);
    }
    public void cansle(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Toast",getString(R.string.camera_cansle));
        startActivity(intent);
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class PhotoSendTask extends AsyncTask<Void, Void, Boolean> {

        private final Uri uri;
        AppCompatActivity act;

        PhotoSendTask(Uri image, AppCompatActivity activity) {
            uri = image;
            act=activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            /**
             * Show share dialog BOTH image and text
             */
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            //Target whatsapp:
            shareIntent.setPackage("com.whatsapp");
            //Add text and then Image URI
            shareIntent.putExtra(Intent.EXTRA_TEXT, "I am using the new Kidum app!");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/jpeg");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(shareIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            finish();
        }

        @Override
        protected void onCancelled() {
            sendTask = null;
            showProgress(false);
        }
    }
}