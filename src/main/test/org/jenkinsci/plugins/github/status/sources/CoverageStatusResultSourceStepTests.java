package org.jenkinsci.plugins.github.status.sources;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
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
public class CoverageStatusResultSourceStepTests {

    @Rule
    public JenkinsRule jRule = new JenkinsRule();

    @Test
    public void shouldPerform() throws Exception {
        FreeStyleProject base = jRule.createFreeStyleProject("1");

        FreeStyleBuild baseBuild = base.scheduleBuild2(0).get();
        baseBuild.addAction(
                new JacocoBuildAction(
                        getRatios(5, 15),
                        new JacocoHealthReportThresholds(),
                        listener(),
                        new String[]{},
                        new String[]{}
                )
        );


        FreeStyleProject project = jRule.createFreeStyleProject();

        FreeStyleBuild run = project.scheduleBuild2(0).get();
        run.addAction(new JacocoBuildAction(
                getRatios(5, 20),
                new JacocoHealthReportThresholds(),
                listener(),
                new String[]{},
                new String[]{}
        ));


        CoverageStatusResultSource cov = new CoverageStatusResultSource();
        cov.setBaseJob("1");

        GitHubStatusResultSource.StatusResult result = cov.get(run, listener());
        assertThat(result.getState(), is(GHCommitState.SUCCESS));
        assertThat(result.getMsg(), containsString("80.00% (+5.00%)"));
    }

    private static HashMap<CoverageElement.Type, Coverage> getRatios(int missed, int covered) {
        HashMap<CoverageElement.Type, Coverage> map = new HashMap<>();
        map.put(CoverageElement.Type.LINE, new Coverage(missed, covered));
        return map;
    }

    private LogTaskListener listener() {
        return new LogTaskListener(Logger.getLogger(
                CoverageStatusResultSourceStepTests.class.getCanonicalName()), Level.INFO
        );
    }
}
