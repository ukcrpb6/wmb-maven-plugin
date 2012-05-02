/**
 * Copyright (c) 2011, The Press Association.
 */
package com.pressassociation.maven.wmb.mojo;

import com.pressassociation.maven.wmb.utils.BarUtils;
import com.pressassociation.maven.wmb.types.BrokerArchive;
import com.pressassociation.maven.wmb.Types;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoExecute;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maven mojo class to create a Websphere Message Broker (WMB) BAR file.
 *
 * @author Bob Browning
 */
@MojoGoal("package")
@MojoExecute(goal = "process-resources")
public final class PackageBarMojo extends AbstractBarMojo {

    @MojoParameter(required = true, expression = "${project}", readonly = true)
    private MavenProject project;

    @MojoComponent
    private MavenProjectHelper projectHelper;

    private File _mqsicreatebar;

    private File getMQSICreateBar() throws MojoExecutionException {
        if (_mqsicreatebar == null) {
            _mqsicreatebar = new File(toolkitDirectory, "mqsicreatebar");
            if (!_mqsicreatebar.exists()) {
                throw new MojoExecutionException(String.format("Invalid toolkit directory (%s), cannot locate mqsicreatebar.", toolkitDirectory));
            }
            if (!_mqsicreatebar.canExecute()) {
                throw new MojoExecutionException("Permission denied, cannot execute mqsicreatebar.");
            }
        }
        return _mqsicreatebar;
    }

    /**
     * Standard Maven mojo method for running plugin classes. This examines the
     * mode specified in the project's POM file and processes the rest of the
     * configuration accordingly.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        processBarArtifacts();
    }

    /**
     * Builds one or more BAR files specified using the 'bars' configuration element.
     * This allows customised sets of artifacts and their dependencies to be built for a
     * single project.
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public void processBarArtifacts() throws MojoExecutionException, MojoFailureException {
        if (brokerArchives == null || brokerArchives.length == 0) {
            throw new MojoFailureException("Invalid configuration, no BAR artifacts found.");
        }

        for (BrokerArchive archive : brokerArchives) {
            FileSet flowFileSet = archive.getFlows();
            if (flowFileSet.getDirectory() == null) {
                flowFileSet.setDirectory(basedir);
            }

            String[] includedFiles = fileSetManager.getIncludedFiles(flowFileSet);
            for (int i = 0; i < includedFiles.length; i++) {
                includedFiles[i] = FileUtils.catPath(flowFileSet.getDirectory(), includedFiles[i]);
            }

            if (archive.isFilenameProvided()) {
                process(archive.getClassifier(), includedFiles, archive.getProjects());
            } else {
                for (String filename : includedFiles) {
                    process(BarUtils.createIndividualBarClassifier(archive, filename),
                            new String[]{filename}, archive.getProjects());
                }
            }
        }
    }

    /**
     * Perform the build of the specified BAR file.
     * This involves copying the relevant projects to a temporary
     * location, removing any .metadata directories, running
     * mqsicreatebar to create the BAR file, and moving the BAR
     * file back to the main target directory.
     * <p/>
     * This somewhat convoluted procedure is a result of the recommended
     * best practice for using mqsicreatebar, which runs a headless instance
     * of Eclipse each time it is invoked.
     *
     * @param classifier
     * @param artifacts
     * @param projects
     * @throws MojoExecutionException
     */
    private void process(String classifier, String[] artifacts, String[] projects) throws MojoExecutionException {

        StringBuilder artifactFilename = new StringBuilder();
        artifactFilename.append(project.getArtifactId())
                .append("-")
                .append(project.getVersion())
                .append("-").append(classifier)
                .append(Types.BROKER_ARCHIVE_EXTENSION);
        File barArchive = new File(targetdir, artifactFilename.toString());

        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add(getMQSICreateBar().getAbsolutePath());
        cmdlist.add("-data");
        cmdlist.add(generatedSourcesDir);
        cmdlist.add("-b");
        cmdlist.add(barArchive.getAbsolutePath());
        cmdlist.add("-cleanBuild");
        cmdlist.add("-o");

        Collections.addAll(cmdlist, artifacts);

        if (projects != null && projects.length > 0) {
            cmdlist.add("-p");
            Collections.addAll(cmdlist, projects);
        }

        ProcessBuilder pb = new ProcessBuilder(cmdlist);
        pb.directory(new File(generatedSourcesDir));

        // Run the build command.
        try {
            getLog().info("Building artifact " + artifactFilename + ", this may take a few moments.");
            getLog().debug(pb.toString());
        
            Process proc = pb.start();
            try {
                proc.waitFor();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String s;
                    while((s = bufferedReader.readLine()) != null) {
                        getLog().info(s.trim());
                    }
                    
                    getLog().debug("mqsicreatebar returned exit code " + proc.exitValue());
                    if (proc.exitValue() > 0) {
                        throw new MojoExecutionException(IOUtil.toString(proc.getErrorStream()));
                    }
                } catch (IOException e) {
                    throw new MojoExecutionException("Error trying to output process input stream");
                }
            } catch (InterruptedException e) {
                throw new MojoExecutionException("Error waiting for mqsicreatebar.", e);
            } finally {
                IOUtil.close(proc.getInputStream());
                IOUtil.close(proc.getOutputStream());
                IOUtil.close(proc.getErrorStream());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error running mqsicreatebar command.", e);
        }

        projectHelper.attachArtifact(project, Types.BROKER_ARCHIVE_TYPE, classifier, barArchive);
    }
}
