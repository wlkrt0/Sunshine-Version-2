package com.example.android.sunshine.app.data;

import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by zach on 3/25/2016.
 */
public class WeatherContract {

    //normalize all dates to start of the julian day at UTC
    //TODO why aren't we using the gregorian calendar here?
    public static long normalizeDate(long startDate) {
        Time t = new Time();
        t.set(startDate);
        int julianDay = Time.getJulianDay(startDate, t.gmtoff);
        return t.setJulianDay(julianDay);
    }

    /* Inner class for location table */
    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";

        //the location setting input by the user which is sent as the query to the Oweather API - type: String
        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        //the latitude and longitude in decimal degrees (not minutes and seconds). - type: float
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        //the city name sting provided by the Oweather API response - type: String
        public static final String COLUMN_CITY_NAME = "city_name";
    }

    /* Inner class for weather table */
    public static final class WeatherEntry implements BaseColumns {
        public static final String TABLE_NAME = "weather";
        //foreign key to location table
        public static final String COLUMN_LOC_KEY = "location_id";

        //milliseconds since epoch - type: long
        public static final String COLUMN_DATE = "date";

        //weather description ID as returned by the Oweather API
        public static final String COLUMN_WEATHER_ID = "weather_id";

        //weather description STRING as returned by Oweather API
        public static final String COLUMN_SHORT_DESC = "short_desc";

        //min and max daily temperatures - type: float
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        //daily percent humidity - type: float
        public static final String COLUMN_HUMIDITY = "humidity";

        //barometric pressure in inHg - type: float
        public static final String COLUMN_PRESSURE = "pressure";

        //windspeed in mph - type: float
        public static final String COLUMN_WIND_SPEED = "wind";

        //wind direction in degrees (0 = N, 180 = S) - type: float
        public static final String COLUMN_DEGREES = "degrees";
    }
}
