package com.alexliu.lecturetimes;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;

public class MainActivity extends ActionBarActivity {

    EditText courseCodeField;
    Button addCourseButton;
    Button undoAddButton;
    ListView coursesListView;
    LinkedList<String> coursesList;
    TextView listEmptyText;

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        courseCodeField = (EditText) findViewById(R.id.courseCodeField);

        addCourseButton = (Button) findViewById(R.id.addButton);
        undoAddButton = (Button) findViewById(R.id.undoAddButton);

        listEmptyText = (TextView) findViewById(R.id.listEmptyTextView);

        coursesListView = (ListView) findViewById(R.id.courseList);
        coursesList = new LinkedList();
        coursesList = DataStorage.readData(coursesList, this);
        final ArrayAdapter<String> courseListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, coursesList);
        coursesListView.setAdapter(courseListAdapter);
        courseListAdapter.notifyDataSetChanged();

        coursesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, LectureTimesActivity.class);

                boolean connected = isOnline();

                if(connected) {
                    intent.putExtra("course", coursesList.get(position));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Toast alertToast = Toast.makeText(getApplicationContext(),
                R.string.course_code_format_invalid, Toast.LENGTH_SHORT);


        addCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = courseCodeField.getText().toString().replaceAll("\\s", "").toUpperCase();
                if (str.length() > 0 && str.matches("[A-Z]+\\d{3}")) {
                    str = str.replaceAll("[0-9]+","") + " " +str.replaceAll("[A-Z]+","");
                    coursesList.addFirst(str);
                    courseListAdapter.notifyDataSetChanged();
                    courseCodeField.setText("");
                } else {
                    alertToast.show();
                }

                if(coursesList.isEmpty()) {
                    listEmptyText.setVisibility(View.VISIBLE);
                } else {
                    listEmptyText.setVisibility(View.INVISIBLE);
                }
            }
        });


        undoAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(coursesList.size()>0) {
                    coursesList.removeFirst();
                    courseListAdapter.notifyDataSetChanged();
                }

                if(coursesList.isEmpty()) {
                    listEmptyText.setVisibility(View.VISIBLE);
                } else {
                    listEmptyText.setVisibility(View.INVISIBLE);
                }
            }
        });

        if(coursesList.isEmpty()) {
            listEmptyText.setVisibility(View.VISIBLE);
        } else {
            listEmptyText.setVisibility(View.INVISIBLE);
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

    @Override
    public void onStop() {
        try {
            DataStorage.storeData(coursesList, this);
        } catch (IOException e) {

        }
        super.onStop();
    }
}
