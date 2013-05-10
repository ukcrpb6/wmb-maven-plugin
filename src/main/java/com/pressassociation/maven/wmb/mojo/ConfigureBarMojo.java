package com.pressassociation.maven.wmb.mojo;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.pressassociation.maven.wmb.Types;
import com.pressassociation.maven.wmb.types.ArtifactItem;
import com.pressassociation.maven.wmb.types.ConfiguredArtifact;
import com.pressassociation.maven.wmb.utils.TypeSafetyHelper;
import nu.xom.ParsingException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.pressassociation.maven.wmb.utils.MojoUtils.propagateMojoExecutionException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@MojoRequiresDependencyResolution("runtime")
@MojoGoal("configure")
@MojoThreadSafe
public class ConfigureBarMojo extends AbstractConfigureBarMojo {

    /**
     * Artifact factory component.
     */
    @MojoComponent
    private ArtifactFactory artifactFactory;

    /**
     * Artifact resolver component.
     */
    @MojoComponent
    private ArtifactResolver resolver;

    /**
     * List of remote repositories.
     */
    @MojoParameter(expression = "${project.remoteArtifactRepositories}", required = true, readonly = true)
    private List<ArtifactRepository> remoteRepositories;

    /**
     * Local repository.
     */
    @MojoParameter(expression = "${localRepository}", required = true, readonly = true)
    private ArtifactRepository localRepository;


    @MojoParameter(defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @MojoParameter
    private Set<ConfiguredArtifact> configuredArtifacts = ImmutableSet.of();

    private Artifact createAndResolveArtifact(ArtifactItem model) throws MojoExecutionException {
        Artifact artifact = artifactFactory.createArtifactWithClassifier(
                model.getGroupId(), model.getArtifactId(), model.getVersion(), model.getType(), model.getClassifier());
        try {
            resolver.resolve(artifact, remoteRepositories, localRepository);
        } catch (ArtifactResolutionException e) {
            propagateMojoExecutionException(e);
        } catch (ArtifactNotFoundException e) {
            propagateMojoExecutionException(e);
        }
        return artifact;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Set<ConfiguredArtifact> barArtifacts = configuredArtifacts;

        if (barArtifacts.isEmpty()) {
            getLog().info("No broker artifact dependencies declared.");
        } else {
            for (ConfiguredArtifact configuredArtifact : configuredArtifacts) {
                Artifact sourceArtifact = createAndResolveArtifact(configuredArtifact.getArtifactItem());

                try {
                    if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                        throw new MojoExecutionException("Output directory does not exist and could not be created.");
                    }
                    if (!outputDirectory.isDirectory()) {
                        throw new MojoExecutionException("Output directory property should be a directory.");
                    }

                    File targetFile = artifactFileForClassifier(configuredArtifact.getClassifier());
                    if (targetFile.exists() && !targetFile.delete()) {
                        throw new MojoExecutionException("Failed to remove existing target file.");
                    }

                    if (targetFile.createNewFile()) {
                        final Properties baseProperties = getProperties();
                        baseProperties.putAll(configuredArtifact.getProperties());
                        Properties props = applyPropertiesForClassifier(baseProperties, configuredArtifact.getClassifier());
                        barConfigurator.configure(sourceArtifact, targetFile, props);

                        Artifact artifact = artifactFactory.createArtifactWithClassifier(
                                project.getGroupId(), project.getArtifactId(), project.getVersion(), Types.BROKER_ARCHIVE_TYPE, configuredArtifact.getClassifier());

                        getLog().info("Attaching configured " + sourceArtifact + " as " + artifact);
                        projectHelper.attachArtifact(
                                project, Types.BROKER_ARCHIVE_TYPE, configuredArtifact.getClassifier(), targetFile);
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
                    @Override
                    public boolean apply(String input) {
                        return input.equalsIgnoreCase(classifier);
                    }
                })) {
                    newProperties.setProperty(parts[1], newProperties.getProperty(name));
                }
            }
        }
        return newProperties;
    }

    private File artifactFileForClassifier(final String classifier) {
        final String basename = Joiner.on('-').join(
                project.getArtifact().getArtifactId(), project.getArtifact().getVersion(), classifier);
        return new File(outputDirectory, basename + Types.BROKER_ARCHIVE_EXTENSION);
    }

}