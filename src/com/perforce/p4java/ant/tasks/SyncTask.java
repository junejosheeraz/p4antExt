/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.SyncOptions;

/**
 * Synchronize the client with its view of the depot. </p>
 * 
 * Sync updates the client workspace to reflect its current view (if it has
 * changed) and the current contents of the depot (if it has changed). The
 * client view is used to map client file names to depot file names and vice
 * versa. </p>
 * 
 * Sync adds files that are in the client view but which have not been retrieved
 * before. Sync deletes previously retrieved files which are no longer in the
 * client view or have been deleted from the depot. Sync updates files which are
 * still in the client view and which have been updated in the depot. </p>
 * 
 * Normally, sync affects all files in the client workspace. If file arguments
 * are given, sync limits its operation to those files. The file arguments may
 * contain wildcards. </p>
 * 
 * If the file argument includes a revision specifier, then the given revision
 * is retrieved. Normally, the head revision is retrieved. See 'p4 help
 * revisions' for help specifying revisions. </p>
 * 
 * If the file argument includes a revision range specification, then only files
 * selected by the revision range are updated, and the highest revision in the
 * range is used. </p>
 * 
 * Normally, sync will not clobber files in the client workspace that the user
 * has made writable. Setting the 'clobber' option in the client spec disables
 * this safety check. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class SyncTask extends ClientTask {

    /**
     * If true, it forces re-sync even if the client already has the file, and
     * clobbers writable files. This flag doesn't affect open files.
     */
    protected boolean forceUpdate = false;

    /**
     * If true, it causes sync not to update the client workspace, but to list
     * what normally would be updated. <br>
     */
    protected boolean noUpdate = false;

    /**
     * If true, it bypasses the client file update. It can be used to make the
     * server believe that a client workspace already has the file.
     */
    protected boolean clientBypass = false;

    /**
     * If true, it populates the client workspace, but does not update the
     * server to reflect those updates.
     */
    protected boolean serverBypass = false;

    /**
     * Default constructor.
     */
    public SyncTask() {
        super();
        commandOptions = new SyncOptions(forceUpdate, noUpdate, clientBypass,
                serverBypass);
    }

    /**
     * Sets the force update.
     * 
     * @param forceUpdate
     *            the new force update
     */
    public void setForceUpdate(boolean forceUpdate) {
        ((SyncOptions) commandOptions).setForceUpdate(forceUpdate);
    }

    /**
     * Sets the no update.
     * 
     * @param noUpdate
     *            the new no update
     */
    public void setNoUpdate(boolean noUpdate) {
        ((SyncOptions) commandOptions).setNoUpdate(noUpdate);
    }

    /**
     * Sets the client bypass.
     * 
     * @param clientBypass
     *            the new client bypass
     */
    public void setClientBypass(boolean clientBypass) {
        ((SyncOptions) commandOptions).setClientBypass(clientBypass);
    }

    /**
     * Sets the server bypass.
     * 
     * @param serverBypass
     *            the new server bypass
     */
    public void setServerBypass(boolean serverBypass) {
        ((SyncOptions) commandOptions).setServerBypass(serverBypass);
    }

    /**
     * Execute the Perforce sync command with file specs, changelist and
     * options. Log the returned file specs.
     * <p>
     * Sync a Perforce client workspace against the Perforce server.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().sync(fileSpecs,
                    ((SyncOptions) commandOptions));
            logFileSpecs(retFileSpecs);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
