package com.example.emmanuelschilling.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.emmanuelschilling.sunshine.R;
import com.example.emmanuelschilling.sunshine.app.data.WeatherContract;

/**
 * Created by emmanuelschilling on 8/24/14.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private final String SUNSHINE_HASHTAG = " #SunshineApp";

    private static final int DETAIL_LOADER = 0;
    public static final String DATE_KEY = "date";
    public static final String LOCATION_KEY = "location";

    private String mForecastString;
    private String mLocation;
    private View mView;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null.");
        }
    }

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        Bundle args = getArguments();
        if (args != null && args.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_detail, container, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        mView = view;

        return view;
    }

    private Intent createShareForecastIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecastString + SUNSHINE_HASHTAG);

        return intent;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCATION_KEY, mLocation);
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle args = getArguments();
        if (args != null &&
                args.containsKey(DetailActivity.DATE_KEY) &&
                mLocation != null &&
                !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String columns[] = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_DEGREES,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_CONDITION_ID,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
        };

        String dateString = getArguments().getString(DetailActivity.DATE_KEY);

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, dateString);

        return new CursorLoader(
                getActivity(),
                weatherUri,
                columns,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String forecast = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            String dateText = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));

            float high = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            float low = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));

            float windSpeed = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            float windDir = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));

            float humidity = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));

            float pressure = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));

            boolean isMetric = Utility.isMetric(getActivity());

            int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_CONDITION_ID));

            ViewHolder viewHolder =(ViewHolder) mView.getTag();

            viewHolder.dayView.setText(Utility.getDayName(getActivity(), dateText));
            viewHolder.dateView.setText(Utility.getFormattedMonthDay(getActivity(), dateText));
            viewHolder.forecastView.setText(forecast);
            viewHolder.highTempView.setText(Utility.formatTemperature(getActivity(), high, isMetric));
            viewHolder.lowTempView.setText(Utility.formatTemperature(getActivity(), low, isMetric));

            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            viewHolder.iconView.setContentDescription(forecast);

            viewHolder.humidityView.setText(Utility.formatHumidity(getActivity(), humidity));
            viewHolder.windView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDir));
            viewHolder.pressureView.setText(Utility.formatPressure(getActivity(), pressure));

            mForecastString = String.format("%s - %s - %s/%s",
                    viewHolder.dateView.getText(),
                    viewHolder.forecastView.getText(),
                    viewHolder.highTempView.getText(),
                    viewHolder.lowTempView.getText()
            );
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dayView;
        public final TextView dateView;
        public final TextView forecastView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidityView;
        public final TextView windView;
        public final TextView pressureView;

        public ViewHolder(View view) {
            iconView = (ImageView)view.findViewById(R.id.detail_icon);
            dayView = (TextView)view.findViewById(R.id.detail_day_textview);
            dateView = (TextView)view.findViewById(R.id.detail_date_textview);
            forecastView = (TextView)view.findViewById(R.id.detail_forecast_textview);
            highTempView = (TextView)view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView)view.findViewById(R.id.detail_low_textview);
            humidityView = (TextView)view.findViewById(R.id.detail_humidity_textview);
            windView = (TextView)view.findViewById(R.id.detail_wind_textview);
            pressureView = (TextView)view.findViewById(R.id.detail_pressure_textview);
        }
    }
}
