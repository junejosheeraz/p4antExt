/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.GetDepotFilesOptions;

/**
 * List files in the depot. List files named or matching wild card
 * specification. Display shows depot file name, revision, file type, change
 * action and changelist number of the current head revision. If client file
 * names are given as arguments the view mapping is used to list the
 * corresponding depot files. </p>
 *
 * If the file argument has a revision, then all files as of that revision are
 * listed. If the file argument has a revision range, then only files selected
 * by that revision range are listed, and the highest revision in the range is
 * used for each file. Normally, the head revision is listed.</p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class FilesTask extends ClientTask {

    /**
     * If true, displays all revisions within the specific range, rather than
     * just the highest revision in the range.
     */
    protected boolean allRevs = false;

    /**
     * Default constructor.
     */
    public FilesTask() {
        super();
        commandOptions = new GetDepotFilesOptions(allRevs);
    }

    /**
     * Sets the all revs.
     *
     * @param allRevs
     *            the new all revs
     */
    public void setAllRevs(boolean allRevs) {
        ((GetDepotFilesOptions) commandOptions).setAllRevs(allRevs);
    }

    /**
     * Execute the Perforce files command with file specs and options. Log the
     * returned file specs.
     * <p>
     * List all Perforce depot files known to the Perforce server that conform
     * to the passed-in wild-card file specification(s).
     * <p>
     * If client file names are given as file spec arguments the current
     * Perforce client view mapping is used to list the corresponding depot
     * files, if the client and view exist (if not, the results are undefined).
     * <p>
     * See 'p4 help revisions' for help specifying revisions.
     * <p>
     * Note that the IFileSpec objects returned will have null client and local
     * path components.
     *
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Server().getDepotFiles(fileSpecs,
                    ((GetDepotFilesOptions) commandOptions));
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
