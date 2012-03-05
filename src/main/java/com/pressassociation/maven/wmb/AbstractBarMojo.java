package com.pressassociation.maven.wmb;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import java.io.File;

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

    /**
     * Full path to mqsicreatebar command.
     */
    @MojoParameter(defaultValue = "/opt/IBM/WMBT700", expression = "${wmb.toolkitDirectory}")
    protected File toolkitDirectory;
}
