package com.searchengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class APICallHandler {
	public static JSONArray bingCall(String searchText) throws IOException, JSONException {
		final String accountKey = "cdf3f7315d9f47ec9124343dcc8f3a14";
		final String bingUrlPattern = "https://api.cognitive.microsoft.com/bing/v5.0/search?q=%s&count=15&offset=0&mkt=en-us&safesearch=Moderate";
		
		final String query = URLEncoder.encode(searchText, Charset.defaultCharset().name());
		final String bingUrl = String.format(bingUrlPattern, query);
		
		//final String accountKeyEnc = Base64.getEncoder().encodeToString((accountKey + ":" + accountKey).getBytes());
		
		final URL url = new URL(bingUrl);
		final URLConnection connection = url.openConnection();
		connection.setRequestProperty("Ocp-Apim-Subscription-Key", accountKey);
		
		try(final BufferedReader in = new BufferedReader(
				new InputStreamReader(connection.getInputStream()))) {
			String inputLine;
			final StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			final JSONObject json = new JSONObject(response.toString());
			final JSONObject d = json.getJSONObject("webPages");
			final JSONArray results = d.getJSONArray("value");
			return results;

		}
	}

	public static JSONArray googleCall(String searchText) throws IOException,
		JSONException {
		String key = "AIzaSyBpOSJGpCpalItURCXdbl-CZwiAFPcyRMo"; //
		String cref = "000919623271246924971:skhhkxaozmy"; //
		URL url = new URL("https://www.googleapis.com/customsearch/v1?key="
				+ key + "&cx=" + cref + "&q="
				+ URLEncoder.encode(searchText, "UTF-8") + "&alt=json");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/json");
		BufferedReader br = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		final StringBuilder response = new StringBuilder();
		JSONArray items = null;
		while ((line = br.readLine()) != null) {
			response.append(line);
		}
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			items = obj.getJSONArray("items");
		
		}
		
		return items;
	}
	
	public static JSONArray solrCall(String searchText, int maxRows) throws IOException,
		JSONException {
	
		final String solrQuery = "http://127.0.0.1:8983/solr/collection1/select?q="
				+ URLEncoder.encode(searchText, "UTF-8")
				+ "&rows="+maxRows+"&wt=json&indent=true";
		
		System.out.println(solrQuery);
		final URL url = new URL(solrQuery);
		final URLConnection connection = url.openConnection();
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		JSONObject items = null;
		JSONArray dataArray = null;
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			items = obj.getJSONObject("response");
			dataArray = items.getJSONArray("docs");
		}
		
		return dataArray;
	}
	public static JSONArray hitsCall(JSONArray result) throws IOException,
	JSONException {
		String searchText="";
		Map<String, JSONObject> jsonMap=new HashMap<String,JSONObject>();
		for (int i = 0; i < 15; i++) {
		    JSONObject jsonobject = result.getJSONObject(i);
		    String url = jsonobject.getString("url");
		    searchText += url + ",";
		    jsonMap.put(url, jsonobject);
		}
		System.out.println(result.length());
		searchText = searchText.replaceAll(",$", "");
		final String hitsQuery = "http://127.0.0.1:5000/getHITS/"+searchText;
				//+ URLEncoder.encode(searchText, "UTF-8");
		
		System.out.println(hitsQuery);
		final URL url = new URL(hitsQuery);
		final URLConnection connection = url.openConnection();
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		JSONArray items = null;
		JSONArray data = new JSONArray();
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			items = obj.getJSONArray("a_score");
			for(int j = 0;j<items.length();j++){
				data.put(jsonMap.get(items.optString(j)));
			}
		}
		//System.out.println(data.toString());
		return data;
	}
	
	public static JSONObject queryExpCall(String query) throws IOException,
	JSONException {
		JSONArray result = APICallHandler.solrCall(query, 15);
		JSONObject solrResults = new JSONObject();
		solrResults.put("query", query);
		solrResults.put("results", result);
		final URL url = new URL("http://127.0.0.1:5000/getQueryExp/");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setUseCaches(false);
		httpCon.setRequestProperty( "Content-Type", "application/json" );
		httpCon.setRequestProperty("Accept", "application/json");
		httpCon.setRequestMethod("POST");
		OutputStream os = httpCon.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(solrResults.toString());
		osw.flush();
		osw.close();    
		os.close();  
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				httpCon.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		String newQuery = null;
		
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			newQuery = obj.optString("newQuery");
			
		}
		JSONArray res = APICallHandler.solrCall(newQuery, 15);
		JSONObject finRes = new JSONObject();
		finRes.put("newQuery", newQuery);
		finRes.put("result", res);
		return finRes;
	}
	
	public static JSONArray clusterCall(String query) throws IOException,
	JSONException {
		JSONArray result = APICallHandler.solrCall(query, 100);
		JSONObject solrResults = new JSONObject();
		solrResults.put("results", result);
		final URL url = new URL("http://127.0.0.1:5001/getClusteringResults/");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setUseCaches(false);
		httpCon.setRequestProperty( "Content-Type", "application/json" );
		httpCon.setRequestProperty("Accept", "application/json");
		httpCon.setRequestMethod("POST");
		OutputStream os = httpCon.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(solrResults.toString());
		osw.flush();
		osw.close();    
		os.close();  
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				httpCon.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		JSONArray newObj = null;
		
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			newObj = obj.getJSONArray("results");
			
		}		
		return newObj;
	}
	public static JSONArray clusterCall2(String query) throws IOException,
	JSONException {
		JSONArray result = APICallHandler.solrCall(query, 100);
		JSONObject solrResults = new JSONObject();
		solrResults.put("results", result);
		final URL url = new URL("http://127.0.0.1:5002/getAgglomerativeClusteringResults/");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setUseCaches(false);
		httpCon.setRequestProperty( "Content-Type", "application/json" );
		httpCon.setRequestProperty("Accept", "application/json");
		httpCon.setRequestMethod("POST");
		OutputStream os = httpCon.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(solrResults.toString());
		osw.flush();
		osw.close();    
		os.close();  
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				httpCon.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		JSONArray newObj = null;
		
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			newObj = obj.getJSONArray("results");
			
		}		
		return newObj;
	}
	public static JSONArray clusterCall3(String query) throws IOException,
	JSONException {
		JSONArray result = APICallHandler.solrCall(query, 100);
		JSONObject solrResults = new JSONObject();
		solrResults.put("results", result);
		final URL url = new URL("http://127.0.0.1:5003/getAgglomerativeClusteringResults/");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setUseCaches(false);
		httpCon.setRequestProperty( "Content-Type", "application/json" );
		httpCon.setRequestProperty("Accept", "application/json");
		httpCon.setRequestMethod("POST");
		OutputStream os = httpCon.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(solrResults.toString());
		osw.flush();
		osw.close();    
		os.close();  
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				httpCon.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		JSONArray newObj = null;
		
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			newObj = obj.getJSONArray("results");
			
		}		
		return newObj;
	}
	public static JSONArray clusterCall4(String query) throws IOException,
	JSONException {
		JSONArray result = APICallHandler.solrCall(query, 100);
		JSONObject solrResults = new JSONObject();
		solrResults.put("results", result);
		final URL url = new URL("http://127.0.0.1:5004/getAgglomerativeClusteringResults/");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setUseCaches(false);
		httpCon.setRequestProperty( "Content-Type", "application/json" );
		httpCon.setRequestProperty("Accept", "application/json");
		httpCon.setRequestMethod("POST");
		OutputStream os = httpCon.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(solrResults.toString());
		osw.flush();
		osw.close();    
		os.close();  
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				httpCon.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		JSONArray newObj = null;
		
		if (response != null) {
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			newObj = obj.getJSONArray("results");
			
		}		
		return newObj;
	}
}
