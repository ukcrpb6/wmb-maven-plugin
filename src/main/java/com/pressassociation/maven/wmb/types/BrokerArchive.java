/**
 * Copyright (c) 2009, The Press Association.
 */
package com.pressassociation.maven.wmb.types;

import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Data object holding parameters needed to create a BAR file.
 *
 * @author Bob Browning
 */
public class BrokerArchive {

    /**
	 * Broker archive classifier
	 */
    private String classifier = "";

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
	 * @return the classifier
	 */
	public String getClassifier() {
		return classifier;
	}

	/**
	 * @param pName the classifier to set
	 */
	public void setClassifier(final String pName) {
		classifier = pName;
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
        return !"".equals(classifier);
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
