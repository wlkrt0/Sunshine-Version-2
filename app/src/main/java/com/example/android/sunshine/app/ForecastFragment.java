package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by zach on 3/14/2016.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //allow the fragment to handle menu events
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //Add the refresh button to the same menu as in MainActivity
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemID = item.getItemId();
        if (menuItemID == R.id.action_refresh) {
            //perform a refresh of the weather data
            //call the networking code to get real weather data
            FetchWeatherTask weatherWorker = new FetchWeatherTask();
            weatherWorker.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //required to return rootView at the end of this method
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //mock weather forecast data to use until we implement the networking code
        ArrayList<String> forecastItems = new ArrayList<String>();
        forecastItems.add("Today-Sunny-88/63");
        forecastItems.add("Tomorrow-Foggy-72/51");
        forecastItems.add("Weds-Cloudy-72/64");
        forecastItems.add("Thurs-Sunny-88/63");
        forecastItems.add("Fri-Sunny-88/63");
        forecastItems.add("Sat-Sunny-88/63");


        //bind the mock weather forecast data above to the list view control
        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<String>(
                this.getActivity(),
                R.layout.list_item_layout,
                R.id.list_item_forecast_textview,
                forecastItems);
        //bind the mock weather forecast data above to the list view control
        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(forecastAdapter);

        //android needs every fragment.onCreateView(...) method to return inflated XML
        return rootView;
    }
    public class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //try to implement networking code here but expect failure.
            //android won't let you run networking code on the main UI thread.
            //Declare these two outside the try/catch so that they can be closed in the finally block
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=43443&mode=JSON&units=metric&cnt=7&APPID=473d04c5c8aa6d960b814744bf0b3ac7");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString(); //TODO: our JSON isn't being used right now
                Log.v("ForecastFragment", forecastJsonStr);
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}
