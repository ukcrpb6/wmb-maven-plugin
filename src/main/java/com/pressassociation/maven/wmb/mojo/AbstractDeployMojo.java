package com.pressassociation.maven.wmb.mojo;

import com.ibm.broker.config.proxy.MQBrokerConnectionParameters;
import org.apache.maven.plugin.AbstractMojo;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import javax.inject.Provider;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public abstract class AbstractDeployMojo extends AbstractMojo {
    /**
     * Hostname of server to which BAR files will be deployed.
     */
    @MojoParameter(expression = "${wmb.host}", defaultValue = "localhost")
    protected String hostname;

    /**
     * Port on which to connect to server.
     */
    @MojoParameter(expression = "${wmb.port}", defaultValue = "1414")
    protected int port;

    /**
     * Queue Manager to use when connecting to Message Broker.
     */
    @MojoParameter(expression = "${wmb.queueMgr}", required = true)
    protected String queueMgr;

    /**
     * Connection parameters provider.
     */
    protected Provider<MQBrokerConnectionParameters> connectionParameters = new Provider<MQBrokerConnectionParameters>() {
        MQBrokerConnectionParameters parameters;

        @Override
        public MQBrokerConnectionParameters get() {
            if (parameters == null) {
                parameters = new MQBrokerConnectionParameters(hostname, port, queueMgr);
            }
            return parameters;
        }
    };

}
