/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.UnshelveFilesOptions;

/**
 * Restore shelved files from a pending change into a workspace. </p>
 * 
 * 'p4 unshelve' retrieves the shelved files from a pending changelist and
 * copies them into a pending changelist on the invoking user's workspace.
 * Unshelving files from a pending changelist is restricted by the user's
 * permissions on the files. A successful unshelve operation places the shelved
 * files on the user's workspace with the same open action and pending
 * integration history as if it had originated from that user and client. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class UnshelveTask extends ClientTask {

    /**
     * The source pending changelist that contains the shelved files.
     */
    protected String fromChangelist = String.valueOf(IChangelist.DEFAULT);

    /**
     * The target changelist to receive the shelved files.
     */
    protected String toChangelist = String.valueOf(IChangelist.DEFAULT);

    /** Force the unshelve operation. */
    protected boolean forceUnshelve = false;

    /**
     * previews what would be unshelved without actually changing any files or
     * metadata.
     */
    protected boolean preview = false;

    /**
     * Default constructor.
     */
    public UnshelveTask() {
        super();
        commandOptions = new UnshelveFilesOptions(forceUnshelve, preview);
    }

    /**
     * Sets the from changelist.
     * 
     * @param fromChangelist
     *            the new from changelist
     */
    public void setFromChangelist(String fromChangelist) {
        this.fromChangelist = fromChangelist;
    }

    /**
     * Sets the to changelist.
     * 
     * @param toChangelist
     *            the new to changelist
     */
    public void setToChangelist(String toChangelist) {
        this.toChangelist = toChangelist;
    }

    /**
     * Sets the force unshelve.
     * 
     * @param forceUnshelve
     *            the new force unshelve
     */
    public void setForceUnshelve(boolean forceUnshelve) {
        ((UnshelveFilesOptions) commandOptions).setForceUnshelve(forceUnshelve);
    }

    /**
     * Sets the preview.
     * 
     * @param preview
     *            the new preview
     */
    public void setPreview(boolean preview) {
        ((UnshelveFilesOptions) commandOptions).setPreview(preview);
    }

    /**
     * Execute the Perforce unshelve command with source changelist, target
     * changelist and options. Log the returned file specs.
     * <p>
     * Restore shelved files from a pending change into a workspace.
     * <p>
     * Unshelving files from a pending changelist is restricted by the user's
     * permissions on the files. A successful unshelve operation places the
     * shelved files on the user's workspace with the same open action and
     * pending integration history as if it had originated from that user and
     * client.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().unshelveFiles(fileSpecs,
                    parseChangelist(fromChangelist),
                    parseChangelist(toChangelist),
                    ((UnshelveFilesOptions) commandOptions));
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
