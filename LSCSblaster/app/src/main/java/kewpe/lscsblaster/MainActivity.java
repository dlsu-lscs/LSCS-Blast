package kewpe.lscsblaster;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
    Button btn_xls, btn_settings, btn_blast, btn_contacts;
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
        btn_contacts = (Button) findViewById(R.id.btn_contacts);

        btn_blast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String path = sp.getString(SP_KEY_FILEPATH, null);
                Toast.makeText(getBaseContext(), "fp: " + path, Toast.LENGTH_LONG).show();
                if (path != null) {
                    if (!et_what.getText().toString().isEmpty()) {
                        new ProgressTask().execute();
                    } else {
                        Toast.makeText(getBaseContext(), "What is the message about?", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Choose an excel file", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_xls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Open folder"), CHOOSEFILE_RESULT);
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

        btn_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent saveIntent = new Intent();
                saveIntent.setClass(getBaseContext(), SaveActivity.class);
                saveIntent.putExtra("needPassword", false);
                finish();
                startActivity(saveIntent);
            }
        });
    }

    private class ProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
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
        switch (requestCode) {
            case CHOOSEFILE_RESULT:
                if (resultCode == RESULT_OK) {

                    String filePath = getPath(this, data.getData());
                    Log.d(TAG, "filepath: " + filePath);
                    Toast.makeText(getBaseContext(), "filepath: " + filePath, Toast.LENGTH_LONG).show();
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor spEditor = sp.edit();
                    spEditor.putString(MainActivity.SP_KEY_FILEPATH, filePath);
                    spEditor.commit();
                }
                break;
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String pass = sp.getString(SP_KEY_PASSCODE, null);
        String path = sp.getString(SP_KEY_FILEPATH, null);

        Intent i = getIntent();
        needPasscode = i.getBooleanExtra("needPasscode", true);
        if (needPasscode) {
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
            i.putExtra("needPasscode", true);
        }
        if (path != null) {
            tv_filepath.setText(path.substring(path.lastIndexOf("/") + 1));
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
    //checkpoint

    private void readExcelFile(Context context, String filename) {

        if (!isExternalStorageAvailable()) {
            Log.e(TAG, "Storage not available or read only");
            return;
        }

        try {
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
            if (rowIter.hasNext()) {
                SmsManager smsManager = SmsManager.getDefault();
                String message = "LSCS BLAST!\n";
                if (!et_what.getText().toString().isEmpty()) {
                    message += "WHAT: " + et_what.getText().toString() + "\n";
                }
                if (!et_where.getText().toString().isEmpty()) {
                    message += "WHERE: " + et_where.getText().toString() + "\n";
                }
                if (!et_when.getText().toString().isEmpty()) {
                    message += "WHEN: " + et_when.getText().toString() + "\n";
                }
                rowIter.next();
                while (rowIter.hasNext()) {
                    XSSFRow myRow = (XSSFRow) rowIter.next();
                    XSSFCell myCell = myRow.getCell(2);
                    String number;
                    if (myCell.getRawValue().startsWith("+63")) {
                        number = myCell.getRawValue().substring(0, 3);
                    } else if (myCell.getRawValue().startsWith("9")) {
                        number = "0" + myCell.getRawValue();
                    } else {
                        number = myCell.getRawValue();
                    }

                    smsManager.sendTextMessage("0" + number, null, message, null, null);
                    Log.d(TAG, "Cell Value: " + myCell.getRawValue() + " " + number);
                }
            }
        } catch (Exception e) {
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
