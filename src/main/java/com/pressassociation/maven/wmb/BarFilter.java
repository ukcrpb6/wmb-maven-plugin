/**
 * Copyright (c) 2009, The Press Association.
 */
package com.pressassociation.maven.wmb;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filter class to use for listing
 * BAR files in directories.
 *
 * @author Simon Beaver
 * @version $Id$
 */
public class BarFilter implements FilenameFilter {

	@Override
	public boolean accept(File pDir, String pName) {

		if(pName.endsWith(".bar")) {
			return true;
		} else {
			return false;
		}
	}
}
