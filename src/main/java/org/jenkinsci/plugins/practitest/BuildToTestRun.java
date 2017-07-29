package org.jenkinsci.plugins.practitest;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
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

    @DataBoundConstructor
    public BuildToTestRun(String projectId) {
        this.projectId = projectId;
    }


    public String getProjectId() {
        return projectId;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        // This also shows how you can consult the global configuration of the builder
        listener.getLogger().println("~~~~ " + projectId);
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
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

        public ListBoxModel doFillProjectIdItems(){
          load();

          PractitestApi ptClient = new PractitestApi(baseUrl, apiToken);
          Map<String,String> projects = ptClient.getProjects();
          ListBoxModel listBox = new ListBoxModel();
          for (String projectId : projects.keySet()) {
            listBox.add(projects.get(projectId), projectId);
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
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        public String getDisplayName() {
            return "PractiTest";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            apiToken = formData.getString("apiToken");
            baseUrl = formData.getString("baseUrl");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
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
