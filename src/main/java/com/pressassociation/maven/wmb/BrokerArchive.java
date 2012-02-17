/**
 * Copyright (c) 2009, The Press Association.
 */
package com.pressassociation.maven.wmb;

import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Data object holding parameters needed to create a BAR file.
 *
 * @author Bob Browning
 */
public class BrokerArchive {

    public static final String EXT_BAR = ".bar";

    /**
	 * Name of BAR.
	 */
    private String filename = "";

    /**
     * Name prefix for individual bar files.
     */
    private String prefix = "";

	/**
	 * List of dependent projects for the artifacts.
	 */
	private String[] projects;

    /**
     * Fileset of flows
     */
    private FileSet flows;

	/**
	 * Execution Group to which this BAR file should be deployed.
	 */
	private String _executionGroup = "";

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param pName the filename to set
	 */
	public void setFilename(final String pName) {
		filename = pName;
	}

	/**
	 * @param projects the projects to set
	 */
	public void setProjects(final String[] projects) {
		this.projects = projects;
	}

	/**
	 * @return the projects
	 */
	public String[] getProjects() {
		return projects;
	}

	/**
	 * @return the _executionGroup
	 */
	public String getExecutionGroup() {
		return _executionGroup;
	}

	/**
	 * @param pExecGroup the _executionGroup to set
	 */
	public void setExecutionGroup(final String pExecGroup) {
		_executionGroup = pExecGroup;
	}

    /**
     * @return True if the artifact is deployable
     */
    public boolean isDeployable() {
        return !("".equals(_executionGroup));
    }

    public boolean isFilenameProvided() {
        return !"".equals(filename);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public FileSet getFlows() {
        return flows;
    }

    public void setFlows(FileSet flows) {
        this.flows = flows;
    }
}
