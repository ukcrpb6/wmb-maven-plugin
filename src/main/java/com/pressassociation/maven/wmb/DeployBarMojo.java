/**
 * Copyright (c) 2009, The Press Association.
 */
package com.pressassociation.maven.wmb;

import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.ibm.broker.config.proxy.BrokerConnectionParameters;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.CompletionCodeType;
import com.ibm.broker.config.proxy.ConfigManagerProxyLoggedException;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.DeployResult;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;
import com.ibm.broker.config.proxy.MQBrokerConnectionParameters;

/**
 * Maven mojo for deploying BAR files to a Websphere Message Broker instance.
 * Deployment information is stored in Maven profiles, with one profile being
 * created for each possible deployment environment, e.g. dev, stage, etc.
 * The exception to this is the execution group to which the BAR file will be deployed.
 * This can be specified either as a global value, or else within the <barfile/>
 * configuration setting on a per-BAR-file basis.
 *
 * @author Simon Beaver
 * @version 1.0
 * 
 * @goal deploy
 * @phase deploy
 * @requiresDependencyResolution
 */
public class DeployBarMojo extends AbstractMojo {

	/**
	 * Hostname of server to which BAR files will be deployed.
	 * @parameter expression="${hostname}"
	 */
	private String hostname;

	/**
	 * Port on which to connect to server.
	 * @parameter expression="${port}"
	 */
	private int port;

	/**
	 * Queue Manager to use when connecting to Message Broker.
	 * @parameter expression="${queueMgr}"
	 */
	private String queueMgr;

	/**
	 * Name of execution group.
	 * @parameter expression="${execGroup}"
	 */
	private String executionGroup;

	/**
	 * Target directory containing BAR files to deploy.
	 * @parameter expression="${project.build.directory}"
	 */
	private String targetDir;

	/**
	 * List of BAR file configurations.
	 * @parameter
	 */
	private Deploy[] deployments;

	/**
	 * Main execution method.
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		if (deployments != null) {
			processFromDeployments();
		} else {
			processFromTarget();
		}
	}

	/**
	 * Use the 'deployments' configuration element to drive the deployment process.
	 * For each defined BAR file, check that the file exists and then deploy it to
	 * the execution group specified in the configuration, which is assumed to reside
	 * on a server defined in a Maven profile within the project's POM file.
	 * @throws MojoExecutionException 
	 */
	private void processFromDeployments() throws MojoExecutionException {

		BrokerConnectionParameters bcp = new MQBrokerConnectionParameters(hostname, port, queueMgr);
		try {
			BrokerProxy bp = BrokerProxy.getInstance(bcp);
			DeployResult dr = null;
			ExecutionGroupProxy egp = null;
			for (Deploy d : deployments) {
				String filename = targetDir + "/" + d.getFile();
				File file = new File(filename);
				if (file.exists()) {
					egp = bp.getExecutionGroupByName(d.getExecGrp());
					dr = egp.deploy(filename, true, 30000);
					if (dr.getCompletionCode() != CompletionCodeType.success) {
						throw new MojoExecutionException("Error deploying BAR file: " + dr.getCompletionCode().toString());
					} else {
						getLog().info("BAR file '" + d.getFile() + "' deployed to execution group '" + d.getExecGrp() + "'");
					}
				}
			}
		} catch (ConfigManagerProxyLoggedException e) {
			throw new MojoExecutionException("Unable to connect to broker", e);
		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
			throw new MojoExecutionException("Unable to connect to execution group.", e);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to deploy barfile.", e);
		}
	}

	/**
	 * Use the BAR files in the target directory to drive the deployment process.
	 * List all BAR files that currently exist, and deploy them to the server and
	 * execution group defined in a Maven profile within the project's POM file.
	 * @throws MojoExecutionException 
	 */
	private void processFromTarget() throws MojoExecutionException {

		File bardir = new File(targetDir);
		String[] filelist = bardir.list(new BarFilter());
		if (filelist != null) {

			BrokerConnectionParameters bcp = new MQBrokerConnectionParameters(hostname, port, queueMgr);
			try {
				BrokerProxy bp = BrokerProxy.getInstance(bcp);
				ExecutionGroupProxy egp = bp.getExecutionGroupByName(executionGroup);
				DeployResult dr = null;
				String filename = null;
				for (String barfile : filelist) {
					filename = targetDir + "/" + barfile;
					dr = egp.deploy(filename, true, 30000);
					if (dr.getCompletionCode() != CompletionCodeType.success) {
						throw new MojoExecutionException("Error deploying BAR file: " + dr.getCompletionCode().toString());
					} else {
						getLog().info("BAR file '" + barfile + "' deployed to execution group '" + executionGroup + "'");
					}
				}
			} catch (ConfigManagerProxyLoggedException e) {
				throw new MojoExecutionException("Unable to connect to broker", e);
			} catch (ConfigManagerProxyPropertyNotInitializedException e) {
				throw new MojoExecutionException("Unable to connect to execution group.", e);
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to deploy barfile.", e);
			}
		}
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param pHostname the hostname to set
	 */
	public void setHostname(String pHostname) {
		hostname = pHostname;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param pPort the port to set
	 */
	public void setPort(int pPort) {
		port = pPort;
	}

	/**
	 * @return the queueMgr
	 */
	public String getQueueMgr() {
		return queueMgr;
	}

	/**
	 * @param pQueueMgr the queueMgr to set
	 */
	public void setQueueMgr(String pQueueMgr) {
		queueMgr = pQueueMgr;
	}

	/**
	 * @return the executionGroup
	 */
	public String getExecutionGroup() {
		return executionGroup;
	}

	/**
	 * @param pExecutionGroup the executionGroup to set
	 */
	public void setExecutionGroup(String pExecutionGroup) {
		executionGroup = pExecutionGroup;
	}

	/**
	 * @return the targetDir
	 */
	public String getTargetDir() {
		return targetDir;
	}

	/**
	 * @param pTargetDir the targetDir to set
	 */
	public void setTargetDir(String pTargetDir) {
		targetDir = pTargetDir;
	}

}