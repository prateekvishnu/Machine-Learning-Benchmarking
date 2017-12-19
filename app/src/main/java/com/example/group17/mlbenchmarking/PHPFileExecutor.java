package com.example.group17.mlbenchmarking;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class PHPFileExecutor {

    //Using Async task for triggering the PHP script
    //Code referred and modified based on requirement

    public static class updateData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;

            //passing the php script path to execute

            try {
                URL url;
                url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ){
                    InputStream is = conn.getInputStream();
                }else{
                    InputStream err = conn.getErrorStream();
                }
                return "Done";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }
}
