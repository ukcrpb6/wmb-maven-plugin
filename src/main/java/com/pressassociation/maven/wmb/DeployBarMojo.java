/**
 * Copyright (c) 2009, The Press Association.
 */
package com.pressassociation.maven.wmb;

import com.ibm.broker.config.proxy.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.jfrog.maven.annomojo.annotations.MojoExecute;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import java.io.File;
import java.io.IOException;

/**
 * Maven mojo for deploying BAR files to a Websphere Message Broker instance.
 * Deployment information is stored in Maven profiles, with one profile being
 * created for each possible deployment environment, e.g. dev, stage, etc.
 * The exception to this is the execution group to which the BAR file will be deployed.
 * This can be specified either as a global value, or else within the <barfile/>
 * configuration setting on a per-BAR-file basis.
 *
 * @author Bob Browning
 */
@MojoGoal("deploy")
@MojoExecute(goal = "process-resources")
public final class DeployBarMojo extends AbstractBarMojo {

    private static final int BROKER_TIMEOUT = 30000;

    /**
     * Hostname of server to which BAR files will be deployed.
     */
    @MojoParameter(expression = "${wmb.host}", defaultValue = "localhost")
    private String hostname;

    /**
     * Port on which to connect to server.
     */
    @MojoParameter(expression = "${wmb.port}", defaultValue = "7080")
    private int port;

    /**
     * Queue Manager to use when connecting to Message Broker.
     */
    @MojoParameter(expression = "${wmb.queueMgr}", required = true)
    private String queueMgr;

    /**
     * Broker connection parameters.
     */
    private MQBrokerConnectionParameters connectionParameters;

    /**
     * Main execution method.
     *
     * @throws MojoExecutionException
     */
    @Override
    public void execute() throws MojoExecutionException {
        processFromDeployments();
    }

    /**
     * Getter to retrieve broker connection parameters.
     *
     * @return MQBrokerConnectionParameters
     */
    private MQBrokerConnectionParameters getConnectionParameters() {
        if (connectionParameters == null) {
            connectionParameters = new MQBrokerConnectionParameters(hostname, port, queueMgr);
        }
        return connectionParameters;
    }

    /**
     * Use the 'deployments' configuration element to drive the deployment process.
     * For each defined BAR file, check that the file exists and then deploy it to
     * the execution group specified in the configuration, which is assumed to reside
     * on a server defined in a Maven profile within the project's POM file.
     *
     * @throws MojoExecutionException
     */
    private void processFromDeployments() throws MojoExecutionException {
        try {
            BrokerProxy brokerProxy = BrokerProxy.getInstance(getConnectionParameters());
            for (BrokerArchive artifact : brokerArchives) {
                /* Ignore artifacts marked as non-deployable */
                if (!artifact.isDeployable()) {
                    continue;
                }
                if (artifact.isFilenameProvided()) {
                    final String filename = targetdir + File.separatorChar
                            + artifact.getClassifier() + Types.BROKER_ARCHIVE_EXTENSION;
                    deployBarFile(brokerProxy, artifact, new File(filename));
                } else {
                    FileSet fileSet = artifact.getFlows();
                    if(fileSet.getDirectory() == null) {
                        fileSet.setDirectory(generatedSourcesDir);
                    }
                    String[] includedFiles = fileSetManager.getIncludedFiles(artifact.getFlows());
                    for (String filename : includedFiles) {
                        deployBarFile(brokerProxy, artifact,
                                new File(targetdir, BarUtils.createIndividualBarClassifier(artifact, filename)));
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
     * Deploys a bar file using the specified broker connection proxy
     *
     * @param broker The broker proxy to use to deploy the bar
     * @param artifact The artifact to be deployed
     * @param file the filename of the BAR
     * @throws ConfigManagerProxyLoggedException
     * @throws IOException
     * @throws MojoExecutionException
     * @throws ConfigManagerProxyPropertyNotInitializedException
     */
    private void deployBarFile(final BrokerProxy broker, final BrokerArchive artifact, final File file)
            throws ConfigManagerProxyLoggedException, IOException, MojoExecutionException,
            ConfigManagerProxyPropertyNotInitializedException {
        if (file.exists()) {
            ExecutionGroupProxy executionGroup = broker.getExecutionGroupByName(artifact.getExecutionGroup());
            if (executionGroup != null) {
                DeployResult result = executionGroup.deploy(file.getPath(), true, BROKER_TIMEOUT);
                if (result.getCompletionCode() != CompletionCodeType.success) {
                    throw new MojoExecutionException("Error deploying BAR file: "
                            + result.getCompletionCode().toString());
                } else {
                    getLog().info("BAR file '" + file.getName()
                            + "' deployed to execution group '" + artifact.getExecutionGroup() + "'");
                }
            } else {
                getLog().warn("Could not connect to execution group " + artifact.getExecutionGroup() + ".");
            }
        }
    }
}
