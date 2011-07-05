/**
 * Copyright (c) 2009, The Press Association.
 */
package com.pressassociation.maven.wmb;

/**
 * Data object holding the name of a BAR file
 * and the execution group to which it should be deployed.
 *
 * @author Simon Beaver
 * @version $Id$
 */
public class Deploy {

	/**
	 * BAR file to be deployed.
	 */
	private String file;

	/**
	 * Name of execution group to which BAR file will be deployed.
	 */
	private String execGrp;

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param pFile the file to set
	 */
	public void setFile(String pFile) {
		file = pFile;
	}

	/**
	 * @return the execGrp
	 */
	public String getExecGrp() {
		return execGrp;
	}

	/**
	 * @param pExecGrp the execGrp to set
	 */
	public void setExecGrp(String pExecGrp) {
		execGrp = pExecGrp;
	}
}
