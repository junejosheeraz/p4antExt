/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.LockFilesOptions;

/**
 * Lock an opened file against changelist submission. </p>
 * 
 * The open files named are locked in the depot, preventing any user other than
 * the current user on the current client from submitting changes to the files.
 * If a file is already locked then the lock request is rejected. If no file
 * names are given then lock all files currently open in the changelist number
 * given or in the 'default' changelist if no changelist number is given. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class LockTask extends ClientTask {

    /**
     * If positive, use the changelist given instead of the default changelist.
     * Corresponds to the -c option.
     */
    protected String changelist = String.valueOf(IChangelist.DEFAULT);

    /**
     * Default constructor.
     */
    public LockTask() {
        super();
        commandOptions = new LockFilesOptions(parseChangelist(changelist));
    }

    /**
     * Sets the changelist.
     * 
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        ((LockFilesOptions) commandOptions)
                .setChangelistId(parseChangelist(changelist));
    }

    /**
     * Execute the Perforce lock command with file specs and options. Log the
     * returned file specs.
     * <p>
     * Lock an opened file against changelist submission.
     * <p>
     * The open files named are locked in the Perforce depot, preventing any
     * user other than the current user on the current client from submitting
     * changes to the files. If a file is already locked then the lock request
     * is rejected. If no file specs are given then lock all files currently
     * open in the changelist number given.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().lockFiles(fileSpecs,
                    ((LockFilesOptions) commandOptions));
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
