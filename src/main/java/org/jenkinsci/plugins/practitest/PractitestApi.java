package org.jenkinsci.plugins.practitest;

import jenkins.model.Jenkins;

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

  public static final String PLUGIN_VER = "1.0.0";
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
        HttpPost request = new HttpPost(baseUrl + "/api/v2/projects/" + projectId +
              "/runs.json?source=jenkins&jenkins_ver=" + Jenkins.VERSION +
              "&plugin_ver=" + PLUGIN_VER);
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
