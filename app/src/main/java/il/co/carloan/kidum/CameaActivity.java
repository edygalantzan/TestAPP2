package il.co.carloan.kidum;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.*;
import org.apache.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class CameaActivity extends AppCompatActivity implements CameraPermissionDialog.CameraPermissionDialogListener {

    private PhotoSendTask sendTask;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
    private static final int MY_PERMISSIONS_CAMERA = 0;
    private Uri fileUri = null;

    private View mProgressView;

    private void dispatchTakePictureIntent() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = Uri.fromFile(getOutputPhotoFile());
        i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ);
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camea);
        verifyStoragePermissions(this);
        cameraPermission();
        mProgressView = findViewById(R.id.photoSend);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == RESULT_OK) {
                Uri photoUri;
                if (data == null) {
                    photoUri = fileUri;
                } else {
                    photoUri = data.getData();
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
        try {
            showProgress(true);
            SharedPreferences settings = getSharedPreferences("UserInfo", 0);
            String mEmail = settings.getString("Username", "");
            String mPassword = settings.getString("Password", "");
            sendTask = new PhotoSendTask("http://app.carloan.co.il:80/dynamic/android/upload_img",photoUri.getPath(),mEmail,mPassword,this);
            sendTask.execute();
        } catch (Exception e) {
            //Log.e("CameraWEB", e.getMessage());
        }
    }


    private void cameraPermission() {
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
        } else {
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

    private void showDialog() {
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

    public void finish() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Toast", getString(R.string.camera_sent));
        startActivity(intent);
    }

    public void cansle() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Toast", getString(R.string.camera_cansle));
        startActivity(intent);
    }
    public void error(String s){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Toast", s);
        startActivity(intent);
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class PhotoSendTask extends AsyncTask<Void, Void, String> {
        String imgPath;
        Bitmap bitmap;
        String encodedString;
        String url;
        String fileName;
        String username;
        String password;
        AppCompatActivity act;
        PhotoSendTask(String urlString,String img,String username1,String password1,AppCompatActivity activity) {
            imgPath=img;
            url=urlString;
            username=username1;
            password=password1;
            act=activity;
        }

        @Override
        protected String doInBackground(Void... param) {
            // Get the Image's file name
            String fileNameSegments[] = imgPath.split("/");
            fileName = fileNameSegments[fileNameSegments.length - 1];

            BitmapFactory.Options options =new BitmapFactory.Options();
            options.inSampleSize = 3;
            bitmap = BitmapFactory.decodeFile(imgPath,
                    options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Must compress the Image to reduce image size to make upload easy
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byte_arr = stream.toByteArray();
            // Encode Image to String
            encodedString = Base64.encodeToString(byte_arr, 0);

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("username", username));
                nameValuePairs.add(new BasicNameValuePair("password", password));
                nameValuePairs.add(new BasicNameValuePair("data", encodedString));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                try {
                    HttpResponse response= httpclient.execute(httppost);
                    String body = handler.handleResponse(response);
                    int code = response.getStatusLine().getStatusCode();
                    //Log.d("Http Post Response", body);
                    return body;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                // Execute HTTP Post Request
                // ResponseHandler<String> responseHandler=new BasicResponseHandler();
                // String responseBody = httpclient.execute(httppost, responseHandler);

                // if (Boolean.parseBoolean(responseBody)) {
                //	dialog.cancel();
                // }


            } catch (IOException e) {
                //Log.i("HTTP Failed", e.toString());
            }

            return "error";
        }

        @Override
        protected void onPostExecute(final String success) {
            switch (success){
                case "OK": {
                    showProgress(false);
                    Intent intent = new Intent(act, MassageActivity.class);
                    intent.putExtra("text1", getString(R.string.camera_sent));
                    intent.putExtra("text2", "");
                    intent.putExtra("icon", R.drawable.icon_good);
                    startActivity(intent);
                    break;
                }
                default:{
                    showProgress(false);
                    Intent intent = new Intent(act, MassageActivity.class);
                    intent.putExtra("text1", getString(R.string.camera_error));
                    intent.putExtra("text2", "");
                    intent.putExtra("icon", R.drawable.icon_bad);
                    startActivity(intent);
                    break;
                }
            }
        }

        @Override
        protected void onCancelled() {
            sendTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onBackPressed() {
        sendTask=null;
        showProgress(false);
        finish();
    }
}