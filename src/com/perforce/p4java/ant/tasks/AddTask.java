/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.AddFilesOptions;

/**
 * Opens new files for adding to the depot. If the files exist on the client
 * they are read to determine if they are text or binary. If the file type
 * cannot be determined then it is assumed to be text. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class AddTask extends ClientTask {

    /**
     * If positive, the opened files are put into the pending changelist
     * identified by changelistId (this changelist must have been previously
     * created for this to succeed). If zero or negative, the file is opened in
     * the 'default' (unnumbered) changelist.
     */
    protected String changelist = String.valueOf(IChangelist.DEFAULT);

    /**
     * If true, it lists what would be opened for add without actually changing
     * any files or metadata.
     */
    protected boolean noUpdate = false;

    /**
     * If null, it looks for a filetype using the name-to-type mapping table
     * managed by 'p4 typemap'. It senses the filetype by examining the file's
     * contents and execute permission bit. If non-null, the files are added as
     * that filetype.
     */
    protected String fileType = null;

    /**
     * If true, filenames that contain wildcards are permitted. Filenames added
     * to the repository that contain these special wildcard characters '@',
     * '#', '%' or '*' will have those characters formatted into ascii
     * hexadecimal representation. The only way of referring to those files once
     * added will be to use the formatted version, the local filesystem name
     * will not be recognized.
     */
    protected boolean useWildcards = false;

    /**
     * Default constructor.
     */
    public AddTask() {
        super();
        commandOptions = new AddFilesOptions(noUpdate,
                parseChangelist(changelist), fileType, useWildcards);
    }

    /**
     * Sets the no update.
     * 
     * @param noUpdate
     *            the new no update
     */
    public void setNoUpdate(boolean noUpdate) {
        ((AddFilesOptions) commandOptions).setNoUpdate(noUpdate);
    }

    /**
     * Sets the changelist.
     * 
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        ((AddFilesOptions) commandOptions)
                .setChangelistId(parseChangelist(changelist));
    }

    /**
     * Sets the file type.
     * 
     * @param fileType
     *            the new file type
     */
    public void setFileType(String fileType) {
        ((AddFilesOptions) commandOptions).setFileType(fileType);
    }

    /**
     * Sets the use wildcards.
     * 
     * @param useWildcards
     *            the new use wildcards
     */
    public void setUseWildcards(boolean useWildcards) {
        ((AddFilesOptions) commandOptions).setUseWildcards(useWildcards);
    }

    /**
     * Execute the Perforce add command with file specs and options. Log the
     * returned file specs.
     * <p>
     * Open one or more Perforce client workspace files for adding to the
     * Perforce server.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().addFiles(fileSpecs,
                    (AddFilesOptions) commandOptions);
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
