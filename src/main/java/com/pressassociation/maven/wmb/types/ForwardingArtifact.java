package com.pressassociation.maven.wmb.types;

import com.google.common.base.Preconditions;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public abstract class ForwardingArtifact implements Artifact {
    
    private Artifact delegate;

    protected Artifact getDelegate() {
        return Preconditions.checkNotNull(delegate);
    }

    protected final boolean isDelegateConfigured() { return delegate != null; }

    protected Artifact configureDelegate(ArtifactFactory resolver) {
        Preconditions.checkState(delegate == null, "Artifact already configured");
        return delegate = resolver.createArtifactWithClassifier(
                getGroupId(), getArtifactId(), getVersion(), getType(), getClassifier());
    }

    @Override
    public String getGroupId() {
        return getDelegate().getGroupId();
    }

    @Override
    public String getArtifactId() {
        return getDelegate().getArtifactId();
    }

    @Override
    public String getVersion() {
        return getDelegate().getVersion();
    }

    @Override
    public void setVersion(String version) {
        getDelegate().setVersion(version);
    }

    @Override
    public String getScope() {
        return getDelegate().getScope();
    }

    @Override
    public String getType() {
        return getDelegate().getType();
    }

    @Override
    public String getClassifier() {
        return getDelegate().getClassifier();
    }

    @Override
    public boolean hasClassifier() {
        return getDelegate().hasClassifier();
    }

    @Override
    public File getFile() {
        return getDelegate().getFile();
    }

    @Override
    public void setFile(File destination) {
        getDelegate().setFile(destination);
    }

    @Override
    public String getBaseVersion() {
        return getDelegate().getBaseVersion();
    }

    @Override
    public void setBaseVersion(String baseVersion) {
        getDelegate().setBaseVersion(baseVersion);
    }

    @Override
    public String getId() {
        return getDelegate().getId();
    }

    @Override
    public String getDependencyConflictId() {
        return getDelegate().getDependencyConflictId();
    }

    @Override
    public void addMetadata(ArtifactMetadata metadata) {
        getDelegate().addMetadata(metadata);
    }

    @Override
    public ArtifactMetadata getMetadata(Class<?> metadataClass) {
        return getDelegate().getMetadata(metadataClass);
    }

    @Override
    public Collection<ArtifactMetadata> getMetadataList() {
        return getDelegate().getMetadataList();
    }

    @Override
    public void setRepository(ArtifactRepository remoteRepository) {
        getDelegate().setRepository(remoteRepository);
    }

    @Override
    public ArtifactRepository getRepository() {
        return getDelegate().getRepository();
    }

    @Override
    public void updateVersion(String version, ArtifactRepository localRepository) {
        getDelegate().updateVersion(version, localRepository);
    }

    @Override
    public String getDownloadUrl() {
        return getDelegate().getDownloadUrl();
    }

    @Override
    public void setDownloadUrl(String downloadUrl) {
        getDelegate().setDownloadUrl(downloadUrl);
    }

    @Override
    public ArtifactFilter getDependencyFilter() {
        return getDelegate().getDependencyFilter();
    }

    @Override
    public void setDependencyFilter(ArtifactFilter artifactFilter) {
        getDelegate().setDependencyFilter(artifactFilter);
    }

    @Override
    public ArtifactHandler getArtifactHandler() {
        return getDelegate().getArtifactHandler();
    }

    @Override
    public List<String> getDependencyTrail() {
        return getDelegate().getDependencyTrail();
    }

    @Override
    public void setDependencyTrail(List<String> dependencyTrail) {
        getDelegate().setDependencyTrail(dependencyTrail);
    }

    @Override
    public void setScope(String scope) {
        getDelegate().setScope(scope);
    }

    @Override
    public VersionRange getVersionRange() {
        return getDelegate().getVersionRange();
    }

    @Override
    public void setVersionRange(VersionRange newRange) {
        getDelegate().setVersionRange(newRange);
    }

    @Override
    public void selectVersion(String version) {
        getDelegate().selectVersion(version);
    }

    @Override
    public void setGroupId(String groupId) {
        getDelegate().setGroupId(groupId);
    }

    @Override
    public void setArtifactId(String artifactId) {
        getDelegate().setArtifactId(artifactId);
    }

    @Override
    public boolean isSnapshot() {
        return getDelegate().isSnapshot();
    }

    @Override
    public void setResolved(boolean resolved) {
        getDelegate().setResolved(resolved);
    }

    @Override
    public boolean isResolved() {
        return getDelegate().isResolved();
    }

    @Override
    public void setResolvedVersion(String version) {
        getDelegate().setResolvedVersion(version);
    }

    @Override
    public void setArtifactHandler(ArtifactHandler handler) {
        getDelegate().setArtifactHandler(handler);
    }

    @Override
    public boolean isRelease() {
        return getDelegate().isRelease();
    }

    @Override
    public void setRelease(boolean release) {
        getDelegate().setRelease(release);
    }

    @Override
    public List<ArtifactVersion> getAvailableVersions() {
        return getDelegate().getAvailableVersions();
    }

    @Override
    public void setAvailableVersions(List<ArtifactVersion> versions) {
        getDelegate().setAvailableVersions(versions);
    }

    @Override
    public boolean isOptional() {
        return getDelegate().isOptional();
    }

    @Override
    public void setOptional(boolean optional) {
        getDelegate().setOptional(optional);
    }

    @Override
    public ArtifactVersion getSelectedVersion() throws OverConstrainedVersionException {
        return getDelegate().getSelectedVersion();
    }

    @Override
    public boolean isSelectedVersionKnown() throws OverConstrainedVersionException {
        return getDelegate().isSelectedVersionKnown();
    }

    @Override
    public int compareTo(Artifact artifact) {
        return getDelegate().compareTo(artifact);
    }
}
