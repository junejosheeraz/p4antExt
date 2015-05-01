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
import com.perforce.p4java.option.client.IntegrateFilesOptions;

/**
 * Open files for branching or merging. 'p4 integrate' stages change propagation
 * from source files to target files, opening the target files in the client
 * workspace. 'p4 resolve' then merges content from the source files into the
 * opened target files, and 'p4 submit' commits the opened files to the depot.
 * Integrations can be abandoned with 'p4 revert'. </p>
 * 
 * When 'p4 integrate' opens a target file in the client workspace, it chooses
 * an appropriate action: 'branch' for new files, 'integrate' when the source
 * file has changed, and 'delete' when the source file was deleted. Open target
 * files are left read-only in the client workspace. 'p4 edit' can downgrade a
 * 'branch' to an 'add' or an 'integrate' to an 'edit', making the file
 * read-write. </p>
 * 
 * 'p4 integrate' maintains integration history between files. This eliminates
 * duplicate integrations and minimizes file merges by telling 'p4 resolve' what
 * to use as the merge base: generally the highest revision already integrated.
 * Integration history also prevents integrating back a pure, integration-only
 * change. Such a change is one that resulted from 'p4 resolve' without manually
 * editing the file. The search for integration history will include
 * integrations indirectly through intermediate file branches The commands 'p4
 * integrated' and 'p4 filelog' display integration history. </p>
 * 
 * A branch view may be given directly on the command line by stating the source
 * (from) and target (to) files, or indirectly by naming a stored branch view
 * with -b branch. A stored branch view may have many mappings, while a view on
 * the command line can only have one. If a stored branch view is given, the
 * target files and source files and revisions may be further limited on the
 * command. </p>
 * 
 * If no file specification is given then the entire branch view is examined for
 * needed integrations. If a file specification is given, the integration is
 * limited to only those target files. In both cases, the integration is also
 * limited to those target files that are also in the client view. </p>
 * 
 * If no revision specification is given then all revisions of the source file
 * are considered for integration. If a single revision is given, then only
 * revisions up to the given revision are included. If a pair of revisions is
 * given (separated by a comma (,)) then only those revisions, inclusively, are
 * integrated. Note that the revision specification concerns the fromFile, but
 * is attached to the toFile. See 'p4 help revisions' for help specifying
 * revisions. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class IntegrateTask extends ClientTask {

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
     * If positive, the integrated files are opened in the numbered pending
     * changelist instead of the default changelist.
     */
    protected String changelist = String.valueOf(IChangelist.UNKNOWN);

    /**
     * Causes the branch view to work bidirectionally, where the scope of the
     * command is limited to integrations whose 'from' files match
     * fromFile[revRange]. Corresponds to the -s flag, with the fromFile arg
     * being specified in the main method fromFile parameter.
     */
    protected boolean bidirectionalInteg = false;

    /**
     * If true, enable integrations around deleted revisions; equivalent to -d
     * (i.e. -Ds + -Di + -Dt)
     */
    protected boolean integrateAroundDeletedRevs = false;

    /**
     * If the target file has been deleted and the source file has changed, will
     * re-branch the source file on top of the target file. A.k.a "-Dt".
     */
    protected boolean rebranchSourceAfterDelete = false;

    /**
     * If the source file has been deleted and the target file has changed, will
     * delete the target file. A.k.a "-Ds".
     */
    protected boolean deleteTargetAfterDelete = false;

    /**
     * If the source file has been deleted and re-added, will attempt to
     * integrate all outstanding revisions of the file, including those
     * revisions prior to the delete. Normally 'p4 integrate' only considers
     * revisions since the last add. A.k.a. "-Di".
     */
    protected boolean integrateAllAfterReAdd = false;

    /**
     * Forces integrate to act without regard for previous integration history.
     * Corresponds to the -f flag.
     */
    protected boolean forceIntegration = false;

    /**
     * Causes the target files to be left at the revision currently on the
     * client (the '#have' revision). Corresponds to the -h flag.
     */
    protected boolean useHaveRev = false;

    /**
     * Enables integration between files that have no integration history.
     * Corresponds to the -i flag.
     */
    protected boolean doBaselessMerge = false;

    /**
     * Display the base file name and revision which will be used in subsequent
     * resolves if a resolve is needed. Corresponds to the -o flag.
     */
    protected boolean displayBaseDetails = false;

    /**
     * Display what integrations would be necessary but don't actually do them.
     * Corresponds to the -n flag.
     */
    protected boolean showActionsOnly = false;

    /**
     * Reverse the mappings in the branch view, with the target files and source
     * files exchanging place. Corresponds to the -r flag.
     */
    protected boolean reverseMapping = false;

    /**
     * Propagate the source file's filetype to the target file. Corresponds to
     * the -t flag.
     */
    protected boolean propagateType = false;

    /**
     * Don't copy newly branched files to the client. Corresponds to the -v
     * flag.
     */
    protected boolean dontCopyToClient = false;

    /**
     * If not null, use this as the integration branch specification.
     */
    protected String branch;

    /**
     * If positive, integrate only the first maxFiles files. Corresponds to -m
     * flag.
     */
    protected int maxFiles = 0;

    /** Source file and revision specifier for the Perforce command. */
    protected IFileSpec fromFileSpec;

    /** Target file and revision specifier for the Perforce command. */
    protected IFileSpec toFileSpec;

    /**
     * Default constructor.
     */
    public IntegrateTask() {
        super();
        commandOptions = new IntegrateFilesOptions(parseChangelist(changelist),
                bidirectionalInteg, integrateAroundDeletedRevs,
                rebranchSourceAfterDelete, deleteTargetAfterDelete,
                integrateAllAfterReAdd, forceIntegration, useHaveRev,
                doBaselessMerge, displayBaseDetails, showActionsOnly,
                reverseMapping, propagateType, dontCopyToClient, maxFiles);
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
        ((IntegrateFilesOptions) commandOptions)
                .setChangelistId(parseChangelist(changelist));
    }

    /**
     * Sets the bidirectional integ.
     * 
     * @param bidirectionalInteg
     *            the new bidirectional integ
     */
    public void setBidirectionalInteg(boolean bidirectionalInteg) {
        ((IntegrateFilesOptions) commandOptions)
                .setBidirectionalInteg(bidirectionalInteg);
    }

    /**
     * Sets the integrate around deleted revs.
     * 
     * @param integrateAroundDeletedRevs
     *            the new integrate around deleted revs
     */
    public void setIntegrateAroundDeletedRevs(boolean integrateAroundDeletedRevs) {
        ((IntegrateFilesOptions) commandOptions)
                .setIntegrateAroundDeletedRevs(integrateAroundDeletedRevs);
    }

    /**
     * Sets the rebranch source after delete.
     * 
     * @param rebranchSourceAfterDelete
     *            the new rebranch source after delete
     */
    public void setRebranchSourceAfterDelete(boolean rebranchSourceAfterDelete) {
        ((IntegrateFilesOptions) commandOptions)
                .setRebranchSourceAfterDelete(rebranchSourceAfterDelete);
    }

    /**
     * Sets the delete target after delete.
     * 
     * @param deleteTargetAfterDelete
     *            the new delete target after delete
     */
    public void setDeleteTargetAfterDelete(boolean deleteTargetAfterDelete) {
        ((IntegrateFilesOptions) commandOptions)
                .setDeleteTargetAfterDelete(deleteTargetAfterDelete);
    }

    /**
     * Sets the integrate all after re add.
     * 
     * @param integrateAllAfterReAdd
     *            the new integrate all after re add
     */
    public void setIntegrateAllAfterReAdd(boolean integrateAllAfterReAdd) {
        ((IntegrateFilesOptions) commandOptions)
                .setIntegrateAllAfterReAdd(integrateAllAfterReAdd);
    }

    /**
     * Sets the force integration.
     * 
     * @param forceIntegration
     *            the new force integration
     */
    public void setForceIntegration(boolean forceIntegration) {
        ((IntegrateFilesOptions) commandOptions)
                .setForceIntegration(forceIntegration);
    }

    /**
     * Sets the use have rev.
     * 
     * @param useHaveRev
     *            the new use have rev
     */
    public void setUseHaveRev(boolean useHaveRev) {
        ((IntegrateFilesOptions) commandOptions).setUseHaveRev(useHaveRev);
    }

    /**
     * Sets the do baseless merge.
     * 
     * @param doBaselessMerge
     *            the new do baseless merge
     */
    public void setDoBaselessMerge(boolean doBaselessMerge) {
        ((IntegrateFilesOptions) commandOptions)
                .setDoBaselessMerge(doBaselessMerge);
    }

    /**
     * Sets the display base details.
     * 
     * @param displayBaseDetails
     *            the new display base details
     */
    public void setDisplayBaseDetails(boolean displayBaseDetails) {
        ((IntegrateFilesOptions) commandOptions)
                .setDisplayBaseDetails(displayBaseDetails);
    }

    /**
     * Sets the show actions only.
     * 
     * @param showActionsOnly
     *            the new show actions only
     */
    public void setShowActionsOnly(boolean showActionsOnly) {
        ((IntegrateFilesOptions) commandOptions)
                .setShowActionsOnly(showActionsOnly);
    }

    /**
     * Sets the reverse mapping.
     * 
     * @param reverseMapping
     *            the new reverse mapping
     */
    public void setReverseMapping(boolean reverseMapping) {
        ((IntegrateFilesOptions) commandOptions)
                .setReverseMapping(reverseMapping);
    }

    /**
     * Sets the propagate type.
     * 
     * @param propagateType
     *            the new propagate type
     */
    public void setPropagateType(boolean propagateType) {
        ((IntegrateFilesOptions) commandOptions)
                .setPropagateType(propagateType);
    }

    /**
     * Sets the dont copy to client.
     * 
     * @param dontCopyToClient
     *            the new dont copy to client
     */
    public void setDontCopyToClient(boolean dontCopyToClient) {
        ((IntegrateFilesOptions) commandOptions)
                .setDontCopyToClient(dontCopyToClient);
    }

    /**
     * Sets the branch.
     * 
     * @param branch
     *            the new branch
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Sets the max files.
     * 
     * @param maxFiles
     *            the new max files
     */
    public void setMaxFiles(int maxFiles) {
        ((IntegrateFilesOptions) commandOptions).setMaxFiles(maxFiles);
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
     * Execute the Perforce integrate command with source and target file specs
     * and options. Log the returned file specs.
     * <p>
     * Integrate ("merge") from one Perforce filespec to another. The semantics
     * of Perforce merges are complex and are not explained here; please consult
     * the main Perforce documentation for file merges and the
     * IntegrateFilesOptions Javdoc comments for details of the
     * less-commonly-used options.
     * 
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fromFileSpec = new FileSpec(fromFile);
            toFileSpec = new FileSpec(toFile);
            retFileSpecs = getP4Client().integrateFiles(fromFileSpec,
                    toFileSpec, branch,
                    ((IntegrateFilesOptions) commandOptions));
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
