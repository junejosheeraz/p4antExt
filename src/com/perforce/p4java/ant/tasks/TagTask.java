/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.TagFilesOptions;

/**
 * Tag files with a label. </p>
 *
 * Tag associates the named label with the file revisions indicated by the file
 * argument. Once file revisions are tagged with a label, revision
 * specifications of the form '@label' can be used to refer to them. </p>
 *
 * If the file argument does not include a revision specification, the head
 * revisions will be tagged. See 'p4 help revisions' for revision specification
 * options. </p>
 *
 * If the file argument includes a revision range specification, only the files
 * with revisions in that range will be tagged. Files with more than one
 * revision in the range will be tagged at the highest revision. </p>
 *
 * The "delete" (-d flag) option deletes the association between the specified
 * files and the label, regardless of revision. </p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class TagTask extends ClientTask {

    /**
     * Lists the files that would be tagged, but doesn't actually do anything.
     */
    protected boolean listOnly = false;

    /**
     * Deletes the association between the specified files and the label,
     * regardless of revision.
     */
    protected boolean delete = false;

    /** Name of the label. */
    protected String label;

    /**
     * Default constructor.
     */
    public TagTask() {
        super();
        commandOptions = new TagFilesOptions(listOnly, delete);
    }

    /**
     * Sets the list only.
     *
     * @param listOnly
     *            the new list only
     */
    public void setListOnly(boolean listOnly) {
        ((TagFilesOptions) commandOptions).setListOnly(listOnly);
    }

    /**
     * Sets the delete.
     *
     * @param delete
     *            the new delete
     */
    public void setDelete(boolean delete) {
        ((TagFilesOptions) commandOptions).setDelete(delete);
    }

    /**
     * Sets the label.
     *
     * @param label
     *            the new label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Execute the Perforce tag command with file specs, label and options. Log
     * the returned file specs.
     * <p>
     * Tag associates the named label with the file revisions indicated by the
     * file argument. Once file revisions are tagged with a label, revision
     * specifications of the form '@label' can be used to refer to them.
     *
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        if (isEmpty(label)) {
            throw new BuildException("No label name specified."); //$NON-NLS-1$
        }
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Server().tagFiles(fileSpecs, label,
                    ((TagFilesOptions) commandOptions));
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
