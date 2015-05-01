/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.ShelveFilesOptions;

/**
 * Store files from a pending changelist in the depot, without submitting them.
 * </p>
 *
 * 'p4 shelve' creates, modifies or deletes shelved files in a pending
 * changelist. Shelved files persist in the depot until they are deleted (using
 * 'p4 shelve -d') or replaced by subsequent shelve commands. After 'p4 shelve',
 * the user can revert the files and restore them later using 'p4 unshelve'.
 * Other users can 'p4 unshelve' the stored files into their own workspaces.
 * </p>
 *
 * Files that have been shelved can be accessed by the 'p4 diff', 'p4 diff2',
 * 'p4 files' and 'p4 print' commands using the revision specification
 * '@=change', where 'change' is the pending changelist number. </p>
 *
 * By default, 'p4 shelve' creates a changelist, adds files from the user's
 * default changelist then shelves those files into the depot. The user is
 * presented with a changelist form in the user's text editor that is configured
 * for editing Perforce specifications. </p>
 *
 * If a file pattern is specified, 'p4 shelve' limits the list of files to those
 * matching the pattern. </p>
 *
 * @see PerforceTask
 * @see ClientTask
 */
public class ShelveTask extends ClientTask {

    /**
     * The pending changelist that contains shelved files to be created,
     * deleted, or modified.
     */
    protected String changelist = String.valueOf(IChangelist.UNKNOWN);

    /** If true, force the shelve operation. */
    protected boolean forceShelve = false;

    /** If true, allow the incoming files to replace the shelved files. */
    protected boolean replaceFiles = false;

    /** If true, delete incoming files from the shelve. */
    protected boolean deleteFiles = false;

    /**
     * Default constructor.
     */
    public ShelveTask() {
        super();
        commandOptions = new ShelveFilesOptions(forceShelve, replaceFiles,
                deleteFiles);
    }

    /**
     * Sets the changelist.
     *
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        this.changelist = changelist;
    }

    /**
     * Sets the force shelve.
     *
     * @param forceShelve
     *            the new force shelve
     */
    public void setForceShelve(boolean forceShelve) {
        ((ShelveFilesOptions) commandOptions).setForceShelve(forceShelve);
    }

    /**
     * Sets the replace files.
     *
     * @param replaceFiles
     *            the new replace files
     */
    public void setReplaceFiles(boolean replaceFiles) {
        ((ShelveFilesOptions) commandOptions).setReplaceFiles(replaceFiles);
    }

    /**
     * Sets the delete files.
     *
     * @param deleteFiles
     *            the new delete files
     */
    public void setDeleteFiles(boolean deleteFiles) {
        ((ShelveFilesOptions) commandOptions).setDeleteFiles(deleteFiles);
    }

    /**
     * Execute the Perforce shelve command with file specs, changelist and
     * options. Log the returned file specs.
     * <p>
     * Shelve files in a changelist. Store them on the Perforce server without
     * committing them.
     *
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        if (parseChangelist(changelist) == IChangelist.DEFAULT
                || parseChangelist(changelist) == IChangelist.UNKNOWN) {
            throw new BuildException("Must specify a changelist;" +
            		" it cannot be default or unknown."); //$NON-NLS-1$
        }
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().shelveFiles(fileSpecs,
                    parseChangelist(changelist),
                    ((ShelveFilesOptions) commandOptions));
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
