package com.example.emmanuelschilling.sunshine.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.emmanuelschilling.sunshine.app.data.WeatherContract;
import com.example.emmanuelschilling.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.emmanuelschilling.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.emmanuelschilling.sunshine.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

//import com.example.emmanuelschilling.sunshine.app.data.WeatherContract;

/**
 * Created by emmanuelschilling on 8/15/14.
 */
public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = TestDb.class.getSimpleName();

    public String TEST_CITY_NAME = "North Pole";

    private ContentValues buildWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    private ContentValues buildLocationValues() {
        String testLocationSetting = "99705";
        double testLatitude = 64.772;
        double testLongitude = -147.355;

        ContentValues locationValues = new ContentValues();

        locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, testLatitude);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, testLongitude);

        return locationValues;
    }

    private void validateCursor(ContentValues expectedValues, Cursor cursor) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int index = cursor.getColumnIndex(columnName);
            assertFalse(index == -1);

            String expectedValue = entry.getValue().toString();
            String testValue = cursor.getString(index);
            assertEquals(expectedValue, testValue);
        }
    }

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();

        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();

        ContentValues locationValues = buildLocationValues();

        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, locationValues);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New location row id: " + locationRowId);

        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            validateCursor(locationValues, cursor);

            // Fantastic.  Now that we have a location, add some weather!
            ContentValues weatherValues = buildWeatherValues(locationRowId);

            long weatherRowId;
            weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

            assertTrue(weatherRowId != -1);
            Log.d(LOG_TAG, "New weather row id: " + weatherRowId);

            Cursor weatherCursor = db.query(
                    WeatherEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather values returned.");
            }
        } else {
            fail("No location values returned.");
        }

        db.close();
    }
}
