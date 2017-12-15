package org.jenkinsci.plugins.github.status.sources;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.Ratio;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.plugins.jacoco.JacocoBuildAction;
import hudson.plugins.jacoco.JacocoHealthReportThresholds;
import hudson.plugins.jacoco.model.Coverage;
import hudson.plugins.jacoco.model.CoverageElement;
import hudson.util.LogTaskListener;
import org.jenkinsci.plugins.github.extension.status.GitHubStatusResultSource;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.github.GHCommitState;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class CoberturaCoverageStatusResultSourceStepTest {

    @Rule
    public JenkinsRule jRule = new JenkinsRule();

    @Test
    public void shouldPerform() throws Exception {
        FreeStyleProject base = jRule.createFreeStyleProject("1");

        FreeStyleBuild baseBuild = base.scheduleBuild2(0).get();
        baseBuild.addAction(
                CoberturaBuildAction.load(
                        coverage(75, 100),
                        new CoverageTarget(),
                        new CoverageTarget(),
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        10
                )
        );


        FreeStyleProject project = jRule.createFreeStyleProject();

        FreeStyleBuild run = project.scheduleBuild2(0).get();
        run.addAction(CoberturaBuildAction.load(
                coverage(80, 100),
                new CoverageTarget(),
                new CoverageTarget(),
                false,
                false,
                false,
                false,
                false,
                false,
                10
        ));


        CoberturaCoverageStatusResultSource cov = new CoberturaCoverageStatusResultSource();
        cov.setBaseJob("1");

        GitHubStatusResultSource.StatusResult result = cov.get(run, listener());
        assertThat(result.getState(), is(GHCommitState.SUCCESS));
        assertThat(result.getMsg(), containsString("80.00% (+5.00%)"));
    }

    private static CoverageResult coverage(int missed, int covered) {
        CoverageResult project = new CoverageResult(hudson.plugins.cobertura.targets.CoverageElement.PROJECT, null, "project");
        project.updateMetric(CoverageMetric.LINE, Ratio.create(missed, covered));
        return project;
    }

    private LogTaskListener listener() {
        return new LogTaskListener(Logger.getLogger(
                CoberturaCoverageStatusResultSourceStepTest.class.getCanonicalName()), Level.INFO
        );
    }
}
