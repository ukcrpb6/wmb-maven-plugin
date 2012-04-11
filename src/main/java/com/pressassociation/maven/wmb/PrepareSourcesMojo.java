package com.pressassociation.maven.wmb;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.jfrog.maven.annomojo.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@MojoGoal("process-resources")
@MojoPhase("process-resources")
@MojoRequiresDependencyResolution("compile")
@MojoThreadSafe
public class PrepareSourcesMojo extends AbstractBarMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Iterator<BrokerArchive> it = Iterators.forArray(checkNotNull(brokerArchives));

        /* Compose distinct set of projects */
        Set<String> projects = Sets.newHashSet();
        while (it.hasNext()) {
            Collections.addAll(projects, it.next().getProjects());
        }

        for (String project : projects) {
            File rootFile = new File(basedir, project);

            if (!rootFile.isDirectory()) {
                continue;
            }

            try {
                FileUtils.copyDirectoryStructure(rootFile, new File(generatedSourcesDir, rootFile.getName()));
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

}
