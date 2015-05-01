/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;

/**
 * Base class for Perforce client specific Ant tasks. It initializes an instance
 * of the Perforce server and an instance of the Perforce client. </p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public abstract class ClientTask extends ServerTask {

    /** Perforce client. */
    protected IClient p4Client;

    /**
     * Default constructor.
     */
    public ClientTask() {
        super();
    }

    /**
     * Gets the p4 client.
     *
     * @return the p4 client
     */
    public IClient getP4Client() {
        return p4Client;
    }

    /**
     * Sets the p4 client.
     *
     * @param p4Client
     *            the new p4 client
     */
    public void setP4Client(IClient p4Client) {
        this.p4Client = p4Client;
    }

    /**
     * Initialize an instance of the Perforce client from the server with a
     * specified client name. Set the current client on the server.
     */
    private void initP4Client() {
        try {
            // Get an instance of the Perforce client.
            p4Client = p4Server.getClient(client);
            if (p4Client == null) {
                String errorMsg = p4Messages.getMessage(
                        "p4.client.unknown", new Object[] { client }); //$NON-NLS-1$
                throw new BuildException(errorMsg);
            }
            // Set it to the sever as the current client.
            p4Server.setCurrentClient(p4Client);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }

    /**
     * Cleanup the Perforce client instance.
     */
    private void cleanupP4Client() {
        // Set the client to null.
        p4Client = null;
    }

    /**
     * Override method to initialize the Perforce server and client,
     * respectively. The server is initialized first, since the client is
     * initialized from the server instance.
     *
     * @see ServerTask#initP4()
     */
    @Override
    protected void initP4() throws BuildException {
        // Initialize the P4J server.
        initP4Server();
        // Initialize the Perforce client.
        initP4Client();
    }

    /**
     * Override method to cleanup the Perforce client and server instances.
     *
     * @see ServerTask#cleanupP4()
     */
    @Override
    protected void cleanupP4() throws BuildException {
        // Cleanup the P4J client.
        cleanupP4Client();
        // Cleanup the P4J server.
        cleanupP4Server();
    }
}
