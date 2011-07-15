/**
 * Copyright (c) 2011, The Press Association.
 */
package com.pressassociation.maven.wmb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maven mojo class to create a Websphere Message Broker (WMB) BAR file.
 * 
 * The 'bar' package goal operates in one of three modes.<br/>
 * <table>
 * <tr><td>'single'</td><td>builds a single BAR file containing all specified artifacts.</td></tr>
 * <tr><td>'individual'&nbsp;</td><td>builds a BAR file for each artifact.</td></tr>
 * <tr><td>'barfile'</td><td>builds a set of custom BAR files as defined using the 'bars' configuration element.</td></tr>
 * </table>
 *
 * @author Simon Beaver
 * @author Bob Browning
 * @version 1.0
 * 
 * @goal package
 * @phase package
 */
public final class CreateBarMojo extends AbstractBarMojo {

    /*
     * Message flow file extension
     */
    private static final String EXT_MSGFLOW = ".msgflow";

    /**
	 * Full path to mqsicreatebar command.
	 * @parameter default-value="/opt/IBM/WMBT700" expression="${wmb.toolkitDirectory}"
	 */
	private File toolkitDirectory;

    private File _mqsicreatebar;

    private File getMQSICreateBar() throws MojoExecutionException {
        if(_mqsicreatebar == null) {
            _mqsicreatebar = new File(toolkitDirectory, "mqsicreatebar");
            if(!_mqsicreatebar.exists()) {
                throw new MojoExecutionException(String.format("Invalid toolkit directory (%s), cannot locate mqsicreatebar.", toolkitDirectory));
            }
            if(!_mqsicreatebar.canExecute()) {
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
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	public void processBarArtifacts() throws MojoExecutionException, MojoFailureException {
		if(barArtifacts == null || barArtifacts.length == 0) {
			throw new MojoFailureException("Invalid configuration, no BAR artifacts found.");
        }

		for(BarArtifactSet artifact : barArtifacts) {
            if(artifact.isFilenameProvided()) {
			    process(artifact.getFilename().concat(BarArtifactSet.EXT_BAR),
                        artifact.getIncludesArray(),
                        artifact.getProjects());
            } else {
                for (String filename : artifact.getIncludesArray()) {
                    process(BarUtils.createIndividualBarFilename(artifact, filename), new String[] { filename }, artifact.getProjects());
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
	 * 
	 * This somewhat convoluted procedure is a result of the recommended
	 * best practice for using mqsicreatebar, which runs a headless instance
	 * of Eclipse each time it is invoked.
	 * 
	 * @param filename
     * @param artifacts
     * @param projects
	 * @throws MojoExecutionException 
	 * @throws IOException
	 */
	private void process(String filename, String[] artifacts, String[] projects) throws MojoExecutionException {
		
		List<String> cmdlist = new ArrayList<String>();
		cmdlist.add(getMQSICreateBar().getAbsolutePath());
		cmdlist.add("-data");
		cmdlist.add(basedir);
		cmdlist.add("-b");
		cmdlist.add(targetdir + File.separator + filename);
		cmdlist.add("-cleanBuild");
		cmdlist.add("-o");

        Collections.addAll(cmdlist, artifacts);

		if (projects != null && projects.length > 0) {
			cmdlist.add("-p");
            Collections.addAll(cmdlist, projects);
		}

		ProcessBuilder pb = new ProcessBuilder(cmdlist);
		pb.directory(new File(basedir));

		getLog().info(pb.command().toString());

		// Run the build command.
		Process proc = null;
		try {
			proc = pb.start();
            try {
				proc.waitFor();
			} catch (InterruptedException e) {
				throw new MojoExecutionException("Error waiting for mqsicreatebar.", e);
			} finally {
                try {
                    proc.getErrorStream().close();
                    proc.getInputStream().close();
                    proc.getOutputStream().close();
                } catch (IOException e) {
                    getLog().warn("Error closing mqsicreatebar process IO stream.", e);
                }
            }
		} catch (IOException e) {
			throw new MojoExecutionException("Error running mqsicreatebar command.", e);
		} finally {
            if(proc != null) {
                getLog().info("mqsicreatebar returned exit code " + proc.exitValue());
            }
        }
	}
}
