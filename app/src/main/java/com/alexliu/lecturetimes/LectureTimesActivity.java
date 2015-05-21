package com.alexliu.lecturetimes;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;


public class LectureTimesActivity extends ActionBarActivity {

    private static final String UW_API_URL = "https://api.uwaterloo.ca/v2/";
    private static final String API_KEY = "1a70170d5709e5e4df2e95769e7fc6ba";


    String courseCode;
    LinkedList<String> lectureTimes = new LinkedList();
    LinkedList<String> tutorialTimes = new LinkedList();
    TextView courseCodeText;
    TextView codeInvalidText;
    ListView lecturesList;
    ListView tutorialsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_times);

        courseCodeText = (TextView) findViewById(R.id.courseCode);
        codeInvalidText = (TextView) findViewById(R.id.codeInvalidText);
        Intent intent = getIntent();
        String courseCode = intent.getStringExtra("course");
        courseCodeText.setText(courseCode);
        codeInvalidText.setVisibility(View.INVISIBLE);

        lectureTimes = new LinkedList();
        tutorialTimes = new LinkedList();

        final ArrayAdapter<String> lectureTimesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lectureTimes);
        final ArrayAdapter<String> tutorialTimesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tutorialTimes);
        lecturesList = (ListView) findViewById(R.id.lectureTimesList);
        tutorialsList = (ListView) findViewById(R.id.tutorialTimesList);
        lecturesList.setAdapter(lectureTimesAdapter);
        tutorialsList.setAdapter(tutorialTimesAdapter);



        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(UW_API_URL + "courses/" + courseCode.replaceAll("[ ]","/") + "/schedule.json?key=" + API_KEY)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {

                         @Override
                         public void onFailure(Request request, IOException e) {
                             Log.e("error", "Fetch unsuccessful");
                         }

                         @Override
                         public void onResponse(Response response) throws IOException {

                             try {
                                 if (!response.isSuccessful()) {
                                     Log.e("error", "Fetch unsuccessful");
                                     return;
                                 }

                                 JSONObject mainObj = new JSONObject(response.body().string());

                                 if (!mainObj.getJSONObject("meta").getString("message").equals("Request successful")) {
                                     LectureTimesActivity.this.runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             codeInvalidText.setVisibility(View.VISIBLE);
                                         }
                                     });
                                     return;
                                 }

                                 JSONArray dataArray = new JSONArray();
                                 dataArray = mainObj.getJSONArray("data");
                                 String str = "";
                                 JSONObject obj;
                                 int len = dataArray.length();

                                 for(int i=0; i<len; i++) {

                                     obj = dataArray.getJSONObject(i);

                                     str += obj.getString("section") + " | ";
                                     str += obj.getJSONArray("classes").getJSONObject(0)
                                             .getJSONObject("date")
                                             .getString("start_time") + " ";
                                     str += obj.getJSONArray("classes").getJSONObject(0)
                                             .getJSONObject("date")
                                             .getString("end_time") + " ";
                                     str += obj.getJSONArray("classes").getJSONObject(0)
                                             .getJSONObject("date")
                                             .getString("weekdays") + " | ";
                                     str += obj.getJSONArray("classes").getJSONObject(0)
                                             .getJSONObject("location")
                                             .getString("building") + " ";
                                     str += obj.getJSONArray("classes").getJSONObject(0)
                                             .getJSONObject("location")
                                             .getString("room");


                                     if(obj.getString("section").startsWith("LEC") &&
                                             obj.getString("campus").equals("UW U")) {
                                         lectureTimes.add(str);
                                     } else if(obj.getString("section").startsWith("TUT") &&
                                             obj.getString("campus").equals("UW U")) {
                                         tutorialTimes.add(str);
                                     }

                                     str = "";
                                 }


                                 LectureTimesActivity.this.runOnUiThread(new Runnable() {
                                     @Override
                                     public void run() {
                                         lectureTimesAdapter.notifyDataSetChanged();
                                         tutorialTimesAdapter.notifyDataSetChanged();
                                         Log.d("list", "list updated");
                                     }
                                 });

                             } catch (IOException e) {
                                 Log.e("error", "IOException");
                             } catch (JSONException e) {
                                 Log.e("error", "JSONException");
                             }

                         }
                     }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lecture_times, menu);
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
    public void onBackPressed() {
        finish();
    }
}
