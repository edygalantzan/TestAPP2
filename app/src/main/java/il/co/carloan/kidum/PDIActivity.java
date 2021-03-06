package il.co.carloan.kidum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class PDIActivity extends AppCompatActivity implements DatePickerFragment.DatePickerFragmentController{

    EditText mtz1;
    CheckBox agree;

    PhotoSendTask sendTask;
    private View mProgressView;
    private View mLoginFormView;
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    public void internet(){
        Intent intent = new Intent(this,MassageActivity.class);
        intent.putExtra("text1","בעית חיבור");
        intent.putExtra("text2","בדוק את חיבור האינטרנט שלך");
        intent.putExtra("icon",R.drawable.icon_network);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!isNetworkConnected()){
            internet();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdi);
        if (!isNetworkConnected()){
            internet();
        }
        mtz1 = (EditText) findViewById(R.id.tz1);
        agree = (CheckBox) findViewById(R.id.agree);
        mProgressView = findViewById(R.id.photoSend);
        mLoginFormView = findViewById(R.id.login_form);
        agree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    agree.setError(null);
                    agree.clearFocus();
                }
            }
        });
    }

    public void showDatePickerDialog(View v) {
        agree.setError(null);
        mtz1.setError(null);
        if(mtz1.getText().toString().matches("")){
            mtz1.setError("שדה זה חובה");
            mtz1.requestFocus();
        }else if(agree.isChecked()){
            //DialogFragment newFragment = new DatePickerFragment();
            //newFragment.show(getFragmentManager(), "datePicker");
            timeSetNoCalendar();
        }else{
            agree.setError(getString(R.string.checkbox_error));
            agree.requestFocus();
        }
    }
    @Override
    public void timeSet(DialogFragment dialog,int year , int month , int day) {
        showProgress(true);
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String mEmail = settings.getString("Username", "");
        String mPassword = settings.getString("Password", "");
        sendTask = new PhotoSendTask(mtz1.getText().toString(),mEmail,mPassword,this);
        sendTask.execute();
    }
    public void timeSetNoCalendar(){
        showProgress(true);
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String mEmail = settings.getString("Username", "");
        String mPassword = settings.getString("Password", "");
        sendTask = new PhotoSendTask(mtz1.getText().toString(),mEmail,mPassword,this);
        sendTask.execute();
    }
    @Override
    public void onBackPressed() {
        sendTask=null;
        finish();
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
    public class PhotoSendTask extends AsyncTask<Void, Void, String> {


        String mEmail,mPassword,id;
        AppCompatActivity act;

        PhotoSendTask(String tz11, String email, String pass,AppCompatActivity activity) {

            mEmail=email;
            mPassword=pass;
            act=activity;
            id=tz11;
        }

        @Override
        protected String doInBackground(Void... params) {
            //Log.d("BDI","test");
            long startTime = System.currentTimeMillis();
            long elapsedTime;
            do{
                try {
                    String httpsURL = "https://app.carloan.co.il/dynamic/android/test_bdi/?"+"username="+ this.mEmail + "&password=" + this.mPassword+"&id="+id;
                    URL url = new URL(httpsURL);
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    InputStream ins = con.getInputStream();
                    InputStreamReader isr = new InputStreamReader(ins);
                    BufferedReader in = new BufferedReader(isr);
                    String inputLine;
                    List<String> list=new ArrayList<>();
                    while ((inputLine = in.readLine()) != null){
                        list.add(inputLine);
                    }
                    in.close();
                    String code=findcode(list);
                    if(!code.equals("try_again")){
                        return code;
                    }
                }catch (javax.net.ssl.SSLHandshakeException e) {
                    try {
                        String httpsURL = "http://app.carloan.co.il/dynamic/android/test_bdi/?username="+ this.mEmail + "&password=" + this.mPassword+"&id="+id;
                        URL url = new URL(httpsURL);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        InputStream ins = con.getInputStream();
                        InputStreamReader isr = new InputStreamReader(ins);
                        BufferedReader in = new BufferedReader(isr);
                        String inputLine;

                        List<String> list=new ArrayList<>();
                        while ((inputLine = in.readLine()) != null){
                            list.add(inputLine);
                        }
                        in.close();
                        String code=findcode(list);
                        boolean i="try_again".equals(code);
                        if(!i){
                            return code;
                        }
                    } catch (Exception e2){
                        //Log.e("EXCEPTION", e2.getMessage());
                        return "EXCEPTION";
                    }
                } catch (Exception e){
                    //Log.e("EXCEPTION", e.getMessage());
                    return "EXCEPTION";
                }
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    //Log.e("EXCEPTION", e.getMessage());
                    return null;
                }
                elapsedTime = (new Date()).getTime() - startTime;
            }while (elapsedTime<1*60*1000);
            return "try_again1";
        }

        @Override
        protected void onPostExecute(final String response) {
            sendTask = null;
            //Log.d("BDI", response);
            switch (response) {
                case "manual": {
                    showProgress(false);
                    Intent intent = new Intent(act, MassageActivity.class);
                    intent.putExtra("text1", getString(R.string.maybe_bdi));
                    intent.putExtra("text2", "דרושה בדיקה ידנית");
                    intent.putExtra("icon", R.drawable.icon_mark);
                    startActivity(intent);
                    break;
                }
                case "error_x": {
                    Intent intent = new Intent(act, MainActivity.class);
                    intent.putExtra("Toast", getString(R.string.err));
                    startActivity(intent);
                    break;
                }
                case "error_y": {
                    Intent intent = new Intent(act, MainActivity.class);
                    intent.putExtra("Toast", getString(R.string.err));
                    startActivity(intent);
                    break;
                }
                case "id _error":
                    showProgress(false);
                    mtz1.setError(getString(R.string.id));
                    break;
                case "id_not_valid":
                    showProgress(false);
                    mtz1.setError(getString(R.string.id));
                    break;
                case "black_list": {
                    showProgress(false);
                    Intent intent = new Intent(act, MassageActivity.class);
                    intent.putExtra("text1", "לקוח זה אינו יכול לקבל הלוואה");
                    intent.putExtra("text2", getString(R.string.black));
                    intent.putExtra("icon", R.drawable.icon_bad);
                    startActivity(intent);
                    break;
                }
                case "": {
                    showProgress(false);
                    Intent intent = new Intent(act, LoginActivity.class);
                    intent.putExtra("Toast", getString(R.string.sign));
                    startActivity(intent);
                    break;
                }
                case "test_ok": {
                    showProgress(false);
                    Intent intent = new Intent(act, MassageActivity.class);
                    intent.putExtra("text1", getString(R.string.successful_bdi));
                    intent.putExtra("text2", "");
                    intent.putExtra("icon", R.drawable.icon_good);
                    startActivity(intent);
                    break;
                }
                default:{
                    showProgress(false);
                    Intent intent = new Intent(act, MassageActivity.class);
                    intent.putExtra("text1", "בעית חיבור");
                    intent.putExtra("text2", "בדוק את חיבור האינטרנט שלך");
                    intent.putExtra("icon", R.drawable.icon_network);
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
    public String findcode(List list){
        if(list.contains("try_again")){
            return "try_again";
        }else if(list.contains("manual")){
            return "manual";
        }else if(list.contains("test_ok")){
            return "test_ok";
        }else if(list.contains("blacklist")){
            return "blacklist";
        }else if(list.contains("id_not_valid")){
            return "id_not_valid";
        }else if(list.contains("id_error")){
            return "id_error";
        }else if(list.contains("error_x")){
            return "error_x";
        }else if(list.contains("error_y")){
            return "error_y";
        }
        return "";
    }
}