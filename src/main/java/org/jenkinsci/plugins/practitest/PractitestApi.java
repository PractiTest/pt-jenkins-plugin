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

  public Map<String,String> getProjects(){
    Map<String,String> projectIdToName = new HashMap<String,String>();
    byte[] encoding = Base64.encodeBase64(("jenkins@ci.com" + ":" + apiToken).getBytes());

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet request = new HttpGet(baseUrl + "/api/v2/projects.json");
    request.setHeader("Authorization", "Basic " + new String(encoding));
    request.addHeader("Content-Type", "application/json");

    try {
    // Create a response handler
        HttpResponse response = httpclient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        if (statusCode == 200) {
            JSONObject responseObj = new JSONObject(responseBody);
            JSONArray responseData = responseObj.getJSONArray("data");
            for (int i = 0; i < responseData.length(); i++){
                String id = responseData.getJSONObject(i).getString("id");
                String name = responseData.getJSONObject(i).getJSONObject("attributes").getString("name");
                projectIdToName.put(id,name);
            }
        } else {
            System.out.println("ERROR: " + statusCode + ": " + responseBody);
        }
    } catch (Throwable e) {
        e.printStackTrace();
    }
    httpclient.getConnectionManager().shutdown();
    return projectIdToName;
  }


}
