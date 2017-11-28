package com.example.mareklaskowski.cbc_news_f2017;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        we're going to use the AsyncTask class to manage
        tasks that take 1-5 seconds or so (recall for very long or continuous tasks, you would start a Service!
        To perform the task of downloading the cbc rss xml file an AsyncTask is appropriate
         */

        //instantiate a URL object used to point to the xml resource
        URL url = null;
        try{
            url = new URL("http://www.cbc.ca/cmlink/rss-topstories");
        }catch (Exception e){
            Log.e("AN EXCEPTION OCCURRED", e.getMessage());
        }

        //use our custom AsyncTask to download and process the xml resource available at the indicated URL
        //call a framework method that will "eventually" call doInBackground defined below..
        new DownloadAndParseXMLTask().execute(url);

    }

    //declare a private inner class, a custom AsyncTask to handle the file download and parsing
    private class DownloadAndParseXMLTask extends AsyncTask<URL, Integer, Long> {

        /*FYI - doInBackground is what is referred to as a "slot" method in a framework because
        you have to fill in its behaviour (abstract class!) before the class can be compiled

        The android framework requires us to be able to handle multiple requests or tasks at the same time,
        so this method has to be able to accept multiple arguments (technically a variadic argument)
        the design is this way so that AsyncTask is more flexible and resusable in other contexts...
        */
        @Override
        protected Long doInBackground(URL... urls) {
            long count = urls.length; //this tells us how many urls we have to process. (in this app always 1)
            long totalSize = 0; //count how many files we have downloaded
            //a loop to download files!
            for(int i  = 0; i < count; i++){
                //delegate the actual file download to a method in MainActivity so we can access all the
                //functionality of the Android Context class
                downloadFile(urls[i]);//note! do not access UI stuff in downloadFile!
                totalSize++;
            }

            return totalSize; //we don't really use the return value, this is more to satisfice the framework
        }
    }

    //all our work will go into this method
    //it will download and then parse the XML
    public void downloadFile(URL url){
        try {
            //1) download
            //create a new http url connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //create an InputStreamReader
            InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
            //let's do something with the data!

            /*
            for now, to test things let's create a buffered reader and just display the contents of the file
            */
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //create a string variable to store each line at a time as we read it from bufferedReader
            String inputLine = null;
            //here's a cool Java one-liner to read a line and make sure it's not NULL (in case of EOF)
            while((inputLine = bufferedReader.readLine())!=  null){
                //display for debugging purposes
                Log.d("HEADLINES DOWNLOADED", inputLine);
            }


            //2) parse

        }catch (Exception e){
            Log.e("DOWNLOAD OR PARSE ERROR", e.getMessage());
        }

    }

}
