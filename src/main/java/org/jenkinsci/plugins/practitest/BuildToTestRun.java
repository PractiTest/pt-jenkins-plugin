package org.jenkinsci.plugins.practitest;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.*;
import hudson.tasks.Builder;
import hudson.tasks.Notifier;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
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

public class BuildToTestRun extends Notifier {

    private final String instanceUrl;

    @DataBoundConstructor
    public BuildToTestRun(String instanceUrl) {
      this.instanceUrl = instanceUrl;
    }

    public String getInstanceUrl() {
        return "";
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        PractitestApi ptClient = new PractitestApi(getDescriptor().getBaseUrl(),
          getDescriptor().getApiToken());
        String exitCode = Result.SUCCESS.equals(build.getResult()) ? "0" : "1";
        String buildUrl = build.getAbsoluteUrl();
        ptClient.createRun(instanceUrl, exitCode, buildUrl);
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
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
