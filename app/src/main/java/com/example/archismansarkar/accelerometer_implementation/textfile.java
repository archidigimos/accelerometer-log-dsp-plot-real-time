package com.example.archismansarkar.accelerometer_implementation;

import android.os.Environment;
import android.text.format.DateFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class textfile{
    private static String h;

    private static File filepath;
    private static FileWriter writer;

    private static File filepathx;
    private static FileWriter writerx;

    private static File filepathy;
    private static FileWriter writery;

    private static File filepathz;
    private static FileWriter writerz;

    public static void textfile_initialize(){
        h = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();
            // this will create a new name everytime and unique

        File root = new File(Environment.getExternalStorageDirectory(), "Accelerometer_Log");
            // if external memory exists and folder with name Notes
            if (!root.exists()) {
                root.mkdirs(); // this will create folder.
            }

        File rootx = new File(Environment.getExternalStorageDirectory(), "Accelerometer_Log_X");
        // if external memory exists and folder with name Notes
        if (!rootx.exists()) {
            rootx.mkdirs(); // this will create folder.
        }

        File rooty = new File(Environment.getExternalStorageDirectory(), "Accelerometer_Log_Y");
        // if external memory exists and folder with name Notes
        if (!rooty.exists()) {
            rooty.mkdirs(); // this will create folder.
        }

        File rootz = new File(Environment.getExternalStorageDirectory(), "Accelerometer_Log_Z");
        // if external memory exists and folder with name Notes
        if (!rootz.exists()) {
            rootz.mkdirs(); // this will create folder.
        }

            filepath = new File(root, h + ".txt");  // file path to save
        try{
        writer = new FileWriter(filepath);
       }
         catch (IOException e) {
            e.printStackTrace();
        }

        filepathx = new File(rootx, h + "_x.txt");  // file path to save
        try{
            writerx = new FileWriter(filepathx);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        filepathy = new File(rooty, h + "_y.txt");  // file path to save
        try{
            writery = new FileWriter(filepathy);
        }
        catch (IOException ey) {
            ey.printStackTrace();
        }

        filepathz = new File(rootz, h + "_z.txt");  // file path to save
        try{
            writerz = new FileWriter(filepathz);
        }
        catch (IOException ez) {
            ez.printStackTrace();
        }
    }

    public static void textfile_write(String data,String datax,String datay,String dataz) {

        try {
            writer.append(data);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writerx.append(datax);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            writery.append(datay);

        } catch (IOException ey) {
            ey.printStackTrace();
        }

        try {
            writerz.append(dataz);

        } catch (IOException ez) {
            ez.printStackTrace();
        }
    }

    public static void textfile_close(){
        try {
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writerx.flush();
            writerx.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            writery.flush();
            writery.close();

        } catch (IOException ey) {
            ey.printStackTrace();
        }

        try {
            writerz.flush();
            writerz.close();

        } catch (IOException ez) {
            ez.printStackTrace();
        }
    }

}
