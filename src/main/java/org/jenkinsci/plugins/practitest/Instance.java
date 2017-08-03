package org.jenkinsci.plugins.practitest;

public class Instance {
  public String id;
  public String projectId;
  public String setId;

  public Instance(String id, String setId, String projectId){
    this.id = id;
    this.projectId = projectId;
    this.setId = setId;
  }
}
