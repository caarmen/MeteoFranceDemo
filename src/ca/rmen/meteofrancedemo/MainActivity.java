/*
 * ----------------------------------------------------------------------------
 * "THE WINE-WARE LICENSE" Version 1.0:
 * Carmen Alvarez wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a glass of wine in return. 
 * 
 * THE AUTHOR OF THIS FILE IS NOT RESPONSIBLE FOR LOSS OF LIFE, LIMBS, SELF-ESTEEM,
 * MONEY, RELATIONSHIPS, OR GENERAL MENTAL OR PHYSICAL HEALTH CAUSED BY THE
 * CONTENTS OF THIS FILE OR ANYTHING ELSE.
 * ----------------------------------------------------------------------------
 */
package ca.rmen.meteofrancedemo;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Demo app which retrieves and displays the rain forecast over the next hour
 * for a city the user enters.
 */
public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	/**
	 * Read the search term the user entered, retrieve the near forecast, and
	 * update the TextView with the forecast details.
	 */
	public void getForecast(View v) {

		final TextView textViewSearch = (TextView) findViewById(R.id.txt_search);
		if (TextUtils.isEmpty(textViewSearch.getText()))
			return;
		final TextView textViewForecast = (TextView) findViewById(R.id.txt_forecast);
		textViewForecast.setText("");
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... args) {
				MeteoFrance mfForecast = new MeteoFrance();
				String forecastText = "";
				try {
					List<Forecast> forecasts = mfForecast
							.getNearForecast(String.valueOf(
									textViewSearch.getText()).trim());
					if (forecasts.size() == 0) {
						forecastText = "Uhm, where is that?";
					} else {
						for (Forecast forecast : forecasts) {
							forecastText += forecast.time + ": "
									+ forecast.forecast + "\n";
						}
					}
				} catch (IOException e) {
					Log.v(TAG, e.getMessage(), e);
				}
				return forecastText;
			}

			@Override
			protected void onPostExecute(String forecastText) {
				textViewForecast.setText(forecastText);
			};
		}.execute();
	}
}
