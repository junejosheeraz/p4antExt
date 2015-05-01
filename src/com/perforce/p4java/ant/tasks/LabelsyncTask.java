/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.LabelSyncOptions;

/**
 * Synchronize label with the current client contents. Labelsync causes the
 * named label to reflect the current contents of the client. It records the
 * last revision of each file taken onto the client. The label's name can
 * subsequently be used in a revision specification as @label to refer to the
 * revision of a file as stored in the label. </p>
 * 
 * Without a file argument, labelsync causes the label to reflect the contents
 * of the whole client, by adding, deleting, and updating the label. If a file
 * is given, labelsync updates only that named file. </p>
 * 
 * If the file argument includes a revision specification, then that revision is
 * used instead of the revision taken by the client. If the revision specified
 * is a deleted revision, then the label will include that deleted revision. See
 * 'p4 help revisions' for help specifying revisions. </p>
 * 
 * If the file argument includes a revision range specification, then only files
 * selected by the revision range are updated, and the highest revision in the
 * range is used. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class LabelsyncTask extends ClientTask {

    /** The name of label. */
    protected String name;

    /** If true, don't actually do the update. */
    protected boolean noUpdate = false;

    /** If true, add the files in fileSpecs to the label. */
    protected boolean addFiles = false;

    /** If true, delete the files in fileSpecs from the label. */
    protected boolean deleteFiles = false;

    /**
     * Default constructor.
     */
    public LabelsyncTask() {
        super();
        commandOptions = new LabelSyncOptions(noUpdate, addFiles, deleteFiles);
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the no update.
     * 
     * @param noUpdate
     *            the new no update
     */
    public void setNoUpdate(boolean noUpdate) {
        ((LabelSyncOptions) commandOptions).setNoUpdate(noUpdate);
    }

    /**
     * Sets the adds the files.
     * 
     * @param addFiles
     *            the new adds the files
     */
    public void setAddFiles(boolean addFiles) {
        ((LabelSyncOptions) commandOptions).setAddFiles(addFiles);
    }

    /**
     * Sets the delete files.
     * 
     * @param deleteFiles
     *            the new delete files
     */
    public void setDeleteFiles(boolean deleteFiles) {
        ((LabelSyncOptions) commandOptions).setDeleteFiles(deleteFiles);
    }

    /**
     * Execute the Perforce labelsync command with file specs, name and options.
     * Log the returned jobs.
     * <p>
     * Perform a label sync operation for this client. See the main Perforce
     * documentation for an explanation of the labelsync operation.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        if (isEmpty(name)) {
            throw new BuildException("No label name specified."); //$NON-NLS-1$
        }
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().labelSync(fileSpecs, name,
                    ((LabelSyncOptions) commandOptions));
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
