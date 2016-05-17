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
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    EditText et_passcode;
    Button btn_enter;
    String activityName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_passcode = (EditText) findViewById(R.id.et_passcode);
        btn_enter = (Button) findViewById(R.id.btn_enter);
        Intent i = getIntent();
        activityName = i.getStringExtra("activity");

        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String pass = sp.getString(MainActivity.SP_KEY_PASSCODE, null);
                String input = et_passcode.getText().toString();

                if(!input.isEmpty()){

                    if(input.equals(pass)){
                        if(activityName!=null){
                            Intent i = new Intent();
                            switch(activityName){
                                case "main": i.setClass(getBaseContext(), MainActivity.class);
                                             break;
                                case "setup": i.setClass(getBaseContext(), SetupActivity.class);
                                              i.putExtra("ChangePass", true);
                                              break;
                                case "settings": i.setClass(getBaseContext(), SettingsActivity.class);
                                                 break;
                            }
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("needPasscode", false);
                            startActivity(i);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
