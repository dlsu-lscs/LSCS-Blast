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
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {
    EditText et_passcode1, et_passcode2;
    Button btn_save;
    boolean changePass = false;
    boolean needPasscode = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        et_passcode1 = (EditText) findViewById(R.id.et_passcode1);
        et_passcode2 = (EditText) findViewById(R.id.et_passcode2);
        btn_save = (Button) findViewById(R.id.btn_save);

        Intent i = getIntent();
        changePass = i.getBooleanExtra("ChangePass", false);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passcode1 = et_passcode1.getText().toString();
                String passcode2 = et_passcode2.getText().toString();
                if(!passcode1.isEmpty() && !passcode2.isEmpty()){
                    if(passcode1.equals(passcode2)){
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor spEditor = sp.edit();
                        spEditor.putString(MainActivity.SP_KEY_PASSCODE, passcode1);
                        spEditor.commit();
                        Intent intent = new Intent();
                        if(changePass){
                            intent.setClass(getBaseContext(), SettingsActivity.class);
                        }else {
                            intent.setClass(getBaseContext(), MainActivity.class);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("needPasscode", false);
                        finish();
                        startActivity(intent);
                    }else{
                        //passcodes do not match
                    }
                }else{
                    //both fields must not be empty
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String pass = sp.getString(MainActivity.SP_KEY_PASSCODE, null);


        Intent i = getIntent();
        needPasscode = i.getBooleanExtra("needPasscode", true);
        Toast.makeText(getBaseContext(), "on resume: " + needPasscode, Toast.LENGTH_LONG).show();
        if(needPasscode) {
            if (pass != null) {
                Intent loginIntent = new Intent();
                loginIntent.setClass(getBaseContext(), LoginActivity.class);
                loginIntent.putExtra("activity", "setup");
                startActivity(loginIntent);
            }
        } else {
            i.putExtra("needPasscode",true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
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
