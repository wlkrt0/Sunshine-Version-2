/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private final static int CURSOR_LOADER_ID = 0;
        private ShareActionProvider mShareActionProvider = null;
        private String mDetailText = null;
        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private Uri mForecastUri;
        private static final String[] DETAIL_COLUMNS = {
                //must fully qualify _ID column because we are joining two tables (weather and location)
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_DATE
        };
        //NOTE: ints below are tied to columns above. if one changes the other needs to as well.
        private static final int COL_WEATHER_ID = 0;
        private static final int COL_MAX_TEMP = 1;
        private static final int COL_MIN_TEMP = 2;
        private static final int COL_SHORT_DESC = 3;
        private static final int COL_DATE = 4;


        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mForecastUri = intent.getData();
                Log.v("ZACH", mForecastUri.toString());
                //get a loader
                this.getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
            }
//            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
//                mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
//                ((TextView) rootView.findViewById(R.id.detail_text))
//                        .setText(mForecastStr);
//            }

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            //if onLoadFinished happens before this, we can set the share intent now
            if (mDetailText != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    mDetailText + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v("ZACH", mForecastUri.toString());
            //BIG NOTE! the reason we don't need to do any filtering (SELECT WHERE LOCATION = ?, TIME = ?)
            //is because we instantiate CursorLoader with mForecastUri which looks like:
            //content://com.example.android.sunshine.app/weather/90210/1460098800000
            //CursorLoader asks ContentResolver which Provider handles this type of URI.
            //In our manifest file, we've said that any URI that starts with com.example.android.sunshine.app
            //is handled by .data.WeatherProvider, so
            //ContentResolver finds our WeatherProvider, sends it our Uri, and asks for a cursor in return.
            //our weatherProvider disassembles the Uri and does the filtering for us before returning the cursor.
            CursorLoader cl = new CursorLoader(getActivity(), mForecastUri, DETAIL_COLUMNS, null, null, null);
            return cl;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            //get a reference to our textview where we'll display the detailed weather forecast string
            TextView weatherDetail = (TextView) getActivity().findViewById(R.id.detail_text);
            //get the user's units setting
            boolean isMetric = Utility.isMetric(getActivity());
            //only expected one result from our cursor
            data.moveToFirst();
            //get values from database to be build our weather description string
            String maxTemp = Utility.formatTemperature(data.getDouble(COL_MAX_TEMP), isMetric);
            String minTemp = Utility.formatTemperature(data.getDouble(COL_MIN_TEMP), isMetric);
            String weatherDate = Utility.formatDate(data.getLong(COL_DATE));
            String weatherDesc = data.getString(COL_SHORT_DESC);
            //build the final detailed weather forecast string to be displayed
            mDetailText = weatherDate + " - " + weatherDesc + " - " + maxTemp + "/" + minTemp;
            weatherDetail.setText(mDetailText);

            //if onCreateOptionsMenu has already happened, we need to update the share intent now
            if (mShareActionProvider != null ) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            //do nothing
        }
    }
}
