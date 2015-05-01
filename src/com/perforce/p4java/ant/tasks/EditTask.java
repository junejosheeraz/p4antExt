/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.EditFilesOptions;

/**
 * Open an existing file for edit. The server notes that the current user on the
 * current client has the file opened, and then changes the file permission from
 * read-only to read/write. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class EditTask extends ClientTask {

    /**
     * If true, don't actually do the edit, just return the files that would
     * have been opened for edit.
     */
    protected boolean noUpdate = false;

    /**
     * If true, bypass updating the client.
     */
    protected boolean bypassClientUpdate = false;

    /**
     * If positive, the opened files are put into the pending changelist
     * identified by changelistId (this changelist must have been previously
     * created for this to succeed). If zero or negative, the file is opened in
     * the 'default' (unnumbered) changelist.
     */
    protected String changelist = String.valueOf(IChangelist.DEFAULT);

    /**
     * If non-null, the files are added as that filetype. See 'p4 help
     * filetypes' to attempt to make any sense of Perforce file types.
     */
    protected String fileType = null;

    /**
     * Default constructor.
     */
    public EditTask() {
        super();
        commandOptions = new EditFilesOptions(noUpdate, bypassClientUpdate,
                parseChangelist(changelist), fileType);
    }

    /**
     * Sets the no update.
     * 
     * @param noUpdate
     *            the new no update
     */
    public void setNoUpdate(boolean noUpdate) {
        ((EditFilesOptions) commandOptions).setNoUpdate(noUpdate);
    }

    /**
     * Sets the bypass client update.
     * 
     * @param bypassClientUpdate
     *            the new bypass client update
     */
    public void setBypassClientUpdate(boolean bypassClientUpdate) {
        ((EditFilesOptions) commandOptions)
                .setBypassClientUpdate(bypassClientUpdate);
    }

    /**
     * Sets the changelist.
     * 
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        ((EditFilesOptions) commandOptions)
                .setChangelistId(parseChangelist(changelist));
    }

    /**
     * Sets the file type.
     * 
     * @param fileType
     *            the new file type
     */
    public void setFileType(String fileType) {
        ((EditFilesOptions) commandOptions).setFileType(fileType);
    }

    /**
     * Execute the Perforce edit command with file specs and options. Log the
     * returned file specs.
     * <p>
     * Open one or more Perforce client workspace files for editing.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().editFiles(fileSpecs,
                    ((EditFilesOptions) commandOptions));
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
