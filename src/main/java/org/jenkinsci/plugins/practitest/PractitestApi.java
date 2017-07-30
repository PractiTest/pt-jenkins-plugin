package org.jenkinsci.plugins.practitest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.commons.codec.binary.Base64;

import org.json.*;

import java.util.HashMap;
import java.util.Map;

/**
  Handles all API requests to PractiTest
**/
public class PractitestApi {

  private String baseUrl;
  private String apiToken;

  public PractitestApi(String baseUrl, String apiToken){
    this.baseUrl = baseUrl;
    this.apiToken = apiToken;
  }

  private HttpGet initRequest(String path){
    byte[] encoding = Base64.encodeBase64(("jenkins@ci.com" + ":" + apiToken).getBytes());
    HttpGet request = new HttpGet(baseUrl + path);
    request.setHeader("Authorization", "Basic " + new String(encoding));
    request.addHeader("Content-Type", "application/json");
    return request;
  }

  private Map<String,String> extractIdAndName(String responseBody){
    Map<String,String> mapIdToName = new HashMap<String,String>();
    JSONObject responseObj = new JSONObject(responseBody);
    JSONArray responseData = responseObj.getJSONArray("data");
    for (int i = 0; i < responseData.length(); i++){
        String id = responseData.getJSONObject(i).getString("id");
        String name = responseData.getJSONObject(i).getJSONObject("attributes").getString("name");
        mapIdToName.put(id,name);
    }
    return mapIdToName;
  }

  private Map<String,String> getIdAndName(String pathQuery){
    HttpClient httpclient = new DefaultHttpClient();
    HttpGet request = initRequest(pathQuery);
    Map<String,String> idToName = new HashMap<String,String>();
    try {
    // Create a response handler
        HttpResponse response = httpclient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        if (statusCode == 200) {
             idToName = extractIdAndName(responseBody);
        } else {
            System.out.println("ERROR: " + statusCode + ": " + responseBody);
        }
    } catch (Throwable e) {
        e.printStackTrace();
    }
    httpclient.getConnectionManager().shutdown();
    return idToName;
  }

  public Map<String,String> getProjects(){
    return getIdAndName("/api/v2/projects.json");
  }

  public Map<String, String> getTestSets(String projectId){
    return getIdAndName("/api/v2/projects/" + projectId + "/sets.json");
  }


}
