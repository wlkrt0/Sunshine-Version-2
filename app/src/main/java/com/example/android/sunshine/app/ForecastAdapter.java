package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
//    private String formatHighLows(double high, double low) {
//        boolean isMetric = Utility.isMetric(mContext);
//        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
//        return highLowStr;
//    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
//    private String convertCursorRowToUXFormat(Cursor cursor) {
//        // get row indices for our cursor
//        int idx_max_temp = ForecastFragment.COL_WEATHER_MAX_TEMP;
//        int idx_min_temp = ForecastFragment.COL_WEATHER_MIN_TEMP;
//        int idx_date = ForecastFragment.COL_WEATHER_DATE;
//        int idx_short_desc = ForecastFragment.COL_WEATHER_DESC;
//
//        String highAndLow = formatHighLows(
//                cursor.getDouble(idx_max_temp),
//                cursor.getDouble(idx_min_temp));
//
//        return Utility.formatDate(cursor.getLong(idx_date)) +
//                " - " + cursor.getString(idx_short_desc) +
//                " - " + highAndLow;
//    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //TODO resume here

        //get all the textviews/imageviews that we will populate with weather data from our cursor
        TextView tvHighTemp = (TextView) view.findViewById(R.id.list_item_high_textview);
        TextView tvLowTemp = (TextView) view.findViewById(R.id.list_item_low_textview);
        TextView tvDate = (TextView) view.findViewById(R.id.list_item_date_textview);
        TextView tvDesc = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        ImageView ivIcon = (ImageView) view.findViewById(R.id.list_item_icon);

        // get row indices for our cursor
        int idx_max_temp = ForecastFragment.COL_WEATHER_MAX_TEMP;
        int idx_min_temp = ForecastFragment.COL_WEATHER_MIN_TEMP;
        int idx_date = ForecastFragment.COL_WEATHER_DATE;
        int idx_short_desc = ForecastFragment.COL_WEATHER_DESC;

        //use the cursor to build the strings to be displayed
        boolean isMetric = Utility.isMetric(mContext);
        String highTemp = Utility.formatTemperature(cursor.getDouble(idx_max_temp), isMetric);
        String lowTemp = Utility.formatTemperature(cursor.getDouble(idx_min_temp), isMetric);
        String wDate = Utility.getFriendlyDayString(context, cursor.getLong(idx_date));
        String wDesc = cursor.getString(idx_short_desc);

        //set the textviews to display our strings
        tvHighTemp.setText(highTemp);
        tvLowTemp.setText(lowTemp);
        tvDate.setText(wDate);
        tvDesc.setText(wDesc);
        ivIcon.setImageResource(R.drawable.ic_launcher); //nothing for ivIcon yet - will just use a default image for now
    }
}