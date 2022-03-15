package com.example.common.network.http;

import android.os.AsyncTask;
import android.util.Log;

import com.example.common.callback.ITaskCompleted;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpAsyncTaskGet extends AsyncTask<Object, Void, String> {

    private static final String TAG = HttpAsyncTaskGet.class.getSimpleName();

    private final ITaskCompleted listener;
    private final int requestId;

    public HttpAsyncTaskGet(ITaskCompleted listener, int requestId) {
        this.listener = listener;
        this.requestId = requestId;
    }

    public static String get(String urlString, Map<String, String> headers) {
        String result = "";
        try {
            Log.d(TAG, "Sending url[" + urlString + "]");
            // create HttpPost
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = null;
            try {
                urlConnection.setRequestMethod("GET");
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
                int code = urlConnection.getResponseCode();
                if (code == 200) {
                    // receive response as inputStream
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    // convert inputstream to string
                    result = convertInputStreamToString(inputStream);
                } else {
                    result = "Did not work!";
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream)throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));
        String line = "";
        StringBuilder result = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        inputStream.close();
        return result.toString();
    }

    // doInBackground execute tasks when asynctask is run
    @SuppressWarnings("unchecked")
    @Override
    protected String doInBackground(Object... parameters) {
        return get((String) parameters[0], (Map<String, String>) parameters[1]);
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String response) {
        Log.d(TAG, response);
        listener.onTaskCompleted(response, requestId);
    }
}
