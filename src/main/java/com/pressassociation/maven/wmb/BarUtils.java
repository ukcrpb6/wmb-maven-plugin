package com.pressassociation.maven.wmb;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author Bob Browning
 */
public final class BarUtils {
    public static String createIndividualBarFilename(BarArtifactSet artifact, String filename) {
        String suffix = FileUtils.basename(filename, ".msgflow") + BarArtifactSet.EXT_BAR;
        if(!"".equals(artifact.getPrefix())) {
            return artifact.getPrefix() + "-" + suffix;
        }
        return suffix;
    }
}
