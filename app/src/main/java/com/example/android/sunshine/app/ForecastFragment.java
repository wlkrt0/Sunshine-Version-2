package com.example.android.sunshine.app;

import android.net.Uri;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by zach on 3/14/2016.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> forecastAdapter;

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
            weatherWorker.execute("43443");
            return true;
        }
        return super.onOptionsItemSelected(item); //defaults to false per javadoc, showing that we did not handle the menu item selection
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
        forecastAdapter = new ArrayAdapter<String>(
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
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private static final String OWeatherAPIkey = "473d04c5c8aa6d960b814744bf0b3ac7";
        private static final String tempUnits = "metric";
        private static final int forecastDays = 7;

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            forecastAdapter.clear();
            for (String dayForecast : strings) {
                forecastAdapter.add(dayForecast);
            }
        }

        @Override
        protected String[] doInBackground(String... postcode) {

            if (postcode.length == 0) {
                return null;
            }

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
                Uri.Builder URIbuilder = new Uri.Builder();
                URIbuilder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("q", postcode[0])
                        .appendQueryParameter("mode", "JSON")
                        .appendQueryParameter("units", tempUnits)
                        .appendQueryParameter("cnt", Integer.toString(forecastDays))
                        .appendQueryParameter("APPID", OWeatherAPIkey);
                URL url = new URL(URIbuilder.build().toString());

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
                forecastJsonStr = buffer.toString();
                return this.extractForecastFromJSON(forecastJsonStr);
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
            //return null; now unreachable
        }

        private String[] extractForecastFromJSON (String weatherJSON) {
            try {
                JSONObject weatherAPIresponse = new JSONObject(weatherJSON);
                JSONArray weekForecast = weatherAPIresponse.getJSONArray("list");
                String[] finalForecast = new String[weekForecast.length()];
                for (int i = 0; i < weekForecast.length(); i++) {
                    finalForecast[i] = this.getDayForecast(weekForecast, i);
                }
                return finalForecast;
        } catch (JSONException exception) {
                Log.e("ForecastFragment", "Error creating JSON object for weather API response");
                throw new RuntimeException(exception);
            }
        }

        private String getDayForecast(JSONArray weekForecast, int dayIndex) {
            try {
                JSONObject dayForecast = weekForecast.getJSONObject(dayIndex);
                //determine the name of the day (eg. Tomorrow, Friday, etc)
                //note that we must now use GregorianCalendar instead of Time which was deprecated in Android API level 22
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.add(GregorianCalendar.DATE, dayIndex);
                Date timeNow = calendar.getTime();
                SimpleDateFormat todayDate = new SimpleDateFormat("EEE MMM dd");
                String dayName = todayDate.format(timeNow);
                //determine weather description text (eg. Sunny, Cloudy, etc)
                String weatherDesc = dayForecast.getJSONArray("weather").getJSONObject(0).getString("main");
                //get max and min temp. Note no unit conversion is required here because our API query specified our desired units
                double maxTemp = dayForecast.getJSONObject("temp").getDouble("max");
                double minTemp = dayForecast.getJSONObject("temp").getDouble("min");
                String maxTempText = Integer.toString((int) Math.round(maxTemp));
                String minTempText = Integer.toString((int) Math.round(minTemp));
                String dayForecastText = dayName + " - " + weatherDesc + " - " + maxTempText + "/" + minTempText;
                return dayForecastText;
            } catch (JSONException exception) {
                Log.e("ForecastFragment", "Error extracting daily forecast from weekly weather JSONArray");
                throw new RuntimeException(exception);
            }
        }
    }
}
