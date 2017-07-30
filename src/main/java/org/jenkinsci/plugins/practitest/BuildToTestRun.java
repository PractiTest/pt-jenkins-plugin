package org.jenkinsci.plugins.practitest;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link BuildToTestRun} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like )
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 */
public class BuildToTestRun extends Builder implements SimpleBuildStep {

    private final String projectId;
    private final String setId;
    private final String instanceId;

    @DataBoundConstructor
    public BuildToTestRun(String projectId, String setId, String instanceId) {
      this.projectId = projectId;
      this.setId = setId;
      this.instanceId = instanceId;
    }

    public String getProjectId() {
        return "Loading Values...";
    }

    public String getSetId() {
        return "Select Project...";
    }

    public String getInstanceId() {
        return "Select Instance...";
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        PractitestApi ptClient = new PractitestApi(getDescriptor().getBaseUrl(),
          getDescriptor().getApiToken());
        String exitCode = Result.SUCCESS.equals(build.getResult()) ? "0" : "1";
        ptClient.createRun(projectId, instanceId, exitCode);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String apiToken;
        private String baseUrl = "https://api.practitest.com";

        public DescriptorImpl() {
            load();
        }

        private static ListBoxModel convertToListBoxModel(Map<String,String> data){
          ListBoxModel listBox = new ListBoxModel();
          for (String id : data.keySet()) {
            listBox.add(data.get(id), id);
          }
          return listBox;
        }

        public ListBoxModel doFillProjectIdItems(){
          PractitestApi ptClient = new PractitestApi(baseUrl, apiToken);
          Map<String,String> projects = ptClient.getProjects();
          return convertToListBoxModel(projects);
        }

        public ListBoxModel doFillSetIdItems(@QueryParameter String projectId){
          PractitestApi ptClient = new PractitestApi(baseUrl, apiToken);
          Map<String,String> testSets = ptClient.getTestSets(projectId);
          return convertToListBoxModel(testSets);
        }

        public ListBoxModel doFillInstanceIdItems(@QueryParameter String projectId, @QueryParameter String setId){
          PractitestApi ptClient = new PractitestApi(baseUrl, apiToken);
          Map<String,String> instances = ptClient.getInstances(projectId, setId);
          return convertToListBoxModel(instances);
        }

        public FormValidation doCheckBaseUrl(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a URL");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "PractiTest";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            apiToken = formData.getString("apiToken");
            baseUrl = formData.getString("baseUrl");
            save();
            return super.configure(req,formData);
        }

        public String getApiToken() {
          return apiToken;
        }

        public String getBaseUrl(){
          return baseUrl;
        }
    }
}
