/*
 * ----------------------------------------------------------------------------
 * "THE WINE-WARE LICENSE" Version 1.0:
 * Authors: Carmen Alvarez. 
 * As long as you retain this notice you can do whatever you want with this stuff. 
 * If we meet some day, and you think this stuff is worth it, you can buy me a 
 * glass of wine in return. 
 * 
 * THE AUTHORS OF THIS FILE ARE NOT RESPONSIBLE FOR LOSS OF LIFE, LIMBS, SELF-ESTEEM,
 * MONEY, RELATIONSHIPS, OR GENERAL MENTAL OR PHYSICAL HEALTH CAUSED BY THE
 * CONTENTS OF THIS FILE OR ANYTHING ELSE.
 * ----------------------------------------------------------------------------
 */
package ca.rmen.meteofrancedemo;

/**
 * Forecast for a given time interval.
 */
public class Forecast {
	// String describing the time interval. Example: 22h30 à 22h45
	public final String time;
	// String describing the forecast. Example: Pas de pluie
	public final String forecast;

	public Forecast(String time, String forecast) {
		this.time = time;
		this.forecast = forecast;
	}

	@Override
	public String toString() {
		return time + ": " + forecast;
	}
}