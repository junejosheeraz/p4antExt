/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.ReopenFilesOptions;

/**
 * Move opened files between changelists or change the files' type. Reopen takes
 * an already opened file and reopens it for the current user, optionally
 * changing its changelist or filetype. The changelist must have previously been
 * created with 'p4 change' or may be the 'default' changelist. </p>
 *
 * @see PerforceTask
 * @see ClientTask
 */
public class ReopenTask extends ClientTask {

    /**
     * The changelist to be reopened to; if non-negative, specifies which
     * changelist to reopen onto.
     */
    protected String toChangelist = String.valueOf(IChangelist.UNKNOWN);

    /** If not null, the file is reopened as the given filetype. */
    protected String fileType = null;

    /**
     * Default constructor.
     */
    public ReopenTask() {
        super();
        commandOptions = new ReopenFilesOptions(parseChangelist(toChangelist),
                fileType);
    }

    /**
     * Sets the to changelist.
     *
     * @param toChangelist
     *            the new to changelist
     */
    public void setToChangelist(String toChangelist) {
        ((ReopenFilesOptions) commandOptions)
                .setChangelistId(parseChangelist(toChangelist));
    }

    /**
     * Sets the file type.
     *
     * @param fileType
     *            the new file type
     */
    public void setFileType(String fileType) {
        ((ReopenFilesOptions) commandOptions).setFileType(fileType);
    }

    /**
     * Execute the Perforce reopen command with file specs and options. Log the
     * returned file specs.
     * <p>
     * Reopen Perforce files in a new changelist.
     *
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().reopenFiles(fileSpecs,
                    ((ReopenFilesOptions) commandOptions));
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
