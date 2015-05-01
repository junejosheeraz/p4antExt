/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;

/**
 * List files and revisions that have been synced to the client workspace. List
 * revisions of named files that were last synced from the depot. If no file
 * name is given list all files synced on this client. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class HaveTask extends ClientTask {

    /**
     * Default constructor.
     */
    public HaveTask() {
        super();
    }

    /**
     * Execute the Perforce have command with file specs. Log the returned file
     * specs.
     * <p>
     * Return a list of all Perforce-managed files and versions that the
     * Perforce server believes this Perforce client workspace has as of the
     * latest sync. If fileSpecs is given, this method returns, only information
     * on those files is returned.
     * <p>
     * Only the depotFile, revision, clientPath, and localPath fields of the
     * returned file specs are guaranteed to be valid.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().haveList(fileSpecs);
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
