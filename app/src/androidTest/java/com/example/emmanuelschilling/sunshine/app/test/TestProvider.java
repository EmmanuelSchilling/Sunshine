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
package com.example.emmanuelschilling.sunshine.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.emmanuelschilling.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.emmanuelschilling.sunshine.app.data.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteAllRecords() {
        mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(LocationEntry.CONTENT_URI, null, null);

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), 0);
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), 0);
        cursor.close();
    }

    public void testInsertReadProvider() {
        testDeleteAllRecords();

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ContentValues testValues = TestDb.buildLocationValues();
        Uri insertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);

        long locationRowId;
        locationRowId = ContentUris.parseId(insertUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor locationCursor = mContext.getContentResolver().query(LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (locationCursor.moveToFirst()) {
            TestDb.validateCursor(testValues, locationCursor);

            // Fantastic.  Now that we have a location, add some weather!
            ContentValues weatherValues = TestDb.buildWeatherValues(locationRowId);

            Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);

            // A cursor is your primary interface to the query results.
            Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (weatherCursor.moveToFirst()) {
                TestDb.validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather data returned.");
            }

            weatherCursor.close();

            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (weatherCursor.moveToFirst()) {
                TestDb.validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather data returned.");
            }

            weatherCursor.close();

            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithStartDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (weatherCursor.moveToFirst()) {
                TestDb.validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather data returned.");
            }

            weatherCursor.close();

            weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (weatherCursor.moveToFirst()) {
                TestDb.validateCursor(weatherValues, weatherCursor);
            } else {
                fail("No weather data returned.");
            }
        } else {
            fail("No location data returned.");
        }
    }

    public void testUpdateProvider() {
        testDeleteAllRecords();

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ContentValues testValues = TestDb.buildLocationValues();
        Uri insertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);

        long locationRowId;
        locationRowId = ContentUris.parseId(insertUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedLocation = new ContentValues(testValues);
        updatedLocation.put(LocationEntry._ID, locationRowId);
        updatedLocation.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(LocationEntry.CONTENT_URI, updatedLocation, LocationEntry._ID + "= ?", new String[] {Long.toString(locationRowId)});
        assertEquals(count, 1);

        // Data's updated.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor locationCursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (locationCursor.moveToFirst()) {
            TestDb.validateCursor(updatedLocation, locationCursor);
            locationCursor.close();

            // Fantastic.  Now that we have a location, add some weather!
            ContentValues weatherValues = TestDb.buildWeatherValues(locationRowId);

            Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
            long weatherRowId = ContentUris.parseId(weatherInsertUri);

            // Update the row.
            ContentValues updatedWeather = new ContentValues(weatherValues);
            updatedWeather.put(WeatherEntry._ID, weatherRowId);
            updatedWeather.put(WeatherEntry.COLUMN_SHORT_DESC, "Meteorites");

            count = mContext.getContentResolver().update(WeatherEntry.CONTENT_URI, updatedWeather, WeatherEntry._ID + "= ?", new String[] {Long.toString(weatherRowId)});
            assertEquals(count, 1);

            // A cursor is your primary interface to the query results.
            Cursor weatherCursor = mContext.getContentResolver().query(
                    WeatherEntry.CONTENT_URI,
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (weatherCursor.moveToFirst()) {
                TestDb.validateCursor(updatedWeather, weatherCursor);
            } else {
                fail("No weather data returned.");
            }

            weatherCursor.close();
        }
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testDeleteAllRecordsEnd() {
        testDeleteAllRecords();
    }
}