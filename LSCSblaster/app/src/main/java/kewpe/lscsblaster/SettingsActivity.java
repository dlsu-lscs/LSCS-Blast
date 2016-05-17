package kewpe.lscsblaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {
    Button btn_exit, btn_passcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btn_exit = (Button) findViewById(R.id.btn_exit);
        btn_passcode = (Button) findViewById(R.id.btn_passcode);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent();
                mainIntent.setClass(getBaseContext(), MainActivity.class);
                mainIntent.putExtra("needPasscode", false);
                finish();
                startActivity(mainIntent);
            }
        });

        btn_passcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent();
                loginIntent.setClass(getBaseContext(), LoginActivity.class);
                loginIntent.putExtra("activity", "setup");
                loginIntent.putExtra("needPasscode", false);
                startActivity(loginIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String pass = sp.getString(MainActivity.SP_KEY_PASSCODE, null);

        Intent i = getIntent();
        boolean needPasscode = i.getBooleanExtra("needPasscode", true);

        if(needPasscode) {
            if (pass == null) {
                Intent setupIntent = new Intent();
                setupIntent.setClass(getBaseContext(), SetupActivity.class);
                startActivity(setupIntent);
            } else if (pass != null) {
                Intent loginIntent = new Intent();
                loginIntent.setClass(getBaseContext(), LoginActivity.class);
                loginIntent.putExtra("activity", "settings");
                startActivity(loginIntent);
            }
        }else{
            i.putExtra("needPasscode",true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
}
