package kewpe.lscsblaster;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class SaveActivity extends AppCompatActivity {

    Button btn_save, btn_exit;
    RecyclerView contactList;
    ContactAdapter contactAdapter;

    static String TAG = "ExcelLog";
    boolean changePass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        Intent i = getIntent();
        changePass = i.getBooleanExtra("ChangePass", false);

        btn_save = (Button) findViewById(R.id.btn_save);
        btn_exit = (Button) findViewById(R.id.btn_exit);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String filePath = sp.getString(MainActivity.SP_KEY_FILEPATH, null);

        contactList = (RecyclerView) findViewById(R.id.recyclerview);

        final ArrayList<Contact> allContacts = readExcelFile(filePath);

        contactAdapter = new ContactAdapter(allContacts);

        contactList.setAdapter(contactAdapter);

        contactList.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        contactList.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Contact> selectedContacts = new ArrayList<>();
                ArrayList<Contact> contacts = allContacts;
//                String data = "";

                for (int i = 0; i < contacts.size(); i++) {
                    Contact contact = contacts.get(i);
                    if (contact.isSelected() == true) {
                        selectedContacts.add(contact);
//                        data += contact.getName() + "\n";
                    }

                }
                addToContacts(selectedContacts);

//                Toast.makeText(SaveActivity.this,
//                        "Selected contacts: \n" + data, Toast.LENGTH_LONG)
//                        .show();

                Toast.makeText(SaveActivity.this,
                        "Saved to contacts", Toast.LENGTH_LONG)
                        .show();
            }
        });

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
    }

    public boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private ArrayList<Contact> readExcelFile(String filename) {
        ArrayList<Contact> contacts = new ArrayList<>();

        if (!isExternalStorageAvailable()) {
            Log.e(TAG, "Storage not available or read only");
            return null;
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
                rowIter.next();
                while (rowIter.hasNext()) {
                    XSSFRow myRow = (XSSFRow) rowIter.next();
                    XSSFCell nameCell = myRow.getCell(1);
                    XSSFCell numberCell = myRow.getCell(2);

                    String number = "";

                    if (numberCell.getRawValue().startsWith("+63")) {
                        number = numberCell.getRawValue().substring(0, 3);
                    } else if (numberCell.getRawValue().startsWith("9")) {
                        number = "0" + numberCell.getRawValue();
                    } else {
                        number = numberCell.getRawValue();
                    }

                    String name = nameCell.getStringCellValue();

                    Log.d(TAG, "Cellnumber Value: " + numberCell.getRawValue() + " " + number);
                    Log.d(TAG, "Name Value: " + nameCell.getStringCellValue() + " " + name);

                    contacts.add(new Contact(name, number));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contacts;
    }

    public void addToContacts(ArrayList<Contact> contacts) {
        for (Contact c : contacts) {
            ArrayList <ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            String name = c.getName();
            if (name != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                name).build());
            }

            String number = c.getNumber();
            if (number != null) {
                ops.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }

            // Asking the Contact provider to create a new contact
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
