/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.RevertFilesOptions;

/**
 * Discard changes from opened files. Revert an open file back to the revision
 * previously synced from the depot, discarding any pending changelists or
 * integrations that have been made. This command requires naming files
 * explicitly. After running revert the named files will no longer be locked or
 * open. </p>
 *
 * @see PerforceTask
 * @see ClientTask
 */
public class RevertTask extends ClientTask {

    /**
     * If true, don't actually do the revert, just return the files that would
     * have been opened for reversion.
     */
    protected boolean noUpdate = false;

    /**
     * If positive, the reverted files are put into the pending changelist
     * identified by changelistId (this changelist must have been previously
     * created for this to succeed). If zero or negative, the file is opened in
     * the 'default' (unnumbered) changelist.
     */
    protected String changelist = String.valueOf(IChangelist.UNKNOWN);

    /**
     * If true, revert only unchanged files.
     */
    protected boolean revertOnlyUnchanged = false;

    /**
     * If true bypass the client file refresh.
     */
    protected boolean noClientRefresh = false;

    /**
     * Default constructor.
     */
    public RevertTask() {
        super();
        commandOptions = new RevertFilesOptions(noUpdate,
                parseChangelist(changelist), revertOnlyUnchanged,
                noClientRefresh);
    }

    /**
     * Sets the changelist.
     *
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        ((RevertFilesOptions) commandOptions)
                .setChangelistId(parseChangelist(changelist));
    }

    /**
     * Sets the no update.
     *
     * @param noUpdate
     *            the new no update
     */
    public void setNoUpdate(boolean noUpdate) {
        ((RevertFilesOptions) commandOptions).setNoUpdate(noUpdate);
    }

    /**
     * Sets the revert only unchanged.
     *
     * @param revertOnlyUnchanged
     *            the new revert only unchanged
     */
    public void setRevertOnlyUnchanged(boolean revertOnlyUnchanged) {
        ((RevertFilesOptions) commandOptions)
                .setRevertOnlyUnchanged(revertOnlyUnchanged);
    }

    /**
     * Sets the no client refresh.
     *
     * @param noClientRefresh
     *            the new no client refresh
     */
    public void setNoClientRefresh(boolean noClientRefresh) {
        ((RevertFilesOptions) commandOptions)
                .setNoClientRefresh(noClientRefresh);
    }

    /**
     * Execute the Perforce revert command with file specs and options. Log the
     * returned file specs.
     * <p>
     * Revert open Perforce client workspace files back to the revision
     * previously synced from the Perforce depot, discarding any pending
     * changelists or integrations that have been made so far.
     *
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().revertFiles(fileSpecs,
                    ((RevertFilesOptions) commandOptions));
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
