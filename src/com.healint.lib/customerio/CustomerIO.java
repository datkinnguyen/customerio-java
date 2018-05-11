package com.healint.lib.customerio;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

public class CustomerIO {

	private String siteId;
	private String apiKey;

	public CustomerIO(String siteId, String apiKey) {
		this.siteId = siteId;
		this.apiKey = apiKey;
	}

	public boolean updateUser(String userId, Map<String, Object> attributes) throws Exception {

		String apiPath = CustomerIOURIs.URL_CUSTOMERS + "/" + userId;

		return sendPut(apiPath, attributes);
	}

	public boolean deleteUser(String userId) throws Exception {

		String apiPath = CustomerIOURIs.URL_CUSTOMERS + "/" + userId;

		return sendDelete(apiPath);
	}

	public boolean sendUserEvent(String userId, String eventName) throws Exception {

		String apiPath = CustomerIOURIs.URL_CUSTOMERS + "/" + userId + "/"
				+ CustomerIOURIs.URL_EVENTS;

		Map<String, Object> mapContent = new HashMap<>();
		mapContent.put(CustomerIOConstants.PARAM_ATTRIBUTE_EVENT_NAME, eventName);
		return sendPost(apiPath, mapContent);
	}

	public boolean updateUserTimezone(String userId, int timezoneOffset) throws Exception {

		// filter if the timezone is in the supported list by customer.io
		Optional<String> firstTimezone = firstTimezoneOffsetSupported(timezoneOffset);
		if (firstTimezone.isPresent()) {
			String apiPath = CustomerIOURIs.URL_CUSTOMERS + "/" + userId;

			// construct body
			Map<String, Object> params = new HashMap<String, Object>() {
				{
					put(CustomerIOConstants.PARAM_ATTRIBUTE_TIMEZONE, firstTimezone.get());
					// customer.io deals with timestamps in seconds
					put(CustomerIOConstants.PARAM_ATTRIBUTE_TIMEZONE_LAST_MODIFIED_TIME,
							timezoneOffset / 1000);
				}
			};

			return sendPut(apiPath, params);

		} else {
			return false;
		}

	}

	private Optional<String> firstTimezoneOffsetSupported(int timezoneOffset) {
		Set<String> setTimezones =
				new HashSet<>(Arrays.asList(TimeZone.getAvailableIDs(timezoneOffset)));
		return setTimezones.stream().filter(CustomerIOConstants.supportedTimeZones::contains)
				.findFirst();
	}

	// HTTP GET request
	private boolean sendPost(String apiPath, Map<String, Object> attributes) throws Exception {

		URL url = new URL(CustomerIOURIs.HOST + CustomerIOURIs.URL_PREFIX + apiPath);

		HttpURLConnection urlConnection = connectionForUrl(url, "POST");

		urlConnection.connect();

		// For POST only - START
		urlConnection.setDoOutput(true);
		OutputStream os = urlConnection.getOutputStream();
		String jsonBody = (new Gson()).toJson(attributes);
		os.write(jsonBody.getBytes());

		os.flush();
		os.close();
		// For POST only - END

		int responseCode = urlConnection.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in =
					new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			String responseString = response.toString();
			System.out.println(responseString);
			return true;
		} else {
			System.out.println("POST request not worked");
			return false;
		}

	}

	// HTTP GET request
	private boolean sendPut(String apiPath, Map<String, Object> attributes) throws Exception {

		URL url = new URL(CustomerIOURIs.HOST + CustomerIOURIs.URL_PREFIX + apiPath);

		HttpURLConnection urlConnection = connectionForUrl(url, "PUT");

		urlConnection.connect();

		// For POST only - START
		urlConnection.setDoOutput(true);
		OutputStream os = urlConnection.getOutputStream();

		String jsonBody = (new Gson()).toJson(attributes);
		os.write(jsonBody.getBytes());
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = urlConnection.getResponseCode();
		System.out.println("PUT Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in =
					new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			String responseString = response.toString();
			System.out.println(responseString);
			return true;
		} else {
			System.out.println("PUT request not worked");
			return false;
		}

	}

	// HTTP POST request
	private boolean sendGet(String apiPath) throws Exception {

		URL url = new URL(CustomerIOURIs.HOST + CustomerIOURIs.URL_PREFIX + apiPath);

		HttpURLConnection urlConnection = connectionForUrl(url, "GET");

		int responseCode = urlConnection.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in =
					new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			// print result
			String responseString = response.toString();
			System.out.println(responseString);
			return true;
		} else {
			System.out.println("GET request not worked");
			return false;
		}

	}

	// HTTP GET request
	private boolean sendDelete(String apiPath) throws Exception {

		URL url = new URL(CustomerIOURIs.HOST + CustomerIOURIs.URL_PREFIX + apiPath);

		HttpURLConnection urlConnection = connectionForUrl(url, "DELETE");

		urlConnection.connect();

		int responseCode = urlConnection.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in =
					new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			// print result
			String responseString = response.toString();
			System.out.println(responseString);
			return true;
		} else {
			System.out.println("GET request not worked");
			return false;
		}

	}

	private HttpURLConnection connectionForUrl(URL url, String method) throws Exception {

		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

		urlConnection.setRequestMethod(method);
		urlConnection.setRequestProperty(CustomerIOConstants.CONTENT_TYPE_HEADER,
				CustomerIOConstants.JSON_MIME_TYPE);

		String encoded = Base64.getEncoder()
				.encodeToString((siteId + ":" + apiKey).getBytes(StandardCharsets.UTF_8));  //Java 8
		urlConnection.setRequestProperty("Authorization", "Basic " + encoded);

		return urlConnection;
	}

}

