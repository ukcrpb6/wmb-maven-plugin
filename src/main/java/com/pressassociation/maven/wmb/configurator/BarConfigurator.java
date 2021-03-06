package com.pressassociation.maven.wmb.configurator;

import nu.xom.ParsingException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public interface BarConfigurator {
    final String ROLE = BarConfigurator.class.getName();

    /**
     * Perform a configuration operation.
     *
     * @param sourceArtifact     Artifact to configure
     * @param targetArtifactFile Destination filename
     * @param properties         Properties to configure BAR with
     * @throws IOException
     * @throws MojoExecutionException
     */
    Artifact configure(Artifact sourceArtifact, File targetArtifactFile, Properties properties)
            throws IOException, MojoExecutionException, ParsingException;

    /**
     * Extract properties from broker archive into a map.
     *
     * @param artifact Broker artifact to resolve entries from
     * @return Map of configurable properties
     * @throws IOException
     * @throws ParsingException
     */
    Map<String, String> resolveProperties(Artifact artifact) throws IOException, ParsingException;
}
