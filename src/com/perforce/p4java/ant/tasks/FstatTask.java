/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.List;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.FileStatAncilliaryOptions;
import com.perforce.p4java.core.file.FileStatOutputOptions;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.GetExtendedFilesOptions;

/**
 * Dumps information about each file, with each item of information on a
 * separate line. It is best used within a Perforce API application where the
 * items can be accessed as variables, but its output is also suitable for
 * parsing from the client command output. </p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class FstatTask extends ClientTask {

    // FileStat Options
    /**
     * Limits the output to files satisfying the expression given as 'filter'.
     * Example, fstat -Ol -F "fileSize > 1000000 & headType=text".
     */
    protected String filterString = null; // -F [filter]

    /** Limits output to the first 'max' number of files. */
    protected int maxResults = 0; // -m

    /** Sorts the output in reverse order. */
    protected boolean reverseSort = false; // -r

    /**
     * Instructs fstat to display only files affected since the given changelist
     * number.
     */
    protected int sinceChangelist = IChangelist.UNKNOWN; // -c

    /**
     * Instructs fstat to display only files affected by the given changelist
     * number.
     */
    protected int affectedByChangelist = IChangelist.UNKNOWN; // -e

    // FileStat Sort Options (-Sx options)
    /** Sort by filetype. */
    protected boolean sortByFiletype = false; // -St

    /** Sort by date. */
    protected boolean sortByDate = false; // -Sd

    /** Sort by head revision. */
    protected boolean sortByHeadRev = false; // -Sr

    /** Sort by have revision. */
    protected boolean sortByHaveRev = false; // -Sh

    /** Sort by filesize. */
    protected boolean sortByFileSize = false; // -Ss

    // FileStat Output Options (-Rx options)
    /** Files mapped through the client view. */
    private boolean mappedFiles = false; // -Rc

    /** Files synced to the client. */
    private boolean syncedFiles = false; // -Rh

    /** Files opened not at the head revision. */
    private boolean openedNotHeadRevFiles = false; // -Rn

    /** Files opened. */
    private boolean openedFiles = false; // -Ro

    /** Files opened that have been resolved. */
    private boolean openedResolvedFiles = false; // -Rr

    /** Files shelved (requires -e). */
    private boolean openedNeedsResolvingFiles = false; // -Ru

    /** Files opened that need resolving. */
    private boolean shelvedFiles = false; // -Rs

    // FileStat Ancilliary Options (-Ox options)
    /**
     * Output all revisions for the given files (this option suppresses other*
     * and resolve* fields).
     */
    private boolean allRevs = false; // -Of

    /**
     * Output a fileSize and digest field for each revision (this may be
     * expensive to compute).
     */
    private boolean fileSizeDigest = false; // -Ol

    /**
     * Output the local file path in both Perforce syntax (//client/) as
     * 'clientFile' and host form as 'path'.
     */
    private boolean bothPathTypes = false; // -Op

    /**
     * Output pending integration record information for files opened on the
     * current client.
     */
    private boolean pendingIntegrationRecs = false; // -Or

    /** Exclude client-related data from output. */
    private boolean excludeLocalPath = false; // -Os

    /**
     * Collection of extended file and revision specifiers returned from the
     * Perforce command.
     */
    protected List<IExtendedFileSpec> retExtendedFileSpecs;

    /**
     * Default constructor.
     */
    public FstatTask() {
        super();
        FileStatOutputOptions outputOptions = new FileStatOutputOptions(
                mappedFiles, syncedFiles, openedNotHeadRevFiles, openedFiles,
                openedResolvedFiles, openedNeedsResolvingFiles, shelvedFiles);
        FileStatAncilliaryOptions ancilliaryOptions = new FileStatAncilliaryOptions(
                allRevs, fileSizeDigest, bothPathTypes, pendingIntegrationRecs,
                excludeLocalPath);
        commandOptions = new GetExtendedFilesOptions(filterString, maxResults,
                reverseSort, sinceChangelist, affectedByChangelist,
                sortByFiletype, sortByDate, sortByHeadRev, sortByHaveRev,
                sortByFileSize, outputOptions, ancilliaryOptions);
    }

    /**
     * Sets the filter string.
     *
     * @param filterString
     *            the new filter string
     */
    public void setFilterString(String filterString) {
        ((GetExtendedFilesOptions) commandOptions)
                .setFilterString(filterString);
    }

    /**
     * Sets the max results.
     *
     * @param maxResults
     *            the new max results
     */
    public void setMaxResults(int maxResults) {
        ((GetExtendedFilesOptions) commandOptions).setMaxResults(maxResults);
    }

    /**
     * Sets the reverse sort.
     *
     * @param reverseSort
     *            the new reverse sort
     */
    public void setReverseSort(boolean reverseSort) {
        ((GetExtendedFilesOptions) commandOptions).setReverseSort(reverseSort);
    }

    /**
     * Sets the since changelist.
     *
     * @param sinceChangelist
     *            the new since changelist
     */
    public void setSinceChangelist(int sinceChangelist) {
        ((GetExtendedFilesOptions) commandOptions)
                .setSinceChangelist(sinceChangelist);
    }

    /**
     * Sets the affected by changelist.
     *
     * @param affectedByChangelist
     *            the new affected by changelist
     */
    public void setAffectedByChangelist(int affectedByChangelist) {
        ((GetExtendedFilesOptions) commandOptions)
                .setAffectedByChangelist(affectedByChangelist);
    }

    /**
     * Sets the sort by filetype.
     *
     * @param sortByFiletype
     *            the new sort by filetype
     */
    public void setSortByFiletype(boolean sortByFiletype) {
        ((GetExtendedFilesOptions) commandOptions)
                .setSortByFiletype(sortByFiletype);
    }

    /**
     * Sets the sort by date.
     *
     * @param sortByDate
     *            the new sort by date
     */
    public void setSortByDate(boolean sortByDate) {
        ((GetExtendedFilesOptions) commandOptions).setSortByDate(sortByDate);
    }

    /**
     * Sets the sort by head rev.
     *
     * @param sortByHeadRev
     *            the new sort by head rev
     */
    public void setSortByHeadRev(boolean sortByHeadRev) {
        ((GetExtendedFilesOptions) commandOptions)
                .setSortByHeadRev(sortByHeadRev);
    }

    /**
     * Sets the sort by have rev.
     *
     * @param sortByHaveRev
     *            the new sort by have rev
     */
    public void setSortByHaveRev(boolean sortByHaveRev) {
        ((GetExtendedFilesOptions) commandOptions)
                .setSortByHaveRev(sortByHaveRev);
    }

    /**
     * Sets the sort by file size.
     *
     * @param sortByFileSize
     *            the new sort by file size
     */
    public void setSortByFileSize(boolean sortByFileSize) {
        ((GetExtendedFilesOptions) commandOptions)
                .setSortByFileSize(sortByFileSize);
    }

    /**
     * Sets the mapped files.
     *
     * @param mappedFiles
     *            the new mapped files
     */
    public void setMappedFiles(boolean mappedFiles) {
        ((GetExtendedFilesOptions) commandOptions).getOutputOptions()
                .setMappedFiles(mappedFiles);
    }

    /**
     * Sets the synced files.
     *
     * @param syncedFiles
     *            the new synced files
     */
    public void setSyncedFiles(boolean syncedFiles) {
        ((GetExtendedFilesOptions) commandOptions).getOutputOptions()
                .setSyncedFiles(syncedFiles);
    }

    /**
     * Sets the opened not head rev files.
     *
     * @param openedNotHeadRevFiles
     *            the new opened not head rev files
     */
    public void setOpenedNotHeadRevFiles(boolean openedNotHeadRevFiles) {
        ((GetExtendedFilesOptions) commandOptions).getOutputOptions()
                .setOpenedNotHeadRevFiles(openedNotHeadRevFiles);
    }

    /**
     * Sets the opened files.
     *
     * @param openedFiles
     *            the new opened files
     */
    public void setOpenedFiles(boolean openedFiles) {
        ((GetExtendedFilesOptions) commandOptions).getOutputOptions()
                .setOpenedFiles(openedFiles);
    }

    /**
     * Sets the opened resolved files.
     *
     * @param openedResolvedFiles
     *            the new opened resolved files
     */
    public void setOpenedResolvedFiles(boolean openedResolvedFiles) {
        ((GetExtendedFilesOptions) commandOptions).getOutputOptions()
                .setOpenedResolvedFiles(openedResolvedFiles);
    }

    /**
     * Sets the opened needs resolving files.
     *
     * @param openedNeedsResolvingFiles
     *            the new opened needs resolving files
     */
    public void setOpenedNeedsResolvingFiles(boolean openedNeedsResolvingFiles) {
        ((GetExtendedFilesOptions) commandOptions).getOutputOptions()
                .setOpenedNeedsResolvingFiles(openedNeedsResolvingFiles);
    }

    /**
     * Sets the shelved files.
     *
     * @param shelvedFiles
     *            the new shelved files
     */
    public void setShelvedFiles(boolean shelvedFiles) {
        ((GetExtendedFilesOptions) commandOptions).getOutputOptions()
                .setShelvedFiles(shelvedFiles);
    }

    /**
     * Sets the all revs.
     *
     * @param allRevs
     *            the new all revs
     */
    public void setAllRevs(boolean allRevs) {
        ((GetExtendedFilesOptions) commandOptions).getAncilliaryOptions()
                .setAllRevs(allRevs);
    }

    /**
     * Sets the file size digest.
     *
     * @param fileSizeDigest
     *            the new file size digest
     */
    public void setFileSizeDigest(boolean fileSizeDigest) {
        ((GetExtendedFilesOptions) commandOptions).getAncilliaryOptions()
                .setFileSizeDigest(fileSizeDigest);
    }

    /**
     * Sets the both path types.
     *
     * @param bothPathTypes
     *            the new both path types
     */
    public void setBothPathTypes(boolean bothPathTypes) {
        ((GetExtendedFilesOptions) commandOptions).getAncilliaryOptions()
                .setBothPathTypes(bothPathTypes);
    }

    /**
     * Sets the pending integration recs.
     *
     * @param pendingIntegrationRecs
     *            the new pending integration recs
     */
    public void setPendingIntegrationRecs(boolean pendingIntegrationRecs) {
        ((GetExtendedFilesOptions) commandOptions).getAncilliaryOptions()
                .setPendingIntegrationRecs(pendingIntegrationRecs);
    }

    /**
     * Sets the exclude local path.
     *
     * @param excludeLocalPath
     *            the new exclude local path
     */
    public void setExcludeLocalPath(boolean excludeLocalPath) {
        ((GetExtendedFilesOptions) commandOptions).getAncilliaryOptions()
                .setExcludeLocalPath(excludeLocalPath);
    }

    /**
     * Gets the ret extended file specs.
     *
     * @return the ret extended file specs
     */
    public List<IExtendedFileSpec> getRetExtendedFileSpecs() {
        return retExtendedFileSpecs;
    }

    /**
     * Execute the Perforce fstat command with file specs and options. Log the
     * returned extended file specs.
     * <p>
     * Return a list of everything Perforce knows about a set of Perforce files.
     * <p>
     * This method is not intended for general use, and is not documented in
     * detail here; consult the main Perforce fstat command documentation for
     * detailed help.
     * <p>
     * This method can be a real server and bandwidth resource hog, and should
     * be used as sparingly as possible; alternatively, try to use it with as
     * narrow a set of file specs as possible.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retExtendedFileSpecs = getP4Server().getExtendedFiles(fileSpecs,
                    ((GetExtendedFilesOptions) commandOptions));
            logExtendedFileSpecs(retExtendedFileSpecs);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
