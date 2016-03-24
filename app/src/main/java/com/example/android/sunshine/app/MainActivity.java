package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //show the settings page
            Log.v(LOG_TAG, "Correctly identified the Settings click in MainActivity.java");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_location) {
            //check to make sure the user's device can respond to implicit map intents
            //then send an implicit intent to the user's map application
            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            //get the user's location setting to use as the basis for the map query
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            //build the Uri which contains the map query with the user's location setting
            Uri mapUri = Uri.parse("geo:0,0?q=" + location);
            mapIntent.setData(mapUri);
            //check to make sure the user's device can respond to implicit map intents
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item); //defaults to false
    }

}
