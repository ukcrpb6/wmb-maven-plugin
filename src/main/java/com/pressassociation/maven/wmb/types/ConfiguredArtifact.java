package com.pressassociation.maven.wmb.types;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.pressassociation.maven.wmb.utils.MojoUtils.propagateMojoExecutionException;

public class ConfiguredArtifact {
    /**
     * @parameter
     * @required
     */
    private ArtifactItem artifactItem;

    /**
     * @parameter
     */
    private String classifier;

    /**
     * @parameter
     */
    private File propertiesFile;

    /**
     * @parameter
     */
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
        }
        return properties;
    }

    public ArtifactItem getArtifactItem() {
        return artifactItem;
    }

    public void setArtifactItem(ArtifactItem artifactItem) {
        this.artifactItem = checkNotNull(artifactItem);
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getClassifier() {
        if (classifier == null) {
            return getArtifactItem().getClassifier();
        }
        return classifier;
    }
}
