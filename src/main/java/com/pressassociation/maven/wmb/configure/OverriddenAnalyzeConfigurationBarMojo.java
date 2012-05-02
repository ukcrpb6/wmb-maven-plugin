package com.pressassociation.maven.wmb.configure;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;

/**
* @author Bob Browning <bob.browning@pressassociation.com>
*/
@MojoGoal("analyze-overridden")
public class OverriddenAnalyzeConfigurationBarMojo extends AbstractAnalyzeConfigurationBarMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute(Mode.OVERRIDDEN);
    }
}