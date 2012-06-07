package com.pressassociation.maven.wmb.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDirectInvocation;

/**
* @author Bob Browning <bob.browning@pressassociation.com>
*/
@MojoGoal("analyze-inherited")
@MojoRequiresDirectInvocation
public class InheritedAnalyzeConfigurationBarMojo extends AbstractAnalyzeConfigurationBarMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute(Mode.INHERITED);
    }
}
