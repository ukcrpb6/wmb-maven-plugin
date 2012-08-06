package com.pressassociation.maven.wmb.utils;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public final class MojoUtils {

    /**
     * Propagate exception messages as MojoExecutionException instances.
     *
     * @param t Throwable to be wrapped.
     * @return MojoExecutionException instance wrapping t.
     */
    public static MojoExecutionException propagateMojoExecutionException(Throwable t) {
        if (t instanceof MojoExecutionException) {
            return (MojoExecutionException) t;
        }
        return new MojoExecutionException(t.getMessage(), t);
    }

}
