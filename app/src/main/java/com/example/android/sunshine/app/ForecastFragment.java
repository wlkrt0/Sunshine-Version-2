package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.Toast;

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

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
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
    public void onStart() {
        //TODO what is the difference between onCreate and onStart? Covered in lesson 5 per udacity
        super.onStart();
        FetchWeather();
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
            FetchWeather();
            return true;
        }
        return super.onOptionsItemSelected(item); //defaults to false per javadoc, showing that we did not handle the menu item selection
    }

    //perform a refresh of the multi-day weather forecast using background thread
    private void FetchWeather() {
        //call the networking code to get real weather data using a worker object (off UI thread)
        FetchWeatherTask weatherWorker = new FetchWeatherTask();
        //get the zip code from the settings/preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String zip = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        weatherWorker.execute(zip);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //required to return rootView at the end of this method
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //empty arrayList to hold daily weather forecasts
        final ArrayList<String> forecastItems = new ArrayList<String>();

        //bind the mock weather forecast data above to the list view control
        forecastAdapter = new ArrayAdapter<String>(
                this.getActivity(),
                R.layout.list_item_layout,
                R.id.list_item_forecast_textview,
                forecastItems);
        //bind the mock weather forecast data above to the list view control
        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(forecastAdapter);
        //display the DetailActivity when the user clicks on a weather item in our ListView
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getActivity(), forecastAdapter.getItem(i), Toast.LENGTH_SHORT).show();
                Intent weatherDetailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecastAdapter.getItem(i));
                startActivity(weatherDetailIntent);
            }
        });

        //android needs every fragment.onCreateView(...) method to return inflated XML
        return rootView;
    }
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private static final String OWeatherAPIkey = "473d04c5c8aa6d960b814744bf0b3ac7";
        private static final String tempUnits = "metric";
        private static final int forecastDays = 7;

        @Override
        protected void onPostExecute(String[] finalForecast) {
            super.onPostExecute(finalForecast);
            forecastAdapter.clear();
            for (String dayForecast : finalForecast) {
                //note that each time we call forecastAdapter.add,
                // the adapter automatically runs .notifyDataSetChanged() on itself
                //also note that if we were targeting android API 11 or higher, we could use .addAll instead
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
            //get the user's preferred units from settings so we know whether to return C or F
            //do this now so we don't make repeated calls to the preferences for each day of the week
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ForecastFragment.this.getActivity());
            String units = prefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));
            //try is required any time we are doing JSON parsing
            try {
                JSONObject weatherAPIresponse = new JSONObject(weatherJSON);
                JSONArray weekForecast = weatherAPIresponse.getJSONArray("list");
                String[] finalForecast = new String[weekForecast.length()];
                //iterate through weekForecast by each dayForecast, building up finalForecast array
                for (int i = 0; i < weekForecast.length(); i++) {
                    finalForecast[i] = this.getDayForecast(weekForecast, i, units);
                }
                return finalForecast;
        } catch (JSONException exception) {
                Log.e("ForecastFragment", "Error creating JSON object for weather API response");
                throw new RuntimeException(exception);
            }
        }

        private String getDayForecast(JSONArray weekForecast, int dayIndex, String units) {
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
                //extract max and min temp from JSON (in metric (C), regardless of user's setting)
                double maxTempC = dayForecast.getJSONObject("temp").getDouble("max");
                double minTempC = dayForecast.getJSONObject("temp").getDouble("min");
                //convert C to F if necessary and return the max and min temp strings ready to concatenate
                String maxTempText = getTempText(maxTempC, units);
                String minTempText = getTempText(minTempC, units);
                //now that everything is a string, concatenate together in the form it will be displayed to the user
                String dayForecastText = dayName + " - " + weatherDesc + " - " + maxTempText + "/" + minTempText;
                return dayForecastText;
            } catch (JSONException exception) {
                Log.e("ForecastFragment", "Error extracting daily forecast from weekly weather JSONArray");
                throw new RuntimeException(exception);
            }
        }

        private String getTempText(double tempC, String units) {
            double temp = tempC;
            //convert to F if the user's preferences are set to imperial
            //if not, leave it as-is since the open weather API always returns metric (C)
            if (units.equals(getString(R.string.pref_units_key_imperial))) {
                temp = (tempC * 1.8) + 32.0;
            } else {
                //do nothing. this block is here for debugging only
                //Log.v(ForecastFragment.LOG_TAG, "units = " + units);
                //Log.v(ForecastFragment.LOG_TAG, "R.string.pref_units_key_imperial = " + units);
            }
            //convert double to int and int to string so it will be ready to display to the user
            int tempInt = (int) Math.round(temp);
            String tempText = Integer.toString(tempInt);
            return tempText;
        }
    }
}
