package com.example.android.sunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DetailActivityFragment extends Fragment {

    //moved the weather detail string to a field so that it will be available to our share provider
    private String _weatherDetail;
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState); //note: always need to call this super or app will crash
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent weatherDetailIntent = this.getActivity().getIntent();
        View detailView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView weatherDetailText = (TextView) detailView.findViewById(R.id.weatherDetailText);
        //store the weather detail string in a field so that it will be available to our share provider
        _weatherDetail = weatherDetailIntent.getStringExtra(Intent.EXTRA_TEXT);
        weatherDetailText.setText(_weatherDetail);
        return detailView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //TODO shouldn't have to call menu.clear() here. find out why menu items are being added twice otherwise
        menu.clear();
        inflater.inflate(R.menu.menu_detail, menu);
        //attach a ShareActionProvider to the Share menu item
        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        //note this ShareActionProvider is a little screwy for compatability with API <14
        ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
        if (shareProvider != null) {
            shareProvider.setShareIntent(createShareIntent());
        }
        //super.onCreateOptionsMenu(menu, inflater); //TODO not sure why we call super sometimes and sometimes not
    }

    //create a share intent to be attached to the ShareActionProvider on create options menu
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        //set the following (deprecated) flag to make sure the user returns to OUR app, not maps
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, _weatherDetail + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //note that the action bar will automatically handle clicks
        //on the home/up button as long as a parent activity is specified in AndroidManifest.xml
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        //defaults to false
        return super.onOptionsItemSelected(item);
    }

}
