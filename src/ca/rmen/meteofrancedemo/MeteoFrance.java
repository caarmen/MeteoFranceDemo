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
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Retrieves data from the meteo france web server concerning rain forecasts for
 * cities in France.
 */
public class MeteoFrance {
	private static final String UTF8 = "UTF-8"; // Check if this constant is
												// defined somewhere standard
	private static final String HEADER_LOCATION = "location"; // Check if this
																// constant is
																// defined
																// somewhere
																// standard

	private static final String URL_BASE = "http://france.meteofrance.com/";

	// The first query we execute to lookup a city code for a postal code.
	private static final String URL_SEARCH = URL_BASE
			+ "france/accueil/resultat?RECHERCHE_RESULTAT_PORTLET.path=rechercheresultat&query=%s&type=PREV_FRANCE&satellite=france";

	// The server replies to the first query with a couple of redirects, the
	// last one containing the following string.
	private static final String URL_CITY_FORECAST = URL_BASE
			+ "france/meteo?PREVISIONS_PORTLET.path=previsionsville/";

	// The second query we execute to get the rain forecast in the near future,
	// for a given city id.
	private static final String URL_RAIN_FORECAST = URL_BASE
			+ "france/meteo?PREVISIONS_PORTLET.path=previsionspluie/%s";

	private final HttpContext mLocalContext;

	public MeteoFrance() {

		// Let the CookieStore store our cookies.
		CookieStore cookieStore = new BasicCookieStore();
		mLocalContext = new BasicHttpContext();
		mLocalContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	/**
	 * @param postalCode
	 *            a postal code of a city in France.
	 * @return the meteo france city id for this location.
	 * @throws IOException
	 */
	public String getCityId(String postalCode) throws IOException {

		// Send our first get request with the postal code.
		String url = String.format(URL_SEARCH,
				URLEncoder.encode(postalCode, UTF8));
		URI uri = URI.create(url);
		HttpGet httpGet = new HttpGet(uri.toString());
		HttpResponse response = mHttpClient.execute(httpGet, mLocalContext);

		// We expect the response to be a redirect response. Normally
		// the HttpClient would have continued, and would have queried the
		// new url, to give us the content of the final page. However, we
		// have explicitly stopped the redirection with our RedirectHandler,
		// so we can parse the URL here and extract the city id.
		Header locationHeader = response.getFirstHeader(HEADER_LOCATION);
		if (locationHeader == null || locationHeader.getValue() == null)
			return null;

		String location = locationHeader.getValue();

		// Extract the city id from the redirect URL.
		if (location.startsWith(URL_CITY_FORECAST)) {
			String cityId = location.substring(URL_CITY_FORECAST.length());
			return cityId;
		}
		return null;

	}

	/**
	 * @param postalCode
	 *            a postal code for a city in France
	 * @return the rain forecasts for the near future (within the next hour).
	 *         May return one forecast for one hour, or multiple forecasts for
	 *         smaller intervals (ex: 15 minutes).
	 * @throws IOException
	 */
	public List<Forecast> getNearForecast(String postalCode) throws IOException {
		List<Forecast> result = new ArrayList<Forecast>();
		// Get the city id for this location
		String cityId = getCityId(postalCode);
		if (cityId == null)
			return result;

		// Execute our http get request for the forecast for this location.
		String url = String.format(URL_RAIN_FORECAST, cityId);
		URI uri = URI.create(url);
		HttpGet httpGet = new HttpGet(uri.toString());
		HttpResponse response = mHttpClient.execute(httpGet, mLocalContext);
		HttpEntity entity = response.getEntity();

		// Parse the html document returned by the server.
		Document doc = Jsoup.parse(entity.getContent(), UTF8, URL_BASE);
		// There should only be one table containing the forecasts
		Elements forecastTables = doc.getElementsByClass("tablPluie");
		for (Element forecastTable : forecastTables) {
			Elements rows = forecastTable.getElementsByTag("tr");
			for (Element row : rows) {
				// The first row has <th> instead of <td>.
				// All the cells with forecast data are in <td>
				Elements cells = row.getElementsByTag("td");
				if (cells != null && cells.size() >= 2) {
					Element timeCell = cells.get(0);
					Element forecastCell = cells.get(1);
					Forecast forecast = new Forecast(timeCell.text(),
							forecastCell.text());
					result.add(forecast);
				}
			}
		}
		return result;

	}

	private HttpClient mHttpClient = new DefaultHttpClient() {

		@Override
		protected RedirectHandler createRedirectHandler() {
			return mRedirectHandler;
		}

	};
	private RedirectHandler mRedirectHandler = new DefaultRedirectHandler() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.http.impl.client.DefaultRedirectHandler#isRedirectRequested
		 * (org.apache.http.HttpResponse, org.apache.http.protocol.HttpContext)
		 * We want to stop redirection if the redirection is to a URL containing
		 * the city id. In this case, we want to extract the city id from the
		 * location URL.
		 */
		@Override
		public boolean isRedirectRequested(HttpResponse response,
				HttpContext context) {
			// Check if this is a redirect.
			Header locationHeader = response.getFirstHeader(HEADER_LOCATION);
			if (locationHeader != null) {
				String location = locationHeader.getValue();
				// If this is a redirect to a URL which contains the city id,
				// don't actually do the redirect.
				if (location != null && location.contains(URL_CITY_FORECAST))
					return false;
			}
			return super.isRedirectRequested(response, context);
		}

	};

}
