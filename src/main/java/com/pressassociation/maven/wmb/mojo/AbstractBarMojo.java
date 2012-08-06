package com.pressassociation.maven.wmb.mojo;

import com.pressassociation.maven.wmb.types.BrokerArchive;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

/**
 * @author Bob Browning
 */
public abstract class AbstractBarMojo extends AbstractMojo {

    @MojoParameter(readonly = true, required = true, alias = "bars")
    protected BrokerArchive[] brokerArchives;

    /**
     * Base directory.
     */
    @MojoParameter(expression = "${basedir}", readonly = true, required = true)
    protected String basedir;

    /**
     * Target directory for build output.
     */
    @MojoParameter(expression = "${project.build.directory}/generated-sources", readonly = true, required = true)
    protected String generatedSourcesDir;

    /**
     * Target directory for build output.
     */
    @MojoParameter(expression = "${project.build.directory}", readonly = true, required = true)
    protected String targetdir;

    protected FileSetManager fileSetManager = new FileSetManager();

}
