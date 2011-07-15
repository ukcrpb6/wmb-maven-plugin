package com.pressassociation.maven.wmb;

import org.apache.maven.plugin.AbstractMojo;

/**
 * @author Bob Browning
 */
public abstract class AbstractBarMojo extends AbstractMojo {
    /**
     * @parameter alias="bars"
     * @required
     */
    protected BarArtifactSet[] barArtifacts;

    /**
     * Base directory.
     * @parameter expression="${basedir}"
     */
    protected String basedir;

    /**
     * Target directory for build output.
     * @parameter expression="${project.build.directory}"
     */
    protected String targetdir;

}
