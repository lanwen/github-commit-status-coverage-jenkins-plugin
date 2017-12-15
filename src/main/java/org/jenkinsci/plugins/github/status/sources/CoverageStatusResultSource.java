package org.jenkinsci.plugins.github.status.sources;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github.extension.status.GitHubStatusResultSource;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * @author lanwen (Merkushev Kirill)
 */
public abstract class CoverageStatusResultSource<T extends Action> extends GitHubStatusResultSource {

    private String baseJob;
    private String logPrefix;

    public CoverageStatusResultSource(String baseJob, String logPrefix) {
        this.baseJob = baseJob;
        this.logPrefix = logPrefix;
    }

    protected abstract T coverageActionFrom(Run<?, ?> run);

    protected abstract float lineCoverage(T action);

    @Override
    public StatusResult get(@Nonnull Run<?, ?> run, @Nonnull TaskListener taskListener) {
        Job base = Jenkins.getInstance().getItemByFullName(baseJob, Job.class);
        requireNonNull(base, () -> String.format("Job with name [ %s ] not found", baseJob));

        T current = coverageActionFrom(run);

        if (isNull(current)) {
            taskListener.error("%s Current coverage unknown", logPrefix);
            return new StatusResult(GHCommitState.ERROR, "Current coverage unknown");
        }

        float currentLineCoverage = lineCoverage(current);

        T baseCoverage = ofNullable(base.getLastSuccessfulBuild())
                .map(this::coverageActionFrom)
                .orElse(null);

        if (isNull(baseCoverage)) {
            String coverage = String.format("%.2f%% (base unknown)", currentLineCoverage);
            taskListener.error("%s Base coverage unknown, use only current - %s", logPrefix, coverage);
            return new StatusResult(GHCommitState.ERROR, coverage);
        }

        float baseLineCoverage = lineCoverage(baseCoverage);

        GHCommitState state = from(baseLineCoverage, currentLineCoverage);
        String message = String.format(
                "%.2f%% (%+.2f%%)",
                currentLineCoverage,
                currentLineCoverage - baseLineCoverage
        );
        taskListener.getLogger().printf("%s Reporting: %s - %s%n", logPrefix, state, message);
        return new StatusResult(state, message);
    }

    private GHCommitState from(float baseLineCoverage, float currentLineCoverage) {
        if (currentLineCoverage >= baseLineCoverage) {
            return GHCommitState.SUCCESS;
        }

        return GHCommitState.FAILURE;
    }
}
