/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.UnlockFilesOptions;

/**
 * Release a locked file but leave it open. </p>
 * 
 * The unlock command releases a lock on an open file in a pending changelist.
 * If the file is open in a specific pending changelist other than 'default',
 * then the -c flag is required to specify the pending changelist. If no file
 * name is given then all files in the designated changelist are unlocked. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class UnlockTask extends ClientTask {

    /**
     * If positive, use the changelist given instead of the default changelist.
     * Corresponds to the -c option.
     */
    protected String changelist = String.valueOf(IChangelist.DEFAULT);

    /**
     * Force unlock of any file; normally files can only be unlocked by their
     * owner. The forceUnlock (-f flag) options requires 'admin' access granted
     * by 'p4 protect'
     */
    protected boolean forceUnlock = false;

    /**
     * Default constructor.
     */
    public UnlockTask() {
        super();
        commandOptions = new UnlockFilesOptions(parseChangelist(changelist),
                forceUnlock);
    }

    /**
     * Sets the changelist.
     * 
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        ((UnlockFilesOptions) commandOptions)
                .setChangelistId(parseChangelist(changelist));
    }

    /**
     * Sets the force unlock.
     * 
     * @param forceUnlock
     *            the new force unlock
     */
    public void setForceUnlock(boolean forceUnlock) {
        ((UnlockFilesOptions) commandOptions).setForceUnlock(forceUnlock);
    }

    /**
     * Execute the Perforce unlock command with file specs and options. Log the
     * returned file specs.
     * <p>
     * Release locked files but leave them open.
     * <p>
     * Note that the file specs returned are only partially filled out; the
     * Perforce server seems to only return path information for this command.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().unlockFiles(fileSpecs,
                    ((UnlockFilesOptions) commandOptions));
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
