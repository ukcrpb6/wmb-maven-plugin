/**
 * Copyright (c) 2011, The Press Association.
 */
package com.pressassociation.maven.wmb.mojo;

import com.pressassociation.maven.wmb.Types;
import com.pressassociation.maven.wmb.types.BrokerArchive;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
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
public final class PackageBarMojo extends AbstractBarMojo {

    /**
     * Maven Toolchain Manager
     */
    @MojoComponent
    private ToolchainManager toolchainManager;

    /**
     * Maven Session
     */
    @MojoParameter(expression = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * Maven Project
     */
    @MojoParameter(expression = "${project}", readonly = true, required = true)
    private MavenProject project;

    @MojoParameter(expression = "wmb.skipWSErrorCheck", defaultValue = "false", readonly = true)
    private boolean skipWSErrorCheck;

    @MojoParameter(expression = "wmb.cleanBuild", defaultValue = "true", readonly = true)
    private boolean cleanBuild;

    /**
     * Maven Project Helper
     */
    @MojoComponent
    private MavenProjectHelper projectHelper;

    @MojoParameter(expression = "${wmb.compiler.compilerId}", defaultValue = "mqsicreatebar")
    private String compilerId;

    /**
     * Full path to mqsicreatebar command.
     */
    @MojoParameter(defaultValue = "/opt/IBM/WMBT800", expression = "${wmb.toolkitDirectory}")
    private File toolkitDirectory;

    private File _mqsicreatebar;

    private Toolchain getToolchain() {
        if (toolchainManager != null) {
            return toolchainManager.getToolchainFromBuildContext("mqsitoolkit", session);
        }
        return null;
    }

    private File getMQSICreateBar() throws MojoExecutionException {
        if (_mqsicreatebar == null) {
            Toolchain tc = getToolchain();
            if (tc != null) {
                getLog().info("Toolchain in wmb-maven-plugin: " + tc);
                _mqsicreatebar = new File(tc.findTool(compilerId));
            }

            if (_mqsicreatebar != null) {
                toolkitDirectory = _mqsicreatebar.getParentFile();
            } else {
                _mqsicreatebar = new File(toolkitDirectory, compilerId);
            }

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
            if (!archive.isFilenameProvided()) {
                throw new MojoFailureException("Require archive filename");
            }

            process(archive);
        }
    }

    private String[] resolveIncludedFiles(FileSet flowFileSet) {
        if (flowFileSet.getDirectory() == null) {
            flowFileSet.setDirectory(basedir);
        }
        String[] includedFiles = fileSetManager.getIncludedFiles(flowFileSet);
        for (int i = 0; i < includedFiles.length; i++) {
            includedFiles[i] = FileUtils.catPath(flowFileSet.getDirectory(), includedFiles[i]);
        }
        return includedFiles;
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
     * @param archive
     * @throws MojoExecutionException
     */
    private void process(BrokerArchive archive) throws MojoExecutionException {
        final String artifactFilename = project.getArtifactId() +
                "-" + project.getVersion() + "-" + archive.getClassifier()+ Types.BROKER_ARCHIVE_EXTENSION;

        File targetBarFile = new File(targetdir, artifactFilename);

        ProcessBuilder pb = new ProcessBuilder(buildCommand(archive, targetBarFile));
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
                    while ((s = bufferedReader.readLine()) != null) {
                        getLog().info(s.trim());
                    }

                    getLog().debug("mqsicreatebar returned exit code " + proc.exitValue());
                    if (proc.exitValue() > 0) {
                        throw new MojoExecutionException(IOUtil.toString(proc.getErrorStream()));
                    }
                } catch (IOException e) {
                    throw new MojoExecutionException("Error trying to output process input stream", e);
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

        projectHelper.attachArtifact(project, Types.BROKER_ARCHIVE_TYPE, archive.getClassifier(), targetBarFile);
    }

    private List<String> buildCommand(BrokerArchive archive, File targetBarFile) throws MojoExecutionException {
        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add(getMQSICreateBar().getAbsolutePath());
        cmdlist.add("-data");
        cmdlist.add(generatedSourcesDir);
        cmdlist.add("-b");
        cmdlist.add(targetBarFile.getAbsolutePath());

        if (archive.getDeployableFiles() != null) {
            cmdlist.add("-o");
            Collections.addAll(cmdlist, resolveIncludedFiles(archive.getDeployableFiles()));
        }

        if (archive.getProjects() != null && archive.getProjects().length > 0) {
            cmdlist.add("-p");
            Collections.addAll(cmdlist, archive.getProjects());
        }

        if (archive.getApplications() != null && archive.getApplications().length > 0) {
            cmdlist.add("-a");
            Collections.addAll(cmdlist, archive.getApplications());
        }

        if (archive.getLibraries() != null && archive.getLibraries().length > 0) {
            cmdlist.add("-l");
            Collections.addAll(cmdlist, archive.getLibraries());
        }

        cmdlist.add("-version");
        cmdlist.add(project.getVersion());

        if (skipWSErrorCheck) {
            cmdlist.add("-skipWSErrorCheck");
        }

        if (cleanBuild) {
            cmdlist.add("-cleanBuild");
        }

        return cmdlist;
    }
}
