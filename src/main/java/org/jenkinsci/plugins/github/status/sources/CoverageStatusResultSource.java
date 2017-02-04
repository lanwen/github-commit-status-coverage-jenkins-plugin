package org.jenkinsci.plugins.github.status.sources;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.jacoco.JacocoBuildAction;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github.extension.status.GitHubStatusResultSource;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class CoverageStatusResultSource extends GitHubStatusResultSource {

    public static final String LOG_PREFIX = "[GitHub Coverage Status Setter]";

    private String baseJob;

    @DataBoundConstructor
    public CoverageStatusResultSource() {
    }

    public String getBaseJob() {
        return baseJob;
    }

    @DataBoundSetter
    public void setBaseJob(String baseJob) {
        this.baseJob = baseJob;
    }

    @Override
    public StatusResult get(@Nonnull Run<?, ?> run, @Nonnull TaskListener taskListener) throws IOException {
        Job base = Jenkins.getInstance().getItemByFullName(baseJob, Job.class);
        requireNonNull(base, () -> String.format("Job with name [ %s ] not found", baseJob));

        JacocoBuildAction current = run.getAction(JacocoBuildAction.class);

        if (isNull(current)) {
            taskListener.error("%s Current coverage unknown", LOG_PREFIX);
            return new StatusResult(GHCommitState.ERROR, "Current coverage unknown");
        }

        float currentLineCoverage = current.getLineCoverage().getPercentageFloat();

        JacocoBuildAction baseCoverage = ofNullable(base.getLastSuccessfulBuild())
                .map(last -> last.getAction(JacocoBuildAction.class))
                .orElse(null);

        if (isNull(baseCoverage)) {
            String coverage = String.format("%.2f%% (base unknown)", currentLineCoverage);
            taskListener.error("%s Base coverage unknown, use only current - %s", LOG_PREFIX, coverage);
            return new StatusResult(GHCommitState.ERROR, coverage);
        }

        float baseLineCoverage = baseCoverage.getLineCoverage().getPercentageFloat();

        GHCommitState state = from(baseLineCoverage, currentLineCoverage);
        String message = String.format(
                "%.2f%% (%+.2f%%)",
                currentLineCoverage,
                currentLineCoverage - baseLineCoverage
        );
        taskListener.getLogger().printf("%s Reporting: %s - %s%n", LOG_PREFIX, state, message);
        return new StatusResult(state, message);
    }

    private GHCommitState from(float baseLineCoverage, float currentLineCoverage) {
        if (currentLineCoverage >= baseLineCoverage) {
            return GHCommitState.SUCCESS;
        }

        return GHCommitState.FAILURE;
    }


    @Extension(optional = true)
    public static class CoverageStatusResultSourceDescriptor extends Descriptor<GitHubStatusResultSource> {
        @Override
        public String getDisplayName() {
            return "Coverage status";
        }
    }
}
