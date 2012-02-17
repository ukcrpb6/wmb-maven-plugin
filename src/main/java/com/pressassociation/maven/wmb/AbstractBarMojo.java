package com.pressassociation.maven.wmb;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

/**
 * @author Bob Browning
 */
public abstract class AbstractBarMojo extends AbstractMojo {

    @MojoParameter(required = true, alias = "bars")
    protected BrokerArchive[] brokerArchives;

    /**
     * Base directory.
     */
    @MojoParameter(expression = "${basedir}")
    protected String basedir;

    /**
     * Target directory for build output.
     */
    @MojoParameter(expression = "${project.build.directory}")
    protected String targetdir;

    protected FileSetManager fileSetManager = new FileSetManager();

}
