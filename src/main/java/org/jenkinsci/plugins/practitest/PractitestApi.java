package org.jenkinsci.plugins.practitest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.apache.commons.codec.binary.Base64;

import org.json.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.jenkinsci.plugins.practitest.Instance;
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

  private String authorizationHeader(){
    byte[] headerBytes = Base64.encodeBase64(("jenkins@ci.com" + ":" + apiToken).getBytes());
    return "Basic " + new String(headerBytes);
  }

  private HttpGet initRequest(String path){
    HttpGet request = new HttpGet(baseUrl + path);
    request.setHeader("Authorization", authorizationHeader());
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

  public void createRun(String instanceUrl, String exitCode, String buildUrl){
    Instance instance = parseInstanceFromUrl(instanceUrl);
    createRun(instance, exitCode, buildUrl);
  }

  private Instance parseInstanceFromUrl(String url){
    if(url.startsWith("https://") || url.startsWith("http://")){
      url = url.split(Pattern.quote("//"))[1];
    }
    String[] parts = url.split(Pattern.quote("/"));
    String projectId = parts[2];
    String setId = parts[4];
    String instanceId = parts[6];
    return new Instance(instanceId, setId, projectId);
  }

  public void createRun(Instance instance, String exitCode, String buildUrl){
    String projectId = instance.projectId;
    String instanceId = instance.id;

    HttpClient httpclient = new DefaultHttpClient();

    String postData = "{" +
      "\"data\" : {\"attributes\" : {" +
        "\"instance-id\": " + instanceId + "," +
        "\"exit-code\": " + exitCode + "," +
        "\"automated-execution-output\":" + "\"" +  buildUrl + "\"" +
      "}}}";


    try {
        HttpPost request = new HttpPost(baseUrl + "/api/v2/projects/" + projectId + "/runs.json");
        request.setEntity(new StringEntity(postData));
        request.setHeader("Authorization", authorizationHeader());
        request.addHeader("content-type", "application/json");
    // Create a response handler
        HttpResponse response = httpclient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        if (statusCode == 200) {
            System.out.println("SUCCESS: " + responseBody);
        } else {
            System.out.println("ERROR: " + statusCode + ": " + responseBody);
        }
    } catch (Throwable e) {
        e.printStackTrace();
    }
    httpclient.getConnectionManager().shutdown();
  }


}
