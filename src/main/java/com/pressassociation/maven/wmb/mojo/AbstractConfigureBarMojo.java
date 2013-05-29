package com.pressassociation.maven.wmb.mojo;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.pressassociation.maven.wmb.Types;
import com.pressassociation.maven.wmb.configurator.BarConfigurator;
import com.pressassociation.maven.wmb.types.BrokerArtifact;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.pressassociation.maven.wmb.Types.*;
import static com.pressassociation.maven.wmb.utils.MojoUtils.propagateMojoExecutionException;
import static com.pressassociation.maven.wmb.utils.TypeSafetyHelper.typeSafeSet;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@MojoRequiresDependencyResolution("runtime")
public abstract class AbstractConfigureBarMojo extends AbstractMojo {

    @MojoParameter(expression = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @MojoComponent(role = "com.pressassociation.maven.wmb.configurator.BarConfigurator", roleHint = "default")
    protected BarConfigurator barConfigurator;

    @MojoComponent
    protected MavenProjectHelper projectHelper;

    @MojoParameter(readonly = true)
    private File propertiesFile;

    @MojoParameter
    private Properties properties;

    public Properties getProperties() throws MojoExecutionException {
        if (propertiesFile != null) {
            properties = new Properties();
            try {
                properties.load(new FileInputStream(propertiesFile));
            } catch (IOException e) {
                throw propagateMojoExecutionException(e);
            }
        }

        if (properties == null) {
            properties = new Properties();
            getLog().warn("No properties or propertiesFile found in configuration, no changes will be made to the broker archives.");
        }
        return properties;
    }

}