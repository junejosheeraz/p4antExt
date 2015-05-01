/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.DeleteFilesOptions;

/**
 * Opens a file that currently exists in the depot for deletion. If the file is
 * present on the client it is removed. If a pending changelist number is given
 * the opened file is associated with that changelist, otherwise it is
 * associated with the 'default' pending changelist. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class DeleteTask extends ClientTask {

    /**
     * If positive, the deleted files are put into the pending changelist
     * identified by changelistId (this changelist must have been previously
     * created for this to succeed). If zero or negative, the file is opened in
     * the 'default' (unnumbered) changelist.
     */
    protected String changelist = String.valueOf(IChangelist.DEFAULT);

    /**
     * If true, don't actually do the deletes, just return the files that would
     * have been opened for deletion.
     */
    protected boolean noUpdate = false;

    /**
     * If true, delete files that are not synced into the client workspace.
     */
    protected boolean deleteNonSyncedFiles = false;

    /**
     * Default constructor.
     */
    public DeleteTask() {
        super();
        commandOptions = new DeleteFilesOptions(parseChangelist(changelist),
                noUpdate, deleteNonSyncedFiles);
    }

    /**
     * Sets the no update.
     * 
     * @param noUpdate
     *            the new no update
     */
    public void setNoUpdate(boolean noUpdate) {
        ((DeleteFilesOptions) commandOptions).setNoUpdate(noUpdate);
    }

    /**
     * Sets the delete non synced files.
     * 
     * @param deleteNonSyncedFiles
     *            the new delete non synced files
     */
    public void setDeleteNonSyncedFiles(boolean deleteNonSyncedFiles) {
        ((DeleteFilesOptions) commandOptions)
                .setDeleteNonSyncedFiles(deleteNonSyncedFiles);
    }

    /**
     * Sets the changelist.
     * 
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        ((DeleteFilesOptions) commandOptions)
                .setChangelistId(parseChangelist(changelist));
    }

    /**
     * Execute the Perforce delete command with file specs and options. Log the
     * returned file specs.
     * <p>
     * Open Perforce client workspace files for deletion from a Perforce depot.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().deleteFiles(fileSpecs,
                    ((DeleteFilesOptions) commandOptions));
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
