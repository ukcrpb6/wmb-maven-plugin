package com.pressassociation.maven.wmb;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Bob Browning
 */
public final class BarUtils {

    /*
     * Message flow file extension
     */
    private static final String EXT_MSGFLOW = ".msgflow";

    public static String createIndividualBarFilename(BrokerArchive artifact, String filename) {
        String suffix = FileUtils.basename(filename, EXT_MSGFLOW) + BrokerArchive.EXT_BAR;
        if(StringUtils.isNotBlank(artifact.getPrefix())) {
            return artifact.getPrefix() + "-" + suffix;
        }
        return suffix;
    }
}
