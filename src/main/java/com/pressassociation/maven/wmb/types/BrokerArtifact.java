package com.pressassociation.maven.wmb.types;

import com.google.common.base.Objects;
import com.pressassociation.maven.wmb.Types;
import org.apache.maven.artifact.factory.ArtifactFactory;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class BrokerArtifact extends ForwardingArtifact {

    private String groupId;

    private String artifactId;

    private String version;

    private String type = Types.BROKER_ARCHIVE_TYPE;

    private String classifier;

    private String executionGroup;

    public void setArtifactFactory(ArtifactFactory artifactFactory) {
        this.configureDelegate(artifactFactory);
    }

    @Override
    public String getGroupId() {
        if (isDelegateConfigured()) {
            return super.getGroupId();
        }
        return groupId;
    }

    @Override
    public String getVersion() {
        if (isDelegateConfigured()) {
            return super.getVersion();
        }
        return version;
    }

    @Override
    public void setVersion(String version) {
        if (isDelegateConfigured()) {
            super.setVersion(version);
        }
        this.version = version;
    }

    @Override
    public void setGroupId(String groupId) {
        if (isDelegateConfigured()) {
            super.setGroupId(groupId);
        }
        this.groupId = groupId;
    }

    @Override
    public void setArtifactId(String artifactId) {
        if (isDelegateConfigured()) {
            super.setArtifactId(artifactId);
        }
        this.artifactId = artifactId;
    }

    @Override
    public String getClassifier() {
        if (isDelegateConfigured()) {
            return super.getClassifier();
        }
        return classifier;
    }

    @Override
    public String getType() {
        if (isDelegateConfigured()) {
            return super.getType();
        }
        return type;
    }

    @Override
    public String getArtifactId() {
        if (isDelegateConfigured()) {
            super.getArtifactId();
        }
        return artifactId;
    }

    public String getExecutionGroup() {
        return executionGroup;
    }

    @Override
    public String toString() {
        if (isDelegateConfigured()) {
            return Objects.toStringHelper(getClass() + "[" + (isResolved() ? "Resolved]" : "Configured]"))
                    .add("groupId", getGroupId())
                    .add("artifactId", getArtifactId())
                    .add("version", getVersion())
                    .add("type", getType())
                    .add("classifier", getClassifier())
                    .add("executionGroup", executionGroup).toString();
        }
        return Objects.toStringHelper(getClass())
                .add("groupId", groupId)
                .add("artifactId", artifactId)
                .add("version", version)
                .add("type", type)
                .add("classifier", classifier)
                .add("executionGroup", executionGroup).toString();
    }
}
