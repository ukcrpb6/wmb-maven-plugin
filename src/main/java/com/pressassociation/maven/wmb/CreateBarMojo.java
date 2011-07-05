/**
 * Copyright (c) 2011, The Press Association.
 */
package com.pressassociation.maven.wmb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Maven mojo class to create a Websphere Message Broker (WMB) BAR file.
 * 
 * The 'bar' package goal operates in one of three modes.<br/>
 * <table>
 * <tr><td>'single'</td><td>builds a single BAR file containing all specified artifacts.</td></tr>
 * <tr><td>'individual'&nbsp;</td><td>builds a BAR file for each artifact.</td></tr>
 * <tr><td>'barfile'</td><td>builds a set of custom BAR files as defined using the 'barfiles' configuration element.</td></tr>
 * </table>
 *
 * @author Simon Beaver
 * @version 1.0
 * 
 * @goal package
 * @phase package
 */
public class CreateBarMojo extends AbstractMojo {

	/**
	 * Full path to mqsicreatebar command.
	 * @parameter
	 */
	String mqsicreatebar = "/opt/IBM/WMBT700/mqsicreatebar";

	/**
	 * Compilation mode.
	 * @parameter
	 */
	private String mode;

	/**
	 * List of BAR files to build.
	 * @parameter
	 */
	private Barfile[] barfiles;

	/**
	 * File name for single BAR file.
	 * @parameter
	 */
	private String filename;

	/**
	 * List of artifacts to build.
	 * @parameter
	 */
	private String[] artifacts;

	/**
	 * List of dependent projects for artifacts.
	 * @parameter
	 */
	private String[] projects;

	/**
	 * Base directory.
	 * @parameter expression="${basedir}"
	 */
	private String basedir;

	/**
	 * Target directory for build output.
	 * @parameter expression="${project.build.directory}"
	 */
	private String targetdir;
	

	/**
	 * Standard Maven mojo method for running plugin classes. This examines the
	 * mode specified in the project's POM file and processes the rest of the
	 * configuration accordingly.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		if (mqsicreatebar == null || mqsicreatebar.equals("")) {
			throw new MojoFailureException("Location of 'mqsicreatebar' not specified.");
		}

		File cmdcheck = new File(mqsicreatebar);
		if(!cmdcheck.exists()) {
			throw new MojoFailureException("'mqsicreatebar' is not at the specified location.");
		}

		if (mode.equals("single")) {
			processSingle();
		} else if (mode.equals("individual")) {
			processIndividual();
		} else if (mode.equals("barfile")) {
			processBarfiles();
		} else {
			throw new MojoFailureException("Missing or invalid mode specified.");
		}
	}

	/**
	 * Build a single BAR file using all specified artifacts and dependencies.
	 * @throws MojoFailureException 
	 * @throws MojoExecutionException 
	 */
	public void processSingle() throws MojoFailureException, MojoExecutionException {
		if (filename == null || filename.equals("")) {
			throw new MojoFailureException("Missing BAR file name. Add a <filename>VALUE</filename> element to the configuration.");
		}

		if (artifacts == null || artifacts.length == 0) {
			throw new MojoFailureException("No artifacts specified.");
		}

		process(filename, artifacts, projects);
	}

	/**
	 * Build a BAR file for each specified artifact containing that artifact
	 * plus all specified dependencies.
	 * @throws MojoFailureException
	 * @throws MojoExecutionException 
	 */
	public void processIndividual() throws MojoFailureException, MojoExecutionException {
		if (artifacts == null || artifacts.length == 0) {
			throw new MojoFailureException("No artifacts specified.");
		}

		for (String artifact : artifacts) {

			String barname = (artifact.substring((artifact.lastIndexOf("/") + 1), artifact.lastIndexOf(".")) + ".bar");
			process(barname, new String[] {artifact}, projects);
		}
		
	}

	/**
	 * Builds one or more BAR files specified using the 'barfiles' configuration element.
	 * This allows customised sets of artifacts and their dependencies to be built for a
	 * single project.
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	public void processBarfiles() throws MojoExecutionException, MojoFailureException {
		if(barfiles == null || barfiles.length == 0) {
			throw new MojoFailureException("'barfile' mode specified, but no BAR file definitions found.");
		}

		for(Barfile b : barfiles) {
			process(b.getFilename(), b.getArtifacts(), b.getProjects());
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
	 * @param barfile
	 * @throws MojoExecutionException 
	 * @throws IOException
	 */
	private void process(String filename, String[] artifacts, String[] projects) throws MojoExecutionException {
		
		List<String> cmdlist = new ArrayList<String>();
		cmdlist.add(mqsicreatebar);
		cmdlist.add("-data");
		cmdlist.add(basedir);
		cmdlist.add("-b");
		cmdlist.add(targetdir + File.separator + filename);
		cmdlist.add("-cleanBuild");
		cmdlist.add("-o");

		for (String artifact : artifacts) {
			cmdlist.add(artifact);
		}

		if (projects != null && projects.length > 0) {
			cmdlist.add("-p");
			for (String project : projects) {
				cmdlist.add(project);
			}
		}

		ProcessBuilder pb = new ProcessBuilder(cmdlist);
		pb.directory(new File(basedir));

		getLog().info(pb.command().toString());

		// Run the build command.
		Process proc = null;
		try {
			proc = pb.start();
		} catch (IOException e) {
			throw new MojoExecutionException("Error running mqsicreatebar command.", e);
		} finally {
			try {
				proc.waitFor();
				proc.getErrorStream().close();
				proc.getInputStream().close();
				proc.getOutputStream().close();
				getLog().info("mqsicreatebar returned exit code " + proc.exitValue());
			} catch (InterruptedException e) {
				throw new MojoExecutionException("Error waiting for mqsicreatebar.", e);
			} catch (IOException e) {
				throw new MojoExecutionException("Error closing mqsicreatebar.", e);
			}
		}
	}

	/**
	 * @return the mqsicreatebar
	 */
	public String getMqsicreatebar() {
		return mqsicreatebar;
	}

	/**
	 * @param pMqsicreatebar the mqsicreatebar to set
	 */
	public void setMqsicreatebar(String pMqsicreatebar) {
		mqsicreatebar = pMqsicreatebar;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param pMode the mode to set
	 */
	public void setMode(String pMode) {
		mode = pMode;
	}

	/**
	 * @return the barfiles
	 */
	public Barfile[] getBarfiles() {
		return barfiles;
	}

	/**
	 * @param pBarfiles the barfiles to set
	 */
	public void setBarfiles(Barfile[] pBarfiles) {
		barfiles = pBarfiles;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param pFilename the filename to set
	 */
	public void setFilename(String pFilename) {
		filename = pFilename;
	}

	/**
	 * @return the artifacts
	 */
	public String[] getArtifacts() {
		return artifacts;
	}

	/**
	 * @param pArtifacts the artifacts to set
	 */
	public void setArtifacts(String[] pArtifacts) {
		artifacts = pArtifacts;
	}

	/**
	 * @return the projects
	 */
	public String[] getProjects() {
		return projects;
	}

	/**
	 * @param pProjects the projects to set
	 */
	public void setProjects(String[] pProjects) {
		projects = pProjects;
	}
}
