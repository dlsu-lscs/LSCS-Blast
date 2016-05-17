package kewpe.lscsblaster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    Button btn_xls, btn_settings, btn_blast;
    EditText et_what, et_where, et_when;
    ProgressBar bar;
    TextView tv_filepath;
    final static String SP_KEY_PASSCODE = "PASSCODE";
    final static String SP_KEY_FILEPATH = "FILEPATH";
    boolean needPasscode = true;
    static String TAG = "ExelLog";
    private static final int CHOOSEFILE_RESULT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_filepath = (TextView) findViewById(R.id.tv_filepath);
        et_what = (EditText) findViewById(R.id.et_what);
        et_where = (EditText) findViewById(R.id.et_where);
        et_when = (EditText) findViewById(R.id.et_when);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        btn_xls = (Button) findViewById(R.id.btn_xls);
        btn_settings = (Button) findViewById(R.id.btn_settings);
        btn_blast = (Button) findViewById(R.id.btn_blast);

        btn_blast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ProgressTask().execute();
            }
        });

        btn_xls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String path = sp.getString(SP_KEY_FILEPATH, null);
                if(path!=null) {
                    if(!et_what.getText().toString().isEmpty()) {
                        Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                        intent.putExtra("CONTENT_TYPE", "*/*");
                        startActivityForResult(intent, CHOOSEFILE_RESULT);
                    }else{
                        Toast.makeText(getBaseContext(), "What is the message about?", Toast.LENGTH_LONG);
                    }
                }else{
                    Toast.makeText(getBaseContext(), "Choose an excel file", Toast.LENGTH_LONG);
                }
            }
        });

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(getBaseContext(), SettingsActivity.class);
                settingsIntent.putExtra("needPasscode", false);
                finish();
                startActivity(settingsIntent);
            }
        });
    }

    private class ProgressTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute(){
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String filePath = sp.getString(MainActivity.SP_KEY_FILEPATH, null);

            readExcelFile(getBaseContext(), filePath);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            bar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch(requestCode){
            case CHOOSEFILE_RESULT:
                if(resultCode==RESULT_OK){

                    String filePath = data.getData().getPath();
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor spEditor = sp.edit();
                    spEditor.putString(MainActivity.SP_KEY_FILEPATH, filePath);
                    spEditor.commit();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String pass = sp.getString(SP_KEY_PASSCODE, null);
        String path = sp.getString(SP_KEY_FILEPATH, null);

        Intent i = getIntent();
        needPasscode = i.getBooleanExtra("needPasscode", true);
        if(needPasscode) {
            if (pass == null) {
                Intent setupIntent = new Intent();
                setupIntent.setClass(getBaseContext(), SetupActivity.class);
                startActivity(setupIntent);
            } else if (pass != null) {
                Intent loginIntent = new Intent();
                loginIntent.setClass(getBaseContext(), LoginActivity.class);
                loginIntent.putExtra("activity", "main");
                startActivity(loginIntent);
            }
        } else {
            i.putExtra("needPasscode",true);
        }
        if(path!=null){
            tv_filepath.setText(path.substring(path.lastIndexOf("/")+1));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void readExcelFile(Context context, String filename) {

        if (!isExternalStorageAvailable())
        {
            Log.e(TAG, "Storage not available or read only");
            return;
        }

        try{
            // Creating Input Stream
            File file = new File(filename);
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            //POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            XSSFWorkbook myWorkBook = new XSSFWorkbook(myInput);

            // Get the first sheet from workbook
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            Iterator rowIter = mySheet.rowIterator();
            if(rowIter.hasNext()) {
                SmsManager smsManager = SmsManager.getDefault();
                String message = "LSCS BLAST!\n";
                if(!et_what.getText().toString().isEmpty()){
                    message += "WHAT: "+et_what.getText().toString()+"\n";
                }
                if(!et_where.getText().toString().isEmpty()){
                    message += "WHERE: "+et_where.getText().toString()+"\n";
                }
                if(!et_when.getText().toString().isEmpty()){
                    message += "WHEN: "+et_when.getText().toString()+"\n";
                }
                rowIter.next();
                while (rowIter.hasNext()) {
                    XSSFRow myRow = (XSSFRow) rowIter.next();
                    XSSFCell myCell = myRow.getCell(2);
                    //Toast.makeText(getBaseContext(), myCell.getRawValue(), Toast.LENGTH_SHORT);
                    smsManager.sendTextMessage("0"+myCell.getRawValue(), null, message, null, null);
                    Log.d(TAG, "Cell Value: " + myCell.getRawValue());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
