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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameaActivity extends AppCompatActivity implements CameraPermissionDialog.CameraPermissionDialogListener {

    private PhotoSendTask sendTask;
    private static final String TAG = "Camera";
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
     * @param activity
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
            File imageFile = new File(photoUri.getPath());
            if (imageFile.exists()) {
                showProgress(true);
                SharedPreferences settings = getSharedPreferences("UserInfo", 0);
                String mEmail = settings.getString("Username", "");
                String mPassword = settings.getString("Password", "");
                sendTask = new PhotoSendTask("http://app.carloan.co.il/dynamic/android/upload_img?username="+mEmail+"&password="+mPassword, "test", "descrption", new FileInputStream(imageFile), mEmail, mPassword);
                sendTask.execute();
            }
        } catch (Exception e) {
            Log.e("CameraWEB", e.getMessage());
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class PhotoSendTask extends AsyncTask<Void, Void, Boolean> {

        URL connectURL;
        String Title;
        String username;
        String password;
        String Description;
        byte[] dataToServer;
        FileInputStream fileInputStream = null;

        PhotoSendTask(String urlString, String vTitle, String vDesc, FileInputStream fStream, String username1, String password1) {
            username = username1;
            password = password1;
            fileInputStream = fStream;
            try {
                connectURL = new URL(urlString);
                Title = vTitle;
                Description = vDesc;
            } catch (Exception ex) {
                Log.i("HttpFileUpload", "URL Malformatted");
            }
        }

        @Override
        protected Boolean doInBackground(Void... param) {
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            String Tag = "CameraNET";
            try {
                Log.d(Tag, "Starting Http File Sending to URL");
                // Open a HTTP connection to the URL
                HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();

                // Allow Inputs
                conn.setDoInput(true);

                // Allow Outputs
                conn.setDoOutput(true);

                // Don't use a cached copy.
                conn.setUseCaches(false);

                // Use a post method.
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection", "Keep-Alive");

                conn.setRequestProperty("Content-Type", "multipart/form-data;");

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(Title);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"description\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(Description);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + username.replace("@","_")+"_"+System.currentTimeMillis() / 1000 + ".JPEG\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.d(Tag, "Headers are written");

                // create a buffer of maximum size
                int bytesAvailable = fileInputStream.available();

                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close streams
                fileInputStream.close();

                dos.flush();

                Log.d(Tag, "File Sent, Response: " + String.valueOf(conn.getResponseCode()));

                // retrieve the response from server
                InputStream ins = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(ins);
                BufferedReader in = new BufferedReader(isr);
                String s;
                while ((s=in.readLine())!=null) {
                    Log.d(TAG, s);
                }
                dos.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
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

    @Override
    public void onBackPressed() {
        sendTask=null;
    }
}