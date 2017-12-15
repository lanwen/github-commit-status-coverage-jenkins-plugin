package org.jenkinsci.plugins.github.status.sources;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.targets.CoverageMetric;
import org.jenkinsci.plugins.github.extension.status.GitHubStatusResultSource;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class CoberturaCoverageStatusResultSource extends GitHubStatusResultSource {

    public static final String LOG_PREFIX = "[GitHub Cobertura coverage Status Setter]";

    private String baseJob;

    @DataBoundConstructor
    public CoberturaCoverageStatusResultSource() {
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
        return new BaseCoverageStatusResultSource<CoberturaBuildAction>(baseJob, LOG_PREFIX) {
            @Override
            protected CoberturaBuildAction coverageActionFrom(Run<?, ?> run) {
                return run.getAction(CoberturaBuildAction.class);
            }

            @Override
            protected float lineCoverage(CoberturaBuildAction action) {
                return action.getResult().getCoverage(CoverageMetric.LINE).getPercentageFloat();
            }
        }.get(run, taskListener);
    }


    @Extension(optional = true)
    public static class CoberturaCoverageStatusResultSourceDescriptor extends Descriptor<GitHubStatusResultSource> {
        @Override
        public String getDisplayName() {
            return "Cobertura coverage status";
        }
    }
}
