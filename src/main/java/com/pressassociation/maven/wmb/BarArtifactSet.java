/**
 * Copyright (c) 2009, The Press Association.
 */
package com.pressassociation.maven.wmb;

import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Data object holding parameters needed to create a BAR file.
 *
 * @author Simon Beaver
 * @author Bob Browning
 * @version $Id$
 */
public class BarArtifactSet extends FileSet {

    public static final String EXT_BAR = ".bar";

    /**
	 * Name of BAR.
	 */
	private String _filename = "";

    /**
     * Name _prefix for individual bar files.
     */
    private String _prefix = "";

	/**
	 * List of dependent _projects for the artifacts.
	 */
	private String[] _projects;

	/**
	 * Execution Group to which this BAR file should be deployed.
	 */
	private String _executionGroup = "";

	/**
	 * @return the _filename
	 */
	public String getFilename() {
		return _filename;
	}

	/**
	 * @param pName the _filename to set
	 */
	public void setFilename(final String pName) {
		_filename = pName;
	}

	/**
	 * @param projects the _projects to set
	 */
	public void setProjects(final String[] projects) {
		this._projects = projects;
	}

	/**
	 * @return the _projects
	 */
	public String[] getProjects() {
		return _projects;
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
        return !"".equals(_filename);
    }

    public String getPrefix() {
        return _prefix;
    }

    public void setPrefix(final String prefix) {
        this._prefix = prefix;
    }
}
