package org.jenkinsci.plugins.github.status.sources;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.jacoco.JacocoBuildAction;
import org.jenkinsci.plugins.github.extension.status.GitHubStatusResultSource;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class JacocoCoverageStatusResultSource extends GitHubStatusResultSource {

    public static final String LOG_PREFIX = "[GitHub Jacoco coverage Status Setter]";

    private String baseJob;

    @DataBoundConstructor
    public JacocoCoverageStatusResultSource() {
    }

    public String getBaseJob() {
        return baseJob;
    }

    @DataBoundSetter
    public void setBaseJob(String baseJob) {
        this.baseJob = baseJob;
    }


    @Override
    public StatusResult get(@Nonnull Run<?, ?> run, @Nonnull TaskListener taskListener) {
        return new CoverageStatusResultSource<JacocoBuildAction>(baseJob, LOG_PREFIX) {
            @Override
            protected JacocoBuildAction coverageActionFrom(Run<?, ?> run) {
                return run.getAction(JacocoBuildAction.class);
            }

            @Override
            protected float lineCoverage(JacocoBuildAction action) {
                return action.getLineCoverage().getPercentageFloat();
            }
        }.get(run, taskListener);
    }


    @Extension(optional = true)
    public static class JacocoCoverageStatusResultSourceDescriptor extends Descriptor<GitHubStatusResultSource> {
        @Override
        public String getDisplayName() {
            return "Jacoco coverage status";
        }
    }
}
