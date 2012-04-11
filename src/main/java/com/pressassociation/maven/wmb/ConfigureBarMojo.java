package com.pressassociation.maven.wmb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;

import java.io.File;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@MojoGoal("configure")
public class ConfigureBarMojo extends AbstractBarMojo {

    private File applyBarOverrideExecutable;

    private File getApplyBarOverrideExecutable() throws MojoExecutionException {
        if (applyBarOverrideExecutable == null) {
            applyBarOverrideExecutable = new File(toolkitDirectory, "mqsiapplybaroverride");
            if (!applyBarOverrideExecutable.exists()) {
                throw new MojoExecutionException(String.format("Invalid toolkit directory (%s), cannot locate mqsicreatebar.", toolkitDirectory));
            }
            if (!applyBarOverrideExecutable.canExecute()) {
                throw new MojoExecutionException("Permission denied, cannot execute mqsicreatebar.");
            }
        }
        return applyBarOverrideExecutable;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }

}
