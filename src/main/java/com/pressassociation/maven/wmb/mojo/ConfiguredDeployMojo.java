package com.pressassociation.maven.wmb.mojo;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.ibm.broker.config.proxy.*;
import com.pressassociation.maven.wmb.types.BrokerArtifact;
import com.pressassociation.maven.wmb.types.ConfigurableServiceItem;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;

import static com.pressassociation.maven.wmb.utils.MojoUtils.propagateMojoExecutionException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@MojoGoal("deploy")
@MojoRequiresDependencyResolution
public class ConfiguredDeployMojo extends AbstractDeployMojo {

    private static final int BROKER_TIMEOUT = 300000;

    /**
     * Set of deployable artifacts.
     */
    @MojoParameter(required = true, readonly = true)
    private BrokerArtifact[] artifacts;


    @MojoParameter(readonly = true)
    private List<ConfigurableServiceItem> configurableServices;

    /**
     * Artifact factory component.
     */
    @MojoComponent
    private ArtifactFactory artifactFactory;

    /**
     * Artifact resolver component.
     */
    @MojoComponent
    private ArtifactResolver resolver;

    /**
     * List of remote repositories.
     */
    @MojoParameter(expression = "${project.remoteArtifactRepositories}", required = true, readonly = true)
    private List<ArtifactRepository> remoteRepositories;

    /**
     * Local repository.
     */
    @MojoParameter(expression = "${localRepository}", required = true, readonly = true)
    private ArtifactRepository localRepository;

    @MojoParameter(expression = "${wmb.cs.remove}", required = false)
    private boolean removeExistingConfigurableService = false;

    /**
     * Maven project.
     */
    @MojoParameter(expression = "${project}", required = true, readonly = true)
    private MavenProject project;

    private static void waitForProxyPopulation(BrokerProxy proxy) {
        while(!proxy.hasBeenPopulatedByBroker()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final BrokerProxy proxy;
        try {
            proxy = BrokerProxy.getInstance(connectionParameters.get());
            waitForProxyPopulation(proxy);
        } catch (ConfigManagerProxyLoggedException e) {
            throw propagateMojoExecutionException(e);
        }

        for (ConfigurableServiceItem serviceItem : configurableServices) {
            try {
                if(Arrays.binarySearch(proxy.getConfigurableServiceTypes(), serviceItem.getType()) > -1) {
                    getLog().info("Looking up configurable service " + serviceItem);

                    ConfigurableService configurableService = proxy.getConfigurableService(serviceItem.getType(), serviceItem.getName());
                    if(removeExistingConfigurableService) {
                        if(configurableService != null) {
                            getLog().info("Deleting configurable service " + serviceItem);
                            configurableService.delete();
                            proxy.refresh();
                            configurableService = null;
                        }
                    }

                    if(configurableService == null) {
                        getLog().info("Creating configurable service " + serviceItem);
                        proxy.createConfigurableService(serviceItem.getType(), serviceItem.getName());
                        proxy.refresh();
                        configurableService = proxy.getConfigurableService(serviceItem.getType(), serviceItem.getName());
                    } else {
                        getLog().info("Configurable service " + serviceItem + " exists.");
                    }

                    if(configurableService == null) {
                        throw new MojoExecutionException("Failed to create configurable service " + serviceItem);
                    }

                    configurableService.setProperties(serviceItem.getProperties());
                    proxy.refresh();
                } else {
                    throw new MojoExecutionException("Invalid configurable service " + serviceItem + " possible type values are " + Arrays.toString(proxy.getConfigurableServiceTypes()));
                }
            } catch (ConfigManagerProxyPropertyNotInitializedException e) {
                propagateMojoExecutionException(e);
            } catch (ConfigManagerProxyLoggedException e) {
                propagateMojoExecutionException(e);
            }
        }

        for (BrokerArtifact artifact : artifacts) {
            /* Broker artifacts should always be attached artifacts */
            artifact.setGroupId(project.getGroupId());
            artifact.setArtifactId(project.getArtifactId());
            artifact.setVersion(project.getVersion());

            artifact.setArtifactFactory(artifactFactory);

            if (artifact.getExecutionGroup() == null) {
                throw new MojoExecutionException("Missing required execution group value for artifact " + artifact);
            }

            getLog().info("Deploying " + artifact + " to " + hostname + ":" + port + "/" + queueMgr + "/"
                    + artifact.getExecutionGroup());

            try {
                resolver.resolve(artifact, remoteRepositories, localRepository);
            } catch (ArtifactResolutionException e) {
                throw propagateMojoExecutionException(e);
            } catch (ArtifactNotFoundException e) {
                throw propagateMojoExecutionException(e);
            }

            try {
                ExecutionGroupProxy executionGroupProxy = proxy.getExecutionGroupByName(artifact.getExecutionGroup());
                if(executionGroupProxy == null) {
                    executionGroupProxy = proxy.createExecutionGroup(artifact.getExecutionGroup());
                }
                DeployResult deployResult = executionGroupProxy.deploy(artifact.getFile().getPath(), true, BROKER_TIMEOUT);
                if (deployResult.getCompletionCode() != CompletionCodeType.success) {
                    Enumeration<LogEntry> responses = deployResult.getDeployResponses();
                    while (responses.hasMoreElements()) {
                        getLog().error(responses.nextElement().getDetail());
                    }
                    throw new MojoExecutionException("Error deploying broker archive.");
                }
            } catch (ConfigManagerProxyPropertyNotInitializedException e) {
                throw propagateMojoExecutionException(e);
            } catch (ConfigManagerProxyLoggedException e) {
                throw propagateMojoExecutionException(e);
            } catch (IOException e) {
                throw propagateMojoExecutionException(e);
            }
        }
    }

}
