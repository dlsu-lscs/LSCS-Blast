package kewpe.lscsblaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class BlastActivity extends AppCompatActivity {

    RadioButton rbn_idNum, rbn_course, rbn_selected;
    RadioGroup radio_filter;
    Button btn_blast, btn_back;
    RecyclerView listView_ID, listView_course;
    ProgressBar bar;

    FilterAdapter filterAdapter, filterAdapter2;

    static String TAG = "ExcelLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blast);

        rbn_idNum = (RadioButton) findViewById(R.id.rbn_idNum);
        rbn_course = (RadioButton) findViewById(R.id.rbn_course);
        radio_filter = (RadioGroup) findViewById(R.id.radio_filter);
        btn_blast = (Button) findViewById(R.id.btn_blast);
        btn_back = (Button) findViewById(R.id.btn_back);
        listView_ID = (RecyclerView) findViewById(R.id.listView);
        listView_course = (RecyclerView) findViewById(R.id.listView2);
        bar = (ProgressBar) findViewById(R.id.progressBar);

        //Recycler View for Filter by ID Number
        final ArrayList<Filter> idNumbers = initializeIDNumber();
        filterAdapter = new FilterAdapter(idNumbers);

        listView_ID.setAdapter(filterAdapter);

        listView_ID.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        listView_ID.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        //Recycler View for Filter by Course
        final ArrayList<Filter> courses = initializeCourses();
        filterAdapter2 = new FilterAdapter(courses);

        listView_course.setAdapter(filterAdapter2);

        listView_course.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        listView_course.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        rbn_idNum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listView_ID.setVisibility(View.VISIBLE);
            }
        });

        rbn_course.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listView_course.setVisibility(View.VISIBLE);
            }
        });

        btn_blast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String filePath = sp.getString(MainActivity.SP_KEY_FILEPATH, null);

                ArrayList<Filter> filters = new ArrayList<>();
                String columnName = "";

                int selectedId = radio_filter.getCheckedRadioButtonId();
                rbn_selected = (RadioButton) findViewById(selectedId);
                if (rbn_selected.getText().equals("ID Number")) {
                    filters = idNumbers;
                    columnName = "ID Number";
                }
                else if (rbn_selected.getText().equals("Course")) {
                    filters = courses;
                    columnName = "Course";
                }

                ArrayList<Filter> selectedFilters = new ArrayList<>();

                for (int i = 0; i < filters.size(); i++) {
                    Filter filter = filters.get(i);
                    if (filter.isSelected() == true) {
                        selectedFilters.add(filter);
                    }
                }

                final ArrayList<String> numbers = filter(filePath, selectedFilters, columnName);
                new BlastActivity.ProgressTask(numbers).execute();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
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

    public ArrayList<Filter> initializeIDNumber() {
        ArrayList<Filter> idNumbers = new ArrayList<>();
        ArrayList<String> id = new ArrayList<>(Arrays.asList("112 and up", "113", "114", "115", "116"));

        for (int i = 0; i < id.size(); i++) {
            Filter f = new Filter(id.get(i));
            idNumbers.add(f);
        }

        return idNumbers;
    }

    public ArrayList<Filter> initializeCourses() {
        ArrayList<Filter> courses = new ArrayList<>();
        ArrayList<String> c = new ArrayList<>(Arrays.asList("BS CS", "BS CS-ST", "BS CS-NE", "BS CS-IST", "IT", "INSYS"));

        for (int i = 0; i < c.size(); i++) {
            Filter f = new Filter(c.get(i));
            courses.add(f);
        }

        return courses;
    }

    public boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public ArrayList<String> filter(String filename, ArrayList<Filter> filters, String columnName) {
        ArrayList<String> chosenPeople = new ArrayList<>();

        if (!isExternalStorageAvailable()) {
            Log.e(TAG, "Storage not available or read only");
            return null;
        }

        try {
            // Creating Input Stream
            File file = new File(filename);
            FileInputStream myInput = new FileInputStream(file);

            // Create a workbook using the File System
            XSSFWorkbook myWorkBook = new XSSFWorkbook(myInput);

            // Get the first sheet from workbook
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            Integer columnNo = null;
            Integer numberColumnNo = null;

            Row firstRow = mySheet.getRow(0);

            for(Cell cell : firstRow){
                if (columnName.equals(cell.getStringCellValue())){
                    columnNo = cell.getColumnIndex();
                }
                if ("Number".equalsIgnoreCase(cell.getStringCellValue())) {
                    numberColumnNo = cell.getColumnIndex();
                }
            }

            if (columnNo != null){
                Iterator rowIter = mySheet.rowIterator();
                if (rowIter.hasNext()) {
                    rowIter.next();
                    while (rowIter.hasNext()) {
                        XSSFRow myRow = (XSSFRow) rowIter.next();
                        XSSFCell c = myRow.getCell(columnNo);
                        if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK) {
                            // Nothing in the cell in this row, skip it
                        } else {
                            if ("ID Number".equalsIgnoreCase(columnName)) {
                                for (Filter f : filters) {
                                    if (f.getFilter().contains("and up")) { //112 and up
                                        String[] idSplit = f.getFilter().split(" and");
                                        int idNum = Integer.parseInt(idSplit[0]);
                                        String firstThreeLetters = c.getRawValue().substring(0, Math.min(c.getRawValue().length(), 3));

                                        if (idNum >= Integer.parseInt(firstThreeLetters)) {
                                            String number = getNumber(myRow.getCell(numberColumnNo));
                                            chosenPeople.add(number);
                                        }
                                    } else if (c.getRawValue().startsWith(f.getFilter())) { //113, 114, 115, 116
                                        String number = getNumber(myRow.getCell(numberColumnNo));
                                        chosenPeople.add(number);
                                    }
                                }
                            } else if ("Course".equalsIgnoreCase(columnName)) {
                                for (Filter f : filters) {
                                    if (c.getStringCellValue().equalsIgnoreCase(f.getFilter())) {
                                        String number = getNumber(myRow.getCell(numberColumnNo));
                                        chosenPeople.add(number);
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(BlastActivity.this,
                        "Could not find column " + columnName + " in " + filename, Toast.LENGTH_LONG)
                        .show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chosenPeople;
    }

    public String getNumber(XSSFCell numberCell) {
        String number = "";

        if (numberCell.getRawValue().startsWith("+63")) {
            number = numberCell.getRawValue().substring(0, 3);
        } else if (numberCell.getRawValue().startsWith("9")) {
            number = "0" + numberCell.getRawValue();
        } else {
            number = numberCell.getRawValue();
        }

        return number;
    }

    private class ProgressTask extends AsyncTask<Void, Void, Void> {
        ArrayList<String> numbers = new ArrayList<>();

        public ProgressTask(ArrayList<String> numbers) {
            super();
            this.numbers = numbers;
        }

        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            MainActivity.sendSMS(numbers);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            bar.setVisibility(View.GONE);
        }
    }
}
