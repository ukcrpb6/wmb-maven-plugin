/**
 * Copyright (c) 2009, The Press Association.
 */
package com.pressassociation.maven.wmb;

/**
 * Data object holding parameters needed to create a BAR file.
 *
 * @author Simon Beaver
 * @version $Id$
 */
public class Barfile {

	/**
	 * Name of BAR file.
	 */
	private String filename;

	/**
	 * List of artifacts to place in this file.
	 */
	private String[] artifacts;

	/**
	 * List of dependent projects for the artifacts.
	 */
	private String[] projects;

	/**
	 * Execution Group to which this BAR file should be deployed.
	 */
	private String execGroup;

	/**
	 * @return the name
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param pFilename the name to set
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
	 * @param projects the projects to set
	 */
	public void setProjects(String[] projects) {
		this.projects = projects;
	}

	/**
	 * @return the projects
	 */
	public String[] getProjects() {
		return projects;
	}

	/**
	 * @return the execGroup
	 */
	public String getExecGroup() {
		return execGroup;
	}

	/**
	 * @param pExecGroup the execGroup to set
	 */
	public void setExecGroup(String pExecGroup) {
		execGroup = pExecGroup;
	}
}
