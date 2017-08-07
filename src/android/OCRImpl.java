package com.tv.plugin;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tv.plugin.ocrsdk.Client;
import com.tv.plugin.ocrsdk.ProcessingSettings;
import com.tv.plugin.ocrsdk.Task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class OCRImpl extends CordovaPlugin {
    CallbackContext callbackCtx;
    Context ctx;
    Activity activity;

    public OCRImpl(){
    }

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "startImageProcessing":
                // need to send appId and password and image path
                callbackCtx = callbackContext;
                ctx = this.cordova.getActivity().getApplicationContext();
                activity = this.cordova.getActivity();
                JSONObject request = new JSONObject(data.getString(0));

                try {
                    String  applicationId = "";
                    if(request.has("app_ID"))
                        applicationId = request.getString("app_ID");
                    String app_password = "";
                    if(request.has("app_password"))
                        app_password = request.getString("app_password");
                    Client restClient = new Client();
                    
                    // To create an application and obtain a password,
                    // register at http://cloud.ocrsdk.com/Account/Register
                    // More info on getting your application id and password at
                    // http://ocrsdk.com/documentation/faq/#faq3
                    
                    // Name of application you created
                    restClient.applicationId = applicationId;
                    // You should get e-mail from ABBYY Cloud OCR SDK service with the application password
                    restClient.password = app_password;
                    
                    publishProgress( "Uploading image...");
                    
                    String language = "English"; // Comma-separated list: Japanese,English or German,French,Spanish etc.
                    
                    ProcessingSettings processingSettings = new ProcessingSettings();
                    processingSettings.setOutputFormat( ProcessingSettings.OutputFormat.txt );
                    processingSettings.setLanguage(language);
                    
                    publishProgress("Uploading..");

                    // If you want to process business cards, uncomment this
                    /*
                    BusCardSettings busCardSettings = new BusCardSettings();
                    busCardSettings.setLanguage(language);
                    busCardSettings.setOutputFormat(BusCardSettings.OutputFormat.xml);
                    Task task = restClient.processBusinessCard(filePath, busCardSettings);
                    */
                    String inputFile = "";
                    if(request.has("imagePath"))
                       inputFile = request.getString("imagePath");
                    inputFile = inputFile.substring(7);
                        Log.v("input file", inputFile);
                    Task task = restClient.processImage(inputFile, processingSettings);
                    
                    while( task.isTaskActive() ) {
                        // Note: it's recommended that your application waits
                        // at least 2 seconds before making the first getTaskStatus request
                        // and also between such requests for the same task.
                        // Making requests more often will not improve your application performance.
                        // Note: if your application queues several files and waits for them
                        // it's recommended that you use listFinishedTasks instead (which is described
                        // at http://ocrsdk.com/documentation/apireference/listFinishedTasks/).

                        Thread.sleep(5000);
                        publishProgress( "Waiting.." );
                        task = restClient.getTaskStatus(task.Id);
                    }
                    String outputFile = "result.txt";
                    
                    if( task.Status == Task.TaskStatus.Completed ) {
                        publishProgress( "Downloading.." );
                        FileOutputStream fos = activity.openFileOutput(outputFile,Context.MODE_PRIVATE);
                        
                        try {
                            restClient.downloadResult(task, fos);
                            //download complete
                        } finally {
                            fos.close();
                        }
                        updateData();
                        publishProgress( "Ready" );
                    } else if( task.Status == Task.TaskStatus.NotEnoughCredits ) {
                        throw new Exception( "Not enough credits to process task. Add more pages to your application's account." );
                    } else {
                        throw new Exception( "Task failed" );
                    }
                    
                    return true;
                } catch (Exception e) {
                    final String message = "Error: " + e.getMessage();
                    e.printStackTrace();
                    publishProgress( message);
                    //activity.displayMessage(message);
                    return false;
                }
               // return true;
            default:
                return true;
        }
    }

    public void updateData() {
        try {
            StringBuffer contents = new StringBuffer();

            FileInputStream fis = cordova.getActivity().openFileInput("result.txt");
            try {
                Reader reader = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufReader = new BufferedReader(reader);
                String text = "";
                while ((text = bufReader.readLine()) != null) {
                    contents.append(text).append(System.getProperty("line.separator"));
                }
                Log.v("ABBYY response", new String(contents));
                PluginResult result;
                result = new PluginResult(PluginResult.Status.OK, new String(contents));
                result.setKeepCallback(true);
                callbackCtx.sendPluginResult(result);
            } finally {
                fis.close();
            }

        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        } catch (UnsupportedEncodingException ue) {
            ue.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void publishProgress(String message) {
        Toast toast = Toast.makeText(ctx,message, Toast.LENGTH_SHORT);
        toast.show();

    }
}
