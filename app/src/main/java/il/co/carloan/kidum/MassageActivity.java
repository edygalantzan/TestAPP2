package il.co.carloan.kidum;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MassageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_massage);
        Intent intent = getIntent();
        String text1 = intent.getStringExtra("text1");
        String text2 = intent.getStringExtra("text2");
        TextView mtext1 = (TextView) findViewById(R.id.textView);
        TextView mtext2 = (TextView) findViewById(R.id.textView2);
        mtext1.setText(text1);
        mtext2.setText(text2);
        ImageView image = (ImageView) findViewById(R.id.imageIcon);
        image.setImageDrawable(get(intent.getIntExtra("icon", R.drawable.icon_bad)));
    }
    public void finish(View v){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
    private Drawable get(int i){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(i, getTheme());
        } else {
            return getResources().getDrawable(i);
        }
    }
}
