package il.co.carloan.kidum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PDIActivity extends AppCompatActivity implements DatePickerFragment.DatePickerFragmentController{

    AutoCompleteTextView tz1;
    AutoCompleteTextView tz2;

    PhotoSendTask sendTask;
    private View mProgressView;
    private View mLoginFormView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdi);
        tz1 = (AutoCompleteTextView) findViewById(R.id.tz1);
        tz2 = (AutoCompleteTextView) findViewById(R.id.tz2);
        mProgressView = findViewById(R.id.photoSend);
        mLoginFormView = findViewById(R.id.login_form);
    }
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }
    @Override
    public void timeSet(DialogFragment dialog,int year , int month , int day) {
        showProgress(true);
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String mEmail = settings.getString("Username", "");
        String mPassword = settings.getString("Password", "");
        sendTask = new PhotoSendTask(tz1.getText().toString(),tz2.getText().toString(),year,month,day,mEmail,mPassword,this);
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class PhotoSendTask extends AsyncTask<Void, Void, Boolean> {


        String mEmail,mPassword,tz1,tz2;
        int year,month,day;
        AppCompatActivity act;

        PhotoSendTask(String tz11, String tz21,int year1,int month1,int day1, String email, String pass,AppCompatActivity activity) {

            mEmail=email;
            mPassword=pass;
            act=activity;
            tz1=tz11;
            tz2=tz21;
            year=year1;
            month=month1;
            day=day1;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                //TODO: Change URL
                String httpsURL = "http://app.carloan.co.il/login/user/check/?username=" + this.mEmail + "&password=" + this.mPassword;
                URL url = new URL(httpsURL);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                //add reuqest header
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                String urlParameters = "username="+ this.mEmail + "&password=" + this.mPassword+"id1="+tz1+"id2="+tz2+"year="+year+"month="+month+"day="+day;
                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
                return true;
            }catch (javax.net.ssl.SSLHandshakeException e) {
                try {
                    //TODO: Change URL
                    String httpsURL = "http://app.carloan.co.il/login/user/check/?username=" + this.mEmail + "&password=" + this.mPassword;
                    URL url = new URL(httpsURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    //add reuqest header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                    String urlParameters = "username="+ this.mEmail + "&password=" + this.mPassword+"id1="+tz1+"id2="+tz2+"year="+year+"month="+month+"day="+day;
                    // Send post request
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();
                    return true;
                } catch (Exception e2){
                    Log.e("EXCEPTION", e2.getMessage());
                }
            } catch (Exception e){
                Log.e("EXCEPTION", e.getClass().getName());
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            sendTask = null;

            if (success) {
                finish();
            }else{
                Toast.makeText(act, "Something went wrong while sending the image. Please check your internet connection!",
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            sendTask = null;
            showProgress(false);
        }
    }
}