package com.example.mareklaskowski.cbc_news_f2017;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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
        //onPostExecute is called by the framework when the end of doInBackground is reached
        protected void onPostExecute(Long result){
            //once finished all we do here is print out the headlines..
            //or exted this to refresh any views you have (if necessary)
            Log.d("ASYNCTASK COMPLETE", "Downloaded " + result + "files");
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


            //for now, to test things let's create a buffered reader and just display the contents of the file

            /*
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //create a string variable to store each line at a time as we read it from bufferedReader
            String inputLine = null;
            //here's a cool Java one-liner to read a line and make sure it's not NULL (in case of EOF)
            while((inputLine = bufferedReader.readLine())!=  null){
                //display for debugging purposes
                Log.d("HEADLINES DOWNLOADED", inputLine);
            }
            */
            //2) parse
            //now that we have downloaded the file, use the XMLPullParser API to parse the xml
            //and extract the information we need
            //first, get an instance of XmlPullParser from XmlPullParserFactory (recall the factory pattern)
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            //configure the factory to create the specific xml parser flavour we will use
            factory.setNamespaceAware(true);
            //get the XmlPullParser instance from the factory
            XmlPullParser xmlPullParser = factory.newPullParser();
            //connect the XmlPullParser instance to the data source
            //note: we can pass a variety of standard Java IO classes here
            xmlPullParser.setInput(inputStreamReader);
            //recall that the XmlPullParser communicates with your code using "Events"
            int event = xmlPullParser.getEventType();//call this every time we want to inspect the event that occurred

            //pattern: use a flag to recall that we are inside a particular xml element, for example the title element
            boolean insideTitle = false;//we're at the start of the docuemnt to begin with, not inside the title
            while(event != XmlPullParser.END_DOCUMENT){
                //process events/elements inside this loop
                if(event == XmlPullParser.START_DOCUMENT){//this will be the case when the start of the document is reached
                    Log.d("PARSING XML", "We reached the start of the document");
                }else if(event == XmlPullParser.START_TAG){//case when the start of an element (tag) is reached
                    String tagName = xmlPullParser.getName(); //gets the element type
                    Log.d("PARSING XML", "We reached the start of tag: " + tagName);
                    //TODO: use the tag name or type here to determine whether you have reached a news <item> or <link>
                    if(tagName.equalsIgnoreCase("title")){
                        //store this headline in the headlines
                        Log.d("PARSING XML", "found a title tag:" + tagName);
                        insideTitle = true;
                    }
                }else if(event == XmlPullParser.END_TAG){//encountered the end of an element
                    Log.d("PARSING XML", "We reached the end of tag: " + xmlPullParser.getName());
                    insideTitle = false;

                }else if(event == XmlPullParser.TEXT){//encountered the CONTENT of an element
                    String text = xmlPullParser.getText();
                    Log.d("PARSING XML", "found text: " + text);
                    if(insideTitle){
                        //add the headline to the list of headlines...
                    }
                }

                //don't forget to get the next event or element in the xml file!
                event = xmlPullParser.next();//bit like navigating SQL records...
            }

        }catch (Exception e){
            Log.e("DOWNLOAD OR PARSE ERROR", e.getMessage());
        }

    }

}
