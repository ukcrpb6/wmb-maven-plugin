package com.pressassociation.maven.wmb.mojo;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import nu.xom.ParsingException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.jfrog.maven.annomojo.annotations.MojoThreadSafe;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.pressassociation.maven.wmb.utils.MojoUtils.propagateMojoExecutionException;
import static com.pressassociation.maven.wmb.utils.TypeSafetyHelper.typeSafeSet;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@MojoRequiresDependencyResolution("runtime")
@MojoGoal("configure")
@MojoThreadSafe
public class ConfigureBarMojo extends AbstractConfigureBarMojo {

    @MojoParameter(defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
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
                    Properties props = applyPropertiesForClassifier(getProperties(), artifact.getClassifier());
                    barConfigurator.configure(artifact, targetFile, props);
                    projectHelper.attachArtifact(project, artifact.getType(), artifact.getClassifier(), targetFile);
                } else {
                    throw new MojoExecutionException("Failed to create target file.");
                }
            } catch (ParsingException e) {
                throw propagateMojoExecutionException(e);
            } catch (IOException e) {
                throw propagateMojoExecutionException(e);
            }
        }
    }

    /**
     * Properties can be stored using the format classifer1\:propertyName=propertyValue this allows properties to be
     * specific for a given classified artifact.
     *
     * @param properties
     * @param classifier
     * @return
     */
    private Properties applyPropertiesForClassifier(Properties properties, final String classifier) {
        Properties newProperties = new Properties();
        newProperties.putAll(properties);
        for (final String name : newProperties.stringPropertyNames()) {
            if (name.contains(":")) {
                final String[] parts = name.split(":", 2);
                if (Iterables.any(Splitter.on(',').trimResults().omitEmptyStrings().split(parts[0]), new Predicate<String>() {
                    @Override public boolean apply(String input) {
                        return input.equalsIgnoreCase(classifier);
                    }
                })) {
                    newProperties.setProperty(parts[1], newProperties.getProperty(name));
                }
            }
        }
        return newProperties;
    }

    private File artifactFileWithClassifier(final Artifact artifact) {
        final String name = project.getArtifact().getArtifactId() + "-" +
                project.getArtifact().getVersion() + "-" +
                artifact.getClassifier() + "." +
                artifact.getArtifactHandler().getExtension();
        return new File(outputDirectory, name);
    }

}