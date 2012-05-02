package com.pressassociation.maven.wmb.types;

import java.util.Set;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class ArtifactSet {
    private Set<String> includes;

    private Set<String> excludes;

    public Set<String> getIncludes() {
        return includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }
}
