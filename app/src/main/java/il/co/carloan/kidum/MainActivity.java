package il.co.carloan.kidum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_main);
        if (!isNetworkConnected()){
            internet();
        }
        ImageView mBackground = (ImageView) findViewById(R.id.imageViewMain);
        Picasso.with(this).load("http://app.carloan.co.il/android/main.jpg").into(mBackground);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Toast.makeText(this, extras.getString("Toast"), Toast.LENGTH_LONG).show();
        }
    }
    public void cam(View v) {
        Intent intent = new Intent(this, CameaActivity.class);
        startActivity(intent);
    }
    public void pdi(View v) {
        Intent intent = new Intent(this, PDIActivity.class);
        startActivity(intent);
    }
    public void signOut(View v) {
        SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mySPrefs.edit();
        editor.remove("Username");
        editor.remove("Password");
        editor.apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("Auto",false);
        startActivity(intent);
    }
}
