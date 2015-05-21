package com.alexliu.lecturetimes;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


public class DataStorage {

    private static String FILENAME = "data.txt";

    public static void storeData(LinkedList<String> list, Context context) throws IOException {
        try {
            File file = new File(context.getFilesDir(), FILENAME);
            FileWriter writer = new FileWriter(file);
            for(String str : list) {
                writer.write(str);
                writer.write('\n');
            }
            Log.d("DataStorage", "file written");
            writer.close();
        } catch(IOException e) {
            Toast alertToast = Toast.makeText(context.getApplicationContext(),
                    R.string.file_write_error, Toast.LENGTH_SHORT);
        }
    }

    public static LinkedList<String> readData(LinkedList<String> list, Context context) {
        File file = new File(context.getFilesDir(), FILENAME);
        String str = "";
        int character;
        FileReader reader;

        try {
            reader = new FileReader(file);

            while((character = reader.read()) > 0) {
                if (character != (int) '\n') {
                    str += (char) character;
                } else {
                    list.add(str);
                    str = "";
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Log.d("DataStorage", "file not found");
        } catch (IOException e) {

        }
        return list;
    }

}
