/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.option.server.MoveFileOptions;

/**
 * Move file(s) from one location to another. </p>
 * 
 * Move takes an already opened file and moves it from one client location to
 * another, reopening it as a pending depot move. When the file is submitted
 * with 'p4 submit', its depot file is moved accordingly. </p>
 * 
 * Wildcards in fromFile and toFile must match. The fromFile must be a file
 * opened for add or edit. </p>
 * 
 * 'p4 opened' lists pending moves. 'p4 diff' can compare a moved client file
 * with its depot original, 'p4 sync' can schedule an update of a moved file,
 * and 'p4 resolve' can resolve the update. </p>
 * 
 * A client file may be moved many times before it is submitted. Moving a file
 * back to its original location will undo a pending move, leaving unsubmitted
 * content intact. Using 'p4 revert' will both undo the move and revert the
 * unsubmitted content. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class MoveTask extends ClientTask {

    /**
     * Source file and revision specifiers, separated by space. If the path
     * contains whitespace, it must be double-quoted.
     */
    protected String fromFile;

    /**
     * Target file and revision specifiers, separated by space. If the path
     * contains whitespace, it must be double-quoted.
     */
    protected String toFile;

    /**
     * If not IChangelist.UNKNOWN, the files are opened in the numbered pending
     * changelist instead of the 'default' changelist. Corresponds to the -c
     * flag.
     */
    protected String changelist = String.valueOf(IChangelist.UNKNOWN);

    /**
     * If true, don't actually perform the move, just return what would happen
     * if the move was performed. Corresponds to the -n flag.
     */
    protected boolean listOnly = false;

    /**
     * If true, force a move to an existing target file; the file must be synced
     * and not opened. Note that the originating source file will no longer be
     * synced to the client. Corresponds to the -f flag.
     */
    protected boolean force = false;

    /**
     * If true, bypasses the client file rename. This option can be used to tell
     * the server that the user has already renamed a file on the client. The
     * use of this option can confuse the server if you are wrong about the
     * client's contents. Only works for 2009.2 and later servers; earlier
     * servers will produce a RequestException if you set this true. Corresponds
     * to the -k flag.
     */
    protected boolean noClientMove = false;

    /**
     * If not null, the file is reopened as that filetype. Corresponds to the -t
     * flag.
     */
    protected String fileType = null;

    /** Source file and revision specifier for the Perforce command. */
    protected IFileSpec fromFileSpec;

    /** Target file and revision specifier for the Perforce command. */
    protected IFileSpec toFileSpec;

    /**
     * Default constructor.
     */
    public MoveTask() {
        super();
        commandOptions = new MoveFileOptions(parseChangelist(changelist),
                listOnly, force, noClientMove, fileType);
    }

    /**
     * Sets the from file.
     * 
     * @param fromFile
     *            the new from file
     */
    public void setFromFile(String fromFile) {
        this.fromFile = fromFile;
    }

    /**
     * Sets the to file.
     * 
     * @param toFile
     *            the new to file
     */
    public void setToFile(String toFile) {
        this.toFile = toFile;
    }

    /**
     * Sets the changelist.
     * 
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        ((MoveFileOptions) commandOptions)
                .setChangelistId(parseChangelist(changelist));
    }

    /**
     * Sets the list only.
     * 
     * @param listOnly
     *            the new list only
     */
    public void setListOnly(boolean listOnly) {
        ((MoveFileOptions) commandOptions).setListOnly(listOnly);
    }

    /**
     * Sets the force.
     * 
     * @param force
     *            the new force
     */
    public void setForce(boolean force) {
        ((MoveFileOptions) commandOptions).setForce(force);
    }

    /**
     * Sets the no client move.
     * 
     * @param noClientMove
     *            the new no client move
     */
    public void setNoClientMove(boolean noClientMove) {
        ((MoveFileOptions) commandOptions).setNoClientMove(noClientMove);
    }

    /**
     * Sets the file type.
     * 
     * @param fileType
     *            the new file type
     */
    public void setFileType(String fileType) {
        ((MoveFileOptions) commandOptions).setFileType(fileType);
    }

    /**
     * Gets the from file spec.
     * 
     * @return the from file spec
     */
    public IFileSpec getFromFileSpec() {
        return fromFileSpec;
    }

    /**
     * Gets the to file spec.
     * 
     * @return the to file spec
     */
    public IFileSpec getToFileSpec() {
        return toFileSpec;
    }

    /**
     * Execute the Perforce move command with source and target file specs and
     * options. Log the returned file specs.
     * <p>
     * Move a file already opened for edit or add (the fromFile) to the
     * destination file (the toFile). A file can be moved many times before it
     * is submitted; moving it back to its original location will reopen it for
     * edit. The full semantics of this operation (which can be confusing) are
     * found in the main 'p4 help' documentation.
     * <p>
     * Note that this operation is not supported on servers earlier than 2009.1;
     * any attempt to use this on earlier servers will result in a
     * RequestException with a suitable message.
     * 
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fromFileSpec = new FileSpec(fromFile);
            toFileSpec = new FileSpec(toFile);
            retFileSpecs = getP4Server().moveFile(fromFileSpec, toFileSpec,
                    ((MoveFileOptions) commandOptions));
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
