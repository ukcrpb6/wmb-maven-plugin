package com.pressassociation.maven.wmb.configure;

import nu.xom.ParsingException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.jfrog.maven.annomojo.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.pressassociation.maven.wmb.configure.TypeSafetyHelper.*;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@MojoRequiresDependencyResolution("runtime")
@MojoGoal("configure")
@MojoThreadSafe
public class ConfigureBarMojo extends AbstractMojo { //implements Contextualizable

    @MojoParameter(expression = "${project}", required = true, readonly = true)
    private MavenProject project;

    @MojoComponent(role = "com.pressassociation.maven.wmb.configure.BarConfigurator", roleHint = "default")
    private BarConfigurator barConfigurator;

    @MojoComponent
    private MavenProjectHelper projectHelper;

    @MojoParameter(defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @MojoParameter(readonly = true)
    private File propertiesFile;

    @MojoParameter
    private Properties properties;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (propertiesFile != null) {
            properties = new Properties();
            try {
                properties.load(new FileInputStream(propertiesFile));
            } catch (IOException e) {
                throw propagate(e);
            }
        }

        if (properties == null) {
            properties = new Properties();
            getLog().warn("No properties or properiesFile found in configuration, no changes will be made to the broker archives.");
        }

        for (Artifact artifact : typeSafeSet(project.getArtifacts(), Artifact.class)) {
            if (!"bar".equals(artifact.getType())) {
                getLog().info("Skipping " + artifact.getType() + " dependency " + artifact.getId() +
                        " when configuring bars.");
                continue;
            }

            try {
                if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                    throw new MojoExecutionException("Output directory does not exist and could not be created.");
                }
                if (!outputDirectory.isDirectory()) {
                    throw new MojoExecutionException("Output directory property should be a directory.");
                }

                File targetFile = artifactFileWithClassifier(artifact);
                if (targetFile.exists() && !targetFile.delete()) {
                    throw new MojoExecutionException("Failed to remove existing target file.");
                }

                if (targetFile.createNewFile()) {
                    barConfigurator.configure(artifact, targetFile, properties);
                    projectHelper.attachArtifact(project, artifact.getType(), artifact.getClassifier(), targetFile);
                } else {
                    throw new MojoExecutionException("Failed to create target file.");
                }
            } catch (ParsingException e) {
                throw propagate(e);
            } catch (IOException e) {
                throw propagate(e);
            }
        }
    }

    private File artifactFileWithClassifier(final Artifact artifact) {
        final String name = project.getArtifact().getArtifactId() + "-" +
                project.getArtifact().getVersion() + "-" +
                artifact.getClassifier() + "." +
                artifact.getArtifactHandler().getExtension();
        return new File(outputDirectory, name);
    }

    private static MojoExecutionException propagate(Throwable throwable) {
        if (throwable instanceof MojoExecutionException) {
            return (MojoExecutionException) throwable;
        }
        return new MojoExecutionException(throwable.getMessage(), throwable);
    }

}